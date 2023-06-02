package ru.yandex.practicum.filmorate.exception;

public class UserNotInFriendsListException extends RuntimeException {
    public UserNotInFriendsListException(String message) {
        super(message);
    }
}
