package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.model.dto.behaviorstep.Violations;

import lombok.experimental.UtilityClass;

import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public class InboundMetaNormalizer {

    public record NormalizedMeta(
            Map<String, String> meta,
            Violations violations) {}

    public NormalizedMeta normalize(Map<String, String> input, InboundMetaSchema schema) {
        Map<String, String> normalized = renameKeyThenNormalizeValue(input, schema);

        // 2) required check
        Violations vio = Violations.empty();
        for (String rk : schema.requiredKeys()) {
            if (!normalized.containsKey(rk)) {
                vio = vio.join(Violations.violate(
                        "meta.required.missing", "缺少必要 meta: " + rk));
            }
        }

        return new NormalizedMeta(Map.copyOf(normalized), vio);
    }

    private static Map<String, String> renameKeyThenNormalizeValue(Map<String, String> input, InboundMetaSchema schema) {
        Map<String, String> raw = (input == null) ? Map.of() : input;

        // 1) rename + normalize value
        Map<String, String> normalized = new LinkedHashMap<>();
        raw.forEach((k, v) -> {
            if (k == null) {
                return;
            }

            String standardKey = schema.renames().getOrDefault(k, k);
            if (!schema.allowedKeys().contains(standardKey)) {
                return;
            }

            String nv = schema.normalizeValue(standardKey, v);
            if (nv != null) {
                normalized.put(standardKey, nv);
            }
        });
        return normalized;
    }
}
