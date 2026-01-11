package com.evote.app.citizen_management.domain.events;

import com.evote.app.citizen_management.application.dto.CitizenDto;

/**
 * Domain Event, das die Erstellung eines neuen Bürgers repräsentiert.
 *
 * <p>Dieses Event wird ausgelöst, wenn ein {@link CitizenDto} erfolgreich im System angelegt wurde.
 * Es enthält die relevanten Bürgerdaten, die zum Zeitpunkt der Erstellung vorlagen.
 */
public class CitizenCreatedEvent extends DomainEvent {

  /** DTO mit den Daten des neu erstellten Bürgers. */
  private CitizenDto citizenDto;

  /**
   * Liefert die Daten des erstellten Bürgers.
   *
   * @return das {@link CitizenDto} des neu angelegten Bürgers
   */
  public CitizenDto getCitizenDto() {
    return citizenDto;
  }

  /**
   * Setzt die Daten des erstellten Bürgers.
   *
   * @param citizenDto DTO mit den Bürgerdaten
   */
  public void setCitizenDto(CitizenDto citizenDto) {
    this.citizenDto = citizenDto;
  }

  /**
   * Erstellt ein neues {@code CitizenCreatedEvent}.
   *
   * @param citizenDto DTO mit den Daten des neu erstellten Bürgers
   */
  public CitizenCreatedEvent(CitizenDto citizenDto) {
    this.setCitizenDto(citizenDto);
  }
}
