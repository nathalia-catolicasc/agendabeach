package com.bookings.repository;

import com.bookings.entity.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b.user.id from Booking b where b.id = :id")
    java.util.Optional<Long> ownerId(@Param("id") Long id);

    long countByUserIdAndStatusAndStartTimeAfter(
            Long userId, com.bookings.enums.BookingStatus status, LocalDateTime now);

    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    java.util.Optional<Booking> lockById(@Param("id") Long id);

    @Query(
            "select b from Booking b where b.court.id = :court and b.status = :status and"
                    + " b.startTime < :end and b.endTime > :start")
    List<Booking> findOverlapping(
            @Param("court") Long court,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") com.bookings.enums.BookingStatus status);

    default List<Booking> overlapping(Long court, LocalDateTime start, LocalDateTime end) {
        return findOverlapping(court, start, end, com.bookings.enums.BookingStatus.SCHEDULED);
    }

    List<Booking> findAllByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByCourt_IdAndStartTimeBetween(
            Long courtId, LocalDateTime start, LocalDateTime end);

    @Query("select b from Booking b where b.user.id = :userId order by b.startTime desc")
    List<Booking> findAllByUserId(@Param("userId") Long userId);
}
