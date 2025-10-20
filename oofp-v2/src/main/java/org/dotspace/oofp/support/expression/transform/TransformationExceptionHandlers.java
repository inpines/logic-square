package org.dotspace.oofp.support.expression.transform;

import java.util.function.BiFunction;

public interface TransformationExceptionHandlers {

    BiFunction<Exception, String, RuntimeException> getExceptionGenerator();

}
