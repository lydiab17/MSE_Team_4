package com.evote.app.votingmanagement.ui.api;

import com.evote.app.citizen_management.application.dto.CitizenResponseDto;
import com.evote.app.votingmanagement.interfaces.dto.CastVoteRequest;
import com.evote.app.votingmanagement.interfaces.dto.CreateVotingRequest;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResultsResponse;
import com.evote.app.votingmanagement.ui.api.exceptions.VotingApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Kleiner HTTP-Client für die Voting-REST-API (UI/Client-Seite).
 *
 * Wirft bei Fehlern ausschließlich {@link VotingApiException} (keine checked Exceptions).
 */
public class VotingApiClient {

  // Defaults für die echte Anwendung
  private static final String DEFAULT_SERVER_ORIGIN = "http://localhost:8080";

  // Optionale Overrides per System Property (praktisch für Tests / CI)
  // z.B. -Devote.api.origin=http://localhost:8081
  private static final String ORIGIN_PROP = "evote.api.origin";

  private final HttpClient http;
  private final ObjectMapper om;
  private final Supplier<Optional<String>> tokenSupplier;

  // Statt static final: instanzbasiert
  private final String baseUrlVotings;   // .../api/votings
  private final String baseUrlCitizens;  // .../api/citizens

  /**
   * Erstellt einen API-Client mit Default-Origin (oder Property Override).
   *
   * @param tokenSupplier liefert optional ein JWT (z.B. aus einer Session); kann {@code null} sein
   */
  public VotingApiClient(Supplier<Optional<String>> tokenSupplier) {
    this(HttpClient.newHttpClient(), new ObjectMapper().findAndRegisterModules(), tokenSupplier, resolveOrigin());
  }

  /**
   * Optionaler Konstruktor für Tests/DI: Base-Origin explizit setzen.
   * Beispiele:
   *   new VotingApiClient(Optional::empty, "http://localhost:8081")
   *   new VotingApiClient(Optional::empty, "http://127.0.0.1:12345")
   */
  public VotingApiClient(Supplier<Optional<String>> tokenSupplier, String serverOrigin) {
    this(HttpClient.newHttpClient(), new ObjectMapper().findAndRegisterModules(), tokenSupplier, serverOrigin);
  }

  // Optionaler Konstruktor für Tests/DI (voll konfigurierbar)
  public VotingApiClient(HttpClient http, ObjectMapper om, Supplier<Optional<String>> tokenSupplier, String serverOrigin) {
    this.http = http;
    this.om = om;
    this.tokenSupplier = tokenSupplier;

    String origin = normalizeOrigin(serverOrigin);
    this.baseUrlVotings = origin + "/api/votings";
    this.baseUrlCitizens = origin + "/api/citizens";
  }

  private static String resolveOrigin() {
    // Property gewinnt, sonst Default
    String prop = System.getProperty(ORIGIN_PROP);
    return (prop == null || prop.isBlank()) ? DEFAULT_SERVER_ORIGIN : prop;
  }

  private static String normalizeOrigin(String origin) {
    if (origin == null || origin.isBlank()) return DEFAULT_SERVER_ORIGIN;
    // trailing slash entfernen, damit ".../api/..." sauber wird
    return origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin;
  }

