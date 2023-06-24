package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "/create-data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationTests {

    @Autowired
    @Qualifier("filmDbStorage")
    FilmStorage filmStorage;

    @Test
    void contextLoads() {
    }

    @Test
    public void testGetFilmById() {
        Film film = filmStorage.getFilmById(1);
        assertThat(film).hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    public void testGetAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();
        assertThat(films.size()).isEqualTo(5);
    }

    @Test
    public void testCreateFilm() {
        Film expectedFilm = new Film(6,
                "Film name",
                "description",
                LocalDate.of(2020, 10, 5),
                120,
                new HashSet<Integer>(),
                new HashSet<Genre>(),
                new Mpa(1, "G"));
        Film actualFilm = filmStorage.create(expectedFilm);
        assertThat(actualFilm).usingRecursiveComparison()
                .ignoringFields("filmId").isEqualTo(expectedFilm);
    }

    @Test
    public void testUpdateFilm() {
        Film expectedFilm = new Film(1,
                "Film name",
                "description",
                LocalDate.of(2020, 10, 5),
                120,
                new HashSet<Integer>(),
                new HashSet<Genre>(),
                new Mpa(1, "G"));

        Film actualFilm = filmStorage.update(expectedFilm);
        assertThat(actualFilm).usingRecursiveComparison()
                .ignoringFields("filmId").isEqualTo(expectedFilm);
    }

    @Test
    public void testAddLike() {
        HashSet<Integer> expectedLikes = new HashSet<>(Arrays.asList(1, 2, 3));

        Film expectedFilm = new Film(1,
                "Inception",
                "description",
                LocalDate.of(2010, 6, 8),
                148,
                expectedLikes,
                new HashSet<Genre>(),
                new Mpa(3, "PG-13"));

        filmStorage.addLike(1, 3);
        Film actualFilm = filmStorage.getFilmById(1);
        assertThat(actualFilm).usingRecursiveComparison()
                .ignoringFields("filmId", "name", "description", "genres").isEqualTo(expectedFilm);
    }

    @Test
    public void testRemoveLike() {
        HashSet<Integer> expectedLikes = new HashSet<>(Arrays.asList(1));

        //filmStorage.addLikes(1, new HashSet<>(Arrays.asList(1, 2, 3)));
        Film expectedFilm = new Film(1,
                "Inception",
                "description",
                LocalDate.of(2010, 6, 8),
                148,
                expectedLikes,
                new HashSet<Genre>(),
                new Mpa(3, "PG-13"));

        filmStorage.removeLike(1, 2);
        Film actualFilm = filmStorage.getFilmById(1);
        assertThat(actualFilm).usingRecursiveComparison()
                .ignoringFields("filmId", "description", "genres").isEqualTo(expectedFilm);
    }

    @Test
    public void testGetMostPopularFilms() {
        List<Film> mostPopularFilms = filmStorage.getMostPopularFilms(1);
        assertEquals("Inception", mostPopularFilms.stream().findAny().get().getName());
    }
}
