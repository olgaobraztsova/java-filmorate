package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage = InMemoryFilmStorage.getInstance();
    private final UserStorage userStorage = InMemoryUserStorage.getInstance();

    public Film addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        // добавить лайк и обновить фильм в базе
        Set<Integer> likes = film.getLikes();
        likes.add(user.getId());
        film.setLikes(likes);
        filmStorage.update(film);
        return film;
    }

    public Film removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        // удалить лайк и обновить фильм в базе
        Set<Integer> likes = film.getLikes();
        if (likes.contains(userId)) {
            likes.remove(user.getId());
            film.setLikes(likes);
            filmStorage.update(film);
        }
        return film;
    }

    public List<Film> getMostLikedFilms(Integer count) {

        Collection<Film> films = filmStorage.getAllFilms();
        List<Film> sortedFilmList = new ArrayList<>();

        if (films.size() == 1) {
            return new ArrayList<>(films);
        }

        if (films.size() > 1) {
            sortedFilmList = films.stream()
                    .sorted(this::compare)
                    .limit(count)
                    .collect(Collectors.toList());
        }
        return sortedFilmList;
    }

    private int compare(Film f1, Film f2) {
        if (f1.getLikes() != null && f2.getLikes() != null) {
            return f2.getLikes().size() - f1.getLikes().size();
        }
        return 0;
    }
}
