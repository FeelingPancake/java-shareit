package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
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
