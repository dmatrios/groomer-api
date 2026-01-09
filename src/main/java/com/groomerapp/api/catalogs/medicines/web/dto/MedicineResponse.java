package com.groomerapp.api.catalogs.medicines.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicineResponse {

    private final Long id;
    private final String name;
    private final String normalizedName;
}
