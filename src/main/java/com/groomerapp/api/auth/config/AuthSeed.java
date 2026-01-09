package com.groomerapp.api.auth.config;

import com.groomerapp.api.auth.data.UserRepository;
import com.groomerapp.api.auth.domain.User;
import com.groomerapp.api.auth.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AuthSeed {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedAdmin() {
        return args -> {
            if (userRepository.existsByUsername("admin")) return;

            userRepository.save(
                    new User(
                            "admin",
                            "Administrador del sistema",   // 👈 fullName
                            passwordEncoder.encode("admin123"),
                            UserRole.ADMIN
                    )
            );
        };
    }
}
