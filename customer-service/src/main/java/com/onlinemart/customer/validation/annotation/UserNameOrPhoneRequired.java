package com.onlinemart.customer.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.onlinemart.customer.validation.validator.UserNameOrPhoneValidator;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserNameOrPhoneValidator.class)
@Documented
public @interface UserNameOrPhoneRequired {

    String message() default "Either userName or phoneNumber must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}