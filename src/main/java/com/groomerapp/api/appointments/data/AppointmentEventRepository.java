package com.groomerapp.api.appointments.data;

import com.groomerapp.api.appointments.domain.AppointmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentEventRepository extends JpaRepository<AppointmentEvent, Long> {

    List<AppointmentEvent> findAllByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
}
