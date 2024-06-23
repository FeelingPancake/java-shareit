package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestBody @Valid ItemRequestCreateDto itemRequestCreateDto) {

        return itemRequestService.createItemRequest(userId, itemRequestCreateDto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getAllRequestsForUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                              @RequestParam(value = "from", defaultValue = "0", required = false) @Min(value = 0) int from,
                                                              @RequestParam(value = "size", defaultValue = "10", required = false) @Min(value = 0) int size) {

        return itemRequestService.getAllRequestsForUser(userId);
    }

    @GetMapping(value = "/all")
    public List<ItemRequestResponseDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(value = "from", defaultValue = "0", required = false) @Min(value = 0) int from,
                                                       @RequestParam(value = "size", defaultValue = "10", required = false) @Min(value = 0) int size) {

        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable(value = "requestId") Long requestId) {

        return itemRequestService.getItemRequest(userId, requestId);
    }
}
