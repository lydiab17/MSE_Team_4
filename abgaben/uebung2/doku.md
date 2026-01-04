# 1. Einführung in CI/CD

## Grundlegende Konzepte

### Continuous Integration

Continous Integration in der Softwareentwicklung unterstützt frühzeitig die Integration von neuem Code in das bestehende Software-System .
Hierzu werden verschiedene Schritte nacheinander ausgeführt, um eine reibungslose Integration zu gewährleisten.
Dazu gehören beispielsweise das Compilieren, Bauen und Testen der Software.
Durch einen gut abgestimmten CI-Prozess kann eine reduzierte Auslieferungszeit (Cycle-Time) der Software erzielt werden.

### Continuous Deployment

Änderungen bzw. Updates von Software wird beim Continous Deployment automatisiert in die Produktionsumgebung überführt.
Dabei ist es wichtig, dass die durch die CI definierten Prozessschritte (Compilieren, Bauen, Testen) erfolgreich absolviert worden sind.
Da es beim Deployment um die tatsächliche zur Verfügungstellung der Software auf dem Produktivsystem handelt, ist es unabdingbar, dass Fehler in der Pipeline zuvor erkannt werden.
Andernfalls können sich Bugs einschleichten und es kommt womöglich erst Stunden/Tage/Wochen später zu einer entsprechenden Wahrnhemnung dieser.
Durch die Nutzung automatisierter Pipelines im Kontext des CI/CD-Prozesses kann somit die Qualität der Software und seiner Lebenszyklen erheblich verbessert werden.


### Wahl der Platform

Wir haben die **Plattform GitHub Actions** ausgewählt, da wir uns bereits schon für Übung 1 entschieden haben unser Repository auf GitHub zu verwalten.
GitHub Actions wird von GitHub zur Verfügung gestellt und stellt somit ein Tool zur Verfügung, das alle notwendigen Funktionen für den Aufbau einer CI/CD-Pipeline bietet.
Insbesondere im Hinblick auf unsere bevorstehende Aufgabe eignet sich GitHub Actions hervorragend, um den Automatisierungsprozess effizient umzusetzen.


# 4. Deployment-Konzepte
Unser Deployment-Konzpet sieht vor, dass bei jedem Commit auf ein Branch das Kompilieren, Bauen und Testen des aktuellen Zustands des Repositories durchgeführt wird.
Erst nach erfolgreichem Bestehen dieser Schritte, wird es möglich sein, den entsprechenden Pull-Request auf den `main`-Branch zu mergen.
Bei einem Merge auf der `main`-Branch werden dann zusätlich Deployment-Schritte ausgeführt.
Dabei kann zwischen verschiedenen Deployment-Stages unterschieden werden.

- Stage QS:
  - Umgebung, welche nah an der Prudktivumgebung nah dran ist
  - Entwickler können hier zusätliche Tests durchführen und das Verhalten der Software auf dem Produktivsystem antizipieren
  - weitere Stakeholder können sich die neuen Features ansehen und abnehmen
- Stage PROD:
  - finale Umgebung, welche als Produktivumgebung dient
  - Software steht dem Endkunden nun zur Verfügung


Die Pipeline wird nach dem Merge auf `main` die Software auf der QS-Stage automatisch ausrollen.
Somit steht sie den Entwicklern/Stakeholdern für weitere Tests zur Verfügung.
Nach erfolgreicher Abnahme der Entwickler/Stakeholder wird durch einen manuellen Knopfdruck in der Pipeline die Software auf der PROD-Stage ausgeleiefert.
 



