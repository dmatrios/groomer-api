package com.groomerapp.api.clients.domain;

import com.groomerapp.api.shared.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "client",
        indexes = {
                @Index(name = "ix_client_code", columnList = "code"),
                @Index(name = "ix_client_zone_id", columnList = "zone_id"),
                @Index(name = "ix_client_last_name", columnList = "last_name")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_client_code", columnNames = "code")
        }
)
public class Client extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código visible para búsqueda: CL-000001 (se setea en service)
    @Column(name = "code", length = 20, unique = true)
    private String code;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    // Zona por catálogo (opcional)
    @Column(name = "zone_id")
    private Long zoneId;

    // Zona libre (opcional)
    @Column(name = "zone_text", length = 120)
    private String zoneText;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Client(String firstName, String lastName, Long zoneId, String zoneText, String notes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.zoneId = zoneId;
        this.zoneText = zoneText;
        this.notes = notes;
        this.active = true;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void update(String firstName, String lastName, Long zoneId, String zoneText, String notes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.zoneId = zoneId;
        this.zoneText = zoneText;
        this.notes = notes;
    }

    public void deactivate() {
        this.active = false;
    }
}
