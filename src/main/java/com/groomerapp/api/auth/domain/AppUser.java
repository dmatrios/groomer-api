package com.groomerapp.api.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private boolean active = true;

    public AppUser(String username, String passwordHash, UserRole role) {
        this.username = username.trim();
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }
}
