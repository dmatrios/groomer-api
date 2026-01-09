package com.groomerapp.api.catalogs.zones.web;

import com.groomerapp.api.catalogs.zones.domain.Zone;
import com.groomerapp.api.catalogs.zones.service.ZoneService;
import com.groomerapp.api.catalogs.zones.web.dto.ZoneCreateRequest;
import com.groomerapp.api.catalogs.zones.web.dto.ZoneResponse;
import com.groomerapp.api.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalogs/zones")
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping
    public ApiResponse<List<ZoneResponse>> list() {
        List<ZoneResponse> data = zoneService.listAll()
                .stream()
                .map(ZoneMapper::toResponse)
                .toList();

        return ApiResponse.ok(data);
    }

    @PostMapping
    public ApiResponse<ZoneResponse> create(@Valid @RequestBody ZoneCreateRequest request) {
        Zone created = zoneService.create(request.getName());
        return ApiResponse.ok(ZoneMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ApiResponse<ZoneResponse> update(@PathVariable Long id,
                                            @Valid @RequestBody ZoneCreateRequest request) {
        Zone updated = zoneService.update(id, request.getName());
        return ApiResponse.ok(ZoneMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        zoneService.deactivate(id);
        return ApiResponse.ok(null);
    }

}
