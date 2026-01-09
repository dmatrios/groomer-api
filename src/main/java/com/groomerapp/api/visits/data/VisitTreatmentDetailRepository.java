package com.groomerapp.api.visits.data;

import com.groomerapp.api.visits.domain.VisitTreatmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitTreatmentDetailRepository extends JpaRepository<VisitTreatmentDetail, Long> {

    Optional<VisitTreatmentDetail> findByVisitItemId(Long visitItemId);

    void deleteByVisitItemId(Long visitItemId);
}
