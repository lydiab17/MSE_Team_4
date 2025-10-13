# EA1 Grundlagen Git

## Was ist Git und warum sollte es verwendet werden? (Lydia Boes)
Git ist ein verteiltes Versionskontrollsystem (= Distributed Version Control System). Mithilfe von Git ist es möglich, Änderungen an Softwareprojekten (z.B. an Quellcode oder Dateien) zu verfolgen und nachzuvollziehen. Zum Beispiel lassen sich damit die folgenden Fragen beantworten:
- Wer hat etwas geändert?
- Wann wurde etwas geändert?
- Was wurde geändert?
- Wo wurde etwas geändert?
- Warum wurde etwas geändert?

Git bietet die Möglichkeit mit mehreren Entwicklern gleichzeitig am selben Softwareprojekt zu arbeiten. Auf dieser Grundlage sind die Entwickler in der Lage zwischen verschiedenen Versionen zu wechseln oder eigene Versionen mit denen der Teammitglieder zusammenzuführen (mergen).

Heutzutage gilt Git als Standard in der Softwareentwicklung. 

## Grundlegende Git-Befehle (z. B. git init, git add, git commit, git push) 

## Branches und ihre Nutzung, Umgang mit Merge-Konflikten

## Git mit IntelliJ/PyCharm benutzen: Local Repository und Remote Repository (Fabian Schmitz)
1. **Git muss auf dem Rechner installiert sein.**  
   Um zu prüfen, ob IntelliJ den richtigen Pfad findet:  
   `File → Settings → Version Control → Git`  
   Hier kann man überprüfen, ob der Git-Pfad korrekt ist.

   ![Git Path prüfen](assets/Settings_Menu_IntelliJ.png)
   ![Git Path prüfen](assets/Git_Path_IntelliJ.png)
   
2. **Über das Menü VCS können Git-Befehle auch ohne Konsole ausgeführt werden.**

   a. **Beispiel: Ein neues Git-Repository erstellen:**  
   `VCS → Create Git Repository → (Projektordner auswählen)`

   ![Git Repository erstellen](assets/Create_Git_Repo_IntelliJ.png)

   b. **Einen Commit erstellen:**  
   Über Menü `Git → Commit`.  
   ![Commit erstellen](assets/Commit_IntelliJ1.png)

   Dort auswählen, welche Dateien committed werden sollen, und eine Commit Message schreiben.  
   Die Commits können bei Bedarf auch direkt gepusht werden. Danach erscheint unten rechts eine Erfolgsmeldung.

   ![Commit erstellen](assets/Commit_IntelliJ2.png)


## Nützliche Git-Tools und Pla ormen (z. B. GitHub) 