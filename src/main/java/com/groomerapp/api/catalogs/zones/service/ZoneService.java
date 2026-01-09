package com.groomerapp.api.catalogs.zones.service;

import com.groomerapp.api.catalogs.zones.data.ZoneRepository;
import com.groomerapp.api.catalogs.zones.domain.Zone;
import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.shared.exceptions.NotFoundException;
import com.groomerapp.api.shared.utils.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    @Transactional(readOnly = true)
    public List<Zone> listAll() {
        return zoneRepository.findAllByActiveTrueOrderByNameAsc();
    }


    @Transactional
    public Zone create(String name) {
        String normalized = TextNormalizer.normalize(name);

        if (normalized == null || normalized.isBlank()) {
            throw new BusinessRuleException(ErrorCode.ZONE_NAME_REQUIRED, "El nombre de la zona es obligatorio");
        }

        if (zoneRepository.existsByNormalizedNameAndActiveTrue(normalized)) {
            throw new BusinessRuleException(ErrorCode.ZONE_ALREADY_EXISTS, "La zona ya existe");
        }


        Zone zone = new Zone(name.trim(), normalized);
        return zoneRepository.save(zone);
    }

    @Transactional
    public Zone update(Long id, String name) {
        Zone zone = zoneRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Zona no encontrada"));

        String normalized = TextNormalizer.normalize(name);
        if (normalized == null || normalized.isBlank()) {
            throw new BusinessRuleException(ErrorCode.ZONE_NAME_REQUIRED, "El nombre de la zona es obligatorio");
        }

        if (zoneRepository.existsByNormalizedNameAndIdNotAndActiveTrue(normalized, id)) {
            throw new BusinessRuleException(ErrorCode.ZONE_ALREADY_EXISTS, "La zona ya existe");
        }


        zone.rename(name.trim(), normalized);
        return zoneRepository.save(zone);
    }

    @Transactional
    public void deactivate(Long id) {
        Zone zone = zoneRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Zona no encontrada"));

        zone.deactivate();
        zoneRepository.save(zone);
    }

}
