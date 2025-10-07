# PostgreLite

**PostgreLite** ist ein Java-basierter PostgreSQL-Client, der die Verwaltung von Datenbanken, Tabellen und Daten besonders komfortabel und sicher gestaltet. Die Anwendung richtet sich sowohl an Einsteiger als auch an fortgeschrittene Nutzer und bietet eine intuitive Oberfläche mit Drag&Drop-Funktionalität, Expertenmodi, sowie einem PowerShell-inspirierten Adminbereich.
Ziel war es, eine Anwendung zu entwickeln, die mit wenigen Klicks produktiv macht — ohne auf Sicherheit, Flexibilität oder Transparenz zu verzichten.

## Features
- **Datenbanken erstellen mit einem Klick**: Schnell und unkompliziert neue PostgreSQL-Datenbanken anlegen — direkt aus der Oberfläche heraus.
- **Tabellen erstellen in zwei Modi**:
  1) Einsteiger-Modus: Tabellen und Spalten per Drag&Drop oder Button erstellen und im TableView anzeigen lassen und dort ggf. nochmal bearbeiten
  2) Experten-Modus: Freihändige Definition von Tabellen und Spalten über Textfeld

- **Whitelist & Blacklist-Mechanismen**:
  1) Whitelist: Nur erlaubte Aktionen beim Tabellenerstellen (z. B. keine DROP-Befehle)
  2) Blacklist: Kritische SQL-Befehle wie DELETE oder DROP sind im QueryTool gesperrt

- **QueryTool mit Sicherheitsfilter**: Daten abfragen und eintragen — mit intelligentem Schutz vor gefährlichen Befehlen. Ausgabe im TableView mit Postgre-Datentypen

- **Excel-Import mit Startzellenwahl**: Importiere Excel-Dateien in Tabellen, auch wenn die Daten nicht bei Zelle A1 beginnen

- **Export als Excel oder CSV**: Session-Historie oder Abfrageergebnisse bequem als Datei speichern

- **Dynamische PostgreSQL-Anbindung**: Nutzer können während der Laufzeit geändert werden, ohne dass die Anwendung neu gestartet werden muss

- **Benutzer- und Konfigurationswechsel während der Session**: Flexibles Umschalten zwischen verschiedenen PostgreSQL-Konfigurationen und Usern

- **Adminbereich im PowerShell-Stil**: Vollständiger Zugriff auf alle SQL-Befehle für erfahrene Nutzer

- **Datenbankauswahl per Dropdown**: Wähle bequem aus vorhandenen Datenbanken und sieh deren Tabellenstruktur ein

- **Tabellenübersicht**: Anzeige aller Tabellen einer Datenbank als Pop-up

- **Infobereich & Fehleranzeige**: Umfangreiche Rückmeldungen bei Fehlern, inklusive Ursachenbeschreibung und Lösungsvorschlägen

- **SQL-Highlighting**: Befehlsfelder sind mit SQL-Highlighting ausgestattet

- **Regex**: Schutz vor gefährlichen oder verbotenen Befehlen u.a. mit Regex-Prüfung


## Klonen
   ```bash
   git clone https://github.com/MGuenther-build/PostgreLite.git
   ```

## Start der App
   ``` bash
   Main.java
   ```

# Nutzung
* PostgreSQL-Konfiguration in der App hinterlegen
* Benutzer erstellen oder anmelden
* Datenbank auswählen oder neu erstellen
* Tabellen per Drag&Drop oder Expertenmodus definieren
* Daten importieren, abfragen oder exportieren
* Bei Bedarf in den Adminbereich wechseln


# Technologie
* Java
* JavaFX
* Maven
* PostgreSQL JDBC
* Apache POI (Excel-Import)
* RichTextFX (Editor-Komponente)
* Log4j (Logging)


# Lizenz
Diese App unterliegt der MIT-Lizenz. Sie kann von Jedem herunterladen oder geklont werden und dann natürlich auch verändert/erweitert werden! Ich bin dann allerdings nicht mehr Teil solcher Projekte.

# Autor
Martin Günther

# Link zum Projekt
https://www.mguenther-build.de/portfolio