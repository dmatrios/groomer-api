package com.groomerapp.api.clients.service;

import com.groomerapp.api.clients.data.ClientRepository;
import com.groomerapp.api.clients.domain.Client;
import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public Client create(String firstName, String lastName, Long zoneId, String zoneText, String notes) {
        validateNames(firstName, lastName);
        validateZone(zoneId, zoneText);

        Client saved = clientRepository.save(new Client(
                firstName.trim(),
                lastName.trim(),
                zoneId,
                normalizeZoneText(zoneId, zoneText),
                normalizeNotes(notes)
        ));

        // Generar code basado en id (simple, estable, MVP)
        String code = generateClientCode(saved.getId());
        saved.setCode(code);

        return clientRepository.save(saved);
    }

    @Transactional(readOnly = true)
    public Client getById(Long id) {
        return clientRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<Client> list(Long zoneId, Pageable pageable) {
        if (zoneId != null) {
            return clientRepository.findAllByActiveTrueAndZoneId(zoneId, pageable);
        }
        return clientRepository.findAllByActiveTrue(pageable);
    }

    @Transactional
    public Client update(Long id, String firstName, String lastName, Long zoneId, String zoneText, String notes) {
        validateNames(firstName, lastName);
        validateZone(zoneId, zoneText);

        Client client = getById(id);
        client.update(
                firstName.trim(),
                lastName.trim(),
                zoneId,
                normalizeZoneText(zoneId, zoneText),
                normalizeNotes(notes)
        );

        return clientRepository.save(client);
    }

    @Transactional
    public void deactivate(Long id) {
        Client client = getById(id);
        client.deactivate();
        clientRepository.save(client);
    }

    private void validateNames(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new BusinessRuleException(ErrorCode.CLIENT_NAME_REQUIRED, "Nombre y apellidos son obligatorios");
        }
    }

    private void validateZone(Long zoneId, String zoneText) {
        // Permitimos:
        // - zoneId (catálogo) y zoneText null/blank
        // - zoneText (libre) y zoneId null
        // - ambos null (sin zona)
        if (zoneId != null && zoneText != null && !zoneText.isBlank()) {
            throw new BusinessRuleException(ErrorCode.CLIENT_ZONE_INVALID, "Selecciona una zona o escribe una zona, no ambas");
        }
    }

    private String normalizeZoneText(Long zoneId, String zoneText) {
        if (zoneId != null) return null; // si elige catálogo, no guardamos texto
        if (zoneText == null) return null;
        String t = zoneText.trim();
        return t.isBlank() ? null : t;
    }

    private String normalizeNotes(String notes) {
        if (notes == null) return null;
        String t = notes.trim();
        return t.isBlank() ? null : t;
    }

    private String generateClientCode(Long id) {
        // CL-000001
        return "CL-" + String.format("%06d", id);
    }
}
