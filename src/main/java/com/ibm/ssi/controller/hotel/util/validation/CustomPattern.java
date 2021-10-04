package com.ibm.ssi.controller.hotel.util.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomPatternValidator.class)
public @interface CustomPattern {
    String type() default "general";

    String message() default "Property includes forbidden characters.";
    
    Class<?>[] groups() default {};

    public abstract Class<? extends Payload>[] payload() default {};
}