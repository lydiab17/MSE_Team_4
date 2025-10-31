# Aufgabe 5: Domain Services und Repositories - eVote System


## Domain Services

Domain Services enthalten Geschäftslogik, die nicht natürlich in eine Entität passt, insbesondere wenn mehrere Aggregate oder externe Systeme beteiligt sind.

### 1. Citizen Registration Service

**Zweck**: Verwaltung des komplexen Registrierungsprozesses mit E-Mail-Verifizierung

**Verantwortlichkeiten:**
- Koordiniert Citizen-Erstellung mit E-Mail-Verifizierung
- Verwaltet Verifizierungscodes und deren Ablauf
- Sicherstellung der E-Mail-Eindeutigkeit

**Methoden:**
```java
public class CitizenRegistrationService {
    
    // Startet Registrierungsprozess
    public RegistrationResult registerCitizen(
        String email, 
        String password, 
        String firstName, 
        String lastName
    );
    
    // Verarbeitet E-Mail-Verifizierung
    public VerificationResult verifyEmail(
        String verificationCode
    );
    
    // Prüft E-Mail-Verfügbarkeit
    public boolean isEmailAvailable(String email);
    
    // Sendet Verifizierung erneut
    public void resendVerification(CitizenId citizenId);
    
    // Komplettiert Registrierung nach Verifizierung
    public CompletionResult completeRegistration(CitizenId citizenId);
}
```

**Domain Events:**
- `CitizenRegistrationRequested`
- `EmailVerificationSent`
- `CitizenRegistrationCompleted`

---

### 2. Voting Management Service

**Zweck**: Verwaltung des Voting-Lebenszyklus und Koordination zwischen Contexts

**Verantwortlichkeiten:**
- Koordiniert Voting-Erstellung und -Publikation
- Überwacht Voting-Deadlines
- Verwaltet Status-Übergänge

**Methoden:**
```java
public class VotingManagementService {
    
    // Erstellt neue Abstimmung
    public VotingCreationResult createVoting(
        String title, 
        String description, 
        String category,
        List<String> options,
        VotingSchedule schedule
    );
    
    // Publiziert Abstimmung
    public PublicationResult publishVoting(VotingId votingId);
    
    // Schließt Abstimmung automatisch bei Deadline
    public ClosureResult closeVoting(VotingId votingId);
    
    // Prüft alle offenen Votings auf Deadline
    public void checkVotingDeadlines();
    
    // Validiert Voting vor Publikation
    public ValidationResult validateVotingForPublication(VotingId votingId);
    
    // Holt aktive Votings für Citizen
    public List<VotingInfo> getActiveVotingsForCitizen(CitizenId citizenId);
}
```

**Domain Events:**
- `VotingCreated`
- `VotingPublished` 
- `VotingClosed`

---

### 3. Vote Processing Service

**Zweck**: Koordiniert Stimmabgabe-Prozess und Validierung zwischen Contexts

**Verantwortlichkeiten:**
- Validiert Voting-Berechtigung (Cross-Context)
- Prüft Duplicate-Voting
- Koordiniert Vote-Erstellung mit Validation

**Methoden:**
```java
public class VoteProcessingService {
    
    // Hauptmethode für Stimmabgabe
    public VoteCastingResult castVote(
        CitizenId citizenId,
        VotingId votingId, 
        List<OptionId> choices
    );
    
    // Prüft Berechtigung zur Stimmabgabe
    public EligibilityResult checkVotingEligibility(
        CitizenId citizenId, 
        VotingId votingId
    );
    
    // Prüft ob bereits abgestimmt
    public boolean hasAlreadyVoted(
        CitizenId citizenId, 
        VotingId votingId
    );
    
    // Validiert Stimme gegen Voting-Regeln
    public VoteValidationResult validateVote(
        VotingId votingId, 
        List<OptionId> choices
    );
    
    // Verarbeitet Stimme nach erfolgreichem Cast
    public void processVote(VoteId voteId);
}
```

**Domain Events:**
- `VoteCast`
- `VoteValidated`

---

### 4. Results Calculation Service

**Zweck**: Berechnung und Publikation von Abstimmungsergebnissen

**Verantwortlichkeiten:**
- Aggregiert alle Stimmen einer Abstimmung
- Berechnet Ergebnisse und Statistiken
- Publiziert finale Ergebnisse

**Methoden:**
```java
public class ResultsCalculationService {
    
    // Berechnet Ergebnisse für geschlossene Abstimmung
    public CalculationResult calculateResults(VotingId votingId);
    
    // Publiziert berechnete Ergebnisse
    public PublicationResult publishResults(ResultId resultId);
    
    // Holt Ergebnisse für Anzeige
    public VotingResultView getPublishedResults(VotingId votingId);
    
    // Berechnet Live-Statistiken (ohne Speicherung)
    public LiveStatistics calculateLiveStatistics(VotingId votingId);
    
    // Validiert Ergebnisse vor Publikation
    public ValidationResult validateResults(ResultId resultId);
    
    // Archiviert alte Ergebnisse
    public void archiveResults(VotingId votingId);
}
```

