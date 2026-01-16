package org.dotspace.oofp.model.dto.eip;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface InboundDecisionView {
    Map<String, String> getMeta();
    MessageStatus getCurrentStatus();
    List<Failure> getFailures();
    MessageClaims getClaims();   // policy 常需要看是否匿名 / tenant
    Instant getNow();

    InboundEnvelope.InboundSource getSource();
    String getSourceId();
}
