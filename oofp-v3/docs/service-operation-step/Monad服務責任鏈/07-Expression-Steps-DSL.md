
---

# 📘 `CH7 — ExpressionSteps：SpEL 行為步驟模型`

完整技術版

在前一章（`CH6`）我們談的是「`ExpressionOperation` 把 `SpEL` 變成一級操作子」的理念。  
本章開始接上 **實作層**：

> **`ExpressionSteps` 就是「把 `ExpressionOperation` 轉成 `BehaviorStep` 的工廠」。**

也就是：

```text
ExpressionOperation   --(ExpressionSteps)-->  BehaviorStep<T>
BehaviorStep<T>       ≒  StepContext<T> -> Validation<Violations, StepContext<T>>
```

它讓 `SpEL` 不只是「被呼叫一次的字串」，而是能放進 `BehaviorPipeline` 裡、  
像一般 Step 一樣被編排、重用、測試與組合的行為單元。

---

## 7.1 `BehaviorStep` 的角色：行為層 Step

在你的實作裡，`ExpressionSteps` 回傳的是：

```java
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
        
```

也就是說：

- **所有 `ExpressionSteps` 產出的 Step 都是 `BehaviorStep`**
    
- 每個 `BehaviorStep` 接收一個 `StepContext<T>`
    
- 回傳的是 `Validation<Violations, StepContext<T>>`
    

完全符合我們前幾章建立的主軸：

> **「Step = Context → Validation<Violations, Context>」**

而 ExpressionSteps 的任務，就是把：

- `ExpressionOperation`（描述 SpEL 的操作）
    
- `varsProvider`（從 StepContext 產出 SpEL 變數 Map）
    
- `onInvalid` / `attributeName` / `valueProvider` 等語意資訊
    

組合成一個 `BehaviorStep`。

---

## 7.2 共用守門人：`getValidStepContext`

先看一個所有 Step 都共用的小工具：

```java
private <T> Validation<Violations, StepContext<T>> getValidStepContext(StepContext<T> stepContext) {
    try {
        return Validation.valid(stepContext);
    }
    catch (Exception ex) {
        return Validation.invalid(Violations.violate(
                "verifyValidated.stack-trace", ExceptionUtils.getStackTrace(ex)));
    }
}
```

設計語意：

1. **行為保證**：  
    `BehaviorStep` 在進入 `SpEL` evaluation 前，先把 `StepContext` 包成 `Validation.valid(ctx)`。
    
2. **意外錯誤保護**：  
    若中途（理論上極少見）拋出例外，會被轉為：
    
    ```java
    Validation.invalid(
        Violations.violate("verifyValidated.stack-trace", stackTrace)
    )
    ```
    
    也就是：**任何不可預期的例外不會把 pipeline 打爆，而是回到 Validation 錯誤通道。**
    

所以你可以把 `getValidStepContext()` 想成：

> 「保證 `BehaviorStep` 的起點永遠是一個 `Validation<Violations, StepContext<T>>`，  
> 而不是裸奔的 `StepContext<T>`。」

接下來所有 Step 都是從這裡開始。

---

## 7.3 Predicate Step：條件驗證（條件守門員）

程式碼：

```java
public <T> BehaviorStep<T> predicate(
        ExpressionOperation op,
        @NonNull Function<StepContext<T>, Map<String, Object>> varsProvider,
        @NonNull Supplier<Violations> onInvalid) {

    Predicate<StepContext<T>> predicate = op.predicate(varsProvider);

    return stepContext -> getValidStepContext(stepContext)
            .filter(predicate, onInvalid);
}
```

語意分解：

1. `op.predicate(varsProvider)`
    
    - `ExpressionOperation` 依據 SpEL 建立一個  
        `Predicate<StepContext<T>>`。
        
    - `varsProvider` 負責把 `StepContext<T>` 轉成 SpEL 評估用變數 Map。
        
    - SpEL 的 root 通常就是 `StepContext` 本身，變數則由 varsProvider 提供。
        
2. BehaviorStep 的實際行為：
    
    ```java
    stepContext -> getValidStepContext(stepContext)
            .filter(predicate, onInvalid);
    ```
    
    - 若目前是 `Valid(ctx)` 且 `predicate.test(ctx) == true` ⇒ 保持 `Valid(ctx)`
        
    - 若 `predicate.test(ctx) == false` ⇒ 回傳 `Validation.invalid(onInvalid.get())`
        
3. `onInvalid`
    
    - 由呼叫端決定條件不通過時要產生哪種 `Violations`，
        
    - 可以內含 `ViolationSeverity`（例如 ERROR / FATAL）。
        

**使用情境：**

- SpEL 型條件守門：
    
    - 帳號是否啟用：`payload.user.active == true`
        
    - 金額是否在安全範圍：`payload.amount < maxAmount`
        
    - Feature flag：`#vars['featureXEnabled'] == true`
        
