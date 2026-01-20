
---

# 📘 `CH3 — Monad 作為流程控制引擎`

完整技術版

---

## 3.1 服務流程真正需要的是「可組合的控制流」

在 `CH1` 我們已經說過，傳統的 service 方法很容易長成這樣：

```java
public Result doSomething(Request request) {
    if (!basicCheck(request)) {
        log.warn("basic check failed");
        return Result.error("BAD_REQUEST");
    }

    User user;
    try {
        user = userRepository.findById(request.userId());
    } catch (Exception e) {
        log.error("db error", e);
        return Result.error("DB_ERROR");
    }

    if (!user.isActive()) {
        return Result.error("USER_INACTIVE");
    }

    // ... 一路 if / try / return 疊上去 ...
}
```

可以看到幾個問題：

- **控制流分裂**：return 到處出現（成功、失敗、例外…）
    
- **錯誤語意混亂**：有時回傳 errorCode，有時丟 exception，有時只 log
    
- **步驟無法重組**：每個條件判斷直接寫死在方法內
    
- **難以掛副作用**：log / audit / metrics 參雜在邏輯中
    

我們想要的是：

> 「一條線的流程」＋「顯式錯誤通道」＋「可插拔步驟」＋「副作用有固定位置」。

這正是 Monad pipeline 擅長的領域。

---

## 3.2 我們實際使用的 Monad 家族與職責分工

在這套服務模型中，我們不是抽象談「所有 Monad」，而是選定一些有明確用途的：

|Monad|型別形式|主要用途|
|---|---|---|
|`Maybe<T>`|`T` 或「空」|選擇性資料、可能不存在的值|
|`Validation`|`Valid(T)` / `Invalid(E)`|可累積錯誤的驗證|
|`Either<L,R>`|`Left(L)` / `Right(R)`|fail-fast 型錯誤／結果|
|`Try<T>`|`Success(T)` / `Failure(ex)`|技術性例外包裝|
|`Task<T>`|延遲執行的計算|非同步／延後執行（若有需要）|

> 核心概念：**成功通道 + 錯誤通道**，以型別封裝控制流。

在服務責任鏈中，真正掛在 Step 上的主要是：

- `Validation<Violations, StepContext<T>>`
    
- 有需要時會透過 `Try` → `Validation` 的轉換，把技術錯誤轉入同一條錯誤通道。
    

---

## 3.3 共通操作：`map / flatMap / filter / peek / peekError`

### 3.3.1 成功通道上的操作：map

`map` 用於「在成功的情況下變換值」，例如：

```java
Validation<Violations, StepContext<T>> vCtx = // ...

// 更新 payload
Validation<Violations, StepContext<T>> updated =
        vCtx.map(ctx -> ctx.transit(transform(ctx.getPayload())));
```

特性：

- 如果當前是 `Valid`：套用函數並回傳新的 `Valid`
    
- 如果當前是 `Invalid`：直接傳遞錯誤，不執行函數
    

---

### 3.3.2 串接下一個步驟：flatMap

`flatMap` 是把「一個成功的結果」傳給「下一個會回傳 Monad 的運算」：

```java
Validation<Violations, StepContext<T>> next =
        vCtx.flatMap(this::nextStep);
```

其中：

```java
Validation<Violations, StepContext<T>> nextStep(StepContext<T> ctx) { ... }
```

所有 Step 都被設計成：

```java
StepContext<T> -> Validation<Violations, StepContext<T>>
```

因此 pipeline 可以自然寫成：

```java
return given(initialCtx)
    .flatMap(this::loadUser)
    .flatMap(this::checkQuota)
    .flatMap(this::calcRiskScore)
    .flatMap(this::writeRecord);
```

---

### 3.3.3 filter：條件驗證

`filter` 將「布林條件」轉換成「成功或錯誤」：

```java
Validation<Violations, StepContext<T>> checked =
        vCtx.filter(
            ctx -> ctx.getPayload().isActive(),
            ctx -> Violations.from("USER_INACTIVE")
        );
```

語意：

- 條件為 true：保持 `Valid`
    
- 條件為 false：轉為 `Invalid(violations)`
    

這裡的 `Violations` 可根據你的錯誤模型，並可搭配 `ViolationSeverity.ERROR` 或 `FATAL`。

---

### 3.3.4 副作用掛載點：peek / peekError

`peek` 用於成功路徑的副作用：

```java
vCtx.peek(ctx -> auditSuccess(ctx));
```

`peekError` 用於錯誤路徑的副作用：

