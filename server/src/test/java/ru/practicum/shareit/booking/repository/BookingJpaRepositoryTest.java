package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;
import ru.practicum.shareit.utils.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
public class BookingJpaRepositoryTest {

    @Autowired
    private BookingJpaRepository bookingRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ItemJpaRepository itemRepository;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        booker = User.builder()
                .name("Test Booker")
                .email("booker@example.com")
                .build();
        booker = userRepository.save(booker);

        owner = User.builder()
                .name("Test Owner")
                .email("owner@example.com")
                .build();
        owner = userRepository.save(owner);

        item = Item.builder()
                .name("Test Item")
                .description("Test Description")
                .owner(owner)
                .available(true)
                .build();
        item = itemRepository.save(item);

        booking = Booking.builder()
                .item(item)
                .booker(booker)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();
        booking = bookingRepository.save(booking);
    }

    @Test
    public void testFindByBookerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                booker.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1), PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
    }

    @Test
    public void testFindByBookerIdAndEndDateLessThan() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndEndDateLessThan(
                booker.getId(), LocalDateTime.now().plusDays(3), PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
    }

    @Test
    public void testFindByBookerIdAndStartDateGreaterThan() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndStartDateGreaterThan(
                booker.getId(), LocalDateTime.now(), PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
    }

    @Test
    public void testFindByBookerId() {
        List<Booking> bookings = bookingRepository.findByBookerId(booker.getId(), PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
        assertThat(bookings.get(0).getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    public void testFindByBookerIdAndStatus() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndStatus(
                booker.getId(), BookingStatus.WAITING, PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
        List<Booking> emptyBookings = bookingRepository.findByBookerIdAndStatus(
                booker.getId(), BookingStatus.APPROVED, PageRequest.of(0, 10));
        assertThat(emptyBookings).isEmpty();
    }

    @Test
    public void testFindByItemOwnerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                owner.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1), PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
    }

    @Test
    public void testFindByItemOwnerIdAndEndDateLessThan() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndEndDateLessThan(
                owner.getId(), LocalDateTime.now().plusDays(3), PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
    }

    @Test
    public void testFindByItemOwnerIdAndStartDateGreaterThan() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStartDateGreaterThan(
                owner.getId(), LocalDateTime.now(), PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
    }

    @Test
    public void testFindByItemOwnerId() {
        List<Booking> bookings = bookingRepository.findByItemOwnerId(owner.getId(), PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
        assertThat(bookings.get(0).getItem().getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    public void testFindByItemOwnerIdAndStatus() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStatus(
                owner.getId(), BookingStatus.WAITING, PageRequest.of(0, 10));
        assertThat(bookings).isNotEmpty();
        List<Booking> emptyBookings = bookingRepository.findByBookerIdAndStatus(
                owner.getId(), BookingStatus.APPROVED, PageRequest.of(0, 10));
        assertThat(emptyBookings).isEmpty();
    }

    @Test
    public void testFindLastBookingForItem() {
        booking.setStartDate(LocalDateTime.now().minusDays(3));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        Booking lastBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now())
                .build();

        bookingRepository.save(lastBooking);
        lastBooking.setStatus(BookingStatus.APPROVED);
        lastBooking = bookingRepository.save(lastBooking);

        List<ItemDtoOwner.LastBooking> lastBookings = bookingRepository.findLastBookingForItem(item.getId());
        assertThat(lastBookings).isNotEmpty();
        assertThat(lastBookings.get(0).getid()).isEqualTo(lastBooking.getId());
    }

    @Test
    public void testFindNextBookingForItem() {
        booking.setStartDate(LocalDateTime.now().plusDays(7));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        Booking nextBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(4))
                .build();

        bookingRepository.save(nextBooking);
        nextBooking.setStatus(BookingStatus.APPROVED);
        nextBooking = bookingRepository.save(nextBooking);

        List<ItemDtoOwner.NextBooking> nextBookings = bookingRepository.findNextBookingForItem(item.getId());
        assertThat(nextBookings).isNotEmpty();
        assertThat(nextBookings.size()).isEqualTo(2);
        assertThat(nextBookings.get(0).getid()).isEqualTo(nextBooking.getId());
    }

    @Test
    public void testFindCompletedBookingForUserAndItem() {
        booking.setEndDate(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        Booking uncompletedBooking = Booking.builder()
                .item(item)
                .booker(booker)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(4))
                .build();

        bookingRepository.save(uncompletedBooking);
        uncompletedBooking.setStatus(BookingStatus.APPROVED);
        uncompletedBooking = bookingRepository.save(uncompletedBooking);

        List<Booking> bookings = bookingRepository.findCompletedBookingForUserAndItem(item.getId(), booker.getId());
        assertThat(bookings).isNotEmpty();
        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    public void testExistsApprovedBookingByItemIdAndTimeRange() {
        boolean exists = bookingRepository.existsApprovedBookingByItemIdAndTimeRange(
                item.getId(), booking.getStartDate(), booking.getEndDate());
        assertThat(exists).isFalse();

        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        exists = bookingRepository.existsApprovedBookingByItemIdAndTimeRange(
                item.getId(), booking.getStartDate(), booking.getEndDate());
        assertThat(exists).isTrue();
    }
}

