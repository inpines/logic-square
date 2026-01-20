
---

# 🧭 **`README` — Monad-Based Service Flow Architecture**

### _A unified flow engine for deterministic services × configurable behaviors_

---

# 1. Overview

本架構是一套以 **Monad 思維、流程語意（Flow Intent）、錯誤模型（Error Strategy）** 為中心的  
企業級流程引擎（Service Flow Engine）。

它以 `StepContext<T>` 為資料流核心，  
以 `Validation<Violations, StepContext<T>>` 為控制流核心，  
以 `BehaviorStep<T>` 為主要運算單元（`combinator`），  
形成：

> **固定主流程（`ServiceChain`） × 可配置行為流程（`BehaviorPipeline`）**
> 
> **共同以相同語意運作的一致性 flow model。**

整套架構由以下概念組成：

- **`StepContext`**：流程資料的唯一容器
    
- **`Validation`**：成功／失敗的流程控制
    
- **`BehaviorStep`**：可組合的行為函數
    
- **`ServiceChain`**：固定業務流程，由工程師撰寫
    
- **`BehaviorPipeline`**：可配置行為流程，由規則或 `SpEL` 組合
    
- **`ExpressionSteps` / `ExpressionOperation`**：將 `SpEL` 轉為 `BehaviorStep`
    
- **`ViolationSeverity` / Flow Intent**：錯誤語意與流程語意
    

這使得業務流程可以：

- 可預測
    
- 可組合
    
- 可配置
    
- 可測試
    
- 可觀察
    
- 易於擴張
    

---

# 2. Core Abstractions

---

## 2.1 `StepContext`

所有流程的資料載體。

```java
StepContext<T> {
    T payload;
    Violations violations;
    Map<String, Object> attributes;
    boolean aborted;
}
```

語意：

- `payload`：主資料
    
- `attributes`：中繼資料（供 Expression 或後續步驟使用）
    
- `violations`：錯誤累積器
    
- `aborted`：提前停止但視為成功
    

---

## 2.2 Validation<Violations, StepContext>

流程控制流模型：

- `Validation.valid(ctx)` → 成功，繼續
    
- `Validation.invalid(violations)` → 失敗，中止
    

這讓所有 Step 與 Pipeline 都可遵循相同語意。

---

## 2.3 `BehaviorStep`

整個架構的最小運算單位（`composable operator`）：

```java
BehaviorStep<T> = StepContext<T> → Validation<Violations, StepContext<T>>
```

特點：

- 可 `flatMap/compose（andThenStep）`
    
- 可 map（`andThenMapper`）
    
- 可 condition（when）
    
- 可 filter
    
- 可 recover
    
- 可 peek / `peekOnError`
    

它是：

> **`ServiceChain` 與 `BehaviorPipeline` 的共同語言。**

---

### 2.4 `ServiceChain 是「概念式 Monad Flow」，不是單一介面`

在本架構裡，**`ServiceChain` 不是某個固定的 interface 或 class 名稱**，  
而是一種「用 Monad/Either 風格來串接服務流」的 **概念**：

- 把「服務步驟」寫成：  
    `A -> F<A>` 或 `A -> F<B>`
    
- 其中 `F<_>` 可以是：
    
    - `Validation<Violations, StepContext<T>>`（目前 `BehaviorPipeline` 使用的）
        
    - `Either<E, A>`
        
    - `Try<A>`
        
    - `Task<Try<E, A>>`（例如 `async` I/O + domain error 的組合）
        
- 用 `flatMap` / `map` 把這些步驟串起來
    
- 由 `F` 的型別語意決定：
    
    - 是否失敗
        
    - 如何失敗
        
    - 是否支援非同步
        
    - 是否同時攜帶 domain error + technical error
        

在這個觀點下：

- 現在實作的 `BehaviorStep<T>` + `Validation<Violations, StepContext<T>>`  
    只是 **`ServiceChain` 概念的一個具體實作**。
    
- 未來我們完全可以定義：
    
    - `Either<Error, Domain>` 版本的 `ServiceChain`
        
    - `Task<Try<Error, Domain>>` 版本的 `ServiceChain`（例如 `Reactor/Coroutine` 風格）  
        而不用改變「整體架構哲學」。

