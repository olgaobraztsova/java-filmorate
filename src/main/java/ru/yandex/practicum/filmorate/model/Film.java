package ru.yandex.practicum.filmorate.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class Film {

    @PositiveOrZero
    private Integer id;
    @NotNull
    @NotBlank
    private String name;
    private String description;
    private LocalDate releaseDate;
    @PositiveOrZero
    private int duration;
}
