package com.groomerapp.api.auth.web.dto;

import lombok.Value;

@Value
public class ResetPasswordResponse {
    String temporaryPassword;
}
