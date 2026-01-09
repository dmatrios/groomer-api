package com.groomerapp.api.pets.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PetPhotoCreateRequest {

    @NotBlank(message = "La url es obligatoria")
    @Size(max = 300, message = "La url no puede exceder 300 caracteres")
    private String url;
}
