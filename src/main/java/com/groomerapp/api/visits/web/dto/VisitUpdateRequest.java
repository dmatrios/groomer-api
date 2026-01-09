package com.groomerapp.api.visits.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class VisitUpdateRequest {

    @NotNull(message = "visitedAt es obligatorio")
    private LocalDateTime visitedAt;

    @Size(max = 500, message = "notes no puede exceder 500 caracteres")
    private String notes;

    @Valid
    private List<VisitItemRequest> items;

    @Valid
    private PaymentRequest payment;
}
