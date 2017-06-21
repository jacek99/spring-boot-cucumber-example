package com.github.jacek99.springbootcucumber.validator;

import com.github.jacek99.springbootcucumber.security.SecurityConstants;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Security role validator
 */
public class SecurityRolesImpl implements ConstraintValidator<SecurityRoles,Set<String>> {

    @Override
    public void initialize(SecurityRoles constraintAnnotation) {}

    @Override
    public boolean isValid(Set<String> values, ConstraintValidatorContext context) {
        if (values == null || values.isEmpty()) {
            return true;
        } else {
            return !values.stream()
                    .filter(v -> !SecurityConstants.ALL_ROLES.contains(v))
                    .findFirst().isPresent();
        }
    }

}
