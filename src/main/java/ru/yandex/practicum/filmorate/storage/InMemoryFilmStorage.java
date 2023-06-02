package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static InMemoryFilmStorage obj;
    private static final Map<Integer, Film> films = new HashMap<>();

    private InMemoryFilmStorage() {

    }

    public static InMemoryFilmStorage getInstance() {
        if (obj == null) {
            obj = new InMemoryFilmStorage();
        }
        return obj;
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(Integer id) {
        if (!films.containsKey(id)) {
            throw new FilmNotFoundException("Фильма c ID " + id + "  в базе не существует");
        }
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
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

        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
        } else {
            throw new FilmNotFoundException("Такого фильма в базе не существует");
        }
        return film;
    }

    @Override
    public Boolean delete(Film film) {
        return null;
    }

}
