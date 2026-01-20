# BehaviorPipeline（流程執行模型）

本章說明 `BehaviorPipeline` 在整個 DSL 中的角色與執行語意。
如果說 `BehaviorStep` 描述的是「流程中發生的事情」，
那麼 `BehaviorPipeline` 描述的就是「這些事情如何被依序執行」。

---

## 1. Pipeline 的角色與邊界

### 1.1 Pipeline 不是業務邏輯容器

`BehaviorPipeline` 的責任**不包含**：

- 業務判斷
- 資料轉換
- 錯誤內容的定義

Pipeline **只負責流程控制**：

- Step 的執行順序
- valid / invalid 的處理策略
- aborted 的判定
- 最終結果的組裝

---

### 1.2 Pipeline 的輸入與輸出

Pipeline 接收：

- 一個初始輸入資料（payload）
- 一組已組裝好的 Steps
- 一個 resultApplier（將 Context 轉為最終結果）

Pipeline 回傳：

- `Validation<Violations, R>`

這確保 Pipeline 的輸出**永遠具有錯誤語意**。

---

## 2. Pipeline 的組裝模型

### 2.1 steps().with(...)

Pipeline 透過 builder 風格組裝：

```java
BehaviorPipeline.steps()  
.with(stepA)  
.with(stepB)  
.with(stepC)
```

這種設計刻意保持簡單：

- 不提供條件語法糖
- 不隱藏執行順序
- 不嘗試自動最佳化

> **Pipeline 的順序本身就是一種設計決策。**

---

### 2.2 Pipeline 與 Step 的關係

- Pipeline **擁有** Step
- Step **不知道** Pipeline 的存在

這是單向依賴，用以避免循環與隱性耦合。

---

## 3. apply：Fail-Fast Pipeline

### 3.1 行為定義

`apply` 代表 **Fail-Fast 策略**：

- Step 回傳 invalid → 流程立即停止
- aborted 為 true → 流程停止，但不視為錯誤

這是最常見、也最安全的執行模式。

---

### 3.2 執行流程概念

Fail-Fast Pipeline 的概念流程：

1. 建立初始 StepContext（payload + empty violations）
2. 依序執行每個 Step
3. 若 Step 回傳 invalid → 立即回傳錯誤
4. 若 Step 回傳 valid 且 aborted → 停止執行
5. 全部 Step 成功 → 套用 resultApplier

---

### 3.3 invalid 與 aborted 的差異處理

在 apply 模式中：

- **invalid**
  - 代表流程發生問題
  - 立即回傳 Validation.invalid
  - 不再執行後續 Step

- **aborted**
  - 代表流程已語意完成
  - 不視為錯誤
  - 直接進入結果產出

---

### 3.4 resultApplier 的責任

`resultApplier` 的角色是：

> **將最終 Context 轉換為對外輸出。**

它應該：

- 只讀取 Context
- 不再產生新的錯誤
- 不改變流程狀態

這讓「流程執行」與「結果呈現」保持清楚分離。

---

## 4. applyCorrectErrors：Error-Accumulating Pipeline

### 4.1 為什麼需要累積錯誤？

某些場景中，Fail-Fast 並不合適，例如：

- 表單驗證（希望一次回報多個錯誤）
- 批次資料檢核
- 非關鍵錯誤不應阻斷整體流程

這時就需要 **Error-Accumulating 策略**。

---

### 4.2 行為定義

`applyCorrectErrors` 的特性是：

- Step 回傳 invalid → 不中斷流程
- violations 會被累積進 Context
- Step 仍可持續執行
- 最終若 violations 非空 → 回傳 invalid

---

### 4.3 invalid 時的 Context 處理

在 Error-Accumulating 模式中：

- invalid 的 Step **不會提供新的 Context**
- Pipeline 會：
  - 合併 violations
  - 保留原 Context
- aborted 判定改以「目前 Context」為準

這確保流程狀態不會被破壞。

---

### 4.4 最終結果決策

Error-Accumulating Pipeline 的結尾行為：

- 若 Context 含有 violations → Validation.invalid
- 若無 violations → Validation.valid(result)

這將錯誤決策延後到整個流程完成後。

---

## 5. aborted 在 Pipeline 中的角色

### 5.1 aborted 的判定時機

Pipeline 在每個 Step 後都會檢查：

- Context.isAborted()

一旦 aborted 為 true：

- 後續 Step 不再執行
- 流程進入結尾處理

---

### 5.2 aborted 與錯誤無直接關係

重要原則：

> **aborted 不是錯誤，而是流程狀態。**

aborted 可與：

- valid 共存
- invalid 共存（視策略）

Pipeline 不應自行推斷 aborted 的原因。

---

## 6. Pipeline 的設計原則

### 6.1 不做「聰明判斷」

Pipeline 不會：

- 根據 violation severity 自動中斷
- 嘗試推測 Step 意圖
- 動態重排 Step

所有決策都應是**顯式設計**。

---

### 6.2 明確的策略分流

Fail-Fast 與 Error-Accumulating 是：

- 不同策略
- 不同語意
- 不應混用在同一條 Pipeline 中

---

## 7. 本章小結

- **Pipeline 負責流程控制，不負責業務邏輯**
- **Step 決定「發生什麼」，Pipeline 決定「怎麼跑」**
- **Fail-Fast 與 Error-Accumulating 是策略選擇**
- **aborted 是顯式的流程終止訊號**

---

> **當 Pipeline 足夠單純，流程行為才能被清楚理解。**

下一章將專注於流程控制細節：`invalid / aborted / severity` 的語意區分。
