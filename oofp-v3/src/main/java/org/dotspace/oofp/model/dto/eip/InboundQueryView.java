package org.dotspace.oofp.model.dto.eip;

import java.time.Instant;
import java.util.Map;

public interface InboundQueryView<T> {
    InboundEnvelope.InboundSource getSource();
    String getSourceId();
    Map<String, String> getMeta();
    T getPayload();
    MessageClaims getClaims();   // 可為 null
    Instant getNow();
}
