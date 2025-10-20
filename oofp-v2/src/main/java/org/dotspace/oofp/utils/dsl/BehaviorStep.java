package org.dotspace.oofp.utils.dsl;

import lombok.NonNull;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.violation.joinable.Violations;

import java.util.List;
import java.util.Optional;
import java.util.function.*;

/**
 * DSL Template: Behavior Chain with Filtering, Side Effects, and Deferred Execution.
 * 適用於 Validation、Builder、Transformer、RuleEngine 等具備「步驟組裝、條件過濾、副作用插入、延後執行」的架構。
 */
public interface BehaviorStep<T>
        extends Function<StepContext<T>, Validation<Violations, StepContext<T>>> {

    static <T> BehaviorStep<T> of(
            Function<StepContext<T>, Validation<Violations, StepContext<T>>> function) {
        return function::apply;
    }

    /** 延遲取得 Step */
    static <T> BehaviorStep<T> supply(Supplier<BehaviorStep<T>> supplier) {
        return context -> supplier.get().apply(context);
    }

    static <T> BehaviorStep<T> chain(List<BehaviorStep<T>> steps) {
        BehaviorStep<T> result = Validation::valid;

        for (BehaviorStep<T> step : steps) {
            result = result.andThenStep(step);
        }
        return result;
    }

    static <T> BehaviorStep<T> when(
            Predicate<StepContext<T>> condition, BehaviorStep<T> step) {
        return input -> Optional.ofNullable(input)
                .filter(condition)
                .map(step)
                .orElse(Validation.valid(input));
    }

    default BehaviorStep<T> andThenStep(BehaviorStep<T> step) {
        return input -> apply(input).flatMap(step);
    }

    default BehaviorStep<T> andThenMapper(UnaryOperator<StepContext<T>> mapper) {
        return input -> apply(input).map(mapper);
    }

    /** 成功時過濾資料，否則加上違規 */
    default BehaviorStep<T> filter(
            Predicate<T> predicate, Function<T, Violations> violationProvider) {
        return input -> apply(input).flatMap(ctx -> {
            if (predicate.test(ctx.getPayload())) {
                return Validation.valid(ctx);
            }
            return Validation.invalid(
                    ctx.withViolation(violationProvider.apply(ctx.getPayload())));
        });
    }

    /**
     * 加入副作用觀察行為（僅在成功結果執行）。
     */
    default BehaviorStep<T> peek(Consumer<StepContext<T>> observer) {
        return input -> this.apply(input).peek(observer);
    }

    /**
     * 加入錯誤觀察（僅在錯誤結果執行）。
     */
    default BehaviorStep<T> peekOnError(Consumer<Violations> handler) {
        return input -> {
            Validation<Violations, StepContext<T>> result = this.apply(input);
            return result.peekError(handler);
        };
    }

    default BehaviorStep<T> recover(@NonNull Function<Violations, T> recoveryFunction) {
        return context -> this.apply(context)
                .fold(violations ->
                        Maybe.given(recoveryFunction.apply(violations))
                                .fold(x -> new Validation.Valid<>(
                                        StepContext.<T>builder()
                                                .withPayload(x)
                                                .withViolations(Violations.empty())
                                                .build()),
                                        () -> new Validation.Invalid<>(violations)
                        ), Validation.Valid::new
                );
    }
}
