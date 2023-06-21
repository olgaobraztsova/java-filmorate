package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.FilmOrItsPartNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class MpaDaoImpl implements MpaDao {

    JdbcTemplate jdbcTemplate;

    @Override
    public Mpa getMpaRatingById(Integer id) {
        final String sqlQuery = "SELECT rating_id, rating_code FROM mpa_ratings WHERE rating_id = ?";
        final List<Mpa> mpaList = jdbcTemplate.query(sqlQuery, MpaDaoImpl::construct, id);

        if (mpaList.isEmpty()) {
            throw new FilmOrItsPartNotFoundException("Рейтинга с ID " + id + " не существует");
        } else if (mpaList.size() > 1) {
            throw new IllegalStateException();
        } else {
            return mpaList.get(0);
        }
    }

    @Override
    public List<Mpa> getAllRatings() {
        return jdbcTemplate.query("SELECT rating_id, rating_code FROM mpa_ratings ORDER BY rating_id ASC",
                MpaDaoImpl::construct);
    }

    private static Mpa construct(ResultSet rs, int rowNum) throws SQLException {
        try {
            return new Mpa(
                    rs.getInt("rating_id"),
                    rs.getString("rating_code")
            );
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Ошибка создания объекта Mpa");
    }
}
