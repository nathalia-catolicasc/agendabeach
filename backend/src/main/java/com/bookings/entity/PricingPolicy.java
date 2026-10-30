package com.bookings.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pricing_policy")
public class PricingPolicy {
    @Id private Long id = 1L;

    @Column(nullable = false)
    private BigDecimal regularPrice = new BigDecimal("60.00");

    @Column(nullable = false)
    private BigDecimal peakPrice = new BigDecimal("100.00");

    public BigDecimal getRegularPrice() {
        return regularPrice;
    }

    public BigDecimal getPeakPrice() {
        return peakPrice;
    }

    public void setRegularPrice(BigDecimal value) {
        regularPrice = value;
    }

    public void setPeakPrice(BigDecimal value) {
        peakPrice = value;
    }
}