```java
vCtx.peekError(violations -> auditFailure(violations));
```

重要的是：

- 這兩個操作**不改變**成功或錯誤的結果
    
- 單純用於記錄、log、metrics、通知等副作用
    
- 副作用應使用具名方法，而非匿名 lambda（在 CH5 詳談）
    

---

## 3.4 把 StepContext 納入型別：Step 的正式形式

在 CH2 我們確認了 `StepContext<T>` 的實作。  
在這一章的觀點下，我們可以更具體地定義 Step：

> **Step = `StepContext<T> -> Validation<Violations, StepContext<T>>`**

也就是說，所有 Step method 至少長這樣：

```java
public Validation<Violations, StepContext<MyPayload>> doSomething(StepContext<MyPayload> ctx) {
    ...
}
```

整個責任鏈就是一條：

```text
StepContext<T> 
  -> Validation<Violations, StepContext<T>>
  -> Validation<Violations, StepContext<T>>
  -> ...
```

在 ServiceChain 中，會用 `flatMap` 將所有步驟串成一條線。

---

## 3.5 實際模式：幾種典型 Step 寫法

下面用幾個「實際會出現」的 Step 來說明 Monad 在流程中的角色。

---

### 3.5.1 載入資料 Step：Maybe + Validation

目標：

- 從 payload 中拿出 id
    
- 查 repository 找到 domain object
    
- 若不存在 → 回報錯誤
    

```java
public Validation<Violations, StepContext<MyPayload>> loadUser(StepContext<MyPayload> ctx) {
    MyPayload payload = ctx.getPayload();

    return Maybe.given(payload.getUserId())
            .map(userRepository::findById)
            .filter(Objects::nonNull)
            .toValidation(() -> Violations.from("USER_NOT_FOUND", ViolationSeverity.ERROR))
            .peek(user -> ctx.withAttribute("user", user))
            .map(ok -> ctx);
}
```

這裡 `Maybe.given(...)`：

- 沒有 userId → 直接變成 Invalid
    
- 找不到 user → filter false → Invalid
    
- 成功找到 user → 設到 `ctx.attributes`，最後 `map(ok -> ctx)` 回傳 Context
    

---

### 3.5.2 驗證 Step：Validation 集中錯誤

假設有多個業務條件要檢查：

- quota 是否充足
    
- 風險等級是否允許
    
- 帳號是否未鎖定
    

這類情境適合用 `Validation` 聚合錯誤（詳細在 CH4 展開），這裡先展示 Monad 層的寫法：

```java
public Validation<Violations, StepContext<MyPayload>> validateBusinessRules(StepContext<MyPayload> ctx) {
    User user = ctx.getAttribute("user", User.class::cast);

    Validation<Violations, User> v1 =
            checkQuota(user);          // Validation<Violations, User>
    Validation<Violations, User> v2 =
            checkRiskLevel(user);
    Validation<Violations, User> v3 =
            checkAccountStatus(user);

    return Validation.merge(v1, v2, v3)   // Valid(User) 或 Invalid(Violations)
            .peek(u -> ctx.withAttribute("validatedUser", u))
            .map(u -> ctx);
}
```

在這裡，Monad 做了兩件事：

1. 保證只有成功才會傳遞到下一步
    
2. 把所有錯誤統一保存在 `Violations` 裡（而非散落各處）
    

---

### 3.5.3 寫入 Step：Try + Validation

寫入 DB、呼叫外部 API 都可能丟出技術性例外，  
這時可以先用 `Try` 包裝，再轉 `Validation`：

```java
public Validation<Violations, StepContext<MyPayload>> writeRecord(StepContext<MyPayload> ctx) {
    MyPayload payload = ctx.getPayload();

    return Try.attempt(() -> writer.save(payload))   // Try<SavedRecord>
            .toValidation(e -> Violations.from("WRITE_FAILED", ViolationSeverity.FATAL))
            .peek(saved -> ctx.withAttribute("savedRecord", saved))
            .map(ok -> ctx);
}
```

語意：

- `Try` 負責：把 `Exception` 收進 `Failure`
    
- `toValidation` 負責：把 `Failure` 轉成 `Invalid(Violations)`
    
- pipeline 的錯誤通道仍統一是 `Violations`
    

---

### 3.5.4 副作用 Step：只用 peek / peekError，不影響結果

例如做 audit：

```java
public Validation<Violations, StepContext<MyPayload>> audit(StepContext<MyPayload> ctx) {
    return Validation.valid(ctx)
            .peek(this::auditSuccess)
            .peekError(this::auditFailure);
}
```

