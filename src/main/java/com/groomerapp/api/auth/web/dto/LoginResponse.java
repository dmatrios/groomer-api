package com.groomerapp.api.auth.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {
    String accessToken;
    String tokenType; // "Bearer"
    long expiresInSeconds;
    MeResponse user;
}
