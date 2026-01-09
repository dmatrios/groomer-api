package com.groomerapp.api.visits.domain;

import com.groomerapp.api.shared.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "visit",
        indexes = {
                @Index(name = "ix_visit_pet_id", columnList = "pet_id"),
                @Index(name = "ix_visit_appointment_id", columnList = "appointment_id"),
                @Index(name = "ix_visit_visited_at", columnList = "visited_at")
        }
)
public class Visit extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "appointment_id")
    private Long appointmentId; // null si es walk-in

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Visit(Long petId, Long appointmentId, LocalDateTime visitedAt, BigDecimal totalAmount, String notes) {
        this.petId = petId;
        this.appointmentId = appointmentId;
        this.visitedAt = visitedAt;
        this.totalAmount = totalAmount;
        this.notes = notes;
        this.active = true;
    }

    public void update(LocalDateTime visitedAt, BigDecimal totalAmount, String notes) {
        this.visitedAt = visitedAt;
        this.totalAmount = totalAmount;
        this.notes = notes;
    }

    public void deactivate() {
        this.active = false;
    }
}
