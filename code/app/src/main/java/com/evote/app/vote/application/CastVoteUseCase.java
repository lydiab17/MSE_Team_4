package com.evote.app.Vote.application;

import com.evote.app.Vote.application.port.AuthPort;
import com.evote.app.Vote.application.port.EventPublisher;
import com.evote.vote.domain.Vote;
import com.evote.vote.domain.VoteRepository;
import com.example.evote.vote.events.VoteSubmittedEvent;
import com.evote.shared.VotingId;

public class CastVoteUseCase {

    private final AuthPort authPort;
    private final VoteRepository voteRepository;
    private final VotingRepository votingRepository; // assumed in voting context
    private final EventPublisher eventPublisher;

    public CastVoteUseCase(AuthPort authPort, VoteRepository voteRepository, VotingRepository votingRepository, EventPublisher eventPublisher) {
        this.authPort = authPort;
        this.voteRepository = voteRepository;
        this.votingRepository = votingRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(CastVoteDto dto) {
        var vs = authPort.verify(dto.token());
        if (!vs.isVerified()) throw new UnauthorizedException();

        // check voting exists and open
        var voting = votingRepository.findById(dto.votingId()).orElseThrow();
        // domain method will validate option/time, but not duplicates
        // create vote (domain)
        PseudonymToken pseudonym = vs.getPseudonym();
        Vote vote = Vote.createNew(new VotingId(dto.votingId()), dto.optionId(), pseudonym);

        // duplicate check (sync approach)
        if (voteRepository.existsByVotingIdAndPseudonym(dto.votingId(), pseudonym.value())) {
            throw new DuplicateVoteException();
        }

        voteRepository.save(vote);
        eventPublisher.publish(new VoteSubmittedEvent(vote.getId(), dto.votingId(), dto.optionId(), pseudonym, vote.getSubmittedAt()));
    }
}
