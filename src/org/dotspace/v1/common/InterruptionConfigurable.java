package org.dotspace.v1.common;

public interface InterruptionConfigurable<T> {

    public boolean isBrokenOnFail();

    public T dontInterruptOnFail();

}
