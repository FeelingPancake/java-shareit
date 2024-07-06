package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookItemRequestDto {
	@NotNull
	long itemId;
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
