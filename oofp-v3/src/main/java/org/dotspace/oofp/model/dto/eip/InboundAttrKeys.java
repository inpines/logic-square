package org.dotspace.oofp.model.dto.eip;

import com.fasterxml.jackson.core.type.TypeReference;

import org.dotspace.oofp.utils.eip.AttrKey;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

@UtilityClass
public final class InboundAttrKeys {

    /**
     * 原始 InboundEnvelope（完整保留，用於 trace / debug / audit）
     * ⚠️ 僅第一層 translator 寫入，其後只讀
     */
    public static final AttrKey<InboundEnvelope<?>> ENVELOPE =
            AttrKey.of("inbound.envelope", new TypeReference<>() {
            });

    /**
     * Inbound source 類型（HTTP / MQ / FILE）
     */
    public static final AttrKey<InboundEnvelope.InboundSource> SOURCE = AttrKey.of(
            "inbound.source", new TypeReference<>() {});

    /**
     * sourceId（URI / topic / file path）
     */
    public static final AttrKey<String> SOURCE_ID = AttrKey.of(
            "inbound.sourceId", new TypeReference<>() {});

    /**
     * 正規化後的 meta（已套白名單、rename、sanitize）
     */
    public static final AttrKey<Map<String, String>> META = AttrKey.of
            ("inbound.meta", new TypeReference<>() {});

    public static final AttrKey<MessageClaims> CLAIMS = AttrKey.of(
            "inbound.message.claims", new TypeReference<>() {});

    public static final AttrKey<QuerySpec> QUERY_SPEC = AttrKey.of(
            "interchange.query", new TypeReference<>() {});

    public static final AttrKey<MessageStatus> STATUS = AttrKey.of(
            "inbound.message.status", new TypeReference<>() {});

    public static final AttrKey<List<Failure>> FAILURES = AttrKey.of(
            "inbound.message.failures", new TypeReference<>() {});

    public static final AttrKey<ControlDecision> NEXT_DECISION = AttrKey.of(
            "inbound.message.control.next-decision", new TypeReference<>() {});
}
