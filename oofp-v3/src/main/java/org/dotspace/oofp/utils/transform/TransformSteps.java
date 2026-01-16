package org.dotspace.oofp.utils.transform;

import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.utils.builder.operation.WriteOperation;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.utils.eip.AttrKey;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class TransformSteps {

    public static final Function<RuntimeException, Violations> DEFAULT_VIOLATIONS_PROVIDER =
            e -> Violations.violate("transform-steps.exception-message", e.getMessage());

    @FunctionalInterface
    public interface Reader<S, O> {
        O read(S source);
    }

    /** 固定 writers 版本 */
    public <T, I, R> BehaviorStep<T> transform(
            @NonNull Reader<StepContext<T>, I> sourceReader,
            @NonNull Reader<I, R> resultReader,
            @NonNull WriteOperation<R> writerOp,
            @NonNull AttrKey<R> resultKey) {

        return transformOnce(sourceReader, resultReader, () -> writerOp, resultKey);
    }

    public <T, I, R> BehaviorStep<T> transformOnce(
            @NonNull Reader<StepContext<T>, I> sourceReader,
            @NonNull Reader<I, R> resultReader,
            @NonNull Supplier<WriteOperation<R>> writerSupplier,
            @NonNull AttrKey<R> resultKey) {

        var ref = new AtomicReference<WriteOperation<R>>();

        final Supplier<WriteOperation<R>> memoized = () ->
                ref.updateAndGet(prev -> prev != null ? prev :
                        Objects.requireNonNull(
                                writerSupplier.get(), "writerSupplier.get() must not return null")
                );

        return transform(sourceReader, resultReader, memoized, resultKey, DEFAULT_VIOLATIONS_PROVIDER);
    }

    public <T, I, R> BehaviorStep<T> transform(
            @NonNull Reader<StepContext<T>, I> sourceReader,
            @NonNull Reader<I, R> resultReader,
            @NonNull Supplier<WriteOperation<R>> writerSupplier,
            @NonNull AttrKey<R> resultKey,
            @NonNull Function<RuntimeException, Violations> violationsProvider) {
        return ctx -> {
            try {
                var info = sourceReader.read(ctx);
                var result = resultReader.read(info);

                var writer = Objects.requireNonNull(
                        writerSupplier.get(), "writerSupplier.get() must not return null");
                writer.write(result);
                return Validation.valid(ctx.withAttribute(resultKey, result));
            } catch (RuntimeException e) {
                return Validation.invalid(violationsProvider.apply(e));
            }
        };
    }

    public <T, R> BehaviorStep<T> transform(
            @NonNull Reader<StepContext<T>, R> resultReader,
            @NonNull WriteOperation<R> writerOp,
            @NonNull AttrKey<R> resultKey) {

        return transform(
                ctx -> ctx,          // sourceReader
                resultReader,   // resultReader
                writerOp,
                resultKey
        );
    }

}
