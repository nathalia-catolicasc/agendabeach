package com.bookings.repository;

public interface HolidayRepository
        extends org.springframework.data.jpa.repository.JpaRepository<
                com.bookings.entity.Holiday, java.time.LocalDate> {}
