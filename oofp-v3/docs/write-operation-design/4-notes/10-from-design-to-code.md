# 從設計到程式碼：抽象如何落地

前面的章節刻意停留在設計語意層，
避免過早被語法與框架牽著走。

本章的目的，是反過來回答一個實務問題：

> **這套設計在實際程式碼中，應該長成什麼樣子，  
> 又有哪些「看起來合理但其實會破壞設計」的實作方式？**

---

## 一個總覽對照表

| 設計角色                       | 程式碼對應                       | 核心責任          |
| -------------------------- | --------------------------- | ------------- |
| `WriteOperation`           | `@FunctionalInterface`      | 描述一次狀態改變      |
| `Composite WriteOperation` | `WriteOperation.chain(..)`  | 已決議的行為組合      |
| `GeneralBuilder`           | `Builder class`             | 組合並產生新物件      |
| `TransformStep`            | `BehaviorStep<T>`           | 觸發行為於 context |
| `Pipeline`                 | `step runner`               | 承載時間性與短路      |
| `Validation`               | `Validation<Violations, T>` | 成敗與錯誤傳遞       |

這張表不是 API 指南，
而是**角色邊界的最小對映**。

---

## `WriteOperation`：語言必須夠小

在程式碼中，
`WriteOperation` 應該是一個非常克制的抽象。

典型形式會接近：

```java
@FunctionalInterface
interface WriteOperation<T> {
    void write(T target);
}
```

或在需要 context 時：

```java
@FunctionalInterface
interface WriteOperation<T> {
    void write(T target, StepContext<?> ctx);
}
```

關鍵不在方法簽名，  
而在於 **`WriteOperation` 不回傳結果、不做判斷、不處理流程**。

---

## chain(..)：Plan 被內建進語言本體

在這套設計中：

- 沒有獨立的 `WritePlan` 型別
    
- 「已決議的行為集合」透過 `chain(..)` 形成
    

概念上，`chain(..)` 扮演的是：

> **把多個行為，壓縮成一個可被重用的行為物件。**

這讓後續所有機制只需要面對一個 `WriteOperation`，  
而不是一個集合。

---

## `GeneralBuilder`：只產生物件，不碰 context

在實作時，  `GeneralBuilder` 應該具備以下特徵：

- 有明確的 `build()` 或 `get()` 結果
    
- 回傳的是一個「新物件」（常是 `composite WriteOperation`）
    
- 不接收 `StepContext`
    
- 不回傳 `Validation`
    

任何出現下列特徵的 Builder，  
幾乎可以確定越界了：

- 在 builder 內部呼叫 `write`
    
- 在 builder 內部處理失敗
    
- 在 builder 內部引用 pipeline 或 step
    

---

## `TransformStep`：高階函數的實際樣貌

`TransformStep` 在程式碼中的語意，  
應該非常接近：

```java
interface BehaviorStep<T> {
    Validation<Violations, StepContext<T>> apply(StepContext<T> ctx);
}
```

`TransformStep` 的實作通常只是：

1. 從 context 取出 target
    
2. 套用既有的 `composite WriteOperation`
    
3. 回傳成功或失敗
    

它不負責：

- 組合 `WriteOperation`
    
- 判斷條件
    
- 解釋行為語意
    

---

## Pipeline：最容易被寫壞的地方

Pipeline 在程式碼中，  
通常只是幾行看似無聊的迴圈：

```java
for (BehaviorStep<T> step : steps) {
     result = step.apply(ctx);
     if (result.isInvalid()) break;     
     ctx = result.get(); 
}
```

這種「看起來太簡單」的實作，  
正是正確的樣子。

一旦 Pipeline 開始：

- 判斷業務條件
    
- 修改行為組合
    
- 操作 domain 資料
    

設計就已經被破壞。

---

## Validation：錯誤必須是一等公民

在 Behavior 軸線上，  
失敗不是例外狀況，  
而是正常的控制流結果。

因此：

- 不要用 exception 表達可預期的違規
    
- 不要在 step 內吞掉錯誤
    
- 不要讓 Pipeline 理解錯誤內容
    

Validation 的存在，  
是為了讓「失敗可以被組合與傳遞」。

---

## 幾個常見的錯誤實作（請避免）

### ❌ 把 Builder 寫成執行器

`builder.build().write(target);`

這會讓建構與執行混在一起。

---

### ❌ 在 TransformStep 中動態組合 WriteOperation

`if (ctx.hasX()) {     op = op.chain(extra); }`

這會把組合語意拉回行為時間軸。

---

### ❌ 讓 Pipeline 知道 domain

`if (ctx.getUser().isAdmin()) { ... }`

Pipeline 應該對業務語意完全無知。

---

## 一個實務判斷準則

當你在寫程式碼時，  
可以反問自己一句話：

> **我現在是在「產生一個新東西」，  
> 還是在「改變一個已存在的狀態」？**

- 前者 → `GeneralBuilder / Object Construction`
    
- 後者 → `TransformStep / Pipeline / Behavior`
    

這個問題幾乎可以解開所有設計歧義。

---

## 收尾：讓設計留在設計層

這套設計的真正價值，  
不是某個 interface 或語法技巧，  
而是：

- 把複雜度放在正確的位置
    
- 讓程式碼長期可演進
    
- 避免抽象角色彼此侵蝕
    

只要設計邊界被守住，  
實作就會自然變得簡單。