
---

# 📘 `CH8 — ServiceChain`

### _基於 `BehaviorStep` 語意構築的「固定業務主流程」_

完整技術版

---

# 8.1 `ServiceChain` 的角色：固定、不配置、工程師明確定義的主流程

`BehaviorPipeline（CH9）`負責可配置的行為規則、動態策略、`SpEL` 驅動的行為組合。  
但 `ServiceChain` 的定位完全不同：

> **`ServiceChain` 是企業服務的「核心骨幹」，由工程師手寫，負責不可配置、不可動態調整的業務主流程。**

例如：

- 讀取資料庫
    
- 呼叫外部 API
    
- 計算主要商業邏輯
    
- 寫入交易紀錄
    
- 產生領域事件
    
- 執行事務控制（transaction）
    
- 錯誤不可模糊，必須明確定義
    

與 BehaviorPipeline 不同：

|面向|ServiceChain|BehaviorPipeline|
|---|---|---|
|流程性質|固定、穩定|可配置、可組裝|
|Step 來源|工程師撰寫|SpEL / ExpressionSteps|
|用途|主流程|規則 / 行為層|
|副作用|常見（DB / API）|可控（audit / attribute / 行為）|
|設計導向|程式碼邏輯|DSL 及資料化|

雖然兩者的 Step 在語意上不同，但：

> **它們共享相同的運算核心：  
> `StepContext × Validation<Violations, StepContext>`。**

---

# 8.2 `Step 與 BehaviorStep：本質相同、責任不同`

你的 `BehaviorStep` 定義如下：

```java
public interface BehaviorStep<T>
        extends Function<StepContext<T>, Validation<Violations, StepContext<T>>>
```

`ServiceChain` 的 Step 其本質就是：

```java
Step<T> = StepContext<T> → Validation<Violations, StepContext<T>>
```

`ServiceChain 的 Step 與 BehaviorStep` 的差異不是型別`，  
而是**責任程度與應用場景**：

|Step（ServiceChain）|BehaviorStep（Pipeline）|
|---|---|
|包含 DB、API、寫入、交易|多數純粹或輕量（reader/predicate/validator/writer）|
|失敗通常必須停止|allow recover / allow abort|
|對外界副作用強|副作用較受控（peek/peekOnError）|
|不應該用 SpEL|常搭配 ExpressionSteps|
|主流程必要|行為規則可選|

但兩者共享：

- `Context 流轉（payload、attributes）`
    
- `Validation 控制流`
    
- `flatMap（andThenStep）組合`
    
- `保證流程線性、可預測`
    

因此：  
**`ServiceChain 是 BehaviorStep 的“工程化版本”，用於企業級主流程。`**

---

# 8.3 `ServiceChain 的組成與執行模型（完全對齊 BehaviorStep）`

一個標準的 `ServiceChain` 執行模型如下：

1. 建立初始 `StepContext<T>`
    
2. 依順序執行 Step 列表
    
3. 每個 Step 回傳 `Validation<Violations, StepContext<T>>`
    
4. 若 Step 失敗（Invalid） → 馬上停止流程
    
5. 若 Step 設定 `aborted=true` → 停止流程但不視為錯誤
    
6. 最終將結果轉成 Service 結果物（`DTO` 或領域物件）
    

即：

```
ctx0 → step1 → ctx1 → step2 → ctx2 → ... → 最終 ctxN
```

錯誤路徑則是：

```
某步失敗 → Invalid(violations) → 中止流程 → 返回
```

可對照 `BehaviorPipeline` 的核心迴圈（你實作的）：

```java
for (BehaviorStep<T> step : steps) {
    Validation<Violations, StepContext<T>> result = step.apply(context);

    if (result.isInvalid()) return invalid;
    if (result.map(ctx -> ctx.isAborted()).orElse(false)) break;

    context = result.get()...
}
```

`ServiceChain` 與 `BehaviorPipeline` 共享此模型。

---

# 8.4 `ServiceChain` 的標準設計模式（依你的 `StepContext` 與 `Validation`）

`ServiceChain` 的 Step 有幾個明確規範（與 `BehaviorStep` 同源）：

---

## ✔（1）Step 不應拋出例外

若不小心發生 exception，必須包裝成：

```java
Validation.invalid(Violations.violate("xxx.error", ex.getMessage()))
```

以維持控制流完整。

---

## ✔（2）Step 必須回傳完整的 StepContext

例如：

```java
return Validation.valid(ctx.withAttribute("customer", customer));
```

或：

```java
return Validation.invalid(
    ctx.withViolation(Violations.violate("customer.notfound"))
);
```

---

## ✔（3）成功與失敗的語意全由 Validation 控制

避免：

- return
    
- throw
    
- break
    
- null 回傳
    

---

## ✔（4）Step 可以有副作用，但必須保持語意清楚

例如 DB read/write、外部 API。

與 BehaviorStep 的副作用大原則一致：  
**副作用不得改變 pipeline 的結構語意，只能透過 Context 修改資料。**

---

## ✔（5）`ServiceChain` 的 Step 不應承載可配置邏輯

這些邏輯應交給：

- `BehaviorPipeline（CH9）`
    
- `ExpressionSteps（CH7）`
    
- `ExpressionOperation（CH6）`
    

`ServiceChain` 的 Step 必須：

> **專注於業務流程本身，不負責行為差異處理。**

---

# 8.5 `ServiceChain` 與 `BehaviorPipeline` 的整合策略

這是你架構真正的亮點。

`ServiceChain` 可以與 `BehaviorPipeline` 互補，有三種模式：

---

## **模式 A：`ServiceChain → BehaviorPipeline（後處理）`**

例如：

```java
Validation<Violations, StepContext<T>> mid = serviceChain.run(ctx);

