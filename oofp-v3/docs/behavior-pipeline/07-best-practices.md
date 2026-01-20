# Best Practices（設計規約與實務建議）

本章整理在實際使用 Behavior Step / Pipeline 時，經驗上最穩定、可維護的設計規約。
這些規約不是語法限制，而是**避免流程腐化的結構性建議**。

---

## 1. Step 設計規約

### 1.1 單一責任原則（SRP）

一個 Step 應該只負責**一件清楚可描述的行為**：

- 一個檢核
- 一個轉換
- 一個決策

如果 Step 名稱需要「and / or」連接，通常代表責任過多。

---

### 1.2 Step 要可獨立理解

理想的 Step 應具備以下特性：

- 不假設前後 Step 的存在
- 行為可由輸入 Context 推論
- 不依賴隱性初始化狀態

這讓 Step 能被：
- 重用
- 單獨測試
- 安全重排

---

### 1.3 避免在 Step 中組裝流程

Step 不應：

- 呼叫 Pipeline
- 動態加入其他 Step
- 根據執行結果改變整體流程結構

流程結構應在 Pipeline 組裝階段決定。

---

## 2. Pipeline 使用規約

### 2.1 Pipeline 是流程，不是容器

Pipeline 不應被當作：

- 業務邏輯集合
- Rule Engine
- 服務層替代品

它的角色是**流程控制與執行**。

---

### 2.2 一條 Pipeline，一種策略

同一條 Pipeline 中應固定使用：

- Fail-Fast（apply）
  **或**
- Error-Accumulating（applyCorrectErrors）

混用策略會讓流程行為難以預測。

---

### 2.3 Pipeline 組裝應靠近入口

Pipeline 的組裝位置應：

- 靠近 use case 入口
- 明確反映流程設計意圖
- 避免散落在深層方法中

---

## 3. Context 使用規約

### 3.1 payload 是主線資料

- 核心業務資料應放在 payload
- payload 的型別應穩定
- 不應用 attributes 長期取代 payload

---

### 3.2 attributes 是輔助，不是垃圾桶

attributes 適合：

- 中間計算結果
- 跨 Step 旗標
- 非 domain 的暫存資料

不適合：

- 長期業務狀態
- 關鍵流程判斷依據
- 不具語意的暫存值

---

### 3.3 attributes key 要有語意

避免：

```text
"tmp"
"data1"
"flag"
```

建議：

```text
"validatedUser"
"pricingContext"
"isEligible"
```

---

## 4. 錯誤處理規約

### 4.1 錯誤即資料

- 優先使用 Violations 表達錯誤
    
- Exception 僅用於系統層錯誤
    
- 不以 Exception 控制正常流程
    

---

### 4.2 不在 Step 中做全局錯誤決策

Step 不應：

- 根據 severity 中斷流程
    
- 決定最終錯誤輸出格式
    
- 隱性忽略錯誤
    

錯誤決策應集中於 Pipeline 結尾。

---

### 4.3 適度使用 recover

recover 是：

- 高風險工具
    
- 最後手段
    

使用 recover 前，應確認：

- 是否能用正常 Step 流程設計
    
- recovery 是否具備明確語意
    
- recovery 失敗是否被正確處理
    

---

## 5. aborted 使用規約

### 5.1 aborted 只表示「停止」

aborted 應表示：

- 流程語意結束
    
- 後續步驟不再適用
    

不應用 aborted 表示錯誤。

---

### 5.2 aborted 應由 Step 明確設定

避免：

- Pipeline 自動推斷 aborted
    
- 根據錯誤層級隱性 aborted
    

中斷應是顯式設計。

---

## 6. 可讀性與命名

### 6.1 Step 命名反映行為

Step 名稱應描述：

- 發生什麼事
    
- 而不是怎麼實作
    

例如：

- `validateUserStatus`
    
- `calculatePricing`
    
- `checkEligibility`
    

---

### 6.2 Pipeline 本身應可閱讀

理想的 Pipeline 組裝：

`validate → enrich → decide → finalize`

而不是：

`step1 → step2 → step3`

---

## 7. 測試與演進建議

### 7.1 Step 應可單獨測試

每個 Step 應能：

- 獨立給定 Context
    
- 驗證輸出 Context / Violations
    

---

### 7.2 Pipeline 測試重點在策略

Pipeline 測試應關注：

- 中斷時機
    
- 錯誤累積行為
    
- 結果輸出是否符合策略
    

---

## 8. 本章小結

- **Step 要小、清楚、可獨立**
    
- **Pipeline 控制流程，不承載邏輯**
    
- **Context 是流程語意的核心**
    
- **錯誤延後決策，集中處理**
    
- **可讀性優先於技巧**
    

---

> **最好的 `DSL`，是讓錯誤用法變得困難。**

下一章將列出常見反模式，說明哪些用法應避免。