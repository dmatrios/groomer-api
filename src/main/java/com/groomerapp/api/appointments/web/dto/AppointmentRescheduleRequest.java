package com.groomerapp.api.appointments.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AppointmentRescheduleRequest {

    @NotNull(message = "startAt es obligatorio")
    private LocalDateTime startAt;

    @NotNull(message = "endAt es obligatorio")
    private LocalDateTime endAt;

    @NotBlank(message = "reason es obligatorio")
    private String reason;
}
