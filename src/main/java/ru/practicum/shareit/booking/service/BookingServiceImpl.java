package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.controller.State;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.error.*;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.user.model.BookingStatus;
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
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotExistsException(bookingId.toString()));

        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return booking;
        } else {
            throw new PermissionException("У пользователя " + userId + " нет доступа к данной аренде - " + bookingId);
        }
    }

    @Override
    public List<Booking> getBookingsForBooker(Long userId, State state) {
        if (!userRepository.existsById(userId)) {
            throw new UserDoesNotExixtsException(userId.toString());
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
            throw new UserDoesNotExixtsException(userId.toString());
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

        if (!userRepository.existsById(userId)) {
            throw new UserDoesNotExixtsException(userId.toString());
        }

        if (!itemRepository.existsById(itemId)) {
            throw new ItemNotExistsExeption(itemId.toString());
        }

        if (!itemRepository.getById(itemId).getAvailable()) {
            throw new ItemUnavailableException("Вещь " + itemId + " не доступна для аренды");
        }

        if (itemRepository.getById(itemId).getOwner().getId().equals(userId)) {
            throw new OwnerBookingHisItemExeption("Пользователь " + userId
                    + " не может арендовать собственную вещь - " + itemId);
        }

        Booking booking = Booking.builder().item(itemRepository.getById(itemId)).booker(userRepository.getById(userId)).startDate(bookingRequest.getStart()).endDate(bookingRequest.getEnd()).build();

        Booking savedBooking = bookingRepository.save(booking);

        return savedBooking;
    }

    @Override
    public Booking setApprove(Long ownerId, Long bookingId, Boolean approved) {
        if (!userRepository.existsById(ownerId)) {
            throw new UserDoesNotExixtsException(ownerId.toString());
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotExistsException(bookingId.toString()));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new UserPermissionException("Пользователь " + ownerId + "не является владельцем данной вещи");
        }

        if (approved) {
            if (booking.getStatus().equals(BookingStatus.APPROVED)) {
                throw new ItemAlreadyReservedException(booking.getItem().getId().toString());
            }
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        Booking savedBooking = bookingRepository.save(booking);

        return savedBooking;
    }
}
