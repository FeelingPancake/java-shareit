package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ReplyDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;
import ru.practicum.shareit.utils.DtoMapper;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestJpaRepository itemRequestStorage;
    private final UserJpaRepository userJpaRepository;

    @Override
    public ItemRequestResponseDto createItemRequest(Long userId, ItemRequestCreateDto itemRequestCreateDto) {
        User user = userJpaRepository.findById(userId).orElseThrow(
                () -> new EntityNotExistsExeption("Пользователя " + userId + "нет"));
        ItemRequest itemRequest = DtoMapper.toItemRequestEntity(user, itemRequestCreateDto);

        return DtoMapper.toItemRequestResponseDto(itemRequestStorage.save(itemRequest), Collections.emptyList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequestsForUser(Long userId) {
        User user = userJpaRepository.findById(userId).orElseThrow(
                () -> new EntityNotExistsExeption("Пользователя " + userId + "нет"));
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        return itemRequestStorage.findByApplicantId(userId, sort).stream()
                .map(itemRequest -> {
                    List<ReplyDto> replyDtoList = itemRequest.getReplies() == null ?
                            Collections.emptyList() : itemRequest.getReplies().stream()
                            .map(DtoMapper::toReplyDto)
                            .collect(Collectors.toList());

                    return DtoMapper.toItemRequestResponseDto(itemRequest, replyDtoList);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return itemRequestStorage.findByApplicantIdNot(userId, pageable).stream()
                .map(itemRequest -> {
                    List<ReplyDto> replyDtoList = itemRequest.getReplies() == null ?
                            Collections.emptyList() : itemRequest.getReplies().stream()
                            .map(DtoMapper::toReplyDto)
                            .collect(Collectors.toList());
                    return DtoMapper.toItemRequestResponseDto(itemRequest, replyDtoList);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getItemRequest(Long userId, Long requestId) {
        userJpaRepository.findById(userId).orElseThrow(
                () -> new EntityNotExistsExeption("Пользователя " + userId + "не существует"));

        ItemRequest itemRequest = itemRequestStorage.findById(requestId).orElseThrow(() ->
                new EntityNotExistsExeption("Запроса " + requestId + " не существует"));

        return DtoMapper.toItemRequestResponseDto(itemRequest, itemRequest.getReplies() == null ?
                Collections.emptyList() : itemRequest.getReplies().stream()
                .map(DtoMapper::toReplyDto)
                .collect(Collectors.toList()));
    }
}
