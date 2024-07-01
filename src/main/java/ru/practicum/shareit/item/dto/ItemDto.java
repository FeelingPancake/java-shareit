package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    Long id;
    User owner;
    String name;
    String description;
    Boolean available;
    List<CommentDto> comments;
    Long requestId;
}