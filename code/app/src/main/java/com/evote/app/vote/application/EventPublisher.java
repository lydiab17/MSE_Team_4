package com.evote.app.vote.application;

/**
 * Simple event publisher for application layer.
 */
public interface EventPublisher {
    void publish(Object event);
}
