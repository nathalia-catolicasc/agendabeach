package com.bookings.repository;

import com.bookings.entity.Court;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtRepository extends JpaRepository<Court, Long> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Court c where c.id = :id")
    java.util.Optional<Court> lockById(
            @org.springframework.data.repository.query.Param("id") Long id);
}
