package citizen_management.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.evote.app.citizen_management.infrastructure.CitizenApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = com.evote.app.EvoteSpringConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CitizenApiClientTest {

  @Autowired CitizenApiClient client;

  // Happy Path Test
  @Test
  void register_and_login_work() {
    String email = "test@example.com";
    String pw = "Secret123";

    boolean registered = client.registerCitizen("Max", "Mustermann", email, pw);
    assertTrue(registered);

    var token = client.loginAndGetToken(email, pw);
    assertTrue(token.isPresent());
    assertFalse(token.get().isBlank());
  }

  // Negativer Test
  @Test
  void login_fails_with_wrong_password() {
    String email = "test2@example.com";
    String pw = "Secret123";

    assertTrue(client.registerCitizen("Max", "Mustermann", email, pw));

    assertTrue(client.loginAndGetToken(email, "wrong").isEmpty());
  }
}