return mid.flatMap(c -> behaviorPipeline.apply(c.getPayload(), finalizer));
```

用途：

- 執行“後置行為”（post-actions）
    
- 如 `audit / enrichment / logging`
    

---

## **模式 B：`BehaviorPipeline → ServiceChain（前置驗證/規則）`**

例如：

```java
behaviorPipeline.apply(input, ctx -> ctx)
    .flatMap(serviceChain::run);
```

用途：

- 下單前先過風控
    
- 建置前先跑可配置驗證
    
- 收件前先跑清洗、轉換
    

---

## **模式 C：`ServiceChain 中某步驟使用 BehaviorStep`**

例：

```java
Step<T> riskAssessment = BehaviorStep.of(
    ctx -> behaviorPipeline.apply(ctx.getPayload(), x -> ctx)
);
```

這可以讓 `ServiceChain` 的固定流程中插入一個「可配置的行為節點」。

等於：

> **`ServiceChain` = 主流程骨幹 + 可插拔行為節點**

這是非常高階的大型系統設計。

---

# 8.6 一個完整實例：建立帳號流程（示例）

假設流程：

1. 驗證輸入格式（由 `BehaviorPipeline` 負責，可配置）
    
2. 讀取並檢查是否已存在
    
3. 計算風險分數（行為流程）
    
4. 決定是否允許建立帳號
    
5. 寫入 DB
    
6. 回傳結果 DTO
    

程式可能如下：

```java
public Validation<Violations, AccountResult> createAccount(AccountPayload payload) {

    // 1. 可配置規則驗證
    Validation<Violations, StepContext<AccountPayload>> pre =
            behaviorPipeline.apply(payload, ctx -> ctx);

    return pre.flatMap(ctx ->
            // 2~5 固定主流程
            serviceChain
                .add(this::loadExistingAccount)
                .add(this::checkDuplicate)
                .add(riskBehaviorStep)         // 行為節點
                .add(this::persistAccount)
                .run(ctx)
        )
        // 6. 最終結果由 resultApplier 決定
        .map(finalCtx -> mapToResult(finalCtx));
}
```

這範例展示：

- `ServiceChain` 與 `BehaviorPipeline` 無縫整合
    
- 行為可配置、流程保持固定
    
- `StepContext` 流轉清晰
    
- Validation 控制流自然
    

---

# 8.7 `CH8` 小結：`ServiceChain` 的真正定位（正式定義）

> **`ServiceChain` 是以 `StepContext` × Validation 為核心的「固定業務主流程」，  
> 用於實作 DB / API / 主邏輯等不應由配置決定的行為。**

它具有以下特徵：

### ✔ 與 `BehaviorStep` 共享完全相同的型別語意（`flatMap` pipeline）

### ✔ Step 必須清楚回傳成功/失敗（Validation）

### ✔ `StepContext` 是唯一資料流載體

### ✔ 副作用可存在但不可破壞流程意圖

### ✔ 不包含可配置規則

### ✔ 可與 `BehaviorPipeline`（可配置行為）互補整合

### ✔ 是整個企業應用中最中心的邏輯脈絡（core business logic）

一句話：

> **`ServiceChain` = deterministic main flow**  
> **`BehaviorPipeline` = configurable behavior flow**  
> **兩者以 `StepContext` × Validation 作為共同語言。**

---
