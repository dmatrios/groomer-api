package com.groomerapp.api.auth.data;

import com.groomerapp.api.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndActiveTrue(String username);

    boolean existsByUsername(String username);
}
