package org.dotspace.oofp.utils.eip;

import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.model.dto.eip.*;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@UtilityClass
public class InboundScopes {

    public <T> InboundScope<T> from(StepContext<T> stepContext) {
        Maybe<InboundEnvelope<?>> envelope = InboundAttrKeys.ENVELOPE.maybe(stepContext);

        InboundEnvelope.InboundSource source = InboundAttrKeys.SOURCE.getOrElse(
                stepContext, envelope.map(InboundEnvelope::source).orElse(null));

        String sourceId = InboundAttrKeys.SOURCE_ID.getOrElse(
                stepContext, envelope.map(InboundEnvelope::sourceId).orElse(null));

        Map<String, String> meta = InboundAttrKeys.META.getOrElse(stepContext, Map.of());
        meta = (meta == null) ? Map.of() : Map.copyOf(meta);

        MessageClaims claims = InboundAttrKeys.CLAIMS.getOrElse(stepContext, null);

        // 下面這二個（status/failures）你可能還沒 binding，先用 AttrKey 取，沒有就 null/empty

        MessageStatus currentStatus = InboundAttrKeys.STATUS.getOrElse(stepContext, null);

        List<Failure> failures = InboundAttrKeys.FAILURES.getOrElse(stepContext, List.of());
        failures = (failures == null) ? List.of() : List.copyOf(failures);

        Instant now = Instant.now();

        return InboundScope.<T>builder()
                .source(source)
                .sourceId(sourceId)
                .meta(meta)
                .payload(stepContext.getPayload())
                .claims(claims)
                .currentStatus(currentStatus)
                .failures(failures)
                .now(now)
                .build();
    }
}
