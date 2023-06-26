package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.FilmOrItsPartNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotInFriendsListException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@RestControllerAdvice
public class ErrorHandler {

    /*
    Для исключений FilmNotFoundException и UserNotFoundException возвращается код ответа 404
    * */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePostNotFound(final FilmOrItsPartNotFoundException e) {
        // возвращаем сообщение об ошибке
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePostNotFound(final UserNotFoundException e) {
        // возвращаем сообщение об ошибке
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserAlreadyExists(final UserNotInFriendsListException e) {
        return new ErrorResponse(e.getMessage());
    }

    // Для исключения ValidationException — код ответа 400
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidEmail(final ValidationException e) {
        return new ErrorResponse(e.getMessage());
    }


    // Для любого другого исключения (Throwable) — код ответа 500
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherErrors(final Throwable e) {
        return new ErrorResponse("Произошла непредвиденная ошибка.");
    }
}
