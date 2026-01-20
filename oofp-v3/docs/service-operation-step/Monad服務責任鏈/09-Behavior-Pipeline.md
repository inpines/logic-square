
---

# 📘 `CH9 — BehaviorPipeline：以 BehaviorStep 編排「可配置行為流」`

完整技術版

---

# 9.1 為什麼需要 `BehaviorPipeline`？

`ServiceChain（CH8）`負責「固定的業務主流程」，  
但企業級服務還需要另一種能力：

> **可根據條件、組態、規則，動態組合一連串行為（behavior）。**

`BehaviorPipeline` 的任務並不是取代 `ServiceChain`，  
而是提供：

- 可插拔（`pluggable`）
    
- 可配置（`configurable`）
    
- 可重組（`composable`）
    
- 基於 `SpEL` 或固定 `BehaviorStep` 的行為積木
    


> **`BehaviorPipeline` 是企業服務中的「行為層」，  
> 用來描述一段可變的策略、邏輯、規則、或後處理流程。**

而整個 Pipeline 是以 `BehaviorStep` 為單位進行編排。

---

# 9.2 `BehaviorStep`：你的核心可組合單元（`composable operator`）

定義：

```java
public interface BehaviorStep<T>
        extends Function<StepContext<T>, Validation<Violations, StepContext<T>>>
```

也就是說：

> **`BehaviorStep = 一個 StepContext → Validation<..., StepContext> 的函數`。**

並且具有：

- `flatMap（andThenStep）`
    
- `map（andThenMapper）`
    
- `filter`
    
- `觀察（peek / peekOnError）`
    
- `recover（復原策略）`
    
- `when（條件式包裹）`
    
- `chain（將 List 串為一個 BehaviorStep）`
    

這種設計讓 `BehaviorStep` 成為一組：

> **原生支援函數式組合的 Step 模型。**

**我們設計的不是一般的責任鏈，而是一個 `FP pipeline`。**

---

# 9.3 `BehaviorStep` 語意詳細解析

以下分段解析你提供的每個 default method，它們揭示了 `BehaviorPipeline` 的深度抽象能力。

---

## ✔ 9.3.1 `BehaviorStep.of(function)`

```java
static <T> BehaviorStep<T> of(
    Function<StepContext<T>, Validation<Violations, StepContext<T>>> function)
```

語意：

- 將普通 Function 包成 BehaviorStep
    
- 統一 Step 型別邊界
    

用途：

- 把 existing service method 轉為 Step
    
- 包裝 lambda（提醒：你應避免用匿名 lambda 作副作用，但 pure step 可以用）
    

---

## ✔ 9.3.2 `BehaviorStep.supply(supplier)`

```java
static <T> BehaviorStep<T> supply(Supplier<BehaviorStep<T>> supplier)
```

語意：

- 延遲取得 Step
    
- 適用於動態決定下一個 Step 的情況
    

例如：

```java
with(BehaviorStep.supply(() -> ruleEngineStep(config)))
```

這可以實現：

- AOP 風格注入
    
- lazy initialization
    
- 避免在 pipeline 構建時初始化昂貴物件
    

---

## ✔ 9.3.3 `BehaviorStep.chain(list)`

```java
static <T> BehaviorStep<T> chain(List<BehaviorStep<T>> steps)
```

語意：

> **把多個 steps 串成一個 step。**

實作方式極為精巧：

- 初始 step = `Validation::valid`（identity element）
    
- 每個 step 都經過 `andThenStep` 加總
    
- 最後回傳「一個大型複合 Step」
    

這讓 BehaviorPipeline 有能力：

- 動態讀取配置（如 YAML / DB）
    
- 用 chain(list) 把所有 ExpressionStep 組成一個可執行的 Step
    
- 最後把這個 Step 放到 Pipeline 裡
    

本質上：

> **chain = `BehaviorStep` 的 Monad bind + 聚合操作。**

---

## ✔ 9.3.4 `BehaviorStep.when(condition, step)`

