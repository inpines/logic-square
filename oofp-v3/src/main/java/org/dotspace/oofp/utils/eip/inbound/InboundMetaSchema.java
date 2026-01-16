package org.dotspace.oofp.utils.eip.inbound;

import java.util.Map;
import java.util.Set;

public interface InboundMetaSchema {
    /** 外部 key -> 內部標準 key（若不在 map 內，表示不接受或不改名） */
    Map<String, String> renames();

    /** 允許進入系統的標準 key */
    Set<String> allowedKeys();

    /** 必要 key（缺少就 invalid） */
    Set<String> requiredKeys();

    /** value 清洗（trim / 空字串 -> null / 長度限制...） */
    default String normalizeValue(String key, String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        if (v.isEmpty()) {
            return null;
        }
        // 可選：防止爆量 header 汙染
        if (v.length() > 2048) {
            v = v.substring(0, 2048);
        }

        return v;
    }
}
