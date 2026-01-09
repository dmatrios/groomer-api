package com.groomerapp.api.appointments.web;

import com.groomerapp.api.appointments.domain.AppointmentStatus;
import com.groomerapp.api.appointments.service.AppointmentService;
import com.groomerapp.api.appointments.web.dto.*;
import com.groomerapp.api.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping
    public ApiResponse<AppointmentResponse> create(
            @RequestParam(defaultValue = "false") boolean forceOverlap,
            @Valid @RequestBody AppointmentCreateRequest request
    ) {
        var created = service.create(
                request.getPetId(),
                request.getStartAt(),
                request.getEndAt(),
                request.getNotes(),
                forceOverlap
        );
        return ApiResponse.ok(AppointmentMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ApiResponse<AppointmentResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(AppointmentMapper.toResponse(service.getById(id)));
    }

    @GetMapping
    public ApiResponse<List<AppointmentResponse>> list(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @RequestParam(required = false) AppointmentStatus status
    ) {
        List<AppointmentResponse> data = service.list(from, to, status).stream()
                .map(AppointmentMapper::toResponse)
                .toList();

        return ApiResponse.ok(data);
    }

    @PutMapping("/{id}")
    public ApiResponse<AppointmentResponse> update(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean forceOverlap,
            @Valid @RequestBody AppointmentUpdateRequest request
    ) {
        var updated = service.update(
                id,
                request.getStartAt(),
                request.getEndAt(),
                request.getNotes(),
                forceOverlap
        );
        return ApiResponse.ok(AppointmentMapper.toResponse(updated));
    }

    @PostMapping("/{id}/reschedule")
    public ApiResponse<AppointmentResponse> reschedule(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean forceOverlap,
            @Valid @RequestBody AppointmentRescheduleRequest request
    ) {
        var updated = service.reschedule(
                id,
                request.getStartAt(),
                request.getEndAt(),
                request.getReason(),
                forceOverlap
        );
        return ApiResponse.ok(AppointmentMapper.toResponse(updated));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<AppointmentResponse> cancel(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentCancelRequest request
    ) {
        var canceled = service.cancel(
                id,
                request.getReason(),
                request.getChargeMethod(),
                request.getChargeAmount()
        );
        return ApiResponse.ok(AppointmentMapper.toResponse(canceled));
    }
    @PostMapping("/{id}/attend")
    public ApiResponse<AppointmentResponse> attend(@PathVariable Long id) {
        var attended = service.markAttended(id);
        return ApiResponse.ok(AppointmentMapper.toResponse(attended));
    }

}