或者直接掛在前面的 Step 後面：

```java
return Try.attempt(...)
        .toValidation(...)
        .peek(saved -> auditSuccess(ctx, saved))
        .peekError(violations -> auditFailure(ctx, violations))
        .map(ok -> ctx);
```

這樣：

- 不改變成功/失敗判斷
    
- audit 的行為被「抽離」成獨立方法
    
- 日後要改 audit 邏輯不會碰到主流程
    

---

## 3.6 技術錯誤 vs 業務錯誤：Try 與 Validation 的配合

在這套模型中，我們將錯誤區分為：

1. **業務錯誤（Business Violations）**
    
    - 來源：輸入不合法、規則不符、狀態不允許
        
    - 表達方式：`Violations`（搭配 `ViolationSeverity`）
        
    - 借助 Validation 來累積／判斷
        
2. **技術錯誤（Technical Failures）**
    
    - 來源：DB 連線、HTTP timeout、序列化失敗、IO 錯誤等
        
    - 初步表達方式：Exception
        
    - 在 pipeline 中：先套用 `Try`，再轉成 `Validation<Violations, T>`
        

例子已在 3.5.3 展示：

```java
Try.attempt(() -> writer.save(payload))
   .toValidation(e -> Violations.from("WRITE_FAILED", ViolationSeverity.FATAL))
```

這樣做的好處是：

- **所有錯誤最終都進入 `Violations`**，型別一致
    
- `ViolationSeverity` 可讓後續流程按嚴重度決定是否 `abort` 或 fallback
    

例如，後面可以有一個決策 Step：

```java
public Validation<Violations, StepContext<MyPayload>> decideAbort(StepContext<MyPayload> ctx) {
    if (ctx.getViolations().stream()
            .anyMatch(v -> v.getSeverity() == ViolationSeverity.FATAL)) {
        ctx.setAborted(true);
    }
    return Validation.valid(ctx);
}
```

（更完整的錯誤策略會在 `CH4／CH10` 詳述）

---

## 3.7 從程式碼長相來看：Monad pipeline 與「人類可讀性」

綜合上面的模式，一個「完整的服務流程」可能會長這樣：

```java
public Validation<Violations, StepContext<MyPayload>> process(MyPayload payload) {

    StepContext<MyPayload> initial = StepContext.<MyPayload>builder()
            .withPayload(payload)
            .withViolations(Violations.empty())
            .build();

    return given(initial)
            .flatMap(this::loadUser)
            .flatMap(this::validateBusinessRules)
            .flatMap(this::calcRiskScore)
            .flatMap(this::writeRecord)
            .flatMap(this::decideAbort)
            .peek(this::auditSuccess)      // 成功副作用
            .peekError(this::auditFailure); // 失敗副作用
}
```

對人類讀者來說，這條線就是：

1. 讀入 payload
    
2. 查 user
    
3. 驗證規則
    
4. 算風險分數
    
5. 寫入
    
6. 判斷是否 abort
    
7. 審計
    

而不是埋在多層 if / try / return 裡的 spaghetti。

---

## 3.8 CH3 小結：Monad 是服務流程的「控制層」

本章重點可以濃縮為幾句話：

1. **Step 的型別**：  
    `StepContext<T> -> Validation<Violations, StepContext<T>>`  
    這讓所有流程步驟都可以被 Monad pipeline 組合。
    
2. **Monad 的職責**：
    
    - `map` / `flatMap`：連接與變換成功通道
        
    - `filter`：把條件轉成成功或錯誤
        
    - `peek` / `peekError`：掛載副作用不改變結果
        
    - `Try`：把技術例外轉進同一條錯誤通道
        
3. **錯誤模型**：
    
    - 業務錯誤 → `Violations`（搭配 `ViolationSeverity { INFO, WARNING, ERROR, FATAL, UNSPECIFIED }`）
        
    - 技術錯誤 → `Exception` → `Try` → `Validation<Violations, T>`
        
4. **整體效果**：
    
    - 流程變成一條可閱讀的線
        
    - 錯誤處理集中且型別一致
        
    - 副作用被抽離
        
    - 步驟可重組、可測試、可重用
        

接下來的 **CH4**，會在這個基礎上更進一步，專注在：

- `Validation` 的 Applicative 語意
    
- `Violations` 如何累積錯誤
    
- `ViolationSeverity` 如何影響流程判斷
    
- 如何設計「一次回報所有錯誤」與「逐步 fail-fast」的策略
    

---
