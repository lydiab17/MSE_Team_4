package voting_management.ui.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.evote.app.sharedkernel.security.AuthSession;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.ui.api.VotingApiClient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.evote.app.votingmanagement.ui.controller.CastVoteFxController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD) // wichtig: JavaFX + parallele Tests = flaky/Deadlocks
class CastVoteFxControllerTest {

  private StubVotingApiClient api;
  private AuthSession authSession;
  private CastVoteFxController controller;

  @BeforeAll
  static void initJavaFxOrSkip() {
    // Linux CI: häufig kein DISPLAY (X11) bzw. ggf. nur Wayland
    String display = System.getenv("DISPLAY");
    String wayland = System.getenv("WAYLAND_DISPLAY");

    assumeTrue(
            (display != null && !display.isBlank()) || (wayland != null && !wayland.isBlank()),
            "Skipping JavaFX tests: no DISPLAY/WAYLAND_DISPLAY available"
    );

    try {
      // Toolkit nur einmal starten
      Platform.startup(() -> { /* noop */ });
    } catch (IllegalStateException alreadyStarted) {
      // ok
    }

    // sorgt dafür, dass die FX-Plattform nicht wieder “ausgeht”
    Platform.setImplicitExit(false);
  }


  @AfterEach
  void cleanupWindows() throws Exception {
    // Nach jedem Test evtl. offene Alerts schließen, damit nichts blockiert
    runOnFxThreadAndWait(() -> {
      for (Window w : new ArrayList<>(Window.getWindows())) {
        if (w.isShowing()) w.hide();
      }
    });
  }

