package com.groomerapp.api.clients.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientResponse {

    private final Long id;
    private final String code;
    private final String firstName;
    private final String lastName;
    private final Long zoneId;
    private final String zoneText;
    private final String notes;
}
