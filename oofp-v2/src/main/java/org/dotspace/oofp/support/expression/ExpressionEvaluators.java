package org.dotspace.oofp.support.expression;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import lombok.extern.slf4j.Slf4j;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.utils.dsl.StepContext;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.violation.joinable.Violations;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Component("expressionEvaluators")
@Slf4j
public class ExpressionEvaluators {

    public static final String VALUE = "value";
    public static final String EVALUATION_FAILED = "evaluation failed!";
    private static final Pattern VAR_PATTERN = Pattern.compile("#(\\w+)");

    private ExpressionEvaluations expressionEvaluations;

    public <T, R> Function<T, R> readerOf(String expression) {
        return t -> maybeParse(expression)
                    .flatMap(ee -> this.<T, R>maybeEvaluate(ee, t))
                    .orElse(null);
    }

    public <T, R> Function<T, Optional<R>> optionalReaderOf(String expression) {
        return t -> maybeParse(expression)
                .flatMap(ee -> this.<T, R>maybeEvaluate(ee, t))
                .unwrap();
    }

    public <T, R> Function<T, R> readerOfOrThrow(String expression, Supplier<RuntimeException> supplier) {
        return t -> maybeParse(expression)
                .flatMap(ee -> this.<T, R>maybeEvaluate(ee, t))
                .orElseThrow(supplier);
    }

    public <T, E extends RuntimeException> void validateOrThrow(
            String expression, T info, Predicate<Object> predicate, Supplier<E> supplier) {
        maybeParse(expression)
                .flatMap(ee -> maybeEvaluate(ee, Map.of("info", info)))
                .filter(predicate)
                .orElseThrow(supplier);
    }

    public <T, E extends RuntimeException> void validateOrThrow(
            String expression, Map<String, Object> variables, String predicateExpression,
            Supplier<E> supplier) {
        maybeParse(expression)
                .<T>flatMap(ee -> maybeEvaluate(ee, variables))
                .filter(t -> Optional.of(t)
                        .filter(this.predicateOf(predicateExpression))
                        .isPresent())
                .orElseThrow(supplier);
    }

    public <T> Validation<Violations, T> validate(
            String expression, Map<String, Object> variables, Predicate<Object> predicate,
            Supplier<Violations> supplier) {
        return maybeParse(expression)
                .<T>flatMap(ee -> maybeEvaluate(ee, variables))
                .filter(predicate)
                .map(Validation::<Violations, T>valid)
                .orElse(Validation.invalid(supplier.get()));
    }
    public <T> Validation<Violations, T> validate(
            String expression, Map<String, Object> variables, String predicateExpression,
            Supplier<Violations> supplier) {
        return maybeParse(expression)
                .<T>flatMap(ee -> this.maybeEvaluate(ee, variables))
                .filter(t -> Optional.of(t)
                        .filter(this.predicateOf(predicateExpression))
                        .isPresent())
                .map(Validation::<Violations, T>valid)
                .orElse(Validation.invalid(supplier.get()));
    }

    private Maybe<ExpressionEvaluation> maybeParse(String expression) {
        try {
            return Maybe.given(expression)
                    .map(expressionEvaluations::parse);
        } catch (Exception e) {
            return Maybe.empty();
        }

    }

    private <T, R> Maybe<R> maybeEvaluate(ExpressionEvaluation expressionEvaluation, T x) {
        return maybeEvaluate(expressionEvaluation, Map.of(), x);
    }

    private <T, R> Maybe<R> maybeEvaluate(
            ExpressionEvaluation expressionEvaluation, @NonNull Map<String, Object> variables, T x) {
        return Maybe.just(variables)
                .filter(Predicate.not(Map::isEmpty))
                .map(vars -> expressionEvaluation.<R>getValueWithVariables(vars, x))
                .or(Maybe.given(expressionEvaluation.getValue(x)));
    }

    public <T, V> BiConsumer<T, V> supplyWriter(
            String expression, BiFunction<Exception, String, RuntimeException> exceptionGenerator) {
        return supplyWriter(expression, e -> {
            throw exceptionGenerator.apply(e, expression);
        });
    }

