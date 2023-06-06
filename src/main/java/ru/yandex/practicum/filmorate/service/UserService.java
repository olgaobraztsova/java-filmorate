package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotInFriendsListException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(Integer userId1, Integer userId2) {

        User user1 = userStorage.getUserById(userId1);
        User user2 = userStorage.getUserById(userId2);

        Set<Integer> friendsUser1 = getFriendsSet(user1);
        Set<Integer> friendsUser2 = getFriendsSet(user2);

        friendsUser1.add(user2.getId());
        user1.setFriends(friendsUser1);
        friendsUser2.add(user1.getId());
        user2.setFriends(friendsUser2);
    }


    public User removeFriend(Integer userId1, Integer userId2) {
        User user1 = userStorage.getUserById(userId1);
        User user2 = userStorage.getUserById(userId2);

        // проверить, что пользователи друзья
        if (!checkIfFriends(user1, user2)) {
            throw new UserNotInFriendsListException("Пользователи с ID " + user1.getId() + " и "
                    + user2.getId() + " не являются друзьями");
        }

        Set<Integer> friendsUser1 = user1.getFriends();
        Set<Integer> friendsUser2 = user2.getFriends();

        friendsUser1.remove(user2.getId());
        user1.setFriends(friendsUser1);
        friendsUser2.remove(user1.getId());
        user2.setFriends(friendsUser2);

        return user1;
    }

    public List<User> displayFriends(Integer id) {
        User user = userStorage.getUserById(id);
        Set<Integer> listOfFriendId = user.getFriends();
        List<User> listOfFriends = new ArrayList<>();

        if (listOfFriendId != null) {
            for (Integer friendId : listOfFriendId) {
                listOfFriends.add(userStorage.getUserById(friendId));
            }
        }
        return listOfFriends;
    }

    public List<User> displayCommonFriends(Integer userId1, Integer userId2) {
        List<User> userFriends = displayFriends(userId1);
        List<User> otherUserFriends = displayFriends(userId2);

        if (userFriends.isEmpty() || otherUserFriends.isEmpty()) {
            return new ArrayList<>();
        }

        return userFriends.stream().filter(otherUserFriends::contains).collect(Collectors.toList());
    }

    private boolean checkIfFriends(User user1, User user2) {
        Set<Integer> friendsUser1 = getFriendsSet(user1);
        Set<Integer> friendsUser2 = getFriendsSet(user2);

        if (!friendsUser1.contains(user2.getId()) || !friendsUser2.contains(user1.getId())) {
            return false;
        }
        return true;
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
