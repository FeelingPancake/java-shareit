package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoOwner {
    Long id;
    String name;
    String description;
    Boolean available;
    LastBooking lastBooking;
    NextBooking nextBooking;
    List<CommentDto> comments;


    public interface LastBooking {
        Long getid();

        Long getBookerId();
    }

    public interface NextBooking {
        Long getid();

        Long getBookerId();
    }
}
