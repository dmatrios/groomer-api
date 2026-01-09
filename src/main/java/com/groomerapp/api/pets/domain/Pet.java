package com.groomerapp.api.pets.domain;

import com.groomerapp.api.shared.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "pet",
        indexes = {
                @Index(name = "ix_pet_code", columnList = "code"),
                @Index(name = "ix_pet_client_id", columnList = "client_id"),
                @Index(name = "ix_pet_name", columnList = "name"),
                @Index(name = "ix_pet_species", columnList = "species")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pet_code", columnNames = "code")
        }
)
public class Pet extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se setea en service (PT-000001)
    @Column(name = "code", length = 20, unique = true)
    private String code;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "species", nullable = false, length = 10)
    private PetSpecies species;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false, length = 10)
    private PetSize size;

    @Enumerated(EnumType.STRING)
    @Column(name = "temperament", nullable = false, length = 12)
    private PetTemperament temperament;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Pet(Long clientId, String name, PetSpecies species, PetSize size, PetTemperament temperament, Double weight, String notes) {
        this.clientId = clientId;
        this.name = name;
        this.species = (species == null ? PetSpecies.DOG : species);
        this.size = size;
        this.temperament = temperament;
        this.weight = weight;
        this.notes = notes;
        this.active = true;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void update(String name, PetSpecies species, PetSize size, PetTemperament temperament, Double weight, String notes) {
        this.name = name;
        this.species = (species == null ? this.species : species);
        this.size = size;
        this.temperament = temperament;
        this.weight = weight;
        this.notes = notes;
    }

    public void deactivate() {
        this.active = false;
    }
}
