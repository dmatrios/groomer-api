package com.groomerapp.api.visits.web;

import com.groomerapp.api.visits.data.VisitTreatmentDetailRepository;
import com.groomerapp.api.visits.domain.Payment;
import com.groomerapp.api.visits.domain.Visit;
import com.groomerapp.api.visits.domain.VisitItem;
import com.groomerapp.api.visits.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VisitMapper {

    private final VisitTreatmentDetailRepository treatmentDetailRepository;

    public VisitDetailResponse toDetailResponse(Visit visit, String petName, List<VisitItem> items, Payment payment) {

        List<VisitItemResponse> itemResponses = items.stream().map(it -> {
            TreatmentDetailResponse td = treatmentDetailRepository.findByVisitItemId(it.getId())
                    .map(d -> TreatmentDetailResponse.builder()
                            .treatmentTypeId(d.getTreatmentTypeId())
                            .treatmentTypeText(d.getTreatmentTypeText())
                            .medicineId(d.getMedicineId())
                            .medicineText(d.getMedicineText())
                            .nextDate(d.getNextDate())
                            .build())
                    .orElse(null);

            return VisitItemResponse.builder()
                    .id(it.getId())
                    .category(it.getCategory())
                    .price(it.getPrice())
                    .treatmentDetail(td)
                    .build();
        }).toList();

        PaymentResponse pr = null;
        if (payment != null) {
            pr = PaymentResponse.builder()
                    .status(payment.getStatus())
                    .method(payment.getMethod())
                    .amountPaid(payment.getAmountPaid())
                    .balance(payment.getBalance())
                    .build();
        }

        return VisitDetailResponse.builder()
                .id(visit.getId())
                .petId(visit.getPetId())
                .petName(petName) // ✅ NUEVO
                .appointmentId(visit.getAppointmentId())
                .visitedAt(visit.getVisitedAt())
                .totalAmount(visit.getTotalAmount())
                .notes(visit.getNotes())
                .items(itemResponses)
                .payment(pr)
                .build();
    }
}
