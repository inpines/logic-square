
---
# 📘 `CH4 — Validation 與 Applicative：錯誤累積模型`

完整技術版

---

# 4.1 為什麼服務流程不能只靠「throw exception」？

在服務邏輯中，有兩種錯誤：

|類型|意義|來源|處理方式|
|---|---|---|---|
|**技術錯誤（Technical Failure）**|系統失敗、不可預期|DB、HTTP、IO、NPE|Try／Exception|
|**業務錯誤（Business Violation）**|條件不符、規範不滿足|欄位、狀態、邏輯|Validation|

兩者本質不同：

- 技術錯誤**應中斷流程**，因為系統不再安全。
    
- 業務錯誤**不一定要中斷流程**，因為系統仍然可運作，只是輸入不符合規範。
    

若全部使用 throw：

- 無法區分業務 vs 技術
    
- 無法累積錯誤（只能第一個錯誤出現）
    
- 錯誤分散在多個 catch 裡
    
- 控制流變成例外驅動（unreadable）
    
- 測試困難（assert exception）
    

因此必須引入更高階的錯誤表達方式：

> **Validation<E, T>：能同時承載成功或多個錯誤的容器。**

---

# 4.2 Validation 的語意：成功與錯誤不再互斥

Validation 只有兩種形態：

```text
Valid(value)
Invalid(errors)
```

但與一般 Either 最大差別是：

> **Invalid 可以累積多個錯誤，而不是遇錯就停（fail-fast）。**

來看直覺範例：

### 一般 fail-fast 行為（不符合業務需求）

```java
if (!checkA()) return errorA;
if (!checkB()) return errorB;
if (!checkC()) return errorC;
```

使用者會得到：

```
第一個錯誤：A
```

但實際上 A、B、C 全部都錯，使用者需要全部訊息。

---

# 4.3 為什麼 Validation 能累積錯誤？（Applicative 語意）

這是 CH4 最重要的一段：

> **Validation 不是 Monad（不能用 flatMap 串多個累積驗證）。  
> 它是 Applicative。**

而 Applicative 允許：

- **每個驗證獨立運作**
    
- **最後把所有結果組合起來**
    
- **錯誤可以被 join 在一起**
    

而不是像 Monad 那樣：

- 先做 A
    
- A 成功才會做 B
    
- B 成功才會做 C
    

Validation 的世界是：

```
同時做 A、B、C
最後組合結果
```

用語意圖示：

```
Validation(A)
Validation(B)
Validation(C)

Applicative merge → Validation(A + B + C)
```

若全部成功：

```
Valid(…)
```

若有任何失敗：

```
Invalid(errors = errorA + errorB + errorC)
```

---

# 4.4 Violations：錯誤集合（`Joinable` 的實作）

在我們的架構中，Validation 的錯誤型別 E 被定義為：

```
E = Violations
```

而 `Violations` 具備：

- `List<GeneralVioloation>`
    
- join(Violations) 方法
    
- 與 Validation 完美搭配
    

範例如：

```java
violations.join(moreViolations)
```

除此之外，我們同時還定義了 `ViolationSeverity`：

```java
public enum ViolationSeverity {
    INFO, WARNING, ERROR, FATAL, UNSPECIFIED
}
```

這使得錯誤不只支援「多筆」，也支援「等級」。

這裡的語意非常重要：

- **Validation 決定的是成功或失敗**
    
- **`ViolationSeverity` 決定的是這個失敗對流程的意義**
    

例如：

- ERROR → 可以累積
    
- FATAL → 之後在 Pipeline 可能導致 abort
    
- WARNING → 不一定要變成 Invalid
    

（決策策略在 CH10 解說）

---

# 4.5 在 StepContext 中累積錯誤

`StepContext<T>` 中核心方法：

```java
public StepContext<T> addViolation(GeneralViolation v)
```

它其實是：

```
violations = violations.join(newViolation)
```

也就是：

```
A + B + C + …
```

當 Validation 回傳 `Invalid(violations)` 時，  
StepContext 仍然保留所有錯誤，讓 Pipeline 可以在後面評估：

- 是否因嚴重度而 abort
    
- 是否進行 fallback
    
- 是否繼續後續步驟
    
- 是否只做 audit 而不寫資料
    

---

# 4.6 如何在 Step 中回傳 Validation

一個典型的業務驗證 Step：

```java
public Validation<Violations, StepContext<P>> validateInput(StepContext<P> ctx) {

    Validation<Violations, Void> v1 =
            payloadValidator.checkA(ctx.getPayload());

    Validation<Violations, Void> v2 =
            payloadValidator.checkB(ctx.getPayload());

    Validation<Violations, Void> v3 =
            payloadValidator.checkC(ctx.getPayload());

    return Validation.merge(v1, v2, v3)
            .map(ok -> ctx);  // 若全部成功，再傳回 Context
}
```

