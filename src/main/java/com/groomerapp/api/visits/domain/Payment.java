package com.groomerapp.api.visits.domain;

import com.groomerapp.api.shared.domain.PaymentMethod;
import com.groomerapp.api.shared.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_visit_id", columnNames = "visit_id")
        },
        indexes = {
                @Index(name = "ix_payment_status", columnList = "status"),
                @Index(name = "ix_payment_method", columnList = "method")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 20)
    private PaymentMethod method;

    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "balance", precision = 10, scale = 2)
    private BigDecimal balance;

    public Payment(Long visitId, PaymentStatus status, PaymentMethod method, BigDecimal amountPaid, BigDecimal balance) {
        this.visitId = visitId;
        this.status = status;
        this.method = method;
        this.amountPaid = amountPaid;
        this.balance = balance;
    }

    public void update(PaymentStatus status, PaymentMethod method, BigDecimal amountPaid, BigDecimal balance) {
        this.status = status;
        this.method = method;
        this.amountPaid = amountPaid;
        this.balance = balance;
    }
}
