package org.dotspace.validation;

import org.dotspace.common.InterruptionConfigurable;

public interface ValidationPolicy<T> extends InterruptionConfigurable<ValidationPolicy<T>> {

    public boolean validate(T model, ValidationsContext<T> ctx);

}