package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        Film validatedFilm = validateFilm(film);
        films.put(validatedFilm.getId(), validatedFilm);
        return validatedFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        Film validatedFilm = validateFilm(film);
        if (films.containsKey(validatedFilm.getId())) {
            films.put(validatedFilm.getId(), validatedFilm);
        } else {
            throw new ValidationException("Такого фильма в базе не существует");
        }

        return validatedFilm;
    }

    private Film validateFilm(Film film) {
        if (film.getId() == null) {
            film.setId(films.size() + 1);
        }
        return film;
    }
}
