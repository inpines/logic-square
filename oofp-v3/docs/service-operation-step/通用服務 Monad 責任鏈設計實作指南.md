
---

# 《通用服務 Monad 責任鏈設計實作指南》

## 0. 前言：服務複雜度正在逼迫我們需要一個更好的模型

在現代後端服務開發中，業務邏輯越來越不像過去的單一「流程」：

- 多個 Repository 呼叫
    
- 多個 Validation
    
- 多個外部資源（Redis / `MQ` / API）
    
- 多個副作用（audit / metrics / side log）
    
- 錯誤不只一種、且要累積呈現
    
- 成功結果也可能需要組裝多段資訊
    

如果用傳統 `imperactive if-else try-catch`，會出現：

- 臃腫的 service class
    
- 多段早退程式碼
    
- 無法複用的邏輯
    
- 控制流散落
    
- 錯誤與副作用交錯、難以維護
    

幸運的是：  
我們已經設計一套完整的具備 **Monad 功能的工具鏈**：

- `Maybe<T>`
    
- `Either<L, R>`
    
- `Validation<E, T>`
    
- `Try<E, T>`
    
- `Task<Try<E, T>>`
    

因此，我們可以以 `FP` 思維為底，建構一套：

> **以 Context 為資料流核心、以 Monad 為流程引擎、  
> 以 Step（概念）為邏輯構成元素的「通用服務責任鏈模型」。**

本指南即為其總說明。

---

# 1. 設計哲學：以 Context 作為唯一資料流

服務責任鏈的核心原則：

> **所有服務行為必須以 Context 作為唯一資料流與狀態流。**

Context 是一個可自由擴展的物件，用來：

- 存放 payload（原始輸入）
    
- 存放執行過程中暫存資料
    
- 標記流程狀態（如 aborted）
    
- 存放最終輸出結果
    
- 承載副作用所需資訊（`audit / traceId / metadata`）
    

因此一個 Context 典型會包含：

```java
public class ServiceContext<T> {
	private T payload;                          // 輸入資料
    private Map<String, Object> attrs;          // 中間狀態
    private boolean aborted;                    // 流程是否終止
    // optional: userId, traceId, tenant, flags...
}
```

**Context 是整條責任鏈中唯一真實流動的物件。**

這代表：

- 不需要 P 泛型
    
- 不需要 R 泛型
    
- 只需要用 Context 的狀態、payload、結果欄位
    

在這個世界中：

> **所有服務步驟都是： Context → Context**

---

# 2. 以 Monad 作為流程控制引擎（Validation、Maybe、Try）

責任鏈的每個 Step 都回傳：

```java
Validation<Violations, ServiceContext>
```

並在 monad 上做：

- map：進行資料變換
    
- flatMap：串接下一個 step
    
- filter： fail-fast 或累積錯誤
    
- peek：副作用（成功時）
    
- peekError：副作用（錯誤時）
    

Monadic 運作確保：

- 一條 linear、有明確語意的流程
    
- 所有錯誤透過 Violations 統一回報
    
- 任何 step 的 fail 都能短路
    
- 成功部分可以無限組裝、自由串接
    

此架構天然對應到：

- 驗證鏈（validation chain）
    
- service pipeline
    
- pure transformer pipeline
    
- 行為裝配器（behavior orchestrator）
    

---

# 3. ServiceOperationStep（概念，不是 interface）

## 3.1 概念定義

**ServiceOperationStep** 是本指南核心概念。

其定義為：

```java
C -> Validation<Violations, C>
```

只要一個方法：

- 輸入是 Context
    
- 回傳是帶有錯誤語義的 Validation
    
- 內部可以對 context 寫入資料、副作用、或中止流程
    

它就符合 ServiceOperationStep 的語意。

**不需要** interface 或 abstract class 描述。

> 這是一個「概念」：一種結構與語意，而不是一個型別。

例：

```java
public Validation<Violations, ServiceContext> doXxx(ServiceContext ctx)
```

這樣的方法就自動被視為一個 step。

### 為什麼不用 interface？

因為我們的實作模式已經具備：

- payload 有 Context 包起來 → 不需 P、R 泛型
    
- 錯誤統一為 Violations → 不需 E 泛型
    
- 服務本來是 class 實作 → 不需額外介面
    
- 過度抽象會增加噪音與複雜度
    

因此最自然的是：

> **`ServiceOperationStep` 是一種「自然成立的函數結構」，而非需要被 implements 的型別。**

---

## 3.2 步驟型別矩陣：`ValidationStep / EitherStep / TryStep`（概念層級）

在前面的設計中，我們已經把「服務步驟」抽象成一種概念：

> **Step**：  
> 接收 `StepContext<T>`，  
> 回傳一個帶錯誤語義的容器（monad），內含更新後的 `StepContext<T>`。

型別形狀統一長這樣：

```java
StepContext<T> -> M<StepContext<T>>
```

其中 `M` 可以是 `Validation`、`Either`、`Try` 等。

因此，我們可以在「概念層級」定義三種常見 Step 類型：

- **`ValidationStep`**
    
- **`EitherStep<E, T>`**
    
- **`TryStep`**
    

這三者在專案中不必一定對應到實際 interface，  
只要 method 型別符合即可視為該類型的 step。

---

### 3.2.1 `ValidationStep`（業務規則／錯誤累積向）

**概念型別：**

```java
StepContext<T> -> Validation<Violations, StepContext<T>>
```

**用途：**

- 業務規則驗證
    
- 欄位/跨欄位檢查
    
- 較完整的錯誤訊息（可累積多筆 Violations）
    
- 適合「錯了要說清楚」的場景
    

**實務型式（方法即可，無需 implements）：**

```java
public Validation<Violations, StepContext<UserPayload>> validateUser(
        StepContext<UserPayload> ctx) {
    return Validation.success(ctx)
            .filter(c -> validators.basicCheck(c.getPayload()),
                    Violations.error("basic check failed"))
            .filter(c -> validators.advancedCheck(c.getPayload()),
                    Violations.error("advanced check failed"));
}
```

只要 method 符合上列簽名與語意，即可稱為一個 **`ValidationStep`**。

---

### 3.2.2 `EitherStep<E, T>`（輕量錯誤／Fail-fast 向）

**概念型別：**

```java
StepContext<T> -> Either<E, StepContext<T>>
```

**用途：**

- 錯誤型別簡單，例如 `ErrorCode` 或 `String`
    
- 不需要複雜的錯誤模型，只要 fail-fast 即可
    
- 適合在內部流程／技術流程中使用
    

**實例：**

