package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {
    @NotNull
    Long itemId;
    @FutureOrPresent
    @NotNull
    LocalDateTime start;
    @Future
    @NotNull
    LocalDateTime end;

    @AssertTrue(message = "Конечная дата аренды не должна быть раньше начальной даты")
    public boolean isStartDateBeforeDate() {
        if (start == null || end == null) {
            return true;
        }
        return start.isBefore(end);
    }
}
