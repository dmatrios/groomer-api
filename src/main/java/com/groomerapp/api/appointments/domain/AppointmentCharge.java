package com.groomerapp.api.appointments.domain;

import com.groomerapp.api.shared.domain.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "appointment_charge",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_appointment_charge_appointment_id", columnNames = "appointment_id")
        },
        indexes = {
                @Index(name = "ix_appointment_charge_created_at", columnList = "created_at")
        }
)
public class AppointmentCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AppointmentCharge(Long appointmentId, PaymentMethod method, BigDecimal amount) {
        this.appointmentId = appointmentId;
        this.method = method;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
}