```java
public Either<ErrorCode, StepContext<UserPayload>> checkRateLimit(
        StepContext<UserPayload> ctx
) {
    UserPayload p = ctx.getPayload();

    if (!rateLimiter.allow(p.userId())) {
        return Either.left(ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    return Either.right(ctx);
}
```

此方法自然成立為 **`EitherStep<ErrorCode, UserPayload>`** 的一個 step。  
實作時不需要真的寫 `implements EitherStep<...>`，  
只要在設計與文件中承認這是「`EitherStep` 概念」即可。

---

### 3.2.3 `TryStep`（技術錯誤／例外封裝向）

**概念型別：**

```java
StepContext<T> -> Try<StepContext<T>>
```

或若有自訂錯誤型別映射：

```java
StepContext<T> -> Try<E, StepContext<T>>
```

**用途：**

- 容易拋出技術性例外的步驟：
    
    - IO / DB / HTTP / Cipher / 序列化…
        
- 希望把 `Throwable` 納入 monad 線性流程
    
- 不想在每個步驟到處寫 try-catch
    

**實例：**

```java
public Try<StepContext<UserPayload>> loadRemoteProfile(
        StepContext<UserPayload> ctx
) {
    return Try.of(() -> {
                RemoteProfile profile = httpClient.fetchProfile(ctx.getPayload().userId());
                ctx.setAttr("remoteProfile", profile);
                return ctx;
            });
}
```

或帶錯誤型別 `ErrorCode`：

```java
public Try<ErrorCode, StepContext<UserPayload>> loadRemoteProfile(
        StepContext<UserPayload> ctx
) {
    return Try.of(() -> httpClient.fetchProfile(ctx.getPayload().userId()))
            .mapError(e -> ErrorCode.REMOTE_ERROR)
            .map(profile -> {
                ctx.setAttr("remoteProfile", profile);
                return ctx;
            });
}
```

此類方法即可視為 **TryStep**。

---

### 3.2.4 步驟型別矩陣總覽

可以用一張表整理三種 step 的定位：

|Step 類型|型別形狀|錯誤語意|適用場景|
|---|---|---|---|
|ValidationStep|`StepContext<T> -> Validation<Violations, StepContext<T>>`|結構化、多筆錯誤、業務導向|表單驗證、業務規則、欄位檢查|
|EitherStep<E, T>|`StepContext<T> -> Either<E, StepContext<T>>`|輕量錯誤、單一錯誤碼|內部流程、簡化 fail-fast 邏輯|
|TryStep|`StepContext<T> -> Try<StepContext<T>>`|例外/技術錯誤|IO、HTTP、DB、加解密、序列化|

這三類都符合「**StepContext 型別的 Monad 步驟**」這個大框架，  
只是在錯誤 channel 上使用不同的容器。

---

### 3.2.5 實作建議（概念優先、interface 選擇性）

在專案中，建議以「概念」為主軸：

1. **設計文件與註解中明確說明：**
    
    - 任何 `StepContext<T> -> Validation<Violations, StepContext<T>>` 方法  
        即視為 ValidationStep。
        
    - 任何 `StepContext<T> -> Either<E, StepContext<T>>` 方法  
        即視為 EitherStep。
        
    - 任何 `StepContext<T> -> Try<StepContext<T>>` 方法  
        即視為 TryStep。
        
2. **只有在需要 DSL / 共用框架時，才定義對應 interface**，例如：
    
    ```java
    public interface EitherStep<E, T>
            extends Function<StepContext<T>, Either<E, StepContext<T>>> {
    
        default EitherStep<E, T> andThen(EitherStep<E, T> next) { ... }
    }
    ```
    
    這類 interface 應該偏向「工具 / DSL」，而非業務程式碼必備。
    
3. **一般 service class 不必實作任何 Step 介面**  
    只要 method 型別與語意符合，即可視為某種 Step。
    

## 3.3 `Validation<E, T> 的特別之處：E 必須是 Joinable<E>`

在本專案中，`Violations` 實作了：

```java
public interface Joinable<T> {     
	T join(T other); 
}
```

因此所有 `Validation<Violations, T>` 都具備一個重要特性：

> **多個錯誤可以被「合併」成一個錯誤物件。**

我們並提供了 `ValidationUtils.mergeAll(...)` 來實作：

```java
@SafeVarargs 
public <E extends Joinable<E>, T, R> Validation<E, R> mergeAll(
	Collector<T, ?, R> collector, Validation<E, T>... validations)

```

語意為：

- 若所有 `Validation` 都成功 → 把所有成功值用 collector 收斂成一個
    
- 若有任一失敗 → 使用 `join` 把所有錯誤累積成一個 `E`，再回傳 invalid
    

相較之下：

- `Either<E, T>` 僅代表「單一錯誤或單一成功」的分支，不具備自然的錯誤累積語意
    
- `Try<T>` 則以 `Throwable` 為錯誤通道，更偏向技術性錯誤處理
    

因此在錯誤處理策略上，我們採用以下約定：

- **要收集所有錯誤、一次回報 → 使用 `Validation<E extends Joinable<E>, T>` 搭配 `mergeAll`**
    
- **只要遇錯就停、錯誤型別簡單 → 使用 `Either<E, T>` 或 `Try<T>`**

---

## 3.4 `Validation 與 Applicative —— 為什麼我們能一次收集所有錯誤？`

在 Functional Programming（`FP`）中，  
`Validation<E, T>` 之所以能「收集所有錯誤」而不是像 `Either<E, T>` 那樣 fail-fast，  
關鍵在於它採用的是 **`Applicative`**（應用函子）語義，而非 Monad（單子）語義。

理解 `Applicative` 的重要性有助於掌握：

- 為什麼 `Validation` 可以一次聚合所有錯誤
    
- 為什麼我們需要 `E extends Joinable<E>`
    
- 為什麼 `mergeAll()` 是自然、必要、語意正確的操作
    

下面我們用最務實的方式解釋 `Applicative`，同時保持專案指南應有的嚴謹度。

---

### 3.4.1 `什麼是 Applicative？`

在本專案的語境裡，可以用一句話總結：

> **`Applicative` 適用於「彼此獨立的運算」：  
> 所有運算都可以同時嘗試，最後再把錯誤合併。**

這和 Monad 的語意不同：

> **Monad 代表「具依賴性的運算」：  
> 下一步必須依賴上一步的成功結果，因此若失敗必須立即中止。**

### 對照表（專案版）

