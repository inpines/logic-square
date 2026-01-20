# Pipeline Orchestration：承載行為時間性的機制

在前面的章節中，我們已經釐清：

- `WriteOperation` 是行為語言
- `GeneralBuilder` 屬於物件建構（object construction）
- `WriteOperation.chain(..)` 代表已完成決策的行為組合
- `Conditional Rules` 影響的是「行為是否存在」，而非「是否執行」

在這個前提下，Pipeline 的角色必須被非常嚴格地限制。

本章要說明的不是「如何設計一個聰明的 pipeline」，
而是：

> **Pipeline 應該只做一件事：  
> 承載行為發生的時間性（temporal ordering）。**

---

## Pipeline 不是語言，而是執行容器

一個常見的誤解是，
把 pipeline 視為「描述行為的主要語言」。

在本設計中，這個理解是錯的。

Pipeline 不負責描述：

- 行為是什麼
- 行為為何存在
- 行為之間的語意關係

它只負責：

> **讓已定義好的行為，在某個順序下被實際發生。**

---

## Pipeline 的輸入與輸出語意

在 Behavior 軸線上，
Pipeline 操作的對象是 `StepContext<T>`。

典型的語意可以抽象為：

```
StepContext<T>  
-> BehaviorStep<T>  
-> Validation<Violations, StepContext<T>>
```


Pipeline 的工作，
只是把多個 `BehaviorStep<T>` 串接起來：

- 成功時，將 context 傳遞給下一步
- 失敗時，立即中斷後續執行

---

## `TransformStep` 在 Pipeline 中的位置

`TransformStep 是 Pipeline 中最常見的一種 BehaviorStep<T>`。

它的特徵是：

- 不產生新的語言
- 不引入新的決策
- 只負責在當下時刻觸發既有的 `WriteOperation`

因此，`TransformStep` 在 Pipeline 中的角色非常單純：

> **當 Pipeline 執行到這一刻時，  
> 把既有的 `composite WriteOperation` 套用到 context 中的 target。**

---

## 為什麼 Pipeline 不應該負責組合

如果讓 Pipeline 開始：

- 決定要不要納入某個 `WriteOperation`
- 在執行時判斷條件
- 動態改變行為組合

那 Pipeline 就會同時承擔：

- 組合語意
- 時間語意

這會直接破壞我們前面建立的邊界。

正確的順序應該是：

1. 組合完成（Object Construction）
2. 行為固化（WriteOperation.chain）
3. Pipeline 只負責執行

---

## Pipeline 與 Validation 的關係

Pipeline 存在的另一個重要理由，
是 **短路（short-circuit）**。

在 Behavior 軸線上：

- 行為可能失敗
- 失敗必須阻止後續行為
- 錯誤必須被保留下來

這也是為什麼 Pipeline 的語意自然會趨近於：

`StepContext<T> -> Validation<Violations, StepContext<T>>`

Pipeline 不理解 Violations 的內容，
但必須尊重它們的存在。

---

## Pipeline 必須保持「語意貧乏」

從設計角度看，
Pipeline 是一個刻意被設計得「不聰明」的元件。

它不應該：

- 試圖理解業務語意
- 嘗試最佳化行為組合
- 內建任何領域規則

它存在的價值只有一個：

> **保證行為依序發生，並在失敗時停止。**

---

## 一個實作層面的自我檢查

在實作 Pipeline 時，
可以用以下問題檢查是否越界：

- Pipeline 是否知道「為什麼要跑這個行為」？
- Pipeline 是否在執行時動態決定行為集合？
- Pipeline 是否開始關心某個欄位或業務條件？

只要其中一題回答是「是」，
幾乎可以確定邊界被破壞了。

---

## Pipeline 的長期價值

當 Pipeline 被限制為純 orchestration：

- 行為語言可以自由演進
- 組合策略可以被替換
- Pipeline 本身幾乎不需要改動

這讓系統能夠在不動核心語言的情況下，
承受行為流程的重構。

---

## 一句話總結

> **Pipeline 不負責思考行為，  
> 它只負責讓行為在時間中發生。**
