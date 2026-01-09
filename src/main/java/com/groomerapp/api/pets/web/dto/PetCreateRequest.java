package com.groomerapp.api.pets.web.dto;

import com.groomerapp.api.pets.domain.PetSize;
import com.groomerapp.api.pets.domain.PetSpecies;
import com.groomerapp.api.pets.domain.PetTemperament;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PetCreateRequest {

    @NotNull(message = "clientId es obligatorio")
    private Long clientId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
    private String name;

    // ✅ nuevo (si viene null, backend asume DOG)
    private PetSpecies species;

    @NotNull(message = "El tamaño es obligatorio")
    private PetSize size;

    @NotNull(message = "El temperamento es obligatorio")
    private PetTemperament temperament;

    private Double weight;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;
}
