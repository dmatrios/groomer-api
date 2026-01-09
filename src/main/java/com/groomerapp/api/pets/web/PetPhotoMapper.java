package com.groomerapp.api.pets.web;

import com.groomerapp.api.pets.domain.PetPhoto;
import com.groomerapp.api.pets.web.dto.PetPhotoResponse;

public final class PetPhotoMapper {

    private PetPhotoMapper() {}

    public static PetPhotoResponse toResponse(PetPhoto photo) {
        return PetPhotoResponse.builder()
                .id(photo.getId())
                .url(photo.getUrl())
                .primary(photo.isPrimaryPhoto())
                .build();
    }
}
