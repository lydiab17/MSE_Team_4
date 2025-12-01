package com.evote.app.citizen_management.application.dto;

import com.evote.app.citizen_management.domain.model.Citizen;

public record CitizenLoginResponseDto(
        String email,
        String password
) {
    public static CitizenLoginResponseDto fromDomain(Citizen c) {
        return new CitizenLoginResponseDto(
                c.getEmail().email(),
                c.getPassword().password()
        );
    }
}