- 實務上你會這樣用（概念示意）：
    
    ```java
    BehaviorStep<OrderPayload> amountGuard =
        expressionSteps.predicate(
            exprOps.of("payload.amount <= #vars['maxAmount']"),
            ctx -> Map.of("maxAmount", 10000),
            () -> Violations.violate("amount.exceed", "超過限額")
        );
    ```
    

---

## 7.4 Reader Step：讀取 `SpEL` 結果，寫入 `StepContext` attribute

程式碼：

```java
public <T, R> BehaviorStep<T> readerWithAttribute(
        ExpressionOperation op,
        @NonNull Function<StepContext<T>, Map<String, Object>> varsProvider,
        String attributeName) {

    Function<StepContext<T>, R> reader = op.reader(varsProvider);

    return stepContext -> getValidStepContext(stepContext)
            .map(ctx -> Maybe.just(ctx)
                    .map(reader)
                    .filter(Objects::nonNull)
                    .map(v -> ctx.withAttribute(attributeName, v))
                    .orElse(ctx.withNoneAttribute(attributeName))
            );
}
```

語意分解：

1. `op.reader(varsProvider)`
    
    - 基於 SpEL 建立一個  
        `Function<StepContext<T>, R>`。
        
    - 在實作內部會透過 `ExpressionEvaluation.getValue(...)` 從 StepContext（或其 payload/attributes）計算出一個值。
        
2. BehaviorStep 的行為：
    
    ```java
    getValidStepContext(stepContext)
        .map(ctx -> Maybe.just(ctx)
                .map(reader)
                .filter(Objects::nonNull)
                .map(v -> ctx.withAttribute(attributeName, v))
                .orElse(ctx.withNoneAttribute(attributeName))
        );
    ```
    
    - 對 `Valid(ctx)`：
        
        - 呼叫 reader(ctx) 得到一個值 `R`（可能為 null）
            
        - 若非 null：`ctx.withAttribute(attributeName, v)`
            
        - 若為 null：`ctx.withNoneAttribute(attributeName)`（清除該 attribute）
            
    - 對 `Invalid(...)`：map 不會執行 reader，錯誤原封不動傳遞。
        

**語意重點：**

- **null 不被視為錯誤**，而是「清掉 attribute」。
    
    - 真正要把 null 視為錯誤時，應搭配 `validatorWithAttribute` 使用（下一節）。
        

**使用情境：**

- 把 SpEL 計算結果放進 attributes：
    
    - 計算折扣後金額：`payload.price * payload.discount`
        
    - 計算風險分數
        
    - 統計欄位加總等
        

---

## 7.5 Writer Step：`SpEL setValue 副作用`

程式碼：

```java
public <T, V> BehaviorStep<T> writer(
        ExpressionOperation op,
        @NonNull Function<StepContext<T>, Map<String, Object>> varsProvider,
        @NonNull Function<StepContext<T>, V> valueProvider) {

    Consumer<StepContext<T>> writer = op.writer(varsProvider, valueProvider);

    return stepContext -> getValidStepContext(stepContext)
            .map(ctx -> {
                Maybe.just(ctx).match(writer);
                return ctx; // 結構不變，只做副作用
            });
}
```

語意分解：

1. `op.writer(varsProvider, valueProvider)`
    
    - 產生一個 `Consumer<StepContext<T>>`
        
    - 實際會透過 `ExpressionEvaluation.setValue(...)` 對 payload 或其欄位執行 setValue。
        
    - `valueProvider`：根據當前 `StepContext<T>` 決定要寫入的值 V（非 SpEL 本身提供，這點很重要）。
        
2. BehaviorStep 行為：
    
    ```java
    getValidStepContext(stepContext)
        .map(ctx -> {
            Maybe.just(ctx).match(writer);
            return ctx;
        });
    ```
    
    - 對 `Valid(ctx)`：執行 writer（副作用），然後仍回傳 `Valid(ctx)`。
        
    - 不改變 Validation 的成功／失敗狀態。
        
    - 不改變 StepContext 的引用（但 payload 內容可能被 SpEL 修改）。
        

**語意重點：**

- Writer 是 **純副作用型 Step**。
    
- 適合用在：
    
    - 設定狀態欄位（例如 `payload.status = 'APPROVED'`）
        
    - 設定標誌（flag）
        
    - 更新某些記錄欄位
        

**典型使用方式：**

```java
BehaviorStep<OrderPayload> markAsVip =
    expressionSteps.writer(
        exprOps.of("payload.vip = #value"),
        ctx -> Map.of(),               // 若 SpEL 需要其他變數可傳入
        ctx -> true                    // valueProvider：寫入 true
    );
```

---

## 7.6 Validation-aware Step：`validatorWithAttribute`

程式碼：

```java
public <T, R> BehaviorStep<T> validatorWithAttribute(
        ExpressionOperation op,
        @NonNull Function<StepContext<T>, Map<String, Object>> varsProvider,
        String attributeName) {

    Function<StepContext<T>, Validation<Violations, R>> validationFunction =
            op.validationFunction(varsProvider);

    return stepContext -> {
        Validation<Violations, StepContext<T>> validStepContext = getValidStepContext(stepContext);
        return validStepContext.flatMap(validationFunction)
                .flatMap(value -> validStepContext.map(
                        ctx -> ctx.withAttribute(attributeName, value))
                );
    };
}
```

