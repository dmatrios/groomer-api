package com.groomerapp.api.pets.web;

import com.groomerapp.api.pets.domain.Pet;
import com.groomerapp.api.pets.domain.PetPhoto;
import com.groomerapp.api.pets.service.PetPhotoService;
import com.groomerapp.api.pets.service.PetService;
import com.groomerapp.api.pets.web.dto.*;
import com.groomerapp.api.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pets")
public class PetController {

    private final PetService petService;
    private final PetPhotoService petPhotoService;

    @PostMapping
    public ApiResponse<PetResponse> create(@Valid @RequestBody PetCreateRequest request) {
        var created = petService.create(
                request.getClientId(),
                request.getName(),
                request.getSpecies(),
                request.getSize(),
                request.getTemperament(),
                request.getWeight(),
                request.getNotes()
        );

        PetResponse data = PetMapper.toResponse(created.pet(), null);

        if (created.duplicateNameWarning()) {
            return ApiResponse.<PetResponse>builder()
                    .data(data)
                    .warnings(List.of("Ya existe una mascota con el mismo nombre para este cliente"))
                    .build();
        }

        return ApiResponse.ok(data);
    }


    @GetMapping("/{id}")
    public ApiResponse<PetResponse> getById(@PathVariable Long id) {
        Pet pet = petService.getById(id);

        String mainPhotoUrl = petPhotoService.listPhotos(id).stream()
                .filter(p -> p.isPrimaryPhoto())
                .findFirst()
                .map(PetPhoto::getUrl)
                .orElse(null);

        return ApiResponse.ok(PetMapper.toResponse(pet, mainPhotoUrl));
    }

    @GetMapping
    public ApiResponse<List<PetResponse>> list(@RequestParam(required = false) Long clientId) {
        List<PetResponse> data = petService.listByClient(clientId).stream()
                .map(p -> PetMapper.toResponse(p, null))
                .toList();

        return ApiResponse.ok(data);
    }

    @PutMapping("/{id}")
    public ApiResponse<PetResponse> update(@PathVariable Long id, @Valid @RequestBody PetUpdateRequest request) {
        Pet updated = petService.update(
                id,
                request.getName(),
                request.getSpecies(),
                request.getSize(),
                request.getTemperament(),
                request.getWeight(),
                request.getNotes()
        );
        return ApiResponse.ok(PetMapper.toResponse(updated, null));
    }

    // Fotos

    @PostMapping("/{id}/photos")
    public ApiResponse<PetPhotoResponse> addPhoto(@PathVariable Long id, @Valid @RequestBody PetPhotoCreateRequest request) {
        return ApiResponse.ok(PetPhotoMapper.toResponse(
                petPhotoService.addPhoto(id, request.getUrl())
        ));
    }

    @GetMapping("/{id}/photos")
    public ApiResponse<List<PetPhotoResponse>> listPhotos(@PathVariable Long id) {
        List<PetPhotoResponse> data = petPhotoService.listPhotos(id).stream()
                .map(PetPhotoMapper::toResponse)
                .toList();

        return ApiResponse.ok(data);
    }

    @PostMapping("/{id}/photos/{photoId}/make-primary")
    public ApiResponse<Void> makePrimary(@PathVariable Long id, @PathVariable Long photoId) {
        petPhotoService.makePrimary(id, photoId);
        return ApiResponse.ok(null);
    }
    @GetMapping("/{id}/stats")
    public ApiResponse<PetStatsResponse> stats(@PathVariable Long id) {
        return ApiResponse.ok(petService.getStats(id));
    }

}
