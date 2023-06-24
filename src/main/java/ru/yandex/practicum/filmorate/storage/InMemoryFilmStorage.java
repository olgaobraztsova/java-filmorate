package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.FilmOrItsPartNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private final UserStorage userStorage = new InMemoryUserStorage();

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(Integer id) {
        if (!checkIfFilmExists(id)) {
            throw new FilmOrItsPartNotFoundException("Фильма c ID " + id + "  в базе не существует");
        }
        return films.get(id);
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilmById(filmId);
        Set<Integer> likesToAdd = film.getLikes();

        likesToAdd.add(userId);

        // добавить лайк и обновить фильм в базе
        film.setLikes(likesToAdd);
        update(film);
    }

    @Override
    public boolean removeLike(Integer filmId, Integer userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        // удалить лайк и обновить фильм в базе
        Set<Integer> likes = film.getLikes();
        if (likes.contains(userId)) {
            likes.remove(user.getId());
            film.setLikes(likes);
            update(film);
            return true;
        }
        return false;
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        Collection<Film> films = getAllFilms();
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
            throw new FilmOrItsPartNotFoundException("Фильма c ID " + id + "  в базе не существует");
        }
        return true;
    }

    private int compare(Film f1, Film f2) {
        if (f1.getLikes() != null && f2.getLikes() != null) {
            return f2.getLikes().size() - f1.getLikes().size();
        }
        return 0;
    }
}
