package com.evote.app.citizen_management.domain.events;

import com.evote.app.citizen_management.application.dto.CitizenDto;

// TODO
public class CitizenCreatedEvent extends DomainEvent {
    private CitizenDto citizenDto;

    public CitizenDto getCitizenDto() {
        return citizenDto;
    }

    public void setCitizenDto(CitizenDto citizenDto) {
        this.citizenDto = citizenDto;
    }
    public CitizenCreatedEvent(CitizenDto citizenDto) {
        this.setCitizenDto(citizenDto);
    }

}
