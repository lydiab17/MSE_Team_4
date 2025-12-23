package com.evote.app.votingmanagement.interfaces.rest;

import com.evote.app.votingmanagement.application.dto.CastVoteDto;
import com.evote.app.votingmanagement.application.dto.OptionResult;
import com.evote.app.votingmanagement.application.services.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Voting;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.evote.app.votingmanagement.interfaces.dto.*;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

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
        // hier nehmen wir die System-Uhr, nicht fixedClock
        return service.getOpenVotings(java.time.Clock.systemDefaultZone()).stream()
                .map(VotingResponse::fromDomain)
                .toList();
    }

    /**
     * Gibt eine Stimme für ein Voting ab.
     *
     * Beispiel-Request:
     * POST /api/votings/1/votes
     * {
     *   "voterKey": "abc123",
     *   "optionId": "Ja"
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
                token,      // voterKey/authToken (String)
                id,         // votingId (aus Path)
                request.optionId()
        );

        service.castVote(dto);
    }


    /**
     * Liefert die Anzahl Stimmen pro Option für ein Voting.
     *
     * Beispiel-Response:
     * {
     *   "votingId": 1,
     *   "results": [
     *     { "option": "Ja",  "count": 10 },
     *     { "option": "Nein","count": 3  }
     *   ]
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

    // in VotingRestController

    @GetMapping("/not-open")
    public List<VotingResponse> getNotOpen() {
        return service.getNotOpenVotings().stream()
                .map(VotingResponse::fromDomain)
                .toList();
    }

}
