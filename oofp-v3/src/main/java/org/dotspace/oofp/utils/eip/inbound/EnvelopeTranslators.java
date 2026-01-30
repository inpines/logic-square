package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.model.dto.eip.InboundAttrKeys;
import org.dotspace.oofp.model.dto.eip.InboundEnvelope;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
public class EnvelopeTranslators {

    public <R> Function<InboundEnvelope<R>, Validation<Violations, StepContext<R>>> toStepContext(
            InboundMetaSchemaResolver schemaResolver // 依 source 選 schema
    ) {
        return env -> {
            Violations vio = Violations.empty();

            if (env == null) {
                return Validation.invalid(Violations.violate(
                        "envelope.null", "InboundEnvelope 為 null"));
            }

            // 1) payload 檢查（在這一步才把 null 變成語意失敗）
            if (env.payload() == null) {
                vio = vio.join(Violations.violate("payload.missing", "缺少 payload"));
            }

            // 2) meta 正規化
            InboundMetaSchema schema = schemaResolver.resolve(env.source());
            var normalized = InboundMetaNormalizer.normalize(env.meta(), schema);
            vio = vio.join(normalized.violations());

            // 3) 建 StepContext（不希望 setAttribute，就用 withAttribute chain）
            StepContext<R> sc = StepContext.<R>builder()
                    .withPayload(env.payload())
                    .build()
                    .withAttribute(InboundAttrKeys.ENVELOPE, env)
                    .withAttribute(InboundAttrKeys.SOURCE, env.source())
                    .withAttribute(InboundAttrKeys.SOURCE_ID, env.sourceId())
                    .withAttribute(InboundAttrKeys.META, normalized.meta());

            // 4) 回傳 validation
            return vio.isEmpty()
                    ? Validation.valid(sc)
                    : Validation.invalid(vio);
        };
    }

    @FunctionalInterface
    public interface InboundMetaSchemaResolver {
        InboundMetaSchema resolve(InboundEnvelope.InboundSource source);
    }

}