這段是整個 ExpressionSteps 中**語意最精巧**的一段。

來分解：

1. `op.validationFunction(varsProvider)`
    
    - 產生 `Function<StepContext<T>, Validation<Violations, R>>`
        
    - SpEL 評估失敗會直接回 `Validation.invalid(Violations)`，  
        而非回 null 或丟例外。
        
    - 評估成功則回 `Validation.valid(R)`。
        
2. BehaviorStep 的流程：
    
    ```java
    Validation<Violations, StepContext<T>> validStepContext = getValidStepContext(stepContext);
    
    return validStepContext.flatMap(validationFunction)
            .flatMap(value -> validStepContext.map(
                    ctx -> ctx.withAttribute(attributeName, value))
            );
    ```
    
    - 先確保有 `validStepContext` 作為起點。
        
    - 第一次 `flatMap(validationFunction)`：
        
        - 若當前為 Valid(ctx) ⇒ 執行 SpEL 驗證與映射 ⇒ 得到 `Validation<Violations, R>`
            
        - 若 SpEL 評估失敗 ⇒ 在這一步直接變成 `Invalid(Violations)`
            
    - 第二次 `flatMap(value -> ...)`：
        
        - 只有在第一步 Valid(R) 的情況下才會執行
            
        - 透過 `validStepContext.map(ctx -> ctx.withAttribute(attributeName, value))`  
            把 R 寫入 attribute，最後回 `Validation<Violations, StepContext<T>>`
            

**語意重點：**

- 這是「`SpEL` + Validation」的完整整合：
    
    - 成功 ⇒ 寫入 attribute，再繼續 pipeline
        
    - 失敗 ⇒ 回傳 Invalid(Violations)，自然融入整體 Validation 流
        
- 使用場合：
    
    - 欄位為必填且必須符合格式：
        
        - 例如 email 正規式檢查
            
    - 計算出來的值**不得為 null**：
        
        - 例如匯率、費率、稅額等
            
    - 重要規則，錯誤時必須中斷或至少回報
        

---

## 7.7 `ExpressionEvaluations / ExpressionOperation：SpEL 底層支援`

我們提供了 `SpEL` 的底層封裝：

```java
@Component
public class ExpressionEvaluations implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public ExpressionEvaluation evaluate(String expression) {
        return new ExpressionEvaluation(applicationContext, expression);
    }

    public ExpressionOperation of(@NonNull String expression) {
        return new ExpressionOperation(expression, this);
    }
}
```

以及：

```java
public class ExpressionEvaluation {

    private final ApplicationContext applicationContext;
    private final Expression expression;

    // 透過 SpelExpressionParser 解析 expressionText
    // 建立 StandardEvaluationContext，掛上 BeanFactoryResolver
    // 支援 root object + variables
    // 提供 getValue / setValue / getValueWithVariables 等多種取值方式
}
```

這兩者一起扮演：

- 把 `Spring ApplicationContext`、`BeanFactoryResolver`、`StandardEvaluationContext` 等複雜度統一包在一層之下。
    
- 讓 `ExpressionOperation` 可以用很簡單的 API（predicate / reader / writer / `validationFunction`）取得型別安全的操作子。
    
- 讓 `ExpressionSteps` 不需要碰 `SpEL` 細節，只專注在「如何把操作子變成 `BehaviorStep`」。
    

---

## 7.8 `CH7 小結：ExpressionSteps 在整體架構中的定位`

現在我們可以對 `CH7` 做一個總結，並完全對齊你的實作：

1. **`ExpressionOperation`**
    
    - 封裝 `SpEL` 字串與評估細節
        
    - 提供 `predicate / reader / writer / validationFunction` 四種 FP 風格操作子
        
2. **`ExpressionSteps`**
    
    - 是「`SpEL` → `BehaviorStep`」的工廠
        
    - 提供四大行為模式：
        
        - `predicate(...)`
            
        - `readerWithAttribute(...)`
            
        - `writer(...)`
            
        - `validatorWithAttribute(...)`
            
3. **`BehaviorStep`**
    
    - 型別為 `StepContext<T> -> Validation<Violations, StepContext<T>>`
        
    - 可以自然插入 `BehaviorPipeline` 或 `ServiceChain`
        
4. **`getValidStepContext(...)`**
    
    - 為 `ExpressionSteps` 提供統一的起點：`Validation.valid(stepContext)`
        
    - 將意外例外轉成 `Invalid(Violations)`，避免 `SpEL` 或外部動作炸掉整條 pipeline。
        
5. **整體效果**
    
    - `SpEL` 被完整拉進 Monad 服務責任鏈中，
        
    - 以 `BehaviorStep` 形式成為可編排、可測試、可配置的行為積木。
        

---
