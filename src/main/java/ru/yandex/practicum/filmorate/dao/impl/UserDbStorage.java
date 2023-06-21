package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotInFriendsListException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component("userDbStorage")
@AllArgsConstructor
public class UserDbStorage implements UserStorage {

    JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> getAllUsers() {
        final String sqlQuery = "SELECT user_id, email, login, name, birthday, FROM users;";
        return jdbcTemplate.query(sqlQuery, UserDbStorage::construct);
    }

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO users (EMAIL, LOGIN, NAME, BIRTHDAY) values (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
                    stmt.setString(1, user.getEmail());
                    stmt.setString(2, user.getLogin());
                    stmt.setString(3, user.getName());
                    stmt.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
                    return stmt;
                },
                keyHolder);

        Integer idFromSql = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(idFromSql);
        user.setFriends(new HashSet<>());
        return user;
    }

    @Override
    public User update(User user) {
        // check if user exists
        if (getUserById(user.getId()) == null) {
            throw new UserNotFoundException("Пользователя с ID " + user.getId() + " не существует");
        }
        //check if friends list is empty in json
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        Set<Integer> currentFriends = getFriendsById(user.getId());
        Set<Integer> newFriends = user.getFriends();

        String sqlQuery = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(
                sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (!currentFriends.equals(newFriends)) {
            if (!currentFriends.isEmpty()) {
                for (Integer friendId : currentFriends) {
                    removeFriend(user.getId(), friendId);
                }
            }

            if (newFriends == null || !newFriends.isEmpty()) {
                for (Integer friendId : newFriends) {
                    addFriend(user.getId(), friendId);
                }
            } else {
                user.setFriends(new HashSet<>());
            }
        }
        return getUserById(user.getId());
    }

    @Override
    public Boolean delete(User user) {
        String sqlQuery = "DELETE FROM users WHERE user_id = ?";
        return jdbcTemplate.update(sqlQuery, user.getId()) > 0;
    }

    @Override
    public User getUserById(Integer id) {
        final String sqlQuery = "SELECT user_id, email, login, name, birthday, FROM users WHERE user_id = ?";
        final List<User> users = jdbcTemplate.query(sqlQuery, UserDbStorage::construct, id);

        if (users.isEmpty()) {
            throw new UserNotFoundException("Пользователя с ID " + id + " не существует");
        } else if (users.size() > 1) {
            throw new IllegalStateException();
        } else {
            // получить список друзей пользователя если они есть и добавить в объект пользователя
            Set<Integer> friendsList = new HashSet<>(getFriendsById(users.get(0).getId()));
            users.get(0).setFriends(friendsList);
            return users.get(0);
        }
    }

    @Override
    public boolean addFriend(Integer userId, Integer friendId) {
        // check if users exist
        if (getUserById(userId) == null) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (getUserById(friendId) == null) {
            throw new UserNotFoundException("Пользователь с ID " + friendId + " не найден");
        }

        // check if user is already in the friend list
        Set<Integer> userFriends = getFriendsById(userId);

        if (!userFriends.contains(friendId)) {
            final String sqlQuery = "INSERT INTO friendships (user_id, friend_Id) VALUES (?, ?);";
            return jdbcTemplate.update(sqlQuery, userId, friendId) > 1;
        } else {
            throw new IllegalArgumentException("Пользователь c ID " + friendId + " уже находится в списке друзей" +
                    " пользователя с ID " + userId);
        }
    }

    @Override
    public boolean removeFriend(Integer userId, Integer friendId) {
        if (getUserById(userId) == null) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (getUserById(friendId) == null) {
            throw new UserNotFoundException("Пользователь с ID " + friendId + " не найден");
        }
        //check if 'friendId' is in the friends list of userId
        Set<Integer> userFriends = getFriendsById(userId);
        if (userFriends.contains(friendId)) {
            final String sqlQuery = "DELETE FROM FRIENDSHIPS WHERE user_id = ? AND friend_id = ?;";
            return jdbcTemplate.update(sqlQuery, userId, friendId) > 1;
        } else {
            throw new UserNotInFriendsListException("Пользователь с ID " + friendId + " " +
                    "не является другом пользователя с ID " + userId);
        }
    }

    @Override
    public List<User> displayFriends(Integer id) {
        final String sqlQuery = "SELECT u.user_id AS user_id, " +
                "u.email AS email, " +
                "u.login AS login, " +
                "u.name AS name, " +
                "u.birthday AS birthday " +
                "FROM FRIENDSHIPS f " +
                "JOIN USERS u ON f.friend_id = u.user_id " +
                "WHERE f.user_id = ?;";

        return jdbcTemplate.query(sqlQuery, UserDbStorage::construct, id);
    }

    @Override
    public List<User> displayCommonFriends(Integer userId1, Integer userId2) {
        Set<Integer> userOneFriends = getFriendsById(userId1);
        Set<Integer> userTwoFriends = getFriendsById(userId2);

        List<User> commonFriends = new ArrayList<>();

        for (Integer userId : userOneFriends) {
            if (userTwoFriends.contains(userId)) {
                commonFriends.add(getUserById(userId));
            }
        }
        return commonFriends;
    }

    private static User construct(ResultSet rs, int rowNum) throws SQLException {
        Set<Integer> friendsList = new HashSet<>();

        return new User(rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate(),
                friendsList);
    }

    private Set<Integer> getFriendsById(Integer id) {
        // Запрос на получение списка друзей по ID
        String query = "SELECT friend_id FROM friendships WHERE user_id = ?";

        return new HashSet<>(jdbcTemplate.query(query, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("friend_id");
            }
        }, id));
    }
}
