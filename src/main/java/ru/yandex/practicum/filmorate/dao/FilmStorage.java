package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> getAllFilms();

    Film create(Film film);

    Film update(Film film);

    Film getFilmById(Integer id);

    Integer addLike(Integer filmId, Integer userId);

    boolean removeLike(Integer filmId, Integer userId);

    List<Film> getMostPopularFilms(Integer count);
}
