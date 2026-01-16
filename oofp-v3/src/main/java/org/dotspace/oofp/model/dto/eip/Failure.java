package org.dotspace.oofp.model.dto.eip;

import org.dotspace.oofp.enumeration.eip.ErrorTaxonomy;

public record Failure(
        ErrorTaxonomy taxonomy,
        String code,                 // 例如 "claims.expired", "schema.unsupported"
        String message,              // 人類可讀
        Throwable cause              // 可為 null
) {

    public static Failure of(ErrorTaxonomy tax, String code, String message) {
        return new Failure(tax, code, message, null);
    }

    public static Failure of(ErrorTaxonomy tax, String code, String message, Throwable cause) {
        return new Failure(tax, code, message, cause);
    }

}
