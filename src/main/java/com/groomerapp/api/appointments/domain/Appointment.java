package com.groomerapp.api.appointments.domain;

import com.groomerapp.api.shared.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "appointment",
        indexes = {
                @Index(name = "ix_appointment_pet_id", columnList = "pet_id"),
                @Index(name = "ix_appointment_start_at", columnList = "start_at"),
                @Index(name = "ix_appointment_end_at", columnList = "end_at"),
                @Index(name = "ix_appointment_status", columnList = "status")
        }
)
public class Appointment extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación lógica (MVP simple)
    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private AppointmentStatus status;

    @Column(name = "notes", length = 500)
    private String notes;

    public Appointment(Long petId,
                       LocalDateTime startAt,
                       LocalDateTime endAt,
                       String notes) {
        this.petId = petId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.notes = notes;
        this.status = AppointmentStatus.PENDING;
    }

    /* ===== Reglas de dominio ===== */

    public void reschedule(LocalDateTime newStart, LocalDateTime newEnd) {
        this.startAt = newStart;
        this.endAt = newEnd;
    }

    public void markAttended() {
        this.status = AppointmentStatus.ATTENDED;
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELED;
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }
}
