package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.model.dto.eip.InboundMetaKeys;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Set;

@UtilityClass
public class InboundMetaSchemas {

    public static InboundMetaSchema http() {
        return new InboundMetaSchema() {
            @Override public Map<String, String> renames() {
                return Map.of(
                        "X-Request-Id", InboundMetaKeys.REQUEST_ID,
                        "User-Agent", InboundMetaKeys.USER_AGENT,
                        "X-Forwarded-For", InboundMetaKeys.SOURCE_IP
                );
            }

            @Override public Set<String> allowedKeys() {
                return Set.of(
                        InboundMetaKeys.TRACE_ID,
                        InboundMetaKeys.CORRELATION_ID,
                        InboundMetaKeys.REQUEST_ID,
                        InboundMetaKeys.USER_AGENT,
                        InboundMetaKeys.SOURCE_IP
                );
            }

            @Override public Set<String> requiredKeys() {
                // 看你要不要強制 tenant/trace
                return Set.of(InboundMetaKeys.TRACE_ID);
            }
        };
    }

    public InboundMetaSchema mqtt() {
        return new InboundMetaSchema() {
            @Override public Map<String, String> renames() { return Map.of(); }

            @Override public Set<String> allowedKeys() {
                return Set.of(
                        InboundMetaKeys.TRACE_ID,
                        InboundMetaKeys.CORRELATION_ID
                );
            }

            @Override public Set<String> requiredKeys() {
                return Set.of(InboundMetaKeys.TRACE_ID);
            }
        };
    }

    public InboundMetaSchema mq() {
        return new InboundMetaSchema() {
            @Override public Map<String, String> renames() { return Map.of(); }

            @Override public Set<String> allowedKeys() {
                return Set.of(
                        InboundMetaKeys.TRACE_ID,
                        InboundMetaKeys.CORRELATION_ID
                );
            }

            @Override public Set<String> requiredKeys() {
                return Set.of(InboundMetaKeys.TRACE_ID);
            }
        };
    }

    public InboundMetaSchema empty() {
        return new InboundMetaSchema() {
            @Override
            public Map<String, String> renames() {
                return Map.of();
            }

            @Override
            public Set<String> allowedKeys() {
                return Set.of();
            }

            @Override
            public Set<String> requiredKeys() {
                return Set.of();
            }
        };
    }
}
