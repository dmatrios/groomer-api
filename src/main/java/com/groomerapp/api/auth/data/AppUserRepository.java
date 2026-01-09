package com.groomerapp.api.auth.data;

import com.groomerapp.api.auth.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);
    Optional<AppUser> findByUsernameIgnoreCaseAndActiveTrue(String username);
    boolean existsByUsernameIgnoreCase(String username);
}
