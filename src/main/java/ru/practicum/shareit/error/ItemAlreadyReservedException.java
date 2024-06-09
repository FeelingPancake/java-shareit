package ru.practicum.shareit.error;

public class ItemAlreadyReservedException extends RuntimeException {
    public ItemAlreadyReservedException(String mes) {
        super(mes);
    }
}
