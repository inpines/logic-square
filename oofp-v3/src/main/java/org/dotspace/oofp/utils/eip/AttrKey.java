package org.dotspace.oofp.utils.eip;

import com.fasterxml.jackson.core.type.TypeReference;

import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.functional.Casters;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;

import java.util.function.Function;

public record AttrKey<R>(String name, TypeReference<R> typeRef, Function<Object, R> caster) {

    public static <R> AttrKey<R> of(String name, TypeReference<R> typeRef) {
        return new AttrKey<>(name, typeRef, Casters.cast(typeRef));
    }

    public <T> R getOrElse(StepContext<T> stepContext, R defaultValue) {
        return maybe(stepContext)
                .orElse(defaultValue);
    }

    public <T> Maybe<R> maybe(StepContext<T> stepContext) {
        return stepContext.getAttribute(name)
                .flatMap(inst -> Maybe.given(inst).map(caster));
    }

    public <T> Validation<Violations, R> require(StepContext<T> stepContext) {
        return stepContext.requireAttr(name, typeRef);
    }

}
