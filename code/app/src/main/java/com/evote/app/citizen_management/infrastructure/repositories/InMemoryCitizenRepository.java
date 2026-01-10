package com.evote.app.citizen_management.infrastructure.repositories;

import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCitizenRepository implements CitizenRepository {

  private final Map<Email, Citizen> store = new HashMap<>();

  @Override
  public void save(Citizen citizen) {
    store.put(citizen.getEmail(), citizen);
  }

  @Override
  public void clear() {
    store.clear();
  }

  @Override
  public Optional<Citizen> findByEmail(Email email) {
    return Optional.ofNullable(store.get(email));
  }
}