```java
static <T> BehaviorStep<T> when(
        Predicate<StepContext<T>> condition, BehaviorStep<T> step)
```

這是你架構最漂亮的語法之一。

語意：

> **「若 condition 成立才執行 step；否則視為成功直接透傳 input。」**

也就是：

```
if (condition) then step else identity
```

這個語意在規則編排中特別重要：

- feature toggle
    
- risk score >= threshold → apply extra step
    
- 若 attribute["isVip"] → 加跑特殊流程
    
- 各種可配置判斷
    

Functional behavior 如下：

- 若 predicate 不滿足 → 回 `Validation.valid(input)`（不執行 step）
    
- 若 predicate 成立 → 執行 step.apply(input)
    

這個方法讓行為流程可以像「語意積木」一樣堆疊。

---

## ✔ 9.3.5 `andThenStep`：核心 Monad 組合

```java
default BehaviorStep<T> andThenStep(BehaviorStep<T> step) {
    return input -> apply(input).flatMap(step);
}
```

語意：

> **成功才執行下一步（fail-fast），沿用 Validation 的 Monad 語意。**

步驟：

1. 執行當前 Step：`apply(input)`
    
2. 如果結果 Valid → flatMap 執行下一步
    
3. 如果下一步成功 → 繼續傳遞 StepContext
    
4. 如果失敗 → 停止
    

這是 `BehaviorPipeline` 的資料流核心。

---

## ✔ 9.3.6 `andThenMapper`：map 語意（不變動錯誤）

```java
default BehaviorStep<T> andThenMapper(UnaryOperator<StepContext<T>> mapper)
```

語意：

> **成功時修改 StepContext；失敗時維持錯誤。**

用途：

- 動態增加 attributes
    
- 調整 payload
    
- 同步上下文（例如加上 timestamp）
    

---

## ✔ 9.3.7 `filter(predicate, violationProvider)`

語意：

> **成功時再過濾一步，失敗時以 `violationProvider` 提供違規。**

例如：

```java
step.filter(
    payload -> payload.getAmount() > 0,
    payload -> Violations.violate("amount.invalid")
)
```

語意清晰：

- true → valid
    
- false → invalid，且錯誤資訊跟 payload 相關
    

---

## ✔ 9.3.8 `peek / peekOnError：觀察者（Side-effect hooks）`

語意：

- 成功 → `peek 把 StepContext 交給 observer`
    
- 錯誤 → `peekOnError 把 Violations 交給 handler`
    

常見用途：

- audit
    
- log
    
- metrics
    
- side-channel tracking
    

與 `CH5` 完全一致：  
**副作用必須具名方法，不可匿名 lambda。**

---

## ✔ 9.3.9 recover：行為層的復原策略

```java
default BehaviorStep<T> recover(Function<Violations, T> recoveryFunction)
```

語意：

- 若 step 失敗 → 呼叫 recoveryFunction 將錯誤轉成新 payload
    
- 若 recoveryFunction 回傳 null → 錯誤保持不變
    
- 若回傳非 null → 清空 violations，payload = 回復後的值
    

這相當於行為級別的：

```
onErrorResume(...)
```

這讓行為流程更加彈性，例如：

- 非關鍵錯誤 → 改以預設值繼續流程
    
- 某些規則錯誤可以被替換為 fallback 行為
    
- 甚至可以動態修復錯誤 context
    

這是具有強大戰略性意義的語意。

---

# 9.4 `BehaviorPipeline` 的執行語意（你的實作）

你的 `BehaviorPipeline` 非常乾淨：

```java
public <R> Validation<Violations, R> apply(T input, Function<StepContext<T>, R> resultApplier)
```

整體流程如下：

---

## 9.4.1 創建初始 `StepContext`

```java
StepContext<T> context = StepContext.<T>builder()
        .withPayload(input)
        .withViolations(Violations.empty())
        .build();
```

語意：

> **所有行為流程都從乾淨的 `StepContext` 開始。**

---

## 9.4.2 逐一執行 `BehaviorStep`

