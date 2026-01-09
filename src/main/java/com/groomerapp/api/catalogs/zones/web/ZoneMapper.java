package com.groomerapp.api.catalogs.zones.web;

import com.groomerapp.api.catalogs.zones.domain.Zone;
import com.groomerapp.api.catalogs.zones.web.dto.ZoneResponse;

public final class ZoneMapper {

    private ZoneMapper() {}

    public static ZoneResponse toResponse(Zone zone) {
        return ZoneResponse.builder()
                .id(zone.getId())
                .name(zone.getName())
                .normalizedName(zone.getNormalizedName())
                .build();
    }
}
