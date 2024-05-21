package ru.practicum.shareit.error;

public class OwnerDoesNotExixtsExeption extends RuntimeException {
    public OwnerDoesNotExixtsExeption(String mes) {
        super(mes);
    }
}
