package org.dotspace.oofp.model.dto.expression;

import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.support.expression.ExpressionEvaluation;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.utils.builder.operation.WriteOperation;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

@Slf4j
public record ExpressionOperation(String expression, ExpressionEvaluations expressionEvaluations) {

    public <T> Predicate<T> test(Function<T, Map<String, Object>> varsProvider) {
        return t -> {
            var validation = validateEvaluation()
                    .flatMap(eval -> Validation.<Violations, T>valid(t)
                            .map(varsProvider)
                            .map(eval::<Boolean>getValueWithVariables));
            if (validation.isInvalid()) {
                String messages = getInvalidViolationsMessage(validation);
                throw new IllegalStateException(messages);
            }

            return validation.get()
                    .orElseThrow(() -> new IllegalStateException("Predicate evaluation is empty"));
        };
    }

    private Validation<Violations, ExpressionEvaluation> validateEvaluation() {
        try {
            return Maybe.given(expressionEvaluations.evaluate(expression))
                    .toValidation(Violations.violate(
                            "expression.evaluation.error",
                            "Failed to evaluate expression: " + expression));
        } catch (RuntimeException e) {
            log.error("Expression evaluation error for expression: {}", expression, e);
            return Validation.invalid(Violations.violate("expression.evaluation.exception",
                    "Exception during expression evaluation: " + e.getMessage()));
        }
    }

    public <T, R> Function<T, R> read(Function<T, Map<String, Object>> varsProvider) {
        return t -> {
            var validation = validateEvaluation()
                    .flatMap(eval -> Validation.<Violations, T>valid(t)
                            .map(varsProvider)
                            .map(eval::<R>getValueWithVariables));

            if (validation.isInvalid()) {
                String messages = getInvalidViolationsMessage(validation);
                throw new IllegalStateException(messages);
            } else {
                return validation.get()
                        .orElseThrow(() -> new IllegalStateException("Reader evaluation is empty"));
            }
        };
    }

    private <R> String getInvalidViolationsMessage(Validation<Violations, R> validation) {
        return validation.error()
                .map(Violations::collectMessages)
                .orElseThrow(() -> new RuntimeException("Unknown validation error"));
    }

    public <T, V> BiConsumer<T, V> write(Function<T, Map<String, Object>> varsProvider) {
        return (t, v) -> {
            var validation = validateEvaluation();

            if (validation.isInvalid()) {
                String msg = getInvalidViolationsMessage(validation);
                throw new IllegalStateException(msg);
            }

            Maybe<ExpressionEvaluation> value = validation.get();
            if (value.isEmpty()) {
                throw  new IllegalStateException("Writer evaluation id empty");
            }

            value.match(eval -> {
                        Map<String, Object> vars = maybeVariables(varsProvider, t);
                        eval.setValue(vars, t, v);
                    });
        };
    }

   public <T, V> WriteOperation<T> writeOp(
            Function<T, Map<String, Object>> varsProvider,
            Function<T, V> valueProvider) {
        return WriteOperation.from(write(varsProvider), valueProvider);
    }

    private <T> Map<String, Object> maybeVariables(Function<T, Map<String, Object>> varsProvider, T t) {
        return Maybe.given(t)
                .map(varsProvider)
                .orElse(new HashMap<>());
    }

    public <T, V> Consumer<T> write(
            Function<T, Map<String, Object>> varsProvider, Function<T, V> valueProvider) {
        return t -> {
            var validation = validateEvaluation();

            if (validation.isInvalid()) {
                var msg = getInvalidViolationsMessage(validation);
                throw new IllegalStateException(msg);
            }

            var value = validation.get();
            if (value.isEmpty()) {
                throw  new IllegalStateException("Writer evaluation id empty");
            }

            value.match(eval -> {
                Map<String, Object> vars = maybeVariables(varsProvider, t);
                Maybe.just(t)
                        .map(valueProvider)
                        .match(v -> eval.setValue(vars, t, v));
            });
        };
    }

    public <T, R> Function<T, Validation<Violations, R>> validate(
            Function<T, Map<String, Object>> varsProvider) {
        return t -> validateEvaluation()
                .flatMap(eval -> Validation.<Violations, T>valid(t)
                        .map(varsProvider)
                        .map(eval::<R>getValueWithVariables));
    }

}
