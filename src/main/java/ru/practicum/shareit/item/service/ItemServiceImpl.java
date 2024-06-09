package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.error.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentJpaRepository;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemJpaRepository itemStorage;
    private final UserJpaRepository userStorage;
    private final BookingJpaRepository bookingStorage;
    private final CommentJpaRepository commentStorage;

    @Override
    public ItemDtoOwner getItem(Long userId, Long itemId) {
        userStorage.findById(userId).orElseThrow(() -> new UserDoesNotExixtsException(userId.toString()));
        Item item = itemStorage.findById(itemId).orElseThrow(() -> new ItemNotExistsExeption(itemId.toString()));
        if (item.getOwner().getId().equals(userId)) {
            return ItemMapper.toItemDtoOwner(item,
                    bookingStorage.findLastBookingForItem(itemId).stream().findFirst().orElse(null),
                    bookingStorage.findNextBookingForItem(itemId).stream().findFirst().orElse(null),
                    commentStorage.findByItemId(itemId).stream()
                            .map(CommentDto::toCommentDto)
                            .collect(Collectors.toList()));
        } else {
            return ItemMapper.toItemDtoOwner(item,
                    null,
                    null,
                    commentStorage.findByItemId(itemId).stream()
                            .map(CommentDto::toCommentDto)
                            .collect(Collectors.toList()));
        }
    }

    @Override
    public List<ItemDtoOwner> getItems(Long ownerId) {
        userStorage.findById(ownerId).orElseThrow(() -> new UserDoesNotExixtsException(ownerId.toString()));

        List<Item> items = itemStorage.findByOwnerId(ownerId);
        List<ItemDtoOwner> itemDtoOwners = items.stream()
                .map(x -> ItemMapper.toItemDtoOwner(x,
                        bookingStorage.findLastBookingForItem(x.getId()).stream().findFirst().orElse(null),
                        bookingStorage.findNextBookingForItem(x.getId()).stream().findFirst().orElse(null),
                        commentStorage.findByItemId(x.getId()).stream()
                                .map(CommentDto::toCommentDto)
                                .collect(Collectors.toList())))
                .sorted(Comparator.comparingLong(ItemDtoOwner::getId))
                .collect(Collectors.toList());
        return itemDtoOwners;
    }

    @Override
    public List<Item> findItems(String text) {
        return itemStorage.findAll().stream()
                .filter(item ->
                        (item.getName().toLowerCase().contains(text.toLowerCase())
                                || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                                && item.getAvailable())
                .collect(Collectors.toList());
    }

    @Override
    public Item createItem(ItemRequest itemRequest, Long ownerId) {
        User user = userStorage.findById(ownerId).orElseThrow(() -> new UserDoesNotExixtsException(ownerId.toString()));

        Item item = Item.builder()
                .name(itemRequest.getName())
                .description(itemRequest.getDescription())
                .available(true)
                .owner(user)
                .build();

        return itemStorage.save(item);
    }

    @Override
    public Comment createComment(Long userId,
                                 CommentRequest commentRequest,
                                 Long itemId) {
        if (!userStorage.existsById(userId)) {
            throw new UserDoesNotExixtsException(userId.toString());
        }

        if (!itemStorage.existsById(itemId)) {
            throw new ItemNotExistsExeption(itemId.toString());
        }

        Boolean isBooked = !bookingStorage.findCompletedBookingForUserAndItem(itemId, userId).isEmpty();

        if (!isBooked) {
            throw new UserBookingNotExistsException("Пользователь " + userId + " не брал вещь в аренду");
        }

        Comment comment = Comment.builder()
                .item(itemStorage.getById(itemId))
                .user(userStorage.getById(userId))
                .text(commentRequest.getText())
                .build();

        return commentStorage.save(comment);
    }

    @Override
    public Item updateItem(ItemRequest itemRequest, Long itemId, Long ownerId) {
        userStorage.findById(ownerId).orElseThrow(() -> new UserDoesNotExixtsException(ownerId.toString()));

        Item item = itemStorage.findById(itemId).orElseThrow(
                () -> new ItemNotExistsExeption(itemId.toString()));

        if (!Objects.equals(item.getOwner().getId(), ownerId)) {
            throw new PermissionException(ownerId.toString());
        }

        Item updateItem = item.toBuilder()
                .name(itemRequest.getName() == null ? item.getName() : itemRequest.getName())
                .description(itemRequest.getDescription() == null ? item.getDescription() : itemRequest.getDescription())
                .available(itemRequest.getAvailable() == null ? item.getAvailable() : itemRequest.getAvailable())
                .build();
        return itemStorage.save(updateItem);
    }

    @Override
    public void deleteItem(Long id, Long ownerId) {
        Item item = itemStorage.findById(id).orElseThrow(() -> new ItemNotExistsExeption(id.toString()));

        User user = item.getOwner();


        if (Objects.equals(user.getId(), ownerId)) {
            itemStorage.delete(item);
        } else {
            throw new PermissionException(ownerId.toString());
        }
    }
}
