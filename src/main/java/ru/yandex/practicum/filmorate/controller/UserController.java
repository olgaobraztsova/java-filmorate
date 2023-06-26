package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") String id) {
        return userService.getUser(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        User validatedSavedUser = userService.create(validateUser(user));
        log.debug("Сохранен пользователь {}", validatedSavedUser);
        return validatedSavedUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        User validatedSavedUser = userService.update(validateUser(user));
        log.debug("Обновлен пользователь {}", validatedSavedUser);
        return validatedSavedUser;
    }

    //PUT /users/{id}/friends/{friendId} — добавление в друзья.
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.debug("Пользователи с ID={} и ID={} стали друзьями", id, friendId);
        userService.addFriend(id, friendId);
    }

    // DELETE /users/{id}/friends/{friendId} — удаление из друзей.
    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.debug("Пользователи с ID={} и ID={} теперь не друзья", id, friendId);
        userService.removeFriend(id, friendId);
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
            log.debug("Используется логин вместо пустого поля имени: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        return user;
    }
}
