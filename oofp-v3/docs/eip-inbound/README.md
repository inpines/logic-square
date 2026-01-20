# `eip-inbound（Inbound Control Plane）`

**`eip-inbound`** 定義了一套用於處理「外部事件進入系統」的 **Inbound Control Plane**。

它的核心目標不是實作業務邏輯，
而是將來自不同來源（`HTTP / MQ / MQTT / FILE / Scheduled`）的事件，
轉換為 **可觀察、可推理、可決策** 的處理結果。

`eip-inbound` 建立在 **`BehaviorStep / StepContext / Validation`** 之上，
為事件導向系統提供一致、可治理的 Inbound 邊界。

---

## 1. 問題背景

在多數系統中，Inbound 處理常見以下問題：

- 不同 transport 各自實作接收與處理邏輯
- meta / header / payload 混雜在 handler 中
- retry / dlq / ack 決策分散於各層
- 失敗原因只能靠 exception 或 log 事後推測

這些問題的本質是：

> **Inbound 行為沒有被視為一條可被描述與決策的流程。**

---

## 2. `eip-inbound` 的定位

`eip-inbound` **不是**：

- handler framework
- transport adapter 套件
- 通用 utility 集合
- workflow engine

`eip-inbound` **是**：

> **Inbound Event 的語意模型與控制平面（Control Plane）。**

它關注的是：

- 外部事件「是什麼」
- 事件目前「處於什麼狀態」
- 系統「接下來要怎麼處置它」

---

## 3. 核心設計概念總覽

### 3.1 `InboundEnvelope`：統一外部輸入

所有外部事件，首先被封裝為：

- `InboundEnvelope<T>`
  - `source`（`HTTP / MQTT / MQ / FILE / …`）
  - `sourceId`（`URI / topic / path`）
  - `meta`（已正規化）
  - `payload`
  - `receivedAt`

Envelope 是 **跨 transport 的共同語言**，
也是後續所有處理的起點。

---

### 3.2 `StepContext`：進入流程世界

Envelope 不直接進入業務邏輯，
而是先被轉換為 `StepContext`：

- 檢查 payload 是否存在
- 套用 meta schema（白名單 / rename / required）
- 將結果寫入標準 attributes

這一步建立了 **Inbound 與流程語言的邊界**。

---

### 3.3 `InboundFlow`：`Inbound` 的流程入口

`InboundFlow<T>` 定義了事件進入流程的方式：

- `from(InboundEnvelope<T>)`
- `andThen(BehaviorStep<T>)`

`InboundFlow` 是流程的外殼，而非流程本身：
- 不承載業務邏輯
- 不取代 Pipeline
- 不隱藏流程語意

---

### 3.4 `InboundScope` / `Views`：決策用視圖

為了避免 policy / decision logic 直接依賴流程內部結構，
`eip-inbound` 提供：

- `InboundQueryView`
- `InboundDecisionView`
- `InboundScope<T>`

這些是 **只讀聚合視圖**，用於：
- 查詢（query / binding）
- 狀態觀察
- 決策判斷

---

### 3.5 `Failure` 與 `ControlDecision`：處置模型化

在 `eip-inbound` 中：

- **錯誤** → `Failure`
- **下一步處置** → `ControlDecision`

`ControlDecision` 是顯式的結果模型，例如：

- `Ack`
- `Retry`（含` nextRetryAt`）
- `Dlq`
- `Noop`
- `FailInternal`

這讓 `retry / dlq / noop` 不再是隱性行為，
而是可被儲存、觀察與重放的決策結果。

---

## 4. `StatefulGate`：狀態導向的閘門

`StatefulGate<T>` 是一個 **`BehaviorStep` 聚合器**，
用來組裝 Inbound 常見的 `stateful concern`：

1. `Claims binding`（身份 / 授權）
2. `QuerySpec binding`（查詢條件）
3. `Status observation`（狀態 / 失敗歷史）
4. `Decision`（控制決策）

Gate 本身不定義流程，
而是產生一個可被接入 Pipeline 的 Step。

---

## 5. 與 Behavior Pipeline 的關係

- `eip-inbound` **建立在** BehaviorStep / StepContext / Validation 之上
- `Behavior Pipeline` 提供「流程語言」
- `eip-inbound` 提供「Inbound 領域模型與標準步驟」

兩者是 **分工明確、可獨立演進** 的模組。

---

## 6. 文件結構導覽

- **01-`inbound-envelope.md`**  
  Adapters / Envelope / Meta schema / Normalization

- **02-`inbound-flow.md`**  
  `Stateless / Stateful InboundFlow`

- **03-`scope-and-views.md`**  
  `InboundScope / QueryView / DecisionView / AttrKey`

- **04-`stateful-gate.md`**  
  Claims / Query / Status / Decision 的責任邊界

- **05-`decision-policy.md`**  
  `ControlDecision` 與 `retry / dlq` 策略

- **06-`routing-and-split.md`**  
  `RouteTable / Selector / Splitter / QueryTable`

- **07-`design-guidelines.md`**  
  使用規約、設計邊界與反模式

---

## 7. 設計原則摘要

- Inbound 是流程，而不是 `handler`
- 錯誤與處置必須被模型化
- `meta / claims / status` 是決策資料，不是副作用
- 控制決策必須顯式、可觀察、可重放

---

> **當 Inbound 行為被模型化，系統才真正具備治理能力。**
