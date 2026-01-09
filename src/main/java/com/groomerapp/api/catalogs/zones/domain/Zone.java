package com.groomerapp.api.catalogs.zones.domain;

import com.groomerapp.api.shared.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "zone",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_zone_normalized_name", columnNames = "normalized_name")
        },
        indexes = {
                @Index(name = "ix_zone_normalized_name", columnList = "normalized_name")
        }
)
public class Zone extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 140)
    private String normalizedName;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Zone(String name, String normalizedName) {
        this.name = name;
        this.normalizedName = normalizedName;
    }

    public void rename(String name, String normalizedName) {
        this.name = name;
        this.normalizedName = normalizedName;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

}
