package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.annotations.Marker;
import ru.practicum.shareit.error.UserDoesNotExixtsException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;
    private final UserService userService;

    @GetMapping("/{id}")
    public Item get(@PathVariable Long id) {
        return itemService.getItem(id);
    }

    @GetMapping
    public List<Item> getAll(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        if (userService.getUser(ownerId) == null) {
            throw new UserDoesNotExixtsException(ownerId.toString());
        }
        return itemService.getItems(ownerId);
    }

    @GetMapping("/search")
    public List<Item> find(@RequestParam(value = "text", required = true) String text) {
        return itemService.findItems(text);
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public Item create(@Valid @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        if (userService.getUser(ownerId) == null) {
            throw new UserDoesNotExixtsException(ownerId.toString());
        }
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{id}")
    @Validated({Marker.OnUpdate.class})
    public Item update(@Valid @RequestBody ItemDto itemDto,
                       @PathVariable Long id,
                       @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        if (userService.getUser(ownerId) == null) {
            throw new UserDoesNotExixtsException(ownerId.toString());
        }

        return itemService.updateItem(itemDto, id, ownerId);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        if (userService.getUser(ownerId) == null) {
            throw new UserDoesNotExixtsException(ownerId.toString());
        }

        return itemService.deleteItem(id, ownerId);
    }

}
