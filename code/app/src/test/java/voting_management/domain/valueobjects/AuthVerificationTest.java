package voting_management.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.evote.app.votingmanagement.domain.valueobjects.AuthVerification;
import org.junit.jupiter.api.Test;

class AuthVerificationTest {

  @Test
  void verificationStatus_constructor_setsFields_correctly() throws Exception {
    AuthVerification.VerificationStatus status =
            new AuthVerification.VerificationStatus(true, "pseudonym-1");

    Field verifiedField = AuthVerification.VerificationStatus.class.getDeclaredField("verified");
    verifiedField.setAccessible(true);

    Field pseudonymField = AuthVerification.VerificationStatus.class.getDeclaredField("pseudonym");
    pseudonymField.setAccessible(true);

    assertEquals(true, verifiedField.getBoolean(status));
    assertEquals("pseudonym-1", pseudonymField.get(status));
  }

  @Test
  void verificationStatus_allowsNullPseudonym_whenNotVerified() throws Exception {
    AuthVerification.VerificationStatus status =
            new AuthVerification.VerificationStatus(false, null);

    Field verifiedField = AuthVerification.VerificationStatus.class.getDeclaredField("verified");
    verifiedField.setAccessible(true);

    Field pseudonymField = AuthVerification.VerificationStatus.class.getDeclaredField("pseudonym");
    pseudonymField.setAccessible(true);

    assertFalse(verifiedField.getBoolean(status));
    assertNull(pseudonymField.get(status));
  }

  @Test
  void verificationStatus_fieldsAreFinal_immutable() throws Exception {
    Field verifiedField = AuthVerification.VerificationStatus.class.getDeclaredField("verified");
    Field pseudonymField = AuthVerification.VerificationStatus.class.getDeclaredField("pseudonym");

    assertTrue(Modifier.isFinal(verifiedField.getModifiers()), "verified sollte final sein");
    assertTrue(Modifier.isFinal(pseudonymField.getModifiers()), "pseudonym sollte final sein");
  }

  @Test
  void authVerification_hasPrivateConstructor_utilityClass() throws Exception {
    Constructor<AuthVerification> ctor = AuthVerification.class.getDeclaredConstructor();

    assertTrue(Modifier.isPrivate(ctor.getModifiers()), "AuthVerification Konstruktor sollte private sein");

    ctor.setAccessible(true);
    // sollte nur instanziierbar sein, aber ohne Exception (ist ja leer)
    assertNotNull(ctor.newInstance());
  }
}
