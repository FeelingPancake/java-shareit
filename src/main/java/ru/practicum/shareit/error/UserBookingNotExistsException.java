package ru.practicum.shareit.error;

public class UserBookingNotExistsException extends RuntimeException {
    public UserBookingNotExistsException(String mes) {
        super(mes);
    }
}
