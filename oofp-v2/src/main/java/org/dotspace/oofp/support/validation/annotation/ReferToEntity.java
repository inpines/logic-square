package org.dotspace.oofp.support.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.support.validation.constraint.ReferToEntityValidator;

@Documented
@Retention(RUNTIME)
@Constraint(validatedBy = {ReferToEntityValidator.class})
@Target({ FIELD, METHOD })
public @interface ReferToEntity {

    public String message() default "{com.skh.hrm.component.firsttech.constraint.ReferToEntity}";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};

	public Class<?> entityClazz();
	
	public String keyFieldName() default StringUtils.EMPTY;
	
	public boolean isPresent() default true;
	
}
