
---

# 📘 `CH6 — ExpressionOperation：讓 SpEL 成為「可語意化的行為操作子」`

完整技術版

`CH6` 的目標不是創造一個新的 `FP Monad`，而是：

> **將 Spring `SpEL` 以一致、安全、可測試、可配置的方式封裝起來，使其能參與 `BehaviorStep` 與 `BehaviorPipeline`。**

你的系統做到的是：

- 讓 `SpEL` 不再是任意字串
    
- 讓 `SpEL` 的執行受控於流程語意（Validation）
    
- 讓 `SpEL` 能讀、能寫、能驗證
    
- 並能透過 `varsProvider` 注入流程上下文
    
- 最後以 `ExpressionSteps` 轉換成行為積木（`BehaviorStep`）
    

所以我們從實作開始解說。

---

# 6.1 `ExpressionEvaluations` 與 `ExpressionEvaluation`：`SpEL` 的安全執行環境

我們的 `SpEL` 執行底層由兩個類別組成：

---

### **①` ExpressionEvaluations：入口（Factory）`**

```java
public ExpressionEvaluation evaluate(String expression)
public ExpressionOperation of(@NonNull String expression)
```

語意：

- 建立 `SpEL` 的「執行物件」（`ExpressionEvaluation`）
    
- 建立「操作子描述物件」（`ExpressionOperation`）
    

也就是說：

```
evaluate()  → 用於立即計算（較底層）
of()        → 用於建立操作子（較上層，用於 DSL）
```

---

### **② `ExpressionEvaluation`：包裝 `SpEL` 的安全執行 API**

包含：

- `getValue(root)`
    
- `getValue(Class, root)`
    
- `getValueWithVariables(variables)`
    
- `setValue(...)`
    
- 自動建立 `EvaluationContext`
    
- `BeanFactoryResolver` 支援 Spring Bean 解析
    
- 所有 evaluate operations 都以 root object 為主（你的 `StepContext`）
    

特色：

- **不會讓 `SpEL` 例外直接炸出**（我們有 Optional 包裝與 null fallback）
    
- 提供「安全執行」語意（null / error tolerant）
    
- `EvaluationContext` 統一管理（bean resolver、variables、root）
    

這也是 `ExpressionOperation` 的運作基礎。

---

# 6.2 `ExpressionOperation`：將 `SpEL` 包裝為四種「可語意化操作子」

你的 `ExpressionOperation` 背後的角色是：

> **把 `SpEL` 表達式轉換成 predicate / reader / writer / `validatorFunction` 四種語意單位。**

這四種不是抽象的「`FP` 操作」，  
而是「可以插入 `BehaviorStep` 的行為描述」。

因此它不是 Monad Operator，  
而是 `Behavior DSL` 的基本構件。

---

# 6.3 `varsProvider`：`SpEL`表達式參數化架構的靈魂

`ExpressionOperation`在 `ExpressionSteps` 裡大量使用：

```java
Function<StepContext<T>, Map<String, Object>> varsProvider
```

這其實是一個非常強大的設計：

> **`varsProvider` 決定 `SpEL` 執行時可見的變數集合，使 `SpEL` 能夠根據 `StepContext` 的狀態動態調整行為。**

它解決了 `SpEL` 在企業應用中最大痛點：

- evaluation 時 contextual variables 的綁定不乾淨
    
- `SpEL expression`s 難以寫出可維護邏輯
    
- 多階段流程中要注入多個值
    

在你架構中：

- `SpEL root` 是 `StepContext`
    
- payload = `stepContext.getPayload()`
    
- attributes = `stepContext.getAttributes()`
    
- `varsProvider` 再補上 pipeline 需要的變數（如閾值、外部配置等）
    

因此 `SpEL` 具有完整上下文，又能保持安全性。

---

# 6.4 四大操作語意（依照你的程式碼描述）

`ExpressionOperation API`：

---

## **① `predicate(varsProvider)` → `Predicate<StepContext>`**

語意：

```
ExpressionOperation.predicate(varsProvider)
→ 產生 Predicate<StepContext<T>>
```

`SpEL` boolean expression 評估後：

- true → predicate 成功
    
- false / null / exception → predicate 失敗
    

無論 `SpEL` 是否出錯，都不會拋出例外。  
錯誤會由 `ExpressionSteps.predicate(...)` 轉成 Validation.invalid。

用於：