  @BeforeEach
  void setup() throws Exception {
    api = new StubVotingApiClient();
    authSession = new AuthSession();
    controller = new CastVoteFxController(api, authSession);

    runOnFxThreadAndWait(() -> {
      try {
        inject(controller, "openVotingsList", new ListView<VotingResponse>());
        inject(controller, "optionsList", new ListView<String>());

        inject(controller, "selectedVotingTitle", new Label());
        inject(controller, "selectedVotingDates", new Label());
        inject(controller, "selectedVotingInfo", new TextArea());

        inject(controller, "voterKeyField", new TextField());
        inject(controller, "statusLabel", new Label());

        @SuppressWarnings("unchecked")
        ListView<String> opts = (ListView<String>) getField(controller, "optionsList");
        opts.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  // ------------------------------------------------------------
  // initialize()
  // ------------------------------------------------------------

  @Test
  void initialize_hidesVoterKeyField_andRefreshesOpenVotings() throws Exception {
    VotingResponse v1 = new VotingResponse(
            1, "Voting 1", "Info text ist lang genug",
            LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 10),
            true, List.of("Ja", "Nein")
    );
    api.openVotingsToReturn = List.of(v1);

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    runOnFxThreadAndWait(() -> {
      TextField voterKey = (TextField) getField(controller, "voterKeyField");
      assertTrue(voterKey.isDisabled());
      assertFalse(voterKey.isManaged());
      assertFalse(voterKey.isVisible());
    });

    // initialize() triggert async refresh -> warten
    waitUntilFx(() -> {
      @SuppressWarnings("unchecked")
      ListView<VotingResponse> open = (ListView<VotingResponse>) getField(controller, "openVotingsList");
      Label status = (Label) getField(controller, "statusLabel");
      return open.getItems().size() == 1
              && status.getText().equals("Offene Abstimmungen geladen: 1");
    });

    assertEquals(1, api.getOpenVotingsCalls.get());
  }

  // ------------------------------------------------------------
  // onRefreshOpenVotings()
  // ------------------------------------------------------------

  @Test
  void onRefreshOpenVotings_setsListAndStatusLabel() throws Exception {
    VotingResponse v1 = new VotingResponse(
            11, "Offen A", "Info ...",
            LocalDate.of(2030, 2, 1), LocalDate.of(2030, 2, 5),
            true, List.of("A", "B")
    );
    VotingResponse v2 = new VotingResponse(
            12, "Offen B", "Info ...",
            LocalDate.of(2030, 2, 1), LocalDate.of(2030, 2, 5),
            true, List.of("A", "B")
    );
    api.openVotingsToReturn = List.of(v1, v2);

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onRefreshOpenVotings"));

    waitUntilFx(() -> {
      @SuppressWarnings("unchecked")
      ListView<VotingResponse> open = (ListView<VotingResponse>) getField(controller, "openVotingsList");
      Label status = (Label) getField(controller, "statusLabel");
      return open.getItems().size() == 2
              && status.getText().equals("Offene Abstimmungen geladen: 2");
    });

    assertEquals(1, api.getOpenVotingsCalls.get());
  }

  // ------------------------------------------------------------
  // Auswahl eines Votings -> loadVotingDetails()
  // ------------------------------------------------------------

  @Test
  void selectingVoting_loadsDetailsAndOptions() throws Exception {
    VotingResponse listItem = new VotingResponse(
            5, "ListItem", "Info ...",
            LocalDate.of(2030, 3, 1), LocalDate.of(2030, 3, 2),
            true, List.of("X")
    );

    VotingResponse details = new VotingResponse(
            5, "DetailName", "DetailInfo",
            LocalDate.of(2030, 3, 10), LocalDate.of(2030, 3, 20),
            true, List.of("Ja", "Nein", "Enthaltung")
    );

    api.openVotingsToReturn = List.of(listItem);
    api.byIdToReturn = details;

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    waitUntilFx(() -> {
      @SuppressWarnings("unchecked")
      ListView<VotingResponse> open = (ListView<VotingResponse>) getField(controller, "openVotingsList");
      return open.getItems().size() == 1;
    });

    // Auswahl -> Controller sollte Details nachladen
    runOnFxThreadAndWait(() -> {
      @SuppressWarnings("unchecked")
      ListView<VotingResponse> open = (ListView<VotingResponse>) getField(controller, "openVotingsList");
      open.getSelectionModel().select(0);
    });

    waitUntilFx(() -> {
      Label title = (Label) getField(controller, "selectedVotingTitle");
      Label dates = (Label) getField(controller, "selectedVotingDates");
      TextArea info = (TextArea) getField(controller, "selectedVotingInfo");

      @SuppressWarnings("unchecked")
      ListView<String> opts = (ListView<String>) getField(controller, "optionsList");
      Label status = (Label) getField(controller, "statusLabel");

      return "DetailName".equals(title.getText())
              && dates.getText().contains("2030-03-10 bis 2030-03-20")
              && "DetailInfo".equals(info.getText())
              && opts.getItems().equals(FXCollections.observableArrayList("Ja", "Nein", "Enthaltung"))
              && "Voting geladen: ID 5".equals(status.getText());
    });

    assertEquals(1, api.getByIdCalls.get());
  }

  // ------------------------------------------------------------
  // onCastVote()
  // ------------------------------------------------------------

  @Test
  void onCastVote_whenNotLoggedIn_doesNotCallApi() throws Exception {
    // AuthSession hat kein Token

    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onCastVote"));
    } finally {
      closer.stop();
    }

    assertEquals(0, api.castVoteCalls.get());
  }

  @Test
  void onCastVote_whenNoVotingSelected_doesNotCallApi() throws Exception {
    authSession.setToken("jwt");

    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onCastVote"));
    } finally {
      closer.stop();
    }

    assertEquals(0, api.castVoteCalls.get());
  }

  @Test
  void onCastVote_whenNoOptionSelected_doesNotCallApi() throws Exception {
    authSession.setToken("jwt");

    VotingResponse selected = new VotingResponse(
            77, "Sel", "Info",
            LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2),
            true, List.of("Ja", "Nein")
    );
    setField(controller, "selectedVoting", selected);

    runOnFxThreadAndWait(() -> {
      @SuppressWarnings("unchecked")
      ListView<String> opts = (ListView<String>) getField(controller, "optionsList");
      opts.getItems().setAll("Ja", "Nein");
      opts.getSelectionModel().clearSelection();
    });

    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onCastVote"));
    } finally {
      closer.stop();
    }

