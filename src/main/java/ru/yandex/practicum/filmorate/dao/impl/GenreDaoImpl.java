package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.FilmOrItsPartNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class GenreDaoImpl implements GenreDao {

    JdbcTemplate jdbcTemplate;

    @Override
    public Genre getGenreById(Integer id) {
        final String sqlQuery = "SELECT genre_id, genre_name FROM genres WHERE genre_id = ?";
        final List<Genre> genreList = jdbcTemplate.query(sqlQuery, GenreDaoImpl::construct, id);

        if (genreList.isEmpty()) {
            throw new FilmOrItsPartNotFoundException("Жанра с ID " + id + " не существует");
        } else if (genreList.size() > 1) {
            throw new IllegalStateException();
        } else {
            return genreList.get(0);
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query("SELECT genre_id, genre_name FROM genres ORDER BY genre_id ASC;",
                GenreDaoImpl::construct);
    }


    private static Genre construct(ResultSet rs, int rowNum) throws SQLException {
        try {
            return new Genre(
                    rs.getInt("genre_id"),
                    rs.getString("genre_name")
            );
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Ошибка создания объекта Genre");
    }
}
