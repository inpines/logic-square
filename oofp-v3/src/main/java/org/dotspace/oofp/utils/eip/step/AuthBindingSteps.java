package org.dotspace.oofp.utils.eip.step;

import org.dotspace.oofp.model.dto.auth.AuthContext;
import org.dotspace.oofp.model.dto.eip.InboundAttrKeys;
import org.dotspace.oofp.model.dto.eip.MessageClaims;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.eip.auth.AuthContexts;
import org.dotspace.oofp.utils.eip.auth.EntitlementsResolver;
import org.dotspace.oofp.utils.functional.Extractor;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Set;
import java.util.function.Supplier;

@UtilityClass
public class AuthBindingSteps {

    public static final String AUTH_CLAIMS_FAILED = "auth.claims.failed";

    @FunctionalInterface
    public interface AuthCondition<T> {
        boolean isOptional(StepContext<T> ctx);

        static <T> AuthCondition<T> required() { // 永遠不 optional
            return ctx -> false;
        }

        static <T> AuthCondition<T> optional() { // 永遠 optional
            return ctx -> true;
        }

    }

    public <T> BehaviorStep<T> bindClaims(
            AuthCondition<T> authCondition, @NonNull Supplier<Validation<Violations, MessageClaims>> claimsSupplier) {
        return stepContext -> {

            Validation<Violations, MessageClaims> claims = claimsSupplier.get();

            if (claims.isValid()) {
                return claims.map(c -> stepContext.withAttribute(InboundAttrKeys.CLAIMS, c));
            }

            if (authCondition.isOptional(stepContext)) {
                // 允許匿名：不寫 claims，也不 invalid
                return Validation.valid(stepContext);
            }

            // 需要 claims 但缺失：交給 policy / writer 走 401 / reject / DLQ
            return Validation.invalid(Violations.violate(AUTH_CLAIMS_FAILED, "Claims 解析失敗" +
                    claims.error()
                            .map(Violations::collectMessages)
                            .orElse("violations is empty"))
            );
        };
    }

    public static final String AUTH_MISSING_VIO = "auth.ctx.missing";
    public static final String AUTH_BUILD_FAILED_VIO = "auth.ctx.build.failed";

    public <T> BehaviorStep<T> bindAuthContext(
            AuthCondition<T> authCondition,
            @NonNull Supplier<Validation<Violations, Authentication>> authSupplier,
            @NonNull Extractor<Authentication, AuthContext> authContextExtractor) {

        return stepContext ->
                authSupplier.get()
                        .flatMap(auth -> {
                            // unauthenticated / anonymous → treat as missing
                            if (!auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                                // optional → anonymous
                                if (authCondition.isOptional(stepContext)) {
                                    return Validation.valid(AuthContext.anonymous());
                                }

                                return Validation.invalid(Violations.violate(
                                        AUTH_MISSING_VIO, "Authentication missing or anonymous"));
                            }
                            try {
                                // authenticated → build auth context
                                return Validation.valid(authContextExtractor.extract(auth));
                            } catch (Exception ex) {
                                return Validation.invalid(Violations.violate(
                                        AUTH_BUILD_FAILED_VIO,
                                        "AuthContext build failed: " + ex.getClass().getSimpleName() +
                                                Maybe.given(ex.getMessage())
                                                        .map(msg -> ", message=" + msg)
                                                        .orElse("") + ", principal=" + auth.getName()
                                ));
                            }
                        })
                        .map(ac -> stepContext.withAttribute(InboundAttrKeys.AUTH_CONTEXT, ac));
    }

    public static final String AUTH_BINDING_RESOLVE_FAILED_VIO = "auth-binding.resolve.failed";
    public static final String AUTH_BINDING_UNAUTHORIZED_VIO = "auth-binding.unauthorized";

    record Entitlements(Set<String> roles, Set<String> roleGroups, Set<String> authorities) {}

    // 根據 AuthContext 的 principalId 解析並豐富使用者權限資訊
    public <T> BehaviorStep<T> resolveEntitlements(
            AuthCondition<T> authCondition,
            @NonNull EntitlementsResolver resolver) {
        return stepContext -> {
            var principal = InboundAttrKeys.AUTH_CONTEXT.maybe(stepContext)
                    .map(AuthContext::getPrincipalId)
                    .orElse(null);

            return InboundAttrKeys.AUTH_CONTEXT.maybe(stepContext)

                    // 僅保留「有 principal 且非 anonymous」的情況
                    .filter(ac -> !"anonymous".equals(ac.getPrincipalId()))

                    // 有 principal → resolve → enrich
                    .map(ac -> {
                        try {
                            return resolver.resolve(principal)
                                    .map(ent -> AuthContexts
                                            .enrich(ac, ent))
                                    .map(enriched ->
                                            stepContext.withAttribute(
                                                    InboundAttrKeys.AUTH_CONTEXT, enriched));
                        } catch (Exception ex) {
                            return Validation.<Violations, StepContext<T>>invalid(Violations.violate(
                                    AUTH_BINDING_RESOLVE_FAILED_VIO, "Entitlements resolve failed: "
                                            + ex.getClass().getSimpleName()
                                            + Maybe.given(ex.getMessage())
                                            .map(msg -> ", message=" + msg)
                                            .orElse("")
                                            + ", principal=" + ac.getPrincipalId()
                            ));
                        }
                    })

                    // 沒有 principal / anonymous
                    .orElseGet(() ->
                            authCondition.isOptional(stepContext)
                                    ? Validation.valid(stepContext)
                                    : Validation.invalid(Violations.violate(
                                    AUTH_BINDING_UNAUTHORIZED_VIO,
                                    "Unauthorized principal:" + principal
                            ))
                    );
        };
    }

}
