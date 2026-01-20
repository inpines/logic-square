

# 📘 `CH2 — Context：唯一資料流模型`

完整技術版

---

# 2. Context：服務資料流的唯一來源

---

# 2.1 為什麼 Context 是不可或缺的？

在一個服務流程中，若：

- 有資料來回在多個方法間傳遞
    
- 有中間結果需要在後面步驟使用
    
- 有錯誤資訊需要共享
    
- 有副作用需要記錄（audit/log/trace）
    
- 有配置值、旗標、判斷結果需要保留
    

那麼你就需要一個「不會散落、可統一管理」的資料載體。

傳統作法通常使用：

- 多個 local variables
    
- 在方法之間傳 object
    
- 把 context 塞在 ThreadLocal
    
- 用 DTO 或 Map 四處傳遞
    

然而這些方式會造成：

- **狀態破碎（state fragmentation）**
    
- **流程不可預測（hidden flow）**
    
- **方法間高度耦合（high coupling）**
    
- **測試成本高（hard to mock / hard to assert）**
    
- **service class 容易變成 1000 行以上的巨物（god method）**
    

因此你的架構採取一個核心原則：

> **服務層中，資料流與狀態流必須統一由 Context 承載。**

---

# 2.2 Context 的結構設計

你的 Context 是一個 **可擴展資料容器**，包含三類資訊：

1. **payload（原始輸入資料）**
    
2. **attrs（步驟之間共享的中間狀態）**
    
3. **flags（控制流程的語意，例如 aborted）**
    

以下是典型結構：

```java
public class ServiceContext<T> {

    private T payload;                       // 原始輸入
    private Map<String, Object> attrs = new HashMap<>(); 
    private boolean aborted;                 // 控制流程是否中止

    public T getPayload() { return payload; }

    public <A> A get(String key, Class<A> type) {
        return type.cast(attrs.get(key));
    }

    public void set(String key, Object val) {
        attrs.put(key, val);
    }

    public void abort() {
        this.aborted = true;
    }

    public boolean isAborted() {
        return aborted;
    }
}
```

這是一個刻意被保持為「低魔法（low-magic）」、「低認知負擔」的抽象。

---

# 2.3 為什麼 Context 必須是「唯一資料流」？

## 理由 1：避免泛型地獄（P、R 在每一層傳遞）

若每個步驟都接受不同的輸入 P 和輸出 R：

```java
P1 -> R1
R1 -> R2
R2 -> R3
```

你會需要一連串：

- type parameter
    
- adapter method
    
- 轉換 DTO
    

而 Context 模型將此全部歸一化：

```
Context<T> -> Context<T>
```

你只需要關心 Context 用起來怎樣，不需要關心泛型繁殖。

---

## 理由 2：步驟之間可以共享資料（attr space）

Context 的 attrs 是**共享記憶空間**，提供 step 之間傳遞資料：

例：

```java
ctx.set("user", user);
ctx.set("quota", quota);
ctx.set("riskScore", score);
```

後面的步驟只要：

```java
User user = ctx.get("user", User.class);
```

不需要額外傳參數，也不需要 DTO nesting。

---

## 理由 3：Context 允許流程語意（abort / continue）具體化

Context 可標記：

```java
ctx.abort();
```

Pipeline 可以依據 aborted flag 決定：

- 是否短路
    
- 是否執行副作用
    
- 是否進入 fallback path
    

這讓流程控制成為語意，而不是 if/else。

---

## 理由 4：Context 是 Monad pipeline 與行為步驟（Step）的橋樑

所有 Step 都接受 Context，並返回 Context：

```
Context → Validation<Violations, Context>
```

這讓：

- 所有步驟的型別統一
    
- ServiceChain 不需要知道步驟內部結構
    
- 每個步驟都可以作為積木被重組
    

換句話說：

> Context 是 Flow（Monad chain）與 Step（行為）之間的通用介面。

---

# 2.4 Context 的六種語意角色（語意層）

