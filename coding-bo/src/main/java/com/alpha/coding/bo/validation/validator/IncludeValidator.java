package com.alpha.coding.bo.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.alpha.coding.bo.validation.constraint.Include;

/**
 * IncludeValidator
 *
 * @version 1.0
 * Date: 2020/12/17
 */

public class IncludeValidator implements ConstraintValidator<Include, Object> {

    private String[] includes;

    @Override
    public void initialize(Include constraintAnnotation) {
        this.includes = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || includes.length == 0) {
            return true; // 为空时不校验
        }
        for (String in : includes) {
            if (in.equals(String.valueOf(value))) {
                return true;
            }
        }
        return false;
    }
}
