package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.model.dto.eip.InboundMetaHeaders;
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
                        InboundMetaHeaders.REQUEST_ID, InboundMetaKeys.REQUEST_ID,
                        InboundMetaHeaders.USER_AGENT, InboundMetaKeys.USER_AGENT,
                        InboundMetaHeaders.FORWARDED_FOR, InboundMetaKeys.SOURCE_IP
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
