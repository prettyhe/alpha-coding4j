package com.alpha.coding.common.utils;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import com.google.common.collect.Lists;

/**
 * BeanValidationUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class BeanValidationUtils {

    private static ValidatorFactory vf;

    static {
        try {
            vf = Validation.byProvider(HibernateValidator.class).configure()
                    .messageInterpolator(new ResourceBundleMessageInterpolator(new ResourceBundleLocator() {
                        @Override
                        public ResourceBundle getResourceBundle(Locale arg0) {
                            ResourceBundle bundle =
                                    ResourceBundle.getBundle("ValidationMessages", Locale.SIMPLIFIED_CHINESE);
                            return bundle;
                        }
                    })).buildValidatorFactory();
        } catch (Exception e) {
            vf = Validation.buildDefaultValidatorFactory();
        }
    }

    public static <T> List<String> validate(T request) {
        return validate(request, false);
    }

    public static <T> List<String> validate(T request, boolean withPropertyPath) {
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<T>> set = validator.validate(request);
        List<String> errMsgs = Lists.newArrayList();
        for (ConstraintViolation<T> cv : set) {
            StringBuilder sb = new StringBuilder(cv.getMessage());
            if (withPropertyPath) {
                sb.append(",字段").append(cv.getPropertyPath().toString())
                        .append("=").append(cv.getInvalidValue());
            }
            errMsgs.add(sb.toString());
        }
        return errMsgs;
    }

}
