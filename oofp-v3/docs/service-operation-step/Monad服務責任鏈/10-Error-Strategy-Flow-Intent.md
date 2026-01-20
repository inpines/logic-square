
---

# 📘 `CH10 — 錯誤策略（Error Strategy）× 流程意圖（Flow Intent）`

### _以 Validation × `StepContext` 構築一致、可預測、可分層的錯誤語法_

完整技術版

---

# 10.1 我們的架構並非「例外導向」，而是「語意導向」

在傳統 Java 服務中：

- 錯誤來自 throw exception
    
- 成功則 return value
    
- 寫法散落、不可控、不連貫
    
- 錯誤常被吞掉、或暴衝至外層
    

但我們的架構（從 `CH1` 起）一貫遵守：

> **成功與失敗皆是流程語意的一部分，而不是例外控制流程。**

這由三個核心構造支撐：

1. Validation
    
    - success: valid(value)
        
    - failure: invalid(violations)
        
2. StepContext
    
    - 儲存 payload / attributes / violations
        
    - 可掛上 aborted（非錯誤，但提前結束）
        
3. ViolationSeverity
    
    - INFO / WARNING / ERROR / FATAL / UNSPECIFIED
        
    - 定義錯誤的「性質」，不是單純 boolean
        

這讓我們擁有：

- 一致資料流
    
- 一致錯誤流
    
- 一致語意流
    

這就是 **Flow Intent** —— 流程意圖。

---

# 10.2 Flow Intent：流程只有三種結果語意

這樣的系統設計非常乾淨，其流程語意可濃縮為三種：

```
(1) Valid continuation    → 正常向後傳遞
(2) Invalid termination   → 錯誤 → 流程停止
(3) Aborted completion    → 正常結束但提前停止
```

### ✔ Valid continuation（正常繼續）

所有 Step 的成功結果都是：

```java
Validation.valid(ctx)
```

語意：

- 流程仍可繼續
    
- StepContext 攜帶資料流（payload, attributes）往後傳
    

---

### ✔ Invalid termination（失敗 → 停止流程）

```java
Validation.invalid(violations)
```

語意：

- 此處視為業務失敗
    
- Pipeline（`ServiceChain` / `BehaviorPipeline`）立即停止
    
- 最終回傳 violations（可包含多筆，可包含多種 severity）
    

---

### ✔ Aborted completion（提前停止）

```java
ctx.setAborted(true)
```

`BehaviorPipeline` 解讀方式：

```java
if (aborted) break;
```

語意：

- 不是錯誤
    
- 不是 invalid
    
- 可視為「流程已滿足語義，不需繼續」
    

應用場景：

- 風險評分達到免審標準 → 不用跑後面步驟
    
- 某條規則判定後不再需要執行更多行為
    
- pipeline 的 early-return substitute（但有語意層級，而非裸 return）
    

---

# 10.3 錯誤策略（Error Strategy）：我們的系統將“錯誤等級”分成五種

`ViolationSeverity`：

```
INFO
WARNING
ERROR
FATAL
UNSPECIFIED
```

這不是一般 `enum`，而是錯誤策略的核心機制。

---

## 10.3.1 INFO：流程可視為成功，但提供訊息

用途：

- 記錄訊息
    
- 非必要提醒
    
- 審計中常見
    

不會造成 invalid。

---

## 10.3.2 WARNING：可繼續流程，但有潛在風險

用途：

- 輕度異常
    
- 不影響主要流程
    
- 可能由後續 Step 採取補救措施
    

依你架構的語意，WARNING 不會讓流程 invalid，除非 Step 將它回傳為 invalid。

---

## 10.3.3 ERROR：邏輯上不成立，流程應停止

用途：

- 必要欄位缺失
    
- 狀態不符合
    
- 無法通過業務規則
    

若 Step 回傳：

```java
Validation.invalid(Violations.violate("xxx", ERROR))
```

Pipeline 停止。

---

## 10.3.4 FATAL：不可挽回的重大錯誤

用途：

- 技術性錯誤（DB、API）
    
- 重大業務違規
    
- 風控禁止、機制違反
    

特性：

> 即使 Step 未回傳 invalid，你也可以透過 StepContext 的 hasFatalErrors() 在後續 Step 強制 abort 或 invalid。

這讓 FATAL 不只是 Validation 的結果，  
更是整體流程語意的主導因子。

