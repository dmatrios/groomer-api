package com.groomerapp.api.pets.service;

import com.groomerapp.api.clients.service.ClientService;
import com.groomerapp.api.pets.data.PetRepository;
import com.groomerapp.api.pets.domain.Pet;
import com.groomerapp.api.pets.domain.PetSize;
import com.groomerapp.api.pets.domain.PetSpecies;
import com.groomerapp.api.pets.domain.PetTemperament;
import com.groomerapp.api.pets.web.PetMapper;
import com.groomerapp.api.pets.web.dto.PetResponse;
import com.groomerapp.api.pets.web.dto.PetStatsResponse;
import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.shared.exceptions.NotFoundException;
import com.groomerapp.api.visits.data.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final ClientService clientService; // valida que el cliente exista y esté activo
    private final VisitRepository visitRepository;

    /* ========= CREATE ========= */

    @Transactional
    public CreatedPet create(
            Long clientId,
            String name,
            PetSpecies species,
            PetSize size,
            PetTemperament temperament,
            Double weight,
            String notes
    ) {
        if (clientId == null) {
            throw new BusinessRuleException(ErrorCode.PET_CLIENT_REQUIRED, "El cliente es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException(ErrorCode.PET_NAME_REQUIRED, "El nombre de la mascota es obligatorio");
        }

        // valida cliente exista (y activo)
        clientService.getById(clientId);

        boolean duplicateNameWarning =
                petRepository.existsByClientIdAndNameIgnoreCaseAndActiveTrue(clientId, name.trim());

        Pet saved = petRepository.save(new Pet(
                clientId,
                name.trim(),
                (species == null ? PetSpecies.DOG : species),
                size,
                temperament,
                weight,
                normalizeNotes(notes)
        ));

        // code PT-000001
        saved.setCode(generatePetCode(saved.getId()));
        Pet finalSaved = petRepository.save(saved);

        return new CreatedPet(finalSaved, duplicateNameWarning);
    }

    /* ========= GET / LIST ========= */

    @Transactional(readOnly = true)
    public Pet getById(Long id) {
        return petRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Mascota no encontrada"));
    }

    /**
     * MVP: si clientId es null, lista todas las mascotas activas (para combos / búsqueda)
     */
    @Transactional(readOnly = true)
    public List<Pet> listByClient(Long clientId) {
        if (clientId == null) {
            return petRepository.findAllByActiveTrueOrderByNameAsc();
        }
        return petRepository.findAllByClientIdAndActiveTrueOrderByNameAsc(clientId);
    }

    /**
     * OJO: este método usa findByClientId() (no filtra active).
     * Si lo mantienes por compatibilidad, úsalo solo si te conviene traer también inactivas.
     * (Ideal: migrar a findAllByClientIdAndActiveTrueOrderByNameAsc)
     */
    @Transactional(readOnly = true)
    public List<PetResponse> getByClient(Long clientId) {
        return petRepository.findByClientId(clientId)
                .stream()
                .map(pet -> PetMapper.toResponse(pet, null))
                .toList();
    }

    /* ========= UPDATE ========= */

    @Transactional
    public Pet update(
            Long id,
            String name,
            PetSpecies species,
            PetSize size,
            PetTemperament temperament,
            Double weight,
            String notes
    ) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException(ErrorCode.PET_NAME_REQUIRED, "El nombre de la mascota es obligatorio");
        }

        Pet pet = getById(id);
        pet.update(
                name.trim(),
                species,
                size,
                temperament,
                weight,
                normalizeNotes(notes)
        );

        return petRepository.save(pet);
    }

    /* ========= STATS ========= */

    @Transactional(readOnly = true)
    public PetStatsResponse getStats(Long petId) {
        // valida exista (si no, 404)
        getById(petId);

        long count = visitRepository.countByPetId(petId);

        var last = visitRepository.findTopByPetIdOrderByVisitedAtDesc(petId)
                .map(v -> new PetStatsResponse.LastVisit(v.getId(), v.getVisitedAt().toString()))
                .orElse(null);

        return new PetStatsResponse(count, last);
    }

    /* ========= HELPERS PARA FRONT (batch) ========= */

    /**
     * Devuelve un mapa petId -> petName (solo activas).
     * Útil para enriquecer listados (ej: VisitsPage) sin hacer N+1 llamadas.
     */
    @Transactional(readOnly = true)
    public Map<Long, String> mapNamesByIds(Collection<Long> petIds) {
        if (petIds == null || petIds.isEmpty()) return Map.of();

        // Ideal: usar un método del repo que ya filtre active true:
        // petRepository.findAllByIdInAndActiveTrue(petIds)
        // Si aún no lo agregas, esto funciona igual y filtramos en memoria.
        return petRepository.findAllById(petIds).stream()
                .filter(Pet::isActive)
                .collect(Collectors.toMap(
                        Pet::getId,
                        Pet::getName,
                        (a, b) -> a // por si se repite (no debería)
                ));
    }

    /* ========= INTERNALS ========= */

    private String normalizeNotes(String notes) {
        if (notes == null) return null;
        String t = notes.trim();
        return t.isBlank() ? null : t;
    }

    private String generatePetCode(Long id) {
        return "PT-" + String.format("%06d", id);
    }

    public record CreatedPet(Pet pet, boolean duplicateNameWarning) {}
}
