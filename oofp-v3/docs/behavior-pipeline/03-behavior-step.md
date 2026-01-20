# BehaviorStep（行為步驟）

本章說明 Behavior Pipeline 中最小且最重要的組成單位：**BehaviorStep**。

如果說 Pipeline 定義了「流程如何被執行」，
那麼 Step 定義的就是「流程中發生了什麼事」。

---

## 1. Step 是什麼？

### 1.1 最小行為單位

一個 BehaviorStep 代表流程中的**單一行為節點**。

它的角色是：

- 接收當前的 StepContext
- 執行一件明確、有限的行為
- 回傳新的流程狀態，或對應的錯誤

Step **不負責**：

- 決定流程順序
- 組裝 Pipeline
- 決定最終輸出格式

---

### 1.2 Step 的最小合約

每個 Step 都遵循相同的合約：

`execute(StepContext<T>)  → Validation<Violations, StepContext<T>>`

這個合約表達了三件事：

1. Step **一定知道當前流程狀態**
2. Step **可能成功，也可能失敗**
3. Step **不直接中斷流程**

流程是否繼續，永遠由 Pipeline 決定。

---

## 2. 建立 Step 的方式

### 2.1 `BehaviorStep.of`

最直接的方式，將一個 function 視為 Step：

- 輸入：`StepContext`
- 輸出：`Validation`

適用於：
- 純轉換
- 檢核與標記
- 無延遲需求的行為

---

### 2.2 BehaviorStep.supply（延遲建立）

`supply` 用於 **Step 本身需要延遲建立** 的情境：

- Step 依賴 runtime 狀態
- Step 需要注入動態資源
- 避免過早初始化

重點在於：

> Step 的建立被延後，但執行語意不變。

---

### 2.3 BehaviorStep.chain

`chain` 用於將多個 Step 組合為單一 Step。

這在以下情境特別有用：

- 將一組固定流程包裝成 reusable Step
- 隱藏內部細節，對外只暴露一個行為節點

---

## 3. Step 的組合語意

### 3.1 andThenStep

`andThenStep` 表示：

> **前一個 Step 成功後，才執行下一個 Step。**

它是 Step 層級的流程串接方式，
對應的是「局部 pipeline」。

---

### 3.2 andThenMapper

`andThenMapper` 用於：

- 不產生錯誤
- 僅修改 Context

適合：
- payload 轉換
- attribute 補充
- 狀態標記

---

### 3.3 when（條件式 Step）

`when(predicate, step)` 表示：

- 條件成立 → 執行 step
- 條件不成立 → 原 Context 原樣通過

關鍵在於：

> 條件判斷本身**不屬於 Step 行為**，而是 Step 的使用方式。

---

## 4. 常用 Step 行為模式（DSL）

### 4.1 requirePayload（前置條件）

`requirePayload` 用於描述：

> **某個條件若不成立，流程即產生語意化錯誤。**

它的特點是：

- 失敗時回傳 invalid
- 不丟出 Exception
- 錯誤被模型化為 Violations

適合用於：
- 必要欄位檢核
- 狀態前置條件
- 流程門檻檢查

---

### 4.2 peek（成功時觀察）

`peek` 是**非侵入式的觀察行為**：

- 僅在 valid 時執行
- 不影響 Context
- 不影響流程結果

常用於：
- logging
- metrics
- debug 觀察

---

### 4.3 peekOnError（錯誤觀察）

與 peek 相對，`peekOnError`：

- 僅在 invalid 時觸發
- 不改變錯誤內容
- 用於錯誤紀錄或統計

---

### 4.4 recover（錯誤修復）

`recover` 表示：

> **在錯誤發生後，嘗試產生新的 payload，讓流程得以繼續。**

這是一個高風險、但必要的能力。

使用 recover 時，應注意：

- recovery 本身可能失敗
- recovery 失敗時，應產生新的 violation
- recover 不應掩蓋結構性錯誤

---

## 5. Step 設計原則

### 5.1 單一責任

一個 Step 應該只做一件事：

- 一個檢核
- 一個轉換
- 一個決策

若 Step 需要同時做多件事，通常代表它該被拆分。

---

### 5.2 無隱性副作用

Step 不應：

- 修改外部狀態
- 依賴全域變數
- 假設某個 Step 一定在它之前執行

Step 的行為應完全可由輸入 Context 推論。

---

### 5.3 錯誤即資料

Step 不應將錯誤視為例外情況，
而是將錯誤**轉換為 Violations**。

這讓錯誤成為可觀察、可處理的流程資訊。

---

## 6. 常見誤用（預告）

以下情況通常代表 Step 設計有問題：

- 在 Step 中組裝 Pipeline
- 在 Step 中決定最終回傳格式
- 將大量 if / else 塞進單一 Step
- 用 recover 取代正常流程設計

這些將在後續「反模式」章節中詳述。

---

## 7. 本章小結

- **BehaviorStep 是流程的最小行為單元**
- **Step 本身不控制流程，只回報結果**
- **DSL 方法只是輔助，核心仍是 execute 合約**
- **好的 Step 設計，會讓 Pipeline 變得單純**

---

> **當 Step 足夠清楚，Pipeline 就只剩下排列組合的問題。**

下一章將說明 Step 如何被組裝並實際執行：`BehaviorPipeline`。
