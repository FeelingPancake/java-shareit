package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto createItemRequest(Long userId, ItemRequestCreateDto itemRequestCreateDto);

    List<ItemRequestResponseDto> getAllRequestsForUser(Long userId);

    List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size);

    ItemRequestResponseDto getItemRequest(Long userId, Long requestId);
}
