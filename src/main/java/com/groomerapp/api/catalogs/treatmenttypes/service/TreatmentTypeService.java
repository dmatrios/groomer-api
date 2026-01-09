package com.groomerapp.api.catalogs.treatmenttypes.service;

import com.groomerapp.api.catalogs.treatmenttypes.data.TreatmentTypeRepository;
import com.groomerapp.api.catalogs.treatmenttypes.domain.TreatmentType;
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
public class TreatmentTypeService {

    private final TreatmentTypeRepository repository;

    @Transactional(readOnly = true)
    public List<TreatmentType> listAll() {
        return repository.findAllByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public TreatmentType create(String name) {
        String normalized = TextNormalizer.normalize(name);

        if (normalized == null || normalized.isBlank()) {
            throw new BusinessRuleException(
                    ErrorCode.TREATMENT_TYPE_NAME_REQUIRED,
                    "El nombre del tipo de tratamiento es obligatorio"
            );
        }

        if (repository.existsByNormalizedNameAndActiveTrue(normalized)) {
            throw new BusinessRuleException(
                    ErrorCode.TREATMENT_TYPE_ALREADY_EXISTS,
                    "El tipo de tratamiento ya existe"
            );
        }

        TreatmentType entity = new TreatmentType(name.trim(), normalized);
        return repository.save(entity);
    }

    @Transactional
    public TreatmentType update(Long id, String name) {
        TreatmentType entity = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Tipo de tratamiento no encontrado"));

        String normalized = TextNormalizer.normalize(name);
        if (normalized == null || normalized.isBlank()) {
            throw new BusinessRuleException(
                    ErrorCode.TREATMENT_TYPE_NAME_REQUIRED,
                    "El nombre del tipo de tratamiento es obligatorio"
            );
        }

        if (repository.existsByNormalizedNameAndIdNotAndActiveTrue(normalized, id)) {
            throw new BusinessRuleException(
                    ErrorCode.TREATMENT_TYPE_ALREADY_EXISTS,
                    "El tipo de tratamiento ya existe"
            );
        }

        entity.rename(name.trim(), normalized);
        return repository.save(entity);
    }

    @Transactional
    public void deactivate(Long id) {
        TreatmentType entity = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Tipo de tratamiento no encontrado"));

        entity.deactivate();
        repository.save(entity);
    }
}