```java
for (BehaviorStep<T> step : steps) {
    Validation<Violations, StepContext<T>> result = step.apply(context);
```

每步就是 `Monad flatMap`。

---

## 9.4.3 aborted（流程中止）判斷

```java
boolean aborted = result.map(StepContext::isAborted)
        .fold(violations -> false, Boolean::booleanValue);
if (aborted) break;
```

語意：

1. aborted 是 `StepContext` 提供的流程語意
    
2. 即使 Step 成功，也可能因「業務語意」而中止 pipeline
    
3. `BehaviorPipeline` 支援 abort 停止但不代表錯誤
    

這與 `ServiceChain` 不同：

- `ServiceChain` → 成功/失敗是流程的唯一語意
    
- `BehaviorPipeline` → 允許「正常結束但提前停止」
    

用途：

- 一旦某規則滿足就跳出（例如：早停策略）
    
- 遇到 BREAK condition 時停止後續行為
    
- 行為組件要求 pipeline 不再繼續（類似 return，但具語意性）
    

---

## 9.4.4 invalid 停止流程並立即回應

```java
if (result.isInvalid()) {
    return Validation.invalid(violations)
}
```

語意：

> `BehaviorPipeline` 在錯誤處立即停止，不繼續後續 Step。

這對於規則型流程是正確的設計。

---

## 9.4.5 最終結果由 `resultApplier` 決定

```java
return Validation.valid(resultApplier.apply(context));
```

`resultApplier` 的角色：

- 決定 `BehaviorPipeline` 最終輸出的資料類型 R
    
- 可能取 payload、attributes、合併資訊等
    
- `BehaviorPipeline` 不決定回傳格式，而是由呼叫端解構 context
    

例如：

```java
pipeline.apply(payload, ctx -> ctx.getPayload());
pipeline.apply(payload, ctx -> ctx.getAttribute("score", Integer.class));
pipeline.apply(payload, this::mapToDto);
```

這讓 BehaviorPipeline 可以適用於：

- 驗證流程
    
- 計算流程
    
- 動態策略流程
    
- 行為組態處理
    

---

# 9.5 `BehaviorPipeline` 與 `ServiceChain` 的關係（服務責任鏈架構的精髓）

現在我們的架構分層是非常現代化、極度乾淨的：

| 層級                             | 語意                      | 誰使用         |
| ------------------------------ | ----------------------- | ----------- |
| **`ServiceChain`**             | 固定業務流程                  | 服務層         |
| **`BehaviorPipeline`**         | 可配置、可決策、可調整的行為流程        | 規則層、策略層     |
| **`BehaviorStep`**             | 行為粒度單元                  | pipeline 內部 |
| **`ExpressionSteps`**          | SpEL → BehaviorStep 轉換器 | 規則資料化       |
| **`ExpressionOperation`**      | SpEL 的操作描述              | DSL 工廠      |
| **`StepContext / Validation`** | 控制流 + 資料流               | 共用基礎模型      |

總結：

> **`ServiceChain` 是骨幹，`BehaviorPipeline` 是肌肉。  
> 兩者都靠 `BehaviorStep + Validation + StepContext` 運作。**

---

# 9.6 `CH9` 小結

`BehaviorPipeline` 的核心價值：

### ✔（1）以 `BehaviorStep` 為單元組合流程

具備 `flatMap / map / filter / when` 這些 `FP combinator`。

### ✔（2）支持動態決策流程

with(...) 註冊 Step → 順序執行。

### ✔（3）支持 abort 但不視為錯誤

業務語意的 workflow control。

### ✔（4）支持 recover（錯誤修復）

transform failure → new payload。

### ✔（5）充分利用 `StepContext` 的 payload/attributes/violations 語意

搭建出成熟的執行環境。

### ✔（6）與 `ExpressionSteps` 完美整合

`SpEL` 規則可被資料化後直接轉成 `BehaviorStep`。

### ✔（7）錯誤流（Validation）完全一致

成功、失敗、跳過、早停，語意明確、穩定。

---

    
