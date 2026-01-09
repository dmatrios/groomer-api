package com.groomerapp.api.clients.web;

import com.groomerapp.api.clients.domain.ClientPhone;
import com.groomerapp.api.clients.web.dto.ClientPhoneResponse;

public final class ClientPhoneMapper {

    private ClientPhoneMapper() {}

    public static ClientPhoneResponse toResponse(ClientPhone p) {
        return ClientPhoneResponse.builder()
                .id(p.getId())
                .phone(p.getPhone())
                .build();
    }
}
