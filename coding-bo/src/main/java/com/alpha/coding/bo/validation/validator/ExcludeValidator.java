package com.alpha.coding.bo.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.alpha.coding.bo.validation.constraint.Exclude;

/**
 * ExcludeValidator
 *
 * @version 1.0
 * Date: 2020/12/17
 */
public class ExcludeValidator implements ConstraintValidator<Exclude, Object> {

    private String[] excludes;

    @Override
    public void initialize(Exclude constraintAnnotation) {
        this.excludes = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || excludes.length == 0) {
            return true; // 为空时不校验
        }
        for (String in : excludes) {
            if (in.equals(String.valueOf(value))) {
                return false;
            }
        }
        return true;
    }
}
