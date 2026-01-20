# `TransformStep`：借用 `WriteOperation`，而不是新的行為語言

在理解 `WriteOperation` 與 `GeneralBuilder` 之後，
`TransformStep` 往往是最容易被誤解的一個角色。

常見的誤會是：

> 「`TransformStep` 是不是另一種 `Step`？」
>  
> 「它是不是代表一個新的行為層級？」

本章的目的，就是**明確否定這兩個想像**。

---

## `TransformStep` 的真實角色

`TransformStep` 並不是新的行為抽象。

它的角色可以非常精準地描述為一句話：

> **`TransformStep` 只是「承載並執行一組 `WriteOperation` 的容器」。**

它不創造新的行為語言，
也不引入新的語意層級。

它只是**借用**已經存在、已經被定義清楚的 `WriteOperation`。

---

## 為什麼 `TransformStep` 不應該擁有自己的行為語言

一個常見的設計誘惑是：

- 為 `TransformStep` 定義新的 step interface
- 讓它承載更多流程或語意
- 逐漸變成「真正的行為單位」

這條路的結果通常是：

- 行為語言被分裂成多套
- 有些邏輯在 `WriteOperation`
- 有些邏輯在 `TransformStep`
- 邊界開始模糊，推理開始困難

本設計刻意拒絕這件事。

---

## 「借用」的真正含義

當我們說 `TransformStep` **借用** `WriteOperation`，
意思並不是：
- 包一層
- 換個名字
- 重複包裝相同行為

而是：

> **`TransformStep` 本身沒有語意，  
> 它完全以 `WriteOperation` 作為自己的語言。**

對 `TransformStep` 而言：
- 行為是外來的
- 它只負責在適當的時機，逐一執行這些行為
- 不對行為本身做任何詮釋或判斷

---

## `TransformStep` 解決的是「時間性問題」

如果 `WriteOperation` 解決的是：

- 「做了什麼改變」

那 `TransformStep` 解決的就是：

> **這些改變，在什麼時候被發生。**

`TransformStep` 的存在理由，
不是因為我們需要更多抽象，
而是因為：

- 寫入行為本身是時間無關的
- 但實際執行，必然發生在某個時間點

`TransformStep` 正是這個「落地點」。

---

## 為什麼不把這些責任丟回 `GeneralBuilder`

有人可能會問：

> 既然 `TransformStep` 這麼薄，
> 為什麼不乾脆讓 `GeneralBuilder` 直接執行？

答案很簡單：

- `GeneralBuilder` 負責的是「可能有哪些行為」
- `TransformStep` 面對的是「現在要不要執行這些行為」

這兩者分屬不同層次：

- 一個是**組合語法**
- 一個是**執行時刻**

把它們混在一起，
只會讓 Builder 失去穩定性。

---

## `TransformStep` 不應該知道的事情

為了保持角色純度，
`TransformStep` 必須對下列事情保持無知：

- 為什麼這些 `WriteOperation` 被選中
- 它們是否來自某個條件判斷
- 是否還有其他流程階段存在（validate / query / verify）

`TransformStep` 的視角非常單一：

> **我被給了一組 `WriteOperation`，  
> 我的責任就是把它們跑完。**

---

## 一個刻意保持「笨」的元件

從某個角度看，`TransformStep` 是一個「不聰明」的設計：

- 不判斷
- 不推理
- 不做決策

但正因為它笨：

- 行為不會被藏在執行器裡
- 語意不會被流程吸走
- `WriteOperation` 可以在不同 `TransformStep` 中被重用

---

## 這個選擇帶來的長期效果

當 `TransformStep` 被限制為純執行容器時：

- 行為語言不會分裂
- Pipeline 可以自由更換
- 測試可以集中在 `WriteOperation`
- 流程重構不會影響行為定義

這使得整個系統可以在不改變核心語言的情況下，
承受結構性的變動。

---

## 一句話總結

> **`TransformStep` 不代表一種新的行為，  
> 它只是行為被實際發生的地方。**

---

## 下一章要談的是什麼

當行為語言（`WriteOperation`）、
組合語法（`GeneralBuilder`）、
以及執行載體（`TransformStep`）都被釐清之後，

下一個自然浮現的問題是：

> **這些 `WriteOperation` 是如何被整理成一個可執行整體的？**

下一章將介紹：
**`WritePlan` 與組合結果的語意**。
