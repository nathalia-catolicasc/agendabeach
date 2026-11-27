package com.bookings.dto;

import com.bookings.enums.CancellationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Política de cancelamento (RN04) válida no momento da consulta. O frontend exibe estes valores
 * em vez de recalcular a regra.
 *
 * @param type resultado se a reserva for cancelada agora
 * @param percentage percentual do valor da reserva devolvido (100, 50 ou 0)
 * @param refundAmount valor devolvido em dinheiro se cancelar agora
 * @param creditAmount valor devolvido como crédito se cancelar agora
 * @param refundDeadline último instante com reembolso integral (24h antes do início)
 * @param creditDeadline último instante com crédito de 50% (2h antes do início)
 */
public record CancellationPolicyDTO(
        CancellationType type,
        BigDecimal percentage,
        BigDecimal refundAmount,
        BigDecimal creditAmount,
        LocalDateTime refundDeadline,
        LocalDateTime creditDeadline) {}
