package org.dotspace.oofp.model.dto.eip;

import java.time.Instant;

public record MessageClaims(
        String issuer,
        String subject,
        String clientId,
        Instant issuedAt,
        Instant expiration,
        String tokenId) {
}
