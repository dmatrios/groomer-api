package com.groomerapp.api.clients.web;

import com.groomerapp.api.clients.domain.Client;
import com.groomerapp.api.clients.web.dto.ClientResponse;

public final class ClientMapper {

    private ClientMapper() {}

    public static ClientResponse toResponse(Client c) {
        return ClientResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .zoneId(c.getZoneId())
                .zoneText(c.getZoneText())
                .notes(c.getNotes())
                .build();
    }
}
