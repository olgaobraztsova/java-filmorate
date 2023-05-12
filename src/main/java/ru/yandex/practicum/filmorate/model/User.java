package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class User {

    @PositiveOrZero
    private Integer id;
    @NotNull
    @Email
    private String email;
    @NotNull
    @Pattern(regexp = "^([a-zA-Z0-9.]+@){0,1}([a-zA-Z0-9.])+$", message =
            "Некорректно заданный логин: значение пустое либо содержит пробел.")
    private String login;
    private String name;
    @Past(message = "Дата рождения пользователя не может быть в будущем")
    private LocalDate birthday;
}
