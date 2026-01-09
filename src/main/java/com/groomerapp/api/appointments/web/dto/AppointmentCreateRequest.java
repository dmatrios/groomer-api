package com.groomerapp.api.appointments.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AppointmentCreateRequest {

    @NotNull(message = "petId es obligatorio")
    private Long petId;

    @NotNull(message = "startAt es obligatorio")
    private LocalDateTime startAt;

    @NotNull(message = "endAt es obligatorio")
    private LocalDateTime endAt;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;


}
