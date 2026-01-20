# Architecture（整體設計與結構說明）

本資料夾收錄的是 **本 `repo` 所有設計的上位架構說明**。

這裡的文件不隸屬於任何單一模組，
而是用來回答以下問題：

- 各設計模組為什麼存在？
- 它們之間如何分工？
- 邊界畫在哪裡，為什麼不能混用？
- 這些設計在整體系統中各自扮演什麼角色？

---

## Architecture 層的責任

`architecture/` 關注的是 **結構與邊界**，而不是：

- API 用法
- DSL 細節
- 實作技巧

它的責任是：

> **在所有設計之上，維持一個一致的心智模型。**

---

## 本資料夾文件導覽

### 1️⃣ conceptual-architecture-behavior-pipeline-eip-inbound.md

**定位：整體概念圖（Conceptual View）**

- 用最少的元素，描述整體系統如何運作
- 區分 control plane 與 execution plane
- 說清楚 Inbound、流程、執行的責任分離

👉 適合第一次建立全局理解時閱讀。

---

### 2️⃣ behavior-pipeline-and-eip-inbound.md

**定位：模組關係與設計動機（Structural View）**

- 說明為什麼拆成 behavior-pipeline 與 eip-inbound
- 它們如何互補，而非重疊
- 為什麼 Inbound 不等於流程
- 為什麼決策不等於執行

👉 適合想理解「設計為什麼長這樣」的讀者。

---

> ⚠️ 注意：  
> architecture 文件**不會**重複各模組內的說明，
> 而是引用、總結並劃定邊界。

---

## 與其他 docs 區塊的關係

### 與 write-operation-design

- write-operation-design  
  → 關注「如何建模寫入行為與組裝 DSL」

- architecture  
  → 關注「這套 DSL 在整體系統中的位置」

---

### 與 behavior-pipeline

- behavior-pipeline  
  → 流程語言本身的定義與演進

- architecture  
  → 為什麼流程語言不能承擔 Inbound / Decision / Side-effect

---

### 與 eip-inbound / service-operation-step

- 這些模組  
  → 特定語意場景的設計（Inbound / Service）

- architecture  
  → 說清楚它們彼此之間「不能互相侵蝕」的原因

---

## 建議閱讀路線（全 repo 視角）

如果你是第一次閱讀這個 repo：

1. `architecture/README.md`（你正在看的這份）
2. `architecture/conceptual-architecture-*.md`
3. `architecture/behavior-pipeline-and-eip-inbound.md`
4. 回到各模組 README 與章節

如果你是要擴充或設計新模組：

- **先確認是否違反 architecture 的邊界說明**
- 再決定要擴充哪個模組，而不是直接加功能

---

## 設計共識（Architecture Level）

- 設計模組不是為了方便，而是為了邊界清楚
- DSL 可以組合，但語意不能混雜
- Control 與 Execution 必須分離
- 越靠近邊界，模型越應該簡單

---

> **Architecture 不是用來解釋「怎麼寫」，  
> 而是用來防止「寫到不該寫的地方」。**