|模型|代表資料結構|語意|錯誤策略|
|---|---|---|---|
|**Applicative**|`Validation<E, T>`|步驟可並列、互不依賴|**合併所有錯誤**|
|**Monad**|`Either<E, T>`、`Try<T>`|步驟具有依賴性、需鏈式執行|**遇錯即停（fail-fast）**|

### 白話版比喻

- **Applicative**：  
    像一次批改整份考卷，把所有錯誤找出來。
    
- **Monad**：  
    像打電動破關，一關過不了就不能進下一關。
    

Validation 就是那種「適合批量檢查」的模型。

---

### 3.4.2 為什麼 Validation 必須是 `Applicative`？

因為當你要做多欄位、多規則、多表單的驗證時：

- 驗證欄位 A，與驗證欄位 B、C…互不依賴
    
- 你希望一次回報所有錯誤
    
- 而不是在第一個錯誤就終止（例如使用者一次提交多欄位表單）
    

所以你希望：

1. 每個欄位或規則得到一個 `Validation<E, T>`
    
2. 然後用某種 `combinator` 把它們全部合併
    
3. 若全部成功 → 組成 `domain object / DTO`
    
4. 若任何失敗 → 合併所有錯誤 → 一次回傳
    

這正是 `Applicative` 的核心用途：

> **獨立的運算→可同時檢查→錯誤可合併→輸出一份完整的錯誤報告。**

---

### 3.4.3 為什麼錯誤型別必須實作 `Joinable`？

`Applicative` 必須要能把錯誤合併。  
而錯誤合併本質上是一種 **半群（`Semigroup`）結構**：

```
E + E = E
```

在 `FP` 世界裡，這通常以 `Semigroup<E>` 表示；  
在你的專案中，正是以：

```java
public interface Joinable<E> {
    E join(E other);
}
```

來表示。

也就是：

> **Validation 的錯誤型別 E 必須可合併（join）成同一型別。**

本專案的 `Violations`（錯誤集合）正是最佳例子：

- 新錯誤可以加入 `Violations`
    
- `existing.join(newOne)` 可合併兩批錯誤
    
- 因此適合做一次性錯誤收集
    

---

### 3.4.4 `mergeAll()：Applicative Combine 的實作`

`Applicative` 的靈魂操作就是「將多個 Validation 一次合併」。

我們實作的：

```java
ValidationUtils.mergeAll(...)
```

其語意完全符合 `Applicative`：

- **全部成功 → combine 成新結構**
    
- **任一失敗 → 用 join 合併所有錯誤 → invalid**
    

例如：

```java
var vName  = validateName(input.name());
var vEmail = validateEmail(input.email());
var vAge   = validateAge(input.age());

return ValidationUtils.mergeAll(
        Collectors.collectingAndThen(
            Collectors.toList(),
            list -> new User(list.get(0), list.get(1), list.get(2))
        ),
        vName, vEmail, vAge
);
```

這就是典型 Applicative validation pattern。

---

### 3.4.5 為什麼 Either/Try 無法替代 Validation？

因為：

### 1. Either/Try 是 Monad

- 下一步依賴上一步結果
    
- 若上一步錯 → 無法繼續往下
    
- 這是「流程控制」，不是「批次驗證」
    

### 2. Either/Try 的錯誤不可自然合併

`Either<E, T>` 的語意是「要嘛左要嘛右」，  
沒有提供「兩個 E 合併為一個 E」的結構。

所以：

- 適合 fail-fast
    
- 不適合集合錯誤
    

---

### 3.4.6 總結：`一句話掌握 Applicative 與 Validation 的關係`

這一句可以直接放在章節開頭或結尾：

> **Validation 之所以能「一次匯總所有錯誤」而非遇錯即停，是因為它遵循 `Applicative` 語意；  
> 而 `Applicative` 能成立的前提是：錯誤型別 E 必須是可合併的（`Joinable`）。**

更簡短地說：

> **`Applicative` = 可以一次看完所有錯誤。  
> Monad = 錯了就停。**

---

## 3.5 `Validation、Joinable` 與錯誤合併策略

（Validation vs. Either 的正式規範）

在本專案的服務模型中，錯誤處理需要同時滿足兩種需求：

1. **Fail-fast**：遇到錯誤立即停止（典型於流程控制、授權、技術性錯誤）
    
2. **錯誤累積（collect errors）**：在表單驗證或規則引擎中，需要一次判斷所有欄位、一次回報所有錯誤，而非逐步中止
    

這兩種需求無法僅用一種錯誤模型處理，因此本專案採用 **Validation** 與 **Either** 兩種結構，各有其語意不同的責任。

核心判別點在於：

> **Validation 的錯誤必須是可合併的；Either 的錯誤永遠是單寫、不可合併。**

而「可合併」在本專案中是透過 `Joinable<E>` 介面明確描述的。

---

### 3.5.1 `Joinable — Validation` 表達錯誤資訊的抽象界面

```java
public interface Joinable<T> {
    T join(T other);
}
```

此介面描述一種結構：

- 錯誤可以與其它錯誤「合併」
    
- 合併後仍然落在同一個錯誤型別中 (`E`)
    
- 合併順序不改變錯誤語意
    

在本專案中：

- `Violations` 實作了 `Joinable<Violations>`
    
- 因此所有 `Validation<Violations, T>` 都支援錯誤合併（accumulation）
    

這使 `Validation<E, T>` 成為具備 **`Applicative 錯誤語意`** 的結構。

---

### 3.5.2 Validation vs Either — 語意層級的差異

| 結構                                     | 錯誤型別    | 錯誤特性                      | 典型用途                 |
| -------------------------------------- | ------- | ------------------------- | -------------------- |
| `Validation<E extends Joinable<E>, T>` | E（可合併）  | **錯誤可累積，適合一次收集所有錯誤**      | 表單驗證、多規則檢查、欄位驗證、組合驗證 |
| `Either<E, T>`                         | E（不可合併） | **fail-fast：遇到第一個錯誤立即停止** | 授權、流程管控、技術性流程、簡單決策   |

因此我們採用以下策略：

✔ 使用 `Validation<E, T>` 的情境

- 多欄位驗證
    
- 多張表、一組欄位、一批規則
    
- 需要在 UI 一次回傳所有錯誤
    
- 驗證之間彼此獨立，沒有先後依賴
    

✔ 使用 `Either<E, T>` 的情境

- 單點決策（pass / fail）
    
- 驗證之間具備依賴性（前面不過，後面就不必看）
    
- 權限檢查、狀態檢查、限流檢查等快速 fail 流程
    
- 錯誤型別輕量：`ErrorCode`、`String`、`Throwable`
    

---

### 3.5.3 `mergeAll — Validation 的 Applicative 組合法`

