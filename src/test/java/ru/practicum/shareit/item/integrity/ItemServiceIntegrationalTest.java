package ru.practicum.shareit.item.integrity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.error.EntityNotExistsExeption;
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
import ru.practicum.shareit.utils.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ItemServiceIntegrationalTest {
    @Autowired
    private ItemJpaRepository itemStorage;
    @Autowired
    private UserJpaRepository userStorage;
    @Autowired
    private BookingJpaRepository bookingStorage;
    @Autowired
    private CommentJpaRepository commentStorage;
    @Autowired
    private ItemRequestJpaRepository itemRequestStorage;
    @Autowired
    private ReplyJpaRepository replyStorage;
    @Autowired
    private ItemServiceImpl itemService;

    private User itemOwner;
    private User itemSearcher;
    private Item item;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        itemOwner = User.builder()
                .name("ItemOwner")
                .email("owner@email.com")
                .build();

        itemOwner = userStorage.save(itemOwner);

        itemSearcher = User.builder()
                .name("ItemSearcher")
                .email("searcher@email.ru")
                .build();

        itemSearcher = userStorage.save(itemSearcher);

        item = Item.builder()
                .owner(itemOwner)
                .name("test item")
                .description("test")
                .available(true).build();

        item = itemStorage.save(item);

        itemRequest = ItemRequest.builder()
                .applicant(itemSearcher)
                .description("FindSomeItem")
                .build();

        itemRequest = itemRequestStorage.save(itemRequest);
    }

    @Test
    void getItem() {
        ItemDtoOwner foundItem = itemService.getItem(itemOwner.getId(), item.getId());

        assertNotNull(foundItem);
        assertEquals(foundItem.getId(), item.getId());
    }

    @Test
    void getItems() {
        List<ItemDtoOwner> foundItems = itemService.getItems(itemOwner.getId(), 0, 10);

        assertNotNull(foundItems);
        assertEquals(foundItems.get(0).getId(), item.getId());
    }

    @Test
    void findItems() {
        List<ItemDto> foundItems = itemService.findItems("tes", 0, 10);
        assertNotNull(foundItems);
        assertEquals(foundItems.get(0).getId(), item.getId());
    }

    @Test
    void createItem_withRequestId() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("AddedItem")
                .description("This is new item")
                .available(false)
                .requestId(itemRequest.getId()).build();

        ItemDto itemDto = itemService.createItem(itemDtoRequest, itemOwner.getId());

        Reply reply = replyStorage.findAll().get(0);

        assertNotNull(itemDto);
        assertEquals(itemDto.getName(), itemDtoRequest.getName());
        assertEquals(itemDto.getOwner().getId(), itemOwner.getId());
        assertNotNull(reply);
        assertEquals(reply.getItem().getId(), itemDto.getId());
    }

    @Test
    void createItem_withoutRequestId() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("AddedItem")
                .description("This is new item")
                .available(false)
                .build();

        ItemDto itemDto = itemService.createItem(itemDtoRequest, itemOwner.getId());

        List<Reply> reply = replyStorage.findAll();

        assertNotNull(itemDto);
        assertEquals(itemDto.getName(), itemDtoRequest.getName());
        assertEquals(itemDto.getOwner().getId(), itemOwner.getId());
        assertEquals(0, reply.size());
    }

    @Test
    void createComment() {
        CommentDtoRequest commentDtoRequest = new CommentDtoRequest("New Comment");
        Booking booking = Booking.builder()
                .item(item)
                .booker(itemSearcher)
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();
        booking = bookingStorage.save(booking);
        booking.setStatus(BookingStatus.APPROVED);
        bookingStorage.save(booking);
        Comment comment = itemService.createComment(itemSearcher.getId(), commentDtoRequest, item.getId());

        assertNotNull(comment);
        assertEquals(comment.getText(), commentDtoRequest.getText());
    }

    @Test
    void updateItem() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("UpdatedItem")
                .description("This is old item")
                .available(false)
                .build();

        ItemDto updatedtItem = itemService.updateItem(itemDtoRequest, item.getId(), itemOwner.getId());

        assertNotNull(updatedtItem);
        assertEquals(item.getId(), updatedtItem.getId());
        assertEquals(false, updatedtItem.getAvailable());
        assertEquals("UpdatedItem", updatedtItem.getName());
    }

    @Test
    void deleteItem() {
        ItemDtoOwner savedItem = itemService.getItem(itemOwner.getId(), item.getId());

        assertNotNull(savedItem);

        itemService.deleteItem(item.getId(), itemOwner.getId());

        assertThrows(EntityNotExistsExeption.class,
                () -> itemService.getItem(itemOwner.getId(), item.getId()));
    }

}
