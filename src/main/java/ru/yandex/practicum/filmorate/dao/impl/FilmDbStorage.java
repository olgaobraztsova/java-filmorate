package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.exception.FilmOrItsPartNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component("filmDbStorage")
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> getAllFilms() {

        final String sqlQuery =
                "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
                        "l.USER_ID, fg.genre_id, g.genre_name, f.mpa_rating_id, mr.rating_code " +
                        "FROM films f " +
                        "LEFT JOIN likes l ON l.film_id = f.film_id " +
                        "LEFT JOIN films_and_genres fg ON f.film_id = fg.film_id " +
                        "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                        "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.rating_id " +
                        "GROUP BY f.film_id, fg.genre_id, l.user_id " +
                        "ORDER BY film_id ASC;";

        return jdbcTemplate.query(sqlQuery, new FilmMapper());
    }

    @Override
    public Film create(Film film) {
        final String sqlQuery = "INSERT INTO films (title, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";

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

        if (film.getGenres() != null) {
            addFilmGenres(film.getId(), film.getGenres());
        } else {
            film.setGenres(new HashSet<>());
        }

        if (film.getLikes() != null) {
            Set<Integer> likes = film.getLikes();
            addMultipleLikes(film.getId(), likes);
        } else {
            film.setLikes(new HashSet<>());
        }

        return getFilmById(film.getId());
    }

    @Override
    public Film update(Film film) {

        // get the film from DB or check if film exists
        Film filmFromDb = getFilmById(film.getId()); //method throws exception if film doesn't exist

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
            //update genres
            if (film.getGenres() != null) {
                Set<Genre> currentGenres = filmFromDb.getGenres();
                Set<Genre> newGenres = film.getGenres();
                if (!newGenres.equals(currentGenres)) {
                    //delete current genres from the table
                    deleteFilmGenres(film.getId(), currentGenres);
                    // insert new genres to the table (if any)
                    addFilmGenres(film.getId(), newGenres);
                }
            } else {
                film.setGenres(new HashSet<>());
            }

            // update likes
            if (film.getLikes() != null) {
                Set<Integer> currentLikes = filmFromDb.getLikes();
                Set<Integer> newLikes = film.getLikes();
                if (!currentLikes.equals(newLikes)) {
                    // delete old likes
                    deleteFilmLikes(film.getId(), currentLikes);
                    // update new likes
                    addMultipleLikes(film.getId(), newLikes);
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
                "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
                        "l.USER_ID, fg.genre_id, g.genre_name, f.mpa_rating_id, mr.rating_code " +
                        "FROM films f " +
                        "LEFT JOIN likes l ON l.film_id = f.film_id " +
                        "LEFT JOIN films_and_genres fg ON f.film_id = fg.film_id " +
                        "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                        "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.rating_id " +
                        "WHERE f.film_id = ? " +
                        "GROUP BY f.film_id, fg.genre_id, l.user_id;";

        final List<Film> films = jdbcTemplate.query(sqlQuery, new FilmMapper(), id);

        assert films != null;
        if (films.isEmpty()) {
            throw new FilmOrItsPartNotFoundException("Фильма с ID " + id + " не существует");
        } else if (films.size() > 1) {
            throw new IllegalStateException();
        } else {
            return films.get(0);
        }
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
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
        final String sqlQuery =
                "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
                        "l.USER_ID, fg.genre_id, g.genre_name, f.mpa_rating_id, mr.rating_code " +
                        "FROM films f " +
                        "LEFT JOIN likes l ON l.film_id = f.film_id " +
                        "LEFT JOIN films_and_genres fg ON f.film_id = fg.film_id " +
                        "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                        "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.rating_id " +
                        "GROUP BY f.film_id, fg.genre_id, l.user_id " +
                        "ORDER BY COUNT(l.film_id) DESC " +
                        "LIMIT ?;";

        List<Film> films = jdbcTemplate.query(sqlQuery, new FilmMapper(), count);

        return films;
    }

    private Set<Integer> getLikesByFilmId(Integer id) {
        String query = "SELECT user_id FROM likes WHERE film_id = ?";
        System.out.println();
        return new HashSet<Integer>(jdbcTemplate.query(query, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("user_id");
            }
        }, id));
    }

    private void addFilmGenres(Integer filmId, Set<Genre> genres) {
        String sqlQuery = "INSERT INTO films_and_genres(film_id, genre_Id) VALUES(?, ?);";

        List<Genre> listOfGenres = new ArrayList<>();
        listOfGenres.addAll(genres);

        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, listOfGenres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    private void deleteFilmGenres(Integer filmId, Set<Genre> genres) {
        String sqlQuery = "DELETE FROM films_and_genres WHERE film_id = ? AND genre_id = ?;";

        List<Genre> listOfGenres = new ArrayList<>(genres);

        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, listOfGenres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }


    private void addMultipleLikes(Integer filmId, Set<Integer> likes) {
        String sqlQuery = "INSERT INTO likes(film_id, user_id) VALUES(?, ?);";

        List<Integer> listOfLikes = new ArrayList<>(likes);

        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, listOfLikes.get(i));
            }

            @Override
            public int getBatchSize() {
                return likes.size();
            }
        });

    }

    private void deleteFilmLikes(Integer filmId, Set<Integer> likes) {
        String sqlQuery = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";

        List<Integer> listOfLikes = new ArrayList<>(likes);

        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, listOfLikes.get(i));
            }

            @Override
            public int getBatchSize() {
                return likes.size();
            }
        });
    }
}