為了支援錯誤累積，本專案提供 `ValidationUtils.mergeAll`，  
它實現了「`Applicative-style merge`」語意：

✔ `mergeAll(Collector, Validation...)`

```java
@SafeVarargs
public <E extends Joinable<E>, T, R> Validation<E, R> mergeAll(
        Collector<T, ?, R> collector,
        Validation<E, T>... validations)
```

語意：

- 若全部 validations 都成功 → 使用 collector 將成功值收斂成一個結果 `R`
    
- 若 _任一_ validation 失敗 → 使用 `E.join(...)` 將所有錯誤合併成單一錯誤 `E`，回傳 invalid
    

此模式典型用於：

- 多欄位表單驗證
    
- 多規則一次檢查
    
- 多步驟合併值（如建構聚合根）
    

---

✔ mergeAll(Map<String, Validation<E, ?>>)

```java
public <E extends Joinable<E>> Validation<E, Map<String, Object>> mergeAll(
        Map<String, Validation<E, ?>> validations)
```

語意同上，只是成功結果會以 map 輸出，適合：

- 欄位名稱固定（如 DTO 欄位）
    
- 想要直接根據 map 建構 domain object
    

---

### 3.5.4 `mergeAll 如何整合 Validation 與 Joinable`

下面展示簡化後的實作重點（非完整程式碼）：

```java
void addError(E error) {
    errors = errors
            .map(existing -> existing.join(error)) // 若已有錯誤 → 合併
            .orElseGet(() -> Maybe.just(error));   // 若尚未有錯誤 → 包起第一個錯誤
}

validation.error().match(
        this::addError,            // invalid
        () -> addValue(...));      // valid
```

此模式具備以下特性：

### ✔ 所有錯誤會以 join 的方式合併

例如：

- 欄位 `name` 錯
    
- 欄位 `email` 錯
    
- 欄位 `age` 錯
    

最終 `Violations` 將包含所有錯誤。

### ✔ 一次看完所有錯誤，不需要 try-catch 逐步中止

這是 Validation 相比 Either 最大的語意差異。

---

### 3.5.5 何時應該從 Validation 升級到 StepContext + Pipeline？

當你遇到：

- 錯誤不是 Validation 的累積模型，而是流程控制（例如 early abort）
    
- 驗證之間有跨步驟依賴（需共享 context）
    
- 涉及外部 IO、DB、MQ（Try / Task 更適合）
    
- 想在多步驟之間傳遞多個 intermediate value
    
- 想執行 side effect（audit/log）但不破壞主流程
    

此時應升級到：

- `EitherStep<E, T>`
    
- `TryStep<T>`
    
- `ValidationStep<T>`
    
- 或整合 `BehaviorStep<T>` / `BehaviorPipeline`
    

Validation 本身只適用於「純邏輯」且「結果可累積」的場景。

---

## 3.5.6 小結：錯誤語意的選擇策略

|需求|推薦用法|
|---|---|
|一次累積所有錯誤|`Validation<E extends Joinable<E>, T>` + `mergeAll`|
|只需簡單 fail-fast|`Either<E, T>`|
|可能丟例外（技術性錯誤）|`Try<T>` 或 `Try<E, T>`|
|有 IO / async / external side effect|`Task<T>` 或 `Task<Try<E, T>>`|
|多步驟共享狀態與流程運行控制|StepContext / BehaviorPipeline|

---

## ✦ 最重要的一句話

> **Validation 適合「全部看完再說」；  
> Either 適合「錯了就停」；  
> 差別核心在於 `Joinable<E>` 提供了錯誤合併的語義。**

---

## 3.6 副作用行為的命名慣例

（`為什麼 peek / peekError 不應使用匿名 lambda`）

在 `Monadic` 責任鏈中：

- `.map / .flatMap` → 核心業務邏輯
    
- `.filter` → 驗證邏輯
    
- `.peek / .peekError` → 副作用（觀察、紀錄、審計）
    

雖然語法上 `.peek(v -> log.info(...))` 是合法的，但在本專案的設計規範中，  
**副作用應避免匿名 lambda，皆需抽為具名方法**：

```java
.peek(this::logSuccess)
.peekError(this::logFailure)
```

以下是正式理由，也是 `FP/DDD/Clean Architecture` 下的共識。

---

### ✔ 1. 副作用不應匿名：避免匿名副作用污染核心流程

責任鏈的目的，是讓主流程邏輯：

- **線性**
    
- **可讀**
    
- **可追蹤**
    
- **無雜訊**
    

但匿名副作用通常長這樣：

```java
.peek(v -> log.info("..."))
.peekError(e -> log.warn("...", e))
```

這會讓「主流程」與「副作用」混合在一起，降低可讀性。

具名方法可清楚顯示語意：

```java
.peek(this::auditSuccess)
.peekError(this::auditFailure)
```

主流程清晰，副作用抽離。

---

### ✔ 2. 副作用通常涉及 cross-cutting concerns（橫切關注）

橫切關注包括：

- log
    
- audit
    
- metrics
    
- tracing
    
- side-channel signaling
    

這些在 Clean Architecture 中絕不該隱藏在匿名 lambda 裡。

抽成具名方法後就非常清楚：

```java
private void auditSuccess(DomainObject obj) { … }

private void auditFailure(Violations v) { … }
```

責任鏈變得像文件一樣：

```java
.map(...)
.filter(...)
.map(...)
.peek(this::auditSuccess)
.peekError(this::auditFailure)
```

---

### ✔ 3. 可測試性（Testability）：匿名方法難以對應與 Mock

匿名 lambda：

- 不能單獨測試
    
- 無法被 mock
    
- 堆疊紀錄中只會叫成 `lambda$0`、`lambda$1`
    
- 在 Exception trace 中不具可辨識性
    

具名方法則可以：

```java
verify(logger).info(contains("success"));
```

而且 stack trace 會顯示：

```
at com.xxx.Service.auditSuccess(Service.java:87)
```

可追蹤度大幅提高。

---

### ✔ 4. 程式碼維運性（Maintainability）

副作用很常需要新增功能：

- log 格式調整
    
- 增加 trace id
    
- 在成功/失敗時送 metrics
    
- 記錄執行時間
    
- 發送 domain event
    

匿名 lambda 無法合理擴展，只能越寫越肥：

```java
.peek(v -> {
    log.info(...);
    metrics.record(...);
    ...
})
```

具名方法則可自然擴展：

```java
private void auditSuccess(Order order) {
    log.info("...");
    metric.orderSuccess();
    tracing.tag("orderId", order.id());
}
```

領域與基礎設施的界限也更明確。

