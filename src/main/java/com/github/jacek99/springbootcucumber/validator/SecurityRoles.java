package com.github.jacek99.springbootcucumber.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validates a security role code
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SecurityRolesImpl.class)
public @interface SecurityRoles {
    String message() default "{SecurityRoles.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