---

# 3. Two Flow Systems

### 3.1 `ServiceChain` —— 概念層的「主流程 Monad 串接」

> **`ServiceChain` 在本架構中是一種概念：  
> 用 _可 `flatMap` 的結構_ 來串接服務流。**

也就是說，只要滿足：

`Step:  A -> F<B> F:     支援 map / flatMap 的型別構造（Either / Try / Validation / Task<Try<…>> 等） Chain: F<A> --flatMap(Step)--> F<B> --flatMap(...)--> ...`

就可以視為一種 `ServiceChain`。

目前在本架構中的具體實作是：

- 使用 `Validation<Violations, StepContext<T>>` 作為 F
    
- 使用 `BehaviorStep<T>` 作為 Step
    
- 由 `BehaviorPipeline` 組裝出實際可執行流程
    

但抽象層面上，你也可以建立其他版本的 `ServiceChain`，例如：

- `Either<Error, Domain>` 為主的 error-first 流程
    
- `Try<A>` 為主的 exception 包裝流程
    
- `Task<Try<E, A>>` 為主的非同步 + 例外 + domain error 雙層結構
    

因此：

- **`BehaviorPipeline`** = 一個「具體實作」的 `ServiceChain`（`F = Validation<Violations, StepContext<T>>`）
    
- **`ServiceChain`（廣義）** = 任何基於 `Either` / `Try` / `Task<Try<…>>` 等 Monad 概念，  
    並以 `flatMap` 串接服務步驟的流程設計。

特點：

- 必須穩定、不可配置
    
- 可含 DB / API / 交易行為
    
- 完整主業務邏輯
    
- 錯誤一般採 fail-fast
    
- 不允許 `SpEL` 注入（避免邏輯漂移）
    

本質上：

> **`ServiceChain` = `BehaviorStep` 的通用化版本  
> 用於確定性的主流程。**

---

## 3.2 `BehaviorPipeline` —— 可配置的行為流程（`composable behavior flow`）

### 固定業務主流程

> 在實務上，我們會用「某一種 F」來具體化 `ServiceChain` 概念，並在 Service 層寫出固定的主流程。例如目前專案中以 `Validation<Violations, StepContext<T>>` + `BehaviorStep<T>` 實作的主流程，就可以看成是一種 **`ServiceChain 實作`**。

用來處理：

- 規則
    
- 加值行為
    
- 可變策略
    
- `YAML/DB` 讀出的行為清單
    
- `SpEL` 驅動的動態行為
    

建立方式：

```java
BehaviorPipeline.steps()
    .with(stepA)
    .with(stepB)
    .with(stepC)
    .apply(input, finalizer);
```

特點：

- 使用 `BehaviorStep` 語意
    
- 支援 abort
    
- 支援 recover
    
- 支援 chain（複合 Step）
    
- 支援 `SpEL` 驅動（`ExpressionSteps`）
    

---

# 4. Expression-Based Behavior

## 4.1 `ExpressionOperation`

封裝 `SpEL` 表達式，提供：

- predicate()
    
- reader()
    
- writer()
    
- `validationFunction`()
    

將 `SpEL` 調用抽象成安全的操作子（operator object）。

---

## 4.2 `ExpressionSteps`

`ExpressionOperation` → `BehaviorStep` 的工廠。

包含：

- `predicate`
    
- `readerWithAttribute`
    
- `writer（副作用）`
    
- `validatorWithAttribute`
    

讓 `SpEL` 能自然融入 `BehaviorPipeline`：

```java
behaviorPipeline.with(
    expressionSteps.readerWithAttribute(expr("payload.age"), ctxVars, "age")
);
```

---

# 5. Unified Error Strategy

## 5.1 `ViolationSeverity`

```
INFO        → 無害訊息
WARNING     → 可繼續但需注意
ERROR       → 業務不成立 → invalid
FATAL       → 嚴重錯誤 → 可強制 abort 或 invalid
UNSPECIFIED → 未分類
```

## 5.2 Flow Intent

流程語意明確且有限：

|Intent|語意|結果|
|---|---|---|
|Valid|成功|繼續|
|Invalid|錯誤|停止|
|Aborted|正常結束|停止但非錯誤|