---

## 3.6.1 推薦的副作用命名模式

以下是你專案中標準可採用的慣例：

|動作|命名建議|
|---|---|
|成功紀錄|`logSuccess`, `auditSuccess`, `recordSuccess`|
|失敗紀錄|`logFailure`, `auditFailure`, `recordViolation`|
|審計|`auditXxx`, `traceXxx`|
|發送事件|`emitXxxEvent`, `publishXxx`|
|指標統計|`metricXxx`, `countXxx`|

範例：

```java
return Maybe.given(payload)
        .toViolation(Violations.error("error", "missing"))
        .map(repository::find)
        .filter(validator::checkRule, Violations.error("rule fail"))
        .map(writer::write)
        .peek(this::auditSuccess)
        .peekError(this::auditFailure);
```

---

## 3.6.2 最佳實作範例（正式風格）

```java
private void auditSuccess(Result result) {
    log.info("[order succeed] id={}", result.id());
    metrics.counter("order.success").increment();
}

private void auditFailure(Violations violations) {
    log.warn("[order failed] {}", violations);
    metrics.counter("order.failure").increment();
}

public Validation<Violations, Result> handle(OrderPayload payload) {
    return Maybe.given(payload)
            .toViolation(Violations.error("missing"))
            .map(repository::findOrder)
            .filter(order -> validator.isValid(order),
                    Violations.error("invalid"))
            .map(writer::saveOrder)
            .peek(this::auditSuccess)
            .peekError(this::auditFailure);
}
```

這條鏈：

- 主流程乾淨
    
- 副作用可維護
    
- stack trace 可追
    
- 日誌統一
    
- 測試可讀
    

完全符合 `FP、DDD、Clean Architecture`。

---

### ✔ 風格指南的規範

> **所有副作用行為（log/audit/metrics）建議不要使用匿名 lambda，  
> 必須抽為具名 private method，以確保可讀性、可測試性與可維運性。**

---
# 4. `ServiceContext：資料流與狀態流的核心`

Context 作為 pipeline 的唯一資料來源，必須滿足：

1. **可放入 payload（輸入資料）**
    
2. **可被更新與累積（屬於 pipeline 的中間結果）**
    
3. **可被標記中止（abort flag）**
    
4. **可加入副作用（利用 attrs 或特定方法）**
    

典型範例：

```java
public class ServiceContext<T> {
    private T payload;
    private Map<String, Object> attrs = new HashMap<>();
    private boolean aborted;

    public T getPayload(Class<T> type) { ... }
    public void set(String key, Object val) { ... }
    public <A> T get(String key, Class<A> type) { ... }
    public void abort() { this.aborted = true; }
}
```

Context 作為 carrier 有以下特性：

- pipeline 只需要操作 C 不需要操作泛型
    
- 不同 step 可以交換資料（透過 `attrs`）
    
- 可以用作 adapter（從 service → pipeline，把輸入掛到 payload）
    
- 也能支撐更多 metadata（`tenant、userId、traceId`）
    

---

## 4.1 `ExpressionOperation：將 SpEL 納入 FP 服務責任鏈的操作單元`

在服務責任鏈（Service Operation Monad）中，我們以 `map / flatMap / filter / peek`  
等函式式操作組構流程。  
大部分邏輯以 Java method references 實作，但在某些場景下，我們希望「部分邏輯」可以用  
配置、資料庫、或策略描述的方式提供，而不必硬寫在程式中。

這些邏輯通常包括：

- 動態資料查詢：`@repo.find(#id)`
    
- 條件驗證：`#root.status == 'ACTIVE'`
    
- 屬性寫入：`#root.name = #value`
    

若直接在流程中嵌入 SpEL 評估，就會讓 pipeline 混入技術細節，破壞語意一致性。

為此，我們引入 **ExpressionOperation** ——  
將 SpEL 表達式提升為可參與 FP 操作的「一級操作單元」。

---

### 4.1.1 設計角色

`ExpressionOperation` 是一個不可變的 Value Object，由兩個資訊構成：

1. **expression**：一段 `SpEL` 字串
    
2. **`expressionEvaluations`**：評估引擎（`Façade`）
    

它本身不執行表達式，只描述：

> 「這段 `SpEL` 在服務操作中能被映射為什麼操作？」

換句話說，它是一個 _operation descriptor_，在 `FP` 的語境中是：

> **延後求值（lazy evaluation）的運算描述子**

---

### 4.1.2 `ExpressionOperation 映射的四種 FP Operation`

`ExpressionOperation` 本身不是可執行函式。  
要使用它，必須將它「投射」成以下四種 `FP`-friendly operation：

---

**① Predicate：用於 filter 的條件判斷**

```java
<T> Predicate<T>
```

用途：

- 驗證 payload 是否符合條件
    
- 應用於 `.filter(...)`
    

語意：

> 「當前 payload 是否符合這段 `SpEL` 的布林表達式？」

`SpEL` 錯誤或回傳 null → `false`。

---

**② Reader：用於 map 的資料取用**

```java
<T, R> Function<T, R>
```

用途：

- 根據 payload 計算出下一步的資料
    
- 用於 `.map(...)`
    

語意：

> 「根據這段 `SpEL` 從 payload 中推導出 R。」

`SpEL` 錯誤或 null → 回傳 null，不丟例外。

---

**③ Writer：用於 `SpEL setValue` 的副作用**

Writer 代表「在 `StepContext.payload` 上執行寫入行為」的 `SpEL` 包裝。

**型別**

`Consumer<StepContext<T>>`

**用途**

- 執行 SpEL `setValue` 副作用
    
- 更新 payload 的欄位
    
- 多用於 `.writer(...)`、`.peek(...)`、`.peekError(...)` 內部觸發
    

**語意**

> 「根據 `StepContext` 取得 vars 與 value，並在 payload 上執行 `SpEL setValue`。」

Writer **不會修改 `StepContext` 本身**，  
但 `SpEL` 的寫入副作用可能會改變 payload 的內容。

**觸發方式**

Writer 不會直接被 pipeline 當作 callback 使用；  
而是由 `BehaviorStep` 或 `.peek(ctx -> writer.accept(ctx))` 間接觸發。

---

**④ `Validation-aware Mapper：flatMap 等價操作`**

```java
<T, R> Function<T, Validation<Violations, R>>
```

若 R 為 null，或評估錯誤，則回傳：

```
Validation.invalid(Violations)
```

用途：

- 可直接套入 `.flatMap(...)`
    
- 將 `SpEL` 的失敗語意轉換為 Validation Monad 的 invalid
    

