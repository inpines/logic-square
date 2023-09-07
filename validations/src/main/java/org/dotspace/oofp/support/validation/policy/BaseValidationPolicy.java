package org.dotspace.oofp.support.validation.policy;

import org.dotspace.oofp.support.validation.ValidationPolicy;

public abstract class BaseValidationPolicy<T> implements ValidationPolicy<T> {

    private boolean brokenOnFali = true;

    @Override
    public boolean isBrokenOnFail() {
        return brokenOnFali;
    }

    @Override
    public ValidationPolicy<T> dontInterruptOnFail() {
        this.brokenOnFali = false;
        return this;
    }

}