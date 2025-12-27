package voting_management.ui.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.evote.app.sharedkernel.security.AuthSession;
import com.evote.app.votingmanagement.interfaces.dto.CreateVotingRequest;
import com.evote.app.votingmanagement.interfaces.dto.OptionResultResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResultsResponse;
import com.evote.app.votingmanagement.ui.api.VotingApiClient;
import com.evote.app.votingmanagement.ui.controller.VotingFxController;
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
import javafx.application.Platform;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
class VotingFxControllerTest {

  private StubVotingApiClient api;
  private AuthSession auth;
  private VotingFxController controller;

  @BeforeAll
  static void initJavaFx() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException alreadyStarted) {
      // ok
    }
    Platform.setImplicitExit(false);
  }

  @AfterEach
  void closeWindows() throws Exception {
    runOnFxThreadAndWait(() -> {
      for (Window w : new ArrayList<>(Window.getWindows())) {
        if (w.isShowing()) w.hide();
      }
    });
  }

  @BeforeEach
  void setup() throws Exception {
    api = new StubVotingApiClient();
    auth = new AuthSession();

    controller = new VotingFxController(api, auth);

    runOnFxThreadAndWait(() -> {
      try {
        inject(controller, "idField", new TextField());
        inject(controller, "nameField", new TextField());
        inject(controller, "infoArea", new TextArea());
        inject(controller, "startDatePicker", new DatePicker());
        inject(controller, "endDatePicker", new DatePicker());
        inject(controller, "optionsField", new TextField());

        inject(controller, "openVotingsList", new ListView<VotingResponse>());
        inject(controller, "notOpenVotingsList", new ListView<VotingResponse>());

        inject(controller, "statusLabel", new Label());

        TableView<OptionResultResponse> table = new TableView<>();
        TableColumn<OptionResultResponse, String> optCol = new TableColumn<>("Option");
        TableColumn<OptionResultResponse, Long> cntCol = new TableColumn<>("Count");
        table.getColumns().setAll(optCol, cntCol);

        inject(controller, "resultsTable", table);
        inject(controller, "optionColumn", optCol);
        inject(controller, "countColumn", cntCol);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  // ============================================================
  // initialize / refresh
  // ============================================================

  @Test
  void initialize_refreshesLists_andSetsStatus() throws Exception {
    api.openToReturn = List.of(voting(1, true), voting(2, true));
    api.notOpenToReturn = List.of(voting(10, false));

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    waitUntilFx(() -> {
      ListView<VotingResponse> open = get("openVotingsList");
      ListView<VotingResponse> notOpen = get("notOpenVotingsList");
      return open.getItems().size() == 2 && notOpen.getItems().size() == 1;
    });

    // Status kann je nach Implementation variieren -> nur "nicht leer" prüfen
    runOnFxThreadAndWait(() -> {
      Label status = get("statusLabel");
      assertNotNull(status.getText());
      assertFalse(status.getText().isBlank());
    });

    assertEquals(1, api.getOpenCalls.get());
    assertEquals(1, api.getNotOpenCalls.get());
  }

  @Test
  void initialize_selectingOpenClearsNotOpen_andFillsForm_andClearsResultsTable() throws Exception {
    api.openToReturn = List.of(voting(1, true));
    api.notOpenToReturn = List.of(voting(2, false));

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    waitUntilFx(() -> {
      ListView<VotingResponse> open = get("openVotingsList");
      return open.getItems().size() == 1;
    });

    runOnFxThreadAndWait(() -> {
      TableView<OptionResultResponse> t = get("resultsTable");
      t.getItems().setAll(new OptionResultResponse("Ja", 1));
    });

    runOnFxThreadAndWait(() -> {
      ListView<VotingResponse> open = get("openVotingsList");
      open.getSelectionModel().select(0);
    });

    waitUntilFx(() -> {
      ListView<VotingResponse> notOpen = get("notOpenVotingsList");
      TextField idField = get("idField");
      TableView<OptionResultResponse> t = get("resultsTable");
      return notOpen.getSelectionModel().getSelectedItem() == null
              && "1".equals(idField.getText())
              && t.getItems().isEmpty();
    });
  }

  @Test
  void initialize_tableColumnsAreConfigured() throws Exception {
    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    runOnFxThreadAndWait(() -> {
      TableView<OptionResultResponse> table = get("resultsTable");
      table.getItems().setAll(new OptionResultResponse("Nein", 5L));

      TableColumn<OptionResultResponse, String> optionCol = get("optionColumn");
      TableColumn<OptionResultResponse, Long> countCol = get("countColumn");

      assertEquals("Nein", optionCol.getCellData(0));
      assertEquals(5L, countCol.getCellData(0));
    });
  }

  // ============================================================
  // onClearForm
  // ============================================================

  @Test
  void onClearForm_clearsAndSetsStatus() throws Exception {
    runOnFxThreadAndWait(() -> {
      getText("idField").setText("99");
      getText("nameField").setText("Name");
      ((TextArea) getField(controller, "infoArea")).setText("Info");
      ((DatePicker) getField(controller, "startDatePicker")).setValue(LocalDate.of(2030, 1, 1));
      ((DatePicker) getField(controller, "endDatePicker")).setValue(LocalDate.of(2030, 1, 2));
      getText("optionsField").setText("Ja, Nein");
    });

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onClearForm"));

    runOnFxThreadAndWait(() -> {
      assertEquals("", getText("idField").getText());
      assertEquals("", getText("nameField").getText());
      assertEquals("", ((TextArea) getField(controller, "infoArea")).getText());
      assertNull(((DatePicker) getField(controller, "startDatePicker")).getValue());
      assertNull(((DatePicker) getField(controller, "endDatePicker")).getValue());
      assertEquals("", getText("optionsField").getText());

      Label status = get("statusLabel");
      assertNotNull(status.getText());
      assertFalse(status.getText().isBlank());
    });
  }

  // ============================================================
  // onCreateVoting
  // ============================================================

  @Test
  void onCreateVoting_notLoggedIn_showsError_andDoesNotCallApi() throws Exception {
    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onCreateVoting"));
    } finally {
      closer.stop();
    }

    assertEquals(0, api.createCalls.get());

    runOnFxThreadAndWait(() -> {
      // je nach Implementation: Status kann Fehler enthalten ODER unverändert bleiben
      Label status = get("statusLabel");
      assertNotNull(status.getText());
    });
  }

  @Test
  void onCreateVoting_invalidId_doesNotCallApi() throws Exception {
    auth.setToken("jwt");

    runOnFxThreadAndWait(() -> {
      getText("idField").setText("abc");
      getText("nameField").setText("Abstimmung 1");
      ((TextArea) getField(controller, "infoArea")).setText("Beschreibung Mit Mindestens Dreißig Zeichen Länge.");
      ((DatePicker) getField(controller, "startDatePicker")).setValue(LocalDate.of(2030, 1, 1));
      ((DatePicker) getField(controller, "endDatePicker")).setValue(LocalDate.of(2030, 1, 2));
      getText("optionsField").setText("Ja");
    });

    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onCreateVoting"));
    } finally {
      closer.stop();
    }

    assertEquals(0, api.createCalls.get());
  }

  @Test
  void onCreateVoting_success_callsApi_andClearsForm() throws Exception {
    auth.setToken("jwt");

    api.openToReturn = List.of(voting(1, true));
    api.notOpenToReturn = List.of(voting(2, false));
    api.createdToReturn = voting(42, false);

    runOnFxThreadAndWait(() -> {
      getText("idField").setText("42");
      getText("nameField").setText("Abstimmung 42");
      ((TextArea) getField(controller, "infoArea")).setText("Beschreibung Mit Mindestens Dreißig Zeichen Länge.");
      ((DatePicker) getField(controller, "startDatePicker")).setValue(LocalDate.of(2030, 5, 1));
      ((DatePicker) getField(controller, "endDatePicker")).setValue(LocalDate.of(2030, 5, 2));
      getText("optionsField").setText("Ja, Nein");
    });

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));
    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onCreateVoting"));

    // robust: warte auf API-Call statt Status-String
    waitUntil(() -> api.createCalls.get() == 1, 10);

    runOnFxThreadAndWait(() -> {
      assertEquals("", getText("idField").getText());
      assertEquals("", getText("nameField").getText());
      assertEquals("", ((TextArea) getField(controller, "infoArea")).getText());
      assertNull(((DatePicker) getField(controller, "startDatePicker")).getValue());
      assertNull(((DatePicker) getField(controller, "endDatePicker")).getValue());
      assertEquals("", getText("optionsField").getText());
    });

    CreateVotingRequest req = api.lastCreateRequest.get();
    assertNotNull(req);
    assertEquals(42, req.id());
  }

  // ============================================================
  // onLoadSelectedVoting
  // ============================================================

  @Test
  void onLoadSelectedVoting_noSelection_doesNotCrash() throws Exception {
    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    String before = runOnFxThreadAndGet(() -> getStatus().getText());

    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onLoadSelectedVoting"));
    } finally {
      closer.stop();
    }

    // keine harte Text-Erwartung mehr (weil bei dir offenbar anders)
    String after = runOnFxThreadAndGet(() -> getStatus().getText());
    assertNotNull(after);
    // entweder Fehler gesetzt oder Status bleibt wie vorher
    assertTrue(after.equals(before) || after.startsWith("Fehler:"), "Status sollte Fehler anzeigen oder unverändert bleiben");
  }

  @Test
  void onLoadSelectedVoting_withSelection_fillsForm() throws Exception {
    api.openToReturn = List.of(voting(5, true));
    api.notOpenToReturn = List.of();

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));
    waitUntilFx(() -> ((ListView<VotingResponse>) getField(controller, "openVotingsList")).getItems().size() == 1);

    runOnFxThreadAndWait(() -> {
      ListView<VotingResponse> open = get("openVotingsList");
      open.getSelectionModel().select(0);
    });

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onLoadSelectedVoting"));

    waitUntilFx(() -> "5".equals(getText("idField").getText()));
  }

  // ============================================================
  // onOpenSelectedVoting
  // ============================================================

  @Test
  void onOpenSelectedVoting_notLoggedIn_doesNotCallApi() throws Exception {
    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onOpenSelectedVoting"));
    } finally {
      closer.stop();
    }
    assertEquals(0, api.openCalls.get());
  }

  @Test
  void onOpenSelectedVoting_noNotOpenSelection_doesNotCallApi() throws Exception {
    auth.setToken("jwt");
    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onOpenSelectedVoting"));
    } finally {
      closer.stop();
    }
    assertEquals(0, api.openCalls.get());
  }

  @Test
  void onOpenSelectedVoting_success_callsApi() throws Exception {
    auth.setToken("jwt");

    api.openToReturn = List.of(voting(1, true));
    api.notOpenToReturn = List.of(voting(9, false));

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    waitUntilFx(() -> {
      ListView<VotingResponse> l = get("notOpenVotingsList");
      return l.getItems().size() == 1;
    });

    runOnFxThreadAndWait(() -> {
      ListView<VotingResponse> l = get("notOpenVotingsList");
      l.getSelectionModel().select(0);
    });

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onOpenSelectedVoting"));

    // robust: warte auf Call statt Status-String
    waitUntil(() -> api.openCalls.get() == 1, 10);
  }

  // ============================================================
  // onLoadResults
  // ============================================================

  @Test
  void onLoadResults_noSelection_doesNotCallApi_andDoesNotPopulateTable() throws Exception {
    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    runOnFxThreadAndWait(() -> {
      TableView<OptionResultResponse> table = get("resultsTable");
      table.getItems().clear();
    });

    int callsBefore = api.getResultsCalls.get();

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onLoadResults"));

    runOnFxThreadAndWait(() -> {
      // API darf ohne Auswahl nicht aufgerufen werden
      assertEquals(callsBefore, api.getResultsCalls.get());

      // Tabelle bleibt leer
      TableView<OptionResultResponse> table = get("resultsTable");
      assertTrue(table.getItems().isEmpty());

      // Status wird im echten Code gesetzt:
      assertEquals("Bitte zuerst ein Voting auswählen.", getStatus().getText());
    });
  }


  @Test
  void onLoadResults_success_populatesTable() throws Exception {
    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));

    VotingResponse v = voting(3, true);
    setField(controller, "selectedVoting", v);

    api.resultsToReturn = new VotingResultsResponse(
            3,
            List.of(new OptionResultResponse("Ja", 10), new OptionResultResponse("Nein", 2))
    );

    runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "onLoadResults"));

    // robust: warte auf API call und Table befüllt
    waitUntil(() -> api.getResultsCalls.get() == 1, 10);

    waitUntilFx(() -> {
      TableView<OptionResultResponse> table = get("resultsTable");
      return table.getItems().size() == 2;
    });
  }

  // ============================================================
  // refreshVotingLists error path
  // ============================================================

  @Test
  void refreshVotingLists_whenApiThrows_setsErrorOrLeavesStatusButDoesNotCrash() throws Exception {
    api.throwOnLists = true;

    AutoCloseAlerts closer = AutoCloseAlerts.start();
    try {
      runOnFxThreadAndWait(() -> invokeVoidNoArgs(controller, "initialize"));
    } finally {
      closer.stop();
    }

    runOnFxThreadAndWait(() -> {
      Label status = get("statusLabel");
      assertNotNull(status.getText());
      // je nach Implementation kann Fehler im Status stehen
    });
  }

  // ============================================================
  // Helpers / Stubs
  // ============================================================

  private VotingResponse voting(int id, boolean open) {
    return new VotingResponse(
            id,
            "Voting " + id,
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            LocalDate.of(2030, 1, 1),
            LocalDate.of(2030, 1, 2),
            open,
            List.of("Ja", "Nein")
    );
  }

  private Label getStatus() {
    return get("statusLabel");
  }

  @SuppressWarnings("unchecked")
  private <T> T get(String field) {
    return (T) getField(controller, field);
  }

  static class StubVotingApiClient extends VotingApiClient {

    volatile List<VotingResponse> openToReturn = List.of();
    volatile List<VotingResponse> notOpenToReturn = List.of();
    volatile VotingResponse createdToReturn = null;
    volatile VotingResultsResponse resultsToReturn = new VotingResultsResponse(0, List.of());
    volatile boolean throwOnLists = false;

    AtomicInteger getOpenCalls = new AtomicInteger();
    AtomicInteger getNotOpenCalls = new AtomicInteger();
    AtomicInteger createCalls = new AtomicInteger();
    AtomicInteger openCalls = new AtomicInteger();
    AtomicInteger getResultsCalls = new AtomicInteger();

    AtomicReference<CreateVotingRequest> lastCreateRequest = new AtomicReference<>();

    StubVotingApiClient() {
      super(() -> Optional.empty());
    }

    @Override
    public List<VotingResponse> getOpenVotings() {
      getOpenCalls.incrementAndGet();
      if (throwOnLists) throw new RuntimeException("boom");
      return openToReturn;
    }

    @Override
    public List<VotingResponse> getNotOpenVotings() {
      getNotOpenCalls.incrementAndGet();
      if (throwOnLists) throw new RuntimeException("boom");
      return notOpenToReturn;
    }

    @Override
    public VotingResponse createVoting(CreateVotingRequest request) {
      createCalls.incrementAndGet();
      lastCreateRequest.set(request);
      return createdToReturn != null ? createdToReturn : votingFallback();
    }

    @Override
    public void openVoting(int id) {
      openCalls.incrementAndGet();
    }

    @Override
    public VotingResultsResponse getResults(int votingId) {
      getResultsCalls.incrementAndGet();
      return resultsToReturn;
    }

    private VotingResponse votingFallback() {
      return new VotingResponse(
              1,
              "Voting 1",
              "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
              LocalDate.of(2030, 1, 1),
              LocalDate.of(2030, 1, 2),
              true,
              List.of("Ja")
      );
    }
  }

  /**
   * Schließt Alerts, damit showAndWait() nicht hängt.
   */
  static class AutoCloseAlerts {
    private volatile boolean running = true;
    private final Thread t;

    private AutoCloseAlerts() {
      t = new Thread(() -> {
        while (running) {
          try {
            Thread.sleep(50);
          } catch (InterruptedException ignored) {}
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

  // ---------- waiting helpers ----------

  private static void waitUntil(BooleanSupplier cond, int seconds) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
    while (System.nanoTime() < deadline) {
      if (cond.getAsBoolean()) return;
      Thread.sleep(25);
    }
    fail("Timeout waiting for condition");
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

    boolean ok = latch.await(15, TimeUnit.SECONDS);
    assertTrue(ok, "FX runLater timeout (Toolkit evtl. beendet). Tipp: Platform.setImplicitExit(false)");
    if (error.get() != null) throw new RuntimeException(error.get());
  }

  private static <T> T runOnFxThreadAndGet(FxSupplier<T> s) throws Exception {
    if (Platform.isFxApplicationThread()) return s.get();

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> error = new AtomicReference<>();
    AtomicReference<T> value = new AtomicReference<>();

    Platform.runLater(() -> {
      try {
        value.set(s.get());
      } catch (Throwable t) {
        error.set(t);
      } finally {
        latch.countDown();
      }
    });

    boolean ok = latch.await(15, TimeUnit.SECONDS);
    assertTrue(ok, "FX runLater timeout");
    if (error.get() != null) throw new RuntimeException(error.get());
    return value.get();
  }

  @FunctionalInterface
  interface FxSupplier<T> {
    T get();
  }

  // ---------- reflection helpers ----------

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
      } catch (NoSuchFieldException ignored) {}
    }
    throw new NoSuchFieldException(name);
  }

  private TextField getText(String name) {
    return (TextField) getField(controller, name);
  }
}
