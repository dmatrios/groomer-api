package com.groomerapp.api.auth.web;

import com.groomerapp.api.auth.data.UserRepository;
import com.groomerapp.api.auth.service.JwtService;
import com.groomerapp.api.auth.web.dto.ChangePasswordRequest;
import com.groomerapp.api.auth.web.dto.LoginRequest;
import com.groomerapp.api.auth.web.dto.LoginResponse;
import com.groomerapp.api.auth.web.dto.MeResponse;
import com.groomerapp.api.shared.config.AppJwtProperties;
import com.groomerapp.api.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AppJwtProperties jwtProps;
    private final PasswordEncoder passwordEncoder; // ✅ nuevo

    /* =====================
       LOGIN
       ===================== */

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {

        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getUsername(),
                        req.getPassword()
                )
        );

        var domainUser = userRepository
                .findByUsernameAndActiveTrue(req.getUsername())
                .orElseThrow();

        String token = jwtService.generateToken(auth);

        return ApiResponse.ok(
                LoginResponse.builder()
                        .accessToken(token)
                        .tokenType("Bearer")
                        .expiresInSeconds(jwtProps.getTtlMinutes() * 60)
                        .user(MeResponse.builder()
                                .id(domainUser.getId())
                                .username(domainUser.getUsername())
                                .fullName(domainUser.getFullName())
                                .role(domainUser.getRole())
                                .active(domainUser.isActive())
                                .mustChangePassword(domainUser.isMustChangePassword())
                                .build())
                        .build()
        );
    }

    /* =====================
       ME
       ===================== */

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getSubject();

        var domainUser = userRepository
                .findByUsernameAndActiveTrue(username)
                .orElseThrow();

        return ApiResponse.ok(
                MeResponse.builder()
                        .id(domainUser.getId())
                        .username(domainUser.getUsername())
                        .fullName(domainUser.getFullName())
                        .role(domainUser.getRole())
                        .active(domainUser.isActive())
                        .mustChangePassword(domainUser.isMustChangePassword())
                        .build()
        );
    }

    /* =====================
       CHANGE PASSWORD ✅
       ===================== */

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest req
    ) {
        String username = jwt.getSubject();

        var user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow();

        // 🔐 Si NO es forzado → validar currentPassword
        if (!user.isMustChangePassword()) {
            if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("Contraseña actual requerida");
            }

            if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Contraseña actual incorrecta");
            }
        }

        user.changePassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.ok(null);
    }
}
