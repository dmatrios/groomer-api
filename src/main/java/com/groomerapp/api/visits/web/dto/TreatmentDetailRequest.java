package com.groomerapp.api.visits.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TreatmentDetailRequest {

    private Long treatmentTypeId;
    private String treatmentTypeText;

    private Long medicineId;
    private String medicineText;

    private LocalDate nextDate;
}
