package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;
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
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingJpaRepository bookingRepository;
    private final UserJpaRepository userRepository;
    private final ItemJpaRepository itemRepository;

    @Transactional(readOnly = true)
    @Override
    public Booking getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotExistsExeption(bookingId.toString()));

        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return booking;
        } else {
            throw new PermissionException("У пользователя " + userId + " нет доступа к данной аренде - " + bookingId);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getBookingsForBooker(Long userId, State state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotExistsExeption(userId.toString());
        }

        LocalDateTime currentDate = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "startDate");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, sort);

        switch (state) {
            case ALL:
                return bookingRepository.findByBookerId(userId, pageable);
            case PAST:
                return bookingRepository.findByBookerIdAndEndDateLessThan(userId, currentDate, pageable);
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartDateGreaterThan(userId, currentDate, pageable);
            case CURRENT:
                return bookingRepository.findByBookerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId,
                        currentDate,
                        currentDate,
                        pageable);
            case WAITING:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            default:
                throw new UnsupportedOperationException("Неизвестный параметр state: " + state);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getBookingsForOwner(Long userId, State state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotExistsExeption(userId.toString());
        }

        LocalDateTime currentDate = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, sort);

        switch (state) {
            case ALL:
                return bookingRepository.findByItemOwnerId(userId, pageable);
            case PAST:
                return bookingRepository.findByItemOwnerIdAndEndDateLessThan(userId, currentDate, pageable);
            case FUTURE:
                return bookingRepository.findByItemOwnerIdAndStartDateGreaterThan(userId, currentDate, pageable);
            case CURRENT:
                return bookingRepository.findByItemOwnerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        userId,
                        currentDate,
                        currentDate,
                        pageable);
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            default:
                throw new UnsupportedOperationException("Unknown state:" + state);
        }
    }

    @Transactional
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

        Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .startDate(bookingRequest.getStart())
                .endDate(bookingRequest.getEnd())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        return savedBooking;
    }

    @Transactional
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
