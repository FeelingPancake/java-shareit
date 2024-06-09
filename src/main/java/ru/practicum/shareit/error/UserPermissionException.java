package ru.practicum.shareit.error;

public class UserPermissionException extends RuntimeException {
    public UserPermissionException(String mes) {
        super(mes);
    }
}
