package com.groomerapp.api.visits.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "visit_item",
        indexes = {
                @Index(name = "ix_visit_item_visit_id", columnList = "visit_id"),
                @Index(name = "ix_visit_item_category", columnList = "category")
        }
)
public class VisitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private VisitItemCategory category;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public VisitItem(Long visitId, VisitItemCategory category, BigDecimal price) {
        this.visitId = visitId;
        this.category = category;
        this.price = price;
    }

    public void update(VisitItemCategory category, BigDecimal price) {
        this.category = category;
        this.price = price;
    }
}
