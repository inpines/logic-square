# 04. Stateful Gate（狀態導向的處理閘門）

本章說明 **StatefulGate** 在 eip-inbound 中的角色與設計原則，以及它如何將多個 *stateful concern* 組合為一個可重用的流程片段。

---

## 1. 為什麼需要 Stateful Gate？

在 Inbound 流程中，下列行為高度重複，且具有明確的「狀態語意」：

- 綁定身份與授權資訊（claims / auth context）
- 擷取查詢條件（query spec）
- 查詢並觀察訊息狀態與歷史失敗
- 根據狀態與錯誤做出處置決策（ack / retry / dlq）

若這些行為直接散落在 Pipeline 中，容易導致：

- 流程冗長、閱讀困難
- stateful concern 與業務流程混雜
- 不同 Inbound 流程難以共享結構

**StatefulGate 的核心目的在於：**

> 將常見的 *stateful concern* 收斂成一個語意清楚、可插拔、可重用的流程閘門。

---

## 2. StatefulGate<T> 的定位

### 2.1 不是流程，不是服務

StatefulGate **不是**：

- 一條完整流程
- 一個 service
- 一個 handler

StatefulGate **是**：

- 一個可被插入流程的 **BehaviorStep 聚合器**

它只負責**組裝順序與存在性**，不負責實作細節。

---

### 2.2 輸入與輸出

- **輸入**：`StepContext<T>`
- **輸出**：`Validation<Violations, StepContext<T>>`

這確保 StatefulGate 能：

- 接入 `InboundFlow`
- 接入 `BehaviorPipeline`
- 被視為一個普通的 `BehaviorStep`

---

## 3. Gate 的內部結構

### 3.1 四個典型步驟（可選）

一個典型的 StatefulGate 可能包含以下四個 *stateful step*：

1. **Claims / Auth Binding**
   解析與綁定身份與授權相關資訊

2. **QuerySpec Binding**
   從 `InboundQueryView` 擷取查詢條件

3. **Status Observation**
   查詢訊息狀態與歷史失敗（failures）

4. **Decision**
   根據 Scope 與 Failures 做出 `ControlDecision`

這四個步驟彼此獨立、可插拔，Gate 只負責組裝。

---

### 3.2 Gate 組裝，而非 Gate 決策

StatefulGate **描述的是結構，不是策略**：

- Gate 決定「有哪些 stateful step、順序如何」
- 每個 Step 決定「怎麼做」

換言之：

> **Gate 描述「應該在這裡發生什麼」，而不是「具體怎麼發生」。**

---

## 4. Claims / Auth Binding 的角色擴充

在實務上，Claims Binding 通常包含以下三段可選行為，仍然屬於 *stateful concern*：

### 4.1 AuthContext Binding

- 從 `Authentication` / JWT / SecurityContext
- 抽取最小且可信的身份事實：

  - `principalId`
  - `authorities`
  - `tokenId`
- 寫入 `AUTH_CONTEXT`

此步驟只做「**身分事實的綁定**」，不做授權判斷。

---

### 4.2 Entitlements Resolution（可選）

若 token 僅包含 role、或未包含細部權限：

- 可透過 `EntitlementsResolver` 以 `principalId` 補齊：

  - `roles`
  - `roleGroups`
  - `authorities`
- 以 `AuthContexts.enrich(..)` **僅做 union 合併** 回 `AUTH_CONTEXT`

> Resolver 的責任是「補齊資料」，不是「決定是否允許」。

---

### 4.3 Access Gate（可選）

針對特定 Inbound flow 的最低授權要求，可插入 AccessGate：

- 是否允許匿名
- 是否需要任一 / 全部 authority
- 是否需要特定 role / role group

AccessGate 只負責：

- **允許流程繼續**
- 或產生 `Validation.invalid`（授權拒絕）

---

## 5. Gate 的失敗處理策略

### 5.1 不同步驟，不同失敗語意

StatefulGate 中，不同步驟對「失敗」的語意是刻意不同的：

| 步驟                    | 失敗語意                              |
| --------------------- | --------------------------------- |
| Claims / Auth Binding | `invalid`（流程前提不成立）                |
| QuerySpec Binding     | `invalid`（請求語意錯誤）                 |
| Status Observation    | **不 invalid**，轉為 `Failure`        |
| Decision Policy       | 產生 `ControlDecision.FailInternal` |

這些差異是設計結果，而非不一致。

---

### 5.2 AuthContext 與授權的三種關鍵情境

#### A) 缺少認證，但流程要求登入

- 情境：`authCondition.required` 且 auth 缺失 / anonymous
- 行為：回 `invalid`
- 理由：流程前提不成立

#### B) 已登入，但無權限

- 情境：auth 存在，但 AccessGate 條件不滿足
- 行為：AccessGate 回 `invalid`
- 理由：正常的授權拒絕（非例外、非 retry）

#### C) 允許匿名（auth optional）

- 情境：auth optional 且缺 auth
- 行為：流程繼續，標示為匿名
- 理由：匿名是合法模式，而非錯誤

---

### 5.3 為什麼 Status 失敗不 invalid？

因為狀態查詢失敗時：

- 事件仍然存在
- 可以選擇 retry / dlq / noop
- 不應直接中斷流程

Gate 的設計允許：

> 用「決策」處理不確定性，而不是用例外終止流程。

---

## 6. Gate 的可重用性

### 6.1 不同 Inbound 流程，共用 Gate

同一個 StatefulGate 可被用於：

- HTTP Inbound
- MQ Consumer
- MQTT Listener
- File Import

只要：

- Scope 語意一致
- 決策策略一致

---

### 6.2 多個 Gate 的組合

在較複雜的流程中，可以：

- 使用多個 Gate
- 或在 Gate 前後插入其他 Step

Gate 本身不限制流程結構。

---

## 7. 常見誤用與邊界

### 7.1 在 Gate 中加入業務邏輯（❌）

Gate 不應：

- 實作 domain 行為
- 修改 payload
- 呼叫 application service

---

### 7.2 讓 Gate 隱性中斷流程（❌）

Gate 不應：

- 自動 aborted
- 根據 severity 偷偷停止
- 吞掉錯誤

所有中斷都必須是 **顯式的設計結果**。

---

## 8. 本章小結

- StatefulGate 是 *stateful concern* 的集中點
- Gate 組裝 Step，但不承擔實作責任
- Auth / AuthZ 屬於 Gate 內的合法 stateful concern
- 不同步驟有不同失敗語意
- Gate 的價值不在於它做了什麼，而在於它界定了**什麼應該在這裡發生**