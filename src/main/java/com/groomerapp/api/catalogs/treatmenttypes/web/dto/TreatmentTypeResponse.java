package com.groomerapp.api.catalogs.treatmenttypes.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TreatmentTypeResponse {

    private final Long id;
    private final String name;
    private final String normalizedName;
}