**Domain Events:**
- `VotingResultsCalculated`
- `ResultsPublished`

---

## Repositories

Repositories abstrahieren die Persistierung der Aggregate Roots und bieten domänen-spezifische Abfrage-Methoden.

### 1. Citizen Repository

**Zweck**: Persistierung und Abfrage von Citizen Aggregates

```java
public interface CitizenRepository {
    
    // Standard CRUD
    void save(Citizen citizen);
    Optional<Citizen> findById(CitizenId citizenId);
    void delete(CitizenId citizenId);
    
    // Domänen-spezifische Queries
    Optional<Citizen> findByEmail(Email email);
    boolean existsByEmail(Email email);
    List<Citizen> findUnverifiedCitizens();
    List<Citizen> findByRegistrationStatus(RegistrationStatusEnum status);
    
    // Sicherheits-Queries
    Optional<Citizen> findByEmailForAuthentication(Email email);
    List<Citizen> findLockedAccounts();
    
    // Statistik-Queries
    long countVerifiedCitizens();
    long countActiveCitizens();
}
```

---

### 2. Voting Repository

**Zweck**: Persistierung und Abfrage von Voting Aggregates

```java
public interface VotingRepository {
    
    // Standard CRUD
    void save(Voting voting);
    Optional<Voting> findById(VotingId votingId);
    void delete(VotingId votingId);
    
    // Status-basierte Queries
    List<Voting> findByStatus(VotingStatusEnum status);
    List<Voting> findActiveVotings();
    List<Voting> findOpenVotings();
    List<Voting> findClosedVotings();
    
    // Zeit-basierte Queries
    List<Voting> findVotingsEndingBefore(DateTime deadline);
    List<Voting> findVotingsStartingAfter(DateTime startTime);
    List<Voting> findVotingsInPeriod(DateTime start, DateTime end);
    
    // Kategorie-basierte Queries
    List<Voting> findByCategory(String category);
    
    // Paging für UI
    Page<Voting> findAll(Pageable pageable);
    Page<Voting> findActiveVotings(Pageable pageable);
}
```

---

### 3. Vote Repository

**Zweck**: Persistierung und Abfrage von Vote Aggregates

```java
public interface VoteRepository {
    
    // Standard CRUD
    void save(Vote vote);
    Optional<Vote> findById(VoteId voteId);
    void delete(VoteId voteId);
    
    // Duplicate-Checking
    Optional<Vote> findByCitizenAndVoting(CitizenId citizenId, VotingId votingId);
    boolean existsByCitizenAndVoting(CitizenId citizenId, VotingId votingId);
    
    // Voting-basierte Queries
    List<Vote> findAllByVoting(VotingId votingId);
    List<Vote> findValidVotesByVoting(VotingId votingId);
    long countVotesByVoting(VotingId votingId);
    
    // Citizen-basierte Queries
    List<Vote> findAllByCitizen(CitizenId citizenId);
    long countVotesByCitizen(CitizenId citizenId);
    
    // Aggregation für Results
    Map<OptionId, Long> countVotesByOption(VotingId votingId);
    
    // Audit-Queries
    List<Vote> findVotesCastInPeriod(DateTime start, DateTime end);
    List<Vote> findInvalidVotes(VotingId votingId);
}
```

---

### 4. Voting Result Repository

**Zweck**: Persistierung und Abfrage von VotingResult Aggregates

```java
public interface VotingResultRepository {
    
    // Standard CRUD
    void save(VotingResult result);
    Optional<VotingResult> findById(ResultId resultId);
    void delete(ResultId resultId);
    
    // Voting-basierte Queries
    Optional<VotingResult> findByVoting(VotingId votingId);
    boolean existsByVoting(VotingId votingId);
    
    // Status-basierte Queries
    List<VotingResult> findPublishedResults();
    List<VotingResult> findUnpublishedResults();
    
    // Zeit-basierte Queries
    List<VotingResult> findResultsCalculatedAfter(DateTime date);
    List<VotingResult> findResultsPublishedInPeriod(DateTime start, DateTime end);
    
    // Statistik-Queries
    long countPublishedResults();
    List<VotingResult> findTopResultsByTurnout(int limit);
}
```

---

## Integration zwischen Services und Repositories

### Service Dependencies
```
// CitizenRegistrationService dependencies
- CitizenRepository
- EmailService (Infrastructure)

// VotingManagementService dependencies  
- VotingRepository
- CitizenRepository (für Eligibility Check)

// VoteProcessingService dependencies
- VoteRepository
- VotingRepository
- CitizenRepository

// ResultsCalculationService dependencies
- VotingResultRepository
- VoteRepository
- VotingRepository
```

### Cross-Context Coordination
- Services koordinieren zwischen Bounded Contexts
- Repositories bleiben innerhalb ihrer Context-Grenzen
- Anti-Corruption Layer durch Service-Abstraktionen
- Domain Events für asynchrone Integration

---