這段包含幾個重要原則：

### ✔ 1. Step 需要回傳 Context，而不是驗證值

因此最後用：

```java
.map(ok -> ctx)
```

### ✔ 2. 多個 Validation 可由 Applicative merge

你的 Validation 類別通常會有一個：

```java
ValidationUtils.mergeAll(v1, v2, v3)
```

或

```java
ValidationUtils.combineAll(list)
ValidationUtils.combineAll(map)
```

### ✔ 3. 若任一驗證失敗，會回 Invalid

但錯誤集合會是：

```
v1.errors + v2.errors + v3.errors
```

而不是只有第一個。

---

# 4.7 Conditional Step：filter 與 Validation 的互動

filter 適合簡單條件：

```java
return Validation.valid(ctx)
        .filter(c -> c.getPayload().isActive(),
                Violations.from("USER_INACTIVE", ERROR));
```

複雜條件則用 Validation：

```java
return checkRuleA(payload)
    .merge(checkRuleB(payload))
    .merge(checkRuleC(payload))
    .map(ok -> ctx);
```

### 哪一種比較好？

- **filter**：只有一個條件 → 一個錯誤
    
- **Validation merge**：多條件 → 多個錯誤
    

---

# 4.8 FATAL 與 ERROR：與 aborted 的關係

我們在 `StepContext` 裡提供：

```java
public boolean hasFatalErrors()
public boolean hasSevereThan(ViolationSeverity level)
```

所以在 pipeline 裡你可以寫一個 Step：

```java
public Validation<Violations, StepContext<P>> decideAbort(StepContext<P> ctx) {
    if (ctx.hasFatalErrors()) {
        ctx.setAborted(true);
    }
    return Validation.valid(ctx);
}
```

語意：

- Validation 決定此步驟的成功/失敗
    
- ViolationSeverity 決定後續流程是否繼續
    
- StepContext 控制流程語意（abort ≠ invalid）
    

這代表：

> **錯誤收集（Validation）與流程控制（abort）是分離的。**

這種設計非常強大，也非常安全。

---

# 4.9 實務範例：綜合使用 Validation + StepContext

假設有三個規則：

1. 權限不足（ERROR）
    
2. 資源配額不足（ERROR）
    
3. 安全檢查失敗（FATAL）
    

你可能會寫：

```java
public Validation<Violations, StepContext<P>> validateRules(StepContext<P> ctx) {

    Validation<Violations, Void> v1 = permCheck(ctx)
        .mapError(e -> e.withSeverity(ERROR));

    Validation<Violations, Void> v2 = quotaCheck(ctx)
        .mapError(e -> e.withSeverity(ERROR));

    Validation<Violations, Void> v3 = securityCheck(ctx)
        .mapError(e -> e.withSeverity(FATAL));

    return Validation.merge(v1, v2, v3)
            .peekError(ctx::withViolation)
            .map(ok -> ctx);
}
```

接著 pipeline：

```java
.then(this::validateRules)
.then(this::decideAbort)
```

結果：

- 兩個 ERROR
    
- 一個 FATAL
    
- ctx.aborted = true
    
- 最終 Validation = Invalid(全部三個錯誤)
    

使用者收到完整錯誤，系統也知道應 abort。

---

# 4.10 Validation 的三種使用策略（架構層面）

我們的系統設計非常彈性。Validation 可以採三種模式：

---

### **策略 1：Accumulation-first（預設策略）**

- 盡可能累積錯誤
    
- 最後再根據 severity 決定流向（abort / fallback / success-with-warnings）
    

適用：  
input 驗證、業務規則檢查

---

### **策略 2：Fail-fast（立即停止）**

使用 Either 或在 Validation 內檢查：

```java
if (ctx.hasFatalErrors()) return Invalid(...)
```

適用：  
無法允許流程繼續的條件（安全、資安、法遵）

---

### **策略 3：Hybrid（混合模式）**

例如：

- 有 WARNING → 繼續
    
- 有 ERROR → 回報但可繼續
    
- 有 FATAL → abort
    

適用：  
需要強調錯誤等級與嚴重度的領域（支付、身份驗證、審計流程）

---

# 4.11 `CH4` 小結：Validation 帶來的三大力量

**（一）錯誤收集的能力（`Applicative merge`）**

- support all errors
    
- 保留全部失敗原因
    
- 提供完整可檢測的錯誤集合
    

**（二）與 `StepContext` 的深度整合作用**  
StepContext 提供：

- `violations` 的累積
    
- `aborted` 的流程語意
    
- `attributes` 與 `payload` 的資料流
    
- `Severity` 用於後續決策
    

**（三）與 pipeline 的可組合性**  

Validation 讓流程：

- 一致
    
- 可預測
    
- 可閱讀
    
- 可重組
    

---