package com.groomerapp.api.visits.web.dto;

import com.groomerapp.api.visits.domain.VisitItemCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class VisitItemRequest {

    @NotNull(message = "category es obligatorio")
    private VisitItemCategory category;

    @NotNull(message = "price es obligatorio")
    private BigDecimal price;

    @Valid
    private TreatmentDetailRequest treatmentDetail;
}
