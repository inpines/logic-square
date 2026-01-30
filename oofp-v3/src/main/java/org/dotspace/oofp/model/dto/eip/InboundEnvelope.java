package org.dotspace.oofp.model.dto.eip;

import java.util.Map;

public record InboundEnvelope<T>(
        InboundSource source,
        String sourceId,                     // topic / queue / endpoint / folder
        Map<String, String> meta,
        T payload,                  // byte[] / String / JsonNode / File / POJO
        java.time.Instant receivedAt) {

    public enum InboundSource {
        HTTP, MQTT, MQ, FILE, SCHEDULED, OTHER, EMPTY_HTTP_CONTEXT
    }

    public boolean hasPayload() {
        return payload != null;
    }

}
