package com.groomerapp.api.pets.domain;

import com.groomerapp.api.shared.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "pet_photo",
        indexes = {
                @Index(name = "ix_pet_photo_pet_id", columnList = "pet_id"),
                @Index(name = "ix_pet_photo_primary", columnList = "is_primary")
        }
)
public class PetPhoto extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "url", nullable = false, length = 300)
    private String url;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryPhoto;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public PetPhoto(Long petId, String url, boolean primaryPhoto) {
        this.petId = petId;
        this.url = url;
        this.primaryPhoto = primaryPhoto;
        this.active = true;
    }

    public void makePrimary() {
        this.primaryPhoto = true;
    }

    public void removePrimary() {
        this.primaryPhoto = false;
    }

    public void deactivate() {
        this.active = false;
    }
}
