package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FilmMapper implements ResultSetExtractor<List<Film>> {
    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Integer, Film> map = new HashMap<>();

        while (rs.next()) {
            int filmId = rs.getInt("film_id");
            Film currentFilm = map.get(filmId);
            if (currentFilm == null) {
                currentFilm = new Film(rs.getInt("film_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("release_date").toLocalDate(),
                        rs.getInt("duration"),
                        new HashSet<>(),
                        new HashSet<>(),
                        new Mpa(rs.getInt("mpa_rating_id"), rs.getString("rating_code")));
                map.put(filmId, currentFilm);
                currentFilm = getGenresAndLikes(rs, currentFilm);
            } else if (currentFilm.getId() == filmId) {
                currentFilm = getGenresAndLikes(rs, currentFilm);
            }
            map.put(filmId, currentFilm);
        }
        return new ArrayList<>(map.values());
    }

    private Film getGenresAndLikes(ResultSet rs, Film currentFilm) throws SQLException {
        int genreId = rs.getInt("genre_id");
        Set<Genre> genreSet;

        if (genreId == 0) {
            genreSet = new HashSet<>();
        } else {
            genreSet = currentFilm.getGenres();
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
            genreSet.add(genre);
            currentFilm.setGenres(genreSet);
        }

        int likeUserId = rs.getInt("user_id");
        Set<Integer> likesSet;

        if (likeUserId == 0) {
            likesSet = new HashSet<>();
        } else {
            likesSet = currentFilm.getLikes();
            if (likeUserId != 0) {
                likesSet.add(likeUserId);
            }
            currentFilm.setLikes(likesSet);
        }
        return currentFilm;
    }
}
