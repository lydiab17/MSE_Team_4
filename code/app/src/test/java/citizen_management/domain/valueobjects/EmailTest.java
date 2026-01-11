package citizen_management.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.evote.app.citizen_management.domain.valueobjects.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testklasse für den Email-Record.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@DisplayName("Email Value Objects Tests")
class EmailTest {

  // Happy-Path-Tests
  @Test
  @DisplayName("Sollte eine gültige E-Mail akzeptieren")
  void shouldNotThrowExceptionWhenEmailIsValid() {
    assertDoesNotThrow(() -> new Email("username@domain.com"));
  }

  // Edge-Cases

  // Negative Tests
  @Test
  @DisplayName("Sollte eine Exception werfen, wenn kein '@' enthalten ist")
  void shouldThrowExceptionWhenEmailHasNoAt() {
    assertThrows(IllegalArgumentException.class, () -> new Email("usernamedomain.com"));
  }

  @Test
  @DisplayName("Sollte Exception werfen, wenn E-Mail leer ist")
  void shouldThrowExceptionWhenEmailIsEmpty() {
    assertThrows(IllegalArgumentException.class, () -> new Email(""));
  }

  @Test
  @DisplayName("Sollte Exception werfen, wenn E-Mail null ist")
  void shouldThrowExceptionWhenEmailIsNull() {
    assertThrows(IllegalArgumentException.class, () -> new Email(null));
  }
}