語意：

> 「讓 `SpEL` 表達式擁有 Validation 失敗語意。」

---

### **4.1.3 `為什麼要把 SpEL 提升成 ExpressionOperation`？**

**(1) 隱藏技術細節，保護 `FP` pipeline 的語意清晰**

服務流程只看到：

- predicate
    
- reader
    
- writer
    
- `validationFunction`
    

不會看到：

- `ExpressionParser`
    
- `EvaluationContext`
    
- `Exception handling`
    
- `BeanResolver`
    

讓 `FP` pipeline 的語意完整維持：

> 「資料轉換、條件判斷、副作用、錯誤語意」

---

**(2) 避免例外污染 `FP` 流程**

`ExpressionOperation` 所產生的 operation 都透過 `Maybe` 包裝：

- `SpEL` parse 錯誤
    
- 評估錯誤
    
- type casting 錯誤
    

全部被吸收，不會中斷流程。  
`FP pipeline` 以資料語意回應：

|操作|錯誤語意|
|---|---|
|predicate|false|
|reader|null|
|writer|no-op|
|validationFunction|Validation.invalid|

---

**(3) 適合配置化／策略化的邏輯注入**

ExpressionOperation 讓整個 monad pipeline：

- 可寫死（hard-coded）
    
- 可配置（config）
    
- 可從資料庫載入（policy-based）
    

都是同一種 `FP` operation。

---

**(4) 語意與 monad 的角色分離明確**

- **pipeline 控制流程**
    
- **`ExpressionOperation 控制語意映射`**
    
- **`ExpressionEvaluation 控制 SpEL 執行`**
    

三者分離，使邏輯清晰且可測試。

---

### **4.1.4 在服務責任鏈中的使用範例**

```java
var expr = expressionEvaluations;

var findUserOp = expr.of("@userRepository.find(#id)");
var activeOp   = expr.of("#root.active");
var setNameOp  = expr.of("#root.name = #value");

return Maybe.given(userId)
    .toViolation(Violations.violate("error", "missing userId"))

    // 讀取資料：T -> R
    .map(findUserOp.reader(id -> Map.of("id", id)))

    // 驗證條件：T -> boolean
    .filter(activeOp.predicate(u -> Map.of()), Violations.violate("inactive"))

    // 寫入副作用：在 peek 中觸發 writer.accept(...)
    .peek(user -> setNameOp.writer(u -> Map.of()).accept(user, "NewName"))

    .peek(this::logSuccess)
    .peekError(this::logFailure);
```

結果：

- pipeline 完全保持 `FP` 語意
    
- 外部邏輯可配置
    
- `SpEL` 變成 `FP DSL` 的一級公民
    

---

### **4.1.5 小結：`ExpressionOperation` 的定位**

**`ExpressionOperation = SpEL 的 FP 封裝形式`。**

它讓 `SpEL` 能夠：

- 不丟 exception
    
- 以 `FP` 語義來表現成功/失敗
    
- 映射成多種 `FP` 操作（`Predicate, Function, BiConsumer, Validation`）
    
- 無痕融入既有的 monad pipeline
    

透過這個設計，服務流程可以自然且一致地整合：

- 程式內邏輯
    
- 配置化策略
    
- 外部注入規則
    

而不破壞整體的 functional structure。

---
## 4.2 `ExpressionSteps`：在 Behavior Pipeline 中使用的 `DSL` 範例

`ExpressionSteps` 的角色，是把 `ExpressionOperation` 轉成具備

```java
StepContext<T> -> Validation<Violations, StepContext<T>>
```

語意的 `BehaviorStep<T>`，  
讓 `SpEL` 可以直接成為 Behavior Pipeline 裡的一級步驟，同時維持：

- **Context 為唯一資料流載體**
    
- **Validation 為錯誤通道**
    
- **SpEL 只負責「語意映射」，不直接拋例外**
    

典型使用流程：

1. 用 `ExpressionEvaluations.of(String)` 建立一個 `ExpressionOperation`
    
2. 用 `ExpressionSteps` 將其轉成 `BehaviorStep<T>`
    
3. 交給 `BehaviorPipeline` 串接多個 Step
    

本節示範 `ExpressionSteps` 提供的四種 SpEL Step 型別：

1. `predicate(...)`：條件驗證 Step
    
2. `readerWithAttribute(...)`：讀取資料並寫入 `StepContext.attribute`
    
3. `writer(...)`：在 payload 上執行 `SpEL setValue` 副作用
    
4. `validateWithAttribute(...)`：帶 Validation 語意的映射 Step
    

---

### 4.2.1 驗證條件：`predicate(...)` 配合 filter 語意

**方法簽名（概念）**

```java
public <T> BehaviorStep<T> predicate(
        ExpressionOperation op,
        Function<StepContext<T>, Map<String, Object>> varsProvider,
        Supplier<Violations> onInvalid)
```

**語意**

- 先把 `ExpressionOperation` 映射成：
    
    ```java
    Predicate<StepContext<T>>
    ```
    
- 在 Step 執行時：
    
    - 若前一個 Step 已經是 `invalid` → 原樣傳遞，不再執行 SpEL
        
    - 否則呼叫 `predicate.test(ctx)`：
        
        - 回傳 `true` → Step 成功：`Validation.valid(ctx)`
            
        - 回傳 `false` → Step 失敗：`Validation.invalid(onInvalid.get())`
            
        - SpEL parse / 評估錯誤 → 視同 `false`，並使用 `onInvalid` 產生錯誤
            

**範例：只允許「狀態為 ACTIVE 的使用者」繼續往下**

```java
// 假設已有
// @Autowired ExpressionEvaluations expressionEvaluations;
// @Autowired ExpressionSteps expressionSteps;
// @Autowired BehaviorPipeline<UserInfo> pipeline;

ExpressionOperation activeExpr =
        expressionEvaluations.of("#root.status == 'ACTIVE'");

BehaviorStep<UserInfo> activeCheckStep =
        expressionSteps.predicate(
                activeExpr,
                ctx -> Map.of(), // root 即為 payload：ctx.getPayload()
                () -> Violations.violate("USER_INACTIVE", "使用者未啟用")
        );

pipeline.add(activeCheckStep);
```

**直覺解讀**

- SpEL：`#root.status == 'ACTIVE'`
    
- `varsProvider` 用 `ctx` 決定 SpEL 的變數來源（此例不需要額外變數）
    
- 若 payload 的 `status` 不是 `"ACTIVE"`，或 SpEL 評估失敗，  
    → 回傳 `Validation.invalid(USER_INACTIVE)`，後續 Step 不再執行。
    

