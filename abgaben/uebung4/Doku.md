# Dokumentation

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