# GitHub Actions

## [GitHub Actions Documentation](https://docs.github.com/de/actions)

## [Erstellung von CI-CP-Pipeline mit Workflows (GitHub Actions)](https://docs.github.com/de/actions/get-started/quickstart#creating-your-first-workflow)

### Ordner im Repo erstellen ( falls nicht bereits vorhanden)

```shell
mkdir -p .github/workflows
```

### GitHub aCtion Workflow Datei erstellen (falls nicht bereits vorhanden)
```shell 
touch .github/workflows/github-actions-demo.yml
```


### Workflow Anforderungen:

- ein oder mehrere Ereignisse, die den Workflow triggern
- ein oder mehrere Aufträge (Jobs), die ausgeführt werden
  - ein oder merhere Schritte innerhalb des Auftrags, die ausgeführt werden 

### Workflow-Trigger 

- Ereignisse die im Repository auftreten
- Ereignisse, die außerhalb des Repo auftreten
- Geplante zeiten
- Manuell