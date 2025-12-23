package com.evote.app.votingmanagement.interfaces.rest;

import com.evote.app.votingmanagement.application.dto.CastVoteDto;
import com.evote.app.votingmanagement.application.dto.OptionResult;
import com.evote.app.votingmanagement.application.services.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.interfaces.dto.CastVoteRequest;
import com.evote.app.votingmanagement.interfaces.dto.CreateVotingRequest;
import com.evote.app.votingmanagement.interfaces.dto.OptionResultResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResultsResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

  // --- Endpoints ---

  /**
   * Legt ein neues Voting an.
   *
   * @param request Daten für das neue Voting
   * @return das angelegte Voting als Response-DTO
   */
  @PostMapping
  @RateLimiter(name = "voteAction")
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
    return service.getOpenVotings(Clock.systemDefaultZone()).stream()
            .map(VotingResponse::fromDomain)
            .toList();
  }

  /**
   * Gibt eine Stimme für ein Voting ab.
   *
   * <p>Beispiel-Request:
   * POST /api/votings/1/votes
   * {
   * "voterKey": "abc123",
   * "optionId": "Ja"
   * }
   */
  @PostMapping("/{id}/votes")
  public void castVote(@PathVariable int id,
                       @RequestBody CastVoteRequest request,
                       @RequestHeader("Authorization") String authorization) {

    // "Bearer <jwt>" -> nur Token extrahieren
    String token = authorization.startsWith("Bearer ")
            ? authorization.substring(7)
            : authorization;

    CastVoteDto dto = new CastVoteDto(
            token,
            id,
            request.optionId()
    );

    service.castVote(dto);
  }

  /**
   * Liefert die Anzahl Stimmen pro Option für ein Voting.
   *
   * <p>Beispiel-Response:
   * {
   * "votingId": 1,
   * "results": [
   * { "option": "Ja",  "count": 10 },
   * { "option": "Nein","count": 3  }
   * ]
   * }
   */
  @GetMapping("/{id}/results")
  public VotingResultsResponse getResults(@PathVariable int id) {
    List<OptionResult> optionResults = service.getResultsForVoting(id);

    List<OptionResultResponse> responseList = optionResults.stream()
            .map(OptionResultResponse::fromOptionResult)
            .toList();

    return new VotingResultsResponse(id, responseList);
  }

  /**
   * Liefert alle aktuell nicht offenen Votings.
   *
   * @return Liste nicht offener Votings als Response-DTO
   */
  @GetMapping("/not-open")
  public List<VotingResponse> getNotOpen() {
    return service.getNotOpenVotings().stream()
            .map(VotingResponse::fromDomain)
            .toList();
  }
}
