package com.evote.app.votingmanagement.ui.api;

import com.evote.app.sharedkernel.AuthSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UiApiConfig {

    @Bean
    public VotingApiClient votingApiClient(AuthSession session) {
        return new VotingApiClient(session::token);
    }
}
