package org.dotspace.oofp.utils.eip;

import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public class SplitterOps {

    public <T, I> BehaviorStep<T> split(
            Predicate<StepContext<T>> predicate, Function<StepContext<T>, List<I>> itemExtractor,
            AttrKey<List<I>> itemsAttrKey) {

        return flatSplit(predicate, sc -> validateItems(itemExtractor, nonEmpty(), sc), itemsAttrKey);
    }

    public <T, I> BehaviorStep<T> split(
            Predicate<StepContext<T>> predicate, Function<StepContext<T>, List<I>> itemExtractor,
            Predicate<List<I>> verifier, AttrKey<List<I>> itemsAttrKey) {

        return flatSplit(predicate, sc -> validateItems(itemExtractor, verifier, sc), itemsAttrKey);
    }

    private <T, I> Validation<Violations, List<I>> validateItems(
            Function<StepContext<T>, List<I>> itemExtractor, Predicate<List<I>> verifier,
            StepContext<T> stepContext) {
        return Maybe.given(stepContext).map(itemExtractor)
                .filter(verifier)
                .toValidation(Violations.violate("split.items.invalid",
                        "items verification failed"));
    }

    public <T, I> BehaviorStep<T> flatSplit(
            Predicate<StepContext<T>> predicate, Function<StepContext<T>, Validation<Violations,
                                List<I>>> validatedItemsResolver, AttrKey<List<I>> itemsAttrKey) {

        return sc -> Maybe.given(sc)
                .toValidation(Violations.violate("split.step-context.empty",
                        "step context is empty"))
                .filter(predicate, Violations.violate("split.predicate.not-matched",
                        "predicate not matched"))
                .flatMap(validatedItemsResolver) // 這裡得到 List<I>
                .map(items -> sc.withAttribute(itemsAttrKey.name(), items));
    }

    private <I> Predicate<List<I>> nonEmpty() {
        return Predicate.not(List::isEmpty);
    }

}
