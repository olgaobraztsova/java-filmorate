package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmControllerTest {

    /*
    *   название не может быть пустым;
        максимальная длина описания — 200 символов;
        дата релиза — не раньше 28 декабря 1895 года;
        продолжительность фильма должна быть положительной.
    * */
    private FilmController filmController;

    @BeforeEach
    public void beforeEach() {
        filmController = new FilmController();
    }

    @Test
    public void failsWhenFilmNameIsEmpty() {
        // arrange
        Film film = new Film(1, null, "film about something",
                LocalDate.of(2020, 12, 10), 120);

        //act
        Exception exception = Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
        // assert
        Assertions.assertEquals("Название фильма не может быть пустым.", exception.getLocalizedMessage());
        Assertions.assertEquals(0, filmController.getAllFilms().size());

    }

    @Test
    public void failsWhenDescriptionIsOver200() {
        Film film = new Film(1, "Film name", "Avatar is an American media franchise created " +
                "by James Cameron, which consists of a planned series of epic science fiction films produced " +
                "by Lightstorm Entertainment and distributed by 20th Century Studio.",
                LocalDate.of(2020, 12, 10), 120);

        //act
        Exception exception = Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
        // assert
        Assertions.assertEquals("Описание фильма не может превышать 200 символов.", exception.getLocalizedMessage());
        Assertions.assertEquals(0, filmController.getAllFilms().size());

    }

    @Test
    public void failsWhenDateIsBefore28Dec1895() {
        Film film = new Film(1, "Film name", "Film description",
                LocalDate.of(1800, 12, 10), 120);

        //act
        Exception exception = Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
        // assert
        Assertions.assertEquals("Дата релиза фильма не может быть ранее дня рождения кино.",
                exception.getLocalizedMessage());
        Assertions.assertEquals(0, filmController.getAllFilms().size());
    }

    @Test
    public void failsWhenDurationIsNegative() {
        Film film = new Film(1, "Film name", "Film description",
                LocalDate.of(2000, 12, 10), -120);

        //act
        Exception exception = Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
        // assert
        Assertions.assertEquals("Продолжительность фильма не может быть отрицательной.",
                exception.getLocalizedMessage());
        Assertions.assertEquals(0, filmController.getAllFilms().size());
    }
}
