package org.dotspace.oofp.support.common;

public interface InterruptionConfigurable<T> {

    public boolean isBrokenOnFail();

    public T dontInterruptOnFail();

}
