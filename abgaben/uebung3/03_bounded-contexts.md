# Bounded Contexts - eVote System

## Identifizierte Bounded Contexts

### 1. Citizen Management Context
**Verantwortlichkeit:** Citizen registration, authentication and access control

**Events:**
- CitizenRegistrationRequested
- CitizenRegistrationCompleted
- CitizenAuthenticated
- EmailVerificationSent

**Kern-Entitäten:**
- `Citizen` (Aggregate Root)
  - `PersonalInfo` (Value Object)
  - `Authentication` (Entity)
  - `EmailVerification` (Value Object)
  - `RegistrationStatus` (Value Object)

**Sprache/Begriffe:**
- Citizen, Registration, Authentication, Authorization, Verification

---

### 2. Voting Management Context
**Verantwortlichkeit:** Voting creation, configuration and administration

**Events:**
- VotingCreated
- VotingPublished
- VotingClosed

**Kern-Entitäten:**
- `Voting` (Aggregate Root)
  - `VotingInfo` (Value Object)
  - `VotingSchedule` (Value Object)
  - `VotingOptions` (Collection of Value Objects)
  - `VotingStatus` (Value Object)

**Sprache/Begriffe:**
- Voting, Poll, Deadline, Publication, Configuration

---

### 3. Vote Management Context
**Verantwortlichkeit:** Vote casting, processing and result calculation

**Events:**
- VoteCast
- VoteValidated
- VotingResultsCalculated
- ResultsPublished

**Kern-Entitäten:**
- `Vote` (Aggregate Root)
  - `VotingReference` (Value Object)
  - `VoterReference` (Value Object)
  - `VoteChoices` (Collection of Value Objects)
  - `VoteMetadata` (Value Object)
- `VotingResult` (Aggregate Root)
  - `VotingReference` (Value Object)
  - `VoteCounts` (Collection of Value Objects)
  - `ResultMetrics` (Value Object)

**Sprache/Begriffe:**
- Vote, Voter, Validation, Result, Counting

## Context Mapping

### Beziehungen zwischen Contexts

```
Citizen Management ←→ Vote Management
- Shared: CitizenId, Authentication Status

Voting Management ←→ Vote Management  
- Shared: VotingId, Voting Configuration
```

### Integration Patterns

**1. Citizen Management ↔ Vote Management:**
- **Pattern:** Shared Kernel
- **Shared Concepts:** CitizenId, Authentication Status
- **Grund:** Security and authorization requirements

**2. Voting Management → Vote Management:**
- **Pattern:** Customer/Supplier
- **Interface:** Voting Configuration, Eligibility Rules
- **Grund:** Vote Management needs voting rules and configuration

## Context-spezifische Modelle

### Citizen Management Model
- Focus on citizen identity and authorization
- Definition of "Citizen" with authentication attributes

### Voting Management Model  
- Focus on voting configuration and state
- Definition of "Voting" with administrative properties

### Vote Management Model
- Focus on vote casting and results
- "Citizen" as VoterReference, "Voting" as VotingReference