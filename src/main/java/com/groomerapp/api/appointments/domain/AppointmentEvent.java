package com.groomerapp.api.appointments.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "appointment_event",
        indexes = {
                @Index(name = "ix_appointment_event_appointment_id", columnList = "appointment_id"),
                @Index(name = "ix_appointment_event_created_at", columnList = "created_at")
        }
)
public class AppointmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 12)
    private AppointmentEventType type;

    @Column(name = "reason", nullable = false, length = 250)
    private String reason;

    @Column(name = "old_start_at")
    private LocalDateTime oldStartAt;

    @Column(name = "old_end_at")
    private LocalDateTime oldEndAt;

    @Column(name = "new_start_at")
    private LocalDateTime newStartAt;

    @Column(name = "new_end_at")
    private LocalDateTime newEndAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AppointmentEvent(Long appointmentId,
                            AppointmentEventType type,
                            String reason,
                            LocalDateTime oldStartAt,
                            LocalDateTime oldEndAt,
                            LocalDateTime newStartAt,
                            LocalDateTime newEndAt) {
        this.appointmentId = appointmentId;
        this.type = type;
        this.reason = reason;
        this.oldStartAt = oldStartAt;
        this.oldEndAt = oldEndAt;
        this.newStartAt = newStartAt;
        this.newEndAt = newEndAt;
        this.createdAt = LocalDateTime.now();
    }
}
