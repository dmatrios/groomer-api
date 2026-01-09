package com.groomerapp.api.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class ChangePasswordRequest {


    String currentPassword;

    @NotBlank
    String newPassword;
}
