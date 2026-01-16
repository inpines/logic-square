package org.dotspace.oofp.model.dto.eip;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InboundMetaKeys {
    // core tracing
    public static final String TRACE_ID = "traceId";
    public static final String CORRELATION_ID = "correlationId";

    // request info
    public static final String SOURCE_IP = "source.ip";
    public static final String USER_AGENT = "http.userAgent";
    public static final String REQUEST_ID = "http.requestId"; // X-Request-Id

    // -------------------------
    // mqtt transport facts
    // -------------------------

    // mqtt core
    public static final String MQTT_CLIENT_ID = "mqtt.clientId";
    public static final String MQTT_QOS = "mqtt.qos";
    public static final String MQTT_RETAIN = "mqtt.retain";
    public static final String MQTT_DUP = "mqtt.dup";

    public static final String MQTT_PACKET_ID = "mqtt.packetId";

    public static final String MQTT_TOPIC = "mqtt.topic"; // 若你不想只靠 sourceId
    public static final String MQTT_CONTENT_TYPE = "mqtt.contentType"; // MQTT v5

}
