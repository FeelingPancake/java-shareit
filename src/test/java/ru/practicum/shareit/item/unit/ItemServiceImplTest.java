package ru.practicum.shareit.item.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
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
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentJpaRepository;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.Reply;
import ru.practicum.shareit.request.storage.ItemRequestJpaRepository;
import ru.practicum.shareit.request.storage.ReplyJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ItemServiceImplTest {

    @Mock
    private ItemJpaRepository itemStorage;
    @Mock
    private UserJpaRepository userStorage;
    @Mock
    private BookingJpaRepository bookingStorage;
    @Mock
    private CommentJpaRepository commentStorage;
    @Mock
    private ItemRequestJpaRepository itemRequestStorage;
    @Mock
    private ReplyJpaRepository replyStorage;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User itemOwner;
    private User itemSearcher;
    private Item item;

    @BeforeEach
    void setUp() {
        Long itemOwnerId = 1L;
        Long itemSearcherId = 2L;
        Long itemId = 3L;

        itemOwner = User.builder()
                .id(itemOwnerId)
                .name("ItemOwner")
                .email("owner@email.com")
                .build();

        itemSearcher = User.builder()
                .id(itemSearcherId)
                .name("ItemSearcher")
                .email("searcher@email.ru")
                .build();

        item = Item.builder()
                .id(itemId)
                .owner(itemOwner)
                .name("test item")
                .description("test")
                .available(true).build();
    }

    @Test
    void getItem_successIfUserItemOwner() {

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingStorage.findLastBookingForItem(item.getId())).thenReturn(Collections.emptyList());
        when(bookingStorage.findNextBookingForItem(item.getId())).thenReturn(Collections.emptyList());
        when(commentStorage.findByItemId(item.getId())).thenReturn(Collections.emptyList());

        ItemDtoOwner result = itemService.getItem(itemOwner.getId(), item.getId());

        assertNotNull(result);
        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(itemStorage, times(1)).findById(item.getId());
        verify(bookingStorage, times(1)).findLastBookingForItem(item.getId());
        verify(bookingStorage, times(1)).findNextBookingForItem(item.getId());
    }

    @Test
    void getItem_successIfUserNotOwner() {

        when(userStorage.findById(itemSearcher.getId())).thenReturn(Optional.of(itemSearcher));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentStorage.findByItemId(item.getId())).thenReturn(Collections.emptyList());

        ItemDtoOwner result = itemService.getItem(itemSearcher.getId(), item.getId());

        assertNotNull(result);
        verify(userStorage, times(1)).findById(itemSearcher.getId());
        verify(itemStorage, times(1)).findById(item.getId());
        verify(bookingStorage, times(0)).findLastBookingForItem(item.getId());
        verify(bookingStorage, times(0)).findNextBookingForItem(item.getId());
    }

    @Test
    void getItem_failedIfUserNotExists() {

        when(userStorage.findById(itemSearcher.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.getItem(itemSearcher.getId(), item.getId()));

        assertEquals(itemSearcher.getId().toString(),
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemSearcher.getId());
        verify(itemStorage, times(0)).findById(item.getId());
        verify(bookingStorage, times(0)).findLastBookingForItem(item.getId());
        verify(bookingStorage, times(0)).findNextBookingForItem(item.getId());
    }

    @Test
    void getItem_failedIfItemNotExists() {
        when(userStorage.findById(itemSearcher.getId())).thenReturn(Optional.of(itemSearcher));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.getItem(itemSearcher.getId(), item.getId()));

        assertEquals(item.getId().toString(),
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemSearcher.getId());
        verify(itemStorage, times(1)).findById(item.getId());
        verify(bookingStorage, times(0)).findLastBookingForItem(item.getId());
        verify(bookingStorage, times(0)).findNextBookingForItem(item.getId());
    }

    @Test
    void getItems_success() {
        int from = 0;
        int size = 10;

        List<Item> items = Collections.singletonList(item);
        Pageable pageable = PageRequest.of(from / size, size);

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(itemStorage.findByOwnerId(anyLong(), any(Pageable.class))).thenReturn(items);
        when(bookingStorage.findLastBookingForItem(anyLong())).thenReturn(Collections.emptyList());
        when(bookingStorage.findNextBookingForItem(anyLong())).thenReturn(Collections.emptyList());
        when(commentStorage.findByItemId(anyLong())).thenReturn(Collections.emptyList());

        List<ItemDtoOwner> result = itemService.getItems(itemOwner.getId(), from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(itemStorage, times(1)).findByOwnerId(itemOwner.getId(), pageable);
    }

    @Test
    void getItems_failedUserNotExists() {
        int from = 0;
        int size = 10;

        List<Item> items = Collections.singletonList(item);
        Pageable pageable = PageRequest.of(from / size, size);

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.getItem(itemOwner.getId(), item.getId()));

        assertEquals(itemOwner.getId().toString(),
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(itemStorage, times(0)).findByOwnerId(itemOwner.getId(), pageable);
    }

    @Test
    void findItems_successIfItemExists() {
        String text = "test";
        int from = 0;
        int size = 10;

        List<Item> items = Collections.singletonList(item);
        Pageable pageable = PageRequest.of(from / size, size);

        when(itemStorage.findAll(pageable)).thenReturn(new PageImpl<>(items));

        List<ItemDto> result = itemService.findItems(text, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemStorage, times(1)).findAll(pageable);
    }

    @Test
    void findItems_successIfItemNotExists() {
        String text = "test343423";
        int from = 0;
        int size = 10;

        List<Item> items = Collections.singletonList(item);
        Pageable pageable = PageRequest.of(from / size, size);

        when(itemStorage.findAll(pageable)).thenReturn(new PageImpl<>(items));

        List<ItemDto> result = itemService.findItems(text, from, size);

        assertEquals(0, result.size());
        verify(itemStorage, times(1)).findAll(pageable);
    }

    @Test
    void createItem_successWithRequestId() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("Item")
                .description("Description")
                .requestId(1L).build();

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L).build();

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(itemStorage.save(any(Item.class))).thenReturn(item);
        when(itemRequestStorage.findById(anyLong())).thenReturn(Optional.of(itemRequest));

        ItemDto result = itemService.createItem(itemDtoRequest, itemOwner.getId());
        assertNotNull(result);
        assertThat(result.getRequestId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(item.getName());
        assertThat(result.getAvailable()).isTrue();
        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(itemStorage, times(1)).save(any(Item.class));
        verify(replyStorage, times(1)).save(any(Reply.class));
    }

    @Test
    void createItem_successWithoutRequestId() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("Item")
                .description("Description")
                .build();

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(itemStorage.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(itemDtoRequest, itemOwner.getId());
        assertNotNull(result);
        assertThat(result.getRequestId()).isNull();
        assertThat(result.getName()).isEqualTo(item.getName());
        assertThat(result.getAvailable()).isTrue();
        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(itemStorage, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_failedUserNotExists() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("Item")
                .description("Description")
                .build();

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.createItem(itemDtoRequest, itemOwner.getId()));

        assertEquals(itemOwner.getId().toString(),
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(itemStorage, times(0)).save(any(Item.class));
    }

    @Test
    void createItem_failedRequestIdNotExists() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("Item")
                .description("Description")
                .requestId(1L)
                .build();

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(itemStorage.save(any(Item.class))).thenReturn(item);
        when(itemRequestStorage.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.createItem(itemDtoRequest, itemOwner.getId()));

        assertEquals("Запроса на вещь " + itemDtoRequest.getRequestId() + " не существует",
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(replyStorage, times(0)).save(any(Reply.class));
        verify(itemStorage, times(0)).save(any(Item.class));

        long countAfter = itemStorage.count();
        assertEquals(0, countAfter);
    }

    @Test
    void createComment_success() {
        CommentDtoRequest commentDtoRequest = new CommentDtoRequest();
        commentDtoRequest.setText("Comment");

        when(userStorage.findById(itemSearcher.getId())).thenReturn(Optional.of(itemSearcher));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingStorage.findCompletedBookingForUserAndItem(item.getId(), itemSearcher.getId()))
                .thenReturn(Collections.singletonList(new Booking()));
        when(commentStorage.save(any(Comment.class))).thenReturn(new Comment());

        Comment result = itemService.createComment(itemSearcher.getId(), commentDtoRequest, item.getId());

        assertNotNull(result);
        verify(userStorage, times(1)).findById(itemSearcher.getId());
        verify(itemStorage, times(1)).findById(item.getId());
        verify(commentStorage, times(1)).save(any(Comment.class));
    }

    @Test
    void createComment_failedOnUserNotExists() {
        CommentDtoRequest commentDtoRequest = new CommentDtoRequest();
        commentDtoRequest.setText("Comment");

        when(userStorage.findById(itemSearcher.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.createComment(itemSearcher.getId(), commentDtoRequest, item.getId()));

        assertEquals(itemSearcher.getId().toString(),
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemSearcher.getId());
        verify(itemStorage, times(0)).findById(item.getId());
        verify(commentStorage, times(0)).save(any(Comment.class));
    }

    @Test
    void createComment_failedOnItemNotExists() {
        CommentDtoRequest commentDtoRequest = new CommentDtoRequest();
        commentDtoRequest.setText("Comment");

        when(userStorage.findById(itemSearcher.getId())).thenReturn(Optional.of(itemSearcher));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.createComment(itemSearcher.getId(), commentDtoRequest, item.getId()));

        assertEquals(item.getId().toString(),
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemSearcher.getId());
        verify(itemStorage, times(1)).findById(item.getId());
        verify(commentStorage, times(0)).save(any(Comment.class));
    }

    @Test
    void createComment_failedOnUserNotBookedItem() {
        CommentDtoRequest commentDtoRequest = new CommentDtoRequest();
        commentDtoRequest.setText("Comment");

        when(userStorage.findById(itemSearcher.getId())).thenReturn(Optional.of(itemSearcher));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingStorage.findCompletedBookingForUserAndItem(item.getId(), itemSearcher.getId()))
                .thenReturn(Collections.emptyList());

        Exception exception = assertThrows(ElementAccessException.class,
                () -> itemService.createComment(itemSearcher.getId(), commentDtoRequest, item.getId()));

        assertEquals("Пользователь " + itemSearcher.getId() + " не брал вещь в аренду",
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemSearcher.getId());
        verify(itemStorage, times(1)).findById(item.getId());
        verify(commentStorage, times(0)).save(any(Comment.class));
    }

    @Test
    void updateItem_success() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("Updated Item")
                .available(false).build();

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemStorage.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.updateItem(itemDtoRequest, item.getId(), itemOwner.getId());

        assertNotNull(result);
        assertEquals(itemDtoRequest.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(itemDtoRequest.getAvailable(), result.getAvailable());
        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(itemStorage, times(1)).findById(item.getId());
        verify(itemStorage, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_failedOnUserNotExists() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("Updated Item")
                .description("Updated Description").build();

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.updateItem(itemDtoRequest, item.getId(), itemOwner.getId()));

        assertEquals(itemOwner.getId().toString(),
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(itemStorage, times(0)).findById(item.getId());
        verify(itemStorage, times(0)).save(any(Item.class));
    }

    @Test
    void updateItem_failedOnItemNotExists() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("Updated Item")
                .description("Updated Description").build();

        when(userStorage.findById(itemOwner.getId())).thenReturn(Optional.of(itemOwner));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.updateItem(itemDtoRequest, item.getId(), itemOwner.getId()));

        assertEquals(item.getId().toString(),
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(itemStorage, times(1)).findById(item.getId());
        verify(itemStorage, times(0)).save(any(Item.class));
    }

    @Test
    void updateItem_failedOnUserNotOwner() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("Updated Item")
                .description("Updated Description").build();

        when(userStorage.findById(itemSearcher.getId())).thenReturn(Optional.of(itemSearcher));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));

        Exception exception = assertThrows(PermissionException.class,
                () -> itemService.updateItem(itemDtoRequest, item.getId(), itemSearcher.getId()));

        assertEquals(itemSearcher.getId().toString(),
                exception.getMessage());

        verify(userStorage, times(1)).findById(itemSearcher.getId());
        verify(itemStorage, times(1)).findById(item.getId());
        verify(itemStorage, times(0)).save(any(Item.class));
    }

    @Test
    void deleteItem_success() {
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));

        itemService.deleteItem(item.getId(), itemOwner.getId());

        verify(itemStorage, times(1)).findById(item.getId());
        verify(itemStorage, times(1)).delete(any(Item.class));
    }

    @Test
    void deleteItem_failedOnItemNotExists() {
        when(itemStorage.findById(item.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> itemService.deleteItem(item.getId(), itemOwner.getId()));

        assertEquals(item.getId().toString(),
                exception.getMessage());

        verify(itemStorage, times(1)).findById(item.getId());
        verify(itemStorage, times(0)).delete(any(Item.class));
    }

    @Test
    void deleteItem_failedOnUserNotOwnsItem() {
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));

        Exception exception = assertThrows(PermissionException.class,
                () -> itemService.deleteItem(item.getId(), itemSearcher.getId()));

        assertEquals(itemSearcher.getId().toString(),
                exception.getMessage());

        verify(itemStorage, times(1)).findById(item.getId());
        verify(itemStorage, times(0)).delete(any(Item.class));
    }
}
