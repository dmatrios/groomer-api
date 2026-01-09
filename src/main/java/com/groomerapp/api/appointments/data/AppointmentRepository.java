package com.groomerapp.api.appointments.data;

import com.groomerapp.api.appointments.domain.Appointment;
import com.groomerapp.api.appointments.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findById(Long id);

    List<Appointment> findAllByStartAtBetweenOrderByStartAtAsc(LocalDateTime from, LocalDateTime to);

    List<Appointment> findAllByStartAtBetweenAndStatusOrderByStartAtAsc(LocalDateTime from, LocalDateTime to, AppointmentStatus status);

    // Overlap: start < newEnd AND end > newStart
    boolean existsByStartAtLessThanAndEndAtGreaterThan(LocalDateTime newEnd, LocalDateTime newStart);

    boolean existsByStartAtLessThanAndEndAtGreaterThanAndIdNot(LocalDateTime newEnd, LocalDateTime newStart, Long id);


    @Query("""
   select a from Appointment a
   where a.petId in :petIds
   order by a.startAt desc
""")
    List<Appointment> findAllByPetIdIn(@Param("petIds") List<Long> petIds);

    @Query("""
   select a from Appointment a
   where lower(coalesce(a.notes, '')) like lower(concat('%', :q, '%'))
   order by a.startAt desc
""")
    List<Appointment> searchByNotes(@Param("q") String q);
}