所有流程控制都由 Validation × `StepContext` 表現。

---

# 6. Flow Composition Vocabulary

以下為架構中的語意語彙：

### `valid(ctx)`

繼續流程。

### invalid(violations)

失敗，中止流程。

### `ctx.withViolation()`

加入錯誤資訊（不自動停止）。

### `ctx.aborted` = true

提前結束流程（成功）。

### `andThenStep`

以 Monad 語意串接流程。

### peek / `peekOnError`

副作用觀察（不影響流程）。

### `recover(fn)`

錯誤修復（`BehaviorStep` 版 try/catch）。

---

# 7. Architectural Goals

本架構旨在提供：

### ✔ 一致的流程語意

所有流程單位都以相同基礎模型運作（`StepContext` × Validation）。

### ✔ 行為可組合、主流程穩定

業務核心不可配置（`ServiceChain`）  
行為策略可配置（`BehaviorPipeline`）。

### ✔ 支援 `SpEL DSL`

讓行為規則可外部化、資料化。

### ✔ 清楚的錯誤模型

分層明確、可觀測、可分析。

### ✔ 副作用語意清楚

透過 writer/peek/mapper，而不是匿名 lambda。

### ✔ 喜愛 `FP` 的工程師能用 `FP` 組合

喜愛命令式的工程師也能正常撰寫 `ServiceChain`。

---

# 8. Example: Combining Both Flows

```java
// (1) 可配置驗證行為
Validation<Violations, StepContext<Order>> validated =
    behaviorPipeline.apply(order, ctx -> ctx);

// (2) 固定主流程
return validated.flatMap(ctx ->
    serviceChain
        .add(this::loadCustomer)
        .add(this::checkStatus)
        .add(riskStep)              // 可插入行為 pipe
        .add(this::persistOrder)
        .run(ctx)
);
```

這展示了架構的核心精神：

> **主流程由工程師控制，行為流程由配置控制，  
> 兩者以 BehaviorStep 語意達成自然整合。**

---

# 9. When to Use What

| 場景            | 選擇                                |
| ------------- | --------------------------------- |
| 核心業務邏輯        | `ServiceChain`                    |
| 規則、策略、行為差異    | `BehaviorPipeline`                |
| SpEL 行為編排     | `ExpressionSteps`                 |
| DB/API/交易行為   | `ServiceChain`                    |
| 審計 / log / 指標 | `BehaviorStep.peek / peekOnError` |
| 自訂錯誤策略        | `ViolationSeverity + recover`     |

### 9.1 進階：以 Either / Try / Task<Try<E, A>> 實作 `ServiceChain`

在更高階或更特殊的情境下，可以將 `ServiceChain` 抽象為 **純概念**，  
再用不同的 Monad 具體化：

- `Either<DomainError, Result>`：  
    清楚區分成功與 domain error 的純函數流程。
    
- `Try<Result>`：  
    把 Java exception 包裝成可組合的 flow。
    
- `Task<Try<InfraError, Result>>`：  
    適用於非同步、I/O 密集服務，把基礎設施錯誤（infra error）與 domain error 分層處理。
    

這些實作都可以沿用本架構建立的核心哲學，只是把 `F` 從 `Validation<Violations, StepContext<T>>` 換成 `Either` / `Try` / `Task<Try<…>>` 而已。

---

# 10. Philosophy

> **錯誤不是例外，而是語意。**  
> **流程不是 case-by-case，而是運算。**  
> **行為不是硬寫，而是可組合。**  
> **主流程不應飄動，但行為可以外化。**
> 
> 這是本架構的精神，也是它能穩定長期維運、支援複雜需求的原因。

---

# 11. Summary

本架構透過：

- `StepContext（資料流）`
    
- `Validation（控制流）`
    
- `BehaviorStep（可組合行為）`
    
- `ServiceChain（確定性主流程）`
    
- `BehaviorPipeline（可配置行為流程）`
    
- `ExpressionSteps（SpEL → 行為 DSL）`
    
- `錯誤語意（ViolationSeverity × Flow Intent）`
    

組成一個：

> **動靜並存（deterministic × configurable）  
> 的企業級流程抽象層。**

---
