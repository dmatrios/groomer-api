package com.groomerapp.api.catalogs.treatmenttypes.web;

import com.groomerapp.api.catalogs.treatmenttypes.domain.TreatmentType;
import com.groomerapp.api.catalogs.treatmenttypes.service.TreatmentTypeService;
import com.groomerapp.api.catalogs.treatmenttypes.web.dto.TreatmentTypeCreateRequest;
import com.groomerapp.api.catalogs.treatmenttypes.web.dto.TreatmentTypeResponse;
import com.groomerapp.api.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalogs/treatment-types")
public class TreatmentTypeController {

    private final TreatmentTypeService service;

    @GetMapping
    public ApiResponse<List<TreatmentTypeResponse>> list() {
        List<TreatmentTypeResponse> data = service.listAll()
                .stream()
                .map(TreatmentTypeMapper::toResponse)
                .toList();

        return ApiResponse.ok(data);
    }

    @PostMapping
    public ApiResponse<TreatmentTypeResponse> create(@Valid @RequestBody TreatmentTypeCreateRequest request) {
        TreatmentType created = service.create(request.getName());
        return ApiResponse.ok(TreatmentTypeMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ApiResponse<TreatmentTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TreatmentTypeCreateRequest request
    ) {
        TreatmentType updated = service.update(id, request.getName());
        return ApiResponse.ok(TreatmentTypeMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deactivate(id);
        return ApiResponse.ok(null);
    }
}
