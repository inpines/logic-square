package org.dotspace.oofp.utils.functional.monad.validation;

import org.dotspace.oofp.utils.dsl.Joinable;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

@UtilityClass
public class ValidationUtils {

    /**
     * 將多個 Validation&lt;E, T&gt; 合併：
     * - 若全部 valid：用 collector 收斂成單一 R
     * - 若有任一 invalid：使用 E.join(...) 將所有錯誤合併成一個 E，回傳 invalid
     * 這是一種 Applicative-style 的 combine。
     */
    @SafeVarargs
    public <E extends Joinable<E>, T, R> Validation<E, R> mergeAll(@NonNull Collector<T, ?, R> collector,
            Validation<E, T>... validations) {

        class Context {
            final List<T> values = new ArrayList<>();
            Maybe<E> errors = Maybe.empty();

            void addError(@NonNull E error) {
                // 若已有錯誤，將新的錯誤與既有錯誤 join
                errors = errors
                        .map(existing -> existing.join(error))
                        .or(Maybe.just(error));
            }

            void addValue(@NonNull T value) {
                values.add(value);
            }

        }

        Context context = new Context();
        for (Validation<E, T> validation : validations) {
            // 先看這一筆有沒有 error
            validation.error().match(
                    // 有錯誤：合併進 Context.errors
                    context::addError
                    // 沒錯誤：把成功的值放進 Context.values
                    , () -> validation.get().match(
                            context::addValue,
                            () -> { /* 不應該發生
                                代表 Validation 既沒有值也沒有錯誤 */
                                throw new IllegalStateException("Validation has neither value nor error");
                            }
                    )
            );
        }

        // 有錯誤 → invalid(合併後的錯誤)
        // 無錯誤 → valid(collector 收斂後的結果)
        return context.errors
                .map(Validation::<E, R>invalid)
                .orElseGet(() -> Validation.valid(
                        context.values.stream().collect(collector)
                ));
    }

    /**
     * 將 Map&lt;String, Validation&lt;E, ?&gt;&gt; 合併：
     * - 若全部 valid：回傳 Map&lt;String, Object&gt;，key 對應各欄位值
     * - 若有任一 invalid：使用 E.join(...) 將所有錯誤合併成一個 E，回傳 invalid
     */
    public <E extends Joinable<E>> Validation<E, Map<String, Object>> mergeAll(
            Map<String, Validation<E, ?>> validations) {

        class Context {
            final Map<String, Object> values = new HashMap<>();
            Maybe<E> errors = Maybe.empty();

            void addError(@NonNull E error) {
                errors = errors
                        .map(existing -> existing.join(error))
                        .or(Maybe.just(error));
            }

            void addValue(@NonNull String name, @NonNull Object value) {
                values.put(name, value);
            }
        }

        Context context = new Context();
        for (var entry : validations.entrySet()) {
            String key = entry.getKey();
            Validation<E, ?> validation = entry.getValue();

            validation.error().match(
                    // 有錯誤：合併錯誤
                    context::addError
                    , // 沒錯誤：把 value 放進 map
                    () -> validation.get().match(
                            value -> context.addValue(key, value),
                            () -> { /* 同樣是理論上的 empty case */
                                throw new IllegalStateException("Validation has neither value nor error");
                            }
                    )
            );

        }

        return context.errors
                .map(Validation::<E, Map<String, Object>>invalid)
                .orElseGet(() -> Validation.valid(context.values));
    }

    /**
     * 幫忙把 value 包成 valid，讓呼叫端不用直接依賴 Validation 的靜態方法。
     */
    public <E extends Joinable<E>, T> Validation<E, T> valid(T value) {
        return Validation.valid(value);
    }

}
