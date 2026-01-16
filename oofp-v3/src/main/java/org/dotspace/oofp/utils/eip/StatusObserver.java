package org.dotspace.oofp.utils.eip;

import org.dotspace.oofp.model.dto.eip.Failure;
import org.dotspace.oofp.model.dto.eip.InboundScope;
import org.dotspace.oofp.model.dto.eip.MessageStatus;

import java.util.List;

@FunctionalInterface
public interface StatusObserver<T> {
    Observation observe(InboundScope<T> scope);

    record Observation(
            MessageStatus status,
            List<Failure> failures
    ) {}
}
