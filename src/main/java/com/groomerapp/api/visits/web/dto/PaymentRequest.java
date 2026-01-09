package com.groomerapp.api.visits.web.dto;

import com.groomerapp.api.shared.domain.PaymentMethod;
import com.groomerapp.api.shared.domain.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PaymentRequest {

    private PaymentStatus status;
    private PaymentMethod method;
    private BigDecimal amountPaid;
}
