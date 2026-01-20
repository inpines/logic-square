# 04. Stateful Gate（狀態導向的處理閘門）

本章說明 **StatefulGate** 在 eip-inbound 中的角色與設計原則，
以及它如何將多個 stateful concern 組合為一個可重用的流程片段。

---

## 1. 為什麼需要 Stateful Gate？

在 Inbound 流程中，以下行為往往反覆出現：

- 綁定身份（claims）
- 擷取查詢條件（query spec）
- 查詢並觀察訊息狀態
- 根據狀態與錯誤做出處置決策

如果這些行為直接散落在 Pipeline 中，會導致：

- 流程冗長
- stateful concern 與業務流程混雜
- 不同 Inbound 流程難以共享結構

StatefulGate 的核心目的在於：

> **把常見的 stateful concern，收斂成一個語意清楚的流程閘門。**

---

## 2. StatefulGate\<T> 的定位

### 2.1 不是流程，不是服務

StatefulGate **不是**：

- 一條完整流程
- 一個 service
- 一個 handler

StatefulGate **是**：

> **一個可被插入流程的 BehaviorStep 聚合器。**

---

### 2.2 輸入與輸出

- **輸入**：`StepContext<T>`
- **輸出**：`Validation<Violations, StepContext<T>>`

這確保 StatefulGate 能：

- 接入 InboundFlow
- 接入 BehaviorPipeline
- 被視為普通的 Step 使用

---

## 3. Gate 的內部結構

### 3.1 四個典型步驟

一個典型的 `StatefulGate` 可能包含四個可選步驟：

1. **`Claims Binding`**  
   解析與綁定身份 / 授權資訊

2. **`QuerySpec Binding`**  
   從 `InboundQueryView` 擷取查詢條件

3. **Status Observation**  
   查詢訊息狀態與歷史失敗

4. **Decision**  
   根據 `Scope` 做出 `ControlDecision`

這四個步驟彼此獨立，可插拔。

---

### 3.2 Gate 組裝，而非 Gate 決策

`StatefulGate` **負責組裝步驟**，
但不負責定義它們的實作細節。

換言之：

> **Gate 描述「順序與存在性」，不描述「怎麼做」。**

---

## 4. Gate 與 Views 的關係

### 4.1 Step 只看 View，不看 Context

Gate 中的每個 Step 都遵守：

- 從 `StepContext` 建立 `View / Scope`
- 基於 `View` 做判斷
- 將結果寫回 `StepContext`

這確保：

- 決策邏輯不耦合流程實作
- View 可被單獨測試

---

### 4.2 View → Context 的回寫

每個 Step 只負責寫入自己關心的資料：

- `Claims step` → CLAIMS
- `Query step` → QUERY_SPEC
- `Status step` → STATUS / FAILURES
- `Decision step` → NEXT_DECISION

這避免：

- attribute 污染
- 跨 Step 的隱性依賴

---

## 5. Gate 的失敗處理策略

### 5.1 不同類型的失敗，不同處理方式

Gate 中不同步驟，對失敗有不同的語意處理：

- **Claims / Query binding 失敗**  
  → 產生 Violations（invalid）

- **Status observation 失敗**  
  → 不 invalid，轉為 Failure（降級處理）

- **Decision policy 失敗**  
  → 產生 FailInternal Decision

這些差異是**刻意設計**，而非不一致。

---

### 5.2 為什麼 Status 失敗不 invalid？

因為狀態查詢失敗時：

- 事件仍然存在
- 可以選擇 retry / dlq / noop
- 不應直接中斷流程

Gate 的設計允許：

> **用決策處理不確定性，而不是用例外終止流程。**

---

## 6. Gate 的可重用性

### 6.1 不同 Inbound 流程，共用 Gate

同一個 Gate 可被用於：

- HTTP Inbound
- MQ Consumer
- MQTT Listener
- File Import

只要：

- Scope 語意一致
- 決策策略一致

---

### 6.2 多個 Gate 的組合

在較複雜場景中，可以：

- 使用多個 Gate
- 或在 Gate 之前 / 之後插入其他 Step

Gate 本身不限制流程結構。

---

## 7. 常見誤用與邊界

### 7.1 在 Gate 中加入業務邏輯

Gate 不應：

- 實作 domain 行為
- 修改 payload
- 呼叫 application service

---

### 7.2 讓 Gate 自動中斷流程

Gate 不應：

- 隱性 aborted
- 根據 severity 自動停止
- 吞掉錯誤

中斷必須是顯式設計。

---

## 8. 本章小結

- `StatefulGate` 是 stateful concern 的集中點
- `Gate` 組裝 Step，但不承擔實作責任
- 不同步驟有不同失敗語意
- `Gate` 可被安全地重用與組合

---

> **Gate 的價值不在於它做了什麼，而在於它界定了什麼應該在這裡發生。**
