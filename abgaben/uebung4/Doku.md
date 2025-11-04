# Dokumentation
## Aufagbe 1
Übung 4
Wir haben uns beim LLM für ChatGPT entschieden, da ChatGPT gut geeignet ist für Review und Verbesserung Implementierungsstrategien sowie Testgenerierung, und Refactoring- und Codequalitäts-Überprüfungen. Außerdem bietet ChatGPT sehr verständliche Erklärungen, wie in unserem Fall bezüglich DDD, TDD und Java-Features und baut Antworten sehr strukturiert auf. Es ist natürlich verständlich, dass ChatGPT auch Fehler machen kann. Daher verwenden wir die KI nur als „Partner“ und überprüfen stets die Antworten. 
 

Die KI hat unsere Implementierungsstrategie auf folgende Punkte überprüfen müssen: 
(a)	Ihre Implementierungsstrategie zu analysieren und potenzielle Schwachstellen zu identifizieren
Die KI fasst unsere Implementierungsstrategie wie folgt zusammen: „drei Schichten in typischem DDD-Stil:
•	Application Layer: Use-Cases, DTOs, Event Handlers — orchestriert Domänenoperationen, spricht Repositories/Services an.
•	Domain Layer: Drei Bounded Contexts (Citizen Management, Voting Management, Vote Processing) mit Aggregaten, Domain-Services und Repositories (Interfaces).
•	Infrastructure Layer: konkrete Adapter (REST, DB, Email).
•	Shared Kernel: gemeinsame primitive Typen/IDs (CitizenID, VotingID, Email, TimeRange).
Das ist ein sauberer Start — Bounded Contexts sind klar getrennt und du hast Service/Repository-Abstraktionen vorgesehen.”
ChatGPT analysiert die Implementierungsstrategie und listet potenzielle Schwachstellen auf: 
1.	„Authentication als Value im Domain Layer vs. Infra
	Problem: Wenn Auth-Mechanik (Tokens, Signaturen) als Domain-Value behandelt wird, vermischst du fachliche Regeln und technische Implementierung. Das erschwert Tests und Austausch der Auth-Implementierung.
2.	Unklare Transaction Boundaries / Invariant Enforcement
	Problem: Die Kerninvariant „max. 1 Stimme pro Bürger pro Abstimmung“ ist nicht spezifiziert, wie sie erzwingbar ist (synchron DB-Constraint vs. asynchrone Deduplication/Saga). Ohne Entscheidung drohen Race Conditions oder Overengineering.
3.	VotingResult als Aggregate statt Projection
	Problem: Wenn VotingResult schreibbar bleibt (Aggregate), vermischst du berechnete Daten mit Autoritativen Writes — das macht Skalierung & Reconciliation schwieriger. Besser: Projection/Read Model.
4.	Events & Audit nicht ausreichend spezifiziert
	Problem: Es fehlen Regeln zu Pseudonymisierung/PII in Events, Idempotency, Event-Sourcing vs. simple Event-Publishing. Das kann Datenschutzverletzungen und nicht-deterministisches Verhalten im Verarbeitungspipeline verursachen.
5.	Testbarkeit / Infra-Abhängigkeiten zu früh
	Problem: Wenn Repositories/Adapters nicht als Ports/Interfaces früh vorgesehen sind, wirst du beim TDD-Start schnell auf konkrete Infra (DB) angewiesen — langsame Tests, schweres Refactoring.“

(b)	Vorschläge für eine bessere Strukturierung Ihrer Domain-Events zu erhalten
Folgende Verbesserungsvorschläge schlägt ChatGPT vor: 
1.	„Auth als Port/Adapter: Domain kennt nur VerificationStatus / pseudonymToken
	Nutzen: Domain bleibt testbar; Auth-Implementierung tauschbar; PII bleibt in Infra.
	Kosten: Mehr Interfaces/Boilerplate, aber klarer DDD-Boundary.
