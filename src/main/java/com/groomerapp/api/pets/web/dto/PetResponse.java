package com.groomerapp.api.pets.web.dto;

import com.groomerapp.api.pets.domain.PetSize;
import com.groomerapp.api.pets.domain.PetSpecies;
import com.groomerapp.api.pets.domain.PetTemperament;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PetResponse {

    private final Long id;
    private final String code;
    private final Long clientId;
    private final String name;
    private final PetSpecies species; // ✅ nuevo
    private final PetSize size;
    private final PetTemperament temperament;
    private final Double weight;
    private final String notes;
    private final String mainPhotoUrl; // opcional (puede ser null)
}
