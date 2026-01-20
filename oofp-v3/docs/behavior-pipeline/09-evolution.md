# Evolution（演進方向與設計邊界）

本章不描述「接下來一定要做什麼」，而是說明 Behavior Step / Pipeline
**為什麼長成現在這樣，以及未來可以如何延伸而不破壞既有結構**。

---

## 1. 為什麼這套設計可以演進？

Behavior Pipeline 的核心價值在於：

- 清楚的責任分離（Step / Pipeline / Model）
- 顯式的流程狀態（Context）
- 可延後決策的錯誤模型（Violations）

這些特性讓它具備良好的「演進彈性」：

> **新增能力時，不必推翻既有語意。**

---

## 2. 穩定核心（Stable Core）

以下設計應視為**穩定核心**，不建議輕易變動：

- StepContext 作為唯一流程狀態載體
- Step.execute 的 Validation 合約
- Pipeline 不承載業務邏輯
- invalid / aborted / severity 的語意區分

這些是整套 DSL 能夠被理解與推理的基礎。

---

## 3. 可演進的方向

### 3.1 Step 模板與工廠（Step Templates）

可能的演進方向包括：

- 常見 Step 模式的封裝（validation、mapping、guard）
- 以 factory / builder 建立具語意的 Step
- 降低重複樣板程式碼

重點仍是：

- 模板不應隱藏流程語意
- 產出的仍然是 BehaviorStep

---

### 3.2 Composite Step（子流程）

當 Pipeline 變長時，可考慮：

- 將一段固定流程包裝為單一 Step
- 對外只暴露高層語意
- 內部仍使用 Pipeline 組裝

這讓流程能在不同層級被理解。

---

### 3.3 與 Domain Model 的整合

Behavior Pipeline 可自然地成為：

- Domain Service 的實作方式
- Use Case 層的行為編排工具

但需注意：

- 不應取代 domain model 本身
- 不應將 domain 狀態隱性藏入 attributes

---

### 3.4 與外部框架的邊界

這套 DSL 可以與以下工具共存：

- Validation framework（作為 violation 來源）
- Logging / Monitoring
- Workflow / BPM（作為更高層 orchestration）

但應避免：

- 將 Pipeline 當作 Workflow Engine
- 在 `DSL` 中引入 framework-specific 行為

---

## 4. 不打算做的事情（Non-Goals）

明確列出「不做什麼」，有助於維持設計純度：

- 不自動推斷流程策略
- 不根據 severity 自動中斷
- 不嘗試成為通用 Rule Engine
- 不隱性修改流程結構

這些選擇是刻意的取捨。

---

## 5. 何時該引入新機制？

在考慮新增 `DSL` 能力前，應先回答：

- 現有模型是否無法表達語意？
- 問題是否其實是 Step 設計不良？
- 是否可以透過組合解決？

若答案多為「可以用現有結構解決」，
則不應引入新機制。

---

## 6. 與歷史實驗的關係

在設計演進過程中，可能曾嘗試過其他 `DSL` 或模式。
這些嘗試的價值在於驗證設計方向，而非成為最終方案。

當核心模型已足以涵蓋需求時：

> **保留穩定主線，讓其他設計自然退場。**

---

## 7. 本章小結

- 核心穩定，周邊可演進
- 演進應建立在既有語意之上
- 不為擴充而擴充
- 清楚的邊界，比功能數量更重要

---

> **一套好的 `DSL`，不是因為功能多而長久，
> 而是因為邊界清楚而能持續演進。**

（全文完）
