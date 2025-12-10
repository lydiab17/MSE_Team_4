package citizen_management.domain.valueobjects;

import com.evote.app.citizen_management.domain.valueobjects.CitizenID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CitizenIDTest {

    // Happy-Path-Tests
    @Test
    @DisplayName("Happy Path: generate() sollte eine gültige CitizenID erzeugen")
    void testGenerateHappyPath() {
        CitizenID cid = CitizenID.generate();

        assertNotNull(cid, "Die CitizenID darf nicht null sein");
    }


    // Negative Tests
    @Test
    @DisplayName("Sollte NullPointerException werfen, wenn UUID null ist")
    void shouldThrowExceptionWhenCitizenIDNull() {
        assertThrows(NullPointerException.class, () -> new CitizenID(null));
    }
}
