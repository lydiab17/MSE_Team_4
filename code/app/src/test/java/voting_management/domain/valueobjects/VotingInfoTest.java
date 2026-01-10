package voting_management.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import com.evote.app.votingmanagement.domain.valueobjects.VotingInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VotingInfoTest {

  // ---------- Happy Path ----------

  @Test
  @DisplayName("Gültige Info: Mindestlänge und Großbuchstabe am Anfang")
  void valid_minLengthAndCapitalStart() {
    // 30 Zeichen, beginnt mit Großbuchstaben
    String text = "AbcdefghijAbcdefghijAbcdefghij"; // 30 Zeichen
    assertEquals(30, text.trim().length());

    VotingInfo info = new VotingInfo(text);

    assertEquals(text, info.getValue());
  }

  @Test
  @DisplayName("Gültige Info: Mehrzeiliger Text mit Zeilenumbruch ist erlaubt")
  void valid_multilineAllowed() {
    String text = "Beschreibung Mit Mehreren Zeilen,\nund noch ein bisschen mehr Text.";
    assertTrue(text.trim().length() >= 30);

    VotingInfo info = new VotingInfo(text);

    assertEquals(text, info.getValue());
  }

  @Test
  @DisplayName("Gültige Info: führende/trailing Spaces werden beim Speichern getrimmt")
  void valid_withLeadingAndTrailingSpaces() {
    String raw = "   Beschreibung mit genug Länge und Großbuchstaben.   ";
    VotingInfo info = new VotingInfo(raw);

    // Neu: getValue() gibt den getrimmten String zurück
    assertEquals(raw.trim(), info.getValue());
  }

  // ---------- Negative: null / leer / nur Spaces ----------

  @Test
  @DisplayName("Null-Wert: wirft NullPointerException")
  void nullValue_throwsException() {
    assertThrows(NullPointerException.class, () -> new VotingInfo(null));
  }

  @ParameterizedTest(name = "Ungültige leere Info: \"{0}\"")
  @ValueSource(strings = {"", "   "})
  @DisplayName("Leere und nur aus Leerzeichen bestehende Strings sind nicht erlaubt")
  void emptyOrBlank_throwsException(String raw) {
    assertThrows(IllegalArgumentException.class, () -> new VotingInfo(raw));
  }

  // ---------- Negative: Länge ----------

  @Test
  @DisplayName("Zu kurz: weniger als 30 Zeichen → Exception")
  void tooShort_throwsException() {
    String text = "Zu kurz für Info";
    assertTrue(text.trim().length() < 30);

    assertThrows(IllegalArgumentException.class, () -> new VotingInfo(text));
  }

  @Test
  @DisplayName("Zu lang: mehr als 1000 Zeichen → Exception")
  void tooLong_throwsException() {
    StringBuilder sb = new StringBuilder();
    sb.append('L');
    for (int i = 0; i < 1000; i++) {
      sb.append('x');
    }
    String text = sb.toString();
    assertTrue(text.trim().length() > 1000);

    assertThrows(IllegalArgumentException.class, () -> new VotingInfo(text));
  }

  // ---------- Negative: erster Buchstabe klein ----------

  @Test
  @DisplayName("Erster Buchstabe klein (nach Trim) → Exception")
  void firstCharLowercase_throwsException() {
    String text = "beschreibung beginnt klein aber ist lang genug........................";
    assertTrue(text.trim().length() >= 30);

    assertThrows(IllegalArgumentException.class, () -> new VotingInfo(text));
  }

  @Test
  @DisplayName("Erster sichtbarer Buchstabe nach Spaces ist klein → Exception")
  void firstCharAfterSpacesLowercase_throwsException() {
    String text =
        "   beschreibung mit führenden Spaces und kleinem Anfangsbuchstaben........................";
    assertTrue(text.trim().length() >= 30);

    assertThrows(IllegalArgumentException.class, () -> new VotingInfo(text));
  }
}
