package org.dotspace.oofp.support.validator.constraint;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface MandatoryFieldCase {

	String when();
	
	boolean present() default true;

	boolean empty() default false;
	
	String valueTest() default StringUtils.EMPTY;

}
