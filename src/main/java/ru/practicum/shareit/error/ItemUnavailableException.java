package ru.practicum.shareit.error;

public class ItemUnavailableException extends RuntimeException {
    public ItemUnavailableException(String mes) {
        super(mes);
    }
}
