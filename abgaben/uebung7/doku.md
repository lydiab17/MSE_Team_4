# Analyse des eigenen Codes

## Validierungsregeln als Daten modellieren

Im ursprünglichen VotingName-Value-Object passiert die Validierung im Konstruktor über eine Kette von if-Abfragen.Der
Eingabestring wird auf null geprüft, getrimmt, auf Leerheit geprüft und anschließend validiert (Länge, Pattern). Bei der
ersten Regelverletzung wird sofort eine IllegalArgumentException geworfen (
fail-fast). Dabei steckt die gesamte Logik als Kaskade im Konstruktor und es gibt sogar redundante Prüfungen (die Länge
wird doppelt geprüft), was Wartung und Erweiterung unnötig fehleranfällig macht.

![VotingNameVorher](./assets/VotingName_Vorher.png)

In der überarbeiteten Version werden die Validierungsregeln als “Daten” modelliert: eine Liste aus Regeln, die jeweils
aus einem Predicate<String> (reine Prüf-Funktion) und einer Fehlermeldung bestehen. Der Konstruktor normalisiert nur
einmal (trim) und wendet dann deklarativ per Stream die Regeln an, indem die erste fehlschlagende Regel gesucht und
deren Fehlermeldung geworfen wird. Das bringt vor allem bessere Wartbarkeit und Lesbarkeit (Regeln sind zentral
definiert, Konstruktor bleibt schlank), reduziert Redundanz, erleichtert Tests einzelner Regeln und macht spätere
Erweiterungen (z.B. neue Regel hinzufügen) sehr unkompliziert, ohne den Konstruktor weiter aufzublähen.

![VotingNameNachher](./assets/VotingName_Nachher.png)

## Token-Verarbeitung als pure Funktion kapseln

Im aktuellen castVote-Endpoint wird das Authorization-Header-Parsing direkt im Controller gemacht: Es wird
geprüft, ob der Header mit "Bearer " beginnt, und dann per substring(7) der Token extrahiert, ansonsten wird der Header
unverändert verwendet. Diese Logik ist zwar kurz, steckt aber als “Spezialfall” im Controller, ist nicht null-robust und
schwerer isoliert zu testen, weil sie nur indirekt über den Endpoint getestet wird.

![CastVoteVorher](./assets/CastVote_Vorher.png)

In der Überarbeitung wird das Parsing in eine pure Funktion ausgelagert, die den Header deterministisch in einen Token
transformiert (trimmen, optional Prefix entfernen, leere Werte abweisen).
Der Controller baut dann nur noch das CastVoteDto und delegiert an den Service. Dadurch wird der Controller
schlanker und klarer, das Token-Handling ist wiederverwendbar und separat testbar, und typische Edgecases (fehlender
Header, zusätzliche Leerzeichen, leere Tokens) werden konsistent und können separat getestet werden.

![CastVoteNachher](./assets/CastVote_Nachher.png)

## Filterlogik als Predicate ausdrücken 

Im ursprünglichen Code wurden offene und nicht offene Votings jeweils direkt über Inline-Lambdas im Stream gefiltert.
Die Filterlogik war dadurch zwar kurz, aber fachlich nicht ganz konsistent (für „open“ wurde eine Methode mit Uhr
verwendet, für „not-open“ nur ein Status-Flag) und die Regeln waren nicht als eigene, benannte Einheit erkennbar. Man
musste vorher die Bedeutung aus den Lambdas herauslesen.

![OpenVotingsVorher](./assets/OpenVotingsVorher.png)

In der Überarbeitung wird die Filterbedingung als Predicate<Voting> explizit gemacht: isOpen beschreibt als benannte
Funktion die Fachregel „Voting ist aktuell offen (abhängig von der Uhr)“. Für „not-open“ wird das Gegenstück über
isOpen.negate() gebildet. Dadurch wird die Logik funktionaler, die Fachdefinition von „not-open“ ist konsistent zum
„open“-Kriterium, und der Code ist leichter zu lesen und zu testen. Das liegt daran, dass die Filterregel klar benannt
ist und nicht nur als Inline-Ausdruck in der Lambdafunktion existiert. Ein Nachteil ist eine duplizierte Code Zeile und
generell mehr LoC als vorher, was wir aber zwecks Lesbarkeit in Kauf genommen haben.

![OpenVotingsNachher](./assets/OpenVotingsNachher.png)