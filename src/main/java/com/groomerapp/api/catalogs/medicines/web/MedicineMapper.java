package com.groomerapp.api.catalogs.medicines.web;

import com.groomerapp.api.catalogs.medicines.domain.Medicine;
import com.groomerapp.api.catalogs.medicines.web.dto.MedicineResponse;

public final class MedicineMapper {

    private MedicineMapper() {}

    public static MedicineResponse toResponse(Medicine entity) {
        return MedicineResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .normalizedName(entity.getNormalizedName())
                .build();
    }
}
