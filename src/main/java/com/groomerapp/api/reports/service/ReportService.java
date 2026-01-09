package com.groomerapp.api.reports.service;

import com.groomerapp.api.appointments.data.AppointmentChargeRepository;
import com.groomerapp.api.reports.web.dto.ReportByCategoryResponse;
import com.groomerapp.api.reports.web.dto.ReportSummaryResponse;
import com.groomerapp.api.reports.web.dto.ReportTimeseriesResponse;
import com.groomerapp.api.shared.domain.PaymentMethod;
import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.visits.data.VisitItemRepository;
import com.groomerapp.api.visits.data.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final VisitRepository visitRepository;
    private final VisitItemRepository visitItemRepository;
    private final AppointmentChargeRepository appointmentChargeRepository;

    @Transactional(readOnly = true)
    public ReportSummaryResponse summary(LocalDateTime from, LocalDateTime to, PaymentMethod paymentMethod) {
        validateRange(from, to);

        BigDecimal gross = visitRepository.sumTotalBetween(from, to, paymentMethod);
        BigDecimal adjustments = appointmentChargeRepository.sumAmountBetween(from, to, paymentMethod);
        BigDecimal net = gross.add(adjustments);

        return ReportSummaryResponse.builder()
                .from(from)
                .to(to)
                .gross(gross)
                .adjustments(adjustments)
                .net(net)
                .build();
    }

    @Transactional(readOnly = true)
    public ReportByCategoryResponse byCategory(LocalDateTime from, LocalDateTime to, PaymentMethod paymentMethod) {
        validateRange(from, to);

        var rows = visitItemRepository.sumByCategory(from, to, paymentMethod);

        BigDecimal total = rows.stream()
                .map(r -> r.getTotal() == null ? BigDecimal.ZERO : r.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ReportByCategoryResponse.Row> mapped = rows.stream()
                .map(r -> {
                    BigDecimal percent = BigDecimal.ZERO;
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        percent = r.getTotal()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(total, 2, RoundingMode.HALF_UP);
                    }
                    return ReportByCategoryResponse.Row.builder()
                            .category(r.getCategory())
                            .total(r.getTotal())
                            .percent(percent)
                            .build();
                })
                .toList();

        return ReportByCategoryResponse.builder()
                .from(from)
                .to(to)
                .rows(mapped)
                .build();
    }

    @Transactional(readOnly = true)
    public ReportTimeseriesResponse timeseries(String period, LocalDateTime from, LocalDateTime to, PaymentMethod paymentMethod) {
        validateRange(from, to);

        String p = normalize(period);
        if (!"day".equals(p) && !"month".equals(p) && !"year".equals(p)) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "period debe ser day|month|year");
        }

        // Para native queries, pasamos string (CASH|CARD|MOBILE_BANKING) o null
        String pm = paymentMethod == null ? null : paymentMethod.name();

        // 1) visits (gross)
        List<VisitRepository.TimePointRow> vRows =
                "day".equals(p) ? visitRepository.timeseriesDayVisits(from, to, pm)
                        : "month".equals(p) ? visitRepository.timeseriesMonthVisits(from, to, pm)
                        : visitRepository.timeseriesYearVisits(from, to, pm);

        // 2) charges (adjustments)
        List<AppointmentChargeRepository.TimePointRow> cRows =
                "day".equals(p) ? appointmentChargeRepository.timeseriesDayCharges(from, to, pm)
                        : "month".equals(p) ? appointmentChargeRepository.timeseriesMonthCharges(from, to, pm)
                        : appointmentChargeRepository.timeseriesYearCharges(from, to, pm);

        // 3) merge
        Map<String, BigDecimal> grossMap = new LinkedHashMap<>();
        for (var r : vRows) grossMap.put(r.getPeriod(), nz(r.getTotal()));

        Map<String, BigDecimal> adjMap = new LinkedHashMap<>();
        for (var r : cRows) adjMap.put(r.getPeriod(), nz(r.getTotal()));

        var buckets = new java.util.TreeSet<String>();
        buckets.addAll(grossMap.keySet());
        buckets.addAll(adjMap.keySet());

        List<ReportTimeseriesResponse.Point> points = buckets.stream()
                .map(b -> {
                    BigDecimal gross = grossMap.getOrDefault(b, BigDecimal.ZERO);
                    BigDecimal adj = adjMap.getOrDefault(b, BigDecimal.ZERO);
                    return ReportTimeseriesResponse.Point.builder()
                            .bucket(b)
                            .gross(gross)
                            .adjustments(adj)
                            .net(gross.add(adj))
                            .build();
                })
                .toList();

        return ReportTimeseriesResponse.builder()
                .period(p)
                .from(from)
                .to(to)
                .points(points)
                .build();
    }

    private void validateRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "from y to son obligatorios");
        }
        if (to.isBefore(from)) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "to no puede ser menor que from");
        }
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String normalize(String v) {
        if (v == null) return null;
        String t = v.trim().toLowerCase();
        return t.isBlank() ? null : t;
    }
}
