package com.groomerapp.api.search.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponse {

    private final String query;

    private final List<ClientHit> clients;
    private final List<PetHit> pets;
    private final List<AppointmentHit> appointments;
    private final List<VisitHit> visits;

    @Getter @Builder
    public static class ClientHit {
        private final Long id;
        private final String code;
        private final String fullName;
        private final String zoneText;
    }

    @Getter @Builder
    public static class PetHit {
        private final Long id;
        private final String code;
        private final String name;
        private final Long clientId;
    }

    @Getter @Builder
    public static class AppointmentHit {
        private final Long id;
        private final Long petId;
        private final String status;
        private final String startAt;
        private final String endAt;
        private final String notes;
    }

    @Getter @Builder
    public static class VisitHit {
        private final Long id;
        private final Long petId;
        private final String visitedAt;
        private final String totalAmount;
        private final String notes;
    }
}
