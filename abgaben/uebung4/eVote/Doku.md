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
String n = Optional.ofNullable(name).map(String::trim) 
        .orElseThrow(() -> new IllegalArgumentException("name"));
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
