Diese Übersetzung wurde von Vito Smolej angefertigt, Copyright© 2009.

==============================================================================
  OmegaT 2.0, LiesMich Datei

  1.  Information über OmegaT
  2.  Was ist OmegaT?
  3.  Installation von OmegaT
  4.  Beiträge zu OmegaT
  5.  Ärger mit OmegaT? Hilfe benötigt?
  6.  Details über die Ausgabe

==============================================================================
  1.  Information über OmegaT


Die aktuellste Info über OmegaT befindet sich hier: 
      http://www.omegat.org/

Unterstützung der Benutzer (mehrsprachig), mit Archiven, die auch ohne Abonnement zugänglich sind:
     http://groups.yahoo.com/group/OmegaT/

Vorschläge für Verbesserungen (Englisch), auf der SourceForge Seite:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Fehlerberichte (in Englisch), auf der SourceForge Seite:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Was ist OmegaT?

OmegaT ist ein Werkzeug für die Computer unterstützte Übersetzung (CAT). Es ist frei, das heißt, für die Benutzung, auch für  professionelle Zwecke, wird kein Entgelt verlangt; darüber hinaus kann jeder Benutzer, so lange die Benutzerlizenz respektiert wird, OmegaT  ändern und/oder weiter verteilen.

Hauptmerkmale von OmegaT sind:
  - es läuft in allen Betriebssystemen, die Java unterstützen. 
  - es benutzt TMX Dateien als die zweisprachige Referenz für die Übersetzungen
  - es wird eine flexible Satz-Segmentierung nach einer SRX-ähnlichen Methode benutzt
  - Suche nach den entsprechenden Segmenten erfolgt im Projekt selber und in den Referenz-TMs
  - es wird in allen Ordnern nach Dateien mit unterstützten Formaten gesucht 
  - unscharfe Treffer
  - clevere Behandlung von Projekten einschließlich komplizierter Hierarchien der Ordner
  - OmegaT unterstützt Glossare (Kontrolle der Terminologie)
  - klare und umfassende Dokumentation und Anleitung
  - Lokalisierung in mehreren Sprachen

Im Ausgangszustand unterstützt OmegaT folgende Dateiformate:
  - Reintext
  - HTML und XHTML
  - HTML Hilfe Erstellung
  - OpenDocument/OpenOffice.org
  - Java Quellbündel (.properties)
  - INI Dateien (Dateien mit Sätzen vom Typ Schlüssel = Wert mit beliebiger Kodierung)
  - PO Dateien
  - DocBook Dokumentation Dateiformat
  - Microsoft OpenXML Dateien
  Okapi einsprachige XLIFF Dateien

OmegaT kann angepasst werden, um andere Dateiformate zu unterstützen.

OmegaT wird sogar die hoch komplizierten Quellordner-Hierarchien automatisch analysieren, um auf alle unterstützten Dateien zuzugreifen und sie in einen Ziel-Ordner mit genau derselben Struktur einschließlich Kopien irgendwelcher nicht unterstützten Dateien zu erzeugen. 

Für eine schnelle Anfangseinführung OmegaT starten und die Schnellstartanleitung lesen.

Das Benutzerhandbuch ist im Paket, das Sie gerade herunter luden; Sie können nach dem Starten von OmegaT darauf mit Menü [Hilfe] zugreifen. 

==============================================================================
 3. Installation von OmegaT

3.1 Allgemein
Um OmegaT benutzen zu können, muss auf dem System Java Runtime Environment (JRE) Version 1. 4 oder höher installiert sein. Um den Benutzern den Ärger mit Suchen, Erwerben und Installieren zu ersparen, ist OmegaT als Standard mit Java Runtime Environment ausgerüstet. 

Für Windows und Linux Benutzer: wenn Sie sicher sind, dass in Ihrem System eine passende Version von JRE installiert ist, können sie OmegaT ohne JRE installieren (darauf wird im Namen der Version mit "Without_JRE" hingewiesen). 
Wenn Sie Zweifel haben, empfehlen wir die "Standard" Version, d.h. mit  JRE. Das ist sicher, denn selbst wenn JRE auf Ihrem System schon installiert ist, wird diese Version damit nicht in Konflikt geraten.

