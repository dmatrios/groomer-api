package com.groomerapp.api.appointments.web.dto;

import com.groomerapp.api.shared.domain.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AppointmentCancelRequest {

    @NotBlank(message = "reason es obligatorio")
    private String reason;

    // opcional: si hubo cobro
    private PaymentMethod chargeMethod;
    private BigDecimal chargeAmount;
}
