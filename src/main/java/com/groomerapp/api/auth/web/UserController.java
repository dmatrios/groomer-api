package com.groomerapp.api.auth.web;

import com.groomerapp.api.auth.data.UserRepository;
import com.groomerapp.api.auth.domain.User;
import com.groomerapp.api.auth.domain.UserRole;
import com.groomerapp.api.auth.service.TemporaryPasswordGenerator;
import com.groomerapp.api.auth.web.dto.ResetPasswordResponse;
import com.groomerapp.api.shared.web.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordGenerator passwordGenerator;

    @GetMapping
    public ApiResponse<List<UserResponse>> list() {
        return ApiResponse.ok(
                userRepository.findAll().stream()
                        .map(UserResponse::from)
                        .toList()
        );
    }

    @PostMapping
    public ApiResponse<UserResponse> create(@RequestBody CreateUserRequest req) {

        User user = new User(
                req.username,
                req.fullName,
                passwordEncoder.encode(req.password),
                req.role
        );

        if (req.forcePasswordChange) {
            user.forcePasswordChange();
        }

        userRepository.save(user);

        return ApiResponse.ok(UserResponse.from(user));
    }

    @PostMapping("/{id}/desactivate")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        var user = userRepository.findById(id).orElseThrow();
        user.deactivate();
        userRepository.save(user);
        return ApiResponse.ok(null);
    }

    // ✅ NUEVO: activar usuario
    @PostMapping("/{id}/activate")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        var user = userRepository.findById(id).orElseThrow();
        user.activate();
        userRepository.save(user);
        return ApiResponse.ok(null);
    }

    /* ========= DTOs ========= */

    @Value
    public static class CreateUserRequest {
        @NotBlank String username;
        @NotBlank String fullName;
        @NotBlank String password;
        @NotNull UserRole role;
        boolean forcePasswordChange;
    }

    @Value
    @Builder
    public static class UserResponse {
        Long id;
        String username;
        String fullName;
        UserRole role;
        boolean active;

        static UserResponse from(User u) {
            return UserResponse.builder()
                    .id(u.getId())
                    .username(u.getUsername())
                    .fullName(u.getFullName())
                    .role(u.getRole())
                    .active(u.isActive())
                    .build();
        }
    }
    @PostMapping("/{id}/reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(@PathVariable Long id) {

        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede resetear la contraseña de un usuario inactivo"
            );
        }

        // 1️⃣ generar clave temporal
        String tempPassword = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);

        // 2️⃣ aplicar cambio
        user.changePassword(passwordEncoder.encode(tempPassword));
        user.forcePasswordChange();

        userRepository.save(user);

        // 3️⃣ devolver SOLO la clave temporal
        return ApiResponse.ok(new ResetPasswordResponse(tempPassword));
    }
}
