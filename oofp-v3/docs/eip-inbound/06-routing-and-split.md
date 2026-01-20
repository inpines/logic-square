# 06. Routing and Split（路由與分拆）

本章說明 `eip-inbound` 中用於**流程分支與資料展開**的結構化工具，
包含 `RouteTable / Selector` 與 `Splitter / QueryTable`，
以及它們在流程中的責任邊界。

---

## 1. 為什麼需要 Routing 與 Split？

Inbound 流程常見兩種需求：

1. **路由（Routing）**  
   - 根據事件性質，選擇不同處理路徑
   - 例如：依事件型別、來源、查詢結果決定下一步

2. **分拆（Split）**  
   - 將一個 Inbound 事件拆成多個處理單元
   - 例如：批次檔案、多筆訊息、清單型 payload

若直接用 if / else 或迴圈實作，會導致：
- 流程語意隱性
- 分支條件分散
- 測試困難

eip-inbound 的立場是：

> **分支與分拆都應該被視為流程結構，而不是流程細節。**

---

## 2. Routing：顯式的流程分支

### 2.1 `RouteKey`：路由語意

`RouteKey` 是路由的語意單位，代表「要走哪一條路」。

設計要點：
- `RouteKey` 是顯式型別（而非裸字串）
- 內建 `core route`（`Enum`）
- 擴充 `route` 使用明確命名（String）

這避免：
- `magic string`
- 隱性分支條件

---

### 2.2 `RouteTable<T>`：路由對照表

`RouteTable<T>` 定義：

> **RouteKey → BehaviorStep\<T>**

特性：
- 路由表是**配置**
- 查不到 route 視為配置錯誤
- 缺失不應被當成業務錯誤處理

因此：
- route 缺失通常 fail-fast
- 不轉為 Violation

---

### 2.3 Selector：選路而非處理

`Selector` 的責任是：

- 根據 `StepContext` / View
- 選出一個 `RouteKey`
- 交由 `RouteTable` 解析下一個 Step

Selector **不處理業務**，
只負責「選哪一條路」。

---

### 2.4 Routing 的責任邊界

Routing 應：

- 描述流程分支
- 不改變 payload
- 不做決策後果（ack / retry / dlq）

Routing 不應：

- 藏在 Step 內
- 與 Decision Policy 混用

---

## 3. Split：資料展開的流程語意

### 3.1 為什麼 Split 不是 for-loop？

for-loop 的問題在於：

- 展開邏輯隱性
- 驗證與錯誤處理混雜
- 流程層級不清楚

Split 的設計目標是：

> **把「展開資料」提升為流程語意。**

---

### 3.2 `Splitter` 的基本結構

`Splitter` 通常包含：

1. **是否要 split 的判斷**
2. **`itemExtractor`**：抽取 items
3. **`verifier`**：驗證 items（非空、格式正確）
4. **寫入 attribute**：保存展開結果

`Splitter` 本身不負責後續處理。

---

### 3.3 Split 與 Validation

Split 過程中可能產生：

- 無法展開 → Violation
- items 為空 → Violation
- 型別不符 → Violation

這些都屬於**流程內結構性錯誤**，
而非處置決策。

---

## 4. `flatSplit`：進階分拆模式

`flatSplit` 提供更完整的控制：

- `extractor` 與 `verifier` 都回傳 Validation
- 可記錄中間失敗
- 保持流程不中斷

適用於：
- 外部格式不穩定
- 資料品質不一致
- 需要完整錯誤回報的場景

---

## 5. `QueryTable`：以名稱選擇行為

### 5.1 定位

`QueryTable` 是一種特殊路由：

> **String name → `BehaviorStep<T>`**

它通常用於：
- `QuerySpec.name`
- 外部配置指定的行為

---

### 5.2 與 `RouteTable` 的差異

- `RouteTable`：
  - 偏流程結構
  - 缺失是配置錯誤

- `QueryTable`：
  - 偏動態選擇
  - 查不到通常回傳 empty / invalid

兩者語意不同，不應混用。

---

## 6. Routing / Split 與 Gate 的關係

- Routing / Split 通常發生在 Gate **之前或之後**
- Gate 專注於 stateful concern
- Routing / Split 專注於流程形狀

這樣的分離確保：
- Gate 可重用
- 流程可讀

---

## 7. 常見誤用與風險

### 7.1 在 Step 中寫 if / else 路由

應避免：
- 在 Step 實作內選路
- 隱性流程分支

---

### 7.2 在 Split 中做業務處理

Split 不應：
- 呼叫 service
- 修改 payload
- 決定處置結果

---

### 7.3 Routing 與 Decision 混用

Routing 決定「走哪條流程」  
Decision 決定「事件怎麼處置」

兩者語意必須分離。

---

## 8. 本章小結

- Routing 讓流程分支顯式化
- Split 讓資料展開成為流程語意
- `RouteTable` 與 `QueryTable` 各有責任
- 分支、分拆不等於決策

---

> **當流程形狀清楚，行為才能被治理。**
