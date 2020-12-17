package com.alpha.coding.bo.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.alpha.coding.bo.validation.CommonTool;
import com.alpha.coding.bo.validation.constraint.IdCard;

/**
 * IdCardValidator
 *
 * @version 1.0
 * Date: 2020/12/17
 */

public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    @Override
    public void initialize(IdCard constraintAnnotation) {
        // nothing
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // 为空时不校验
        }
        if (CommonTool.isBlank(value)) {
            return false;
        }
        return CommonTool.isValidCard(value);
    }
}
