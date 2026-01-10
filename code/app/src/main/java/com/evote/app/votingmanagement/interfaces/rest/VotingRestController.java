package com.evote.app.votingmanagement.interfaces.rest;

import com.evote.app.votingmanagement.application.dto.CastVoteDto;
import com.evote.app.votingmanagement.application.dto.OptionResult;
import com.evote.app.votingmanagement.application.services.VoteCastingService;
import com.evote.app.votingmanagement.application.services.VotingCommandService;
import com.evote.app.votingmanagement.application.services.VotingQueryService;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.interfaces.dto.CastVoteRequest;
import com.evote.app.votingmanagement.interfaces.dto.CreateVotingRequest;
import com.evote.app.votingmanagement.interfaces.dto.OptionResultResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResultsResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST-Controller für Voting-bezogene Endpoints. */
@RestController
@RequestMapping("/api/votings")
public class VotingRestController {

  private final VotingCommandService commandService;
  private final VotingQueryService queryService;
  private final VoteCastingService voteCastingService;

  public VotingRestController(
      VotingCommandService commandService,
      VotingQueryService queryService,
      VoteCastingService voteCastingService) {
    this.commandService = commandService;
    this.queryService = queryService;
    this.voteCastingService = voteCastingService;
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
    Voting v =
        commandService.createVoting(
            request.id(),
            request.name(),
            request.info(),
            request.startDate(),
            request.endDate(),
            options);
    return VotingResponse.fromDomain(v);
  }

  /**
   * Öffnet (aktiviert) ein Voting.
   *
   * @param id die ID des Votings
   */
  @PostMapping("/{id}/open")
  public void open(@PathVariable int id) {
    commandService.openVoting(id);
  }

  /**
   * Liefert ein Voting zu einer gegebenen ID.
   *
   * @param id die ID des Votings
   * @return Voting als Response-DTO
   */
  @GetMapping("/{id}")
  public VotingResponse getById(@PathVariable int id) {
    return queryService
        .getVotingById(id)
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
    return queryService.getOpenVotings().stream().map(VotingResponse::fromDomain).toList();
  }

  /**
   * Gibt eine Stimme für ein Voting ab.
   *
   * <p>Beispiel-Request: POST /api/votings/1/votes { "voterKey": "abc123", "optionId": "Ja" }
   */
  @PostMapping("/{id}/votes")
  public void castVote(
      @PathVariable int id,
      @RequestBody CastVoteRequest request,
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    CastVoteDto dto = new CastVoteDto(extractBearerToken(authorization), id, request.optionId());

    voteCastingService.castVote(dto);
  }

  /**
   * Pure Function: Header -> Token (ohne Side-Effects). Robust gegen null/Leerzeichen/"Bearer"
   * Prefix.
   */
  static String extractBearerToken(String authorization) {
    return Optional.ofNullable(authorization)
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .map(s -> s.regionMatches(true, 0, "Bearer ", 0, 7) ? s.substring(7).trim() : s)
        .filter(s -> !s.isBlank())
        .orElseThrow(
            () -> new IllegalArgumentException("Authorization Header fehlt oder ist leer"));
  }

  /** Liefert die Anzahl Stimmen pro Option für ein Voting. */
  @GetMapping("/{id}/results")
  public VotingResultsResponse getResults(@PathVariable int id) {
    List<OptionResult> optionResults = queryService.getResultsForVoting(id);

    List<OptionResultResponse> responseList =
        optionResults.stream().map(OptionResultResponse::fromOptionResult).toList();

    return new VotingResultsResponse(id, responseList);
  }

  /**
   * Liefert alle aktuell nicht offenen Votings.
   *
   * @return Liste nicht offener Votings als Response-DTO
   */
  @GetMapping("/not-open")
  public List<VotingResponse> getNotOpen() {
    return queryService.getNotOpenVotings().stream().map(VotingResponse::fromDomain).toList();
  }
}
