package com.groomerapp.api.appointments.web;

import com.groomerapp.api.appointments.domain.Appointment;
import com.groomerapp.api.appointments.web.dto.AppointmentResponse;

public final class AppointmentMapper {

    private AppointmentMapper() {}

    public static AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .petId(a.getPetId())
                .startAt(a.getStartAt())
                .endAt(a.getEndAt())
                .status(a.getStatus())
                .notes(a.getNotes())
                .build();
    }
}
