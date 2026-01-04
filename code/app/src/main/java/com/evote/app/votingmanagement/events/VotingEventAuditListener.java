package com.evote.app.votingmanagement.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener für Voting-Events (Audit/Logging).
 */
@Component
public class VotingEventAuditListener {

  private static final Logger log = LoggerFactory.getLogger(VotingEventAuditListener.class);

  @EventListener
  public void onVotingOpened(VotingOpenedEvent event) {
    log.info("AUDIT: Voting opened: id={}, openedAt={}", event.id(), event.openedAt());
  }

  @EventListener
  public void onVotingCreatedEvent(VotingCreatedEvent event) {
    log.info("AUDIT: Voting was created: id={}, name={}, startDate={}, endDate={}", event.id(), event.name(), event.startDate(), event.endDate());
  }

  @EventListener
  public void onVotingClosedEvent(VotingClosedEvent event) {
    log.info("AUDIT: Voting was created: id={}, closedAt={}", event.id(), event.closedAt());
  }

  @EventListener
  public void onVoteSubmittedEvent(VoteSubmittedEvent event) {
    log.info("AUDIT: Voting was created: votingId={}, optionId={}, pseudonymToken={}, submittedAt={}", event.votingId(), event.optionId(), event.pseudonymToken(), event.submittedAt());
  }
}