---

### 4.2.2 讀取資料並放入 Context：`readerWithAttribute(...)`

**方法簽名（概念）**

```java
public <T, R> BehaviorStep<T> readerWithAttribute(
        ExpressionOperation op,
        Function<StepContext<T>, Map<String, Object>> varsProvider,
        String attributeName)
```

**語意**

- 把 `ExpressionOperation` 映射成：
    
    ```java
    Function<StepContext<T>, R>
    ```
    
- 在 Step 執行時：
    
    - 若 Context 已經 invalid → 原樣傳遞，不執行 SpEL
        
    - 否則：
        
        1. 用 `reader.apply(ctx)` 取得結果 `R`
            
        2. 若結果為 **非 null** → 寫入 `ctx.withAttribute(attributeName, value)`
            
        3. 若結果為 **null 或 SpEL 評估失敗** → 呼叫 `ctx.withNoneAttribute(attributeName)`，清掉既有的 attribute
            

也就是：

> 「成功取得值就更新 attribute；失敗或為 null 就把這個 attribute 移除。」

**範例：根據 `request.id` 查詢 user 並存到 `ctx.attr("user")`**

```java
// SpEL：使用 Spring Bean @userRepository 來查詢
ExpressionOperation findUserExpr =
        expressionEvaluations.of("@userRepository.findById(#id)");

BehaviorStep<RequestPayload> loadUserStep =
        expressionSteps.readerWithAttribute(
                findUserExpr,
                ctx -> Map.of("id", ctx.getPayload().getUserId()),
                "user" // 存成 context attribute: "user"
        );

pipeline.add(loadUserStep);
```

**語意重點**

- `varsProvider` 可以看整個 `StepContext`：
    
    - `ctx.getPayload()`：原始請求
        
    - `ctx.getAttribute(...)`：前面 Step 的查詢結果
        
- 查不到或 SpEL 回傳 null：
    
    - 使用 `withNoneAttribute("user")` 清空 `user` attribute
        
    - pipeline 不會因為查不到 user 就變成 invalid，而是讓後面 Step 依據「有沒有 user」自行決策
        

---

### 4.2.3 執行副作用：`writer(...)` 寫入 payload

**方法簽名（概念）**

```java
public <T, V> BehaviorStep<T> writer(
        ExpressionOperation op,
        Function<StepContext<T>, Map<String, Object>> varsProvider,
        Function<StepContext<T>, V> valueProvider)
```

**語意**

- 把 `ExpressionOperation` 映射成：
    
    ```java
    Consumer<StepContext<T>>
    ```
    
- 在 Step 執行時：
    
    - 若 Context 已經 invalid → 直接回傳原本的 invalid，完全不觸發 SpEL
        
    - 若 Context 為 valid →
        
        - 由 `varsProvider` 與 `valueProvider` 從 `StepContext` 中取得：
            
            - SpEL 變數 `vars`
                
            - 要寫入的 `value`
                
        - 在 payload 上執行 `SpEL setValue` 副作用
            
        - 回傳 `Validation.valid(ctx)`，**不改變 Context 結構**
            

Writer **只改變 payload 的內容**，不會建立或修改 attribute。

**範例：把 `order.status` 更新為 `COMPLETED`**

```java
// SpEL：設定 root.status = #value
ExpressionOperation setStatusExpr =
        expressionEvaluations.of("#root.status = #value");

// valueProvider：從 StepContext 計算要寫入的值
BehaviorStep<Order> completeStatusStep =
        expressionSteps.writer(
                setStatusExpr,
                ctx -> Map.of("value", "COMPLETED"), // SpEL 變數
                ctx -> "COMPLETED"                    // 寫入的 value
        );

pipeline.add(completeStatusStep);
```

**語意重點**

- 若前面 Step 失敗 → 此 Step 完全 no-op（不動 payload，也不洗掉錯誤）
    
- 若 Context 為 valid → 使用 SpEL 對 `ctx.getPayload()` 進行寫入
    
- `writer(...)` 適合：
    
    - 更新狀態欄位
        
    - 記錄某種「旗標」型欄位
        
    - 用 SpEL 寫入複雜 nested 結構（例如 map / list 裡的元素）
        

---

### 4.2.4 帶錯誤語意的 SpEL 映射：`validateWithAttribute(...)`

**方法簽名（概念）**

```java
public <T, R> BehaviorStep<T> validateWithAttribute(
        ExpressionOperation op,
        Function<StepContext<T>, Map<String, Object>> varsProvider,
        String attributeName)
```

**語意**

- 把 `ExpressionOperation` 映射成：
    
    ```java
    Function<StepContext<T>, Validation<Violations, R>>
    ```
    
- 在 Step 執行時：
    
    1. 若 Context 已 invalid → 直接傳遞，不再執行 SpEL
        
    2. 否則：
        
        - 執行 `validationFunction.apply(ctx)`：
            
            - 若結果為 `Validation.invalid` → 直接成為此 Step 的結果，後面 Step 不再執行
                
            - 若結果為 `Validation.valid(value)` → 將 `value` 寫入 `ctx.withAttribute(attributeName, value)`，回傳 `Validation.valid(nextCtx)`
                

也就是：

> 「SpEL 失敗 → 直接變成 Validation 的 invalid；  
> 成功 → 同時寫入 attribute 並保持 Context 為 valid。」

**範例：計算風險分數，結果必須有效**

```java
// SpEL：計算評分結果，例如根據 age / income / history 等欄位
ExpressionOperation riskExpr =
        expressionEvaluations.of("#root.calculateRisk()");

BehaviorStep<Customer> scoreStep =
        expressionSteps.validateWithAttribute(
                riskExpr,
                ctx -> Map.of(),     // 不需要額外變數
                "riskScore"          // context attribute: "riskScore"
        );

pipeline.add(scoreStep);
```

**語意重點**

- 若 SpEL 回傳 null、型別錯誤、評估失敗 → 轉成 `Validation.invalid(Violations)`
    
- 一旦 invalid 產生，後續 Step 不再執行
    
- 適合：
    
    - 必須產生的關鍵欄位（fee、score、quota 等）
        
    - 規則引擎型計算，需要把錯誤納入 Validation 流程
        

---

### 4.2.5 綜合範例：用 `ExpressionSteps` 組一條完整的行為管線

以下是一條涵蓋四種 SpEL Step 型別的示意管線：

