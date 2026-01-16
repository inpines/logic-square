package org.dotspace.oofp.utils.eip;

import org.dotspace.oofp.model.dto.eip.InboundQueryView;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.eip.QuerySpec;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;

@FunctionalInterface
public interface QuerySpecExtractor<T> {

    Validation<Violations, QuerySpec> extract(InboundQueryView<T> queryView);

}
