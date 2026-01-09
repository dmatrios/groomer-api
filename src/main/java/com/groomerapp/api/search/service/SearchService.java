package com.groomerapp.api.search.service;

import com.groomerapp.api.appointments.data.AppointmentRepository;
import com.groomerapp.api.clients.data.ClientPhoneRepository;
import com.groomerapp.api.clients.data.ClientRepository;
import com.groomerapp.api.pets.data.PetRepository;
import com.groomerapp.api.search.web.dto.SearchResponse;
import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.visits.data.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ClientRepository clientRepository;
    private final ClientPhoneRepository clientPhoneRepository;

    private final PetRepository petRepository;

    private final AppointmentRepository appointmentRepository;
    private final VisitRepository visitRepository;

    @Transactional(readOnly = true)
    public SearchResponse search(String q) {
        String query = normalize(q);
        if (query == null || query.length() < 2) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_FAILED, "q debe tener al menos 2 caracteres");
        }

        // 1) Clients por texto
        List<com.groomerapp.api.clients.domain.Client> clientsText = clientRepository.searchActive(query);

        // 2) Clients por teléfono (si el query parece número)
        String qDigits = digitsOnly(query);
        List<Long> clientIdsByPhone = qDigits.length() >= 3
                ? clientPhoneRepository.findClientIdsByPhoneLike(qDigits)
                : List.of();

        // 3) Unir clientIds (texto + teléfono)
        Set<Long> clientIds = new LinkedHashSet<>();
        for (var c : clientsText) clientIds.add(c.getId());
        clientIds.addAll(clientIdsByPhone);

        // si vino por teléfono, necesitamos cargar esos clients también:
        List<com.groomerapp.api.clients.domain.Client> clientsAll = new ArrayList<>(clientsText);
        if (!clientIdsByPhone.isEmpty()) {
            // Si tu repo tiene findAllById, úsalo (JpaRepository lo tiene)
            List<com.groomerapp.api.clients.domain.Client> byPhone = clientRepository.findAllById(clientIdsByPhone);
            // filtrar active por seguridad
            byPhone = byPhone.stream().filter(com.groomerapp.api.clients.domain.Client::isActive).toList();
            clientsAll.addAll(byPhone);
        }

        // quitar duplicados
        Map<Long, com.groomerapp.api.clients.domain.Client> clientsMap = new LinkedHashMap<>();
        for (var c : clientsAll) clientsMap.put(c.getId(), c);

        // 4) Pets: por texto + por clientIds encontrados
        List<com.groomerapp.api.pets.domain.Pet> petsText = petRepository.searchActive(query);

        List<com.groomerapp.api.pets.domain.Pet> petsByClients = clientIds.isEmpty()
                ? List.of()
                : petRepository.findAllByClientIdInAndActiveTrueOrderByNameAsc(new ArrayList<>(clientIds));

        Map<Long, com.groomerapp.api.pets.domain.Pet> petsMap = new LinkedHashMap<>();
        for (var p : petsText) petsMap.put(p.getId(), p);
        for (var p : petsByClients) petsMap.put(p.getId(), p);

        List<Long> petIds = new ArrayList<>(petsMap.keySet());

        // 5) Appointments/Visits (por pets + notes)
        // MVP: limitamos volumen con top N en memoria
        List<com.groomerapp.api.appointments.domain.Appointment> appts = new ArrayList<>();
        if (!petIds.isEmpty()) appts.addAll(appointmentRepository.findAllByPetIdIn(petIds));
        appts.addAll(appointmentRepository.searchByNotes(query));
        appts = appts.stream()
                .collect(Collectors.toMap(
                        com.groomerapp.api.appointments.domain.Appointment::getId,
                        a -> a,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                .values().stream()
                .limit(30)
                .toList();

        List<com.groomerapp.api.visits.domain.Visit> visits = new ArrayList<>();
        if (!petIds.isEmpty()) visits.addAll(visitRepository.findAllByPetIdInAndActiveTrue(petIds));
        visits.addAll(visitRepository.searchByNotes(query));
        visits = visits.stream()
                .collect(Collectors.toMap(
                        com.groomerapp.api.visits.domain.Visit::getId,
                        v -> v,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                .values().stream()
                .limit(30)
                .toList();

        // 6) Map a hits (limitamos para UI)
        List<SearchResponse.ClientHit> clientHits = clientsMap.values().stream()
                .limit(20)
                .map(c -> SearchResponse.ClientHit.builder()
                        .id(c.getId())
                        .code(c.getCode())
                        .fullName((c.getFirstName() + " " + c.getLastName()).trim())
                        .zoneText(c.getZoneText())
                        .build())
                .toList();

        List<SearchResponse.PetHit> petHits = petsMap.values().stream()
                .limit(20)
                .map(p -> SearchResponse.PetHit.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .name(p.getName())
                        .clientId(p.getClientId())
                        .build())
                .toList();

        List<SearchResponse.AppointmentHit> apptHits = appts.stream()
                .map(a -> SearchResponse.AppointmentHit.builder()
                        .id(a.getId())
                        .petId(a.getPetId())
                        .status(a.getStatus().name())
                        .startAt(a.getStartAt().toString())
                        .endAt(a.getEndAt().toString())
                        .notes(a.getNotes())
                        .build())
                .toList();

        List<SearchResponse.VisitHit> visitHits = visits.stream()
                .map(v -> SearchResponse.VisitHit.builder()
                        .id(v.getId())
                        .petId(v.getPetId())
                        .visitedAt(v.getVisitedAt().toString())
                        .totalAmount(v.getTotalAmount().toString())
                        .notes(v.getNotes())
                        .build())
                .toList();

        return SearchResponse.builder()
                .query(query)
                .clients(clientHits)
                .pets(petHits)
                .appointments(apptHits)
                .visits(visitHits)
                .build();
    }

    private String normalize(String q) {
        if (q == null) return null;
        String t = q.trim();
        return t.isBlank() ? null : t;
    }

    private String digitsOnly(String q) {
        return q.replaceAll("[^0-9]", "");
    }
}
