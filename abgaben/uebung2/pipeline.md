# Der Aufbau der Pipeline

- Name : CI/CD-Pipeline
- Trigger der Pipeline:
    - bei `Push`-Event auf unsere personalisierten Branches (Fabian, lydia, Ulli)
    - `Pull-Request`-Event auf `main`
- Jobs:
    - `build`:
        - Baut die Anwendung, anhand des Zustands des Repositories
    - `unit-tests`:
        - Führt dir vorhandenen Unit-Tests durch
        - Abhängig vom Ergebnis des `build`-Step
    - `integration-tests`:
        - Führt dir vorhandenen Integrations-Tests durch
        - Abhängig vom Ergebnis des `unit-tests`-Step
    - `code_quality`:
        - Prüft die Code-Qualtität des Repositories
        - Abhängig vom Ergebnis des `build`-Step

Aktuell haben wir Abhängigkeiten bei den Schritten eingebaut, um ein `fail-fast`-Mechanismus in der Pipeline umzusetzen.
Somit werden keine weiteren Schritte durchgeführt, wenn in der Prozesskette der Schritt zuvor nicht erfolgreich war.
