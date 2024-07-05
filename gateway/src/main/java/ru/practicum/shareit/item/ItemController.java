package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.utils.annotations.Marker;

import java.util.Collections;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                         @RequestParam(value = "from", defaultValue = "0", required = false) @Min(value = 0) int from,
                                         @RequestParam(value = "size", defaultValue = "10", required = false) @Min(value = 0) int size) {
        return itemClient.getItems(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> find(@RequestParam(value = "text", required = true) String text,
                                       @RequestParam(value = "from", defaultValue = "0", required = false) @Min(value = 0) int from,
                                       @RequestParam(value = "size", defaultValue = "10", required = false) @Min(value = 0) int size) {
        if (text.isBlank()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        return itemClient.findItems(text, from, size);
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public ResponseEntity<Object> create(@Valid @RequestBody ItemDtoRequest itemDtoRequest, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemClient.createItem(ownerId, itemDtoRequest);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestBody @Valid CommentDtoRequest commentDtoRequest,
                                                @PathVariable Long itemId) {

        return itemClient.createComment(userId, commentDtoRequest, itemId);
    }

    @PatchMapping("/{id}")
    @Validated({Marker.OnUpdate.class})
    public ResponseEntity<Object> update(@Valid @RequestBody ItemDtoRequest itemDtoRequest,
                                         @PathVariable Long id,
                                         @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemClient.updateItem(ownerId, itemDtoRequest, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemClient.deleteItem(id, ownerId);
    }
}
