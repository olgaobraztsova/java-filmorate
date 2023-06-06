package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(Integer id) {
        if (!checkIfFilmExists(id)) {
            throw new FilmNotFoundException("Фильма c ID " + id + "  в базе не существует");
        }
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        if (film.getId() == null) {
            film.setId(getAllFilms().size() + 1);
        }

        Set<Integer> emptyLikes = new HashSet<>();
        film.setLikes(emptyLikes);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getLikes() == null) {
            Set<Integer> emptyLikes = new HashSet<>();
            film.setLikes(emptyLikes);
        }

        if (checkIfFilmExists(film.getId())) {
            films.put(film.getId(), film);
        }
        return film;
    }

    private boolean checkIfFilmExists(Integer id) {
        if (!films.containsKey(id)) {
            throw new FilmNotFoundException("Фильма c ID " + id + "  в базе не существует");
        }
        return true;
    }
}
