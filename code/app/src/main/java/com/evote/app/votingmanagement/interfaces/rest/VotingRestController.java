package com.evote.app.votingmanagement.interfaces.rest;

import com.evote.app.votingmanagement.application.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Voting;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für Voting-bezogene Endpoints.
 */
@RestController
@RequestMapping("/api/votings")
public class VotingRestController {

  private final VotingApplicationService service;

  public VotingRestController(VotingApplicationService service) {
    this.service = service;
  }

  // --- DTOs (Request/Response) ---

  /**
   * Request-DTO zum Erstellen eines neuen Votings.
   */
  public record CreateVotingRequest(
          int id,
          String name,
          String info,
          LocalDate startDate,
          LocalDate endDate,
          List<String> options
  ) {
  }

  /**
   * Response-DTO zur Darstellung eines Votings.
   */
  public record VotingResponse(
          int id,
          String name,
          String info,
          LocalDate startDate,
          LocalDate endDate,
          boolean open
  ) {
    static VotingResponse fromDomain(Voting v) {
      return new VotingResponse(
              v.getId(),
              v.getName(),
              v.getInfo(),
              v.getStartDate(),
              v.getEndDate(),
              v.isVotingStatus() // oder v.isOpen(Clock.systemDefaultZone())
      );
    }
  }

  // --- Endpoints ---

  /**
   * Legt ein neues Voting an.
   *
   * @param request Daten für das neue Voting
   * @return das angelegte Voting als Response-DTO
   */
  @PostMapping
  public VotingResponse create(@RequestBody CreateVotingRequest request) {
    Set<String> options = new LinkedHashSet<>(request.options());
    Voting v = service.createVoting(
            request.id(),
            request.name(),
            request.info(),
            request.startDate(),
            request.endDate(),
            options
    );
    return VotingResponse.fromDomain(v);
  }

  /**
   * Öffnet (aktiviert) ein Voting.
   *
   * @param id die ID des Votings
   */
  @PostMapping("/{id}/open")
  public void open(@PathVariable int id) {
    service.openVoting(id);
  }

  /**
   * Liefert ein Voting zu einer gegebenen ID.
   *
   * @param id die ID des Votings
   * @return Voting als Response-DTO
   */
  @GetMapping("/{id}")
  public VotingResponse getById(@PathVariable int id) {
    return service.getVotingById(id)
            .map(VotingResponse::fromDomain)
            .orElseThrow(() -> new IllegalArgumentException("Voting nicht gefunden"));
  }

  /**
   * Liefert alle aktuell offenen Votings.
   *
   * @return Liste offener Votings als Response-DTO
   */
  @GetMapping("/open")
  public List<VotingResponse> getOpen() {
    // hier nehmen wir die System-Uhr, nicht fixedClock
    return service.getOpenVotings(java.time.Clock.systemDefaultZone()).stream()
            .map(VotingResponse::fromDomain)
            .toList();
  }
}
