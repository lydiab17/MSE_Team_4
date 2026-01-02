import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import com.evote.app.HelloApplication;
import javafx.application.Platform;
import javafx.scene.Parent;
import org.junit.jupiter.api.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.junit.jupiter.api.Tag;

@Tag("javafx")
class HelloApplicationTest {

  private static boolean javafxStarted = false;

  @BeforeAll
  static void startJavaFxToolkitOnce() {
    // JavaFX Toolkit muss einmal initialisiert werden, sonst schlägt FXMLLoader.load oft fehl.
    if (!javafxStarted) {
      try {
        Platform.startup(() -> { });
      } catch (IllegalStateException alreadyStarted) {
        // Toolkit läuft schon (z.B. in anderer Testklasse)
      }
      javafxStarted = true;
    }
  }

  @AfterEach
  void cleanupSpringContextStatic() throws Exception {
    setSpringContext(null);
  }

  @Test
  @DisplayName("loadFXML: lädt FXML und holt Controller über Spring ControllerFactory")
  void loadFXML_usesSpringControllerFactory() throws Exception {
    // Arrange
    ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
    when(ctx.getBean(TestController.class)).thenReturn(new TestController());
    setSpringContext(ctx);

    // Act
    Parent root = HelloApplication.loadFXML("fxml/test.fxml");

    // Assert
    assertNotNull(root);
    verify(ctx, times(1)).getBean(TestController.class);
  }

  @Test
  @DisplayName("loadFXML: fehlende Resource")
  void loadFXML_missingResource_throws() throws Exception {
    ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
    setSpringContext(ctx);

    // Hinweis: Bei fehlender Resource ist getResource(...) == null.
    // Deine Methode fängt nur IOException, daher fliegt hier aktuell eine NPE.
    assertThrows(IllegalStateException.class,
            () -> HelloApplication.loadFXML("fxml/does-not-exist.fxml"));
  }

  @Test
  @DisplayName("stop: schließt den Spring-Context wenn vorhanden")
  void stop_closesSpringContext() throws Exception {
    ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
    setSpringContext(ctx);

    HelloApplication app = new HelloApplication();
    app.stop();

    verify(ctx, times(1)).close();
  }

  // ----------------- Helper: setze private static springContext per Reflection -----------------

  private static void setSpringContext(ConfigurableApplicationContext ctx) throws Exception {
    Field f = HelloApplication.class.getDeclaredField("springContext");
    f.setAccessible(true);
    f.set(null, ctx); // static field -> instance = null
  }
}
