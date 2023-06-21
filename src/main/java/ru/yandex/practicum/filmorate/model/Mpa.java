package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
@AllArgsConstructor
@Builder
public class Mpa {
    @PositiveOrZero
    private Integer id;
    private String name;
}
