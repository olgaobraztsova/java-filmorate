package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

public class UserControllerTest {

    private UserController userController;

    /*
    *   электронная почта не может быть пустой и должна содержать символ @;
        логин не может быть пустым и содержать пробелы;
        имя для отображения может быть пустым — в таком случае будет использован логин;
        дата рождения не может быть в будущем.
    * */

    @BeforeEach
    public void beforeEach() {
        userController = new UserController();
    }


    @Test
    public void failsWhenEmailIsEmpty() {
        // arrange
        User user = new User(1, "","login123", "User name",
                LocalDate.of(2020,12,10));

        //act
        Exception exception = Assertions.assertThrows(ValidationException.class, () -> userController.create(user));
        // assert
        Assertions.assertEquals("Адрес электронной почты не может быть пустым.", exception.getLocalizedMessage());
        Assertions.assertEquals(0, userController.getAllUsers().size());
    }

    @Test
    public void failsWhenEmailIsIncorrect() {
        // arrange
        User user = new User(1, "user.mail.ru","login123", "User name",
                LocalDate.of(2020,12,10));

        //act
        Exception exception = Assertions.assertThrows(ValidationException.class, () -> userController.create(user));
        // assert
        Assertions.assertEquals("Некорректный адрес электронной почты.", exception.getLocalizedMessage());
        Assertions.assertEquals(0, userController.getAllUsers().size());
    }

    @Test
    public void failsWhenLoginIsIncorrect(){
        // arrange
        User user = new User(1, "user@mail.ru","login 123", "User name",
                LocalDate.of(2020,12,10));

        //act
        Exception exception = Assertions.assertThrows(ValidationException.class, () -> userController.create(user));
        // assert
        Assertions.assertEquals("Некорректно заданный логин: значение пустое либо содержит пробел.",
                exception.getLocalizedMessage());
        Assertions.assertEquals(0, userController.getAllUsers().size());
    }

    @Test
    public void acceptsEmptyNameAndReplacesWithLoginValue() {
        // arrange
        User user = new User(1, "user@mail.ru","login123", "",
                LocalDate.of(2020,12,10));

        User expectedUser = new User(1, "user@mail.ru","login123", "login123",
                LocalDate.of(2020,12,10));
        //act
        userController.create(user);
        Collection<User> usersList = userController.getAllUsers();
        User actualUser = usersList.iterator().next();
        // assert
        Assertions.assertEquals(expectedUser, actualUser);
    }


    @Test
    public void failsWhenBirthdateIsInFuture() {
        // arrange
        User user = new User(1, "user@mail.ru","login123", "User name",
                LocalDate.of(2024,12,10));

        //act
        Exception exception = Assertions.assertThrows(ValidationException.class, () -> userController.create(user));
        // assert
        Assertions.assertEquals("Дата рождения пользователя не может быть в будущем",
                exception.getLocalizedMessage());
        Assertions.assertEquals(0, userController.getAllUsers().size());
    }
}
