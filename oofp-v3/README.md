# oofp

**Object-Oriented Functional Programming**

This project explores how to model **functional-style behavior**
within **object-oriented structures**, with a strong emphasis on:

- explicit responsibilities
- composable behavior
- deliberate architectural boundaries

oofp is not a framework for convenience.
It is a collection of design models and DSLs for reasoning about behavior.

> For Chinese readers, see the reading guide in docs/.

---

## What this repository is about

This repository focuses on **how behavior is modeled and composed**,
not on providing ready-made application features.

Key concerns include:

- how processes are expressed as steps and pipelines
- how decisions are modeled as data instead of actions
- how inbound events are controlled without embedding side-effects
- how responsibilities are separated to prevent architectural erosion

---

## Architecture & Design Boundaries

Many design choices in this codebase are **intentional constraints**.

If you are reading the source code and wondering:

- why certain logic is *not* handled automatically
- why decisions are separated from execution
- why inbound handling does not directly run business flows
- why some abstractions feel stricter than typical frameworks

please read the architecture documentation first:

👉 **`docs/architecture/`**

In particular:
- conceptual architecture overview
- the relationship between behavior-pipeline and eip-inbound
- the separation of control plane and execution plane

These documents explain *why the code is shaped the way it is*.

---

## Documentation structure

- `docs/architecture/`  
  High-level design, boundaries, and system-wide reasoning

- `docs/behavior-pipeline/`  
  Process execution model and behavior composition

- `docs/eip-inbound/`  
  Inbound control plane and decision modeling

- `docs/write-operation-design/`  
  DSL design for write operations and builders

Other sections provide supporting motivation and notes.

---

## Design stance

This project optimizes for:

- clarity over convenience
- explicit decisions over implicit behavior
- separation of concerns over feature density

It is expected that some responsibilities feel *deliberately absent*.
Those absences are part of the design.

---

## Status

This repository is a living design space.
Concepts may evolve as boundaries are tested and refined.

---

# oofp

**物件導向風格的函數式程式設計（Object-Oriented Functional Programming）**

本專案探索如何在**物件導向結構中**建模**函數式風格的行為**，並特別強調：

- 明確的責任劃分
- 可組合的行為設計
- 刻意維持的架構邊界

oofp 並不是一個追求便利性的框架。  
它是一組用來**思考與推理行為如何被建模**的設計模型與 DSL。

---

## 這個專案在做什麼？

本專案關注的是**行為如何被建模與組合**，  
而不是提供現成可用的應用功能。

核心關注點包含：

- 流程如何以 step 與 pipeline 的形式被表達
- 為何將「決策」建模為資料，而非直接執行行為
- 外部事件如何在不嵌入 side-effect 的情況下被控制
- 如何透過責任分離，避免架構逐步侵蝕

---

## 架構與設計邊界

此專案中的許多設計選擇，都是**刻意的限制**。

如果你在閱讀原始碼時，心中浮現這些疑問：

- 為什麼有些事情沒有被自動處理？
- 為什麼決策要和執行分開？
- 為什麼 Inbound 處理不直接跑業務流程？
- 為什麼有些抽象看起來比一般框架更嚴格？

請先閱讀架構說明文件：

👉 **`docs/architecture/`**

特別是以下內容：

- 整體概念架構說明
- behavior-pipeline 與 eip-inbound 之間的關係
- control plane 與 execution plane 的分離設計

這些文件說明的是：**為什麼程式碼會長成現在這個樣子**。

---

## 文件結構說明

- `docs/architecture/`  
  高層次設計、架構邊界與系統整體思考

- `docs/behavior-pipeline/`  
  流程執行模型與行為組合方式

- `docs/eip-inbound/`  
  Inbound 控制平面與決策模型

- `docs/write-operation-design/`  
  寫入行為與 builder DSL 的設計說明

其他章節則提供動機說明與補充筆記。

---

## 設計立場

本專案刻意優化以下價值：

- 清楚勝過方便
- 顯式決策勝過隱性行為
- 責任分離勝過功能堆疊

某些責任「刻意缺席」是預期行為，  
而非設計疏漏。

---

## 專案狀態

此專案是一個持續演進的設計空間。  
相關概念可能會隨著邊界驗證與實務使用而調整。
