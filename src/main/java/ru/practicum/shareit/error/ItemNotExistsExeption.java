package ru.practicum.shareit.error;

public class ItemNotExistsExeption extends RuntimeException {
    public ItemNotExistsExeption(String mes) {
        super(mes);
    }
}
