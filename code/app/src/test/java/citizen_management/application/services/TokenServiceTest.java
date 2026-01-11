package citizen_management.application.services;

import com.evote.app.citizen_management.application.services.TokenService;
import com.evote.app.sharedkernel.security.PseudonymToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test für TokenService
 */
class TokenServiceTest {

    /**
     * Echte Instanz des Services.
     */
    private final TokenService tokenService = new TokenService();

    /**
     * Happy-Path-Test:
     * Ein erzeugtes JWT muss sich validieren lassen
     * und den ursprünglichen Username zurückgeben.
     */
    @Test
    void generateAndValidateToken_shouldReturnUsername() {
        // Given: ein gültiger Username
        String username = "alice";

        // When: ein JWT für den User erzeugt wird
        String token = TokenService.generateToken(username);

        // Then: das Token muss existieren
        assertNotNull(token);

        // And: bei der Validierung muss der ursprüngliche Username zurückkommen
        String validatedUsername = TokenService.validateToken(token);
        assertEquals(username, validatedUsername);
    }

    /**
     * Sicherheits-Test:
     * Ein offensichtlich ungültiger Token darf nicht validiert werden.
     */
    @Test
    void validateToken_withInvalidToken_shouldReturnNull() {
        // Given: ein String, der kein gültiges JWT ist
        String invalidToken = "this.is.not.a.jwt";

        // When: versucht wird, den Token zu validieren
        String result = TokenService.validateToken(invalidToken);

        // Then: die Validierung muss fehlschlagen (null)
        assertNull(result);
    }


    /**
     * Die gleiche Bürger-ID muss immer das gleiche Pseudonym erzeugen.
     * Wichtig für Wiedererkennung und Konsistenz.
     */
    @Test
    void pseudonymize_sameCitizenId_shouldProduceSameToken() {
        // Given: eine feste Bürger-ID
        String citizenId = "123456";

        // When: die Pseudonymisierung mehrfach aufgerufen wird
        PseudonymToken t1 = tokenService.pseudonymize(citizenId);
        PseudonymToken t2 = tokenService.pseudonymize(citizenId);

        // Then: beide Pseudonyme müssen identisch sein
        assertEquals(t1.value(), t2.value());
    }

    /**
     * Kollisions-Test:
     * Unterschiedliche Bürger-IDs dürfen nicht das gleiche Pseudonym erhalten.
     */
    @Test
    void pseudonymize_differentCitizenIds_shouldProduceDifferentTokens() {
        // When: zwei unterschiedliche IDs pseudonymisiert werden
        PseudonymToken t1 = tokenService.pseudonymize("123");
        PseudonymToken t2 = tokenService.pseudonymize("456");

        // Then: die erzeugten Pseudonyme müssen unterschiedlich sein
        assertNotEquals(t1.value(), t2.value());
    }
}
