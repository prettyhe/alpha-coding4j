package com.alpha.coding.bo.validation.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.alpha.coding.bo.validation.validator.ExcludeValidator;

/**
 * Exclude
 *
 * @version 1.0
 * Date: 2020/12/17
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
        ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Exclude.List.class)
@Constraint(validatedBy = {ExcludeValidator.class})
@Documented
public @interface Exclude {

    String message() default "不符合要求";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 范围，为空不校验
     */
    String[] value() default {};

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
            ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface List {
        Exclude[] value();
    }

}