2.	Trennung: Vote als eigenes Aggregate (append-only), VotingResult als Projection (CQRS)
	Nutzen: ermöglicht Pseudonymisierung, append-only Speicherung, einfache Replays für Audit; skalierbar.
	Kosten: Mehraufwand für Event Handler & Projections; zusätzliche Komponenten (worker).
3.	Definiere dedizierte Transaction-Strategy pro Invariant
	Nutzen: Vermeidet Race-Conditions (z. B. synchron für MVP, asynchron für Scale).
	Kosten: Entscheidungsaufwand; später Refactor möglich.“
 
(c)	Die Entwicklungsreihenfolge zu optimieren
Ziel: schnelle Feedback-Loops, kleine Schritte, leicht testbar — starte bottom-up:
1.	Value Objects + Validatoren (TDD)
	Email, Name, TimeRange, PseudonymToken — schreibe Unit-Tests (Happy/Edge/Negative).
	Warum: klein, determiniert, schnelle Tests; Grundlage aller Aggregates.
2.	InMemory Repositories & Test Infrastructure
	Implementiere InMemory/Stub Repos für Citizen/Voting/Vote + simple in-memory EventBus.
	Warum: ermöglicht TDD ohne DB-Infra.
3.	Citizen Aggregate + CitizenRegistrationService (TDD)
	Tests: Valid registration, duplicate email, markVerified. AuthPort als Mock.
	Warum: baut user model, needed for auth flows.
4.	Voting Aggregate + VotingManagementService (TDD)
	Tests: create voting, invalid TimeRange, open/close, addOption rules. Emit PollOpened.
	Warum: core domain; other flows depend on it.
5.	Vote Aggregate + VoteRepository existsBy... behavior (TDD)
	Implement append-only Vote model, validation of option existence (via VotingRepository mock). Tests: submit, invalid option.
	Warum: core for cast flow.
6.	CastVoteUseCase orchestration (sync variant) + tests (integration with InMemory repos + AuthPort stub)
	Test flow: auth -> check voting open -> dedupe check -> save vote -> emit VoteSubmitted.
	Warum: provides deterministic behavior for MVP; easy to test.
7.	Event Handlers & Projections (VotingResult) + idempotency tests
	Implement handler for VoteSubmitted → update VotingResult projection. Tests: repeated event handling no double-count.
	Warum: validates event pipeline and read model correctness.
8.	Refactor: Replace InMemory with JPA/H2 (integration tests) / add real AuthAdapter stub
	Run integration tests, fix infra issues.
	Warum: ensures real infra compatibility.
9.	Optional: Asynchronize flow (scale) — enqueue events, worker for dedupe/sagas, add reconciliation tests
	Move to event queue, create background processor for dedupe, update projections via events.
	Warum: scale readiness; complex but optional for homework.

Schrittweise wurde die Implementierungsstrategie, die Vorschläge, Schwächen und die jeweiligen Bauteile eines Domänenmodell nach DDD und mithilfe der KI konnte letztendlich eine neue Implementierungsstrategie entwickelt werden:

--> siehe Implementierungsstrategie_Ue4.png
 
