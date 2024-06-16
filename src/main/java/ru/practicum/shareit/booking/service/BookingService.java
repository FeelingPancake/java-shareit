package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.controller.State;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    Booking getBooking(Long userId, Long bookingId);

    List<Booking> getBookingsForBooker(Long userId, State state);

    List<Booking> getBookingsForOwner(Long userId, State stare);

    Booking create(Long userId, BookingRequest bookingRequest);

    Booking setApprove(Long ownerId, Long bookingId, Boolean approved);
}
