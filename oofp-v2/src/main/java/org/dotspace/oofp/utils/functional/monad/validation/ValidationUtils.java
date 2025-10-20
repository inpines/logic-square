package org.dotspace.oofp.utils.functional.monad.validation;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.violation.Joinable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

@UtilityClass
public class ValidationUtils {


    @SafeVarargs
    public <E extends Joinable<E>, T, R> Validation<E, R> mergeAll(
            @NonNull Collector<T, ?, R> collector, Validation<E, T>... validations) {

        class Context {
            final List<T> values = new ArrayList<>();
            Maybe<E> errors = Maybe.empty();

            void warp(@NonNull E error) {
                errors = Maybe.just(error);
            }

            void add(@NonNull T value) {
                values.add(value);
            }

        }

        Context context = new Context();
        for (Validation<E, T> validation : validations) {
            Maybe<E> error = validation.error();
            context.errors.match(existing -> error.match(existing::join)
                    // If there is an existing error, join it with the new error
                    , () -> error.match(context::warp
                            // If there is no existing error, wrap the new error
                            , () -> validation.get().match(context::add)
                            // If the validation is valid, add the value to the list
                    )
            );
        }

        // If there are no errors, collect the values into the desired type
        // Otherwise, merge the errors into a single error
        // and return an invalid Validation
        // Note: The collector is used to combine the values into the desired type
        return context.errors.map(Validation::<E, R>invalid)
                .orElseGet(() -> Validation.valid(context.values.stream().collect(collector)));
    }

    public <E extends Joinable<E>> Validation<E, Map<String, Object>> mergeAll(
            Map<String, Validation<E, ?>> validations) {

        class Context {
            final Map<String, Object> values = new HashMap<>();
            Maybe<E> errors = Maybe.empty();

            void warp(@NonNull E error) {
                errors = Maybe.just(error);
            }

            void add(@NonNull String name, @NonNull Object value) {
                values.put(name, value);
            }
        }

        Context context = new Context();
        for (var entry : validations.entrySet()) {
            String key = entry.getKey();
            Validation<E, ?> validation = entry.getValue();
            Maybe<E> error = validation.error();

            context.errors.match(existing -> error.match(existing::join)
                    // If there is an existing error, join it with the new error
                    , () -> error.match(context::warp
                            // If there is no existing error, wrap the new error
                            , () -> validation.get().match(value -> context.add(key, value))
                            // If the validation is valid, add the value to the list
                    )
            );

        }

        return context.errors.map(Validation::<E, Map<String, Object>>invalid)
                .orElseGet(() -> Validation.valid(context.values));
    }

    public <E extends Joinable<E>, T> Validation<E, T> valid(T value) {
        return Validation.valid(value);
    }

}
