package com.groomerapp.api.visits.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class VisitDetailResponse {

    private final Long id;

    private final Long petId;
    private final String petName; // ✅ NUEVO

    private final Long appointmentId;
    private final LocalDateTime visitedAt;
    private final BigDecimal totalAmount;
    private final String notes;

    private final List<VisitItemResponse> items;
    private final PaymentResponse payment;
}
