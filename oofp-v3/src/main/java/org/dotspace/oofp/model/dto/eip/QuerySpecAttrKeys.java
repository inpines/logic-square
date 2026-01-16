package org.dotspace.oofp.model.dto.eip;

import com.fasterxml.jackson.core.type.TypeReference;
import org.dotspace.oofp.utils.eip.AttrKey;
import lombok.experimental.UtilityClass;

import java.time.Instant;

@UtilityClass
public class QuerySpecAttrKeys {

    public static final AttrKey<String> TENANT_ID = AttrKey.of("query.tenantId", new TypeReference<>() {});

    public static final AttrKey<String> MESSAGE_ID = AttrKey.of("query.messageId", new TypeReference<>() {});

    public static final AttrKey<Instant> SINCE = AttrKey.of("query.since", new TypeReference<>() {});

    public static final AttrKey<String> STATUS = AttrKey.of("query.status", new TypeReference<>() {});

}
