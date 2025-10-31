# Domänenmodell - eVote System

## Überblick der Aggregate und Entitäten

### 1. Citizen Management Context

#### Aggregate: Citizen
```
Citizen (Aggregate Root)
├── CitizenId (Value Object)
├── PersonalInfo (Value Object)
│   ├── FirstName
│   ├── LastName
│   └── Email
├── Authentication (Entity)
│   ├── PasswordHash
│   └── IsEmailVerified
├── EmailVerification (Value Object)
│   ├── VerificationCode
│   ├── ExpiresAt
│   └── IsUsed
└── RegistrationStatus (Value Object)
    ├── Status (Pending/Verified/Active)
    └── VerifiedAt
```

**Geschäftsregeln:**
- Ein Citizen muss eine gültige E-Mail-Adresse haben
- E-Mail-Verifizierung muss vor der ersten Abstimmung erfolgen
- Citizen kann nur durch CitizenRegistrationService erstellt werden

---

### 2. Voting Management Context

#### Aggregate: Voting
```
Voting (Aggregate Root)
├── VotingId (Value Object)
├── VotingInfo (Value Object)
│   ├── Title
│   ├── Description
│   └── Category
├── VotingSchedule (Value Object)
│   ├── StartDate
│   ├── EndDate
│   └── TimeZone
├── VotingOptions (Collection of Value Objects)
│   ├── OptionId
│   ├── OptionText
│   └── DisplayOrder
└── VotingStatus (Value Object)
    ├── Status (Draft/Published/Closed)
    └── StatusChangedAt
```

**Geschäftsregeln:**
- Voting kann nur im Draft-Status bearbeitet werden
- Mindestens 2 VotingOptions erforderlich
- EndDate muss nach StartDate liegen

---

### 3. Vote Management Context

#### Aggregate: Vote
```
Vote (Aggregate Root)
├── VoteId (Value Object)
├── VotingReference (Value Object)
│   └── VotingId
├── VoterReference (Value Object)
│   └── CitizenId
├── VoteChoices (Collection of Value Objects)
│   └── OptionId
├── VoteMetadata (Value Object)
│   ├── CastAt
│   └── IPAddress (encrypted)
└── IsValid (boolean)
```

**Geschäftsregeln:**
- Ein Citizen kann nur einmal pro Voting abstimmen
- Vote muss innerhalb der Voting-Zeitspanne abgegeben werden

#### Aggregate: VotingResult
```
VotingResult (Aggregate Root)
├── ResultId (Value Object)
├── VotingReference (Value Object)
│   └── VotingId
├── VoteCounts (Collection of Value Objects)
│   ├── OptionId
│   ├── VoteCount
│   └── Percentage
├── ResultMetrics (Value Object)
│   ├── TotalVotes
│   ├── TurnoutPercentage
│   └── CalculatedAt
└── IsPublished (boolean)
```

**Geschäftsregeln:**
- Ergebnisse können nur berechnet werden wenn Voting geschlossen ist
- Results sind unveränderlich nach Veröffentlichung

## Value Objects (domain übergreifend)

### CitizenId
```
CitizenId
├── Value (UUID)
└── IsValid()
```

### VotingId  
```
VotingId
├── Value (UUID)
└── IsValid()
```

### Email
```
Email
├── Address
├── IsValid()
└── Domain
```

### TimeRange
```
TimeRange
├── StartDate
├── EndDate
├── IsValid()
└── Contains(DateTime)
```