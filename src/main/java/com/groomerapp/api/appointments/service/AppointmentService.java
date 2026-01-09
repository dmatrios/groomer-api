package com.groomerapp.api.appointments.service;

import com.groomerapp.api.appointments.data.AppointmentChargeRepository;
import com.groomerapp.api.appointments.data.AppointmentEventRepository;
import com.groomerapp.api.appointments.data.AppointmentRepository;
import com.groomerapp.api.appointments.domain.*;
import com.groomerapp.api.pets.service.PetService;
import com.groomerapp.api.shared.domain.PaymentMethod;
import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentEventRepository eventRepository;
    private final AppointmentChargeRepository chargeRepository;

    private final PetService petService; // valida que la mascota exista

    @Transactional
    public Appointment create(Long petId, LocalDateTime startAt, LocalDateTime endAt, String notes, boolean forceOverlap) {
        validateTime(startAt, endAt);

        if (petId == null) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "petId es obligatorio");
        }
        petService.getById(petId);

        boolean overlap = appointmentRepository.existsByStartAtLessThanAndEndAtGreaterThan(endAt, startAt);
        if (overlap && !forceOverlap) {
            throw new BusinessRuleException(
                    ErrorCode.OVERLAP_CONFIRMATION_REQUIRED,
                    "Ya hay una cita/atención programada en ese horario. Confirma para proceder."
            );
        }

        Appointment created = new Appointment(petId, startAt, endAt, normalizeNotes(notes));
        return appointmentRepository.save(created);
    }

    @Transactional(readOnly = true)
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cita no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Appointment> list(LocalDateTime from, LocalDateTime to, AppointmentStatus status) {
        if (from == null || to == null) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "from y to son obligatorios");
        }
        if (status == null) {
            return appointmentRepository.findAllByStartAtBetweenOrderByStartAtAsc(from, to);
        }
        return appointmentRepository.findAllByStartAtBetweenAndStatusOrderByStartAtAsc(from, to, status);
    }

    @Transactional
    public Appointment update(Long id, LocalDateTime startAt, LocalDateTime endAt, String notes, boolean forceOverlap) {
        validateTime(startAt, endAt);

        Appointment appt = getById(id);

        // Solo se puede modificar si está PENDING
        if (appt.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessRuleException(
                    ErrorCode.DATA_INTEGRITY_VIOLATION,
                    "Solo se puede modificar una cita en estado PENDING"
            );
        }

        boolean overlap = appointmentRepository.existsByStartAtLessThanAndEndAtGreaterThanAndIdNot(endAt, startAt, id);
        if (overlap && !forceOverlap) {
            throw new BusinessRuleException(
                    ErrorCode.OVERLAP_CONFIRMATION_REQUIRED,
                    "Ya hay una cita/atención programada en ese horario. Confirma para proceder."
            );
        }

        appt.reschedule(startAt, endAt);
        appt.updateNotes(normalizeNotes(notes));

        return appointmentRepository.save(appt);
    }

    @Transactional
    public Appointment reschedule(Long id, LocalDateTime newStart, LocalDateTime newEnd, String reason, boolean forceOverlap) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException(ErrorCode.APPOINTMENT_REASON_REQUIRED, "El motivo es obligatorio");
        }
        validateTime(newStart, newEnd);

        Appointment appt = getById(id);

        // Solo se puede reprogramar si está PENDING
        if (appt.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessRuleException(
                    ErrorCode.DATA_INTEGRITY_VIOLATION,
                    "Solo se puede reprogramar una cita en estado PENDING"
            );
        }

        boolean overlap = appointmentRepository.existsByStartAtLessThanAndEndAtGreaterThanAndIdNot(newEnd, newStart, id);
        if (overlap && !forceOverlap) {
            throw new BusinessRuleException(
                    ErrorCode.OVERLAP_CONFIRMATION_REQUIRED,
                    "Ya hay una cita/atención programada en ese horario. Confirma para proceder."
            );
        }

        LocalDateTime oldStart = appt.getStartAt();
        LocalDateTime oldEnd = appt.getEndAt();

        appt.reschedule(newStart, newEnd);
        Appointment saved = appointmentRepository.save(appt);

        eventRepository.save(new AppointmentEvent(
                saved.getId(),
                AppointmentEventType.RESCHEDULED,
                reason.trim(),
                oldStart, oldEnd,
                newStart, newEnd
        ));

        return saved;
    }

    @Transactional
    public Appointment cancel(Long id, String reason, PaymentMethod chargeMethod, BigDecimal chargeAmount) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException(ErrorCode.APPOINTMENT_REASON_REQUIRED, "El motivo es obligatorio");
        }

        Appointment appt = getById(id);

        // Solo se puede cancelar si está PENDING
        if (appt.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessRuleException(
                    ErrorCode.DATA_INTEGRITY_VIOLATION,
                    "Solo se puede cancelar una cita en estado PENDING"
            );
        }

        appt.cancel();
        Appointment saved = appointmentRepository.save(appt);

        // evento
        eventRepository.save(new AppointmentEvent(
                saved.getId(),
                AppointmentEventType.CANCELED,
                reason.trim(),
                saved.getStartAt(), saved.getEndAt(),
                null, null
        ));

        // charge opcional (si hubo cobro)
        if (chargeMethod != null || chargeAmount != null) {
            if (chargeMethod == null || chargeAmount == null || chargeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException(
                        ErrorCode.APPOINTMENT_CANCEL_CHARGE_INVALID,
                        "Si hay cobro, el método y monto deben ser válidos"
                );
            }

            // 0..1 por cita
            chargeRepository.findByAppointmentId(saved.getId()).ifPresent(existing -> {
                // MVP: si ya existe, lo consideramos error (para no duplicar)
                throw new BusinessRuleException(
                        ErrorCode.DATA_INTEGRITY_VIOLATION,
                        "Ya existe un cobro registrado para esta cita"
                );
            });

            chargeRepository.save(new AppointmentCharge(saved.getId(), chargeMethod, chargeAmount));
        }

        return saved;
    }

    @Transactional
    public Appointment markAttended(Long id) {
        if (id == null) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "id es obligatorio");
        }

        Appointment appt = getById(id);

        if (appt.getStatus() == AppointmentStatus.CANCELED) {
            throw new BusinessRuleException(
                    ErrorCode.DATA_INTEGRITY_VIOLATION,
                    "No se puede atender una cita cancelada"
            );
        }

        if (appt.getStatus() == AppointmentStatus.ATTENDED) {
            throw new BusinessRuleException(
                    ErrorCode.DATA_INTEGRITY_VIOLATION,
                    "La cita ya fue atendida"
            );
        }

        appt.markAttended();
        return appointmentRepository.save(appt);
    }

    /* ========= helpers ========= */

    private void validateTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BusinessRuleException(ErrorCode.APPOINTMENT_TIME_INVALID, "startAt y endAt son obligatorios");
        }
        if (!endAt.isAfter(startAt)) {
            throw new BusinessRuleException(ErrorCode.APPOINTMENT_TIME_INVALID, "La hora fin debe ser mayor que la hora inicio");
        }
    }

    private String normalizeNotes(String notes) {
        if (notes == null) return null;
        String t = notes.trim();
        return t.isBlank() ? null : t;
    }
}
