# eVote – Anwendung für E-Voting mit DDD, TDD, JavaFX & Spring

## ToDo
- Shared Kernel mit Pseudonymtoken, so dass Abstimmung nicht Person auf Server zugewiesen werden kann
- Domain Events implementieren 
- Zentrale fetchAPI Klasse, optimalerweise async
- Im Votingmanagement anfragen ans backend über REST
- weitere AOP Einsatzmöglichkeiten außer Logging
- Funktionale Programmierkonzepte (Übung 7)
- SOLID-Prinzipien beim Code überprüfen

## Projekt holen und starten

```bash
git clone https://github.com/lydiab17/MSE_Team_4.git
cd MSE_Team_4/code/app
mvn clean verify
mvn clean javafx:run
```

## Voraussetzungen

- Java 21 (Temurin / OpenJDK)
- Maven 3.9+
- Git


Dieses Projekt implementiert ein vereinfachtes elektronisches Abstimmungssystem (**eVote**) als Unterrichtsbeispiel für:

- Domain Driven Design (DDD)
- Test Driven Development (TDD)
- Java & Spring Boot (Backend)
- JavaFX (Desktop-UI)
- CI/CD mit GitHub Actions
- Code-Qualität (Checkstyle, JaCoCo)
- Aspect Oriented Programming (Logging mit AOP)

---

## 1. Fachlicher Überblick

Ziel des Systems ist es, einfache Online-Abstimmungen zu ermöglichen:

- **Abstimmungen (Votings)** anlegen, mit:
  - Titel, Beschreibung, Zeitraum
  - 2–10 Antwortoptionen
- **Abstimmungen öffnen/schließen**
- **Stimmen abgeben** (Vote)
- **Ergebnisse** pro Option zählen
- **Bürger / Citizen Management** (separater Bounded Context, u. a. für Registrierung & Authentifizierung – noch in Arbeit)

---

## 2. Architektur & Bounded Contexts

### 2.1 Bounded Contexts

Aktuell werden hauptsächlich zwei Bounded Contexts verwendet:

1. **`votingmanagement`**
   - gesamte Abstimmungslogik:
     - Voting-Erstellung, Öffnen/Schließen
     - Stimmabgabe
     - Ergebnisermittlung
   - JavaFX-UI für Voting-Verwaltung & Stimmabgabe
   - REST-API (`/api/votings/...`)

2. **`citizen_management`**
   - Verantwortlich für Bürgerdaten:
     - Name, E-Mail, Passwort, CitizenID
   - Dient perspektivisch der Authentifizierung & Pseudonymisierung
   - Wird später als „Shared Kernel“ für Pseudonym-Token etc. angebunden

> Hinweis: Der ehemals separate `vote`-Bounded-Context wurde konzeptionell in `votingmanagement` integriert, um fachliche Doppelung zu vermeiden (Feedback aus Zwischenpräsentation).

### 2.2 Schichten (DDD)

Im Bounded Context `votingmanagement` wird eine typische DDD-Schichtung verwendet:

- **Domain (`domain`)**
  - `Voting`, `Vote` (Aggregates)
  - Value Objects: `VotingName`, `VotingInfo`, `OptionLabel`, ggf. später PseudonymToken
  - `VotingRepository`, `VoteRepository` (Ports)

- **Application (`application.services`, `application.dto`)**
  - `VotingApplicationService` – zentrale Anwendungslogik:
    - Voting-Use-Cases (create, open, close, getOpen, getNotOpen, getResults…)
    - Vote-Use-Case (`castVote`)
  - DTOs für interne Use-Cases (`CastVoteDto`, `OptionResult` etc.)

- **Interfaces**
  - `interfaces.rest`:
    - `VotingRestController` mit Endpoints unter `/api/votings`
  - `votingmanagement.ui.controller`:
    - JavaFX-Controller für Voting-Verwaltung & Stimmabgabe

- **Infrastructure**
  - `InMemoryVotingRepository`, `InMemoryVoteRepository`
  - AOP-Aspekte (Logging)
  - ggf. spätere Adapter (Datenbank, Messaging…)

---

## 3. Technologiestack

- **Sprache:** Java 21
- **Build Tool:** Maven
- **Frameworks:**
  - Spring Boot 3.5.x (Core & Web)
  - JavaFX 21 (UI)
  - ControlsFX für komfortablere UI-Controls
- **Testing:**
  - JUnit 5
  - Spring Boot Test
- **Qualität / Tools:**
  - JaCoCo (Test Coverage)
  - Checkstyle (Google Java Style)
  - Spring AOP + AspectJ (z. B. LoggingAspect)

---

