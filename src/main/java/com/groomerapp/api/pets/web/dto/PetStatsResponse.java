package com.groomerapp.api.pets.web.dto;

public record PetStatsResponse(
        long visitsCount,
        LastVisit lastVisit
) {
    public record LastVisit(Long id, String visitedAt) {}
}
