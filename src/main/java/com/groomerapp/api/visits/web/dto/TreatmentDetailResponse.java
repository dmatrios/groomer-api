package com.groomerapp.api.visits.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TreatmentDetailResponse {

    private final Long treatmentTypeId;
    private final String treatmentTypeText;
    private final Long medicineId;
    private final String medicineText;
    private final LocalDate nextDate;
}
