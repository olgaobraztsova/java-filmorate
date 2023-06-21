package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FilmOrItsPartNotFoundException extends ResponseStatusException {
    public FilmOrItsPartNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

}
