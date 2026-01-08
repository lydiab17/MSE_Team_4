package com.evote.app.citizen_management.application.commands;

public record CitizenRegistrationCommand(
    String vorname,
    String nachname,
    String email,
    String password
) {

}

