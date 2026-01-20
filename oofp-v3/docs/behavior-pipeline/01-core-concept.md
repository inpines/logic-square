# 核心概念（Core Concepts）

本章說明 `Behavior Step / Pipeline` 的核心概念與設計語言。
在閱讀具體 `API` 與範例之前，請先理解這裡所定義的幾個關鍵角色，
它們構成了整套 `DSL` 的「思想骨架」。

---

## 1. 行為不是方法，而是流程

在這套設計中，「行為（`Behavior`）」**不是**：

- 一個 `method`
- 一段 `if / else`
- 一次 `function call`

而是：

> **資料在一連串步驟中，被檢視、轉換、標記與回應的過程。**

因此，`Behavior` 的基本單位不是 `method`，而是 **`Step`**；
`Behavior` 的呈現方式不是呼叫，而是 **`Pipeline`**。

---

## 2. Step（行為步驟）

### 2.1 Step 是什麼？

一個 `Step` 代表流程中的**單一行為節點**：

- 接收當前流程狀態
- 做一件清楚、有限的事情
- 回傳更新後的流程狀態，或錯誤

它遵循以下最小合約：

- 不假設前後步驟的存在
- 不直接操作外部流程
- 不隱性中斷流程

---

### 2.2 Step 的責任邊界

一個 `Step` **可以做**：

- 檢查 `payload` 是否符合條件
- 根據結果加入 `violations`
- 轉換 `payload（transit）`
- 寫入 `attributes`
- 決定是否 `aborted`

一個 Step **不應該做**：

- 組裝 `Pipeline`
- 決定最終回傳格式
- 同時處理多個不相干責任
- 以 `Exception` 作為主要控制流程

---

## 3. Pipeline（行為流程）

### 3.1 Pipeline 的角色

`Pipeline` 的責任只有一個：

> **依序執行 `Step`，並依規則處理流程控制。**

它本身不包含業務邏輯，也不理解每個 `Step` 在做什麼。

`Pipeline` 負責：

- `Step` 的執行順序
- `invalid` 時是否中斷
- `aborted` 的判定時機
- 最終結果的產出

---

### 3.2 Fail-Fast 與 Error-Accumulating

Pipeline 支援兩種典型策略：

- **Fail-Fast**  
  一旦 `Step` 回傳 `invalid`，流程立即停止

- **Error-Accumulating**  
  `invalid` 不中斷流程，而是累積 `violations`，直到結尾統一處理

這不是 `Step` 的責任，而是 **`Pipeline` 的策略選擇**。

---

## 4. Context（流程上下文）

### 4.1 為什麼需要 Context？

`Context` 是 `Step` 之間**唯一合法的溝通方式**。

沒有 `Context`：

- `Step` 只能靠共享狀態溝通
- 流程狀態無法被完整描述
- 錯誤只能靠 `Exception` 傳遞

有了 `Context`：

> **流程狀態被模型化，而不是隱藏在控制結構中。**

---

### 4.2 Context 承載的四種資訊

Context 包含四個核心面向：

1. **`payload`**  
   主資料，代表流程當下處理的對象

2. **`attributes`**  
   輔助資訊，用於跨 Step 傳遞中間結果或旗標

3. **`violations`**  
   已累積的錯誤集合

4. **`aborted`**  
   顯式標記流程是否應中止

這四者共同構成「流程的當前狀態」。

---

## 5. Validation（流程結果模型）

### 5.1 為什麼不是 `Exception`？

在這套設計中，錯誤不是「意外」，而是流程的一部分。

因此：

- `Exception` 用於「不可恢復的系統錯誤」
- `Validation` 用於「可被理解與處理的流程結果」

`Validation` 清楚區分：

- **`valid`**：流程可繼續
- **`invalid`**：流程需依策略回應

---

### 5.2 Validation 與 Context 的關係

Step 的 execute 方法回傳：

`Validation<Violations, StepContext<T>>`


代表：

- 成功 → 提供新的 `Context`
- 失敗 → 提供 `Violations`

Pipeline 再根據策略，決定是否繼續或中斷。

---

## 6. Violations（錯誤不是單一事件）

### 6.1 為何錯誤需要可累積？

實務中常見需求包括：

- 同時回報多個欄位錯誤
- 區分 `warning` 與 `severe`
- 延後錯誤決策到流程結尾

因此 `Violations` 被設計為：

- 可 `join`
- 可過濾
- 可分類
- 可轉換成最終回應

---

### 6.2 錯誤的生命週期

1. `Step` 產生 `violation`
2. `Context` 累積 `violation`
3. `Pipeline` 根據策略處理
4. 最終轉換為回應或中斷

錯誤**不再是流程的敵人，而是流程的產物**。

---

## 7. aborted：顯式中斷，而非失敗

### 7.1 `aborted` 與 `invalid` 的差異

- **`invalid`**：流程發生問題
- **`aborted`**：流程不再需要繼續

`aborted` 不代表錯誤，例如：

- 條件不成立，後續步驟不必再跑
- 已取得足夠結果
- 流程被策略性終止

---

### 7.2 為什麼 aborted 要是顯式的？

因為：

- 中斷是一種決策
- 決策應該被清楚表達
- 不應藏在 `if / return / exception` 中

---

## 8. 核心概念總結

- **Behavior**：資料在流程中的整體變化
- **Step**：單一、可組合的行為單元
- **Pipeline**：負責執行與控制流程
- **Context**：流程狀態的唯一載體
- **Validation**：流程結果的表達方式
- **Violations**：可被理解與處理的錯誤集合
- **aborted**：顯式的流程中止語意

---

> **理解這些概念之後，API 設計不再是記憶問題，而是自然推論。**

下一章將說明這些概念如何被具體落實為資料模型。
