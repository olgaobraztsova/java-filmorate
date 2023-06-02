package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("users")
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserService userService, UserStorage userStorage) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") String id) {
        return userStorage.getUserById(Integer.parseInt(id));
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        User validatedSavedUser = userStorage.create(validateUser(user));
        log.info("Сохранен пользователь {}", validatedSavedUser);
        return validatedSavedUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        User validatedSavedUser = userStorage.update(validateUser(user));
        log.info("Обновлен пользователь {}", validatedSavedUser);
        return validatedSavedUser;
    }

    //PUT /users/{id}/friends/{friendId} — добавление в друзья.
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Пользователи с ID={} и ID={} стали друзьями", id, friendId);
        userService.addFriend(id, friendId);
    }

    // DELETE /users/{id}/friends/{friendId} — удаление из друзей.
    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Пользователи с ID={} и ID={} теперь не друзья", id, friendId);
        return userService.removeFriend(id, friendId);
    }

    //GET /users/{id}/friends — возвращаем список пользователей, являющихся его друзьями.
    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Integer id) {
        return userService.displayFriends(id);
    }

    // GET /users/{id}/friends/common/{otherId} — список друзей, общих с другим пользователем.
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.displayCommonFriends(id, otherId);
    }

    // имя для отображения может быть пустым — в таком случае будет использован логин
    private User validateUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            log.info("Используется логин вместо пустого поля имени: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getId() == null) {
            user.setId(userStorage.getAllUsers().size() + 1);
        }
        return user;
    }
}