  /** Legt ein neues Voting an. */
  public VotingResponse createVoting(CreateVotingRequest req) {
    HttpRequest request = requestBuilder("")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(req)))
            .build();

    HttpResponse<String> resp = send(request);
    throwIfError(request, resp);

    return fromJson(resp.body(), VotingResponse.class, request.uri());
  }

  /** Öffnet (aktiviert) ein Voting. */
  public void openVoting(int id) {
    HttpRequest request = requestBuilder("/" + id + "/open")
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

    HttpResponse<String> resp = send(request);
    throwIfError(request, resp);
  }

  /** Liefert ein Voting anhand seiner ID. */
  public VotingResponse getById(int id) {
    HttpRequest request = requestBuilder("/" + id)
            .GET()
            .build();

    HttpResponse<String> resp = send(request);
    throwIfError(request, resp);

    return fromJson(resp.body(), VotingResponse.class, request.uri());
  }

  /** Liefert alle aktuell offenen Votings. */
  public List<VotingResponse> getOpenVotings() {
    HttpRequest request = requestBuilder("/open")
            .GET()
            .build();

    HttpResponse<String> resp = send(request);
    throwIfError(request, resp);

    return fromJson(resp.body(), new TypeReference<List<VotingResponse>>() {}, request.uri());
  }

  public CitizenResponseDto getCurrentUser() {
    HttpRequest request = requestBuilderCitizens("/citizen")
            .GET()
            .build();

    HttpResponse<String> resp = send(request);
    throwIfError(request, resp);

    return fromJson(resp.body(), new TypeReference<CitizenResponseDto>() {}, request.uri());
  }

  /** Liefert alle aktuell nicht offenen Votings. */
  public List<VotingResponse> getNotOpenVotings() {
    HttpRequest request = requestBuilder("/not-open")
            .GET()
            .build();

    HttpResponse<String> resp = send(request);
    throwIfError(request, resp);

    return fromJson(resp.body(), new TypeReference<List<VotingResponse>>() {}, request.uri());
  }

  /** Gibt eine Stimme ab. */
  public void castVote(int votingId, String optionId) {
    HttpRequest request = requestBuilder("/" + votingId + "/votes")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(new CastVoteRequest(optionId))))
            .build();

    HttpResponse<String> resp = send(request);
    throwIfError(request, resp);
  }

  /** Liefert die Ergebnisliste (Stimmen pro Option) für ein Voting. */
  public VotingResultsResponse getResults(int votingId) {
    HttpRequest request = requestBuilder("/" + votingId + "/results")
            .GET()
            .build();

    HttpResponse<String> resp = send(request);
    throwIfError(request, resp);

    return fromJson(resp.body(), VotingResultsResponse.class, request.uri());
  }

  // -------------------------
  // Helper
  // -------------------------

  private HttpRequest.Builder requestBuilder(String pathSuffix) {
    URI uri = URI.create(baseUrlVotings + pathSuffix);
    HttpRequest.Builder b = HttpRequest.newBuilder().uri(uri);
    addAuthHeaderIfPresent(b);
    return b;
  }

  private HttpRequest.Builder requestBuilderCitizens(String pathSuffix) {
    URI uri = URI.create(baseUrlCitizens + pathSuffix);
    HttpRequest.Builder b = HttpRequest.newBuilder().uri(uri);
    addAuthHeaderIfPresent(b);
    return b;
  }

  private void addAuthHeaderIfPresent(HttpRequest.Builder b) {
    if (tokenSupplier == null) return;
    Optional<String> tokenOpt = Optional.ofNullable(tokenSupplier.get()).orElse(Optional.empty());
    tokenOpt.ifPresent(t -> b.header("Authorization", "Bearer " + t));
  }

  private HttpResponse<String> send(HttpRequest request) {
    try {
      return http.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (HttpTimeoutException e) {
      throw new VotingApiException("Timeout beim HTTP-Call: " + request.method() + " " + request.uri(), e);
    } catch (IOException e) {
      throw new VotingApiException("I/O-Fehler beim HTTP-Call: " + request.method() + " " + request.uri(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new VotingApiException("HTTP-Call unterbrochen: " + request.method() + " " + request.uri(), e);
    }
  }

  private void throwIfError(HttpRequest request, HttpResponse<String> resp) {
    int sc = resp.statusCode();
    if (sc < 300) return;

    String body = resp.body();
    String bodyPart = (body == null || body.isBlank()) ? "" : " | body=" + abbreviate(body, 2_000);

    String msg = "HTTP " + sc + " bei " + request.method() + " " + request.uri() + bodyPart;
    throw new VotingApiException(msg);
  }

  private String toJson(Object value) {
    try {
      return om.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new VotingApiException("Kann Objekt nicht zu JSON serialisieren (" + value.getClass().getSimpleName() + ")", e);
    }
  }

  private <T> T fromJson(String json, Class<T> type, URI uri) {
    try {
      return om.readValue(json, type);
    } catch (IOException e) {
      throw new VotingApiException("Kann JSON nicht in " + type.getSimpleName() + " deserialisieren (uri=" + uri + ")", e);
    }
  }

  private <T> T fromJson(String json, TypeReference<T> typeRef, URI uri) {
    try {
      return om.readValue(json, typeRef);
    } catch (IOException e) {
      throw new VotingApiException("Kann JSON nicht deserialisieren (uri=" + uri + ")", e);
    }
  }

  private static String abbreviate(String s, int maxLen) {
    if (s == null) return null;
    if (s.length() <= maxLen) return s;
    return s.substring(0, maxLen) + "…";
  }
}
