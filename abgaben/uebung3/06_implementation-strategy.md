# Aufgabe 6: Implementierungsstrategie - eVote System

## Überblick

Diese Implementierungsstrategie beschreibt, wie die DDD-Modelle des eVote Systems in Code umgesetzt werden. Fokus liegt auf Testbarkeit, Modularität und strikter Einhaltung von DDD-Prinzipien.

1. Umsetzung der Entitäten 
2. Umsetzung der Aggregate
3. Umsetzung der Domain Services 
4. Umsetzung der Repositories

---

## 1. Architektur-Prinzipien

### Hexagonal Architecture (Ports & Adapters)
```
┌─────────────────────────────────────────────────────┐
│                  Infrastructure                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │ REST APIs   │  │ Database    │  │ Email       │  │
│  │ (Web)       │  │ (JPA)       │  │ Service     │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  │
└─────────────┬───────────────┬───────────────┬───────┘
              │               │               │
         ┌────▼────┐     ┌────▼────┐     ┌────▼────┐
         │ Port    │     │ Port    │     │ Port    │
         │(Interface)   │(Interface)     │(Interface)
         └────┬────┘     └────┬────┘     └────┬────┘
              │               │               │
┌─────────────▼───────────────▼───────────────▼───────┐
│                Domain Layer                         │
│  ┌─────────────────────────────────────────────────┐│
│  │ Citizen Context │ Voting Context │ Vote Context ││
│  │  ┌───────────┐  │  ┌───────────┐ │  ┌─────────┐ ││ 
│  │  │Aggregates │  │  │Aggregates │ │  │Aggregates ││
│  │  │Services   │  │  │Services   │ │  │Services │ ││
│  │  └───────────┘  │  └───────────┘ │  └─────────┘ ││
│  └─────────────────┴────────────────┴──────────────││
└─────────────────────────────────────────────────────┘
```

### Layer Separation
1. **Domain Layer**: Geschäftslogik, DDD-Patterns
2. **Application Layer**: Use Cases, Orchestrierung
3. **Infrastructure Layer**: Persistierung, externe Services
4. **Presentation Layer**: REST APIs, UI

---

