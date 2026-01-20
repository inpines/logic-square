# `Behavior Pipeline × eip-inbound`  
## 整體架構與責任分工說明

本章說明 **`behavior-pipeline`** 與 **`eip-inbound`** 之間的設計關係，
以及它們在整體系統中的分工位置。

這不是兩個競爭的框架，而是**上下層次清楚、責任互補的設計**。

---

## 1. 先給結論（架構一句話）

> **behavior-pipeline 是通用的流程語言，  
> eip-inbound 是專注於 Inbound 的控制平面。**

兩者的關係是：

- behavior-pipeline：**How to run a process**
- `eip-inbound`：**How an external event enters and is controlled**

---

## 2. 為什麼要分成兩套？

在實務中，Inbound 處理常混合三種不同層次的問題：

1. **流程怎麼跑**（步驟順序、錯誤累積、是否中斷）
2. **外部事件是什麼**（來源、meta、payload、身份、狀態）
3. **事件接下來要怎麼被對待**（`ack / retry / dlq`）

如果這三者混在一起，會導致：

- `DSL` 無法重用
- 決策邏輯分散
- `Inbound` 與業務流程糾纏

因此設計上刻意拆分：

| 層次 | 負責模組 |
|----|----|
| 流程語言 | behavior-pipeline |
| Inbound 語意與控制 | eip-inbound |
| 業務行為 | domain / application layer |

---

## 3. behavior-pipeline 的角色定位

### 3.1 它解決什麼問題？

behavior-pipeline 提供的是一套**通用流程模型**：

- `BehaviorStep`
- `StepContext`
- `Validation / Violations`
- `Pipeline / Flow control`

它關注的是：

> **「一組步驟如何被執行、組合與控制」**

而不是：

- 事件從哪裡來
- `retry / dlq` 是什麼
- `message status` 如何定義

---

### 3.2 behavior-pipeline 不知道的事

behavior-pipeline **刻意不知道**：

- `HTTP / MQ / MQTT`
- `Inbound meta schema`
- `MessageStatus / Failure`
- `ControlDecision`

這讓它能被用在：

- Inbound
- Outbound
- 批次處理
- 純 domain 流程

---

## 4. `eip-inbound` 的角色定位

### 4.1 它解決什麼問題？

`eip-inbound` 專注於一件事：

> **外部事件如何被語意化、觀察，並產生處置決策**

它提供：

- `InboundEnvelope`（統一輸入）
- `InboundFlow`（進場流程）
- `InboundScope / Views`（決策視圖）
- `StatefulGate`（stateful concern 聚合）
- `Failure / ControlDecision`（處置模型）

---

### 4.2 `eip-inbound` 不取代流程語言

`eip-inbound` **不自己定義流程執行模型**，而是：

- 建立在 `BehaviorStep / StepContext` 之上
- 產出的仍然是 `BehaviorStep`
- 交由 `behavior-pipeline` 或 `Flow` 執行

換句話說：

> **`eip-inbound` 是 `behavior-pipeline` 的「Inbound 擴展語意層」。**

---

## 5. 兩者如何實際協作？

### 5.1 高層流程視角

```text
[ External Event ]
        ↓
   InboundEnvelope
        ↓
   InboundFlow (stateless)
        ↓
   StatefulGate (BehaviorStep)
        ↓
   BehaviorPipeline / Domain Steps
        ↓
   Handler executes ControlDecision
````

---

### 5.2 職責對照表

|關注點|behavior-pipeline|eip-inbound|
|---|---|---|
|Step / Context|✅|使用|
|Pipeline / Flow|✅|銜接|
|Envelope / Meta|❌|✅|
|Claims / Status|❌|✅|
|Violation|✅|使用|
|Failure / Decision|❌|✅|
|Ack / Retry / DLQ|❌|描述（不執行）|

---

## 6. 為什麼 eip-inbound 不直接做 Pipeline？

一個常見疑問是：

> 為什麼 eip-inbound 不直接提供完整 Pipeline？

原因是刻意的：

- Pipeline 是**通用語言**
    
- Inbound 只是 Pipeline 的一種應用場景
    
- 若 `eip-inbound` 自帶 Pipeline，會反向污染抽象層級
    

因此：

> **Inbound 的「特殊性」被限制在語意模型，而不是流程機制。**

---

## 7. 錯誤模型的銜接方式

### 7.1 Violation → Failure 的分層

- **Violation**（behavior-pipeline）
    
    - 流程內結構性錯誤
        
    - `validation / binding` 問題
        
- **Failure**（eip-inbound）
    
    - 決策層錯誤事實
        
    - 用於 `retry / dlq` 判斷
        

這兩者的分離，避免了：

- 用 validation 決定 retry
    
- 用 exception 表示流程錯誤
    

---

## 8. Handler / Runtime 的責任邊界

重要原則：

> **`behavior-pipeline` 與 `eip-inbound` 都不執行 side-effect。**

真正做以下事情的是：

- `HTTP framework`
    
- `MQ client`
    
- `Scheduler`
    
- `File processor`
    

它們根據 `ControlDecision`：

- `ack`
    
- `schedule retry`
    
- `send to DLQ`
    
- `log / alert`
    

---

## 9. 設計上的刻意限制

這套架構刻意不做：

- 自動 `retry`
    
- 自動 `ack`
    
- 隱性流程中斷
    
- 框架綁定
    

因為：

> **可治理系統需要的是顯式決策，而不是聰明的隱性行為。**

---

## 10. 總結

- behavior-pipeline 是流程語言
    
- `eip-inbound` 是 Inbound 控制平面
    
- 兩者層次不同、責任互補
    
- 一起使用時，Inbound 才能既可控又可重用
    

---

> **當流程語言與 Inbound 語意分離，  
> 系統才能在複雜環境中保持清晰。**

