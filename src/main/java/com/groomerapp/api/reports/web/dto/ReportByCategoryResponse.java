package com.groomerapp.api.reports.web.dto;

import com.groomerapp.api.visits.domain.VisitItemCategory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReportByCategoryResponse {

    private final LocalDateTime from;
    private final LocalDateTime to;

    private final List<Row> rows;

    @Getter
    @Builder
    public static class Row {
        private final VisitItemCategory category;
        private final BigDecimal total;
        private final BigDecimal percent; // 0..100
    }
}
