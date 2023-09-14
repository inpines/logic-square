package org.dotspace.oofp.support.validation;

import org.dotspace.oofp.support.common.InterruptionConfigurable;

public interface ValidationPolicy<T> extends InterruptionConfigurable<ValidationPolicy<T>> {

    public boolean validate(T model, ValidationContext<T> ctx);

}