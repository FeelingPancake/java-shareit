package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.dto.ItemRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDtoOwner getItem(Long userId, Long itemId);

    List<ItemDtoOwner> getItems(Long ownerId);

    List<Item> findItems(String text);

    Item createItem(ItemRequest itemRequest, Long ownerId);

    Comment createComment(Long userId, CommentRequest commentRequest, Long itemId);

    Item updateItem(ItemRequest itemRequest, Long itemId, Long ownerId);

    void deleteItem(Long id, Long ownerId);
}