Linux Benutzer sollten bedenken: OmegaT kommt nicht mit den freien/open-source Java Anwendungen aus, die mit vielen Linux Auslieferungen (zum Beispiel Ubuntu) als Paket kommen, da sie entweder überholt oder unvollständig sind. Laden Sie Sun's Java Runtime Environment (JRE) anhand des o.a. Links herunter und installieren es oder laden Sie das OmegaT Paket gebündelt mit JRE herunter und installieren es (das .tar.gz Bündel gekennzeichnet als "Linux").

Mac Benutzer: JRE wird bereits mit Mac OS X installiert. 

Linux auf PowerPC Systemen: Benutzer werden IBM's JRE herunterladen müssen, da Sun kein JRE für PPC Systeme bereitstellt. Herunterladen in dem Fall von:
    http://www-128. ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Installation
* Windows Benutzer: Einfach das Installationsprogramm starten. Wenn Sie möchten, kann das Installationsprogramm Verknüpfungen zum Start von OmegaT erstellen.
* Andere: Um OmegaT zu installieren, einfach einen geeigneten Ordner für OmegaT erstellen
(z.B., /usr/local/lib unter Linux). OmegaT zip oder tar.gz Archiv in den Ordner kopieren und auspacken. 

3.3 OmegaT starten
OmegaT kann auf verschiedene Arten gestartet werden.

* Windows Benutzer: mit Doppelklick auf die Datei OmegaT.exe.  Wenn Sie im Dateimanager (Windows Explorer) die Datei OmegaT sehen können, aber nicht OmegaT.exe, dann die Einstellungen so ändern, dass Dateierweiterungen gezeigt werden. 

* mit Doppelklick auf die Datei OmegaT.jar.  Das geht nur, wenn auf dem System der .jar Dateityp mit Java assoziiert ist. 

* Aus der Befehlszeile Der Befehl, OmegaT zu starten, lautet:

cd <Ordner mit der Datei OmegaT.jar>

<Name und Pfad für die ausführbare Java Datei>-jar OmegaT.jar

(Die ausführbare Java Datei ist java unter Linux und java.exe unter Windows.
Ist Java auf Systemebene installiert, muss der volle Pfad nicht angegeben werden.)

* Windows Benutzer: Das Installationsprogramm kann Verknüpfungen im Startmenü, auf dem Desktop und im Schnellstartbereich erzeugen. Sie können manuell mit drag-and-drop 
OmegaT.exe ins Startmenü, auf den Desktop oder in den Schnellstartbereich ziehen, um darauf zu verlinken.

* Linux KDE Benutzer: Sie können OmegaT zu Ihren Menüs wie folgt hinzufügen:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New Submenu

Nach der Auswahl eines passenden Menüs, ein Untermenü/Element mit Datei - Neues Unterverzeichnis und Datei - Neues Element hinzufügen. Geben Sie OmegaT als Name des neuen Elements ein.

Im "Befehl"-Feld die Schaltfläche Navigation benutzen, um das OmegaT Startskript zu finden und auszuwählen. 

Klicken Sie auf die Ikone (rechts von den Feldern Name/Beschreibung/Kommentar) - Andere Ikonen - Durchsuchen, und navigieren Sie zum /images Unterordner im OmegaT Programmordner.  OmegaT.png Ikone auswählen.

Schließlich die Änderungen mit Datei - Speichern sichern. 

Linux GNOME Benutzer: OmegaT der Leiste (oben am Bildschirm) wie folgt hinzufügen:

Rechtsklick auf die Leiste - Neues Programm hinzufügen. Fügen Sie "OmegaT" im Feld "Name" ein; im Feld "Befehl" benutzen Sie die Schaltfläche Navigation, um Ihr OmegaT Startskript zu finden. Auswählen und mit OK bestätigen. 

==============================================================================
 4. Beteiligung am OmegaT Projekt

