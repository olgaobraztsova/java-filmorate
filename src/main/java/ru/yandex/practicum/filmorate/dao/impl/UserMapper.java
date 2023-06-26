package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserMapper implements ResultSetExtractor<List<User>> {
    @Override
    public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Integer, User> map = new HashMap<>();

        while (rs.next()) {
            int userId = rs.getInt("user_id");
            User currentUser = map.get(userId);
            if (currentUser == null) {
                currentUser = new User(rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("login"),
                        rs.getString("name"),
                        rs.getDate("birthday").toLocalDate(),
                        new HashSet<>());
                map.put(userId, currentUser);

                currentUser = getFriends(rs, currentUser);

            } else if (currentUser.getId() == userId) {
                currentUser = getFriends(rs, currentUser);
            }
            map.put(userId, currentUser);
        }
        return new ArrayList<>(map.values());
    }

    private User getFriends(ResultSet rs, User currentUser) throws SQLException {
        Set<Integer> friends = currentUser.getFriends();
        if (rs.getInt("friend_id") != 0) {
                friends.add(rs.getInt("friend_id"));

            currentUser.setFriends(friends);
        }
        return currentUser;
    }
}
