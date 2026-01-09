package com.groomerapp.api.pets.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PetPhotoResponse {

    private final Long id;
    private final String url;
    private final boolean primary;
}
