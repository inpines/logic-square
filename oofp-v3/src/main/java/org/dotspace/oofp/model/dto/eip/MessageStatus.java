package org.dotspace.oofp.model.dto.eip;

import org.dotspace.oofp.enumeration.eip.MessageStatusType;

import java.time.Instant;

public record MessageStatus(
        MessageStatusType status,
        int attempt,
        Instant nextRetryAt,
        Instant lastUpdatedAt,
        String description) {

    public static MessageStatus unknown(String reason, Instant now) {
        return new MessageStatus(
                MessageStatusType.UNKNOWN,
                0,
                null,
                now,
                reason
        );
    }

}
