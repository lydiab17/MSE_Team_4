package com.evote.app.votingmanagement.ui.api;

import com.evote.app.votingmanagement.interfaces.dto.CastVoteRequest;
import com.evote.app.votingmanagement.interfaces.dto.CreateVotingRequest;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResultsResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Kleiner HTTP-Client für die Voting-REST-API (UI/Client-Seite).
 */
public class VotingApiClient {

  private static final String BASE_URL = "http://localhost:8080/api/votings";

  private final HttpClient http = HttpClient.newHttpClient();
  private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

  private final Supplier<Optional<String>> tokenSupplier;

  /**
   * Erstellt einen API-Client.
   *
   * @param tokenSupplier liefert optional ein JWT (z.B. aus einer Session); kann {@code null} sein
   */
  public VotingApiClient(Supplier<Optional<String>> tokenSupplier) {
    this.tokenSupplier = tokenSupplier;
  }

  /**
   * Legt ein neues Voting an.
   *
   * @param req Request-DTO
   * @return das angelegte Voting
   * @throws Exception bei HTTP-/Serialisierungsfehlern
   */
  public VotingResponse createVoting(CreateVotingRequest req) throws Exception {
    String json = om.writeValueAsString(req);

    HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json));

    addAuthHeaderIfPresent(b);

    HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 300) {
      throw new IllegalStateException(resp.body());
    }
    return om.readValue(resp.body(), VotingResponse.class);
  }

  /**
   * Öffnet (aktiviert) ein Voting.
   *
   * @param id Voting-ID
   * @throws Exception bei HTTP-Fehlern
   */
  public void openVoting(int id) throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/" + id + "/open"))
            .POST(HttpRequest.BodyPublishers.noBody());

    addAuthHeaderIfPresent(b);

    HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 300) {
      throw new IllegalStateException(resp.body());
    }
  }

  /**
   * Liefert ein Voting anhand seiner ID.
   *
   * @param id Voting-ID
   * @return Voting-Response
   * @throws Exception bei HTTP-Fehlern
   */
  public VotingResponse getById(int id) throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/" + id))
            .GET();

    addAuthHeaderIfPresent(b);

    HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 300) {
      throw new IllegalStateException(resp.body());
    }
    return om.readValue(resp.body(), VotingResponse.class);
  }

  /**
   * Liefert alle aktuell offenen Votings.
   *
   * @return Liste offener Votings
   * @throws Exception bei HTTP-Fehlern
   */
  public List<VotingResponse> getOpenVotings() throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/open"))
            .GET();

    addAuthHeaderIfPresent(b);

    HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 300) {
      throw new IllegalStateException(resp.body());
    }
    return om.readValue(resp.body(), new TypeReference<List<VotingResponse>>() {
    });
  }

  /**
   * Liefert alle aktuell nicht offenen Votings.
   *
   * @return Liste nicht offener Votings
   * @throws Exception bei HTTP-Fehlern
   */
  public List<VotingResponse> getNotOpenVotings() throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/not-open"))
            .GET();

    addAuthHeaderIfPresent(b);

    HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 300) {
      throw new IllegalStateException(resp.body());
    }
    return om.readValue(resp.body(), new TypeReference<List<VotingResponse>>() {
    });
  }

  /**
   * Gibt eine Stimme ab.
   *
   * @param votingId Voting-ID
   * @param optionId Option-ID/Text
   * @throws Exception bei HTTP-Fehlern
   */
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

  /**
   * Liefert die Ergebnisliste (Stimmen pro Option) für ein Voting.
   *
   * @param votingId Voting-ID
   * @return Ergebnisse als Response-DTO
   * @throws Exception bei HTTP-Fehlern
   */
  public VotingResultsResponse getResults(int votingId) throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/" + votingId + "/results"))
            .GET();

    addAuthHeaderIfPresent(b);

    HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 300) {
      throw new IllegalStateException(resp.body());
    }
    return om.readValue(resp.body(), VotingResultsResponse.class);
  }

  /**
   * Fügt einen Authorization-Header hinzu, sofern ein Token verfügbar ist.
   *
   * @param b Request-Builder
   */
  private void addAuthHeaderIfPresent(HttpRequest.Builder b) {
    if (tokenSupplier == null) {
      return;
    }

    Optional<String> tokenOpt = Optional.ofNullable(tokenSupplier.get())
            .orElse(Optional.empty());

    tokenOpt.ifPresent(t -> b.header("Authorization", "Bearer " + t));
  }
}
