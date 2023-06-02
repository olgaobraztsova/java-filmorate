package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable("id") String id) {
        return filmStorage.getFilmById(Integer.parseInt(id));
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        Film validatedAddedFilm = filmStorage.create(validateFilm(film));
        log.info("Добавлен фильм {}", validatedAddedFilm);
        return validatedAddedFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        Film validatedUpdatedFilm = filmStorage.update(validateFilm(film));
        log.info("Обновлен фильм {}", validatedUpdatedFilm);
        return validatedUpdatedFilm;
    }

    //PUT /films/{id}/like/{userId} — пользователь ставит лайк фильму.
    @PutMapping("{id}/like/{userId}")
    public Film addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Добавлен лайк к фильму с ID {}", id);
        return filmService.addLike(id, userId);
    }

    //DELETE /films/{id}/like/{userId} — пользователь удаляет лайк.
    @DeleteMapping("{id}/like/{userId}")
    public Film removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Удален лайк у фильма с ID {}", id);
        return filmService.removeLike(id, userId);
    }

    //GET /films/popular?count={count} — возвращает список из первых count фильмов по количеству лайков.
    // Если значение параметра count не задано, верните первые 10.
    @GetMapping("/popular")
    public List<Film> getMostLikedFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return filmService.getMostLikedFilms(count);
    }

    private Film validateFilm(Film film) {
        if (film.getId() == null) {
            film.setId(filmStorage.getAllFilms().size() + 1);
        }
        return film;
    }
}
