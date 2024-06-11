package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.controller.State;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.error.ElementAccessException;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.error.PermissionException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.user.model.BookingStatus;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingJpaRepository bookingRepository;
    private final UserJpaRepository userRepository;
    private final ItemJpaRepository itemRepository;

    @Override
    public Booking getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotExistsExeption(bookingId.toString()));

        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return booking;
        } else {
            throw new PermissionException("У пользователя " + userId + " нет доступа к данной аренде - " + bookingId);
        }
    }

    @Override
    public List<Booking> getBookingsForBooker(Long userId, State state) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotExistsExeption(userId.toString());
        }

        LocalDateTime currentDate = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "startDate");

        switch (state) {
            case ALL:
                return bookingRepository.findByBookerId(userId, sort);
            case PAST:
                return bookingRepository.findByBookerIdAndEndDateLessThan(userId, currentDate, sort);
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartDateGreaterThan(userId, currentDate, sort);
            case CURRENT:
                return bookingRepository.findByBookerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId,
                        currentDate,
                        currentDate,
                        sort);
            case WAITING:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
            default:
                throw new UnsupportedOperationException("Неизвестный параметр state: " + state);
        }
    }

    @Override
    public List<Booking> getBookingsForOwner(Long userId, State state) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotExistsExeption(userId.toString());
        }

        LocalDateTime currentDate = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        switch (state) {
            case ALL:
                return bookingRepository.findByItemOwnerId(userId, sort);
            case PAST:
                return bookingRepository.findByItemOwnerIdAndEndDateLessThan(userId, currentDate, sort);
            case FUTURE:
                return bookingRepository.findByItemOwnerIdAndStartDateGreaterThan(userId, currentDate, sort);
            case CURRENT:
                return bookingRepository.findByItemOwnerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        userId,
                        currentDate,
                        currentDate,
                        sort);
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort);
            default:
                throw new UnsupportedOperationException("Unknown state:" + state);
        }
    }

    @Override
    public Booking create(Long userId, BookingRequest bookingRequest) {
        Long itemId = bookingRequest.getItemId();

        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotExistsExeption(userId.toString()));

        Item item = itemRepository.findById(itemId).orElseThrow(()
                -> new EntityNotExistsExeption(itemId.toString()));

        if (!item.getAvailable()) {
            throw new ElementAccessException("Вещь " + itemId + " не доступна для аренды");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new PermissionException("Пользователь " + userId
                    + " не может арендовать собственную вещь - " + itemId);
        }

        if (bookingRepository.existsApprovedBookingByItemIdAndTimeRange(
                itemId,
                bookingRequest.getStart(),
                bookingRequest.getEnd())) {
            throw new PermissionException("Пользователь " + userId + " не может арендовать вещь"
                    + itemId + " в данном временном интервале: старт - "
                    + bookingRequest.getStart() + ", конец - " + bookingRequest.getEnd());
        }

        Booking booking = Booking.builder().item(item).booker(user).startDate(bookingRequest.getStart()).endDate(bookingRequest.getEnd()).build();

        Booking savedBooking = bookingRepository.save(booking);

        return savedBooking;
    }

    @Override
    public Booking setApprove(Long ownerId, Long bookingId, Boolean approved) {
        User user = userRepository.findById(ownerId).orElseThrow(() ->
                new EntityNotExistsExeption(ownerId.toString()));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotExistsExeption(bookingId.toString()));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new PermissionException("Пользователь " + ownerId + "не является владельцем данной вещи");
        }

        if (approved) {
            if (booking.getStatus().equals(BookingStatus.APPROVED)) {
                throw new ElementAccessException(booking.getItem().getId().toString());
            }
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        Booking savedBooking = bookingRepository.save(booking);

        return savedBooking;
    }
}
