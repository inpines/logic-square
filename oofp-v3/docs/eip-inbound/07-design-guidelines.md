# 07. Design Guidelines（設計規約與反模式）

本章彙整在使用 eip-inbound 時，**可長期維持可讀性、可治理性與可演進性**的設計規約。
這些規約不是語法限制，而是結構與責任的約定。

---

## 1. 邊界優先的設計原則

### 1.1 Inbound 是邊界，不是業務層

eip-inbound 的責任止於：

- 接收外部事件
- 建立語意化輸入
- 觀察狀態與失敗
- 產生處置決策

它**不負責**：
- domain 行為
- application service 編排
- 最終 side-effect（ack / retry / dlq 的實作）

---

### 1.2 所有外部資料都不可信

設計時必須假設：

- payload 可能缺失或格式錯誤
- meta 可能不完整或被污染
- claims 可能不存在或過期

因此：
- 一律經過 Envelope → Translator
- 不在 handler 中直接使用外部資料

---

## 2. Flow 與 Gate 的使用規約

### 2.1 `Stateless` 與 `Stateful` 必須分離

- `Stateless Flow`：  
  只處理輸入正規化與基本驗證

- `Stateful Gate`：  
  才處理 claims / status / decision

**不要**在 stateless 階段：
- 查資料庫
- 決定 `retry / dlq`
- 綁定身份

---

### 2.2 Gate 是結構，不是邏輯集合

`StatefulGate` 應該：

- 組裝 Step
- 描述順序
- 提供可插拔能力

不應該：
- 實作業務邏輯
- 決定 side-effect
- 隱性中斷流程

---

## 3. Scope / View 的使用規約

### 3.1 View 只讀，Context 可寫

- Policy / Extractor / Observer  
  → 只接收 View / Scope

- Step  
  → 才能寫入 `StepContext`

這確保：
- 決策可測試
- 流程狀態不被污染

---

### 3.2 不要長期保存 Scope

Scope 是：
- 即時視圖
- 決策用快照

不應：
- 被 cache
- 被跨流程傳遞
- 當成 domain model

---

## 4. Decision Policy 的設計規約

### 4.1 Policy 必須是純判斷

Decision Policy 應滿足：

- 無 side-effect
- 不拋 exception（除非系統錯誤）
- 相同輸入 → 相同輸出

---

### 4.2 `Retry / DLQ` 一定要模型化

禁止：
- 用 exception 表示 retry
- 在 handler 中偷做 `ack` / `dlq`

必須：
- 回傳 `ControlDecision`
- 將處置結果視為資料

---

## 5. Routing / Split 的設計規約

### 5.1 Routing 是流程形狀，不是決策

Routing 用於：
- 決定「走哪條流程」

Decision 用於：
- 決定「事件怎麼處置」

不要混用。

---

### 5.2 Split 只負責展開資料

Split 應該：
- 驗證可否展開
- 寫入展開結果

不應該：
- 呼叫 service
- 修改 payload
- 做處置決策

---

## 6. 錯誤與失敗的規約

### 6.1 Violation 與 Failure 的分工

- **Violation**
  - 流程內結構性問題
  - validation / binding 錯誤

- **Failure**
  - 決策層的錯誤事實
  - 用於 retry / dlq 判斷

不要混用兩者。

---

### 6.2 Status 觀察失敗不等於流程失敗

Status observe 失敗時：
- 降級為 UNKNOWN
- 轉為 Failure
- 交由 Decision Policy 處理

避免：
- 直接 invalid
- 中斷流程

---

## 7. 常見反模式（Anti-Patterns）

### 7.1 把 eip-inbound 當成 Workflow Engine

錯誤做法：
- 在 Gate 中寫完整流程
- 在 Routing 中承載業務邏輯

正確做法：
- eip-inbound 只處理 Inbound control
- 業務流程交給 Behavior Pipeline / Use Case

---

### 7.2 在 Step 中直接處置事件

錯誤做法：
- Step 內 `ack`
- Step 內 `retry`
- Step 內丟 `dlq`

正確做法：
- Step 只產生 Decision
- Handler 根據 Decision 執行 side-effect

---

### 7.3 使用 magic string 操作 attributes

錯誤做法：
```text
context.put("status", status)
```

正確做法：

```text
InboundAttrKeys.STATUS.set(context, status)
```

---

## 8. 演進建議

當需求增加時，優先考慮：

1. 新增 Step（而非擴大 Gate）
    
2. 新增 Policy（而非在流程中加 if）
    
3. 新增 Route（而非在 Step 中分支）
    

只有在**現有模型無法表達語意**時，  
才引入新結構。

---

## 9. 本章小結

- `eip-inbound` 是 `Inbound Control Plane`
    
- 邊界清楚比功能多更重要
    
- 決策顯式化是可治理的關鍵
    
- Flow、Gate、Policy 各司其職
    

---

> **好的 Inbound 設計，讓系統在面對不確定性時依然可預測。**