package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.error.ElementAccessException;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.error.PermissionException;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentJpaRepository;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.Reply;
import ru.practicum.shareit.request.storage.ItemRequestJpaRepository;
import ru.practicum.shareit.request.storage.ReplyJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;
import ru.practicum.shareit.utils.DtoMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemJpaRepository itemStorage;
    private final UserJpaRepository userStorage;
    private final BookingJpaRepository bookingStorage;
    private final CommentJpaRepository commentStorage;
    private final ItemRequestJpaRepository itemRequestStorage;
    private final ReplyJpaRepository replyStorage;

    @Transactional(readOnly = true)
    @Override
    public ItemDtoOwner getItem(Long userId, Long itemId) {
        userStorage.findById(userId).orElseThrow(() -> new EntityNotExistsExeption(userId.toString()));
        Item item = itemStorage.findById(itemId).orElseThrow(() -> new EntityNotExistsExeption(itemId.toString()));
        if (item.getOwner().getId().equals(userId)) {
            return DtoMapper.toItemDtoOwner(item,
                    bookingStorage.findLastBookingForItem(itemId).stream().findFirst().orElse(null),
                    bookingStorage.findNextBookingForItem(itemId).stream().findFirst().orElse(null),
                    commentStorage.findByItemId(itemId).stream()
                            .map(DtoMapper::toCommentDto)
                            .collect(Collectors.toList()));
        } else {
            return DtoMapper.toItemDtoOwner(item,
                    null,
                    null,
                    commentStorage.findByItemId(itemId).stream()
                            .map(DtoMapper::toCommentDto)
                            .collect(Collectors.toList()));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDtoOwner> getItems(Long ownerId, int from, int size) {
        userStorage.findById(ownerId).orElseThrow(() -> new EntityNotExistsExeption(ownerId.toString()));
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<Item> items = itemStorage.findByOwnerId(ownerId, pageable);

        List<ItemDtoOwner> itemDtoOwners = items.stream()
                .map(x -> DtoMapper.toItemDtoOwner(x,
                        bookingStorage.findLastBookingForItem(x.getId()).stream().findFirst().orElse(null),
                        bookingStorage.findNextBookingForItem(x.getId()).stream().findFirst().orElse(null),
                        commentStorage.findByItemId(x.getId()).stream()
                                .map(DtoMapper::toCommentDto)
                                .collect(Collectors.toList())))
                .sorted(Comparator.comparingLong(ItemDtoOwner::getId))
                .collect(Collectors.toList());
        return itemDtoOwners;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> findItems(String text, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<Item> items = itemStorage.findAll(pageable).stream()
                .filter(item ->
                        (item.getName().toLowerCase().contains(text.toLowerCase())
                                || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                                && item.getAvailable())
                .collect(Collectors.toList());

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        List<Comment> comments = commentStorage.findByItemIdIn(itemIds);
        Map<Item, List<Comment>> commentsByItemId = comments.stream().collect(Collectors.groupingBy(Comment::getItem));
        List<ItemDto> itemDtos = items.stream()
                .map(item -> {
                    List<Comment> itemComments = commentsByItemId.getOrDefault(item, Collections.emptyList());
                    return DtoMapper.toItemDto(item, item.getOwner(), null, itemComments);
                }).collect(Collectors.toList());

        return itemDtos;
    }

    @Transactional
    @Override
    public ItemDto createItem(ItemDtoRequest itemDtoRequest, Long ownerId) {
        User user = userStorage.findById(ownerId).orElseThrow(() -> new EntityNotExistsExeption(ownerId.toString()));
        Long requestId = itemDtoRequest.getRequestId();
        Item savedItem;

        if (requestId != null) {
            ItemRequest itemRequest = itemRequestStorage.findById(requestId).orElseThrow(
                    () -> new EntityNotExistsExeption("Запроса на вещь " + requestId + " не существует")
            );
            Item item = Item.builder()
                    .name(itemDtoRequest.getName())
                    .description(itemDtoRequest.getDescription())
                    .available(true)
                    .owner(user)
                    .request(itemRequest)
                    .build();

            savedItem = itemStorage.save(item);
            replyStorage.save(Reply.builder().user(user).itemRequest(itemRequest).item(savedItem).build());
        } else {
            Item item = Item.builder()
                    .name(itemDtoRequest.getName())
                    .description(itemDtoRequest.getDescription())
                    .available(true)
                    .owner(user)
                    .build();

            savedItem = itemStorage.save(item);
        }

        return DtoMapper.toItemDto(savedItem, user, requestId, Collections.emptyList());
    }

    @Transactional
    @Override
    public Comment createComment(Long userId,
                                 CommentDtoRequest commentDtoRequest,
                                 Long itemId) {
        User user = userStorage.findById(userId).orElseThrow(() -> new EntityNotExistsExeption(userId.toString()));
        Item item = itemStorage.findById(itemId).orElseThrow(() -> new EntityNotExistsExeption(itemId.toString()));

        Boolean isBooked = !bookingStorage.findCompletedBookingForUserAndItem(itemId, userId).isEmpty();

        if (!isBooked) {
            throw new ElementAccessException("Пользователь " + userId + " не брал вещь в аренду");
        }

        Comment comment = Comment.builder()
                .item(item)
                .user(user)
                .text(commentDtoRequest.getText())
                .build();

        return commentStorage.save(comment);
    }

    @Transactional
    @Override
    public ItemDto updateItem(ItemDtoRequest itemDtoRequest, Long itemId, Long ownerId) {
        User user = userStorage.findById(ownerId).orElseThrow(() -> new EntityNotExistsExeption(ownerId.toString()));

        Item item = itemStorage.findById(itemId).orElseThrow(
                () -> new EntityNotExistsExeption(itemId.toString()));

        if (!Objects.equals(item.getOwner().getId(), ownerId)) {
            throw new PermissionException(ownerId.toString());
        }

        Item updateItem = item.toBuilder()
                .name(itemDtoRequest.getName() == null ? item.getName() : itemDtoRequest.getName())
                .description(itemDtoRequest.getDescription() == null ? item.getDescription() : itemDtoRequest.getDescription())
                .available(itemDtoRequest.getAvailable() == null ? item.getAvailable() : itemDtoRequest.getAvailable())
                .build();
        Item savedItem = itemStorage.save(updateItem);
        List<Comment> comments = commentStorage.findByItemId(savedItem.getId());
        Long requestId = savedItem.getRequest() != null ? savedItem.getRequest().getId() : null;

        return DtoMapper.toItemDto(savedItem, user, requestId, comments);
    }

    @Transactional
    @Override
    public void deleteItem(Long id, Long ownerId) {
        Item item = itemStorage.findById(id).orElseThrow(() -> new EntityNotExistsExeption(id.toString()));

        User user = item.getOwner();


        if (Objects.equals(user.getId(), ownerId)) {
            itemStorage.delete(item);
        } else {
            throw new PermissionException(ownerId.toString());
        }
    }
}
