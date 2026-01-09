package com.groomerapp.api.catalogs.treatmenttypes.web;

import com.groomerapp.api.catalogs.treatmenttypes.domain.TreatmentType;
import com.groomerapp.api.catalogs.treatmenttypes.web.dto.TreatmentTypeResponse;

public final class TreatmentTypeMapper {

    private TreatmentTypeMapper() {}

    public static TreatmentTypeResponse toResponse(TreatmentType entity) {
        return TreatmentTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .normalizedName(entity.getNormalizedName())
                .build();
    }
}