Um an der OmegaT Entwicklung mitzuwirken, kann man mit den Entwicklern wie folgt Kontakt aufnehmen:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Um die OmegaT Benutzeroberfläche, das Handbuch oder andere Dokumente zu übersetzen, bitte lesen:
      
      http://www.omegat.org/en/translation-info.html

und auf der Liste der Übersetzer eintragen:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Für andere Art von Beiträgen sich zuerst bei der Benutzergruppe anmelden:
      http://tech.groups.yahoo.com/group/omegat/

Bekommen Sie ein Gefühl dafür, was in der OmegaT Welt so vor sich geht...

  OmegaT ist die ursprüngliche Arbeit von Keith Godfrey. 
  Marc Prior ist der Koordinator des OmegaT Projektes. 

Vorherige Mitwirkende schließen ein:
(in alphabetischer Ordnung)

Code wurde beigesteuert von 
  Zoltan Bartko
  Didier Briel (Manager der Ausgabe/n)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

Andere Beiträge von
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary
  Vito Smolej (Manager der Dokumentation)
  Samuel Murray
  Marc Prior (Manager der Lokalisierung)
  und viele, viele andere sehr hilfsbereite Personen

(Wenn Sie der Meinung sind, zum OmegaT Projekt bedeutende Beiträge geleistet zu haben, aber Ihren Namen auf den Listen nicht sehen, treten Sie einfach in Kontakt mit uns.)

OmegaT verwendet die folgenden Bibliotheken:

  HTMLParser von Somik Raha, Derrick Oswald und anderen (LGPL Lizenz)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 von Steve Roy (LGPL Lizenz)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.0.6d von VLSolutions (CeCILL Lizenz)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell von László Németh und anderen (LGPL Lizenz)

  JNA von Todd Fast, Timothy Wall und andere (LGPL Lizenz)

  Swing-Layout 1.0.2 (LGPL Lizenz)

  Jmyspell 2. 1. 4 (LGPL Lizenz)

  JAXB 2.1.7 (GPLv2 + classpath Ausnahme)

==============================================================================
 5.  Ärger mit OmegaT? Hilfe benötigt?

Bevor ein Fehler gemeldet wird, stellen Sie sicher, dass Sie die Dokumentation gründlich eingesehen haben. Was Sie sehen, könnte stattdessen eine Eigenschaft von OmegaT sein, die Sie gerade entdeckt haben. Wenn Sie den Log von OmegaT log einsehen und Wörter wie "Error", "Warning", "Exception" oder "died unexpectedly" finden, dann haben Sie wahrscheinlich ein echtes Problem entdeckt (die log.txt Datei ist im Ordner Einstellungen des Benutzers, siehe das Handbuch für den Ort).

Als Nächstes von anderen Benutzern bestätigen lassen, was gefunden wurde, um sicher zu gehen, dass dies noch nicht berichtet wurde. Sie können den Fehlerbericht auch bei SourceForge verifizieren. Nur, wenn Sie sicher sind, dass Sie der Erste sind, der eine reproduzierbare Folge von Ereignissen gefunden haben, die zu etwas Unerwartetem führte, sollten Sie einen Fehler-Bericht einreichen. 

Jeder gute Fehler-Bericht braucht genau drei Dinge. 
  - nachvollziehbare Schritte,
  - was erwartet wurde und 
  - was stattdessen passierte.

Sie können Kopien von Dateien, Teile des Logs, screen shots, und irgendwas, das den Entwicklern beim Finden und Ausbessern des Fehlers behilflich ist, hinzufügen.

Um die Archive der Benutzergruppe zu durchsuchen, gehen Sie zu:
     http://groups.yahoo.com/group/OmegaT/

Um die Fehler-Berichtsseite zu durchsuchen und notfalls einen neuen Fehler-Bericht einzureichen, gehen Sie zu:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Um zu verfolgen, was mit Ihrem Fehlerbericht geschieht, möchten Sie sich vielleicht als Source Forge Benutzer anmelden. 

==============================================================================
6.   Details über die Ausgabe

Für ausführliche Information über Änderungen in dieser und allen vorherigen Ausgaben bitte die Datei 'changes.txt' anschauen.


==============================================================================