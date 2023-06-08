package ru.yandex.practicum.filmorate.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.After;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class Film {

    @PositiveOrZero
    private Integer id;

    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;

    @Size(max = 200, message = "Передано слишком длинное описание фильма")
    private String description;

    @After("1895-12-28")
    @Past
    private LocalDate releaseDate;

    @PositiveOrZero(message = "Описание фильма не может превышать 200 символов.")
    private int duration;
    private Set<Integer> likes;
    private Set<String> genre;
    private String rating;
}
