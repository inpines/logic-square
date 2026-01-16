package org.dotspace.oofp.model.dto.eip;

import org.dotspace.oofp.utils.functional.monad.Maybe;

import java.util.Map;

public final class InboundMetaView {

    private final Map<String, String> meta;

    private InboundMetaView(Map<String, String> meta) {
        this.meta = meta == null ? Map.of() : meta;
    }

    public static InboundMetaView of(Map<String, String> meta) {
        return new InboundMetaView(meta);
    }

    public Maybe<String> traceId() {
        return Maybe.given(meta.get(InboundMetaKeys.TRACE_ID));
    }

    public Maybe<String> correlationId() {
        return Maybe.given(meta.get(InboundMetaKeys.CORRELATION_ID));
    }

    public Maybe<String> sourceIp() {
        return Maybe.given(meta.get(InboundMetaKeys.SOURCE_IP));
    }

    public Maybe<String> userAgent() {
        return Maybe.given(meta.get(InboundMetaKeys.USER_AGENT));
    }

    // 刻意不提供 tenant / subject / clientId => from (query spec & message claims)

    public Maybe<String> mqttClientId() {
        return Maybe.given(meta.get(InboundMetaKeys.MQTT_CLIENT_ID));
    }

    public Maybe<Integer> mqttQos() {
        return Maybe.given(meta.get(InboundMetaKeys.MQTT_QOS)).map(Integer::parseInt);
    }

    public Maybe<Boolean> mqttRetain() {
        return Maybe.given(meta.get(InboundMetaKeys.MQTT_RETAIN)).map(Boolean::parseBoolean);
    }

}
