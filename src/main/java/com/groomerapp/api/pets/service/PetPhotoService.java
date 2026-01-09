package com.groomerapp.api.pets.service;

import com.groomerapp.api.pets.data.PetPhotoRepository;
import com.groomerapp.api.pets.domain.PetPhoto;
import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetPhotoService {

    private final PetService petService;
    private final PetPhotoRepository photoRepository;

    @Transactional
    public PetPhoto addPhoto(Long petId, String url) {
        petService.getById(petId);

        if (url == null || url.isBlank()) {
            throw new BusinessRuleException(ErrorCode.PET_PHOTO_URL_REQUIRED, "La URL de la foto es obligatoria");
        }

        // Apagar cualquier primary actual (por seguridad)
        List<PetPhoto> primaries = photoRepository.findAllByPetIdAndActiveTrueAndPrimaryPhotoTrue(petId);
        for (PetPhoto p : primaries) {
            p.removePrimary();
            photoRepository.save(p);
        }

        // Nueva foto queda como principal
        PetPhoto created = new PetPhoto(petId, url.trim(), true);
        return photoRepository.save(created);
    }

    @Transactional(readOnly = true)
    public List<PetPhoto> listPhotos(Long petId) {
        petService.getById(petId);
        return photoRepository.findAllByPetIdAndActiveTrueOrderByIdAsc(petId);
    }

    @Transactional
    public void makePrimary(Long petId, Long photoId) {
        petService.getById(petId);

        PetPhoto target = photoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Foto no encontrada"));

        if (!target.getPetId().equals(petId)) {
            throw new NotFoundException("Foto no encontrada");
        }

        List<PetPhoto> primaries = photoRepository.findAllByPetIdAndActiveTrueAndPrimaryPhotoTrue(petId);
        for (PetPhoto p : primaries) {
            p.removePrimary();
            photoRepository.save(p);
        }

        target.makePrimary();
        photoRepository.save(target);
    }
}
