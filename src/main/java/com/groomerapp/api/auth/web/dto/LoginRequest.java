package com.groomerapp.api.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginRequest {
    @NotBlank String username;
    @NotBlank String password;
}
