package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemJpaRepository itemStorage;
    private final UserJpaRepository userStorage;
    private final BookingJpaRepository bookingStorage;
    private final CommentJpaRepository commentStorage;
    private final ItemRequestJpaRepository itemRequestStorage;
    private final ReplyJpaRepository replyStorage;

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

    @Override
    public List<Item> findItems(String text, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        List<Item> items = itemStorage.findAll(pageable).stream()
                .filter(item ->
                        (item.getName().toLowerCase().contains(text.toLowerCase())
                                || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                                && item.getAvailable())
                .collect(Collectors.toList());

        return items;
    }

    @Override
    public ItemDto createItem(ItemDtoRequest itemDtoRequest, Long ownerId) {
        User user = userStorage.findById(ownerId).orElseThrow(() -> new EntityNotExistsExeption(ownerId.toString()));
        ItemRequest itemRequest;

        Item item = Item.builder()
                .name(itemDtoRequest.getName())
                .description(itemDtoRequest.getDescription())
                .available(true)
                .owner(user)
                .build();


        Item savedItem = itemStorage.save(item);
        Long requestId = itemDtoRequest.getRequestId();

        if (requestId != null) {
            itemRequest = itemRequestStorage.findById(requestId).orElseThrow(
                    () -> new EntityNotExistsExeption("Запроса на вещь " + requestId + " не существует")
            );

            replyStorage.save(Reply.builder().user(user).itemRequest(itemRequest).item(savedItem).build());
        }

        return DtoMapper.toItemDto(savedItem, user, requestId);
    }

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

    @Override
    public Item updateItem(ItemDtoRequest itemDtoRequest, Long itemId, Long ownerId) {
        userStorage.findById(ownerId).orElseThrow(() -> new EntityNotExistsExeption(ownerId.toString()));

        Item item = itemStorage.findById(itemId).orElseThrow(
                () -> new EntityNotExistsExeption(itemId.toString()));

        if (!Objects.equals(item.getOwner().getId(), ownerId)) {
            throw new PermissionException(ownerId.toString());
        }

        Hibernate.initialize(item.getComments());

        Item updateItem = item.toBuilder()
                .name(itemDtoRequest.getName() == null ? item.getName() : itemDtoRequest.getName())
                .description(itemDtoRequest.getDescription() == null ? item.getDescription() : itemDtoRequest.getDescription())
                .available(itemDtoRequest.getAvailable() == null ? item.getAvailable() : itemDtoRequest.getAvailable())
                .build();
        return itemStorage.save(updateItem);
    }

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
