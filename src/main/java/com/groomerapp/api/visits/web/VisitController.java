package com.groomerapp.api.visits.web;

import com.groomerapp.api.pets.service.PetService;
import com.groomerapp.api.shared.web.ApiResponse;
import com.groomerapp.api.visits.domain.VisitItemCategory;
import com.groomerapp.api.visits.service.VisitService;
import com.groomerapp.api.visits.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/visits")
public class VisitController {

    private final VisitService visitService;
    private final VisitMapper visitMapper;
    private final PetService petService;

    @PostMapping
    public ApiResponse<VisitDetailResponse> create(@Valid @RequestBody VisitCreateRequest request) {

        var cmd = new VisitService.CreateVisitCommand(
                request.getPetId(),
                request.getAppointmentId(),
                request.getAutoCreateAppointment(),
                request.getVisitedAt(),
                request.getNotes(),
                mapItems(request.getItems()),
                mapPayment(request.getPayment())
        );

        var detail = visitService.create(cmd);

        String petName = petService.getById(detail.visit().getPetId()).getName();

        return ApiResponse.ok(
                visitMapper.toDetailResponse(detail.visit(), petName, detail.items(), detail.payment())
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<VisitDetailResponse> getById(@PathVariable Long id) {
        var detail = visitService.getDetail(id);

        String petName = petService.getById(detail.visit().getPetId()).getName();

        return ApiResponse.ok(
                visitMapper.toDetailResponse(detail.visit(), petName, detail.items(), detail.payment())
        );
    }

    @GetMapping
    public ApiResponse<List<VisitDetailResponse>> list(
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) VisitItemCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        // ✅ 1) Historial por mascota
        if (petId != null) {
            var details = visitService.listDetailsByPet(petId, category);

            // batch pet names (en este caso es 1, pero queda estándar)
            Set<Long> petIds = details.stream()
                    .map(d -> d.visit().getPetId())
                    .collect(Collectors.toSet());

            Map<Long, String> petNamesById = petService.mapNamesByIds(petIds);

            var data = details.stream()
                    .map(d -> {
                        Long pid = d.visit().getPetId();
                        String petName = petNamesById.getOrDefault(pid, "—");
                        return visitMapper.toDetailResponse(d.visit(), petName, d.items(), d.payment());
                    })
                    .toList();

            return ApiResponse.ok(data);
        }

        // ✅ 2) Listado por rango
        if (from != null && to != null) {
            var details = visitService.listDetailsByRange(from, to);

            Set<Long> petIds = details.stream()
                    .map(d -> d.visit().getPetId())
                    .collect(Collectors.toSet());

            Map<Long, String> petNamesById = petService.mapNamesByIds(petIds);

            var data = details.stream()
                    .map(d -> {
                        Long pid = d.visit().getPetId();
                        String petName = petNamesById.getOrDefault(pid, "—");
                        return visitMapper.toDetailResponse(d.visit(), petName, d.items(), d.payment());
                    })
                    .toList();

            return ApiResponse.ok(data);
        }

        throw new com.groomerapp.api.shared.exceptions.BusinessRuleException(
                com.groomerapp.api.shared.exceptions.ErrorCode.VALIDATION_FAILED,
                "Enviar petId o (from y to)"
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<VisitDetailResponse> update(@PathVariable Long id, @Valid @RequestBody VisitUpdateRequest request) {

        var cmd = new VisitService.UpdateVisitCommand(
                request.getVisitedAt(),
                request.getNotes(),
                mapItems(request.getItems()),
                mapPayment(request.getPayment())
        );

        var detail = visitService.update(id, cmd);

        String petName = petService.getById(detail.visit().getPetId()).getName();

        return ApiResponse.ok(
                visitMapper.toDetailResponse(detail.visit(), petName, detail.items(), detail.payment())
        );
    }

    /* ====== helpers ====== */

    private List<VisitService.ItemCommand> mapItems(List<VisitItemRequest> items) {
        if (items == null) return null;

        return items.stream()
                .map(it -> new VisitService.ItemCommand(
                        it.getCategory(),
                        it.getPrice(),
                        mapTreatmentDetail(it.getTreatmentDetail())
                ))
                .toList();
    }

    private VisitService.TreatmentDetailCommand mapTreatmentDetail(TreatmentDetailRequest td) {
        if (td == null) return null;

        return new VisitService.TreatmentDetailCommand(
                td.getTreatmentTypeId(),
                td.getTreatmentTypeText(),
                td.getMedicineId(),
                td.getMedicineText(),
                td.getNextDate()
        );
    }

    private VisitService.PaymentCommand mapPayment(PaymentRequest p) {
        if (p == null) return null;

        return new VisitService.PaymentCommand(
                p.getStatus(),
                p.getMethod(),
                p.getAmountPaid()
        );
    }
}
