# 02. Inbound Flow（事件如何進入流程）

本章說明 **Inbound 事件如何被組織成一條可執行的流程**，  
以及 stateless 與 stateful concern 之間的清楚分界。

---

## 1. 為什麼需要 Inbound Flow？

在多數系統中，Inbound 處理常被寫成：

- 一個 handler
- 一段 listener callback
- 一個 controller method

這些寫法的共同問題是：

- 處理順序隱性存在
- 錯誤如何處理不明確
- retry / dlq 決策散落在各層

eip-inbound 的核心立場是：

> **Inbound 不是 handler，而是一條流程。**

---

## 2. InboundFlow\<T>：流程外殼

### 2.1 定位

`InboundFlow<T>` 是一個極薄的流程包裝，負責：

- 定義事件「如何進入流程世界」
- 串接後續的 BehaviorStep
- 保持流程語意顯式

InboundFlow 本身**不承載業務邏輯**。

---

### 2.2 概念介面

概念上，InboundFlow 提供兩個能力：

- `from(InboundEnvelope<T>)`
- `andThen(BehaviorStep<T>)`

其本質等價於：

> **Envelope → StepContext → StepContext**

---

### 2.3 為什麼不是 Pipeline？

InboundFlow 的設計目的不是取代 Pipeline：

- Pipeline 用於描述「完整業務流程」
- InboundFlow 僅處理「進場與銜接」

因此 InboundFlow：
- 很薄
- 很短
- 很早被用完

---

## 3. Stateless Inbound Flow

### 3.1 定義

Stateless Inbound Flow 指的是：

- 不依賴任何外部狀態
- 不查詢資料庫
- 不綁定身份
- 不做處置決策

它只負責：

> **把外部事件安全地送進流程世界。**

---

### 3.2 Stateless Flow 的組成

Stateless Flow 通常只包含一個步驟：

1. Envelope → StepContext（translator）

產出結果為：

- `Validation<Violations, StepContext<T>>`

此時流程已具備：

- 標準化 meta
- payload 狀態（存在或缺失）
- 基本來源資訊

---

### 3.3 為什麼要獨立出 Stateless？

將 stateless concern 獨立出來的好處是：

- 測試容易
- 行為可預測
- 不受外部系統影響
- 可被多條流程重用

---

## 4. `Stateful Inbound Flow`

### 4.1 定義

`Stateful Inbound Flow` 指的是：

- 需要外部狀態
- 需要身份 / 授權
- 需要歷史資訊
- 需要做處置決策

這些 concern 被集中在 **`StatefulGate`** 中。

---

### 4.2 `Stateful Flow` 的結構

概念結構如下：

```text
InboundEnvelope
    ↓
Stateless Translator
    ↓
StatefulGate (BehaviorStep)
```

也就是：

> **`Stateless Flow + Gate = Stateful Flow`**

---

### 4.3 為什麼用 Gate？

`StatefulGate` 的設計目的在於：

- 把 `stateful concern` 集中
    
- 提供可插拔的步驟組合
    
- 保持主流程可讀
    

Gate 自身不是流程，而是「流程的一段」。

---

## 5. Flow 與 Error Handling 的關係

### 5.1 Flow 不做決策

`InboundFlow` 的責任是「執行流程」，而不是：

- 決定 `retry` 或 `dlq`
    
- 判斷錯誤嚴重性
    
- 中斷流程
    

所有這些行為都必須是：

> **顯式的 Step 行為或 Decision 結果。**

---

### 5.2 Validation 與 Flow

Flow 的輸出一律是：

- `Validation<Violations, StepContext<T>>`
    

這表示：

- Flow 本身不丟 exception（除非系統錯誤）
    
- 所有可預期問題都以 Violations 表達
    

---

## 6. Flow 的可組合性

### 6.1 串接 `BehaviorStep`

`InboundFlow` 提供 `andThen` 的目的在於：

- 將 Inbound 與既有 `BehaviorStep` 銜接
    
- 不需引入新的流程語言
    
- 保持一致的抽象層級
    

---

### 6.2 與 Behavior Pipeline 的關係

常見組合方式：

```text
InboundFlow
    → StatefulGate
    → BehaviorPipeline
```

或：

```text
InboundFlow
    → StatefulGate
    → Custom Step
```

`InboundFlow` 只負責第一段。

## 7. 常見誤用與設計邊界

### 7.1 把 `InboundFlow` 當成業務流程

`InboundFlow` 不應：

- 包含業務決策
    
- 查詢 domain model
    
- 呼叫 application service
    

這些都屬於後續流程的責任。

---

### 7.2 在 `Stateless` 階段做 `Stateful` 事情

避免在 Stateless Flow 中：

- 查 `DB`
    
- 綁定 `user`
    
- 決定 `retry / dlq`
    

這會破壞流程可預測性。

---

## 8. 本章小結

- `InboundFlow` 是 Inbound 的流程入口
    
- `Stateless` 與 `Stateful concern` 必須分離
    
- `Gate` 是 `stateful concern` 的集中點
    
- `Flow` 本身不做決策，只執行步驟
    

---

> **當 Inbound 有清楚的流程入口，系統的邊界才真正成立。**

