package com.groomerapp.api.catalogs.zones.data;

import com.groomerapp.api.catalogs.zones.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    boolean existsByNormalizedName(String normalizedName);

    List<Zone> findAllByOrderByNameAsc();

    boolean existsByNormalizedNameAndIdNot(String normalizedName, Long id);

    List<Zone> findAllByActiveTrueOrderByNameAsc();

    Optional<Zone> findByIdAndActiveTrue(Long id);

    boolean existsByNormalizedNameAndActiveTrue(String normalizedName);

    boolean existsByNormalizedNameAndIdNotAndActiveTrue(String normalizedName, Long id);
}
