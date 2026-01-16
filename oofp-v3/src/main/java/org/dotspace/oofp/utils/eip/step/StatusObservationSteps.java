package org.dotspace.oofp.utils.eip.step;

import org.dotspace.oofp.enumeration.eip.ErrorTaxonomy;
import org.dotspace.oofp.model.dto.eip.Failure;
import org.dotspace.oofp.model.dto.eip.InboundAttrKeys;
import org.dotspace.oofp.model.dto.eip.InboundScope;
import org.dotspace.oofp.model.dto.eip.MessageStatus;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.utils.eip.InboundScopes;
import org.dotspace.oofp.utils.eip.StatusObserver;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.List;

@UtilityClass
public class StatusObservationSteps {

    public static final String VIO_STATUS_OBSERVE_FAILED = "status.observe.failed";

    public <T> BehaviorStep<T> observeStatus(@NonNull StatusObserver<T> observer) {
        return stepContext -> {
            InboundScope<T> scope = InboundScopes.from(stepContext);

            try {
                StatusObserver.Observation obs = observer.observe(scope);

                MessageStatus status = obs.status() != null ? obs.status() : MessageStatus.unknown(
                        "status.missing", Instant.now());
                List<Failure> failures = obs.failures() != null ? List.copyOf(obs.failures()) : List.of();

                return Validation.valid(stepContext
                        .withAttribute(InboundAttrKeys.STATUS, status)
                        .withAttribute(InboundAttrKeys.FAILURES, failures)
                );
            } catch (Exception e) {
                // 觀測失敗通常不要讓整條中斷：降級成 UNKNOWN + failure 記錄（或 invalid，看你策略）
                List<Failure> failures = List.of(
                        Failure.of(ErrorTaxonomy.INTERNAL, VIO_STATUS_OBSERVE_FAILED, e.getClass().getSimpleName())
                );

                return Validation.valid(stepContext
                        .withAttribute(InboundAttrKeys.STATUS, MessageStatus.unknown(
                                VIO_STATUS_OBSERVE_FAILED, Instant.now()))
                        .withAttribute(InboundAttrKeys.FAILURES, failures)
                );
            }
        };
    }
}
