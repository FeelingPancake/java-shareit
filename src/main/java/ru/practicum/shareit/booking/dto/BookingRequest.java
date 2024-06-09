package ru.practicum.shareit.booking.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @NotNull
    private Long itemId;
    @FutureOrPresent
    @NotNull
    private LocalDateTime start;
    @Future
    @NotNull
    private LocalDateTime end;

    @AssertTrue(message = "Конечная дата аренды не должна быть раньше начальной даты")
    public boolean isStartDateBeforeDate() {
        if (start == null || end == null) {
            return true;
        }
        return start.isBefore(end);
    }
}
