# 05. Decision Policy（處置決策模型）

本章說明 `eip-inbound` 如何將「事件要怎麼處理」
建模為顯式的 **`ControlDecision`**，
以及 `Decision Policy` 在整體流程中的角色與設計原則。

---

## 1. 為什麼需要 Decision Policy？

在多數系統中，Inbound 處置常見做法是：

- 直接 `ack`
- 丟 `exception` 觸發 `retry`
- `catch exception` 後丟 `DLQ`
- 在 `handler` 中寫 `if / else`

這些作法的問題在於：

- 處置行為隱性存在
- 無法回放或觀察
- retry 條件難以測試
- 行為分散在各層

`eip-inbound` 的核心立場是：

> **處置結果必須是一個模型，而不是一段程式碼。**

---

## 2. `ControlDecision`：顯式的處置結果

### 2.1 定位

`ControlDecision` 描述的是：

> **「此 Inbound 事件，接下來系統要怎麼對待它」。**

它是一個 sealed / sum type，
確保所有可能的處置方式都是顯式的。

---

### 2.2 常見 Decision 類型

典型的 `ControlDecision` 包含：

- **`Ack`**  
  確認事件處理完成

- **`Retry`**  
  延後重試（包含 `nextRetryAt`）

- **`Dlq`**  
  送入死信佇列

- **`Noop`**  
  不做任何處置（例如去重命中）

- **`FailInternal`**  
  系統層錯誤或未知狀況

這些 decision 都是「結果」，而不是「動作」。

---

## 3. Failure：決策的輸入資料

### 3.1 Failure 的角色

Decision Policy 不直接看 Violations，
而是基於 **Failure** 做判斷。

Failure 描述的是：

- 發生了什麼錯誤
- 錯誤的分類（taxonomy）
- 是否可重試
- 是否屬於外部或內部問題

---

### 3.2 Failure 與 Violation 的差異

- **Violation**  
  用於流程內的結構性錯誤（validation）

- **Failure**  
  用於決策層的處置判斷

Violation 可能導致 Failure，
但兩者語意不同。

---

## 4. Decision Policy 的責任

### 4.1 Policy 是純函式

Decision Policy 應視為：

> **`InboundScope` → `ControlDecision`**

它不應：

- 修改流程狀態
- 存取 `StepContext`
- 產生 side-effect

---

### 4.2 Policy 的輸入

Decision Policy 通常基於以下資訊判斷：

- `failures`（`List\<Failure>`）
- `currentStatus`（`MessageStatus`）
- `claims`（是否授權 / 匿名）
- `meta`（`transport` 相關事實）
- `now`（決策時間）

---

## 5. Taxonomy-driven Decision

### 5.1 為什麼用 Taxonomy？

錯誤碼（string）本身不足以決策：

- 無法判斷可否重試
- 無法區分外部 / 內部
- 易碎、難以擴充

因此 eip-inbound 採用 **ErrorTaxonomy**：

- VALIDATION
- UNAUTHORIZED
- CONFLICT
- NOT_FOUND
- TRANSIENT_DEPENDENCY
- INTERNAL
- …

Decision Policy 以 taxonomy 為主要分支依據。

---

### 5.2 範例：retry / dlq 策略概念

概念決策流程如下：

```text
failures empty
    → Ack

CONFLICT
    → Noop

VALIDATION / UNAUTHORIZED
    → Dlq

TRANSIENT_DEPENDENCY / NOT_FOUND
    → Retry (if attempt < max)
    → Dlq  (if exceeded)

otherwise
    → FailInternal
```

---

## 6. Retry 的建模方式

### 6.1 Retry 是 Decision，不是 Exception

Retry 不是：

- throw exception
    
- return false
    
- 隱性延遲
    

Retry 是一個包含資訊的結果：

- `nextRetryAt`
    
- reason
    

這讓 retry 策略可以被：

- 記錄
    
- 觀察
    
- 調整
    

---

### 6.2 `Backoff` 策略

Decision Policy 可以實作：

- 固定延遲
    
- 指數回退
    
- 自訂時間表
    

這些都屬於 policy 的責任，而非流程本身。

---

## 7. Policy 與 Gate 的關係

- Policy 被 Gate 呼叫
    
- Gate 將 Decision 寫回流程狀態
    
- Gate 不解讀 Decision 的後果
    

實際執行（ack / retry / dlq）：

> **屬於流程外或 handler 層的責任。**

---

## 8. 常見誤用與風險

### 8.1 在 Policy 中拋 Exception

Decision Policy 不應：

- 用 exception 表示 `retry`
    
- 用 exception 表示 `dlq`
    

Exception 只用於真正的系統失敗。

---

### 8.2 在 Step 中偷做處置

Step 不應：

- 自行 `ack`
    
- 自行 `retry`
    
- 自行丟 `dlq`
    

這會破壞決策可觀察性。

---

## 9. 本章小結

- `ControlDecision` 是顯式的處置結果
    
- `Decision Policy` 是純判斷邏輯
    
- `Failure 與 Taxonomy` 是決策基礎
    
- `retry / dlq` 行為應被模型化
    

---

> **當處置被模型化，系統行為才能被治理與演進。**
