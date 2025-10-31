# Aufgabe 4: Entitäten und Aggregates Definition - eVote System


## 1. Citizen Management Context

### Aggregate Root: Citizen

**Zweck**: Zentrale Entität zur Verwaltung von Bürgerdaten und Authentifizierung

**Attribute:**
- `citizenId: CitizenId` (Value Object)
- `personalInfo: PersonalInfo` (Value Object)
- `authentication: Authentication` (Entity)
- `emailVerification: EmailVerification` (Value Object, optional)
- `registrationStatus: RegistrationStatus` (Value Object)

**Methoden:**
```
+ register(email: Email, password: String, firstName: String, lastName: String): CitizenRegistrationRequested
+ verifyEmail(verificationCode: String): EmailVerificationCompleted | InvalidVerificationCode
+ authenticate(password: String): CitizenAuthenticated | AuthenticationFailed
+ updatePersonalInfo(firstName: String, lastName: String): PersonalInfoUpdated
+ resetPassword(newPassword: String): PasswordReset
+ isEligibleToVote(): Boolean
+ isEmailVerified(): Boolean
+ getDisplayName(): String
```

**Geschäftsregeln:**
- Citizen kann nur mit gültiger E-Mail erstellt werden
- E-Mail-Verifizierung ist vor erster Abstimmung erforderlich
- Passwort-Updates erfordern Re-Authentifizierung

---

### Entity: Authentication

**Zweck**: Verwaltung der Authentifizierungsdaten

**Attribute:**
- `passwordHash: String`
- `isEmailVerified: Boolean`
- `lastLoginAt: DateTime` (optional)
- `failedLoginAttempts: Integer`
- `lockedUntil: DateTime` (optional)

**Methoden:**
```
+ verifyPassword(password: String): Boolean
+ updatePassword(newPasswordHash: String): Void
+ recordLoginAttempt(success: Boolean): Void
+ isAccountLocked(): Boolean
+ unlockAccount(): Void
```

---

### Value Objects

#### CitizenId
```
+ value: UUID
+ toString(): String
+ equals(other: CitizenId): Boolean
+ isValid(): Boolean
```

#### PersonalInfo
```
+ firstName: String
+ lastName: String
+ email: Email
+ getFullName(): String
+ updateEmail(newEmail: Email): PersonalInfo
+ isValid(): Boolean
```

#### Email
```
+ address: String
+ domain: String
+ isValid(): Boolean
+ toString(): String
+ equals(other: Email): Boolean
```

#### EmailVerification
```
+ verificationCode: String
+ expiresAt: DateTime
+ isUsed: Boolean
+ isExpired(): Boolean
+ markAsUsed(): EmailVerification
+ isValid(): Boolean
```

#### RegistrationStatus
```
+ status: RegistrationStatusEnum (Pending, Verified, Active)
+ verifiedAt: DateTime (optional)
+ isActive(): Boolean
+ markAsVerified(): RegistrationStatus
+ toString(): String
```

---

## 2. Voting Management Context

### Aggregate Root: Voting

**Zweck**: Zentrale Entität zur Verwaltung von Abstimmungen

**Attribute:**
- `votingId: VotingId` (Value Object)
- `votingInfo: VotingInfo` (Value Object)
- `votingSchedule: VotingSchedule` (Value Object)
- `votingOptions: List<VotingOption>` (Value Object Collection)
- `votingStatus: VotingStatus` (Value Object)

**Methoden:**
```
+ create(title: String, description: String, category: String): VotingCreated
+ addOption(optionText: String, displayOrder: Integer): VotingOptionAdded
+ removeOption(optionId: OptionId): VotingOptionRemoved
+ updateInfo(title: String, description: String): VotingInfoUpdated
+ setSchedule(startDate: DateTime, endDate: DateTime): VotingScheduleSet
+ publish(): VotingPublished | InvalidVotingState
+ close(): VotingClosed | InvalidVotingState
+ isActive(): Boolean
+ isEditable(): Boolean
+ canReceiveVotes(): Boolean
+ getOptionById(optionId: OptionId): VotingOption | NotFound
+ validateVotingRules(): ValidationResult
```

**Geschäftsregeln:**
- Mindestens 2 VotingOptions erforderlich vor Veröffentlichung
- Nur im Draft-Status bearbeitbar
- EndDate muss nach StartDate liegen
- Status-Übergänge: Draft → Published → Closed

---

### Value Objects

#### VotingId
```
+ value: UUID
+ toString(): String
+ equals(other: VotingId): Boolean
+ isValid(): Boolean
```

#### VotingInfo
```
+ title: String
+ description: String
+ category: String
+ isValid(): Boolean
+ updateTitle(newTitle: String): VotingInfo
+ updateDescription(newDescription: String): VotingInfo
```

#### VotingSchedule
```
+ startDate: DateTime
+ endDate: DateTime
+ timeZone: String
+ isActiveAt(checkTime: DateTime): Boolean
+ getDuration(): Duration
+ isValid(): Boolean
+ contains(dateTime: DateTime): Boolean
```

#### VotingOption
```
+ optionId: OptionId
+ optionText: String
+ displayOrder: Integer
+ isValid(): Boolean
+ updateText(newText: String): VotingOption
+ updateOrder(newOrder: Integer): VotingOption
```

