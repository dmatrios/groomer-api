package com.groomerapp.api.catalogs.zones.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ZoneResponse {

    private final Long id;
    private final String name;
    private final String normalizedName;
}
