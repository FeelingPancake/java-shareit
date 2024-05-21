package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item get(Long id);

    List<Item> getAll();

    Item create(Item item);

    Item update(Item item);

    boolean delete(Long id);
}
