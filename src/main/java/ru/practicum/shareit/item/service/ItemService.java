package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item getItem(Long id);

    List<Item> getItems(Long ownerId);

    List<Item> findItems(String text);

    Item createItem(ItemDto itemDto, Long ownerId);

    Item updateItem(ItemDto itemDto, Long itemId, Long ownerId);

    boolean deleteItem(Long id, Long ownerId);
}
