# `GeneralBuilder`：物件組合的 Builder，而非行為的一部分

在前面的章節中，我們已經確立了一個核心立場：
- `WriteOperation 是行為語言`
- `TransformStep 是在 StepContext<T>上運行的行為節點`

在這個脈絡下，**`GeneralBuilder` 被定義為組合物件屬性內容的通用組裝工具**，
注意不要將它誤解為流程、行為，甚至是 pipeline builder。

本章的目的，就是徹底釐清它的角色。

---

## 角色重新錨定（重要）

> **`GeneralBuilder` 的職責屬於「物件建構（`object construction`）」層級，  
> 而非行為流程或執行階段的一部分。**

它的輸出是：
- 一個**新物件**
- 或一個**組合後的結果物件**

而不是：
- 行為節點
- `pipeline step`
- 對 `StepContext` 的轉換函數

因此，**`GeneralBuilder` 與 `TransformStep` 位於完全不同的抽象軸線上**，
兩者不應被視為可以互換或串接的角色。

---

## `GeneralBuilder` 是什麼

`GeneralBuilder` 的本質，可以用一句話描述：

> **`GeneralBuilder` 是一個用來「組合並產生新物件」的 Builder。**

它關心的是：
- 有哪些構件（components / writers / rules）
- 這些構件要如何被組合
- 在條件成立時，是否納入某些構件
- 最終產生一個新的、可被使用的組合結果

這個過程發生在：
- 行為發生之前
- `pipeline` 存在之前
- `context` 被轉換之前

---

## 它要解決的現實問題：避免 setter 樣板碼，但不走全參數建構子

`GeneralBuilder` 的出發點很務實：

我們常常需要產生一個新物件 `X`，並以一連串 setter 完成初始化：

```java
var x = new X();
x.setXXX(...);
x.setYYY(...);
```

這種寫法有三個問題：

1. **樣板碼膨脹**：初始化邏輯散落在各處，且很難重用。
    
2. **可讀性差**：物件在建構過程中長時間處於「半成品狀態」。
    
3. **不想使用 all-attributes constructor**：  
    全參數建構子讓呼叫端背負過多欄位責任，也讓未來欄位增減造成破壞性變更。
    

`GeneralBuilder` 的目的，就是在不引入「全參數建構子」的前提下，  
把「新物件 + 多段初始化」這件事收斂成一個可讀、可重用、可條件化的建構流程，  
並將初始化的細節從呼叫端抽離。

換句話說：`GeneralBuilder` 是為了讓「建構」保持彈性，
而不是把建構壓縮成一個巨大且脆弱的 constructor 簽名。

---

## 建構方式的設計取捨三角形

在「如何產生一個新物件並完成初始化」這件事上，
常見的做法大致落在三個角落，
而 `GeneralBuilder` 正是對這個取捨關係的回應。

---

### 1. Setter 串接

```java
X x = new X(); 
x.setXXX(); 
x.setYYY();
```

這是最直接、也最常見的寫法。

**優點**
- 彈性高
- 不需要調整建構子

**代價**
- 初始化邏輯高度分散
- 樣板碼重複
- 物件長時間處於「未完成狀態」
- 呼叫端必須知道過多初始化細節

這種方式在專案初期尚可接受，
但隨著初始化規則變多，很快就會失控。

---

### 2. 全參數建構子（all-attributes constructor）

另一個極端，是把所有必要屬性一次塞進建構子。

**優點**
- 物件一出生即完整
- 不存在半成品狀態

**代價**
- 建構子簽名脆弱且難以維護
- 呼叫端被迫理解所有屬性
- 欄位增減會造成廣泛破壞
- 很難處理條件式初始化

這種方式把複雜度從流程轉移到介面，
但並沒有真正消除它。

---

### 3. `GeneralBuilder（組合式建構）`

`GeneralBuilder` 刻意不走上述兩個極端。

它的選擇是：
- 不把初始化邏輯散落在 setter 呼叫端
- 也不把所有責任壓進建構子簽名
- 而是提供一個「可組合、可條件化、可重用」的建構機制

