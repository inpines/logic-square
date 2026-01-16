package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.enumeration.eip.ErrorTaxonomy;
import org.dotspace.oofp.model.dto.eip.ControlDecision;
import org.dotspace.oofp.model.dto.eip.Failure;
import org.dotspace.oofp.model.dto.eip.InboundDecisionView;
import org.dotspace.oofp.utils.eip.DecisionPolicy;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.Map;
import java.util.function.Predicate;

@UtilityClass
public class InboundDecisionPolicies {

    public DecisionPolicy taxonomyDrivenRetry(
            Predicate<Map<String, String>> authOptional, int maxAttempts, Duration baseBackoff) {
        return in -> decide(in, authOptional, maxAttempts, baseBackoff);
    }

    private ControlDecision decide(
            InboundDecisionView in, Predicate<Map<String, String>> authOptional,
            int maxAttempts, Duration baseBackoff) {
        var failures = in.getFailures();
        if (failures == null || failures.isEmpty()) {
            return new ControlDecision.Ack();
        }

        boolean isAuthOptional = in.getMeta() != null && authOptional.test(in.getMeta());

        // ✅ 不用先判斷 hasAuthFailure：直接 normalize
        var effectiveFailures = isAuthOptional
                ? failures.stream()
                .filter(f -> f.taxonomy() != ErrorTaxonomy.UNAUTHORIZED)
                .toList()
                : failures;

        // authOptional 且 failure 全是 UNAUTHORIZED → 視為無事（允許匿名）
        if (effectiveFailures.isEmpty()) {
            return new ControlDecision.Ack();
        }

        if (hasTaxonomy(effectiveFailures, ErrorTaxonomy.CONFLICT)) {
            return new ControlDecision.Noop("idempotency/conflict");
        }

        if (hasTaxonomy(effectiveFailures, ErrorTaxonomy.VALIDATION)
                || hasTaxonomy(effectiveFailures, ErrorTaxonomy.UNAUTHORIZED)) {
            return new ControlDecision.Dlq(joinCodes(effectiveFailures));
        }

        if (hasTaxonomy(effectiveFailures, ErrorTaxonomy.TRANSIENT_DEPENDENCY)
                || hasTaxonomy(effectiveFailures, ErrorTaxonomy.NOT_FOUND)) {

            int attempt = Math.max(0, in.getCurrentStatus().attempt());
            if (attempt >= maxAttempts) {
                return new ControlDecision.Dlq("max-attempts-reached: " + joinCodes(effectiveFailures));
            }

            var next = in.getNow().plus(backoff(attempt, baseBackoff));
            return new ControlDecision.Retry(next, joinCodes(effectiveFailures));
        }

        Failure first = effectiveFailures.get(0);
        return new ControlDecision.FailInternal(joinCodes(effectiveFailures), first.cause());
    }

    private boolean hasTaxonomy(java.util.List<Failure> failures, ErrorTaxonomy tax) {
        return failures.stream().anyMatch(f -> f.taxonomy() == tax);
    }

    private String joinCodes(java.util.List<Failure> failures) {
        return failures.stream()
                .map(Failure::code)
                .distinct()
                .reduce((a, b) -> a + "," + b)
                .orElse("unknown");
    }

    private java.time.Duration backoff(int attempt, Duration baseBackoff) {
        // 指數回退：base * 2^attempt（可改成 jitter）
        long factor = 1L << Math.min(attempt, 10);
        return baseBackoff.multipliedBy(factor);
    }

}
