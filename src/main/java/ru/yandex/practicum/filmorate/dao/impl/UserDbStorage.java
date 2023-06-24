package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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
        final String sqlQuery =
                "SELECT u.user_id, u.email, u.login, u.name, u.birthday, f.friend_id " +
                        "FROM users u " +
                        "LEFT JOIN friendships f ON f.user_id = u.user_id " +
                        "ORDER BY user_id ASC;";

        return jdbcTemplate.query(sqlQuery, new UserMapper());
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
        User userFromDb = getUserById(user.getId()); // method throws exception if user doesn't exist

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        Set<Integer> currentFriends = userFromDb.getFriends();
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
                updateFriends(userFromDb, currentFriends, newFriends);
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
        final String sqlQuery =
                "SELECT u.user_id, u.email, u.login, u.name, u.birthday, f.friend_id " +
                        "FROM users u " +
                        "LEFT JOIN friendships f ON f.user_id = u.user_id " +
                        "WHERE u.user_id = ?" +
                        "ORDER BY u.user_id ASC;";

        final List<User> users = jdbcTemplate.query(sqlQuery, new UserMapper(), id);

        assert users != null;
        if (users.isEmpty()) {
            throw new UserNotFoundException("Пользователя с ID " + id + " не существует");
        } else if (users.size() > 1) {
            throw new IllegalStateException();
        } else {
            return users.get(0);
        }
    }

    @Override
    public boolean addFriend(Integer userId, Integer friendId) {

        // check if users exist
        User user = getUserById(userId); // method throws exception if user doesn't exist
        User friend = getUserById(friendId); // method throws exception if user doesn't exist

        // check if user is already in the friend list
        Set<Integer> userFriends = user.getFriends();

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
        User user = getUserById(userId); // method throws exception if user doesn't exist

        //check if 'friendId' is in the friends list of userId
        Set<Integer> userFriends = user.getFriends();
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
        final String sqlQuery =
                "SELECT u.user_id, u.email, u.login, u.name, u.birthday, f.friend_id " +
                        "FROM users u " +
                        "LEFT JOIN friendships f ON u.USER_ID = f.USER_ID " +
                        "WHERE u.user_id IN (SELECT friend_id " +
                        "                   FROM friendships " +
                        "                   WHERE user_id = ?);";

        return jdbcTemplate.query(sqlQuery, new UserMapper(), id);
    }

    @Override
    public List<User> displayCommonFriends(Integer userId1, Integer userId2) {

        final String sqlQuery =
                "SELECT u.user_id, u.email, u.login, u.name, u.birthday, f.friend_id " +
                        "FROM users u " +
                        "LEFT JOIN friendships f ON u.USER_ID = f.USER_ID " +
                        "WHERE u.user_id IN (SELECT f1.friend_id " +
                        "                    FROM   (SELECT * " +
                        "                            FROM FRIENDSHIPS " +
                        "                            WHERE user_id = ?) f1 INNER JOIN ( " +
                        "                                       SELECT * " +
                        "                                       FROM FRIENDSHIPS " +
                        "                                       WHERE user_id = ?) f2 " +
                        "                                       ON f1.friend_id = f2.friend_id);";

        return jdbcTemplate.query(sqlQuery, new UserMapper(), userId1, userId2);
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

    private void updateFriends(User user, Set<Integer> currentFriends, Set<Integer> newFriends) {
        List<Integer> currentFriendsList = new ArrayList<>(currentFriends);
        List<Integer> newFriendsList = new ArrayList<>(newFriends);
        clearUserFriends(user);

        final String sqlQuery = "INSERT INTO friendships (user_id, friend_Id) VALUES (?, ?);";

        if (!newFriendsList.isEmpty()) {
            jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, user.getId());
                    ps.setInt(2, newFriendsList.get(i));
                }

                @Override
                public int getBatchSize() {
                    return newFriendsList.size();
                }
            });
        }
    }

    private void clearUserFriends(User user) {
        final String sqlQuery = "DELETE FROM FRIENDSHIPS WHERE user_id = ?;";

        List<Integer> currentFriendsList = new ArrayList<>(user.getFriends());

        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, user.getId());
            }

            @Override
            public int getBatchSize() {
                return currentFriendsList.size();
            }
        });
    }
}
