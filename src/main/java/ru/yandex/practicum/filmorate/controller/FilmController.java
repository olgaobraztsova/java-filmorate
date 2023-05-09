package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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

    /*
    *   название не может быть пустым;
        максимальная длина описания — 200 символов;
        дата релиза — не раньше 28 декабря 1895 года;
        продолжительность фильма должна быть положительной.
    * */
    private Film validateFilm(Film film) {
        if (film.getName().isBlank() || film.getName() == null) {
            log.error("Передано пустое название фильма: {}", film.getName());
            throw new ValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            log.error("Передано слишком большое описание фильма: {}", film.getDescription().length());
            throw new ValidationException("Описание фильма не может превышать 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Передана некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза фильма не может быть ранее дня рождения кино.");
        }
        if (film.getDuration() < 0) {
            log.error("Передана отрицательная продолжительность фильма: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма не может быть отрицательной.");
        }

        if (film.getId() == null) {
            film.setId(films.size() + 1);
        }

        return film;
    }
}