Context 不只是裝資料的 Map，它對整個責任鏈提供六種語意能力：

## **1. Input Carrier（輸入載體）**

所有 pipeline 的起點來自 payload，  
例如：

```java
new ServiceContext<>(payload)
```

---

## **2. State Registry（流程狀態表）**

多個步驟會進行多項查詢、轉換、判斷，  
Context 的 attrs 就是共享狀態空間。

---

## **3. Flow Controller（流程控制器）**

aborted flag 是 pipeline 的流程語意：

- abort but not error
    
- error but not abort
    
- abort and error both true
    

這三者在日常服務流程非常重要（例如 audit、metrics 等仍需執行）。

---

## **4. Side-effect Space（副作用環境）**

副作用不應亂寫在主流程，  
所以 Step 通常使用 Context 由 pipeline 傳遞：

- traceId
    
- spanId
    
- audit metadata
    
- operator
    
- requestTime
    

---

## **5. Domain Event Staging（領域事件暫存區）**

有時你不想在 Step 內直接送事件，  
可以先放在 Context，最後統一處理：

```java
ctx.set("events", events);
```

---

## **6. Output Aggregator（輸出組裝器）**

最終結果不一定來自 payload，  
Context 可以在流程尾端彙整結果：

```java
result = ctx.get("finalValue", Result.class);
```

---

# 2.5 流程示例：Context 實際運作方式

下面是一個典型的 pipeline：

```java
return ServiceChain.run(
    ctx,
    this::loadUser,      // user -> ctx["user"]
    this::checkQuota,    // quota -> ctx["quota"]
    this::calcRiskScore, // score -> ctx["riskScore"]
    this::writeRecord,   // final write
    this::audit          // side effect
);
```

對應的步驟可能是：

```java
public Validation<Violations, ServiceContext> loadUser(ServiceContext ctx) {
    return Maybe.given(ctx.getPayload())
            .map(repo::findUser)
            .peek(user -> ctx.set("user", user))
            .toValidation(Violations::missingUser)
            .map(ok -> ctx);
}
```

Context：

- 接收 payload（userId）
    
- 存 user
    
- 存 quota
    
- 存 score
    
- audit step 最後依據這些資料寫入審計系統
    

**你可以看到：Context 成功讓所有流程資料在統一空間穿透。**

---

# 2.6 Context 的「可擴展性」與「限制」設計原則

Context 的設計刻意保持：

- **不做過度抽象**（不定義過多 method）
    
- **不做魔法解析**（不自動將資料綁定成欄位）
    
- **只擔任資料載體與狀態功能**
    

這是為了：

> 維持「可觀察、可推論、可維護」的架構特性。

## Context MUST：

- 只承載資料，不執行業務邏輯
    
- 保持簡單，不內嵌流程
    
- 支援自由擴展 attrs
    
- 讓任何 Step 都可讀/寫
    

## Context SHOULD：

- 明確命名 attr key
    
- 避免塞過度複雜結構（尤其是深巢狀 Map）
    
- 避免做為「垃圾桶物件」
    

## Context MUST NOT：

- 實作過度繁瑣的存取 API
    
- 隱藏流程邏輯（例如自動 abort）
    
- 理解 step 的語意（context 必須無知，pipeline 來控制 flow）
    

---

# 2.7 Context 與 FP Monad 的契合

Context 解決的是「資料保存與傳遞」。  
Monad 解決的是「流程控制與錯誤」。

兩者契合點是：

> Step 的輸入與輸出永遠是 Context，因此 Monad 可以自然包住它。

這是整個模型的核心力量：

- pipeline 可以純粹地組合 Step
    
- Step 不需要知道 pipeline 的存在
    
- Context 提供資料一致性
    
- Monad 提供錯誤一致性
    

---

# 2.8 `CH2 小結：Context 作為整個架構的「物理層」`

Context 是：

- **資料載體（data plane）**
    
- **狀態載體（state plane）**
    
- **流程控制旗標（control plane）**
    
