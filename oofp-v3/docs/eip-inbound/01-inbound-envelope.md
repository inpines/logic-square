# 01. Inbound Envelope（外部事件的語意入口）

本章定義 **eip-inbound 的第一個不可變邊界**：  
外部事件如何被接收、正規化，並安全地進入流程世界。

---

## 1. 為什麼需要 Inbound Envelope？

在多數系統中，Inbound 資料往往以以下形式出現：

- HTTP request / headers / body
- MQ message / properties / bytes
- MQTT topic / metadata / payload
- File / path / content

這些資料通常被**直接丟進 handler 或 service**，導致：

- transport 細節滲入業務邏輯
- meta / payload 混雜
- 不同來源的處理方式無法統一

`eip-inbound` 的第一個設計決定是：

> **所有外部事件，必須先被語意化，才能進入流程。**

---

## 2. `InboundEnvelope<T>`：最小共同語言

### 2.1 定位

`InboundEnvelope<T>` 是外部事件的**最小語意封裝**，不包含任何流程或業務含義。

它只回答五件事：

- 事件來自哪裡？
- 來源識別是什麼？
- 有哪些原始 meta？
- payload 是什麼？
- 什麼時候接收到？

---

### 2.2 結構概念

`InboundEnvelope` 包含以下概念性欄位：

- **`source`**  
  事件來源類型（`HTTP / MQ / MQTT / FILE / …`）

- **`sourceId`**  
  `URI / topic / queue / file path` 等識別資訊

- **`meta`**  
  來自 transport 的原始 metadata（尚未驗證）

- **`payload`**  
  實際內容（`byte[] / String / Json / POJO…`）

- **`receivedAt`**  
  接收時間（由系統標記）

> Envelope 本身**不驗證 payload，也不解讀 meta**。

---

### 2.3 設計原則

- Envelope **不拋錯**
- Envelope **不做 validation**
- Envelope **不帶流程狀態**

它只是「事件存在過」的事實記錄。

---

## 3. `InboundAdapters`：從 transport 到 Envelope

### 3.1 Adapter 的角色

`InboundAdapters` 負責：

- 從不同 `transport` 建立 `InboundEnvelope`
- 填入 `source / sourceId / meta / payload`
- 保證 `Envelope` 本身是可建構的

Adapter **不做語意判斷**，也不決定事件是否有效。

---

### 3.2 常見 Adapter 類型

實務上常見的 adapter 包括：

- `HTTP` → `InboundEnvelope`
- `MQTT` → `InboundEnvelope`
- `MQ / Kafka` → `InboundEnvelope`
- `File` → `InboundEnvelope`
- `Generic source` → `InboundEnvelope`

這些 adapter 的共同特性是：

> **只負責「建立封裝」，不負責「判斷對錯」。**

---

## 4. Meta 的處理策略

### 4.1 為什麼 meta 不能直接用？

Transport meta 通常具有以下特性：

- key 名稱不一致（Header / Property / Attribute）
- 可能包含大量不必要資訊
- 來源不可信（外部輸入）

因此 meta 必須被視為 **不安全輸入**。

---

### 4.2 Meta Schema：白名單策略

`eip-inbound` 對 meta 採取明確的白名單策略：

- **allowed keys**：允許保留的欄位
- **required keys**：必須存在的欄位
- **rename rules**：統一 key 命名
- **normalization**：trim / empty → null / 型別轉換

Schema 的目標不是「盡量保留」，而是：

> **只留下對流程有意義的事實。**

---

### 4.3 Normalization 的時機

`Meta` 正規化 **不在 `Adapter` 階段進行**，  
而是在 `Envelope` 轉換為 `StepContext` 時統一處理。

這確保：

- Adapter 保持單純
- 所有 meta 規則集中在一處
- validation 結果可以產生 Violation

---

## 5. Envelope → `StepContext` 的邊界

### 5.1 Translator 的責任

`EnvelopeTranslators.toStepContext` 是一個**關鍵邊界**，負責：

- 檢查 envelope 是否存在
- 檢查 payload 是否缺失
- 套用 meta schema 與 normalization
- 將結果寫入標準 attributes

這一步是：

> **外部世界 → 流程世界 的唯一入口。**

---

### 5.2 唯一寫入原始 Envelope 的地方

設計上刻意規定：

- `InboundAttrKeys.ENVELOPE`  
  **只能在 translator 階段寫入**
- 之後的 Step **只能讀取，不可覆寫**

這個限制的目的在於：

- 保留原始事件事實
- 防止流程中途竄改輸入
- 讓除錯與回放具備依據

---

## 6. Stateless Inbound 的完成定義

完成以下條件後，事件被視為「已進入流程世界」：

- 有 StepContext
- 有正規化後的 meta
- 有 payload（或明確的 payload.missing violation）
- 有來源識別資訊

此時，事件尚未：

- 綁定身份
- 查詢狀態
- 做出處置決策

這些都屬於 **Stateful Concern**，將在後續章節處理。

---

## 7. 本章小結

- InboundEnvelope 是外部事件的最小語意封裝
- Adapter 只負責建立 Envelope，不做判斷
- Meta 必須經過白名單與正規化
- Translator 是外部世界進入流程世界的唯一邊界
- 原始 Envelope 一旦寫入即不可變

---

> **如果 Inbound 的入口不乾淨，後面的流程只會越來越混亂。**