---

## 10.3.5 UNSPECIFIED：預設狀態／尚未分類的錯誤

通常應避免，除非是：

- 外部系統錯誤
    
- 無法分類的異常
    
- fallback 建立的 generic error
    

---

# 10.4 錯誤如何在流程中流轉？

我們的架構讓錯誤可隨 `StepContext` 流動：

```
payload
attributes
violations  ← 錯誤永遠在這裡累積
aborted
```

### `BehaviorStep.filter(...)` 會加上錯誤：

```java
ctx.withViolation(violationProvider.apply(ctx.getPayload()))
```

### `validatorWithAttribute(...)` 回傳 Validation.invalid

### predicate(op) 則依 `onInvalid` 建立 Violations

---

# 10.5 accumulate vs fail-fast：何時累積？何時中止？

在你的設計中：

| 情境                               | 策略                           |
| -------------------------------- | ---------------------------- |
| `ServiceChain`（業務流程）             | fail-fast（invalid → 停止）      |
| `BehaviorPipeline`（行為流程）         | fail-fast（invalid → 停止）      |
| `validatorWithAttribute(..)`     | fail-fast                    |
| `readerWithAttribute(..)`        | 不視為錯誤（null → 清除 attribute）   |
| `ExpressionOperation.reader(..)` | 套用 null-safe，錯誤轉成 null       |
| `ViolationSeverity.FATAL`        | 可 fail-fast 或 abort，取決於 Step |

關鍵：

> 在我們的架構中，**累積錯誤是 `StepContext` 的責任，而不是 Validation 的預設行為。**

Validation 是：

- 單步結果的成功/失敗
    
- 不是 multi-step error accumulation 工具
    

`StepContext` 則負責：

- `violations.join(...)`
    
- `ctx.withViolation(...)`
    

這讓你保持：

- 流程語意乾淨
    
- 錯誤語意彈性
    
- 可控制錯誤累積邊界（避免無窮失敗訊息堆疊）
    

---

# 10.6 Flow Control Vocabulary：我們的架構最重要的語意字典

### **`valid(ctx)`**

→ 成功、可繼續。

### **invalid(violations)**

→ 失敗、立即停止流程。

### **`ctx.withViolation(v)`**

→ 增加錯誤，但不終止流程（由 Step 決定是否 invalid）。

### **`ctx.setAborted(true)`**

→ 流程應提前終止，不視為錯誤。

### **`peek / peekOnError`**

→ 副作用點，不影響結果。

### **recover(...)**

→ 將 invalid 轉成 valid(payload)（`BehaviorStep` 專屬能力）。

---

# 10.7 流程意圖（Flow Intent）判斷表（正式定義）

| Step 回傳               | Pipeline 解讀 | 行為                         |
| --------------------- | ----------- | -------------------------- |
| `Valid(ctx)`          | 繼續          | 下一 Step                    |
| `Invalid(violations)` | 停止          | 回傳錯誤                       |
| `ctx.aborted = true`  | 停止          | 回傳成功（結果由 resultApplier 決定） |

這三條語意界線非常清晰：

- Invalid = 失敗
    
- Aborted = 成功但提前結束
    
- Valid = 持續進行
    

---

# 10.8 `CH10` 最終整理：錯誤模型 × 流程語意 = 設計系統哲學

我們的整個架構之所以乾淨，是因為它維持以下 invariant：

> **所有流程必須以 `StepContext` 驅動；  
> 所有成功/失敗/提前結束必須以 Validation 或 aborted 描述；  
> 所有錯誤資訊必須進入 Violations；  
> 所有例外應被轉成 Violations 或 null-safe 行為。**

因此整體模型非常穩固：

```
[Step]           : ctx -> Validation
[BehaviorStep]   : ctx -> Validation
[ServiceChain]   : 固定流程，以 BehaviorStep 語意運作
[BehaviorPipeline]: 可配置流程，以 BehaviorStep 語意運作
[ExpressionSteps]: SpEL → BehaviorStep
[ExpressionOperation]: SpEL 操作描述子
[StepContext]    : 統一資料流（payload, attributes, violations, aborted）
[Validation]     : 統一控制流（success / failure）
```

這是業務流程 `DSL`、`FP` pipeline、動靜分離架構中最高品質的設計方式之一。

---
