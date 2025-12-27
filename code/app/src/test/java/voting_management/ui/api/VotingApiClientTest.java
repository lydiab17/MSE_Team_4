package voting_management.ui.api;

import static org.junit.jupiter.api.Assertions.*;

import com.evote.app.votingmanagement.interfaces.dto.CreateVotingRequest;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.ui.api.VotingApiClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.*;

class VotingApiClientTest {

  private static HttpServer server;

  /** pro Test konfigurierbare Responses je Pfad */
  private static final Map<String, StubResponse> responses = new ConcurrentHashMap<>();

  /** letzter Request (zum Assert) */
  private static final AtomicReference<CapturedRequest> last = new AtomicReference<>();

  @BeforeAll
  static void startServer() throws Exception {
    // VotingApiClient hat fest localhost:8080 -> daher 8080
    server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);

    // Ein Catch-All Handler reicht, wir matchen nach Pfad
    server.createContext("/api/votings", VotingApiClientTest::handle);
    server.setExecutor(null);
    server.start();
  }

  @AfterAll
  static void stopServer() {
    if (server != null) server.stop(0);
  }

  @BeforeEach
  void reset() {
    responses.clear();
    last.set(null);
  }

  // ------------------------------------------------------------
  // Tests: Auth Header Handling
  // ------------------------------------------------------------

  @Test
  void getOpenVotings_withoutTokenSupplier_doesNotSendAuthorizationHeader() throws Exception {
    // Arrange
    responses.put("/api/votings/open", StubResponse.okJson("[]"));

    VotingApiClient client = new VotingApiClient(null);

    // Act
    client.getOpenVotings();

    // Assert
    CapturedRequest req = last.get();
    assertNotNull(req);
    assertEquals("GET", req.method);
    assertEquals("/api/votings/open", req.path);
    assertFalse(req.headers.containsKey("authorization"), "Authorization Header darf nicht gesetzt sein");
  }

  @Test
  void getById_withTokenSupplier_sendsBearerAuthorizationHeader() throws Exception {
    // Arrange
    responses.put("/api/votings/5", StubResponse.okJson(votingResponseJson(5, true)));

    Supplier<Optional<String>> tokenSupplier = () -> Optional.of("jwt-123");
    VotingApiClient client = new VotingApiClient(tokenSupplier);

    // Act
    VotingResponse vr = client.getById(5);

    // Assert
    assertEquals(5, vr.id());
    CapturedRequest req = last.get();
    assertEquals("GET", req.method);
    assertEquals("/api/votings/5", req.path);
    assertEquals("Bearer jwt-123", req.headers.get("authorization"));
  }

  @Test
  void tokenSupplier_returnsNull_optional_isHandledGracefully() throws Exception {
    // Arrange
    responses.put("/api/votings/open", StubResponse.okJson("[]"));

    Supplier<Optional<String>> tokenSupplier = () -> null; // bewusst
    VotingApiClient client = new VotingApiClient(tokenSupplier);

    // Act + Assert (darf nicht crashen)
    assertDoesNotThrow(client::getOpenVotings);

    CapturedRequest req = last.get();
    assertNotNull(req);
    assertFalse(req.headers.containsKey("authorization"));
  }

  // ------------------------------------------------------------
  // Tests: Pfade + Methoden
  // ------------------------------------------------------------

  @Test
  void openVoting_postsToCorrectEndpoint() throws Exception {
    // Arrange
    responses.put("/api/votings/7/open", StubResponse.noContent());

    VotingApiClient client = new VotingApiClient(() -> Optional.empty());

    // Act
    client.openVoting(7);

    // Assert
    CapturedRequest req = last.get();
    assertEquals("POST", req.method);
    assertEquals("/api/votings/7/open", req.path);
  }

  @Test
  void castVote_postsToVotesEndpoint_andSendsJsonBody() throws Exception {
    // Arrange
    responses.put("/api/votings/9/votes", StubResponse.noContent());

    VotingApiClient client = new VotingApiClient(() -> Optional.of("tkn"));

    // Act
    client.castVote(9, "Nein");

    // Assert
    CapturedRequest req = last.get();
    assertEquals("POST", req.method);
    assertEquals("/api/votings/9/votes", req.path);
    assertEquals("application/json", req.headers.get("content-type"));
    assertEquals("Bearer tkn", req.headers.get("authorization"));
    assertTrue(req.body.contains("Nein"), "Body sollte optionId enthalten");
  }

  // ------------------------------------------------------------
  // Tests: Error handling (status >= 300)
  // ------------------------------------------------------------

  @Test
  void getById_onHttpError_throwsIllegalState_withBody() {
    // Arrange
    responses.put("/api/votings/1", StubResponse.status(404, "not found"));

    VotingApiClient client = new VotingApiClient(() -> Optional.empty());

    // Act
    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> client.getById(1));
    assertEquals("not found", ex.getMessage());
  }

  @Test
  void openVoting_onHttpError_throwsIllegalState_withBody() {
    // Arrange
    responses.put("/api/votings/2/open", StubResponse.status(400, "bad request"));

    VotingApiClient client = new VotingApiClient(() -> Optional.empty());

    // Act
    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> client.openVoting(2));
    assertEquals("bad request", ex.getMessage());
  }

  @Test
  void castVote_onHttpError_throwsIllegalState_withHttpPrefix() {
    // Arrange
    responses.put("/api/votings/3/votes", StubResponse.status(500, "boom"));

    VotingApiClient client = new VotingApiClient(() -> Optional.empty());

    // Act
    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> client.castVote(3, "Ja"));
    assertTrue(ex.getMessage().startsWith("HTTP 500: "), "castVote nutzt spezielles Message-Format");
    assertTrue(ex.getMessage().contains("boom"));
  }

  // ------------------------------------------------------------
  // Tests: createVoting (optional, aber gut für Coverage)
  // ------------------------------------------------------------

  @Test
  void createVoting_success_returnsParsedVotingResponse() throws Exception {
    // Arrange
    responses.put("/api/votings", StubResponse.okJson(votingResponseJson(42, false)));

    VotingApiClient client = new VotingApiClient(() -> Optional.empty());

    // CreateVotingRequest ist bei dir vorhanden, aber Signatur kann variieren -> robust via reflection
    Object req = newCreateVotingRequestReflectively(
            42,
            "Abstimmung 2030",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            LocalDate.of(2030, 5, 10),
            LocalDate.of(2030, 5, 20),
            List.of("Ja", "Nein")
    );

    // Act
    VotingResponse created = client.createVoting((CreateVotingRequest) req);

    // Assert
    assertEquals(42, created.id());
    CapturedRequest r = last.get();
    assertEquals("POST", r.method);
    assertEquals("/api/votings", r.path);
    assertEquals("application/json", r.headers.get("content-type"));
    assertTrue(r.body.contains("Abstimmung 2030"));
  }

  // ============================================================
  // Server Handler + Helpers
  // ============================================================

  private static void handle(HttpExchange ex) throws IOException {
    String path = ex.getRequestURI().getPath(); // z.B. /api/votings/open
    String method = ex.getRequestMethod();

    String body = readAll(ex.getRequestBody());
    Map<String, String> headers = new HashMap<>();
    ex.getRequestHeaders().forEach((k, v) -> {
      if (!v.isEmpty()) headers.put(k.toLowerCase(Locale.ROOT), v.get(0));
    });

    last.set(new CapturedRequest(method, path, headers, body));

    StubResponse resp = responses.getOrDefault(path, StubResponse.status(404, "no stub for " + path));

    byte[] out = resp.body.getBytes(StandardCharsets.UTF_8);
    if (resp.contentType != null) {
      ex.getResponseHeaders().add("Content-Type", resp.contentType);
    }
    ex.sendResponseHeaders(resp.status, out.length);
    try (OutputStream os = ex.getResponseBody()) {
      os.write(out);
    }
  }

  private static String readAll(InputStream is) throws IOException {
    if (is == null) return "";
    byte[] bytes = is.readAllBytes();
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static String votingResponseJson(int id, boolean open) {
    // VotingResponse record:
    // id, name, info, startDate, endDate, open, options
    return "{"
            + "\"id\":" + id + ","
            + "\"name\":\"Voting " + id + "\","
            + "\"info\":\"Beschreibung Mit Mindestens Dreißig Zeichen Länge.\","
            + "\"startDate\":\"2030-01-01\","
            + "\"endDate\":\"2030-01-10\","
            + "\"open\":" + open + ","
            + "\"options\":[\"Ja\",\"Nein\"]"
            + "}";
  }

  // ============================================================
  // Reflection: CreateVotingRequest robust bauen
  // ============================================================

  private static Object newCreateVotingRequestReflectively(
          int id,
          String name,
          String info,
          LocalDate start,
          LocalDate end,
          List<String> options
  ) {
    try {
      Class<?> clazz = CreateVotingRequest.class;

      // Record?
      if (clazz.isRecord()) {
        var comps = clazz.getRecordComponents();
        Class<?>[] types = Arrays.stream(comps).map(rc -> rc.getType()).toArray(Class<?>[]::new);
        Object[] args = new Object[comps.length];

        for (int i = 0; i < comps.length; i++) {
          String n = comps[i].getName().toLowerCase(Locale.ROOT);
          Class<?> t = comps[i].getType();

          if (t == int.class || t == Integer.class) args[i] = id;
          else if (t == String.class) {
            if (n.contains("name")) args[i] = name;
            else if (n.contains("info") || n.contains("desc")) args[i] = info;
            else args[i] = name; // fallback
          } else if (t == LocalDate.class) {
            if (n.contains("start")) args[i] = start;
            else args[i] = end;
          } else if (List.class.isAssignableFrom(t)) {
            args[i] = options;
          } else if (Set.class.isAssignableFrom(t)) {
            args[i] = new LinkedHashSet<>(options);
          } else {
            args[i] = null;
          }
        }

        Constructor<?> ctor = clazz.getDeclaredConstructor(types);
        ctor.setAccessible(true);
        return ctor.newInstance(args);
      }

      // Non-record: suche passenden ctor (heuristisch)
      for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
        ctor.setAccessible(true);
        Class<?>[] p = ctor.getParameterTypes();
        if (p.length < 4) continue;

        Object[] args = new Object[p.length];
        int stringSeen = 0;
        int dateSeen = 0;

        boolean ok = true;
        for (int i = 0; i < p.length; i++) {
          if (p[i] == int.class || p[i] == Integer.class) args[i] = id;
          else if (p[i] == String.class) args[i] = (stringSeen++ == 0) ? name : info;
          else if (p[i] == LocalDate.class) args[i] = (dateSeen++ == 0) ? start : end;
          else if (List.class.isAssignableFrom(p[i])) args[i] = options;
          else if (Set.class.isAssignableFrom(p[i])) args[i] = new LinkedHashSet<>(options);
          else ok = false;
        }
        if (!ok) continue;

        return ctor.newInstance(args);
      }

      throw new IllegalStateException("Konnte CreateVotingRequest nicht konstruieren (unerwartete Signatur).");

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // ============================================================
  // Data holder
  // ============================================================

  private record CapturedRequest(String method, String path, Map<String, String> headers, String body) {}

  private static class StubResponse {
    final int status;
    final String body;
    final String contentType;

    private StubResponse(int status, String body, String contentType) {
      this.status = status;
      this.body = body;
      this.contentType = contentType;
    }

    static StubResponse okJson(String json) {
      return new StubResponse(200, json, "application/json");
    }

    static StubResponse noContent() {
      return new StubResponse(204, "", "text/plain");
    }

    static StubResponse status(int status, String body) {
      return new StubResponse(status, body, "text/plain");
    }
  }
}
