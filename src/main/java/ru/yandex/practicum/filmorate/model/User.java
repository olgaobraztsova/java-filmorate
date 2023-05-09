package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.Duration;
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
    private String login;
    private String name;
    private LocalDate birthday;

}
