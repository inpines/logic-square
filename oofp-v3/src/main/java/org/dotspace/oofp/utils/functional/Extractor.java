package org.dotspace.oofp.utils.functional;

@FunctionalInterface
public interface Extractor<T, R> {

    R extract(T source);
}
