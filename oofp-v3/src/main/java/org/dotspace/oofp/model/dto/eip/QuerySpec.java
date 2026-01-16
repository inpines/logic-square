package org.dotspace.oofp.model.dto.eip;

public record QuerySpec(
        String type,                 // "db" | "http" | "config"...
        String name,                 // 白名單 key
        QuerySpecParams params,  // 純值
        String intoAttr              // 要寫回哪個 attribute)
) {
}