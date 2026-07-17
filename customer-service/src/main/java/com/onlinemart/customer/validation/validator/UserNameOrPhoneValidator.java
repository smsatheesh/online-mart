package com.onlinemart.customer.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import com.onlinemart.customer.dto.request.customer.CreateCustomerDto;
import com.onlinemart.customer.validation.annotation.UserNameOrPhoneRequired;

public class UserNameOrPhoneValidator
        implements ConstraintValidator<UserNameOrPhoneRequired, CreateCustomerDto> {

    @Override
    public boolean isValid(CreateCustomerDto dto,
                           ConstraintValidatorContext context) {

        if (dto == null) {
            return true;
        }

        return StringUtils.hasText(dto.getUserName())
                || StringUtils.hasText(dto.getPhoneNumber());
    }
}