- **副作用協調層（side-effect plane）**
    
- **事件暫存區（event buffer）**
    

正因為 Context 擁有這些能力，  
你才可以在 `CH3` 之後使用：

- `Step 語意`
    
- `Validation pipeline`
    
- `ExpressionOperation`
    
- `BehaviorPipeline`
    

來建構一個真正可組合、可維護、可配置的服務系統。

---

## 2.9 `StepContext<T>：Context 的正式實作形態`

在實作層，我們使用 `StepContext<T>` 作為「Context」的具體型別：

```java
@Getter
@Builder(setterPrefix = "with")
public class StepContext<T> {

    // 1. 核心資料（主資料流）
    private T payload;

    // 2. 錯誤通道（可累積的錯誤集合）
    private Violations violations;

    // 3. 彈性附加資料（步驟共享的狀態空間）
    @Getter(lombok.AccessLevel.NONE)
    @Builder.Default
    private final Map<String, Object> attributes = new HashMap<>();

    // 4. 流程控制旗標（是否中止後續步驟）
    @Builder.Default
    @Setter
    private boolean aborted = false;

    // —— 以下為語意方法 —— //

    // 將 payload 過渡到新值，沿用既有錯誤與屬性
    public StepContext<T> transit(T newPayload) {
        return StepContext.<T>builder()
                .withPayload(newPayload)
                .withViolations(violations)
                .withAttributes(attributes)
                .withAborted(aborted)
                .build();
    }

    // 累積單一錯誤：以 join 語意加入 Violations
    public StepContext<T> addViolation(GeneralViolation violation) {
        return StepContext.<T>builder()
                .withPayload(payload)
                .withViolations(violations.join(Violations.from(List.of(violation))))
                .withAttributes(attributes)
                .withAborted(aborted)
                .build();
    }

    // 將一組 Violations 合併入現有錯誤集合
    public Violations withViolation(Violations violations) {
        return this.violations.join(violations);
    }

    // 錯誤嚴重度判斷
    public boolean hasFatalErrors() {
        return violations.stream()
                .anyMatch(v -> v.getSeverity() == ViolationSeverity.FATAL);
    }

    public boolean hasSevereThan(ViolationSeverity level) {
        return violations.stream()
                .anyMatch(v -> v.getSeverity().ordinal() >= level.ordinal());
    }

    // 取用屬性並套用轉換
    public <R> R getAttribute(String name, Function<Object, R> applier) {
        return Maybe.given(attributes.get(name))
                .map(applier)
                .orElse(null);
    }

    // 設定單一屬性（就地更新）
    public StepContext<T> withAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public StepContext<T> withNoneAttribute(String key) {
        attributes.remove(key);
        return this;
    }

    // 設定多個屬性（建立新的 StepContext）
    public StepContext<T> withAttributes(Map<String, Object> additional) {
        Map<String, Object> merged = new HashMap<>(attributes);
        merged.putAll(additional);
        return StepContext.<T>builder()
                .withPayload(payload)
                .withViolations(violations)
                .withAttributes(merged)
                .withAborted(aborted)
                .build();
    }
}
```

這個 `StepContext<T>` 對應到本章前面提到的 Context 語意，並且具備：

1. **Payload 語意**：`payload` 是整條 pipeline 的主資料流。
    
2. **錯誤累積語意**：`violations` 透過 `join` 保留「所有」錯誤。
    
3. **屬性空間語意**：`attributes` 讓步驟之間可以交換中間結果。
    
4. **流程控制語意**：`aborted` 表示是否中止後續步驟。
    
5. **錯誤嚴重度語意**：`hasFatalErrors` / `hasSevereThan` 提供決策所需的判斷。
    
6. **半不可變設計**：像 `transit` / `addViolation` / `withAttributes` 會建立新物件，  
    而 `withAttribute` / `withNoneAttribute` 則就地更新，兼顧效能與可讀性。
    

後續章節中提到的「Context」，皆以 `StepContext<T>` 為正式實作。