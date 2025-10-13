# EA1 Grundlagen Git

## Was ist Git und warum sollte es verwendet werden? 

## Grundlegende Git-Befehle (z. B. git init, git add, git commit, git push) 
[Git-Befehle.pdf](https://github.com/user-attachments/files/22888931/Git-Befehle.pdf)

| Git-Befehl | Description / Erklärung |
| --- | --- |
|$ git help add	| Öffnet die Browser-Dokumentation für den Befehl add als HTML.|
|$ git init	| Erstellt im aktuellen Verzeichnis ein neues Git-Repository; alle nötigen Dateien unter .git werden angelegt.|
|$ git init mydir	| Erstellt ein neues Verzeichnis mydir und initialisiert dort ein Git-Repository. |
|$ git clone httpsLink | Erstellt eine lokale Kopie eines entfernten Repositories. |
|$ git status	| Zeigt den aktuellen Status des Repositories: welche Dateien geändert, gestaged oder untracked sind. |
|$ git commit	| Speichert alle Änderungen, die gestagt wurden als einen neuen Commit im lokalen Repository. |
|$ git diff | Unterschiede zwischen Arbeitsverzeichnis und Staging (nicht gestagte Änderungen) werden aufgezeigt. |
|$ git diff --cached	| Unterschiede zwischen Staging und letztem Commit (bereit zum Commit) werden gezeigt. |
|$ git diff HEAD	| Unterschiede zwischen Arbeitsverzeichnis und letztem Commit werden gezeigt. |
|$ git add file(s)	| Fügt einzelne Dateien dem Staging hinzu. |
|$ git add README.md	| Fügt die Datei README.md dem Staging hinzu. |
|$ git add *.java	| Fügt alle .java-Dateien dem Staging hinzu. |
|$ git add SecretFolder |	Fügt den gesamten Ordner SecretFolder rekursiv dem Staging hinzu. |
|$ git add .	| Fügt im aktuellen Verzeichnis alle neuen bzw. geänderten Dateien dem Staging hinzu. |
|$ git commit -a -m "Nachricht"	| Erstellt einen Commit aller geänderten Dateien (ohne neue Dateien) mit einer Nachricht. |
|$ git push <repository>	| Auf das entfernte Repository werden alle lokalen Commits übertragen. |
|$ git pull --rebase <remote> <branch>	| Änderungen werden vom Remote-Repository gezogen und in den aktuellen Branch eingefügt, dabei werden lokale Commits "neu angewendet" (Rebase).|
|$ git rm useless.md	| Entfernt die Datei useless.md aus dem Arbeitsverzeichnis und dem Staging-Bereich. |
|$ git log	| Commit-History des aktuellen Branches wird angezeigt.|
|$ git log -p [-2]	| Commit-Historie wird angezeigt sowie die Änderungen (Patch); optional nur die letzten 2 Commits.|
|$ git branch	| Zeigt bzw. listet alle lokalen Branches auf. |
|$ git branch <branch name>	| Erstellt einen neuen Branch mit dem angegebenen Namen.|
|$ git checkout -b <branch name>	| Erstellt einen neuen Branch und wechselt sofort zu diesem Branch.|
|$ git branch -d <branch name>	| Löscht den angegebenen Branch lokal.|


## Branches und ihre Nutzung, Umgang mit Merge-Konflikten

## Git mit IntelliJ/PyCharm benutzen: Local Repository und Remote Repository 

## Nützliche Git-Tools und Pla ormen (z. B. GitHub) 
