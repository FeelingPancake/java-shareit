package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.utils.enums.State;

import java.util.List;

public interface BookingService {
    Booking getBooking(Long userId, Long bookingId);

    List<Booking> getBookingsForBooker(Long userId, State state, int from, int size);

    List<Booking> getBookingsForOwner(Long userId, State state, int from, int size);

    Booking create(Long userId, BookingRequest bookingRequest);

    Booking setApprove(Long ownerId, Long bookingId, Boolean approved);
}
