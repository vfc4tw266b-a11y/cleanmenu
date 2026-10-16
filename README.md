# Clean Menu (Fabric, Minecraft Java 26.2)

Grundgeruest fuer ein modernes Dark-Mode-Menue im Stil von PvP-Clients.

- Taste: Standard Rechte Umschalt-Taste, frei belegbar unter Optionen > Steuerung > Tastenbelegung > "Clean Menu".
  Dieselbe Taste (und Esc) schliesst das Menue wieder.
- Sidebar mit Kategorien: Combat, Visuals, Utility, Settings
- Animierte Schalter (Toggle), ein Schieberegler (Settings), Hover-Effekte mit weicher Skalierung
- Oeffnen/Schliessen: Einblenden + Skalierung 95 % -> 100 %
- Scrollen mit Traegheit (Momentum) und Gummiband-Effekt an den Raendern
- Einstellungen werden in config/cleanmenu.properties gespeichert

Neue Funktion hinzufuegen: in `Modules.java` eine Zeile `add(new ToggleItem(...))` ergaenzen und im eigenen Code
`Modules.MEINE_FUNKTION.value` abfragen. Beispiel: `CoordsHud.java` (Koordinaten-HUD, schon voll funktionsfaehig).
Alle anderen Eintraege sind Platzhalter.

Bauen ohne Programme: Repository auf GitHub anlegen, alle Dateien hochladen (inkl. .github/workflows/build.yml),
unter "Actions" den Bau abwarten und unter "Artifacts" die .jar herunterladen.
