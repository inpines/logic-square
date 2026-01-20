# 03-requirements — 需求與驗收（Requirements）

> 本資料夾將 **已完成的分析結論**  
> 轉化為 **可驗收、可追責的需求條款**。

這一層不是用來「探索問題」，  
而是用來 **確認我們要對什麼負責**。

---

## 本層在黃金圈中的位置

```text
What → Why → Structure → How（方法）→ Analysis → Implementation
                         ↑
                  03-requirements 在這裡
````

- `01-golden-circle/`：需求成立的理由與邊界
    
- `02-domain/`：世界結構與存在定義
    
- `03-requirements/`：將上述結論轉為可驗收條款
    

---

## 本層解決的核心問題

- 系統「必須做到什麼」？
    
- 哪些是「明確不做」？
    
- 何時可以說「需求已被滿足」？
    
- 驗收依據是什麼，而不是感覺或默契？
    

> ⚠️ 若需求無法被驗收，  
> 則代表上游分析仍不完整。

---

## 本資料夾內容說明

```text
03-requirements/
├─ functional-requirements.md
├─ non-functional-requirements.md
├─ acceptance-criteria.md
└─ glossary.md
```

---

### `functional-requirements.md`

**功能性需求（What 的具體承諾）**

- 以「系統必須能夠……」描述
    
- 對應明確事物（Thing）與事件（Event）
    
- 不描述實作方式
    

建議結構：

- FR-001 …
    
- FR-002 …
    

---

### `non-functional-requirements.md`

**非功能性需求（約束與品質）**

- 效能、可用性、安全性、可維運性
    
- 合規、稽核、可追溯性
    
- 時間性要求（SLA / Deadline）
    

> 本文件描述「限制條件」，  
> 而不是「怎麼實作」。

---

### `acceptance-criteria.md`

**驗收條件（責任邊界的落點）**

- 每一條需求都應有對應驗收條件
    
- 驗收描述應可被第三方理解
    
- 不依賴實作者主觀判斷
    

建議格式：

- AC-001：在＿＿條件下，當＿＿發生，系統應＿＿
    

---

### `glossary.md`

**名詞表（語意一致性）**

- 所有關鍵名詞必須對齊 `02-domain/`
    
- 禁止同詞多義、異詞同義未標註
    
- 新增名詞前，需確認其概念類型
    

---

## 需求撰寫原則（請遵守）

- 不引入新事物、新角色、新事件  
    → 若需要，請回到 `02-domain/`
    
- 不引入新 Why  
    → 若出現新的必要性，請回到 `01-golden-circle/why.md`
    
- 不描述流程或操作順序  
    → 請見 `04-analysis/`
    

---

## 常見錯誤（請避免）

- ❌ 把使用者故事直接當成需求
    
- ❌ 把流程步驟寫進需求條款
    
- ❌ 用技術語言描述需求
    
- ❌ 用模糊詞彙（例如：快速、友善、容易）
    

---

## 與其他層的關係

- 本層 **回溯來源**：
    
    - What → 決定需求範圍
        
    - Why → 決定需求必要性
        
    - Domain → 決定需求對象
        
- 本層 **向下約束**：
    
    - Analysis → 不得超出需求條款
        
    - Implementation → 不得反推需求
        

---

## 進入 Implementation 的門檻（Gate）

在進入 `05-implementation/` 前，請確認：

-  所有需求皆有對應驗收條件
    
-  所有名詞皆已在 Glossary 定義
    
-  無需求承擔系統不該負責的判斷
    
-  需求變更已回溯 What / Why
    

---

## 一句定錨

> **需求不是想要什麼，  
> 而是願意為什麼負責。**

---

## 一個總原則（請記住）

> **03-requirements 不創造世界，  
> 只承諾系統願意為哪個世界負責。**