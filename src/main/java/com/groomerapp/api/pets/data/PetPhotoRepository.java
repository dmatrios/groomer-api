package com.groomerapp.api.pets.data;

import com.groomerapp.api.pets.domain.PetPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetPhotoRepository extends JpaRepository<PetPhoto, Long> {

    List<PetPhoto> findAllByPetIdAndActiveTrueOrderByIdAsc(Long petId);

    Optional<PetPhoto> findFirstByPetIdAndActiveTrueAndPrimaryPhotoTrue(Long petId);

    List<PetPhoto> findAllByPetIdAndActiveTrueAndPrimaryPhotoTrue(Long petId);
}
