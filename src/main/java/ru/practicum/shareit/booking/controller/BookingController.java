package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @GetMapping(path = "/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long bookingId) {
        Booking booking = bookingService.getBooking(userId, bookingId);

        return BookingDto.toBookingDto(booking);
    }

    @GetMapping
    public List<BookingDto> getBookingsForBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(name = "state", defaultValue = "ALL", required = false) State state) {
        List<Booking> bookings = bookingService.getBookingsForBooker(userId, state);

        return bookings.stream()
                .map(BookingDto::toBookingDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsForOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(name = "state", defaultValue = "ALL", required = false) State state) {
        List<Booking> bookings = bookingService.getBookingsForOwner(userId, state);

        return bookings.stream()
                .map(BookingDto::toBookingDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody @Valid BookingRequest bookingRequest) {
        Booking booking = bookingService.create(userId, bookingRequest);

        return BookingDto.toBookingDto(booking);
    }

    @PatchMapping(path = "/{bookingId}")
    public BookingDto setApprove(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                 @PathVariable Long bookingId,
                                 @RequestParam(name = "approved") Boolean approved) {
        Booking booking = bookingService.setApprove(ownerId, bookingId, approved);

        return BookingDto.toBookingDto(booking);
    }
}
