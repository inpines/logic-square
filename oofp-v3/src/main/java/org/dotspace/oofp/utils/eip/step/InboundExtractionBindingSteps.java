package org.dotspace.oofp.utils.eip.step;

import org.dotspace.oofp.model.dto.eip.ControlDecision;
import org.dotspace.oofp.model.dto.eip.InboundAttrKeys;
import org.dotspace.oofp.model.dto.eip.InboundQueryView;
import org.dotspace.oofp.model.dto.eip.InboundScope;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.utils.eip.DecisionPolicy;
import org.dotspace.oofp.utils.eip.InboundScopes;
import org.dotspace.oofp.utils.eip.QuerySpecExtractor;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InboundExtractionBindingSteps {

    public static final String VIO_QUERY_BUILD_FAILED = "query.build.failed";

    public <T> BehaviorStep<T> bindQuerySpec(@NonNull QuerySpecExtractor<T> extractor) {
        return ctx -> {
            InboundQueryView<T> queryView = InboundScopes.from(ctx);
            try {
                return extractor.extract(queryView)
                        .map(qs -> ctx.withAttribute(InboundAttrKeys.QUERY_SPEC, qs));
            } catch (Exception e) {
                return Validation.invalid(Violations.violate(
                        VIO_QUERY_BUILD_FAILED,
                        "QuerySpec 建立失敗: " + e.getClass().getSimpleName()
                                + ", source=" + queryView.getSource() + ", sourceId=" + queryView.getSourceId()
                ));
            }
        };
    }

    public <T> BehaviorStep<T> decide(@NonNull DecisionPolicy policy) {
        return stepContext -> {
            InboundScope<T> scope = InboundScopes.from(stepContext);

            ControlDecision decision;
            try {
                decision = policy.decide(scope);
            } catch (Exception e) {
                decision = new ControlDecision.FailInternal(
                        "DecisionPolicy threw: " + e.getClass().getSimpleName(), e);
            }

            return Validation.valid(stepContext.withAttribute(InboundAttrKeys.NEXT_DECISION, decision));
        };
    }

}
