package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user){
        User validatedUser = validateUser(user);
        if (users.containsKey(validatedUser.getId())) {
            throw new ValidationException("Пользователь с электронной почтой " +
                    user.getEmail() + " уже зарегистрирован.");
        }
        users.put(validatedUser.getId(), validatedUser);
        log.info("Сохранен объект {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        User validatedUser = validateUser(user);
        if (users.containsKey(validatedUser.getId())) {
            users.put(validatedUser.getId(), validatedUser);
            log.info("Обновлен объект {}", user);
        } else {
            throw new ValidationException("Такого пользователя не существует");
        }

        return user;
    }

    /*
    *   электронная почта не может быть пустой и должна содержать символ @;
        логин не может быть пустым и содержать пробелы;
        имя для отображения может быть пустым — в таком случае будет использован логин;
        дата рождения не может быть в будущем.
    * */
    private User validateUser(User user) {
        if (user.getEmail().isBlank()) {
            log.error("Передано пустое значение электронной почты: {}", user.getEmail());
            throw new ValidationException("Адрес электронной почты не может быть пустым.");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Передан некорректный адрес электронной почты: {}", user.getEmail());
            throw new ValidationException("Некорректный адрес электронной почты.");
        }
        if (user.getLogin().isBlank() || user.getLogin().split(" ").length > 1) {
            log.error("Некорректно заданный логин: значение пустое либо содержит пробел: {}", user.getLogin());
            throw new ValidationException("Некорректно заданный логин: значение пустое либо содержит пробел.");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения пользователя не может быть в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения пользователя не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            log.info("Используется логин вместо пустого поля имени: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getId() == null) {
            user.setId(users.size() + 1);
        }
        return user;
    }
}
