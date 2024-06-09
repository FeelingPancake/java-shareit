package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.BookingStatus;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class BookingDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    BookingStatus status;
    Booker booker;
    ItemResponse item;

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .status(booking.getStatus())
                .booker(new Booker(booking.getBooker().getId()))
                .item(new ItemResponse(booking.getItem().getId(), booking.getItem().getName()))
                .build();
    }

    @Data
    @RequiredArgsConstructor
    public static class Booker {
        private final Long id;
    }

    @Data
    @RequiredArgsConstructor
    public static class ItemResponse {
        private final Long id;
        private final String name;
    }
}
