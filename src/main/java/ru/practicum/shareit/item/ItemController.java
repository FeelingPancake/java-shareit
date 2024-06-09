package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.annotations.Marker;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.dto.ItemRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemDtoOwner get(@RequestHeader("X-Sharer-User-id") Long userId, @PathVariable Long itemId) {
        return itemService.getItem(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoOwner> getAll(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getItems(ownerId);
    }

    @GetMapping("/search")
    public List<Item> find(@RequestParam(value = "text", required = true) String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemService.findItems(text);
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public Item create(@Valid @RequestBody ItemRequest itemRequest, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.createItem(itemRequest, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestBody @Valid CommentRequest commentRequest,
                                    @PathVariable Long itemId) {

        Comment comment = itemService.createComment(userId, commentRequest, itemId);

        return CommentDto.toCommentDto(comment);
    }

    @PatchMapping("/{id}")
    @Validated({Marker.OnUpdate.class})
    public Item update(@Valid @RequestBody ItemRequest itemRequest,
                       @PathVariable Long id,
                       @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.updateItem(itemRequest, id, ownerId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        itemService.deleteItem(id, ownerId);
    }

}
