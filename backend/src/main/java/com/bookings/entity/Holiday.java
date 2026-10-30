package com.bookings.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "holidays")
public class Holiday {
    @Id private LocalDate date;

    @Column(nullable = false)
    private String description;

    public Holiday() {}

    public Holiday(LocalDate date, String description) {
        this.date = date;
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
