# 鏈結與組合（Chaining & Composition）

在本設計中，`WritePlan` **不是一個獨立存在的角色或型別**。

所謂「已決議的行為集合」，
實際上是透過 `WriteOperation.chain(..)` 所形成的 **複合寫入行為（composite writer）**。

因此，本章不把 `WritePlan` 視為一個中介層，
而將它明確降格為：

> **對「完成組合決策後的 `composite WriteOperation`」的語意稱呼。**

---

## `WriteOperation` 的兩種形態

在這套模型中，`WriteOperation` 本身就具備組合能力，
因此自然形成兩種形態：

- **Single writer**  
  描述一次最小、可獨立推理的寫入行為

- **Composite writer**  
  由多個 `WriteOperation` 透過 `chain(..)` 組合而成的複合行為

兩者 **共用同一個介面**，
差別只在於是否為組合結果。

---

## 為什麼「Plan」不應該是獨立層級

一個常見的設計誘惑是：

> 當組合完成後，是否需要一個「Plan」來承接結果？

在本設計中，答案是否定的。

原因很單純：

- 組合完成後的結果，本身就是一個行為
- 再引入一個 Plan 角色，只會製造階層幻覺
- 行為語言會被迫分裂成「行為」與「計畫」

因此，本設計選擇將「Plan」**內建在行為語言本體之中**，
而不是額外建模。

---

## `chain(..)` 的真正語意

`WriteOperation.chain(..)` 解決的不是流程問題，
而是**語意完成度問題**。

它代表的是：

> **哪些寫入行為，已經被選定、排列、並固化成一個可重用的整體。**

一旦 chain 形成：

- 條件判斷已經結束
- 組合決策已經完成
- 行為內容不再變動

這個狀態，正是過去被稱為 `WritePlan` 的語意來源。

---

## `GeneralBuilder` 與 `composite writer` 的關係

在 Object Construction 軸線上：

- `GeneralBuilder` 的責任是：
  - 選擇要使用哪些 `WriteOperation`
  - 組合它們
  - 產生一個新的 **`composite WriteOperation`**

它**不產生行為流程**，
也**不產生 `TransformStep`**，
只產生「一個可被使用的行為物件」。

---

## `TransformStep` 與 `composite writer` 的關係

在 Behavior Transformation 軸線上：

- `TransformStep` 的責任是：
  - 在適當的時間點
  - 觸發既有的 `composite WriteOperation`
  - 並將結果反映到 `StepContext`

因此：

- `GeneralBuilder` 與 `TransformStep` 位於 **同一抽象階層**
- 只是分屬不同語法（建構 vs 轉換）
- `composite WriteOperation` 則是兩者都可以引用的行為語言產物

---

## 為什麼不把 chain 放進 `TransformStep`

如果在 `TransformStep` 中才動態 chain 行為：

- 組合決策會回到行為時間軸
- 行為是否存在，變成執行期問題
- 組合結果無法事前檢視或重用

這會破壞先前建立的斷軸原則。

因此：

> **chain 應該只出現在建構或準備階段，  
> 而不是行為執行階段。**

---

## 關於「WritePlan」這個名稱

在本文件中：

- `WritePlan` 僅作為語意上的暱稱
- 用來指稱「已完成組合決策的 composite WriteOperation」
- **不是一個角色**
- **不是一個型別**
- **也不是一個架構層級**

這個名稱的價值在於幫助理解「狀態轉折點」，
而不是引入新的抽象。

---

## 一句話總結

> **在這套設計中，  
> 沒有獨立的 WritePlan，  
> 只有 single 與 composite 的 WriteOperation。**

所謂的「Plan」，
只是對「組合已完成、尚未執行」這個狀態的語意描述。

---

## 下一章要談的是什麼

當行為的組合被完全內建進語言本體後，
下一個問題自然是：

> **這些行為是如何在建構階段被有條件地選入的？**

下一章將聚焦在：
**Conditional Rules 與條件式組合的語意**。
