package com.groomerapp.api.reports.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReportTimeseriesResponse {

    private final String period; // "day" | "month"
    private final LocalDateTime from;
    private final LocalDateTime to;

    private final List<Point> points;

    @Getter
    @Builder
    public static class Point {
        private final String bucket;          // YYYY-MM-DD o YYYY-MM
        private final BigDecimal gross;       // visits
        private final BigDecimal adjustments; // charges
        private final BigDecimal net;         // gross + adjustments
        //private final String period; // "day" | "month" | "year"
    }
}
