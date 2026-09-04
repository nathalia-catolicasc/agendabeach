package com.bookings.repository;

import com.bookings.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByCourt_IdAndStartTimeBetween(Long courtId, LocalDateTime start, LocalDateTime end);

    @Query("select b from Booking b where b.user.id = :userId order by b.startTime desc")
    List<Booking> findAllByUserId(@Param("userId") Long userId);
}