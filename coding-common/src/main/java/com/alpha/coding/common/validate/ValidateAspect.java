package com.alpha.coding.common.validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * ValidateAspect
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public abstract class ValidateAspect {

    @Setter
    private boolean showInvalidPropertyPath = false;
    @Setter
    private volatile Validator validator;

    private static Method forExecutablesMethod;
    private static Method validateParametersMethod;
    private static Method validateReturnValueMethod;

    public ValidateAspect() {
        this(forExecutablesMethod != null ? Validation.buildDefaultValidatorFactory()
                : ValidateAspect.HibernateValidatorDelegate.buildValidatorFactory());
    }

    public ValidateAspect(ValidatorFactory validatorFactory) {
        this(validatorFactory.getValidator());
    }

    public ValidateAspect(Validator validator) {
        this.validator = validator;
        afterValidatorSet();
    }

    protected synchronized void afterValidatorSet() {
        try {
            forExecutablesMethod = this.validator.getClass().getMethod("forExecutables");
            Class<?> executableValidatorClass = forExecutablesMethod.getReturnType();
            validateParametersMethod = executableValidatorClass
                    .getMethod("validateParameters", Object.class, Method.class, Object[].class, Class[].class);
            validateReturnValueMethod = executableValidatorClass
                    .getMethod("validateReturnValue", Object.class, Method.class, Object.class, Class[].class);
        } catch (Exception e) {
        }
    }

    /**
     * 切面方法
     */
    public Object doValidate(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return validate(joinPoint);
        } catch (ConstraintViolationException e) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return onValidateFail(signature.getMethod().getReturnType(), e);
        }
    }

    private Object validate(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?>[] groups = new Class[0];

        if (forExecutablesMethod != null) {
            Object execVal;
            try {
                execVal = forExecutablesMethod.invoke(this.validator);
            } catch (AbstractMethodError e) {
                Validator nativeValidator = (Validator) this.validator.unwrap(Validator.class);
                execVal = forExecutablesMethod.invoke(nativeValidator);
                this.validator = nativeValidator;
            }

            Method methodToValidate = signature.getMethod();

            Set result = (Set) validateParametersMethod.invoke(execVal,
                    new Object[] {joinPoint.getTarget(), methodToValidate, joinPoint.getArgs(), groups});

            if (!result.isEmpty()) {
                throw new ConstraintViolationException(result);
            } else {
                Set set = new HashSet();
                set.addAll(result);
                result = set;
                final Object[] args = joinPoint.getArgs();
                final Class[] parameterTypes = signature.getParameterTypes();
                final Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null || parameterTypes[i].isPrimitive()) {
                        continue;
                    }
                    if (parameterAnnotations[i] != null && Arrays.stream(parameterAnnotations[i])
                            .filter(p -> p.getClass().equals(Valid.class)).findAny().isPresent()) {
                        continue;
                    }
                    result.addAll(this.validator.validate(args[i], groups));
                }
                if (!result.isEmpty()) {
                    throw new ConstraintViolationException(result);
                }

                // validate returnValue
                Object returnValue = joinPoint.proceed();
                result = (Set) validateReturnValueMethod.invoke(execVal,
                        new Object[] {joinPoint.getTarget(), methodToValidate, returnValue, groups});
                if (!result.isEmpty()) {
                    throw new ConstraintViolationException(result);
                } else {
                    return returnValue;
                }
            }
        } else {
            return ValidateAspect.HibernateValidatorDelegate
                    .invokeWithinValidation(joinPoint, this.validator, groups);
        }

    }

    private static class HibernateValidatorDelegate {
        private HibernateValidatorDelegate() {
        }

        public static ValidatorFactory buildValidatorFactory() {
            return ((HibernateValidatorConfiguration) Validation.byProvider(HibernateValidator.class).configure())
                    .buildValidatorFactory();
        }

        public static Object invokeWithinValidation(ProceedingJoinPoint joinPoint, Validator validator,
                                                    Class<?>[] groups) throws Throwable {
            ExecutableValidator executableValidator = validator.unwrap(ExecutableValidator.class);
            final Object target = joinPoint.getTarget();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Set result = executableValidator
                    .validateParameters(target, signature.getMethod(), joinPoint.getArgs(), groups);
            if (!result.isEmpty()) {
                throw new ConstraintViolationException(result);
            } else {
                Set set = new HashSet();
                set.addAll(result);
                result = set;
                final Object[] args = joinPoint.getArgs();
                final Class[] parameterTypes = signature.getParameterTypes();
                final Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null || parameterTypes[i].isPrimitive()) {
                        continue;
                    }
                    if (parameterAnnotations[i] != null && Arrays.stream(parameterAnnotations[i])
                            .filter(p -> p.getClass().equals(Valid.class)).findAny().isPresent()) {
                        continue;
                    }
                    result.addAll(validator.validate(args[i], groups));
                }
                if (!result.isEmpty()) {
                    throw new ConstraintViolationException(result);
                }

                // validate returnValue
                Object returnValue = joinPoint.proceed();
                result = executableValidator
                        .validateReturnValue(target, signature.getMethod(), returnValue, groups);
                if (!result.isEmpty()) {
                    throw new ConstraintViolationException(result);
                } else {
                    return returnValue;
                }
            }
        }
    }

    /**
     * 处理校验失败，子类可覆盖
     */
    protected Object onValidateFail(Class<?> returnType, ConstraintViolationException e) {
        return genValidateNotPassRet(returnType, e.getConstraintViolations().stream()
                .map(p -> p.getMessage()
                        + (showInvalidPropertyPath ? (",字段" + p.getPropertyPath().toString() + "=" + p
                        .getInvalidValue()) : ""))
                .collect(Collectors.toList()));
    }

    /**
     * 钩子方法，子类覆盖
     */
    protected abstract Object genValidateNotPassRet(Class<?> returnType, List<String> validateResults);

}
