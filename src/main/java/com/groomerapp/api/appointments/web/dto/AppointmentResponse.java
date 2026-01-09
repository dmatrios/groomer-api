package com.groomerapp.api.appointments.web.dto;

import com.groomerapp.api.appointments.domain.AppointmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AppointmentResponse {

    private final Long id;
    private final Long petId;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final AppointmentStatus status;
    private final String notes;
}
