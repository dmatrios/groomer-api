package com.groomerapp.api.reports.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReportSummaryResponse {
    private final LocalDateTime from;
    private final LocalDateTime to;

    private final BigDecimal gross;       // visits
    private final BigDecimal adjustments; // appointment_charge
    private final BigDecimal net;         // gross + adjustments
}
