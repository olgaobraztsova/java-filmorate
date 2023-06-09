package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

    @Target({ FIELD, PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Constraint(validatedBy = ReleaseDateValidator.class)
    public @interface After {
        //error message
        String message() default "Дата релиза фильма может быть после {}";
        //represents group of constraints
        Class<?>[] groups() default {};
        //represents additional information about annotation
        Class<? extends Payload>[] payload() default {};
        String value();
    }

