package com.groomerapp.api.clients.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClientCreateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
    private String firstName;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80, message = "Los apellidos no pueden exceder 80 caracteres")
    private String lastName;

    // Zona por catálogo (opcional)
    private Long zoneId;

    // Zona libre (opcional)
    @Size(max = 120, message = "La zona no puede exceder 120 caracteres")
    private String zoneText;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;
}
