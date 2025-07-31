package ru.otus.hw.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

public class NotBlankElementsValidator implements ConstraintValidator<NotBlankElements, Collection<String>> {

    @Override
    public void initialize(NotBlankElements constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Collection<String> collection, ConstraintValidatorContext context) {
        if (collection == null) {
            return true; // Let @NotEmpty handle null collections
        }
        
        for (String element : collection) {
            if (element == null || element.trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
}