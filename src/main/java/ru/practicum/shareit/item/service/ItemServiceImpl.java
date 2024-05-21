package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.ItemNotExistsExeption;
import ru.practicum.shareit.error.PermissionException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private static Long id = 1L;
    private final ItemRepository itemStorage;
    private final UserRepository userStorage;

    @Override
    public Item getItem(Long id) {

        return itemStorage.get(id);
    }

    @Override
    public List<Item> getItems(Long ownerId) {

        return itemStorage.getAll().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemStorage.getAll().stream()
                .filter(item ->
                        (item.getName().toLowerCase().contains(text.toLowerCase())
                                || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                                && item.getAvailable())
                .collect(Collectors.toList());
    }

    @Override
    public Item createItem(ItemDto itemDto, Long ownerId) {
        Item item = Item.builder()
                .id(id++)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(true)
                .ownerId(ownerId)
                .build();

        return itemStorage.create(item);
    }

    @Override
    public Item updateItem(ItemDto itemDto, Long itemId, Long ownerId) {
        Optional<Item> itemOptional = Optional.ofNullable(itemStorage.get(itemId));
        if (itemOptional.isEmpty()) {
            throw new ItemNotExistsExeption(itemId.toString());
        }

        Item currentItem = itemOptional.get();
        if (!Objects.equals(currentItem.getOwnerId(), ownerId)) {
            throw new PermissionException(ownerId.toString());
        }

        Item updateItem = currentItem.toBuilder()
                .name(itemDto.getName() == null ? currentItem.getName() : itemDto.getName())
                .description(itemDto.getDescription() == null ? currentItem.getDescription() : itemDto.getDescription())
                .available(itemDto.getAvailable() == null ? currentItem.getAvailable() : itemDto.getAvailable())
                .build();
        return itemStorage.update(updateItem);
    }

    @Override
    public boolean deleteItem(Long id, Long ownerId) {
        if (Objects.equals(itemStorage.get(id).getOwnerId(), ownerId)) {
            return itemStorage.delete(id);
        } else {
            throw new PermissionException(ownerId.toString());
        }
    }
}
