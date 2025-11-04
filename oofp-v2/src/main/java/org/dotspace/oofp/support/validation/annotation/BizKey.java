package org.dotspace.oofp.support.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.dotspace.oofp.support.validation.constraint.BizKeyValidator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Constraint(validatedBy = {BizKeyValidator.class})
@Target({ FIELD, METHOD })
public @interface BizKey {
	
    String message() default "{org.dotspace.oofp.support.validation.BizKey}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

	Class<?> entityClazz();
	
	String criteria();
	
	String isPresentExpression() default "true";
		
}
