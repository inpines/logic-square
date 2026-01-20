# 核心資料模型（Model）

本章說明 Behavior Step / Pipeline 中的核心資料模型。
這些模型的目的不是承載業務資料本身，而是**承載流程狀態與語意**。

理解這一層，才能正確理解後續 Step 與 Pipeline 的設計選擇。

---

## `1. StepContext<T>`

### 1.1 定位

`StepContext<T>` 是整個 Pipeline 中**最重要的資料結構**。

它代表的是：

> **某一個時間點上，流程的完整狀態。**

所有 Step 都只能透過 `StepContext` 讀寫流程資訊，
Pipeline 也只關心 Context 的變化，而不理解其中的業務內容。

---

### 1.2 組成結構

`StepContext` 由四個核心部分組成：

1. **payload : T**  
   - 流程的主資料  
   - 代表「目前正在被處理的是什麼」

2. **violations : Violations**  
   - 目前已累積的錯誤集合  
   - 不等同於流程失敗，而是「已知問題」

3. **attributes : Map<String, Object>**  
   - 跨 Step 傳遞的輔助資訊  
   - 用於中間結果、旗標、暫存資料

4. **aborted : boolean**  
   - 顯式標記流程是否應停止  
   - 與錯誤無直接等價關係

這四者共同構成流程狀態的全貌。

---

### 1.3 payload 與 transit 語意

payload 代表流程的「主線資料」。

當 Step 需要產生新的主資料時，應使用 **transit** 的概念：

- 舊 payload 不再代表當前狀態
- 新 payload 成為後續 Step 的輸入
- violations 與 attributes 仍然延續

這確保了資料流向是**單向且可推理的**。

---

### 1.4 attributes 的設計邊界

attributes 的設計目的不是「隨便塞資料」，而是：

- 補足 payload 不適合承載的資訊
- 傳遞跨 Step 的輔助結果
- 避免為了中間狀態而污染 domain model

使用原則：

- attributes 是 **輔助，不是主線**
- attributes 的 key 應具備語意
- 不應將核心業務資料長期存放在 attributes 中

---

### 1.5 aborted 的語意

`aborted` 用於表示：

> **流程在語意上已完成或不再需要繼續。**

aborted 並不代表錯誤，例如：

- 條件未滿足，後續步驟不再適用
- 已取得足夠資訊
- 流程被策略性終止

這讓「中斷」成為一種**顯式狀態**，而不是隱藏在控制結構中。

---

## 2. Violations

### 2.1 定位

`Violations` 代表的是：

> **流程中所有已知問題的集合，而不是單一錯誤。**

它被設計為：

- 可累積
- 可合併
- 可過濾
- 可延後決策

---

### 2.2 為何不是單一 Error？

在實務中，錯誤常具有以下特性：

- 同時存在多個問題
- 嚴重度不同（warning / severe / fatal）
- 是否中斷流程，取決於策略而非當下

Violations 的存在，正是為了讓錯誤**脫離即時控制流程**。

---

### 2.3 join 與錯誤合併

Violations 支援 join 行為：

- 將新的錯誤加入既有集合
- 避免重複的 violation
- 保留完整錯誤脈絡

這讓每個 Step 都可以「只關心自己發現的問題」。

---

### 2.4 與 Validation 的關係

Violations 本身不是流程結果。

- **Validation** 決定流程是否成功
- **Violations** 描述流程中發生了什麼問題

Pipeline 會根據策略：

- 將 Violations 轉成 invalid
- 或在流程結尾統一回應

---

## 3. GeneralViolation

### 3.1 定位

`GeneralViolation` 是一筆**具備語意的錯誤描述**。

它不是單純的錯誤訊息，而是包含：

- 發生在哪一類檢核
- 發生在哪一個 Step
- 屬於哪一種嚴重度
- 對應哪些訊息

---

### 3.2 結構語意

一筆 GeneralViolation 通常包含：

- **validationName**  
  用於分類與定位錯誤來源

- **stepName**  
  表示錯誤發生的流程節點

- **messages**  
  一或多則人類可閱讀的描述

- **options**  
  包含嚴重度、警告標記等輔助資訊

---

### 3.3 嚴重度（Severity）

GeneralViolation 支援多層級嚴重度：

- warning：提醒性問題
- severe：重大問題
- fatal（若使用）：不可繼續的錯誤

嚴重度本身**不直接決定流程是否中斷**，
而是提供 Pipeline 做決策的依據。

---

## 4. StepContextAttributes（輔助模型）

### 4.1 為什麼需要 copy？

attributes 本質上是 Map，
但 Context 本身具有流程語意。

`StepContextAttributes.copyOf` 的存在是為了：

- 避免外部直接持有 mutable reference
- 保護 Context 作為流程快照的語意
- 明確區分「讀取」與「寫入」

---

## 5. Joinable / JoinableMessage

### 5.1 Joinable 的角色

`Joinable<T>` 是一個語意介面：

> **表示某個型別可以被「合併」成同類型。**

Violations 與 JoinableMessage 都是此概念的具體化。

---

### 5.2 JoinableMessage

JoinableMessage 用於處理：

- 多段訊息逐步累積
- 延後輸出
- 避免在流程中過早格式化字串

它讓「訊息的生成」與「訊息的呈現」解耦。

---

## 6. 模型設計總結

- **StepContext**  
  → 流程狀態的唯一載體

- **payload**  
  → 主線資料流

- **attributes**  
  → 輔助與中介資訊

- **Violations**  
  → 可累積的問題集合

- **GeneralViolation**  
  → 具備語意與層級的錯誤描述

- **aborted**  
  → 顯式的流程終止狀態

---

> **這些模型不是為了「存資料」，  
> 而是為了讓流程本身成為可被理解的對象。**

下一章將說明如何基於這些模型，定義與組合 `BehaviorStep`。
