package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public class ItemMapper {
    public static ItemRequest toRequest(Item item) {
        return ItemRequest.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static ItemDtoOwner toItemDtoOwner(Item item,
                                              ItemDtoOwner.LastBooking lastBooking,
                                              ItemDtoOwner.NextBooking nextBooking,
                                              List<CommentDto> comments) {
        return ItemDtoOwner.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();
    }
}
