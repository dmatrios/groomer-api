package com.groomerapp.api.catalogs.treatmenttypes.data;

import com.groomerapp.api.catalogs.treatmenttypes.domain.TreatmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreatmentTypeRepository extends JpaRepository<TreatmentType, Long> {

    List<TreatmentType> findAllByActiveTrueOrderByNameAsc();

    Optional<TreatmentType> findByIdAndActiveTrue(Long id);

    boolean existsByNormalizedNameAndActiveTrue(String normalizedName);

    boolean existsByNormalizedNameAndIdNotAndActiveTrue(String normalizedName, Long id);
}
