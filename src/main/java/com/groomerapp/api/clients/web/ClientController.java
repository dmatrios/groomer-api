package com.groomerapp.api.clients.web;

import com.groomerapp.api.clients.domain.Client;
import com.groomerapp.api.clients.service.ClientPhoneService;
import com.groomerapp.api.clients.service.ClientService;
import com.groomerapp.api.clients.web.dto.*;
import com.groomerapp.api.pets.service.PetService;
import com.groomerapp.api.pets.web.dto.PetResponse;
import com.groomerapp.api.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;
    private final ClientPhoneService clientPhoneService;
    private final PetService petService;

    @PostMapping
    public ApiResponse<ClientResponse> create(@Valid @RequestBody ClientCreateRequest request) {
        Client created = clientService.create(
                request.getFirstName(),
                request.getLastName(),
                request.getZoneId(),
                request.getZoneText(),
                request.getNotes()
        );
        return ApiResponse.ok(ClientMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ApiResponse<ClientResponse> getById(@PathVariable Long id) {
        Client client = clientService.getById(id);
        return ApiResponse.ok(ClientMapper.toResponse(client));
    }

    @GetMapping
    public ApiResponse<List<ClientResponse>> list(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sort
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<Client> result = clientService.list(zoneId, pageable);

        List<ClientResponse> data = result.getContent()
                .stream()
                .map(ClientMapper::toResponse)
                .toList();

        ApiResponse.Meta meta = ApiResponse.Meta.builder()
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .sort(sort + ",asc")
                .build();

        return ApiResponse.<List<ClientResponse>>builder()
                .data(data)
                .meta(meta)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ClientResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody ClientCreateRequest request) {
        Client updated = clientService.update(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getZoneId(),
                request.getZoneText(),
                request.getNotes()
        );
        return ApiResponse.ok(ClientMapper.toResponse(updated));
    }

    // Phones

    @GetMapping("/{id}/phones")
    public ApiResponse<List<ClientPhoneResponse>> listPhones(@PathVariable Long id) {
        List<ClientPhoneResponse> data = clientPhoneService.listPhones(id)
                .stream()
                .map(ClientPhoneMapper::toResponse)
                .toList();

        return ApiResponse.ok(data);
    }

    @PostMapping("/{id}/phones")
    public ApiResponse<ClientPhoneResponse> addPhone(@PathVariable Long id,
                                                     @Valid @RequestBody ClientPhoneCreateRequest request) {
        return ApiResponse.ok(ClientPhoneMapper.toResponse(
                clientPhoneService.addPhone(id, request.getPhone())
        ));
    }

    @DeleteMapping("/{id}/phones/{phoneId}")
    public ApiResponse<Void> deletePhone(@PathVariable Long id, @PathVariable Long phoneId) {
        clientPhoneService.removePhone(id, phoneId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/pets")
    public ApiResponse<List<PetResponse>> listPets(@PathVariable Long id) {
        List<PetResponse> data = petService.getByClient(id);
        return ApiResponse.ok(data);
    }

}
