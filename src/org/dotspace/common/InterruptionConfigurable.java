package org.dotspace.common;

public interface InterruptionConfigurable<T> {

    public boolean isBrokenOnFail();

    public T dontInterruptOnFail();

}
