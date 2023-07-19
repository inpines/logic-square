package org.dotspace.v1.validation;

import org.dotspace.v1.common.InterruptionConfigurable;

public interface ValidationPolicy<T> extends InterruptionConfigurable<ValidationPolicy<T>> {

    public boolean validate(T model, ValidationsContext<T> ctx);

}