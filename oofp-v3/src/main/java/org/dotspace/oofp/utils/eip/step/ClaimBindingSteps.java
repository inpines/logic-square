package org.dotspace.oofp.utils.eip.step;

import org.dotspace.oofp.model.dto.eip.InboundAttrKeys;
import org.dotspace.oofp.model.dto.eip.MessageClaims;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class ClaimBindingSteps {

    public static final String AUTH_MISSING_VIO = "auth.missing";

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
            AuthCondition<T> authCondition, @NonNull Supplier<MessageClaims> claimsSupplier) {
        return stepContext -> {

            MessageClaims claims;
            try {
                claims = claimsSupplier.get();
            } catch (Exception e) {
                claims = null;
            }

            if (null != claims) {
                return Validation.valid(stepContext.withAttribute(InboundAttrKeys.CLAIMS, claims));
            }

            if (authCondition.isOptional(stepContext)) {
                // 允許匿名：不寫 claims，也不 invalid
                return Validation.valid(stepContext);
            }

            // 需要 claims 但缺失：交給 policy / writer 走 401 / reject / DLQ
            return Validation.invalid(Violations.violate(AUTH_MISSING_VIO, "缺少認證資訊"));
        };
    }
}
