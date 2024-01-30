package com.polimi.PPP.CodeKataBattle.Utilities;

import com.polimi.PPP.CodeKataBattle.Annotations.ValidTimezone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;

public class TimezoneValidator implements ConstraintValidator<ValidTimezone, String> {

    @Override
    public void initialize(ValidTimezone constraintAnnotation) {
    }

    @Override
    public boolean isValid(String timezone, ConstraintValidatorContext context) {
        if (timezone == null || timezone.isEmpty()) {
            return false;
        }

        try {
            ZoneId.of(timezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
