package org.dotspace.oofp.utils.eip.step;

import org.dotspace.oofp.model.dto.auth.AuthContext;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.eip.InboundAttrKeys;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class AccessGateSteps {

    public static final String AUTHORIZATION_DENIED_VIO = "authorization.denied";
    public static final String MISSING_AUTHORITIES = "Missing authorities: ";
    public static final String MISSING_ANY_AUTHORITY = "Missing any authority: ";

    public <T> BehaviorStep<T> denyAnonymousIfRequired(
            @NonNull AuthBindingSteps.AuthCondition<T> authCondition) {
        return sc -> authCondition.isOptional(sc) ? Validation.valid(sc) : denyAnonymous(sc);
    }

    private static <T> Validation<Violations, StepContext<T>> denyAnonymous(StepContext<T> sc) {
        return InboundAttrKeys.AUTH_CONTEXT.maybe(sc)
                .filter(ac -> !"anonymous".equals(ac.getPrincipalId()))
                .map(ac -> Validation.<Violations, StepContext<T>>valid(sc))
                .orElseGet(() -> Validation.invalid(Violations.violate(
                        AUTHORIZATION_DENIED_VIO, "Anonymous not allowed"
                )));
    }

    public <T> BehaviorStep<T> requireAllAuthorities(@NonNull Set<String> required) {
        return sc -> InboundAttrKeys.AUTH_CONTEXT.maybe(sc)
                .map(ac -> denyNotRequired(required, sc, ac))
                .orElseGet(() -> denyMissingAuthorities(required, Set.of()));
    }

    private <T> Validation<Violations, StepContext<T>> denyMissingAuthorities(Set<String> required, Set<String> have) {
        return Validation.invalid(Violations.violate(
                AUTHORIZATION_DENIED_VIO, AccessGateSteps.MISSING_AUTHORITIES + missing(required, have))
        );
    }

    private <T> Validation<Violations, StepContext<T>> denyNotRequired(
            Set<String> required, StepContext<T> sc, AuthContext ac) {
        Set<String> have = ac.getAuthorities() == null ? Set.of() : ac.getAuthorities();
        return have.containsAll(required) ? Validation.valid(sc)
                : denyMissingAuthorities(required, have);
    }

    public <T> BehaviorStep<T> requireAnyAuthority(@NonNull Set<String> anyOf) {
        return sc ->
                InboundAttrKeys.AUTH_CONTEXT.maybe(sc)
                        .map(ac -> {
                            Set<String> have = ac.getAuthorities() == null ? Set.of() : ac.getAuthorities();
                            boolean ok = anyOf.stream().anyMatch(have::contains);
                            return ok ? Validation.<Violations, StepContext<T>>valid(sc)
                                    : AccessGateSteps.<T>denyAnyOf(anyOf);
                        })
                        .orElseGet(() -> denyAnyOf(anyOf));
    }

    private <T> Validation<Violations, StepContext<T>> denyAnyOf(
            Set<String> anyOf) {
        return Validation.invalid(Violations.violate(
                AUTHORIZATION_DENIED_VIO,
                AccessGateSteps.MISSING_ANY_AUTHORITY + String.join(",", anyOf))
        );
    }

    private String missing(Set<String> required, Set<String> have) {
        return required.stream().sorted()
                .filter(r -> !have.contains(r))
                .collect(Collectors.joining(","));
    }

}
