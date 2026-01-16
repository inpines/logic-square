package org.dotspace.oofp.utils.eip.inbound;

@FunctionalInterface
public interface InboundSourceReader<T, R> {
    R read(T source);

    static <T> InboundSourceReader<T, T> identity() {
        return source -> source;
    }
}
