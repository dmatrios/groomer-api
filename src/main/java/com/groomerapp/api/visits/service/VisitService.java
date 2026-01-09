package com.groomerapp.api.visits.service;

import com.groomerapp.api.appointments.domain.AppointmentStatus;
import com.groomerapp.api.appointments.service.AppointmentService;
import com.groomerapp.api.pets.service.PetService;
import com.groomerapp.api.shared.domain.PaymentMethod;
import com.groomerapp.api.shared.domain.PaymentStatus;
import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.shared.exceptions.NotFoundException;
import com.groomerapp.api.visits.data.*;
import com.groomerapp.api.visits.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisitService {

    private static final long AUTO_APPOINTMENT_MINUTES = 60;

    private final VisitRepository visitRepository;
    private final VisitItemRepository itemRepository;
    private final VisitTreatmentDetailRepository treatmentDetailRepository;
    private final PaymentRepository paymentRepository;

    private final PetService petService;
    private final AppointmentService appointmentService;

    /* ========= CREATE ========= */

    @Transactional
    public VisitDetail create(CreateVisitCommand cmd) {
        validateCreate(cmd);

        // valida mascota exista
        petService.getById(cmd.petId());

        // ✅ distinguir: appointmentId enviado por request vs auto-creado
        boolean requestHasAppointment = cmd.appointmentId() != null;

        Long finalAppointmentId = cmd.appointmentId();

        // ✅ Walk-in: si no hay appointmentId y autoCreateAppointment=true -> crear cita constancia
        if (!requestHasAppointment && Boolean.TRUE.equals(cmd.autoCreateAppointment())) {

            LocalDateTime start = cmd.visitedAt();
            LocalDateTime end = start.plusMinutes(AUTO_APPOINTMENT_MINUTES);

            var created = appointmentService.create(
                    cmd.petId(),
                    start,
                    end,
                    "Auto-generada desde atención",
                    true // forceOverlap (recomendado para UX)
            );

            appointmentService.markAttended(created.getId());

            finalAppointmentId = created.getId();
        }

        // ✅ Si el request mandó appointmentId, esta visita representa "atender" esa cita existente
        if (requestHasAppointment) {

            var appt = appointmentService.getById(finalAppointmentId);

            // La cita debe pertenecer a la misma mascota
            if (!appt.getPetId().equals(cmd.petId())) {
                throw new BusinessRuleException(
                        ErrorCode.DATA_INTEGRITY_VIOLATION,
                        "La cita no corresponde a esta mascota"
                );
            }

            // No se puede atender una cita cancelada
            if (appt.getStatus() == AppointmentStatus.CANCELED) {
                throw new BusinessRuleException(
                        ErrorCode.DATA_INTEGRITY_VIOLATION,
                        "No se puede atender una cita cancelada"
                );
            }

            // No se puede atender dos veces la misma cita
            if (appt.getStatus() == AppointmentStatus.ATTENDED) {
                throw new BusinessRuleException(
                        ErrorCode.DATA_INTEGRITY_VIOLATION,
                        "La cita ya fue atendida"
                );
            }

            // Marcar como atendida (transaccional)
            appointmentService.markAttended(appt.getId());
        }

        // 1) calcular total
        BigDecimal total = calculateTotal(cmd.items());

        // 2) crear visit (✅ con finalAppointmentId)
        Visit savedVisit = visitRepository.save(new Visit(
                cmd.petId(),
                finalAppointmentId,
                cmd.visitedAt(),
                total,
                normalizeNotes(cmd.notes())
        ));

        // 3) crear items + detalles de tratamiento
        List<VisitItem> savedItems = saveItemsAndTreatmentDetails(savedVisit.getId(), cmd.items());

        // 4) crear/actualizar pago (opcional)
        Payment savedPayment = upsertPayment(savedVisit.getId(), total, cmd.payment());

        return new VisitDetail(savedVisit, savedItems, savedPayment);
    }

    /* ========= GET / LIST ========= */

    @Transactional(readOnly = true)
    public Visit getById(Long id) {
        return visitRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Atención no encontrada"));
    }

    @Transactional(readOnly = true)
    public VisitDetail getDetail(Long id) {
        Visit visit = getById(id);
        List<VisitItem> items = itemRepository.findAllByVisitIdOrderByIdAsc(id);
        Payment payment = paymentRepository.findByVisitId(id).orElse(null);
        return new VisitDetail(visit, items, payment);
    }

    @Transactional(readOnly = true)
    public List<Visit> listByRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "from y to son obligatorios");
        }
        return visitRepository.findAllByActiveTrueAndVisitedAtBetweenOrderByVisitedAtAsc(from, to);
    }

    @Transactional(readOnly = true)
    public List<Visit> listByPet(Long petId) {
        if (petId == null) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "petId es obligatorio");
        }
        return visitRepository.findAllByPetIdAndActiveTrueOrderByVisitedAtDesc(petId);
    }

    /* ========= UPDATE ========= */

    @Transactional
    public VisitDetail update(Long id, UpdateVisitCommand cmd) {
        validateUpdate(cmd);

        Visit visit = getById(id);
        petService.getById(visit.getPetId());

        BigDecimal total = calculateTotal(cmd.items());

        visit.update(cmd.visitedAt(), total, normalizeNotes(cmd.notes()));
        Visit savedVisit = visitRepository.save(visit);

        // estrategia MVP: borrar items + treatmentDetails y recrear
        // primero borramos treatmentDetails de los items existentes
        List<VisitItem> existingItems = itemRepository.findAllByVisitIdOrderByIdAsc(id);
        for (VisitItem it : existingItems) {
            treatmentDetailRepository.deleteByVisitItemId(it.getId());
        }
        itemRepository.deleteAllByVisitId(id);

        List<VisitItem> savedItems = saveItemsAndTreatmentDetails(id, cmd.items());

        Payment savedPayment = upsertPayment(id, total, cmd.payment());

        return new VisitDetail(savedVisit, savedItems, savedPayment);
    }

    @Transactional(readOnly = true)
    public List<VisitDetail> listDetailsByPet(Long petId, VisitItemCategory category) {
        if (petId == null) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "petId es obligatorio");
        }

        // 1) visits base (con o sin filtro)
        List<Visit> visits = (category == null)
                ? visitRepository.findAllByPetIdAndActiveTrueOrderByVisitedAtDesc(petId)
                : visitRepository.findAllByPetIdAndActiveTrueAndCategory(petId, category);

        if (visits.isEmpty()) return List.of();

        List<Long> visitIds = visits.stream().map(Visit::getId).toList();

        // 2) items batch
        List<VisitItem> items = itemRepository.findAllByVisitIdInOrderByVisitIdAscIdAsc(visitIds);

        // 3) payments batch
        List<Payment> payments = paymentRepository.findAllByVisitIdIn(visitIds);

        // 4) indexar para armar details
        Map<Long, List<VisitItem>> itemsByVisit = new java.util.LinkedHashMap<>();
        for (Long id : visitIds) itemsByVisit.put(id, new java.util.ArrayList<>());
        for (VisitItem it : items) itemsByVisit.get(it.getVisitId()).add(it);

        Map<Long, Payment> paymentByVisit = new java.util.HashMap<>();
        for (Payment p : payments) paymentByVisit.put(p.getVisitId(), p);

        // 5) armar details respetando orden
        return visits.stream()
                .map(v -> new VisitDetail(
                        v,
                        itemsByVisit.getOrDefault(v.getId(), List.of()),
                        paymentByVisit.get(v.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitDetail> listDetailsByRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "from y to son obligatorios");
        }

        List<Visit> visits = visitRepository.findAllByActiveTrueAndVisitedAtBetweenOrderByVisitedAtAsc(from, to);
        if (visits.isEmpty()) return List.of();

        List<Long> visitIds = visits.stream().map(Visit::getId).toList();

        List<VisitItem> items = itemRepository.findAllByVisitIdInOrderByVisitIdAscIdAsc(visitIds);
        List<Payment> payments = paymentRepository.findAllByVisitIdIn(visitIds);

        Map<Long, List<VisitItem>> itemsByVisit = new java.util.LinkedHashMap<>();
        for (Long id : visitIds) itemsByVisit.put(id, new java.util.ArrayList<>());
        for (VisitItem it : items) itemsByVisit.get(it.getVisitId()).add(it);

        Map<Long, Payment> paymentByVisit = new java.util.HashMap<>();
        for (Payment p : payments) paymentByVisit.put(p.getVisitId(), p);

        return visits.stream()
                .map(v -> new VisitDetail(
                        v,
                        itemsByVisit.getOrDefault(v.getId(), List.of()),
                        paymentByVisit.get(v.getId())
                ))
                .toList();
    }

    /* ========= INTERNALS ========= */

    private void validateCreate(CreateVisitCommand cmd) {
        if (cmd.petId() == null) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "petId es obligatorio");
        }
        if (cmd.visitedAt() == null) {
            throw new BusinessRuleException(ErrorCode.VISIT_DATE_REQUIRED, "visitedAt es obligatorio");
        }
        if (cmd.items() == null || cmd.items().isEmpty()) {
            throw new BusinessRuleException(ErrorCode.VISIT_ITEMS_REQUIRED, "Debe registrar al menos 1 servicio");
        }
    }

    private void validateUpdate(UpdateVisitCommand cmd) {
        if (cmd.visitedAt() == null) {
            throw new BusinessRuleException(ErrorCode.VISIT_DATE_REQUIRED, "visitedAt es obligatorio");
        }
        if (cmd.items() == null || cmd.items().isEmpty()) {
            throw new BusinessRuleException(ErrorCode.VISIT_ITEMS_REQUIRED, "Debe registrar al menos 1 servicio");
        }
    }

    private BigDecimal calculateTotal(List<ItemCommand> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemCommand it : items) {
            if (it.category() == null) {
                throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "category es obligatorio en cada item");
            }
            if (it.price() == null || it.price().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException(ErrorCode.VISIT_ITEM_PRICE_INVALID, "Cada item debe tener un precio > 0");
            }
            total = total.add(it.price());
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(ErrorCode.VISIT_TOTAL_INVALID, "El total debe ser mayor a 0");
        }

        return total;
    }

    private List<VisitItem> saveItemsAndTreatmentDetails(Long visitId, List<ItemCommand> items) {
        List<VisitItem> savedItems = new ArrayList<>();

        for (ItemCommand it : items) {
            VisitItem saved = itemRepository.save(new VisitItem(visitId, it.category(), it.price()));
            savedItems.add(saved);

            if (it.category() == VisitItemCategory.TREATMENT) {
                TreatmentDetailCommand td = it.treatmentDetail();
                if (td == null) {
                    throw new BusinessRuleException(
                            ErrorCode.VISIT_TREATMENT_DETAIL_REQUIRED,
                            "Item TREATMENT requiere treatmentDetail"
                    );
                }

                validateTreatmentDetail(td);

                treatmentDetailRepository.save(new VisitTreatmentDetail(
                        saved.getId(),
                        td.treatmentTypeId(),
                        normalizeText(td.treatmentTypeText()),
                        td.medicineId(),
                        normalizeText(td.medicineText()),
                        td.nextDate()
                ));
            }
        }

        return savedItems;
    }

    private void validateTreatmentDetail(TreatmentDetailCommand td) {
        boolean hasType = td.treatmentTypeId() != null
                || (td.treatmentTypeText() != null && !td.treatmentTypeText().isBlank());
        boolean hasMed = td.medicineId() != null
                || (td.medicineText() != null && !td.medicineText().isBlank());

        if (!hasType || !hasMed) {
            throw new BusinessRuleException(
                    ErrorCode.VISIT_TREATMENT_DETAIL_REQUIRED,
                    "Tratamiento requiere tipo de tratamiento y medicamento (catálogo o libre)"
            );
        }
    }

    private Payment upsertPayment(Long visitId, BigDecimal total, PaymentCommand paymentCmd) {
        if (paymentCmd == null) {
            return null;
        }

        PaymentStatus status = paymentCmd.status();
        PaymentMethod method = paymentCmd.method();

        if (status == null) {
            throw new BusinessRuleException(ErrorCode.PAYMENT_INVALID, "payment.status es obligatorio");
        }

        BigDecimal amountPaid = paymentCmd.amountPaid();
        BigDecimal balance;

        switch (status) {
            case PENDING -> {
                // MVP: PENDING no exige method, ni amounts
                amountPaid = null;
                balance = total;
                method = null;
            }
            case PAID -> {
                if (method == null) {
                    throw new BusinessRuleException(ErrorCode.PAYMENT_INVALID, "payment.method es obligatorio si está PAGADO");
                }
                amountPaid = total;
                balance = BigDecimal.ZERO;
            }
            case PARTIAL -> {
                if (method == null) {
                    throw new BusinessRuleException(ErrorCode.PAYMENT_INVALID, "payment.method es obligatorio si es PARCIAL");
                }
                if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessRuleException(ErrorCode.PAYMENT_INVALID, "payment.amountPaid debe ser > 0 si es PARCIAL");
                }
                if (amountPaid.compareTo(total) >= 0) {
                    throw new BusinessRuleException(ErrorCode.PAYMENT_INVALID, "Si paga el total, el estado debe ser PAID");
                }
                balance = total.subtract(amountPaid);
            }
            default -> throw new BusinessRuleException(ErrorCode.PAYMENT_INVALID, "Estado de pago inválido");
        }

        Payment payment = paymentRepository.findByVisitId(visitId).orElse(null);

        if (payment == null) {
            payment = new Payment(visitId, status, method, amountPaid, balance);
        } else {
            payment.update(status, method, amountPaid, balance);
        }

        return paymentRepository.save(payment);
    }

    private String normalizeNotes(String notes) {
        if (notes == null) return null;
        String t = notes.trim();
        return t.isBlank() ? null : t;
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isBlank() ? null : t;
    }

    /* ========= COMMANDS (records) ========= */

    public record CreateVisitCommand(
            Long petId,
            Long appointmentId,
            Boolean autoCreateAppointment,
            LocalDateTime visitedAt,
            String notes,
            List<ItemCommand> items,
            PaymentCommand payment
    ) {}

    public record UpdateVisitCommand(
            LocalDateTime visitedAt,
            String notes,
            List<ItemCommand> items,
            PaymentCommand payment
    ) {}

    public record ItemCommand(
            VisitItemCategory category,
            BigDecimal price,
            TreatmentDetailCommand treatmentDetail
    ) {}

    public record TreatmentDetailCommand(
            Long treatmentTypeId,
            String treatmentTypeText,
            Long medicineId,
            String medicineText,
            java.time.LocalDate nextDate
    ) {}

    public record PaymentCommand(
            PaymentStatus status,
            PaymentMethod method,
            BigDecimal amountPaid
    ) {}

    public record VisitDetail(
            Visit visit,
            List<VisitItem> items,
            Payment payment
    ) {}
}
