package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestBody @Valid ItemRequestCreateDto itemRequestCreateDto) {

        log.info("create itemRequest for userId={}", userId);

        return requestClient.createItemRequest(userId, itemRequestCreateDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsForUser(@RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("get all itemRequests for user {}", userId);

        return requestClient.getAllItemRequestsForUser(userId);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(value = "from", defaultValue = "0", required = false) @Min(value = 0) int from,
                                                 @RequestParam(value = "size", defaultValue = "10", required = false) @Min(value = 0) int size) {
        log.info("Get all itemRequests {}", userId);

        return requestClient.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable(value = "requestId") Long requestId) {
        log.info("Get itemRequest {}, userId={}", requestId, userId);

        return requestClient.getItemRequest(userId, requestId);
    }
}
