package com.groomerapp.api.clients.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientPhoneResponse {

    private final Long id;
    private final String phone;
}
