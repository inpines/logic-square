package org.dotspace.oofp.model.dto.behaviorstep;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class StepContextAttributes {

    public <T> Map<String, Object> copyOf(@NonNull StepContext<T> stepContext) {
        return new HashMap<>(stepContext.getAttributes());
    }

}
