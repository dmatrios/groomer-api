package com.groomerapp.api.catalogs.medicines.service;

import com.groomerapp.api.catalogs.medicines.data.MedicineRepository;
import com.groomerapp.api.catalogs.medicines.domain.Medicine;
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
public class MedicineService {

    private final MedicineRepository repository;

    @Transactional(readOnly = true)
    public List<Medicine> listAll() {
        return repository.findAllByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public Medicine create(String name) {
        String normalized = TextNormalizer.normalize(name);

        if (normalized == null || normalized.isBlank()) {
            throw new BusinessRuleException(
                    ErrorCode.MEDICINE_NAME_REQUIRED,
                    "El nombre del medicamento es obligatorio"
            );
        }

        if (repository.existsByNormalizedNameAndActiveTrue(normalized)) {
            throw new BusinessRuleException(
                    ErrorCode.MEDICINE_ALREADY_EXISTS,
                    "El medicamento ya existe"
            );
        }

        Medicine entity = new Medicine(name.trim(), normalized);
        return repository.save(entity);
    }

    @Transactional
    public Medicine update(Long id, String name) {
        Medicine entity = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Medicamento no encontrado"));

        String normalized = TextNormalizer.normalize(name);
        if (normalized == null || normalized.isBlank()) {
            throw new BusinessRuleException(
                    ErrorCode.MEDICINE_NAME_REQUIRED,
                    "El nombre del medicamento es obligatorio"
            );
        }

        if (repository.existsByNormalizedNameAndIdNotAndActiveTrue(normalized, id)) {
            throw new BusinessRuleException(
                    ErrorCode.MEDICINE_ALREADY_EXISTS,
                    "El medicamento ya existe"
            );
        }

        entity.rename(name.trim(), normalized);
        return repository.save(entity);
    }

    @Transactional
    public void deactivate(Long id) {
        Medicine entity = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Medicamento no encontrado"));

        entity.deactivate();
        repository.save(entity);
    }
}
