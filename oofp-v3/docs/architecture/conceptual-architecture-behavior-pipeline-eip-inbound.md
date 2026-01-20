# `Mermaid：Behavior Pipeline × eip-inbound（Conceptual Architecture）`

```mermaid
flowchart TD
    EXT[External Event]
    ENV[InboundEnvelope]
    GATE[StatefulGate]
    DEC[ControlDecision]
    PIPE[BehaviorPipeline]
    CTX[StepContext]
    RUN[Runtime Handler]

    EXT --> ENV
    ENV --> GATE
    GATE --> PIPE
    PIPE --> CTX
    GATE --> DEC
    DEC --> RUN

```


```mermaid
flowchart TD
    subgraph CP[Control Plane]
        ENV[Inbound<br/>Envelope]
        GATE[StatefulGate]
        DEC[Control<br/>Decision]
    end

    subgraph EP[Execution Plane]
        PIPE[Behavior<br/>Pipeline]
        CTX[StepContext]
        HANDLER[Runtime <br/>Handler]
    end

    ENV --> GATE
    GATE --> DEC
    GATE --> PIPE
    PIPE --> CTX
    DEC --> HANDLER

```

## 圖例說明

```text
External World
  └─ 不可信的外部事件來源

eip-inbound (Inbound Control Plane)
  ├─ InboundEnvelope：統一輸入語意
  ├─ InboundFlow：stateless 進場流程
  ├─ StatefulGate：stateful concern 聚合
  └─ ControlDecision：顯式處置結果

behavior-pipeline (Process Execution Language)
  ├─ 定義流程如何執行
  ├─ StepContext 演進
  └─ 錯誤累積與中斷策略

Runtime / Handler
  └─ 根據 ControlDecision 執行 side-effect
```
