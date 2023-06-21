package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(String id) {
        return filmStorage.getFilmById(Integer.parseInt(id));
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Integer addLike(Integer filmId, Integer userId) {
        return filmStorage.addLike(filmId, userId);
    }

    public boolean removeLike(Integer filmId, Integer userId) {
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostLikedFilms(Integer count) {
        return filmStorage.getMostPopularFilms(count);
    }

}
