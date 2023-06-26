package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

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
        UserStorage userStorage = new InMemoryUserStorage();
        userController = new UserController(new UserService(userStorage));
    }

    @Test
    public void acceptsEmptyNameAndReplacesWithLoginValue() {
        // arrange
        User user = new User(1, "user@mail.ru","login123", "",
                LocalDate.of(2020,12,10), new HashSet<>());

        User expectedUser = new User(1, "user@mail.ru","login123", "login123",
                LocalDate.of(2020,12,10), new HashSet<>());
        //act
        userController.create(user);
        Collection<User> usersList = userController.getAllUsers();
        User actualUser = usersList.iterator().next();
        // assert
        Assertions.assertEquals(expectedUser, actualUser);
    }
}