#### VotingStatus
```
+ status: VotingStatusEnum (Draft, Published, Closed)
+ statusChangedAt: DateTime
+ canTransitionTo(newStatus: VotingStatusEnum): Boolean
+ transitionTo(newStatus: VotingStatusEnum): VotingStatus
+ toString(): String
```

---

## 3. Vote Management Context

### Aggregate Root: Vote

**Zweck**: Einzelne Stimmabgabe eines Bürgers

**Attribute:**
- `voteId: VoteId` (Value Object)
- `votingReference: VotingReference` (Value Object)
- `voterReference: VoterReference` (Value Object)
- `voteChoices: List<VoteChoice>` (Value Object Collection)
- `voteMetadata: VoteMetadata` (Value Object)
- `isValid: Boolean`

**Methoden:**
```
+ cast(citizenId: CitizenId, votingId: VotingId, choices: List<OptionId>): VoteCast
+ validate(): VoteValidated | VoteInvalid
+ invalidate(reason: String): VoteInvalidated
+ getChoiceCount(): Integer
+ hasChoice(optionId: OptionId): Boolean
+ isWithinTimeLimit(votingSchedule: VotingSchedule): Boolean
+ encrypt(): EncryptedVote
```

**Geschäftsregeln:**
- Ein Citizen kann nur einmal pro Voting abstimmen
- Vote muss innerhalb der Voting-Zeitspanne abgegeben werden
- Mindestens eine VoteChoice erforderlich

---

### Aggregate Root: VotingResult

**Zweck**: Ergebnis einer abgeschlossenen Abstimmung

**Attribute:**
- `resultId: ResultId` (Value Object)
- `votingReference: VotingReference` (Value Object)
- `voteCounts: List<VoteCount>` (Value Object Collection)
- `resultMetrics: ResultMetrics` (Value Object)
- `isPublished: Boolean`

**Methoden:**
```
+ calculate(votes: List<Vote>): VotingResultsCalculated
+ publish(): ResultsPublished | InvalidResultState
+ getWinningOption(): OptionId | NoWinner
+ getVoteCountForOption(optionId: OptionId): Integer
+ getTurnoutPercentage(): Double
+ isComplete(): Boolean
+ canBePublished(): Boolean
```

**Geschäftsregeln:**
- Ergebnisse können nur berechnet werden wenn Voting geschlossen ist
- Results sind unveränderlich nach Veröffentlichung
- Alle VotingOptions müssen im Result enthalten sein

---

### Value Objects (Vote Context)

#### VoteId
```
+ value: UUID
+ toString(): String
+ equals(other: VoteId): Boolean
+ isValid(): Boolean
```

#### VotingReference
```
+ votingId: VotingId
+ getVotingId(): VotingId
+ equals(other: VotingReference): Boolean
```

#### VoterReference
```
+ citizenId: CitizenId
+ getCitizenId(): CitizenId
+ equals(other: VoterReference): Boolean
```

#### VoteChoice
```
+ optionId: OptionId
+ getOptionId(): OptionId
+ equals(other: VoteChoice): Boolean
+ isValid(): Boolean
```

#### VoteMetadata
```
+ castAt: DateTime
+ ipAddress: String (encrypted)
+ userAgent: String (optional)
+ getTimestamp(): DateTime
+ isFromSameSession(other: VoteMetadata): Boolean
```

#### ResultId
```
+ value: UUID
+ toString(): String
+ equals(other: ResultId): Boolean
+ isValid(): Boolean
```

#### VoteCount
```
+ optionId: OptionId
+ voteCount: Integer
+ percentage: Double
+ incrementCount(): VoteCount
+ calculatePercentage(totalVotes: Integer): VoteCount
+ isValid(): Boolean
```

#### ResultMetrics
```
+ totalVotes: Integer
+ turnoutPercentage: Double
+ calculatedAt: DateTime
+ eligibleVoters: Integer (optional)
+ calculateTurnout(eligibleVoters: Integer): ResultMetrics
+ isValid(): Boolean
```

---

## Beziehungen zwischen Contexts

### Cross-Context Referenzen
- **Vote → Voting**: über `VotingReference` (enthält VotingId)
- **Vote → Citizen**: über `VoterReference` (enthält CitizenId)  
- **VotingResult → Voting**: über `VotingReference` (enthält VotingId)

### Integration Patterns
- **Anti-Corruption Layer**: Schützt Vote Context vor direkten Änderungen in anderen Contexts
- **Shared Kernel**: Gemeinsame Value Objects (VotingId, CitizenId) zwischen Contexts
- **Customer/Supplier**: Voting Management liefert Daten an Vote Management

### Datenfluss
1. Citizen registriert sich (Citizen Context)
2. Voting wird erstellt (Voting Context)  
3. Vote wird abgegeben (Vote Context referenziert beide anderen Contexts)
4. Results werden berechnet (Vote Context aggregiert Votes)

---

## Domain Events Mapping

Jede Methode löst entsprechende Domain Events aus:
- `Citizen.register()` → `CitizenRegistrationRequested`
- `Citizen.verifyEmail()` → `EmailVerificationCompleted`
- `Voting.publish()` → `VotingPublished`
- `Vote.cast()` → `VoteCast`
- `VotingResult.calculate()` → `VotingResultsCalculated`

Diese Events ermöglichen lose Kopplung zwischen den Bounded Contexts und unterstützen Event-driven Architecture.