package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public User getUserById(Integer id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        throw new UserNotFoundException("Пользователя с ID " + id + " не существует");
    }

    @Override
    public boolean addFriend(Integer userId, Integer friendId) {
        User user1 = getUserById(userId);
        User user2 = getUserById(friendId);

        Set<Integer> friendsUser1 = getFriendsSet(user1);
        Set<Integer> friendsUser2 = getFriendsSet(user2);

        friendsUser1.add(user2.getId());
        user1.setFriends(friendsUser1);
        friendsUser2.add(user1.getId());
        user2.setFriends(friendsUser2);

        return false;
    }

    @Override
    public boolean removeFriend(Integer userId, Integer friendId) {
//        User user1 = userStorage.getUserById(userId1);
//        User user2 = userStorage.getUserById(userId2);
//
//        // проверить, что пользователи друзья
//        if (!checkIfFriends(user1, user2)) {
//            throw new UserNotInFriendsListException("Пользователи с ID " + user1.getId() + " и "
//                    + user2.getId() + " не являются друзьями");
//        }
//
//        Set<Integer> friendsUser1 = user1.getFriends();
//        Set<Integer> friendsUser2 = user2.getFriends();
//
//        friendsUser1.remove(user2.getId());
//        user1.setFriends(friendsUser1);
//        friendsUser2.remove(user1.getId());
//        user2.setFriends(friendsUser2);
//
//        return user1;
        return false;
    }

    @Override
    public List<User> displayFriends(Integer id) {
        return null;
        //        User user = userStorage.getUserById(id);
//        Set<Integer> listOfFriendId = user.getFriends();
//        List<User> listOfFriends = new ArrayList<>();
//
//        if (listOfFriendId != null) {
//            for (Integer friendId : listOfFriendId) {
//                listOfFriends.add(userStorage.getUserById(friendId));
//            }
//        }
//        return listOfFriends;
    }

    @Override
    public List<User> displayCommonFriends(Integer userId, Integer userId2) {
        //        List<User> userFriends = displayFriends(userId1);
//        List<User> otherUserFriends = displayFriends(userId2);
//
//        if (userFriends.isEmpty() || otherUserFriends.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        return userFriends.stream().filter(otherUserFriends::contains).collect(Collectors.toList());
        return null;
    }

    @Override
    public User create(User user) {
        // создание ID пользователя
        if (user.getId() == null) {
            user.setId(getAllUsers().size() + 1);
        }
        if (users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь с электронной почтой " +
                    user.getEmail() + " уже зарегистрирован.");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
        } else {
            throw new UserNotFoundException("Такого пользователя не существует");
        }
        return user;
    }

    @Override
    public Boolean delete(User user) {
        return null;
    }

    private Set<Integer> getFriendsSet(User user) {
        Set<Integer> userFriends;
        if (user.getFriends() != null) {
            userFriends = user.getFriends();
        } else {
            userFriends = new HashSet<>();
        }
        return userFriends;
    }
}
