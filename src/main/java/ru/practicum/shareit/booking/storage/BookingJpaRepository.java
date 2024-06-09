package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.user.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingJpaRepository extends JpaRepository<Booking, Long> {

    // Поиск текущих бронирований по bookerId
    List<Booking> findByBookerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long bookerId,
                                                                                    LocalDateTime startDate,
                                                                                    LocalDateTime endDate,
                                                                                    Sort sort);

    // Поиск прошедших бронирований по bookerId
    List<Booking> findByBookerIdAndEndDateLessThan(Long bookerId,
                                                   LocalDateTime endDate,
                                                   Sort sort);

    // Поиск будущих бронирований по bookerId
    List<Booking> findByBookerIdAndStartDateGreaterThan(Long bookerId,
                                                        LocalDateTime startDate,
                                                        Sort sort);

    // Поиск всех бронирований по bookerId
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    // Поиск бронирований со статусом WAITING или REJECTED по bookerId
    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    // Поиск текущих бронирований по ownerId
    List<Booking> findByItemOwnerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long ownerId,
                                                                                       LocalDateTime startDate,
                                                                                       LocalDateTime endDate,
                                                                                       Sort sort);

    // Поиск прошедших бронирований по ownerId
    List<Booking> findByItemOwnerIdAndEndDateLessThan(Long ownerId,
                                                      LocalDateTime endDate,
                                                      Sort sort);

    // Поиск будущих бронирований по ownerId
    List<Booking> findByItemOwnerIdAndStartDateGreaterThan(Long ownerId,
                                                           LocalDateTime startDate,
                                                           Sort sort);

    // Поиск всех бронирований по ownerId
    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    // Поиск бронирований со статусом WAITING или REJECTED по ownerId
    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    @Query("SELECT b.id as id, b.booker.id as bookerId FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.startDate < CURRENT_TIMESTAMP ORDER BY b.startDate DESC")
    List<ItemDtoOwner.LastBooking> findLastBookingForItem(Long itemId);

    @Query("SELECT b.id as id, b.booker.id as bookerId FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.startDate > CURRENT_TIMESTAMP ORDER BY b.startDate ASC")
    List<ItemDtoOwner.NextBooking> findNextBookingForItem(Long itemId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :userId " +
            "AND b.status = 'APPROVED' " +
            "AND b.endDate < CURRENT_TIMESTAMP")
    List<Booking> findCompletedBookingForUserAndItem(@Param("itemId") Long itemId, @Param("userId") Long userId);


    // Блин, вот создаешь метод, который проверяет на оверлап, а оказывается он не нужен и даже мешает тестам
//    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
//            "FROM Booking b " +
//            "WHERE b.item.id = :itemId " +
//            "AND b.status = 'APPROVED' " +
//            "AND b.id <> :bookingId " +
//            "AND ((b.startDate < :endDate AND b.endDate > :startDate) " +
//            "OR (b.startDate >= :startDate AND b.startDate < :endDate) " +
//            "OR (b.endDate > :startDate AND b.endDate <= :endDate))")
//    boolean existsApprovedBookingByItemIdAndTimeRange(@Param("itemId") Long itemId,
//                                                      @Param("bookingId") Long bookingId,
//                                                      @Param("startDate") LocalDateTime startDate,
//                                                      @Param("endDate") LocalDateTime endDate);
}
