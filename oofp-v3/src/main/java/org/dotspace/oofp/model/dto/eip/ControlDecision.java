package org.dotspace.oofp.model.dto.eip;

import java.time.Instant;

public sealed interface ControlDecision permits
        ControlDecision.Ack, ControlDecision.Retry, ControlDecision.Dlq, ControlDecision.Noop,
        ControlDecision.FailInternal {

    record Ack() implements ControlDecision {}
    record Noop(String reason) implements ControlDecision {}          // 例如去重命中
    record Retry(Instant nextRetryAt, String reason) implements ControlDecision {}
    record Dlq(String reason) implements ControlDecision {}
    record FailInternal(String reason, Throwable cause) implements ControlDecision {}
}
