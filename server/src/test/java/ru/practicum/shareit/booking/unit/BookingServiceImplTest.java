package ru.practicum.shareit.booking.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.error.ElementAccessException;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.error.PermissionException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;
import ru.practicum.shareit.utils.enums.BookingStatus;
import ru.practicum.shareit.utils.enums.State;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class BookingServiceImplTest {

    @Mock
    private BookingJpaRepository bookingRepository;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private ItemJpaRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private Item item;
    private Booking booking;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L).build();

        item = Item.builder()
                .id(1L)
                .available(true)
                .owner(user).build();

        booking = Booking.builder()
                .id(1L)
                .booker(user)
                .item(item)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING).build();
    }

    @Test
    public void testGetBooking_success() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Booking foundBooking = bookingService.getBooking(user.getId(), booking.getId());

        assertEquals(foundBooking.getId(), booking.getId());
    }

    @Test
    public void testGetBooking_failedUserDontHavePermissionToBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(PermissionException.class,
                () -> bookingService.getBooking(2L, booking.getId()));

        assertEquals("У пользователя " + 2L + " нет доступа к данной аренде - " + booking.getId(),
                exception.getMessage());
    }

    @Test
    public void testGetBookingsForBooker_success() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findByBookerId(anyLong(), any(Pageable.class)))
                .thenReturn(Collections.singletonList(booking));

        List<Booking> bookings = bookingService.getBookingsForBooker(user.getId(), State.ALL, 0, 10);

        assertFalse(bookings.isEmpty());
        assertEquals(bookings.get(0).getId(), booking.getId());
    }

    @Test
    public void testGetBookingsForBooker_failedBecauseUserDontExists() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> bookingService.getBookingsForBooker(user.getId(), State.ALL, 0, 10));

        assertEquals(user.getId().toString(), exception.getMessage());
    }

    @Test
    public void testGetBookingsForBookerWithVariousStates() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findByBookerId(anyLong(), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByBookerIdAndEndDateLessThan(anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByBookerIdAndStartDateGreaterThan(anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByBookerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByBookerIdAndStatus(anyLong(), eq(BookingStatus.WAITING), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByBookerIdAndStatus(anyLong(), eq(BookingStatus.REJECTED), any(Pageable.class))).thenReturn(Collections.singletonList(booking));

        for (State state : State.values()) {
            List<Booking> bookings = bookingService.getBookingsForBooker(user.getId(), state, 0, 10);
            assertFalse(bookings.isEmpty());
        }
    }

    @Test
    public void testGetBookingsForOwnerWithVariousStates() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findByItemOwnerId(anyLong(), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByItemOwnerIdAndEndDateLessThan(anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByItemOwnerIdAndStartDateGreaterThan(anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByItemOwnerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByItemOwnerIdAndStatus(anyLong(), eq(BookingStatus.WAITING), any(Pageable.class))).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findByItemOwnerIdAndStatus(anyLong(), eq(BookingStatus.REJECTED), any(Pageable.class))).thenReturn(Collections.singletonList(booking));

        for (State state : State.values()) {
            List<Booking> bookings = bookingService.getBookingsForOwner(user.getId(), state, 0, 10);
            assertFalse(bookings.isEmpty());
        }
    }

    @Test
    public void testCreateBooking_success() {
        User booker = User.builder().id(2L).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(item.getId());
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        Booking createdBooking = bookingService.create(booker.getId(), bookingRequest);

        assertNotNull(createdBooking);
        assertEquals(createdBooking.getId(), booking.getId());
    }

    @Test
    public void testCreateBooking_failedIfUserNotExists() {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(item.getId());
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> bookingService.create(user.getId(), bookingRequest));

        assertEquals(user.getId().toString(), exception.getMessage());
    }

    @Test
    public void testCreateBooking_failedIfItemNotExists() {
        User booker = User.builder().id(2L).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(item.getId());
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> bookingService.create(user.getId(), bookingRequest));

        assertEquals(item.getId().toString(), exception.getMessage());
    }

    @Test
    public void testCreateBooking_failedIfItemNotAvailable() {
        User booker = User.builder().id(2L).build();
        item.setAvailable(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(item.getId());
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        Exception exception = assertThrows(ElementAccessException.class,
                () -> bookingService.create(booker.getId(), bookingRequest));

        assertEquals("Вещь " + item.getId() + " не доступна для аренды", exception.getMessage());
    }

    @Test
    public void testCreateBooking_failedIfUserOwnsItemForBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(item.getId());
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        Exception exception = assertThrows(PermissionException.class,
                () -> bookingService.create(user.getId(), bookingRequest));

        assertEquals("Пользователь " + user.getId()
                + " не может арендовать собственную вещь - " + item.getId(), exception.getMessage());
    }

    @Test
    public void testCreateBooking_failedIfItemAlreadyBookedAtThisTime() {
        User booker = User.builder().id(2L).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsApprovedBookingByItemIdAndTimeRange(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(true);

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(item.getId());
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        Exception exception = assertThrows(PermissionException.class,
                () -> bookingService.create(booker.getId(), bookingRequest));

        assertEquals("Пользователь " + booker.getId() + " не может арендовать вещь"
                + item.getId() + " в данном временном интервале: старт - "
                + bookingRequest.getStart() + ", конец - " + bookingRequest.getEnd(), exception.getMessage());
    }

    @Test
    public void testSetApprove_success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking approvedBooking = bookingService.setApprove(user.getId(), booking.getId(), true);

        assertEquals(approvedBooking.getStatus(), BookingStatus.APPROVED);
    }

    @Test
    public void testSetRejected_success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking approvedBooking = bookingService.setApprove(user.getId(), booking.getId(), false);

        assertEquals(approvedBooking.getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void testSetApprove_failedIfUserNotExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> bookingService.setApprove(user.getId(), booking.getId(), true));

        assertEquals(user.getId().toString(), exception.getMessage());
    }

    @Test
    public void testSetApprove_failedIfBookingNotExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotExistsExeption.class,
                () -> bookingService.setApprove(user.getId(), booking.getId(), true));

        assertEquals(booking.getId().toString(), exception.getMessage());
    }

    @Test
    public void testSetApprove_failedIfUserDontOwnsItem() {
        User booker = User.builder().id(2L).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(PermissionException.class,
                () -> bookingService.setApprove(booker.getId(), booking.getId(), true));

        assertEquals("Пользователь " + booker.getId() + "не является владельцем данной вещи",
                exception.getMessage());
    }

    @Test
    public void testSetApprove_failedIfUserApprovedTwice() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking approvedBooking = bookingService.setApprove(user.getId(), booking.getId(), true);

        assertEquals(approvedBooking.getStatus(), BookingStatus.APPROVED);

        Exception exception = assertThrows(ElementAccessException.class,
                () -> bookingService.setApprove(user.getId(), booking.getId(), true));

        assertEquals(booking.getItem().getId().toString(),
                exception.getMessage());
    }
}
