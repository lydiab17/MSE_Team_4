package com.evote.app.votingmanagement.ui.api;

import com.evote.app.votingmanagement.interfaces.dto.CastVoteRequest;
import com.evote.app.votingmanagement.interfaces.dto.CreateVotingRequest;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class VotingApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/votings";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

    private final Supplier<Optional<String>> tokenSupplier;

    public VotingApiClient(Supplier<Optional<String>> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    public VotingResponse createVoting(CreateVotingRequest req) throws Exception {
        String json = om.writeValueAsString(req);

        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        addAuthHeaderIfPresent(b);

        HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) throw new IllegalStateException(resp.body());
        return om.readValue(resp.body(), VotingResponse.class);
    }

    public void openVoting(int id) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id + "/open"))
                .POST(HttpRequest.BodyPublishers.noBody());

        addAuthHeaderIfPresent(b);

        HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) throw new IllegalStateException(resp.body());
    }

    public VotingResponse getById(int id) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET();

        addAuthHeaderIfPresent(b);

        HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) throw new IllegalStateException(resp.body());
        return om.readValue(resp.body(), VotingResponse.class);
    }

    public List<VotingResponse> getOpenVotings() throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/open"))
                .GET();

        addAuthHeaderIfPresent(b);

        HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) throw new IllegalStateException(resp.body());
        return om.readValue(resp.body(), new TypeReference<List<VotingResponse>>() {});
    }

    public List<VotingResponse> getNotOpenVotings() throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/not-open"))
                .GET();

        addAuthHeaderIfPresent(b);

        HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) throw new IllegalStateException(resp.body());
        return om.readValue(resp.body(), new TypeReference<List<VotingResponse>>() {});
    }

    public void castVote(int votingId, String optionId) throws Exception {
        String json = om.writeValueAsString(new CastVoteRequest(optionId));

        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + votingId + "/votes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        addAuthHeaderIfPresent(b);

        HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + resp.statusCode() + ": " + resp.body());
        }
    }



    private void addAuthHeaderIfPresent(HttpRequest.Builder b) {
        if (tokenSupplier == null) return;

        Optional<String> tokenOpt = Optional.ofNullable(tokenSupplier.get())
                .orElse(Optional.empty());

        tokenOpt.ifPresent(t -> b.header("Authorization", "Bearer " + t));
    }
}
