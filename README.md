# EA1 Grundlagen Git

## Was ist Git und warum sollte es verwendet werden? 

## Grundlegende Git-Befehle (z. B. git init, git add, git commit, git push) 

## Branches und ihre Nutzung, Umgang mit Merge-Konflikten

Damit Teammitglieder in der Softwareentwicklung gleichzeitig, aber unabhängig voneinander arbeiten können,  
ohne sich gegenseitig zu behindern, wird in Versionsverwaltungssystemen wie **Git** mit **Branches** gearbeitet.

Ein **Branch** ist eine Abzweigung von einem bestimmten Zustand des Repositories (also der Projektdateien) zu einem bestimmten Zeitpunkt.  
Wenn beispielsweise ein neuer Branch `feature_123` von dem Branch `development` abzweigt, übernimmt `feature_123` den aktuellen Stand des Repositories von `development`.

---

### 🔧 Beispiel: Arbeiten mit Branches

1. **Überprüfen, auf welchem Branch du dich befindest:**
   ```shell git branch --show-current```

2. Neuen Branch 'Ulli' erstellen und direkt wechseln:
   ```git checkout -b Ulli```

3. Unterschiede zwischen Branches anzeigen lassen:
   ```git diff main```

Keine Ausgabe bedeutet: keine Unterschiede.

![create-branch.png](assets/create-branch.png)


### Simuliertes Beispiel mit mehreren Entwicklern

Um das oben genannte Beispiel umzusetzen, wird der Branch `feature_123` als Quellbranch genutzt.
Auf diesem Branch wird eine einfache Textdatei mit einem Satz erstellt.

Nun simulieren wir die Weiterentwicklung durch zwei Entwickler.
Diese erstellen sich jeweils eigene Branches:

`feature_123_edit_1`

`feature_123_edit_2`

In beiden Branches wird dieselbe Textdatei verändert.
Wenn nun beide Branches per Merge Request (MR) oder Pull Request (PR) wieder in den ursprünglichen Branch `feature_123` gemergt werden sollen,
erscheint eine Merge-Conflict-Warnung.

![merge-conflict-warnung.png](assets/merge-conflict-warnung.png)


### Merge-Konflikte verstehen

Ein Merge Conflict bedeutet, dass dieselbe Datei im Zielbranch (`feature_123`) bereits verändert wurde,
und Git nicht automatisch entscheiden kann, welche Version korrekt ist.

In diesem Fall müssen die Entwickler den Konflikt manuell lösen — also festlegen,
welcher Inhalt der Datei im finalen Merge bestehen bleiben soll.

![merge-conflict-aufoesen.png](assets/merge-conflict-aufoesen.png)

## Git mit IntelliJ/PyCharm benutzen: Local Repository und Remote Repository 

## Nützliche Git-Tools und Pla ormen (z. B. GitHub) 