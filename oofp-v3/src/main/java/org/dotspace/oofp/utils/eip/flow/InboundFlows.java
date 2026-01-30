package org.dotspace.oofp.utils.eip.flow;

import org.dotspace.oofp.model.dto.eip.InboundEnvelope;
import org.dotspace.oofp.model.dto.eip.StatefulGate;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.eip.inbound.*;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class InboundFlows {

    public <T> InboundFlow<T> statelessFlow() {
        return env -> Validation.<Violations, InboundEnvelope<T>>valid(env)
                .flatMap(EnvelopeTranslators.toStepContext(InboundFlows::resolveInboundMetaSchema));
    }

    public <T> Validation<Violations, StepContext<T>> stateless(
            @NonNull InboundEnvelope<T> inboundEnvelope) {
        return Validation.<Violations, InboundEnvelope<T>>valid(inboundEnvelope)
                .flatMap(EnvelopeTranslators.toStepContext(
                        InboundFlows::resolveInboundMetaSchema));
    }

    public <T> Validation<Violations, StepContext<T>> statelessFromHttp(
            @NonNull String requestUri,
            Map<String, String> meta,
            @NonNull T payload) {
        InboundEnvelope<T> inboundEnvelope = InboundAdapters.fromHttp(
                requestUri, null != meta ? Map.copyOf(meta) : Map.of(), payload);
        return stateless(inboundEnvelope);
    }

    public <T> Validation<Violations, StepContext<T>> statelessFromHttp(
            @NonNull HttpServletRequest request,
            @NonNull InboundMetaExtractor<HttpServletRequest> metaExtractor,
            @NonNull T payload) {
        InboundEnvelope<T> inboundEnvelope = InboundAdapters.fromHttp(metaExtractor, request, payload);
        return stateless(inboundEnvelope);
    }

    public <T, H> Validation<Violations, StepContext<T>> statelessFromHttp(
            @NonNull String requestUri,
            @NonNull H headerSource,
            @NonNull InboundMetaExtractor<H> metaExtractor,
            @NonNull T payload) {

        Map<String, String> meta = metaExtractor.extract(headerSource);
        return statelessFromHttp(requestUri, meta, payload);
    }

    private InboundMetaSchema resolveInboundMetaSchema(
            InboundEnvelope.InboundSource inboundSource) {
        return switch (inboundSource) {
            case HTTP -> InboundMetaSchemas.http();
            case MQ -> InboundMetaSchemas.mq();
            case MQTT -> InboundMetaSchemas.mqtt();
            case FILE, SCHEDULED, OTHER, EMPTY_HTTP_CONTEXT -> InboundMetaSchemas.empty();
        };
    }

    public static <T> InboundFlow<T> statefulFlow(StatefulGate<T> gate) {
        // gate 只負責 stateful 相關（claims/query/status/decision），不包含 reader/writer
        return InboundFlows.<T>statelessFlow()
                .andThen(gate.step());
    }

    public <T> Validation<Violations, StepContext<T>> stateful(
            @NonNull InboundEnvelope<T> inboundEnvelope,
            @NonNull StatefulGate<T> statefulGate)  {
        return Validation.<Violations, InboundEnvelope<T>>valid(inboundEnvelope)
                .flatMap(EnvelopeTranslators.toStepContext(
                        InboundFlows::resolveInboundMetaSchema))
                .flatMap(BehaviorStep.chain(listChains(statefulGate))::execute);
    }

    private <T> List<BehaviorStep<T>> listChains(StatefulGate<T> statefulGate) {
        return statefulGate.steps().stream()
                .filter(Objects::nonNull)
                .toList();
    }

    public <T> Validation<Violations, StepContext<T>> statefulFromHttp(
            @NonNull HttpServletRequest request, @NonNull InboundMetaExtractor<HttpServletRequest> metaExtractor,
            T payload, @NonNull StatefulGate<T> statefulGate) {
        InboundEnvelope<T> inboundEnvelope = InboundAdapters.fromHttp(metaExtractor, request, payload);
        return stateful(inboundEnvelope, statefulGate);
    }

    public <T> Validation<Violations, StepContext<T>> statefulFromHttp(
            @NonNull String requestUri,
            Map<String, String> meta,
            @NonNull T payload, @NonNull StatefulGate<T> statefulGate) {
        InboundEnvelope<T> inboundEnvelope = InboundAdapters.fromHttp(
                requestUri, null != meta ? Map.copyOf(meta) : Map.of(), payload);
        return stateful(inboundEnvelope, statefulGate);
    }

    public <T, H> Validation<Violations, StepContext<T>> statefulFromHttp(
            @NonNull String requestUri,
            @NonNull H headerSource,
            @NonNull InboundMetaExtractor<H> metaExtractor,
            @NonNull T payload, @NonNull StatefulGate<T> statefulGate) {
        Map<String, String> meta = metaExtractor.extract(headerSource);
        return statefulFromHttp(requestUri, meta, payload, statefulGate);
    }

}
