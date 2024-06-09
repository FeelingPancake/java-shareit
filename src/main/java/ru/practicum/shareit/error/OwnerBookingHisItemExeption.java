package ru.practicum.shareit.error;

public class OwnerBookingHisItemExeption extends RuntimeException {
    public OwnerBookingHisItemExeption(String mes) {
        super(mes);
    }
}
