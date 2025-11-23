package com.evote.app.votingmanagement.interfaces.rest;

import com.evote.app.votingmanagement.application.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Voting;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/votings")
public class VotingRestController {

    private final VotingApplicationService service;

    public VotingRestController(VotingApplicationService service) {
        this.service = service;
    }

    // --- DTOs (Request/Response) ---

    public record CreateVotingRequest(
            int id,
            String name,
            String info,
            LocalDate startDate,
            LocalDate endDate,
            List<String> options
    ) {}

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

    @PostMapping("/{id}/open")
    public void open(@PathVariable int id) {
        service.openVoting(id);
    }

    @GetMapping("/{id}")
    public VotingResponse getById(@PathVariable int id) {
        return service.getVotingById(id)
                .map(VotingResponse::fromDomain)
                .orElseThrow(() -> new IllegalArgumentException("Voting nicht gefunden"));
    }

    @GetMapping("/open")
    public List<VotingResponse> getOpen() {
        // hier nehmen wir die System-Uhr, nicht fixedClock
        return service.getOpenVotings(java.time.Clock.systemDefaultZone()).stream()
                .map(VotingResponse::fromDomain)
                .toList();
    }
}
