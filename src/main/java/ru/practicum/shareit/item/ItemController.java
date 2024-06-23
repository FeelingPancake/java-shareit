package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utils.DtoMapper;
import ru.practicum.shareit.utils.annotations.Marker;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Transactional
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemDtoOwner get(@RequestHeader("X-Sharer-User-id") Long userId, @PathVariable Long itemId) {
        return itemService.getItem(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoOwner> getAll(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                     @RequestParam(value = "from", defaultValue = "0", required = false) @Min(value = 0) int from,
                                     @RequestParam(value = "size", defaultValue = "10", required = false) @Min(value = 0) int size) {
        return itemService.getItems(ownerId, from, size);
    }

    @GetMapping("/search")
    public List<Item> find(@RequestParam(value = "text", required = true) String text,
                           @RequestParam(value = "from", defaultValue = "0", required = false) @Min(value = 0) int from,
                           @RequestParam(value = "size", defaultValue = "10", required = false) @Min(value = 0) int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemService.findItems(text, from, size);
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public ItemDto create(@Valid @RequestBody ItemDtoRequest itemDtoRequest, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.createItem(itemDtoRequest, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestBody @Valid CommentDtoRequest commentDtoRequest,
                                    @PathVariable Long itemId) {

        Comment comment = itemService.createComment(userId, commentDtoRequest, itemId);

        return DtoMapper.toCommentDto(comment);
    }

    @PatchMapping("/{id}")
    @Validated({Marker.OnUpdate.class})
    public Item update(@Valid @RequestBody ItemDtoRequest itemDtoRequest,
                       @PathVariable Long id,
                       @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.updateItem(itemDtoRequest, id, ownerId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        itemService.deleteItem(id, ownerId);
    }
}
