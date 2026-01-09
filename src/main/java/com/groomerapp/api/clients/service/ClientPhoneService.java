package com.groomerapp.api.clients.service;

import com.groomerapp.api.clients.data.ClientPhoneRepository;
import com.groomerapp.api.clients.domain.ClientPhone;
import com.groomerapp.api.shared.exceptions.BusinessRuleException;
import com.groomerapp.api.shared.exceptions.ErrorCode;
import com.groomerapp.api.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientPhoneService {

    private final ClientService clientService; // para validar que el cliente existe y está activo
    private final ClientPhoneRepository phoneRepository;

    @Transactional(readOnly = true)
    public List<ClientPhone> listPhones(Long clientId) {
        clientService.getById(clientId);
        return phoneRepository.findAllByClientIdAndActiveTrueOrderByIdAsc(clientId);
    }

    @Transactional
    public ClientPhone addPhone(Long clientId, String phone) {
        clientService.getById(clientId);

        if (phone == null || phone.isBlank()) {
            throw new BusinessRuleException(ErrorCode.CLIENT_PHONE_REQUIRED, "El teléfono es obligatorio");
        }

        ClientPhone entity = new ClientPhone(clientId, phone.trim());
        return phoneRepository.save(entity);
    }

    @Transactional
    public void removePhone(Long clientId, Long phoneId) {
        clientService.getById(clientId);

        ClientPhone phone = phoneRepository.findByIdAndClientIdAndActiveTrue(phoneId, clientId)
                .orElseThrow(() -> new NotFoundException("Teléfono no encontrado"));

        phone.deactivate();
        phoneRepository.save(phone);
    }
}
