package ru.practicum.shareit.error;

public class UserDoesNotExixtsException extends RuntimeException {
    public UserDoesNotExixtsException(String mes) {
        super(mes);
    }
}