- feature toggle
    
- 條件守門員（guard）
    
- 行為 enable / disable
    

---

## **② reader(varsProvider) → Function<StepContext, R>**

用於：

- 從 payload 或 attribute 讀取值
    
- 動態計算中繼資料
    
- 提供後續 Step 依賴的參數
    

若 `SpEL` 出錯 → 回 null，而不是炸例外。

reader 的結果交由 `ExpressionSteps.readerWithAttribute(..)` 處理：

- 非 null → `withAttribute`
    
- null → `withNoneAttribute`
    

---

## **③ `writer(varsProvider, valueProvider)` → `Consumer<StepContext>`**

語意：

- `SpEL setValue(..)` 的副作用化
    
- 根據 `StepContext` 的狀態產生動態 value
    
- writer 本身不回傳新 context，只做副作用
    

writer 只在 `BehaviorStep` 中執行，且不改變 Validation 的成功／失敗狀態。

適用於：

- workflow 更新
    
- 標誌變更
    
- 自動填寫欄位
    

---

## **④ `validationFunction(varsProvider)` → `Function<StepContext, Validation<Violations, R>>`**

這是最強大的操作子。

語意：

1. 執行 `SpEL`
    
2. 若成功 → Validation.valid(R)
    
3. 若失敗（null、型別錯誤、`SpEL` error） → Validation.invalid(Violations)
    

此時錯誤已被 `ExpressionOperation` 包裝成 Violations，  
`ExpressionSteps.validatorWithAttribute(...)` 只負責將結果放進 Context。

這使得 `SpE`L 驗證能與整體 Validation error flow 無縫整合。

適用於：

- 必填欄位驗證
    
- 跨欄位條件驗證
    
- 格式驗證
    
- 任何需要 Validation 語意的規則
    

---

# 6.5 `ExpressionOperation` 的錯誤訊息模型

本實作中的關鍵特性：

### **1. `SpEL` 例外不會傳播出去（不會炸掉 pipeline）**

`ExpressionEvaluation` 的 `evaluateValue` 與 `getValue` 系列方法：

```java
Optional.map(...)
	.orElse(null)
```

這表示：

- `SpEL` 無法解析 → null
    
- 單純讀取不存在欄位 → null
    
- 方法找不到 → null
    
- 型別無法轉換 → null
    
- 評估失敗 → null
    

這讓 ExpressionOperation 的四個操作子都能保持：

> **錯誤不丟出、行為語意一致**

---

### **2. 真正的錯誤會在 ExpressionSteps 中被轉成 Violations**

像 predicate(...) 裡的：

```java
.filter(predicate, onInvalid)
```

或 validatorWithAttribute 裡的：

```java
validationFunction(...) → Validation<Violations, R>
```

這表示：

- `SpEL` 執行層只負責安全 evaluation
    
- Validation error flow 完全由 `ExpressionSteps` 控制
    

這是優秀的分層。

---

# 6.6 `ExpressionOperation` 在整個架構中的位置

你整個流程的組成：

```
[SpEL 字串]
    ↓ ExpressionEvaluations.of()
[ExpressionOperation]
    ↓ predicate/reader/writer/validationFunction
[操作子 Operator]
    ↓ ExpressionSteps
[BehaviorStep<T>]
    ↓ BehaviorPipeline / ServiceChain
[Monad Pipeline（Validation + StepContext）]
```

可以看出 `ExpressionOperation` 的精準定位：

> **它是 `BehaviorStep` 的建構原料（ingredient），  
> 而不是流程本身。**

流程是由 `BehaviorPipeline` 組合出來的。

---

# 6.7 `CH6` 小結

**`ExpressionOperation` 的核心價值：**

1. **以高安全性的方式封裝 `SpEL`**
    
2. **以 `predicate / reader / writer / validatorFunction` 四種語意輸出操作子**
    
3. **與 `StepContext、varsProvider` 深度整合**
    
4. **錯誤不丟出、行為可控、流程一致**
    
5. **讓 `ExpressionSteps` 能把操作子包成 `BehaviorStep`**
    
6. **進而讓 `SpEL` 成為 `BehaviorPipeline` 的可組合行為積木**
    

一句話：

> **`CH6` 是「描述 `SpEL` 行為」的層，  
> `CH7` 是「把行為變成 Step」的層，  
> `CH8–CH9` 是「編排 Step 的流程」的層。**

---
