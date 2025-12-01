package com.evote.app.votingmanagement.application;

/**
 * Simple event publisher for application layer.
 */
public interface EventPublisher {
  void publish(Object event);
}
