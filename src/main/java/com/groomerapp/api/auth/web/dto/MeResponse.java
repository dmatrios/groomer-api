package com.groomerapp.api.auth.web.dto;

import com.groomerapp.api.auth.domain.UserRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MeResponse {
    Long id;
    String username;
    String fullName;
    UserRole role;
    boolean active;
    boolean mustChangePassword;
}
