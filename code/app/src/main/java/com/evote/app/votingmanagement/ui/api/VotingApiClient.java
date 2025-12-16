package com.evote.app.votingmanagement.ui.api;

import com.evote.app.votingmanagement.ui.api.TokenStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public class VotingApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/votings";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // --- DTOs passend zu deinem RestController ---
    public record CreateVotingRequest(int id, String name, String info,
                                      LocalDate startDate, LocalDate endDate,
                                      List<String> options) {}

    public record VotingResponse(int id, String name, String info,
                                 LocalDate startDate, LocalDate endDate,
                                 boolean open) {}

    public record CastVoteRequest(String authToken, int votingId, String optionId) {}

    public record OptionResultResponse(String option, long count) {}

    // --- Helper: Request Builder mit JWT ---
    private HttpRequest.Builder authorizedRequest(String url) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");

        TokenStore.getJwt().ifPresent(token ->
                builder.header("Authorization", "Bearer " + token)
        );

        return builder;
    }

    public VotingResponse createVoting(CreateVotingRequest payload) throws Exception {
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = authorizedRequest(BASE_URL)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("createVoting failed: " + response.statusCode() + " " + response.body());
        }
        return mapper.readValue(response.body(), VotingResponse.class);
    }

    public List<VotingResponse> getOpenVotings() throws Exception {
        HttpRequest request = authorizedRequest(BASE_URL + "/open")
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("getOpenVotings failed: " + response.statusCode());
        }
        return mapper.readValue(response.body(), new TypeReference<List<VotingResponse>>() {});
    }

    public void castVote(int votingId, String optionId) throws Exception {
        // Wenn du im Backend aktuell authToken im Body erwartest:
        String token = TokenStore.getJwt().orElseThrow(() -> new IllegalStateException("Not logged in"));

        var payload = new CastVoteRequest(token, votingId, optionId);
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = authorizedRequest(BASE_URL + "/" + votingId + "/votes")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new RuntimeException("castVote failed: " + response.statusCode() + " " + response.body());
        }
    }

    public List<OptionResultResponse> getResults(int votingId) throws Exception {
        HttpRequest request = authorizedRequest(BASE_URL + "/" + votingId + "/results")
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("getResults failed: " + response.statusCode());
        }
        return mapper.readValue(response.body(), new TypeReference<List<OptionResultResponse>>() {});
    }
}