在這個模式下：
- 建構邏輯被集中描述
- 初始化順序不再暴露給呼叫端
- 物件只在建構完成後才被交付使用

---

## 取捨的核心不是技巧，而是責任分配

這個取捨三角形的關鍵，不在於語法，
而在於「誰該知道什麼」。
- setter 串接：責任落在呼叫端
- 全參數建構子：責任落在介面設計
- `GeneralBuilder`：責任被集中在一個專責的建構角色中

`GeneralBuilder` 的存在，
並不是為了創造新的花樣，
而是為了讓「建構」這件事有一個合理的歸屬位置。

---

## 為什麼這個選擇是長期友善的

透過 `GeneralBuilder`：
- 新增或調整初始化規則，不會影響呼叫端
- 條件式建構可以自然表達
- 建構邏輯可以被測試與重用
- 建構與行為執行被清楚分離

這使得系統可以在演進中，
保持建構層的穩定性。

---

## 它解決的是「結構生成」問題

`GeneralBuilder` 解決的不是：
- 何時執行
- 是否成功
- 是否要短路
- 行為之間如何傳遞狀態

而是更早的一件事：

> **我要生成一個什麼樣的「結構性結果物件」。**

在我們的設計中，這個結果物件經常是：
- 一個 `composite WriteOperation`（例如透過 `WriteOperation.chain(..)`）
- 或任何其他「被組合完成的新物件」

---

## `GeneralBuilder 與 WriteOperation 的關係`

`GeneralBuilder` **可以使用 `WriteOperation` 作為組合材料**，
但這並不代表它屬於行為層。

關係可以這樣理解：

- `WriteOperation`：行為語言的最小單位
- `GeneralBuilder`：使用這些語言片段，組出一個新的「句子（物件）」

這個句子本身：
- 仍然只是個物件
- 尚未被放入任何行為時間軸中

---

## 它不是什麼（非常重要）

### 1. 它不是 `BehaviorStep`

`GeneralBuilder` 不實作、也不模擬以下型別語意：

`StepContext<T> -> Validation<Violations, StepContext<T>>`

它不接收 `StepContext`，
也不回傳 `Validation`。

任何將 `GeneralBuilder` 視為「行為節點」的理解，
都是錯誤的。

---

### 2. 它不參與 Pipeline 或 Transform

`GeneralBuilder` 不知道：
- `validate / query / write / verify / transform`
- `pipeline 的存在`
- 行為的執行順序

它只負責產生一個「已組合完成的結果物件」，
至於這個物件何時、是否、如何被使用，
完全是其他機制的責任。

---

### 3. 它不負責時間性與短路

`GeneralBuilder` 不承擔：

- 失敗中斷
- 驗證錯誤傳遞
- 行為是否繼續

這些都是 **`Behavior / TransformStep` 軸線** 的責任，
與 object construction 無關。

---

## 為什麼必須嚴格切開這條界線

如果讓 `GeneralBuilder` 開始：
- 理解 `context`
- 知道 `pipeline`
- 參與行為決策

那麼它將同時橫跨：
- 結構生成
- 行為執行

結果必然是：
- 抽象混亂
- `Builder` 失去穩定性
- 行為與結構彼此污染

透過刻意限制其角色，
`GeneralBuilder` 才能成為一個
**長期穩定、不隨流程演化而變形的元件**。

---

## 與 `TransformStep` 的對照

> **`GeneralBuilder` 負責「新物件如何被組出來」；  
> `TransformStep` 負責「既有 context 如何被一步步轉換」。**

兩者解決的是不同問題，
也必須被放在不同層次理解。

---

## 總結

`GeneralBuilder` 並不是系統的行為核心，
也不屬於執行流程的一部分。

它的價值在於：
- 把「組合結果」從流程中抽離
- 讓結構生成成為一個可閱讀、可推理的動作
- 為後續的行為系統提供穩定、可重用的輸入物件

只要這個邊界被守住，
整個設計就能長期保持清晰。

