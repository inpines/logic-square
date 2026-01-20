# 03. Scope and Views（決策所需的視圖）

本章說明 eip-inbound 如何將流程內部的 `StepContext`
轉換為 **可讀、可推理、可決策** 的視圖（Views），
並藉由 `InboundScope` 封裝決策所需的最小資訊集合。

---

## 1. 為什麼需要 Scope 與 Views？

`StepContext` 是流程語言的核心狀態容器，
但它並不適合作為 policy 或 decision logic 的直接輸入：

- 需要認識 `AttrKey / attribute` 命名
- 容易誤寫流程狀態
- 與流程 `DSL` 耦合過深

`eip-inbound` 的設計原則是：

> **決策應該基於「事實視圖」，而不是流程實作細節。**

---

## 2. Views：只讀的決策介面

### 2.1 View 的設計原則

所有 View 都遵守以下原則：

- 只讀（`read-only`）
- 不暴露 `StepContext`
- 不允許 `side-effect`
- 缺失資料有明確行為（null / empty / default）

---

### 2.2 `InboundQueryView<T>`

`InboundQueryView` 用於「綁定 / 查詢」類行為，
例如產生 `QuerySpec`。

它提供以下語意資料：

- `source / sourceId`
- `meta`（正規化後）
- `payload`
- `claims`（可為 null）
- `now`

這讓 `QuerySpec extractor` 能夠：

- 不依賴流程狀態
- 專注於「如何取得查詢條件」

---

### 2.3 `InboundDecisionView`

`InboundDecisionView` 用於「處置決策」類行為。

它提供：

- `source / sourceId`
- `meta`
- `claims`（可為 `null`）
- `currentStatus`（可能為 `UNKNOWN`）
- `failures`（`List\<Failure>`）
- `now`

Decision policy 因此能夠：

- 基於狀態與失敗歷史做判斷
- 不需了解錯誤如何產生
- 不修改流程狀態

---

## 3. InboundScope\<T>：視圖的聚合

### 3.1 定位

`InboundScope<T>` 是一個 **只讀聚合模型**，
同時實作：

- `InboundQueryView<T>`
- `InboundDecisionView`

它代表的是：

> **「此 Inbound 事件，在此時此刻，可被決策者看見的完整事實」。**

---

### 3.2 Scope 包含的資料

`InboundScope` 聚合以下資訊：

- Envelope 資訊（`source / sourceId / payload`）
- 正規化後的 `meta`
- `Claims`（可為 `null`）
- `Current MessageStatus`（可為 `UNKNOWN`）
- `Accumulated Failures`（可為 `empty`）
- `now`（建構 `Scope` 的時間點）

---

### 3.3 為什麼 Scope 是不可變的？

Scope 的不可變性確保：

- 決策結果可重放
- 不會因流程後續步驟而改變事實
- Policy 行為可被測試與推理

---

## 4. Scope 的建立時機

### 4.1 從 `StepContext` 轉換

Scope 並非一開始就存在，
而是在需要時由 `StepContext` 建立：

- 對 `QuerySpec extractor`
- 對 `Status observer`
- 對 `Decision policy`

這代表：

> **Scope 是 View，不是 State。**

---

### 4.2 預設值與容錯

在轉換過程中：

- 缺少 `claims` → `null`（表示匿名 / 未綁定）
- 缺少 `status` → `UNKNOWN`
- 缺少 `failures` → `empty list`
- 缺少 `meta` → `empty map`

這讓 `policy` 不必處理 `null chaos`。

---

## 5. `AttrKey`：型別安全的資料存取

### 5.1 問題背景

流程中常見的問題是：

- attribute key 是 string
- 需要手動 cast
- runtime 才發現錯誤

---

### 5.2 `AttrKey` 的角色

`AttrKey<R>` 提供：

- 明確的 name
- 明確的型別資訊（`TypeReference`）
- 集中定義的 key 集合

透過 `AttrKey`：

- 流程 Step 能安全讀寫 attributes
- Scope builder 能可靠取得資料

---

### 5.3 `InboundAttrKeys`

`eip-inbound` 定義一組標準 Inbound 專用的 `AttrKey`，例如：

- ENVELOPE
- SOURCE / SOURCE_ID
- META
- CLAIMS
- QUERY_SPEC
- STATUS
- FAILURES
- NEXT_DECISION

這些 key 構成了 **Inbound 流程的事實模型**。

---

## 6. Views 與 Gate 的關係

- Gate 中的每一個 `stateful step`
  - 只讀 View
  - 不直接操作 `StepContext`
- Gate 將 View 轉回流程狀態（透過 `AttrKey` 寫入）

這形成一個清楚的責任分界：

> **View 負責看，Step 負責寫。**

---

## 7. 常見誤用與風險

### 7.1 在 Policy 中操作 `StepContext`

Policy / Decision 不應：

- 直接修改 `attributes`
- 中斷流程
- 依賴流程實作細節

---

### 7.2 把 Scope 當成 State

Scope 不應：

- 被長期保存
- 被跨流程重用
- 作為流程狀態傳遞

---

## 8. 本章小結

- `Views` 提供決策所需的只讀事實
- `Scope` 是 `Views` 的聚合
- `AttrKey` 是流程與 `View` 的橋樑
- `View` 與 `Step` 的責任必須分離

---

> **當決策只看見事實，流程才能保持清晰。**
