package com.polimi.PPP.CodeKataBattle.Annotations;

import com.polimi.PPP.CodeKataBattle.Utilities.TimezoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TimezoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimezone {
    String message() default "Invalid timezone";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

