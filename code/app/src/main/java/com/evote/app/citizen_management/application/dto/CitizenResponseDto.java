package com.evote.app.citizen_management.application.dto;

import com.evote.app.citizen_management.domain.model.Citizen;

public record CitizenResponseDto(
        String email,
        String vorname,
        String nachname
) {
    public static CitizenResponseDto fromDomain(CitizenDto c) {
        return new CitizenResponseDto(
                c.getEmail(),
                c.getVorname(),
                c.getNachname()
        );
    }
}

