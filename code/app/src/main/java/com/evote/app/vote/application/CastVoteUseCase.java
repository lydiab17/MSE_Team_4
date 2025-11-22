package com.evote.app.vote.application;

import com.evote.app.vote.domain.PseudonymToken;
import com.evote.app.vote.domain.Vote;
import com.evote.app.vote.domain.voteRepository;
import com.evote.app.vote.events.VoteSubmittedEvent;

import java.time.Instant;
import java.util.Optional;

/**
 * Minimal UseCase implementation. Throws RuntimeException on errors for brevity.
 */
public class CastVoteUseCase {

    private final AuthPort authPort;
    private final voteRepository voteRepository;
    private final EventPublisher eventPublisher;

    public CastVoteUseCase(AuthPort authPort,
                           voteRepository voteRepository,
//                           VotingRepository votingRepository,
                           EventPublisher eventPublisher) {
        this.authPort = authPort;
        this.voteRepository = voteRepository;
//        this.votingRepository = votingRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(CastVoteDto dto) {
        // 1) auth
        String pseudonym = authPort.verifyAndReturnPseudonym(dto.authToken);
        if (pseudonym == null) {
            throw new RuntimeException("Not authenticated");
        }

        // 2) check voting exists and open
        Optional<VotingRepository.VotingReadModel> maybeVoting = votingRepository.findById(dto.votingId);
        if (maybeVoting.isEmpty() || !maybeVoting.get().open) {
            throw new RuntimeException("Voting not found or not open");
        }

        // 3) create Vote (domain)
        Vote vote = Vote.createNew(
                com.evote.app.vote.domain.VotingId.of(dto.votingId),
                dto.optionId,
                PseudonymToken.of(pseudonym)
        );

        // 4) duplicate check (sync)
        if (voteRepository.existsByVotingIdAndPseudonym(dto.votingId, pseudonym)) {
            throw new RuntimeException("Duplicate vote");
        }

        // 5) save
        voteRepository.save(vote);

        // 6) publish event (use strings for pseudonym to avoid extra dependencies in events package)
        VoteSubmittedEvent event = new VoteSubmittedEvent(vote.getId(), dto.votingId, dto.optionId, pseudonym, Instant.now());
        eventPublisher.publish(event);
    }
}
