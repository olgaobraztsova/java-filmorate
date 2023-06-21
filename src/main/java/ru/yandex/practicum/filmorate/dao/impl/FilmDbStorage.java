package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.FilmOrItsPartNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component("filmDbStorage")
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;

    @Override
    public Collection<Film> getAllFilms() {
        Collection<Film> films = jdbcTemplate.query("SELECT * FROM films", FilmDbStorage::construct);
        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setMpa(mpaDao.getMpaRatingById(film.getMpa().getId()));
            film.setLikes(getLikesByFilmId(film.getId()));
        }
        return films;
    }

    @Override
    public Film create(Film film) {
        String sqlQuery = "INSERT INTO films (title, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
                    stmt.setString(1, film.getName());
                    stmt.setString(2, film.getDescription());
                    stmt.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
                    stmt.setInt(4, film.getDuration());
                    stmt.setInt(5, film.getMpa().getId());
                    return stmt;
                },
                keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        film.setMpa(mpaDao.getMpaRatingById(film.getMpa().getId()));

        if (film.getGenres() != null) {
            film.setGenres(updateFilmGenres(film.getId(), film.getGenres()));
        } else {
            film.setGenres(new HashSet<>());
        }

        if (film.getLikes() != null) {
            Set<Integer> likes = new HashSet<>();
            for (Integer userId : film.getLikes()) {
                likes.add(addLike(film.getId(), userId));
            }
            film.setLikes(likes);
        } else {
            film.setLikes(new HashSet<>());
        }

        return film;
    }

    @Override
    public Film update(Film film) {

        // check if film exists
        if (getFilmById(film.getId()) == null) {
            throw new FilmOrItsPartNotFoundException("Фильма с ID " + film.getId() + " не существует");
        }


        String sqlQuery =
                "UPDATE films SET title = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE film_id = ?";

        //update film
        int updatedFilmId = jdbcTemplate.update(
                sqlQuery,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (updatedFilmId != 0) {
            // update mpa rating and likes
            film.setMpa(mpaDao.getMpaRatingById(film.getMpa().getId()));

            //update genres
            if (film.getGenres() != null) {
                Set<Genre> currentGenres = getGenresByFilmId(film.getId());
                Set<Genre> newGenres = film.getGenres();
                if (!newGenres.equals(currentGenres)) {
                    //delete current genres from the table
                    deleteFilmGenres(film.getId(), currentGenres);
                    // insert new genres to the table (if any)
                    updateFilmGenres(film.getId(), newGenres);
                }
            } else {
                film.setGenres(new HashSet<>());
            }

            // update likes
            if (film.getLikes() != null) {
                Set<Integer> currentLikes = getLikesByFilmId(film.getId());
                Set<Integer> newLikes = film.getLikes();
                if (!currentLikes.equals(newLikes)) {
                    // delete old likes
                    deleteFilmLikes(film.getId(), currentLikes);
                    // update new likes
                    Set<Integer> likesAdded = new HashSet<>();
                    for (Integer userId : newLikes) {
                        likesAdded.add(addLike(film.getId(), userId));
                    }
                    film.setLikes(likesAdded);
                }
            } else {
                film.setLikes(new HashSet<>());
            }

            return getFilmById(film.getId());
        } else {
            throw new FilmOrItsPartNotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
    }

    @Override
    public Film getFilmById(Integer id) {

        final String sqlQuery =
                "SELECT film_id, title, description, release_date, duration, mpa_rating_id FROM films WHERE film_id = ?";

        final List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::construct, id);

        if (films.isEmpty()) {
            throw new FilmOrItsPartNotFoundException("Фильма с ID " + id + " не существует");
        } else if (films.size() > 1) {
            throw new IllegalStateException();
        } else {
            Film film = films.get(0);
            film.setLikes(getLikesByFilmId(film.getId()));
            film.setMpa(mpaDao.getMpaRatingById(film.getMpa().getId()));
            film.setGenres(getGenresByFilmId(film.getId()));
            return film;
        }
    }

    @Override
    public Integer addLike(Integer filmId, Integer userId) {
        String sqlQuery = "INSERT INTO likes(film_id, user_id) VALUES(?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id", "user_id"});
                    stmt.setInt(1, filmId);
                    stmt.setInt(2, userId);
                    return stmt;
                },
                keyHolder);

        return userId;
    }

    @Override
    public boolean removeLike(Integer filmId, Integer userId) {
        //check if film exists
        if (getFilmById(filmId) == null) {
            throw new FilmOrItsPartNotFoundException("Фильма с ID " + userId + " не найдено");
        }
        // check if user id is valid number
        if (userId < 1) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден");
        }
        //check if the film has a like from the user
        Set<Integer> filmLikes = getLikesByFilmId(filmId);
        if (filmLikes.contains(userId)) {
            final String sqlQuery = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";
            return jdbcTemplate.update(sqlQuery, filmId, userId) > 1;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "У фильма с ID " + filmId +
                    " нет лайков от пользователя с ID " + userId);
        }
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        final String sqlQuery = "SELECT l.film_id, " +
                "f.title, " +
                "f.DESCRIPTION, " +
                "f.RELEASE_DATE," +
                "f.DURATION, " +
                "f.MPA_RATING_ID " +
                "FROM LIKES l " +
                "JOIN films f ON l.FILM_ID = f.FILM_ID " +
                "GROUP BY l.FILM_ID " +
                "ORDER BY COUNT(l.film_id) DESC " +
                "LIMIT ?;";

        List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::construct, count);
        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setMpa(mpaDao.getMpaRatingById(film.getMpa().getId()));
            film.setLikes(getLikesByFilmId(film.getId()));
        }
        if (films.isEmpty()) {
            return new ArrayList<>(getAllFilms());
        }
        return films;
    }

    private static Film construct(ResultSet rs, int rowNum) throws SQLException {
        try {
            return new Film(
                    rs.getInt("film_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new HashSet<>(),
                    new HashSet<>(),
                    new Mpa(rs.getInt("mpa_rating_id"), null)
            );
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Ошибка в конструкторе объекта Film");
    }

    private Set<Genre> getGenresByFilmId(Integer id) {
        // Запрос на получение списка жанров по ID фильма
        String query = "SELECT g.genre_id, g.genre_name FROM genres g JOIN films_and_genres fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";

        return new HashSet<Genre>(jdbcTemplate.query(query, new RowMapper<Genre>() {
            @Override
            public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
            }
        }, id));
    }

    private Set<Integer> getLikesByFilmId(Integer id) {
        String query = "SELECT user_id FROM likes WHERE film_id = ?";

        return new HashSet<Integer>(jdbcTemplate.query(query, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("user_id");
            }
        }, id));
    }

    private Set<Genre> updateFilmGenres(Integer filmId, Set<Genre> genres) {
        String sqlQuery = "INSERT INTO films_and_genres(film_id, genre_Id) VALUES(?, ?);";
        Set<Genre> addedGenres = new HashSet<>();

        try {
            for (Genre genre : genres) {
                KeyHolder keyHolder = new GeneratedKeyHolder();

                jdbcTemplate.update(
                        connection -> {
                            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id", "genre_id"});
                            stmt.setInt(1, filmId);
                            stmt.setInt(2, genre.getId());
                            return stmt;
                        },
                        keyHolder);

                Genre addedGenre = genreDao.getGenreById(genre.getId());
                addedGenres.add(addedGenre);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return addedGenres;
    }

    private void deleteFilmGenres(Integer filmId, Set<Genre> genres) {
        String sqlQuery = "DELETE FROM films_and_genres WHERE film_id = ? AND genre_id = ?;";

        for (Genre genre : genres) {
            jdbcTemplate.update(sqlQuery, filmId, genre.getId());
        }
    }

    private void deleteFilmLikes(Integer filmId, Set<Integer> likes) {
        String sqlQuery = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";

        for (Integer userId : likes) {
            jdbcTemplate.update(sqlQuery, filmId, userId);
        }
    }
}
