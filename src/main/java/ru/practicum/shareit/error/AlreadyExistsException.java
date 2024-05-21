package ru.practicum.shareit.error;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String mes) {
        super(mes);
    }
}
