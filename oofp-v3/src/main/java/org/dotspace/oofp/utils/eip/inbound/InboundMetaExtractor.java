package org.dotspace.oofp.utils.eip.inbound;

import java.util.Map;

@FunctionalInterface
public interface InboundMetaExtractor<T> {
    Map<String, String> extract(T headerSource);
}
