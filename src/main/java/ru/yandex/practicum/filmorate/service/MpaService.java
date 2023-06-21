package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDao mpaDao;

    public Mpa getMpaById(Integer id) {
            return mpaDao.getMpaRatingById(id);
    }

    public List<Mpa> getAllMpaRatings() {
        return mpaDao.getAllRatings();
    }
}
