package com.groomerapp.api.catalogs.medicines.web;

import com.groomerapp.api.catalogs.medicines.domain.Medicine;
import com.groomerapp.api.catalogs.medicines.service.MedicineService;
import com.groomerapp.api.catalogs.medicines.web.dto.MedicineCreateRequest;
import com.groomerapp.api.catalogs.medicines.web.dto.MedicineResponse;
import com.groomerapp.api.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalogs/medicines")
public class MedicineController {

    private final MedicineService service;

    @GetMapping
    public ApiResponse<List<MedicineResponse>> list() {
        List<MedicineResponse> data = service.listAll()
                .stream()
                .map(MedicineMapper::toResponse)
                .toList();

        return ApiResponse.ok(data);
    }

    @PostMapping
    public ApiResponse<MedicineResponse> create(@Valid @RequestBody MedicineCreateRequest request) {
        Medicine created = service.create(request.getName());
        return ApiResponse.ok(MedicineMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ApiResponse<MedicineResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicineCreateRequest request
    ) {
        Medicine updated = service.update(id, request.getName());
        return ApiResponse.ok(MedicineMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deactivate(id);
        return ApiResponse.ok(null);
    }
}
