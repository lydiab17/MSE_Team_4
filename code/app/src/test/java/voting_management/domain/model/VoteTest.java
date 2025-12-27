package voting_management.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.evote.app.votingmanagement.domain.model.Vote;
import org.junit.jupiter.api.Test;

class VoteTest {

  @Test
  void createNew_setsAllFields_andGeneratesUuid_andSetsSubmittedAt() {
    int votingId = 42;
    String optionId = "Ja";
    String voterKey = "pseudonym-123";

    Instant before = Instant.now();
    Vote vote = Vote.createNew(votingId, optionId, voterKey);
    Instant after = Instant.now();

    assertNotNull(vote);

    // IDs / basic fields
    assertEquals(votingId, vote.getVotingId());
    assertEquals(optionId, vote.getOptionId());
    assertEquals(voterKey, vote.getVoterKey());

    // id should be UUID
    assertNotNull(vote.getId());
    assertFalse(vote.getId().isBlank());
    assertDoesNotThrow(() -> UUID.fromString(vote.getId()));

    // submittedAt should be within [before, after] (plus small tolerance)
    assertNotNull(vote.getSubmittedAt());

    // Wenn deine CI mal minimal drift hat, ist das robust:
    assertTrue(!vote.getSubmittedAt().isBefore(before.minusMillis(50)),
            "submittedAt sollte nicht deutlich vor 'before' liegen");
    assertTrue(!vote.getSubmittedAt().isAfter(after.plusMillis(50)),
            "submittedAt sollte nicht deutlich nach 'after' liegen");
  }

  @Test
  void createNew_allowsEmptyOptionIdString_butNotNull() {
    Vote vote = Vote.createNew(1, "", "voter");
    assertEquals("", vote.getOptionId());
  }

  @Test
  void createNew_throwsNullPointer_whenOptionIdNull() {
    assertThrows(NullPointerException.class, () -> Vote.createNew(1, null, "voter"));
  }

  @Test
  void createNew_throwsNullPointer_whenVoterKeyNull() {
    assertThrows(NullPointerException.class, () -> Vote.createNew(1, "Ja", null));
  }

  @Test
  void privateConstructor_rejectsNullSubmittedAt_andNullId_viaReflection() throws Exception {
    Constructor<Vote> ctor = Vote.class.getDeclaredConstructor(
            String.class, int.class, String.class, String.class, Instant.class
    );
    ctor.setAccessible(true);

    // id null
    assertThrows(Exception.class, () ->
            ctor.newInstance(null, 1, "Ja", "voter", Instant.now())
    );

    // submittedAt null
    assertThrows(Exception.class, () ->
            ctor.newInstance("id", 1, "Ja", "voter", null)
    );
  }

  @Test
  void createNew_generatesDifferentIds_forDifferentVotes() {
    Vote v1 = Vote.createNew(1, "Ja", "voter");
    Vote v2 = Vote.createNew(1, "Ja", "voter");

    assertNotEquals(v1.getId(), v2.getId());
  }
}
