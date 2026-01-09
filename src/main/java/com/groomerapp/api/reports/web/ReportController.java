package com.groomerapp.api.reports.web;

import com.groomerapp.api.reports.service.ReportService;
import com.groomerapp.api.reports.web.dto.ReportByCategoryResponse;
import com.groomerapp.api.reports.web.dto.ReportSummaryResponse;
import com.groomerapp.api.reports.web.dto.ReportTimeseriesResponse;
import com.groomerapp.api.shared.domain.PaymentMethod;
import com.groomerapp.api.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    public ApiResponse<ReportSummaryResponse> summary(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @RequestParam(required = false) PaymentMethod paymentMethod
    ) {
        return ApiResponse.ok(reportService.summary(from, to, paymentMethod));
    }

    @GetMapping("/by-category")
    public ApiResponse<ReportByCategoryResponse> byCategory(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @RequestParam(required = false) PaymentMethod paymentMethod
    ) {
        return ApiResponse.ok(reportService.byCategory(from, to, paymentMethod));
    }

    @GetMapping("/timeseries")
    public ApiResponse<ReportTimeseriesResponse> timeseries(
            @RequestParam String period,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @RequestParam(required = false) PaymentMethod paymentMethod
    ) {
        return ApiResponse.ok(reportService.timeseries(period, from, to, paymentMethod));
    }
}
