package org.dotspace.oofp.utils.expression;

import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.model.dto.expression.ExpressionOperation;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.tika.utils.ExceptionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.function.*;

@UtilityClass
public class ExpressionSteps {

    // =====================================================================
    //  1. Predicate Step：條件驗證
    // =====================================================================

    /**
     * 建立一個「條件驗證」型 BehaviorStep。
     *
     * <pre>
     * - 使用 op.predicate(...) 檢查 StepContext 是否通過條件
     * （SpEL root 即為 StepContext，可同時存取 payload 與 attributes）
     * - 若 predicate 為 true：Step 成功，原 StepContext 直接透傳
     * - 若 predicate 為 false：Step 失敗，回傳 Validation.invalid(onInvalid.get())
     * </pre>
     *
     * @param op          ExpressionOperation，描述 SpEL 條件
     * @param varsProvider 將 stepContext 映射為 SpEL 評估變數的函式
     * @param onInvalid   當條件不通過時，建立 Violations 的供應器
     */
    public <T> BehaviorStep<T> predicate(
            ExpressionOperation op, @NonNull Function<StepContext<T>, Map<String, Object>> varsProvider,
            @NonNull Supplier<Violations> onInvalid) {
        Predicate<StepContext<T>> predicate = op.test(varsProvider);

        return stepContext -> getValidStepContext(stepContext)
                .filter(predicate, onInvalid);
    }

    private <T> Validation<Violations, StepContext<T>> getValidStepContext(StepContext<T> stepContext) {
        try {
            return Validation.valid(stepContext);
        }
        catch (Exception ex) {
            return Validation.invalid(Violations.violate(
                    "verifyValidated.stack-trace", ExceptionUtils.getStackTrace(ex)));
        }
    }

    // =====================================================================
    //  2. Reader Step：讀取並寫入 StepContext attribute
    // =====================================================================

    /**
     * 建立一個「讀取 SpEL 結果並寫入 StepContext attribute」的 BehaviorStep。
     *
     * <pre>
     * - 使用 op.reader(...) 從 payload 計算出一個值 R
     * - null / 非法情況下, 清除 StepContext 的 attribute (withNoneAttribute)
     * - 寫入方式：stepContext.withAttribute(attributeName, value)
     * </pre>
     *
     * 若需要將 null 視為錯誤，建議搭配 {@link #validatorWithAttribute(ExpressionOperation, Function, String)} 使用。
     *
     * @param op            ExpressionOperation，描述 SpEL 映射
     * @param varsProvider  將 stepContext 映射為 SpEL 評估變數的函式
     * @param attributeName 寫入 StepContext 的 attribute 名稱
     */
    public <T, R> BehaviorStep<T> readerWithAttribute(
            ExpressionOperation op,
            @NonNull Function<StepContext<T>, Map<String, Object>> varsProvider,
            String attributeName) {
        Function<StepContext<T>, R> reader = op.read(varsProvider);

        return stepContext -> getValidStepContext(stepContext)
                .map(ctx -> Maybe.just(ctx)
                        .map(reader)
                        .filter(Objects::nonNull)
                        .map(v -> ctx.withAttribute(attributeName, v))
                        .orElse(ctx.withNoneAttribute(attributeName))
                );
    }

    // =====================================================================
    //  3. Writer Step：副作用／寫入 payload
    // =====================================================================

    /**
     * 建立一個「對 payload 執行 SpEL setValue 副作用」的 BehaviorStep。
     *
     * <pre>
     * - 使用 op.writer(...) 產生 BiConsumer&lt;StepContext&lt;T&gt;&gt;
     * - StepContext 本身（payload 及 attribute）不被修改，除非 SpEL 本身改變 payload 內容
     * - 實務上常用於：更新狀態欄位、執行審計記錄等邏輯
     * </pre>
     *
     * value 的來源由 valueProvider 提供，通常會依據當前 StepContext 狀態決定。
     *
     * @param op            ExpressionOperation，描述 SpEL setValue 邏輯
     * @param varsProvider  將 stepContext 映射為 SpEL 評估變數的函式
     * @param valueProvider 從 StepContext 計算出要寫入的值 V
     */
    public <T, V> BehaviorStep<T> write(
            ExpressionOperation op,
            @NonNull Function<StepContext<T>, Map<String, Object>> varsProvider,
            @NonNull Function<StepContext<T>, V> valueProvider) {
        Consumer<StepContext<T>> writer = op.write(varsProvider, valueProvider);

        return stepContext -> getValidStepContext(stepContext)
                .map(ctx -> {
                    Maybe.just(ctx).match(writer);
                    return ctx; // 結構不變，只做副作用
                });
    }

    // =====================================================================
    //  4. Validation-aware Step：帶 Validation 語意的映射
    // =====================================================================

    /**
     * 建立一個「帶 Validation 語意的 SpEL 映射 Step」，結果寫入 StepContext attribute。
     *
     * <pre>
     * - 使用 op.validationFunction(...) 取得 Function&lt;StepContext&lt;T&gt;&gt;, Validation&lt;Violations, R&gt;&gt;
     * - 若評估成功：將 R 寫入 attributeName，回傳 Validation.valid(nextContext)
     * - 若評估失敗：直接回傳 Validation.invalid(...)
     * </pre>
     *
     * 這個 Step 很適合用於：
     * - 對 SpEL 評估結果有「必須有效」的要求
     * - 希望 SpEL 失敗能自然融入整體 Validation error flow
     *
     * @param op            ExpressionOperation，描述 SpEL 映射邏輯
     * @param varsProvider  將 stepContext 映射為 SpEL 評估變數的函式
     * @param attributeName 寫入 StepContext 的 attribute 名稱
     */
    public <T, R> BehaviorStep<T> validatorWithAttribute(
            ExpressionOperation op,
            @NonNull Function<StepContext<T>, Map<String, Object>> varsProvider,
            String attributeName) {
        Function<StepContext<T>, Validation<Violations, R>> validator = op.validate(varsProvider);

        return stepContext -> {
            Validation<Violations, StepContext<T>> validStepContext = getValidStepContext(stepContext);
            return validStepContext.flatMap(validator)
                    .flatMap(value -> validStepContext.map(
                            ctx -> ctx.withAttribute(attributeName, value))
                    );
        };
    }

}