```java
ExpressionOperation findUserExpr  = expressionEvaluations.of("@userRepository.findById(#id)");
ExpressionOperation activeExpr    = expressionEvaluations.of("#root.active");
ExpressionOperation setFlagExpr   = expressionEvaluations.of("#root.flags[#flagName] = true");
ExpressionOperation riskExpr      = expressionEvaluations.of("#root.calculateRisk()");

BehaviorPipeline<RequestPayload> pipeline = new BehaviorPipeline<>();

pipeline

    // 1. 載入 user -> ctx.attr("user")
    .add(expressionSteps.readerWithAttribute(
            findUserExpr,
            ctx -> Map.of("id", ctx.getPayload().getUserId()),
            "user"
    ))

    // 2. 驗證 user 必須 active
    .add(expressionSteps.predicate(
            activeExpr,
            ctx -> Map.of(),  // root 即為 ctx.getPayload()
            () -> Violations.violate("USER_INACTIVE", "使用者未啟用")
    ))

    // 3. 設定某個 flag（副作用）：user.flags["VERIFIED"] = true
    .add(expressionSteps.writer(
            setFlagExpr,
            ctx -> Map.of("flagName", "VERIFIED"),
            ctx -> "VERIFIED"
    ))

    // 4. 計算風險分數（必須成功）
    .add(expressionSteps.validateWithAttribute(
            riskExpr,
            ctx -> Map.of(),
            "riskScore"
    ));
```

這條 pipeline 的責任切分如下：

- **BehaviorPipeline**：組裝與執行 StepChain
    
- **ExpressionSteps**：把 `ExpressionOperation` 映射成具有 Validation 語意的 `BehaviorStep`
    
- **ExpressionOperation**：描述「這段 SpEL 要被當成什麼 FP 操作」（predicate / reader / writer / validation）
    
- **ExpressionEvaluation**：負責實際 SpEL parse / 評估與錯誤吸收
    

整體效果：

- 服務流程可以以 **Context + Monad** 的 FP 語意建構
    
- SpEL 只是一個「可配置的語意來源」，完全融入 pipeline
    
- 錯誤流統一由 `Validation<Violations, StepContext<T>>` 控制，不會被 Step 洗掉
    

---

# 5. Step Chain（責任鏈）實作

`ServiceOperationStep` 透過 monad 的 `flatMap` 簡單串接。

## 5.1 基礎：`ServiceChain.run`

```java
public final class ServiceChain {

    @SafeVarargs
    public static <C> Validation<Violations, C> run(
            C initial,
            Function<C, Validation<Violations, C>>... steps
    ) {
        Validation<Violations, C> current = Validation.success(initial);

        for (Function<C, Validation<Violations, C>> step : steps) {
            current = current.flatMap(step);
        }

        return current;
    }
}
```

## 5.2 使用方式

```java
Validation<Violations, ServiceContext> result =
        ServiceChain.run(
            ctx,
            svc1::doXxx,
            svc2::doYyy,
            svc3::doZzz
        );
```

每個 step：

- 讀 ctx.payload / ctx.attrs
    
- 寫 ctx.attrs / ctx.response
    
- 若失敗 → monad 短路
    
- 若成功 → 傳到下一個 step
    

---

# 6. Step（服務步驟）寫法範例

以下是典型 service step：

```java
public Validation<Violations, ServiceContext> doLoad(ServiceContext ctx) {

    XxxPayload payload = ctx.getPayload(XxxPayload.class);

    return Maybe.given(payload)
            .toViolation(Violations.violate(ERROR, "missing payload"))
            .map(repository::findById)
            .filter(validators::isValid,
                    Violations.violate(ERROR, "invalid payload"))
            .peek(rec -> ctx.set("record", rec))
            .map(ok -> ctx);
}
```

第二步：

```java
public Validation<Violations, ServiceContext> doWrite(ServiceContext ctx) {

    X rec = ctx.get("record", X.class);

    return Try.of(() -> writer.write(rec))
            .toValidation(Violations::fromException)
            .peek(r -> ctx.set("result", r))
            .map(ok -> ctx);
}
```

第三步（副作用 step）：

```java
public Validation<Violations, ServiceContext> audit(ServiceContext ctx) {

    return Try.run(() -> auditService.log(ctx))
            .toValidation(Violations::fromException)
            .map(ok -> ctx);
}
```

---

# 7. 行為擴充：用 BehaviorPipeline 包裝 ServiceOperationStep

雖然本指南主體以 ServiceChain.flatMap 為主，  
但若流程更複雜（含 abort-but-not-error、多重副作用、多階段 process），  
可以任意插入 BehaviorPipeline：

```java
BehaviorPipeline<ServiceContext> pipeline =
        new BehaviorPipeline<ServiceContext>()
            .addStep(ctx -> svc1.doXxx(ctx))
            .addStep(ctx -> svc2.doYyy(ctx))
            .addStep(ctx -> svc3.doZzz(ctx));
```

`ServiceOperationStep` 自然符合：

```java
ctx -> Validation<Violations, ctx>
```

Pipeline 也只需要：

```java
if (!result.success()) ctx.abort();
```

即可。

---

# 8. 設計決策流程（何時用什麼？）

以下為推薦決策：

## 用 `ServiceChain`（此指南主體）

若需求是：

- 單一線性流程
    
- 錯誤即中止即可
    
- 中間 state 不多
    
- 主要任務是 CRUD / 驗證 / 寫入
    

## 用 `BehaviorPipeline`

若需求包括：

- 多階段、多模塊的 orchestration
    
- 控制流不只成功/失敗
    
- 要 early-abort 但仍記錄後續 log
    
- 有大量副作用（MQ、Audit、Event、Redis）
    
- 需要 reusable 行為 step
    

## 用 BehaviorStep（pure transformer pipeline）

若需求是：

- 多欄位、轉換、建構 DSL
    
- rule engine / validation engine
    
- pure transformation chain
    
- 只操作 `StepContext`，不做重 IO
    

---

# 9. 總結：一個乾淨、一致、可維護的服務模型

本指南的設計重點：

1. **Context 作為唯一資料與狀態流載體**
    
2. **Monad（Validation / Maybe / Try）作為流程控制引擎**
    
3. **ServiceOperationStep 作為概念，而非型別**
    
4. **服務方法自然符合 Step（無需 implements）**
    
5. **所有流程以 StepChain 或 BehaviorPipeline 串接**
    
6. **擴充彈性高，可自由加入副作用、audit、多種中間結果**
    
7. **程式碼精簡、整齊、可測試、可複用**
    

這為服務層提供了：

- 清晰的語意
    
- 一致的責任鏈模型
    
- 極高的可組合性與可維護性
    
- 與現有 Monad 工具完美整合
    

---

# 完整指南初稿到這裡結束。

---

