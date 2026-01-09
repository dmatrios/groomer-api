package com.groomerapp.api.visits.web.dto;

import com.groomerapp.api.shared.domain.PaymentMethod;
import com.groomerapp.api.shared.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentResponse {

    private final PaymentStatus status;
    private final PaymentMethod method;
    private final BigDecimal amountPaid;
    private final BigDecimal balance;
}
