package com.bookings.service;

/** Horário de funcionamento (RN01) e início do horário de pico (RN02), em horas cheias. */
public final class BusinessHours {
    /** Primeiro horário reservável: 06:00. */
    public static final int OPENING_HOUR = 6;

    /** Fim do último horário: 23:00 (o último início é 22:00). */
    public static final int CLOSING_HOUR = 23;

    /** A partir das 17:00 vale o preço de pico em dias úteis. */
    public static final int PEAK_START_HOUR = 17;

    private BusinessHours() {}
}
