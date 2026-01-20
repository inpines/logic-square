# 流程控制（Flow Control）

本章說明 Behavior Pipeline 中「流程如何停止、何時繼續、錯誤如何影響決策」的語意規則。
理解這一章，才能避免在 Step 或 Pipeline 中誤用錯誤與中斷機制。

---

## 1. 為什麼流程控制需要被模型化？

在傳統寫法中，流程控制常隱藏於：

- if / else
- return
- exception

這會導致：

- 中斷原因不清楚
- 錯誤與控制混在一起
- 無法觀察流程狀態

Behavior Pipeline 將流程控制**提升為顯式語意**：

> **流程怎麼停，是一種設計決策，而不是程式副作用。**

---

## 2. invalid：流程遇到問題

### 2.1 invalid 的語意

`invalid` 表示：

> **流程在某個 Step 發現了問題，無法依照原設計正常推進。**

它代表的是「狀態異常」，不是「程式錯誤」。

---

### 2.2 invalid 與 Exception 的差異

- invalid
  - 可預期
  - 可分類
  - 可累積
  - 可恢復（視策略）

- Exception
  - 不可預期
  - 多半代表系統錯誤
  - 會破壞流程結構

設計原則：

> **能用 invalid 表達的情況，不應丟 Exception。**

---

### 2.3 invalid 在不同 Pipeline 中的行為

| Pipeline 模式 | invalid 行為 |
|--------------|-------------|
| apply（Fail-Fast） | 立即中斷流程 |
| applyCorrectErrors | 累積錯誤，流程繼續 |

invalid 的影響不是 Step 決定，而是 Pipeline 決定。

---

## 3. aborted：顯式流程中止

### 3.1 aborted 是什麼？

`aborted` 表示：

> **流程在語意上已完成，或不再需要繼續執行。**

它是一種**狀態標記**，不是錯誤。

---

### 3.2 aborted 的典型使用情境

- 條件不成立，後續流程不適用
- 已取得足夠資訊
- 流程被策略性提前結束

aborted 並不暗示流程成功或失敗。

---

### 3.3 aborted 與 invalid 的關係

aborted 與 invalid 是**正交概念**：

- 可以 valid + aborted
- 可以 invalid + aborted
- 也可以 valid + not aborted

Pipeline 不應自行推論兩者的因果關係。

---

## 4. severity：錯誤的層級，而非流程指令

### 4.1 為什麼需要 severity？

在實務中，不是所有錯誤都應有相同影響：

- 有些只是提醒（warning）
- 有些是重大問題（severe）
- 有些不可繼續（fatal）

severity 的存在，是為了**提供決策資訊**，而不是自動控制流程。

---

### 4.2 severity 不直接控制流程

重要原則：

> **severity 本身不會中斷 Pipeline。**

以下行為是刻意避免的：

- severe 自動 aborted
- fatal 自動丟 Exception

是否中斷，應是：

- Pipeline 策略
- 或顯式 Step 行為

---

### 4.3 severity 的常見用途

- Pipeline 結尾過濾錯誤
- 決定回傳給使用者的錯誤集合
- 區分 logging 等級
- 輔助決策模組

---

## 5. 流程控制責任分工

### 5.1 Step 的責任

Step **可以**：

- 產生 violation
- 標記 aborted
- 回傳 valid / invalid

Step **不應該**：

- 推測 Pipeline 策略
- 自行中斷整體流程
- 根據 severity 做全局決策

---

### 5.2 Pipeline 的責任

Pipeline 負責：

- 決定 invalid 是否中斷
- 決定何時檢查 aborted
- 決定最終流程結果

Pipeline **不解釋錯誤內容**。

---

## 6. 常見誤用與風險

### 6.1 把 aborted 當成錯誤

這會導致：

- 流程語意混亂
- 結果無法正確解讀

aborted 應表示「停止」，不是「失敗」。

---

### 6.2 用 severity 偷偷控制流程

例如：

- severe 就 return invalid
- warning 就忽略後續流程

這會讓流程控制變得不可預測。

---

### 6.3 在 Step 中丟 Exception 控制流程

這會繞過 Pipeline 的設計，
破壞錯誤累積與觀察能力。

---

## 7. 設計建議（Guidelines）

- 流程中斷必須是顯式的
- 錯誤層級只提供資訊，不提供指令
- Pipeline 策略應在組裝時決定
- Step 設計應與 Pipeline 策略解耦

---

## 8. 本章小結

- **invalid**：流程遇到問題
- **aborted**：流程語意終止
- **severity**：錯誤層級資訊
- **流程控制是 Pipeline 的責任**

---

> **當中斷與錯誤被清楚區分，流程才真正可被理解。**

下一章將聚焦於錯誤本身的處理策略：Violations 與過濾機制。
