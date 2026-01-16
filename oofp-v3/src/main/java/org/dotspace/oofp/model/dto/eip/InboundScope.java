package org.dotspace.oofp.model.dto.eip;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class InboundScope<T> implements InboundQueryView<T>, InboundDecisionView {
    // Envelope / inbound context
    private InboundEnvelope.InboundSource source;
    private String sourceId;

    private Map<String, String> meta;
    private T payload;

    // Auth
    private MessageClaims claims; // 允許 null

    // store context（給 policy 用）
    private MessageStatus currentStatus;

    @Singular("failure")
    private List<Failure> failures;

    // Time
    private Instant now;
}
