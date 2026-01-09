package com.groomerapp.api.visits.web.dto;

import com.groomerapp.api.visits.domain.VisitItemCategory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class VisitItemResponse {

    private final Long id;
    private final VisitItemCategory category;
    private final BigDecimal price;
    private final TreatmentDetailResponse treatmentDetail;
}
