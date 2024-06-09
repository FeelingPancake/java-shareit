package ru.practicum.shareit.error;

public class BookingNotExistsException extends RuntimeException {
    public BookingNotExistsException(String mes) {
        super(mes);
    }
}
