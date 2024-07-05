package ru.practicum.shareit.booking.integrity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;
import ru.practicum.shareit.utils.enums.BookingStatus;
import ru.practicum.shareit.utils.enums.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BookingServiceIntegrationalTest {

    @Autowired
    private BookingJpaRepository bookingRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ItemJpaRepository itemRepository;

    @Autowired
    private BookingService bookingService;

    private User user;
    private Item item;
    private Booking booking;
    private User booker;

    @BeforeEach
    public void setUp() {
        booker = User.builder().name("Test Booker").email("testBooker@example.com").build();
        booker = userRepository.save(booker);

        user = User.builder().name("Test User").email("testuser@example.com").build();
        user = userRepository.save(user);

        item = Item.builder().name("Test Item").description("This is a test item").available(true).owner(user).build();
        item = itemRepository.save(item);

        booking = Booking.builder().booker(booker)
                .item(item)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build();
        booking = bookingRepository.save(booking);
    }

    @Test
    public void testGetBooking() {
        Booking foundBookingForOwner = bookingService.getBooking(user.getId(), booking.getId());

        assertNotNull(foundBookingForOwner);
        assertEquals(booking.getId(), foundBookingForOwner.getId());

        Booking foundBookingForBooker = bookingService.getBooking(booker.getId(), booking.getId());

        assertNotNull(foundBookingForOwner);
        assertEquals(booking.getId(), foundBookingForBooker.getId());
    }

    @Test
    public void testGetBookingsForBooker() {
        List<Booking> bookings = bookingService.getBookingsForBooker(booker.getId(), State.ALL, 0, 10);

        assertFalse(bookings.isEmpty());
        assertEquals(bookings.get(0).getId(), booking.getId());
    }

    @Test
    public void testCreateBooking() {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(item.getId());
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        Booking createdBooking = bookingService.create(booker.getId(), bookingRequest);

        assertNotNull(createdBooking);
        assertEquals(createdBooking.getItem().getId(), item.getId());
        assertEquals(createdBooking.getBooker().getId(), booker.getId());
    }

    @Test
    public void testSetApprove() {
        Booking approvedBooking = bookingService.setApprove(user.getId(), booking.getId(), true);

        assertNotNull(approvedBooking);
        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    public void testGetBookingsForBookerWithVariousStates() {
        booking.setEndDate(LocalDateTime.now().minusDays(2));
        bookingRepository.save(booking);

        List<Booking> bookings = bookingService.getBookingsForBooker(booker.getId(), State.PAST, 0, 10);
        assertFalse(bookings.isEmpty());
        assertEquals(bookings.get(0).getId(), booking.getId());

        booking.setEndDate(LocalDateTime.now().plusDays(4));
        booking.setStartDate(LocalDateTime.now().plusDays(2));
        bookingRepository.save(booking);

        bookings = bookingService.getBookingsForBooker(booker.getId(), State.FUTURE, 0, 10);
        assertFalse(bookings.isEmpty());
        assertEquals(bookings.get(0).getId(), booking.getId());
    }

    @Test
    public void testGetBookingsForOwnerWithVariousStates() {
        booking.setEndDate(LocalDateTime.now().minusDays(2));
        bookingRepository.save(booking);

        List<Booking> bookings = bookingService.getBookingsForOwner(user.getId(), State.PAST, 0, 10);
        assertFalse(bookings.isEmpty());
        assertEquals(bookings.get(0).getId(), booking.getId());

        booking.setEndDate(LocalDateTime.now().plusDays(4));
        booking.setStartDate(LocalDateTime.now().plusDays(2));
        bookingRepository.save(booking);

        bookings = bookingService.getBookingsForOwner(user.getId(), State.FUTURE, 0, 10);
        assertFalse(bookings.isEmpty());
        assertEquals(bookings.get(0).getId(), booking.getId());
    }
}