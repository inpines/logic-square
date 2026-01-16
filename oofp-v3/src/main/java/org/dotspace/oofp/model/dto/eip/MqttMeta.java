package org.dotspace.oofp.model.dto.eip;

import org.dotspace.oofp.utils.functional.monad.Maybe;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@UtilityClass
public class MqttMeta {

    public static Map<String, String> normalize(Map<String, String> raw) {
        if (raw == null || raw.isEmpty()) return Map.of();

        var out = new java.util.LinkedHashMap<String, String>();

        // 白名單挑你要的
        putIfPresent(out, "mqtt.clientId", raw.get("clientId"));
        putIfPresent(out, "mqtt.qos", raw.get("qos"));
        putIfPresent(out, "mqtt.retain", raw.get("retain"));
        putIfPresent(out, "mqtt.dup", raw.get("dup"));
        putIfPresent(out, "mqtt.packetId", raw.get("packetId"));

        // trace/correlation 若你 MQTT user properties 會帶
        putIfPresent(out, InboundMetaKeys.TRACE_ID, raw.get(InboundMetaKeys.TRACE_ID));
        putIfPresent(out, InboundMetaKeys.CORRELATION_ID, raw.get(InboundMetaKeys.CORRELATION_ID));

        return Map.copyOf(out);
    }

    private static void putIfPresent(@NonNull Map<String, String> out, @NonNull String key, String value) {
        Maybe.given(value)
                .filter(StringUtils::isNotBlank)
                .match(v -> out.put(key, v));
    }
}
