package com.groomerapp.api.auth.service;

import com.groomerapp.api.shared.config.AppJwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder encoder;
    private final AppJwtProperties props;

    public String generateToken(Authentication auth) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getTtlMinutes() * 60);

        var authorities = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())   // "ROLE_ADMIN", "ROLE_USER"
                .toList();

        var claims = JwtClaimsSet.builder()
                .issuer("groomer-api")
                .issuedAt(now)
                .expiresAt(exp)
                .subject(auth.getName())
                .claim("roles", authorities)
                .build();

        // ✅ IMPORTANTE: header HS256
        var header = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
