# Anti-Patterns（常見反模式）

本章列出在使用 Behavior Step / Pipeline 時，實務上最容易出現、也最具破壞性的反模式。
這些反模式通常不是一開始就明顯錯誤，而是隨著需求累積逐步侵蝕架構。

---

## 1. 巨型 Step（God Step）

### 現象

- 一個 Step 包含大量 if / else
- 同時進行多個檢核、轉換與決策
- Step 名稱模糊（如 `process`, `handle`, `executeAll`）

### 為什麼有問題？

- 無法單獨測試
- 難以重用
- 流程語意不清

### 改善方式

- 拆成多個單一責任 Step
- 讓 Pipeline 承擔組合責任
- 以流程順序表達邏輯，而非巢狀條件

---

## 2. 在 Step 中組裝 Pipeline

### 現象

- Step 內部 new 一個 Pipeline
- Step 動態加入其他 Step
- Step 根據結果改變流程結構

### 為什麼有問題？

- 破壞單向依賴
- 流程結構變得不可預測
- 測試與推理困難

### 改善方式

- Pipeline 組裝應集中在 use case 入口
- Step 僅描述行為，不描述流程

---

## 3. 把 aborted 當成錯誤

### 現象

- aborted 被用來表示失敗
- aborted 與 invalid 混用
- aborted 後仍期待錯誤輸出

### 為什麼有問題？

- 流程語意混亂
- 呼叫端難以判斷結果狀態

### 改善方式

- invalid 表示「有問題」
- aborted 表示「停止」
- 兩者語意必須分離

---

## 4. 用 severity 偷偷控制流程

### 現象

- severe 就中斷流程
- warning 就忽略後續錯誤
- fatal 自動丟 Exception

### 為什麼有問題？

- 流程控制變成隱性規則
- 不同 Step 行為不一致
- Pipeline 策略被破壞

### 改善方式

- severity 僅提供資訊
- 中斷必須是顯式設計
- 流程策略集中在 Pipeline

---

## 5. 用 Exception 取代 Violations

### 現象

- 業務檢核直接丟 Exception
- Exception 被當成正常流程控制
- 錯誤無法被累積

### 為什麼有問題？

- 無法一次回報多個錯誤
- 流程狀態被破壞
- Pipeline 策略失效

### 改善方式

- 可預期錯誤 → Violations
- 系統錯誤 → Exception
- 保持錯誤語意一致

---

## 6. attributes 當成全域變數

### 現象

- attributes 塞滿不具語意的資料
- 多個 Step 讀寫同一 key
- key 命名混亂

### 為什麼有問題？

- 隱性耦合
- 難以追蹤資料來源
- Context 失去可讀性

### 改善方式

- attributes 僅存輔助資料
- key 命名具語意
- 核心資料回到 payload

---

## 7. 在流程中刪除錯誤

### 現象

- 中途清空 violations
- 根據條件移除部分錯誤
- 錯誤被「修掉」而非回報

### 為什麼有問題？

- 錯誤資訊遺失
- 決策依據不完整
- 流程結果不可信

### 改善方式

- 錯誤一旦產生即保留
- 過濾僅發生在流程結尾
- 保留完整錯誤脈絡

---

## 8. 濫用 recover 掩蓋設計問題

### 現象

- recover 成為預設流程
- 用 recovery 補救缺失的設計
- recovery 失敗未被妥善處理

### 為什麼有問題？

- 隱藏真正的流程問題
- 增加不可預期行為
- 測試困難

### 改善方式

- 先修正流程設計
- recover 僅用於少數例外情境
- recovery 必須具備清楚語意

---

## 9. Pipeline 可讀性崩壞

### 現象

- Pipeline 超過十多個 Step
- Step 名稱無法形成語意流程
- 需要讀 Step 實作才能理解流程

### 為什麼有問題？

- 流程無法被快速理解
- 設計意圖不清楚
- 維護成本高

### 改善方式

- 將子流程包裝為 composite Step
- 以語意順序組裝 Pipeline
- 保持 Pipeline 為「高層描述」

---

## 10. 本章小結

- 反模式多半來自「圖方便」
- 問題通常不是語法，而是責任錯置
- 一旦流程語意模糊，DSL 價值即開始流失

---

> **好的架構不是靠避免錯誤，而是讓錯誤難以發生。**

最後一章將簡述這套設計的演進方向與可能擴充。
