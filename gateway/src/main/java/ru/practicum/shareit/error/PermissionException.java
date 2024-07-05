package ru.practicum.shareit.error;

public class PermissionException extends RuntimeException {
    public PermissionException(String mes) {
        super(mes);
    }
}
