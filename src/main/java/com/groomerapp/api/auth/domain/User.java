package com.groomerapp.api.auth.domain;

import com.groomerapp.api.shared.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        },
        indexes = {
                @Index(name = "ix_users_username", columnList = "username"),
                @Index(name = "ix_users_role", columnList = "role"),
                @Index(name = "ix_users_active", columnList = "active")
        }
)
public class User extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    /**
     * ✅ Nombre “humano” para UI:
     * Ej: "Daniel Maturrano"
     */
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String password; // BCrypt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * ✅ Opcional pero recomendado:
     * - Si el admin crea usuarios con clave temporal,
     *   forzamos a que cambien password al primer login.
     */
    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = false;

    public User(String username, String fullName, String password, UserRole role) {
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
        this.active = true;
        this.mustChangePassword = false;
    }

    /* ====== domain behaviors ====== */

    public void updateProfile(String fullName, UserRole role) {
        this.fullName = fullName;
        this.role = role;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.mustChangePassword = false;
    }

    public void forcePasswordChange() {
        this.mustChangePassword = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