Die KI hat beispielsweise vorgeschlagen im Domain Layer Value Objects zu verankern und hierein Events wie Email, Time Range und Pseudonym Token zu „packen“. Diesen Vorschlag haben wir nicht angenommen und stattdessen unseren Shared Kernel behalten. Im Shared Kernel sind die gemeinsamen wiederverwendbaren Domänen-Bausteine enthalten. Time Range gehört daher nicht in Value Objects oder Shared Kernel. Time Range ist abhängig von der jeweiligen Abstimmung und ist daher im Domain Layer unter Voting. Weiter noch waren sehr konkrete und präzise Vorschläge für den Infrastructure Layer (seitens der KI) vorgeschlagen worden, die wir nicht in unsere neue Implementierungsstrategie übernehmen wollten, da wir eine grobe Übersicht im Infrastructure Layer behalten wollten.  
Die wichtigsten Events haben wir festgelegt:
-	Abstimmung starten (mit Option, damit überhaupt eine Stimme abgegeben werden kann und damit aufgrund der abgegebenen Stimmen zu einem Ergebnis kommen kann.
-	Stimme abgeben können (damit Bürger sich nicht nur registrieren können, sondern auch abstimmen könne, um Veränderungen vornehmen bzw. beeinflussen zu können)
Diese wichtigsten Events wurden ChatGPT vorgeschlagen und das ist die Antwort:
„Ja — die beiden Events, die du ausgewählt hast, passen sehr gut als zentrale Domain-Events. Ich erkläre, warum und worauf du achten solltest:
1.	 Event „Abstimmung gestartet“ (VotingStartedEvent)
Warum wichtig:
•	Es markiert den Beginn der Abstimmung – ohne diesen Event kann niemand abstimmen.
•	Triggert oft weitere Prozesse, z. B.:
o	Benachrichtigung an Bürger (Email / Push)
o	Aktivierung von Abstimmungs-Logik in der Application Layer
o	Event-Sourcing / Audit Trails
Fokus: Fachliche Tatsache „Es gibt jetzt eine laufende Abstimmung“ – keine technischen Details wie DB oder REST.
2.	Event „Stimme abgegeben“ (VoteSubmittedEvent)
Warum wichtig:
•	Kern der Domäne: Bürger nehmen Einfluss auf Abstimmungsergebnisse.
•	Wird für mehrere Zwecke gebraucht:
o	Berechnung der Ergebnisse (ResultCalculationService)
o	Event-basierte Updates (Projektionen, Dashboards)
o	Audit / Nachvollziehbarkeit
Fokus: Fachliches Ereignis, wer hat wann für welche Option gestimmt – anonymisiert über PseudonymToken.
3.	 Warum diese Events gut sind
•	Sie bilden den Kern der Domäne ab: ohne Abstimmung gibt es keine Stimme, ohne Stimme keine Ergebnisse.
•	Sie eignen sich hervorragend für Event-Publishing, also lose Kopplung zwischen Domain und Application/Infrastructure.
•	Sie können leicht in TDD-Tests als Trigger für UseCases verwendet werden.
4.	Optional / Ergänzend
Man könnte noch weitere Events definieren, z. B.:
•	VotingClosedEvent → Abstimmung ist beendet
•	CitizenRegisteredEvent → neuer Bürger registriert
Aber für die zentrale Fachlogik deines eVote-Systems sind deine beiden Events genau richtig.
💡 Kurz gesagt:
„VotingStarted“ markiert den Beginn, „VoteSubmitted“ das Kernverhalten der Domäne – zusammen bilden sie das Herz der Business-Logik.“


## Aufgabe 2

### Prompt an die KI
> Es geht darum, eine Abstimmung zu erstellen (für digitale Bürgerabstimmungen).  
> **Abstimmungsname (`name`)**: Muss mit einem Großbuchstaben beginnen und mindestens 10 Zeichen lang sein. Im Titel sind **keine Sonderzeichen** erlaubt.  
> **Beschreibung (`info`)**: Muss mindestens 30 Zeichen lang sein und ebenfalls mit einem Großbuchstaben beginnen.  
> Erstelle bitte **Tests** für das Anlegen einer Abstimmung.  
> Denk dir gern weitere sinnvolle Einschränkungen aus, damit die in der Aufgabe beschriebenen Test-Cases abgedeckt werden. Es können dafür auch neue Variablen in der Klasse vorausgesetzt werden (bitte nicht übertreiben).  
> Wenn du Ideen für weitere Methoden hast, gerne her damit (ebenfalls nicht übertreiben). **Nicht** die Methode „abstimmen“ implementieren (höchstens „Abstimmung noch offen“ o. Ä.), da sich ein anderer Student aus meiner Gruppe um das eigentliche Abstimmen kümmert.

### Einschätzung der generierten Tests
Ich habe mir die Tests angesehen und sie sind inhaltlich schlüssig. **Angepasst habe ich nichts.**  
Etwas ungewohnt finde ich bei den Edge-Cases, dass ChatGPT die Beschreibung (`info`) zunächst als erklärenden Text belegt und sie in der nächsten Zeile direkt mit dem tatsächlichen Testwert überschreibt (z. B. in Zeile 96). Damit kann ich aber gut leben. Beispiel:

```java
void edge_infoExactlyMinLength() {
    // Erst eine erklärende Zeichenkette ...
    String info = "Beschreibung hat genau 30 Zeichen"; // Hinweistext (tatsächlich 31 Zeichen)
    // ... und dann der eigentliche Testwert mit exakt 30 Zeichen:
    info = "AbcdefghijAbcdefghijAbcdefghij"; // 30 exakt
    // ...
}
```
## Aufgabe 3

ChatGPT hat direkt eine **Klasse** erstellt, die **alle Tests bestanden** hat. Der initiale Code war gut lesbar. Einige Parameter (z. B. die **ID**) wurden in der `create`-Methode jedoch **nicht ausreichend geprüft** (beispielsweise konnten **negative IDs** übergeben werden). Dafür gab es allerdings auch noch keine Tests.

Bei der iterativen Verbesserung leidet die **Lesbarkeit** für Laien zunächst etwas. Durch moderne Sprachmittel (**`Optional`**, **Streams**, **Lambdas**) konnten jedoch **Codezeilen reduziert** und die **Langzeit-Wartbarkeit** verbessert werden.

Beispiel:

Vorher:
```java
if (name == null) {
    throw new IllegalArgumentException("name");
}
String n = name.trim();
```
Nachher:
```java
String n = Optional.ofNullable(name).map(String::trim).orElseThrow(() -> new IllegalArgumentException("name"));
```
Einige Änderungen die vorgeschlagen wurden hätten die lesbarkeit so stark eingeschränkt, so dass diese nicht implementiert wurden. Ein Beispiel dafür ist die Umlaute über Unicode Kategorien abzudecken anstatt explizit anzugeben:

Vorher:
```java
private static final Pattern NAME_RE = Pattern.compile("^[A-ZÄÖÜ][A-Za-zÄÖÜäöüß0-9 ]{9,99}$");
```
Vorschlag von ChatGPT:
```java
private static final Pattern NAME_RE = Pattern.compile("^(\\p{Lu})[\\p{L}\\p{N} ]{9,99}$");
```
### Implementierte Patterns / Entscheidungen
- **Statische Factory-Methode** `create(...)` als *Named Constructor* für **Validierung + Erzeugung**.
- **Zeitraum** als **Value Object** (`Period` als `record`), das die Invariante **`start ≤ end`** sicherstellt.
- **Eingabevalidierungen** (Name/Info/Optionen) erfolgen bereits bei der Erstellung.

### Arbeitsweise
- **ChatGPT** den Code generieren lassen.
- **Versucht** den Code selbst zu verstehen
- **Rückfragen** wurden per **Audiofunktion** gestellt und von ChatGPT zufriedenstellend beantwortet (teilweise mehrere Rückfragen).

### Kurzfazit
- Tests: **bestanden** und inhaltlich schlüssig.  
- Code: nach der Iteration **robuster** und **wartbarer**; kurzfristig etwas weniger zugänglich (aufgrund fehlender Java Expertise), langfristig jedoch **konsistenter** und **kürzer**.

## Aufgabe 4

### Test-Erweiterung
- Erster Prompt an ChatGPT: Könntest du mir die Klassen Voting und VotingTest bitte erklären: ...
- Zweiter Prompt an ChatGPT: Bitte erweitere meine Tests in der Klasse "VotingTest" und identifiziere zusätzliche Edge-Cases und Fehlerbedingungen.

1. Negativer Test
- Testet, dass Voting.create() keine null-Werte akzeptiert
- Name, Info, Optionsliste, Start- und Enddatum dürfen nicht null sein
- Wenn name oder info null sind → IllegalArgumentException
- Wenn start, end oder options null sind → NullPointerException
- Problem: ChatGPT hat die falschen Exceptions beim Test angegeben

```java
    @Test
    @DisplayName("Null-Werte: Name, Info, Optionen, Start, End werfen Exception")
    void nullValues_throwException() {
        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(21, null, "Info OK Mit Mehr Als Dreißig Zeichen.",
                        today, today.plusDays(1), opts("Ja", "Nein")));

        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(22, "Abstimmung OK", null,
                        today, today.plusDays(1), opts("Ja", "Nein")));

        assertThrows(NullPointerException.class, () ->
                Voting.create(23, "Abstimmung OK", "Info OK Mit Mehr Als Dreißig Zeichen.",
                        null, today.plusDays(1), opts("Ja", "Nein")));

        assertThrows(NullPointerException.class, () ->
                Voting.create(24, "Abstimmung OK", "Info OK Mit Mehr Als Dreißig Zeichen.",
                        today, null, opts("Ja", "Nein")));

        assertThrows(NullPointerException.class, () ->
                Voting.create(25, "Abstimmung OK", "Info OK Mit Mehr Als Dreißig Zeichen.",
                        today, today.plusDays(1), null));
    }
```

2. Happy-Path-Test
- Test stellt sicher, dass das Info-Feld (Beschreibung der Abstimmung)Zeilenumbrüche enthalten darf (mehrzeilig)

```java
    @Test
    @DisplayName("Info darf Zeilenumbrüche enthalten (DOTALL aktiv)")
    void info_withNewline_valid() {
        Voting v = Voting.create(1, "Abstimmung",
                "Dies ist eine Beschreibung\nmit Zeilenumbruch.",
                LocalDate.now(), LocalDate.now().plusDays(1), opts("Ja", "Nein"));
        assertNotNull(v);
    }
```

### Refactoring-Vorschläge
- Prompt an ChatGPT: Ich schicke dir zwei meiner Java-Klassen (Voting und VotingTest). Bitte gib mir für die Klasse Voting Refactoring-Vorschläge: Code-Smells, Verbesserung der Code-Lesbarkeit, Performance Optimierungen, bessere Verwendung von Java-Features.

1. Code-Smells
- Problem: zu viele Verantwortlichkeiten in create-Methode (Validierung, Regex, Trim)
- Lösung: mehrere kleinere Hilfsmethoden
- VorteiL: Übersichtlichkeit

Vorher:
```java
public static Voting create(int id, String name, String info, LocalDate start, LocalDate end, Set<String> options) {
        if (id <= 0) throw new IllegalArgumentException("Invalid id");

        // Optional für sauberes Trim + Null-Check in einem Schritt. Trim entfernt Leerzeichen am Anfang und Ende.
        String n = Optional.ofNullable(name).map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("name"));
        String i = Optional.ofNullable(info).map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("info"));

        Objects.requireNonNull(options, "options");
        // Regex-Checks für Name und Info
        if (!NAME_RE.matcher(n).matches()) throw new IllegalArgumentException("Invalid name");
        if (!INFO_RE.matcher(i).matches()) throw new IllegalArgumentException("Invalid info");

        // Period (Record) validiert Datumsordnung
        Period period = new Period(start, end);

        // Prüft ob die Anzahl der übergebenen Optionen ok ist
        if (options.size() < 2 || options.size() > 10) throw new IllegalArgumentException("Invalid options size");

        // Streams: trimmen, token prüfen, in definierter Reihenfolge einsammeln
        // peak schaut sich das aktuelle objekt an
        List<String> normalized = options.stream()
                .peek(o -> { if (o == null) throw new IllegalArgumentException("Null option"); })
                .map(String::trim)
                .peek(t -> { if (!OPT_RE.matcher(t).matches()) throw new IllegalArgumentException("Invalid option: " + t); })
                .collect(Collectors.toCollection(ArrayList::new));

        // Generics + Lambda: Duplikate auf einem beliebigen Schlüssel verbieten (hier: lower-case)
        // Optionen werden hier überprüft
        requireNoDuplicate(normalized, s -> s.toLowerCase(Locale.ROOT), dup ->
                new IllegalArgumentException("Duplicate option: " + dup));

        Voting v = new Voting();
        v.votingID = id;
        v.name = n;
        v.info = i;
        v.startDate = period.start();
        v.endDate = period.end();
        // Unmodifiable + Reihenfolge erhalten
        v.options = Collections.unmodifiableSet(new LinkedHashSet<>(normalized));
        v.votingStatus = false;
        return v;
    }
```

Nachher:
```java
public static Voting_R create(int id, String name, String info,
                                LocalDate start, LocalDate end,
                                Set<String> options) {

        if (id <= 0) throw new IllegalArgumentException("Invalid id");

        // Schrittweise Validierung
        String n = validateName(name);
        String i = validateInfo(info);
        Period period = validateDates(start, end);
        Set<String> opts = validateOptions(options);

        // Objekt aufbauen
        Voting_R v = new Voting_R();
        v.votingID = id;
        v.name = n;
        v.info = i;
        v.startDate = period.start();
        v.endDate = period.end();
        v.options = opts;
        v.votingStatus = false;
        return v;
    }
```

2. Verbesserung der Code-Lesbarkeit
- Problem: Magic Numbers sind schlecht lesbar
- Lösung: Verwendung von Konstanten mit sprechenden Namen
- Vorteil: einfachere Wartung, bessere Lesbarkeit der Regex-Ausdrücke

Vorher:
```java
    private static final Pattern NAME_RE = Pattern.compile("^[A-ZÄÖÜ][A-Za-zÄÖÜäöüß0-9 ]{9,99}$");
    private static final Pattern INFO_RE = Pattern.compile("^[A-ZÄÖÜ].{29,999}$", Pattern.DOTALL);
    private static final Pattern OPT_RE  = Pattern.compile("^[A-Za-zÄÖÜäöüß0-9 ]{1,50}$");
```

Nachher: 
```java
  private static final int MIN_NAME_LEN = 10;
  private static final int MAX_NAME_LEN = 100;
  private static final int MIN_INFO_LEN = 30;
  private static final int MAX_INFO_LEN = 1000;
  private static final int MAX_OPTION_LEN = 50;

  private static final Pattern NAME_RE =
    Pattern.compile("^[A-ZÄÖÜ][A-Za-zÄÖÜäöüß0-9 ]{" + (MIN_NAME_LEN - 1) + "," + (MAX_NAME_LEN - 1) + "}$");

  private static final Pattern INFO_RE =
    Pattern.compile("^[A-ZÄÖÜ].{" + (MIN_INFO_LEN - 1) + "," + (MAX_INFO_LEN - 1) + "}$", Pattern.DOTALL);

  private static final Pattern OPT_RE =
    Pattern.compile("^[A-Za-zÄÖÜäöüß0-9 ]{1," + MAX_OPTION_LEN + "}$");
```
### Systematisches Refactoring
- Die für mich sinnvollen Vorschläge von ChatGPT habe ich schrittweise implementiert (siehe oben) in die neue Klasse Voting_R.java. Die Tests bestehen weiterhin. 
- Beispiel für einen nicht sinnvollen Refactoring-Vorschlag: manchmal IllegalArgumentException, manchmal NullPointerException (vereinheitlichen); Unterschiedliche Exception-Typen helfen, Fehlerarten zu trennen


## Aufgabe 5

### Modularität und Testbarkeit
Müssen wir noch machen. Eine kurze Erklärung (8-10 Sätze), wie Sie Modularität und Testbarkeit in Ihrem Code umgesetzt haben.

### CI/CD Pipeline
Die CI/CD-Pipeline startet automatisch, sobald jemand auf die Branches Fabian, lydia oder Malinda pusht oder einen Pull Request auf main stellt.

```yaml
on:
  push:
    branches: [ Fabian, lydia, Malinda ]
  pull_request:
    branches: [ main ]
```

Die Pipeline läuft auf einer GitHub-VM (Ubuntu), richtet Java 17 ein
und lädt anschließend das Projekt aus dem Repository herunter.
Im Anschluss wird der Code mit Maven gebaut und getestet.
Das working-directory gibt an, wo sich die pom.xml Datei befindet.

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: maven
      - name: Build & run tests (Maven)
        working-directory: abgaben/uebung4/eVote
        run: mvn -B -ntp test
```
Test
