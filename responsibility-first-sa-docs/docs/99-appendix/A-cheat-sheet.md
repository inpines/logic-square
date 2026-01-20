# 系統分析黃金圈 — 一頁速查（Cheat Sheet）

> **先想清楚，再做乾淨。**  
> 本 `repo` 以「責任與世界」為核心，而非功能或技術。

---

## 唯一順序（不可顛倒）

**What → Why → Structure → How（方法）→ Analysis（結果）→ Implementation**

---

## 各層在做什麼（一句話版）

- **What**：系統替誰負責什麼？不負責什麼？
- **Why**：不做會造成哪些結構性後果？（含 SWOT）
- **Structure**：世界中有哪些存在，值得被系統尊重？
- **How（方法）**：事件／狀態／活動／作業該如何被思考？
- **Analysis（結果）**：世界實際如何運作？
- **Implementation**：用什麼技術把已定義的世界做出來？

---

## 對應資料夾（速查）

| 層級 | 資料夾 | 性質 |
|---|---|---|
| 方法論 | `01-golden-circle/` | 穩定、少改 |
| 世界建模 | `02-domain/` | 結構性 |
| 需求驗收 | `03-requirements/` | 可驗收 |
| 分析展開 | `04-analysis/` | 可反覆修 |
| 技術實現 | `05-implementation/` | 可替換 |

---

## Domain 概念分類（必記）

| 類型 | 核心判準 |
|---|---|
| **Thing / Entity** | 有身分、有生命週期、需承責 |
| **Value Object** | 無身分、以值為意義 |
| **Enumeration** | 有限、封閉語意 |
| **Message / Event** | 發生了什麼（瞬時） |

> ❗ 名詞未分類前，禁止談流程。

---

## Analysis 視角分工（不混用）

- **Flow**：發生順序（世界怎麼動）
- **Sequence**：互動關係（誰對誰）
- **Activity**：必要行為（責任如何完成）
- **Operation**：可封裝能力（不承擔判斷）
- **User Story Realization**：敘事導覽（對齊用）

---

## 常見誤區（快速自檢）

- ❌ 用 user story 生成結構  
- ❌ 把事件或狀態當成事物  
- ❌ 在需求寫流程  
- ❌ 用技術方便反推業務  
- ❌ 把 Analysis 當 SOP 或行程

---

## Gate（每一層的門檻）

- **進 Structure 前**：What / Why 清楚
- **進 Analysis 前**：Domain 完整
- **進 Implementation 前**：需求可驗收、Analysis 一致

---

## 角色閱讀建議

| 角色 | 先讀 |
|---|---|
| 產品 / PO | 01 → 04（User Story Realization） |
| 架構 / 技術 | 02 → 04 |
| 驗收 / 管理 | 01（srs.md）→ 03 |

---

## 一句總結

> **不是所有問題都需要功能；  
> 但所有系統都需要一個被尊重的世界。**
