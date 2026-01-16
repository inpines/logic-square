package org.dotspace.oofp.support.validator.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Constraint(validatedBy = {CorrelationValidator.class})
@Target({ FIELD, METHOD })
public @interface Correlation {

    String message() default "相關檢核失敗";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
