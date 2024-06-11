package ru.practicum.shareit.error;

public class EntityNotExistsExeption extends RuntimeException {
    public EntityNotExistsExeption(String mes) {
        super(mes);
    }
}
