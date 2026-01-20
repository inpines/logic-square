# Behavior Step / Pipeline

Behavior Step / Pipeline 是一套用於**行為流程組裝（Behavior Orchestration）**的 `DSL`，
用來描述「資料如何在多個步驟中流轉、被檢核、被修正，並最終產出結果」。

它適用於需要 **明確流程結構、可累積錯誤、可中斷控制** 的場景，
例如：驗證流程、領域行為編排、規則式處理、轉換與修復流程等。

---

## 為什麼需要 Behavior Pipeline？

在實務中，我們經常遇到以下問題：

- if / else 邏輯隨著需求成長而失控
- 驗證錯誤只能丟 Exception，無法累積與分類
- 流程「什麼時候該中斷」沒有一致語意
- 副作用、轉換、驗證混在一起，難以閱讀與維護

Behavior Pipeline 的設計目標是：

> **把「行為流程」變成一等公民，而不是隱藏在控制結構裡。**

---

## 核心設計理念

### 1. 行為即流程（Behavior as Pipeline）

- 一個行為不是單一方法，而是一連串 **可組合的 Step**
- 每個 Step 都有清楚的輸入、輸出與錯誤語意
- Pipeline 負責組裝與執行，不負責業務邏輯

---

### 2. Context 是唯一的交換媒介

所有 Step 之間，**只能透過 `StepContext` 互動**：

- `payload`：主要資料
- `attributes`：跨步驟的輔助資訊
- `violations`：累積的錯誤集合
- `aborted`：是否中斷流程

這讓流程具備以下特性：

- 沒有隱性共享狀態
- 錯誤不再靠 Exception 傳遞
- 流程狀態可被觀察與測試

---

### 3. Validation 導向，而非 Exception 導向

Behavior Pipeline 以 `Validation<Violations, StepContext<T>>` 作為基本回傳型態：

- **valid**：流程可繼續
- **invalid**：流程依策略中斷或累積錯誤

這讓錯誤成為**資料的一部分**，而非控制流程的捷徑。

---

## 這套 `DSL` 適合用在什麼地方？

✅ 適合：

- 輸入資料驗證與轉換
- Domain 行為流程（非單純 CRUD）
- 規則需要「可組合、可觀察、可修復」
- 需要區分 warning / severe / fatal 的錯誤模型

❌ 不適合：

- 單一 if 判斷即可解決的簡單邏輯
- 極端效能敏感、不能承擔物件配置成本的場景
- 純資料結構操作（例如 map/filter 一行可解）

---

## 文件結構導覽

本文件夾的內容依照「從概念到實作」的順序編排：

- **00-`introduction.md`**  
  緣起、設計背景與問題定義

- **01-`core-concepts.md`**  
  Behavior、Step、Pipeline、Context 的整體概念

- **02-`model.md`**  
  核心資料模型：
  - `StepContext`
  - `Violations`
  - `GeneralViolation`
  - 附屬 `Value Objects`

- **03-`behavior-step.md`**  
  如何定義與組合 `BehaviorStep`：
  - `of / supply / chain`
  - `when / andThen`
  - `require / peek / recover`

- **`04-pipeline.md`**  
  `BehaviorPipeline` 的執行模型：
  - `apply（Fail-Fast）`
  - `applyCorrectErrors（Error Accumulating）`

- **05-`flow-control.md`**  
  流程控制語意：
  - `invalid vs aborted`
  - `severity` 的影響
  - 中斷設計原則

- **06-`violations.md`**  
  `Violations` 的後處理與過濾策略：
  - `ViolationFilters`
  - 錯誤輸出與決策

- **07-`best-practices.md`**  
  設計規約與實務建議

- **08-`anti-patterns.md`**  
  常見錯誤用法與反模式

- **09-`evolution.md`**  
  架構演進與未來擴充方向（選讀）

---

## 設計原則總結

- **Step 要小且單一責任**
- **Pipeline 不寫業務邏輯**
- **錯誤要被模型化，而不是被丟出**
- **流程中斷必須是顯式決策**
- **可讀性優先於技巧性 `DSL`**

---

## 建議閱讀順序

如果你是第一次接觸這套設計：

1. 01-`core-concepts.md` 
2. 02-`model.md`  
3. 03-`behavior-step.md` 
4. 04-`pipeline.md`  

如果你已熟悉程式碼，想確認設計邊界：

- 05-`flow-control.md`  
- 07-`best-practices.md`  
- 08-`anti-patterns.md`  

---

## 最後一句話

> **Behavior Pipeline 不是為了讓程式更「聰明」，  
> 而是讓行為流程變得可被閱讀、討論與演進。**

