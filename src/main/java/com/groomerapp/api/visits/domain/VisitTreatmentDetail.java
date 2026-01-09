package com.groomerapp.api.visits.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "visit_treatment_detail",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_visit_treatment_detail_item_id", columnNames = "visit_item_id")
        },
        indexes = {
                @Index(name = "ix_visit_treatment_detail_item_id", columnList = "visit_item_id")
        }
)
public class VisitTreatmentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visit_item_id", nullable = false)
    private Long visitItemId;

    @Column(name = "treatment_type_id")
    private Long treatmentTypeId;

    @Column(name = "treatment_type_text", length = 120)
    private String treatmentTypeText;

    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "medicine_text", length = 120)
    private String medicineText;

    @Column(name = "next_date")
    private LocalDate nextDate;

    public VisitTreatmentDetail(Long visitItemId,
                                Long treatmentTypeId,
                                String treatmentTypeText,
                                Long medicineId,
                                String medicineText,
                                LocalDate nextDate) {
        this.visitItemId = visitItemId;
        this.treatmentTypeId = treatmentTypeId;
        this.treatmentTypeText = treatmentTypeText;
        this.medicineId = medicineId;
        this.medicineText = medicineText;
        this.nextDate = nextDate;
    }

    public void update(Long treatmentTypeId,
                       String treatmentTypeText,
                       Long medicineId,
                       String medicineText,
                       LocalDate nextDate) {
        this.treatmentTypeId = treatmentTypeId;
        this.treatmentTypeText = treatmentTypeText;
        this.medicineId = medicineId;
        this.medicineText = medicineText;
        this.nextDate = nextDate;
    }
}
