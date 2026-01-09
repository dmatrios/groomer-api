package com.groomerapp.api.pets.web;

import com.groomerapp.api.pets.domain.Pet;
import com.groomerapp.api.pets.web.dto.PetResponse;

public final class PetMapper {

    private PetMapper() {}

    public static PetResponse toResponse(Pet pet, String mainPhotoUrl) {
        return PetResponse.builder()
                .id(pet.getId())
                .code(pet.getCode())
                .clientId(pet.getClientId())
                .name(pet.getName())
                .species(pet.getSpecies()) // ✅ nuevo
                .size(pet.getSize())
                .temperament(pet.getTemperament())
                .weight(pet.getWeight())
                .notes(pet.getNotes())
                .mainPhotoUrl(mainPhotoUrl)
                .build();
    }
}
