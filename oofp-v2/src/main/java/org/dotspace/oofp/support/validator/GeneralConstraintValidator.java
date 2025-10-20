package org.dotspace.oofp.support.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class GeneralConstraintValidator<I> {

    private final Validator validator;

    public boolean validate(I info) {
        Set<ConstraintViolation<I>> constraintViolations = validator.validate(info);
        return constraintViolations.isEmpty();
    }

}