    assertEquals(0, api.castVoteCalls.get());
  }

  @Test
  void onCastVote_success_callsApi_setsStatus() throws Exception {
    authSession.setToken("jwt");

    VotingResponse selected = new VotingResponse(
            88, "Sel", "Info",
            LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2),
            true, List.of("Ja", "Nein")
    );
    setField(controller, "selectedVoting", selected);

    runOnFxThreadAndWait(() -> {
      @SuppressWarnings("unchecked")
      ListView<String> opts = (ListView<String>) getField(controller, "optionsList");
      opts.getItems().setAll("Ja", "Nein");
      opts.getSelectionModel().select("Nein");
    });

    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onCastVote"));

      waitUntilFx(() -> {
        Label status = (Label) getField(controller, "statusLabel");
        return "Stimme wurde abgegeben ✅".equals(status.getText());
      });
    } finally {
      closer.stop();
    }

    assertEquals(1, api.castVoteCalls.get());
    assertEquals(88, api.lastCastVotingId.get());
    assertEquals("Nein", api.lastCastOption.get());
  }

  // ============================================================
  // Stub API Client (VotingApiClient ist eine echte Klasse)
  // ============================================================

  static class StubVotingApiClient extends VotingApiClient {

    volatile List<VotingResponse> openVotingsToReturn = List.of();
    volatile VotingResponse byIdToReturn;

    AtomicInteger getOpenVotingsCalls = new AtomicInteger();
    AtomicInteger getByIdCalls = new AtomicInteger();
    AtomicInteger castVoteCalls = new AtomicInteger();

    AtomicInteger lastCastVotingId = new AtomicInteger(-1);
    AtomicReference<String> lastCastOption = new AtomicReference<>(null);

    StubVotingApiClient() {
      // ctor erwartet Supplier<Optional<String>> (session::token in Config)
      super(() -> Optional.empty());
    }

    @Override
    public List<VotingResponse> getOpenVotings() {
      getOpenVotingsCalls.incrementAndGet();
      return openVotingsToReturn;
    }

    @Override
    public VotingResponse getById(int id) {
      getByIdCalls.incrementAndGet();
      return byIdToReturn;
    }

    @Override
    public void castVote(int votingId, String optionId) {
      castVoteCalls.incrementAndGet();
      lastCastVotingId.set(votingId);
      lastCastOption.set(optionId);
    }
  }

  /**
   * Schließt alle offenen JavaFX Windows regelmäßig, damit Alert.showAndWait() nicht blockiert.
   */
  static class AutoCloseAlerts {
    private volatile boolean running = true;
    private final Thread t;

    private AutoCloseAlerts() {
      t = new Thread(() -> {
        while (running) {
          try {
            Thread.sleep(50);
          } catch (InterruptedException ignored) {
          }
          Platform.runLater(() -> {
            for (Window w : new ArrayList<>(Window.getWindows())) {
              if (w.isShowing()) w.hide();
            }
          });
        }
      }, "auto-close-alerts");
      t.setDaemon(true);
      t.start();
    }

    static AutoCloseAlerts start() {
      return new AutoCloseAlerts();
    }

    void stop() {
      running = false;
    }
  }

  // ============================================================
  // Reflection helpers
  // ============================================================

  private static void invokeVoidNoArgs(Object target, String method) {
    try {
      Method m = target.getClass().getDeclaredMethod(method);
      m.setAccessible(true);
      try {
        m.invoke(target);
      } catch (java.lang.reflect.InvocationTargetException ite) {
        Throwable cause = ite.getCause();
        if (cause instanceof RuntimeException re) throw re;
        if (cause instanceof Error err) throw err;
        throw ite;
      }
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // ============================================================
  // JavaFX helpers
  // ============================================================

  private static void runOnFxThreadAndWait(Runnable r) throws Exception {
    if (Platform.isFxApplicationThread()) {
      r.run();
      return;
    }
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> error = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        r.run();
      } catch (Throwable t) {
        error.set(t);
      } finally {
        latch.countDown();
      }
    });

    // mehr Zeit + bessere Message
    boolean ok = latch.await(15, TimeUnit.SECONDS);
    assertTrue(ok, "FX runLater timeout (Toolkit evtl. beendet). Stelle sicher: Platform.setImplicitExit(false)");
    if (error.get() != null) throw new RuntimeException(error.get());
  }

  private static void waitUntilFx(BooleanSupplier condition) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    while (System.nanoTime() < deadline) {
      AtomicReference<Boolean> ok = new AtomicReference<>(false);
      runOnFxThreadAndWait(() -> ok.set(condition.getAsBoolean()));
      if (ok.get()) return;
      Thread.sleep(30);
    }
    fail("Timeout waiting for FX condition");
  }

  @FunctionalInterface
  interface BooleanSupplier {
    boolean getAsBoolean();
  }

  // ============================================================
  // Field injection helpers
  // ============================================================

  private static void inject(Object target, String fieldName, Object value) throws Exception {
    Field f = getDeclaredField(target.getClass(), fieldName);
    f.setAccessible(true);
    f.set(target, value);
  }

  private static Object getField(Object target, String fieldName) {
    try {
      Field f = getDeclaredField(target.getClass(), fieldName);
      f.setAccessible(true);
      return f.get(target);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void setField(Object target, String fieldName, Object value) {
    try {
      Field f = getDeclaredField(target.getClass(), fieldName);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Field getDeclaredField(Class<?> c, String name) throws Exception {
    for (Class<?> cur = c; cur != null && cur != Object.class; cur = cur.getSuperclass()) {
      try {
        return cur.getDeclaredField(name);
      } catch (NoSuchFieldException ignored) {
      }
    }
    throw new NoSuchFieldException(name);
  }
}
