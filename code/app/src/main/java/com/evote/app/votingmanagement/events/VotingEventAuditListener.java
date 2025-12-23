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
}
