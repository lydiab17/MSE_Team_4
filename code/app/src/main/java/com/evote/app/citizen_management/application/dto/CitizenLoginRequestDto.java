package com.evote.app.citizen_management.application.dto;

public record CitizenLoginRequestDto(
        String email,
        String password
) {}
