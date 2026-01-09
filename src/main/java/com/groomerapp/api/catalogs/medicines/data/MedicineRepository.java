package com.groomerapp.api.catalogs.medicines.data;

import com.groomerapp.api.catalogs.medicines.domain.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findAllByActiveTrueOrderByNameAsc();

    Optional<Medicine> findByIdAndActiveTrue(Long id);

    boolean existsByNormalizedNameAndActiveTrue(String normalizedName);

    boolean existsByNormalizedNameAndIdNotAndActiveTrue(String normalizedName, Long id);
}
