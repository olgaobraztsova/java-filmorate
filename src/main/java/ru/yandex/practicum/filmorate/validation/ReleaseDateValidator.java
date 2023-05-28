package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<After, LocalDate> {
    private LocalDate date;

    public void initialize(After annotation) {
        date = LocalDate.parse(annotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext cxt) {
        boolean valid = false;
        if (value != null) {
            if (value.isAfter(date)) {
                valid = true;
            }
            if (value.equals(date)) {
                valid = true;
            }
        }
        return valid;
    }
}