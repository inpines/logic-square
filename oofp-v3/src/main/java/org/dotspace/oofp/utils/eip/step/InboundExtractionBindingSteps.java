package org.dotspace.oofp.utils.eip.step;

import org.dotspace.oofp.model.dto.eip.*;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.utils.eip.DecisionPolicy;
import org.dotspace.oofp.utils.eip.InboundScopes;
import org.dotspace.oofp.utils.eip.QuerySpecExtractor;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Stream;

@UtilityClass
public class InboundExtractionBindingSteps {

    public static final String VIO_QUERY_BUILD_FAILED = "query.build.failed";

    @SafeVarargs
    public <T> BehaviorStep<T> bindQuerySpec(@NonNull QuerySpecExtractor<T>... extractors) {
        return ctx -> {
            InboundQueryView<T> queryView = InboundScopes.from(ctx);
            try {
                List<Validation<Violations, QuerySpec>> validations = Stream.of(extractors)
                        .map(extractor -> extractor.extract(queryView))
                        .toList();

                List<Violations> errors = validations.stream()
                        .filter(Validation::isInvalid)
                        .map(Validation::error)
                        .filter(Maybe::isPresent)
                        .map(Maybe::get)
                        .toList();

                if (!errors.isEmpty()) {
                    Violations merged = errors.get(0);
                    errors.stream()
                            .skip(1)
                            .forEach(merged::join);
                    return Validation.invalid(merged);
                }

                List<QuerySpec> querySpecs = validations.stream()
                        .filter(Validation::isValid)
                        .map(Validation::get)
                        .filter(Maybe::isPresent)
                        .map(Maybe::get)
                        .toList();
                return Validation.valid(ctx.withAttribute(InboundAttrKeys.QUERY_SPEC_LIST, querySpecs));
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
