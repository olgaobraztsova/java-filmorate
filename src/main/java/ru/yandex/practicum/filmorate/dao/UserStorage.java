package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    Collection<User> getAllUsers();

    User create(User user);

    User update(User user);

    Boolean delete(User user);

    User getUserById(Integer id);

    boolean addFriend(Integer userId, Integer friendId);

    boolean removeFriend(Integer userId, Integer friendId);

    List<User> displayFriends(Integer id);

    List<User> displayCommonFriends(Integer userId, Integer userId2);

}
