package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUser(String id) {
        return userStorage.getUserById(Integer.parseInt(id));
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void addFriend(Integer userId1, Integer userId2) {
        userStorage.addFriend(userId1, userId2);
    }

    public void removeFriend(Integer userId1, Integer userId2) {
        userStorage.removeFriend(userId1, userId2);
    }

    public List<User> displayFriends(Integer id) {
        return userStorage.displayFriends(id);
    }

    public List<User> displayCommonFriends(Integer userId1, Integer userId2) {
        return userStorage.displayCommonFriends(userId1, userId2);
    }
}
