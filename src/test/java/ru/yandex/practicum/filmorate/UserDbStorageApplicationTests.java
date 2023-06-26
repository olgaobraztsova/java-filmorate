package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "/create-data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserDbStorageApplicationTests {

    @Autowired
    @Qualifier("userDbStorage")
    UserStorage userStorage;

    @Test
    public void testCreateUser() {
        User expectedUser = new User(5,
                "user@mail.ru",
                "user login",
                "user name",
                LocalDate.of(2000, 10, 5),
                new HashSet<Integer>()
        );
        User actualUser = userStorage.create(expectedUser);
        assertThat(actualUser).usingRecursiveComparison()
                .ignoringFields("userId").isEqualTo(expectedUser);
    }

    @Test
    public void testUserUpdate() {
        User expectedUser = new User(1,
                "user@mail.ru",
                "user login",
                "user name",
                LocalDate.of(2000, 10, 5),
                new HashSet<Integer>(List.of(3))
        );
        User actualUser = userStorage.update(expectedUser);
        assertThat(actualUser).usingRecursiveComparison()
                .ignoringFields("userId").isEqualTo(expectedUser);
    }

    @Test
    public void testGetAllUsers() {
        Collection<User> users = userStorage.getAllUsers();
        assertThat(users.size()).isEqualTo(4);
    }

    @Test
    public void testGetUserById() {
        User user = userStorage.getUserById(1);
        assertThat(user).hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    public void testAddFriend() {
        User expectedUser = new User(1,
                "antonuser1@mail.ru",
                "anton-login1",
                "Anton",
                LocalDate.of(2000, 10, 1),
                new HashSet<Integer>(List.of(2, 3))
        );
        userStorage.addFriend(expectedUser.getId(), 3);
        User actualUser = userStorage.getUserById(expectedUser.getId());
        assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    public void testRemoveFriend() {
        User expectedUser = new User(1,
                "antonuser1@mail.ru",
                "anton-login1",
                "Anton",
                LocalDate.of(2000, 10, 1),
                new HashSet<Integer>()
        );
        userStorage.removeFriend(expectedUser.getId(), 2);
        User actualUser = userStorage.getUserById(expectedUser.getId());
        assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    public void testDisplayFriends() {
        List<User> friendsList = userStorage.displayFriends(1);
        assertEquals("Alex", friendsList.stream().findAny().get().getName());
    }

    @Test
    public void testDisplayCommonFriends() {
        List<User> friendsList = userStorage.displayCommonFriends(1, 3);
        assertEquals("Alex", friendsList.stream().findAny().get().getName());
    }
}
