package com.bookings.enums;

/** Resultado de um cancelamento segundo a RN04. */
public enum CancellationType {
    /** Mais de 24h de antecedência: reembolso integral (e devolução do crédito usado). */
    REFUND,
    /** Entre 24h e 2h: 50% do valor volta como crédito, sem reembolso em dinheiro. */
    CREDIT,
    /** Menos de 2h: sem reembolso nem crédito. */
    NONE
}
