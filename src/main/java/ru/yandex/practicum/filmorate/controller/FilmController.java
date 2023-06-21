package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable("id") String id) {
        return filmService.getFilmById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        Film newFilm = filmService.create(film);
        log.debug("Добавлен фильм {}", newFilm);
        return newFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        Film updatedFilm = filmService.update(film);
        log.debug("Обновлен фильм {}", updatedFilm);
        return updatedFilm;
    }

    //PUT /films/{id}/like/{userId} — пользователь ставит лайк фильму.
    @PutMapping("{id}/like/{userId}")
    public Integer addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.debug("Добавлен лайк к фильму с ID {}", id);
        return filmService.addLike(id, userId);
    }

    //DELETE /films/{id}/like/{userId} — пользователь удаляет лайк.
    @DeleteMapping("{id}/like/{userId}")
    public boolean removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.debug("Удален лайк у фильма с ID {}", id);
        return filmService.removeLike(id, userId);
    }

    //GET /films/popular?count={count} — возвращает список из первых count фильмов по количеству лайков.
    // Если значение параметра count не задано, верните первые 10.
    @GetMapping("/popular")
    public List<Film> getMostLikedFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return filmService.getMostLikedFilms(count);
    }
}
