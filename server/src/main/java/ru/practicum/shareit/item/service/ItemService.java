package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface ItemService {
    ItemDtoOwner getItem(Long userId, Long itemId);

    List<ItemDtoOwner> getItems(Long ownerId, int from, int size);

    List<ItemDto> findItems(String text, int from, int size);

    ItemDto createItem(ItemDtoRequest itemDtoRequest, Long ownerId);

    Comment createComment(Long userId, CommentDtoRequest commentDtoRequest, Long itemId);

    ItemDto updateItem(ItemDtoRequest itemDtoRequest, Long itemId, Long ownerId);

    void deleteItem(Long id, Long ownerId);
}