    public <T, V> BiConsumer<T, V> supplyWriter(
            String expression, Consumer<Exception> exceptionHandler) {
        return (t, v) -> {
            try {
                Map<String, Object> variables = Maybe.given(expression)
                        .map(expr -> {
                            Matcher matcher = VAR_PATTERN.matcher(expr);
                            return matcher.find() ? matcher.group(1) : null;
                        })
                        .map(name -> Map.<String, Object>of(name, v))
                        .orElse(Map.of());

                maybeParse(expression)
                        .match(eval -> {
                            if (variables.isEmpty()) {
                                eval.setValue(t, v);
                            } else {
                                eval.setValue(variables, t, v);
                            }
                        }, () -> {
                            throw new RuntimeException("寫入表達式有誤: 無法解析表達式");
                        });
            } catch (Exception e) {
                exceptionHandler.accept(e);
            }
        };
    }

    public <T> Predicate<T> predicateOf(String expression) {
        return t -> {
            Map<String, Object> variables = Map.of(VALUE, t);
            return maybeParse(expression)
                    .map(ee -> evaluateBoolean(ee, variables))
                    .map(Boolean.TRUE::equals)
                    .orElse(false);
             };

    }

    private Boolean evaluateBoolean(ExpressionEvaluation ee, Map<String, Object> variables) {
        return ee.<Boolean>getValueWithVariables(variables);
    }

    public <T, R> Function<T, R> functionOf(String expression) {
        return t -> {
            Map<String, Object> variables = Map.of(VALUE, t);
            return maybeParse(expression)
                    .map(eval -> eval.<R>getValueWithVariables(variables))
                    .orElse(null);
        };
    }

    public <T, R> Function<T, Optional<R>> optionalFunctionOf(String expression) {
        return t -> {
            Map<String, Object> variables = Map.of(VALUE, t);
            return maybeParse(expression)
                    .map(eval -> eval.<Optional<R>>getValueWithVariables(
                            variables))
                    .orElse(Optional.empty());
        };
    }

    public <T, A, R> Collector<T, A, R> collectorOf(String expression) {
        return maybeParse(expression)
                .map(ExpressionEvaluation::<Collector<T, A, R>>getValue)
                .orElse(null);
    }

    public <T, A, R> Optional<Collector<T, A, R>> maybeGetCollector(String expression) {
        return maybeParse(expression)
                .map(ExpressionEvaluation::<Collector<T, A, R>>getValue)
                .unwrap();
    }

    public <T> BehaviorStep<T> evaluate(String expression, String resultName) {
        return stepContext -> maybeStepContextPayload(stepContext)
                        .flatMap(t -> maybeParse(expression)
                                .flatMap(ee -> maybeEvaluate(ee, Map.of(VALUE, t))))
                        .map(r -> stepContext.withAttribute(resultName, r))
                        .toValidation(Violations.violate(String.format("set %s = %s", resultName, expression),
                                EVALUATION_FAILED));
    }

    public <T, I> BehaviorStep<T> evaluateWithStepContext(
            String expression, @NonNull Map<String, Object> variables, String resultName,
            Function<StepContext<T>, I> reader) {
        return stepContext -> {
            Maybe<I> value = maybeStepContext(stepContext).map(reader);

            Map<String, Object> vars = Stream.concat(
                    variables.entrySet().stream(),
                    Stream.of(Map.entry(VALUE, value.orElse(null)))
            ).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

            Maybe<ExpressionEvaluation> evaluation = maybeParse(expression);
            return maybeStepContextPayload(stepContext)
                    .flatMap(t -> evaluation.flatMap(
                            ee -> this.maybeEvaluate(ee, vars))
                    )
                    .map(r -> stepContext.withAttribute(resultName, r))
                    .toValidation(Violations.violate(String.format("set %s = %s, (variables = %s)",
                            resultName, expression, vars), EVALUATION_FAILED)
                    );
        };
    }

    public <T> BehaviorStep<T> evaluate(
            String expression, Map<String, Object> variables, String resultName) {
        return stepContext -> maybeStepContextPayload(stepContext)
                .flatMap(t -> maybeParse(expression).flatMap(
                        ee -> maybeEvaluate(ee, variables))
                )
                .map(r -> stepContext.withAttribute(resultName, r))
                .toValidation(Violations.violate(String.format("set %s = %s, (variables = %s)",
                        resultName, expression, variables), EVALUATION_FAILED));
    }

    private <T> Maybe<T> maybeStepContextPayload(StepContext<T> stepContext) {
        return maybeStepContext(stepContext)
                .map(StepContext::getPayload);
    }

    private <T> Maybe<StepContext<T>> maybeStepContext(StepContext<T> stepContext) {
        return Maybe.given(stepContext);
    }

}
