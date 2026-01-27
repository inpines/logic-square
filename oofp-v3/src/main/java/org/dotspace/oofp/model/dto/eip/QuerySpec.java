package org.dotspace.oofp.model.dto.eip;

import org.dotspace.oofp.utils.eip.AttrKey;

public record QuerySpec(
        String type, // "db" | "http" | "config"...
        String name, // 白名單 key
        QuerySpecParams params,  // 純值
        AttrKey<QuerySpecParams> intoAttr // 要寫回哪個 attribute
) {
}