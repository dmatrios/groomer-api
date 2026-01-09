package com.groomerapp.api.visits.data;

import com.groomerapp.api.visits.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByVisitIdIn(List<Long> visitIds);

    Optional<Payment> findByVisitId(Long visitId);
}
