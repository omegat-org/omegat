@@ÜBERSETZUNGSNOTIZ@@

==============================================================================
  OmegaT 2.0, LiesMich Datei

  1.  Information über OmegaT
  2.  Was ist OmegaT?
  3.  Installation von OmegaT
  4.  Beiträge zu OmegaT
  5.  Ärger mit OmegaT? Hilfe benötigt?
  6.  Details zur Ausgabe

==============================================================================
  1.  Information über OmegaT


Die aktuellste Info über OmegaT befindet sich hier: 
      http://www.omegat.org/

Unterstützung der Benutzer (mehrsprachig), mit Archiven, die auch ohne Abonnement zugänglich sind:
     http://groups.yahoo.com/group/OmegaT/

Anfragen für Verbesserungen (Englisch), auf der SourceForge Seite:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Fehlerberichte (in Englisch), auf der SourceForge Seite:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Was ist OmegaT?

OmegaT ist ein Werkzeug für die Computer unterstützte Übersetzung (CAT). Es ist frei, das heißt, für die Benutzung, auch für  professionelle Zwecke, wird kein Entgelt verlangt; darüber hinaus kann jeder Benutzer, so lange die Benutzerlizenz respektiert wird, OmegaT  ändern und/oder weiter verteilen.

Hauptmerkmale von OmegaT sind:
  - es läuft in allen Betriebssystemen, die Java unterstützen. 
  - es benutzt jede gültige TMX Dateien als Referenz für die Übersetzungen
  - es wird eine flexible Satz-Segmentierung nach einer SRX-ähnlichen Methode benutzt
  - Suche nach den entsprechenden Segmenten erfolgt im Projekt selber und in den Referenz-TMs
  - es wird in allen Ordnern nach Dateien mit unterstützten Formaten gesucht 
  - unscharfe Treffer
  - clevere Behandlung von Projekten einschließlich komplizierter Hierarchien der Ordner
  - Unterstützung von Glossaren (Kontrolle der Terminologie) 
  - Unterstützung für OpenSource on-the-fly Rechtschreibprüfung
  - Unterstützung für StarDict Wörterbücher
  - Unterstützung der Google Translate maschinellen Übersetzungsdienste
  - klare und umfassende Dokumentation und Anleitung
  - Lokalisierung in mehreren Sprachen

Im Ausgangszustand unterstützt OmegaT folgende Dateiformate:

- Reine Text-Dateiformate

  ASCII Text (.txt, etc. )
  - Kodierter Text (*.UTF8)
  - Java Quellbündel (.properties)
  PO Dateien (.po)
  INI ('Schlüssel = Wert') Dateien (.ini)
  - DTD Dateien (*.DTD)
  - DocuWiki Dateien (*.txt)
  - SubRip Untertitel Dateien (*.srt)
  - Magento CE Locale CSV (*.csv)

- Tagged Textdateiformate

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - HTML Help Compiler (*.hhc, *.hhk)
  - DocBook (*.xml)
  - monolingual XLIFF (*.xlf, *.xliff, *.sdlxliff)
  - QuarkXPress CopyFlowGold (*.tag, *.xtg)
  - ResX files (*.resx)
  - Android resource (*.xml)
  - LaTex (*.tex, *.latex)
  - Hilfe (*.xml) und Handbuch (*.hmxp) Dateien
  - Typo3 LocManager (*.xml)
  - WiX Localization (*.wxl)
  - Iceni Infix (*.xml)
  - Flash XML export (*.xml)
  - Wordfast TXML (*.txml)
  - Camtasia für Windows (*.camproj)
  - Visio (*.vxd)

OmegaT kann angepasst werden, um andere Dateiformate zu unterstützen.

OmegaT wird sogar die hoch komplizierten Quellordner-Hierarchien automatisch analysieren, um auf alle unterstützten Dateien zuzugreifen und sie in einen Ziel-Ordner mit genau derselben Struktur einschließlich Kopien irgendwelcher nicht unterstützten Dateien zu erzeugen. 

Für eine schnelle Anfangseinführung OmegaT starten und die Schnellstartanleitung lesen.

Das Benutzerhandbuch ist im Paket, das Sie gerade herunter luden; Sie können nach dem Starten von OmegaT darauf im Menü [Hilfe] zugreifen. 

==============================================================================
 3. Installation von OmegaT

3.1 Allgemein
Um OmegaT benutzen zu können, muss auf dem System Java Runtime Environment (JRE) Version 1.5 oder höher installiert sein. Es gibt nun OmegaT Pakete mit dem 
Java Runtime Environment um den Benutzern den Ärger mit 
der Auswahl, dem Erwerben und der Installation zu ersparen. 

Wenn Sie Java schon besitzen, dann gibt es eine Möglichkeit die aktuelle Version von OmegaT zu installieren, indem Sie Java Web Start benutzen. 
Laden Sie zu diesem Zweck folgende Datei herunter und führen sie dann aus:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Damit wird die richtige Umgebung für Ihren Computer und die Anwendung selber beim ersten Durchlauf installiert. Spätere Aufrufe müssen nicht online erfolgen.

Während der Installation kann es je nach Betriebssystem einige Sicherheitswarnungen erhalten. Das Zertifikat ist eigenhändig von "Didier Briel" unterschrieben. 
Die Befugnisse, die Sie der Version erteilen (die als "unbegrenzter Zugriff auf Ihren Computer" heißen können) sind identisch mit den Rechten, die sie der lokalen Version beim Prozess der Installation geben und später beschrieben werden: sie erlauben einen Zugang zur Festplatte des Computers. Anschließende Klicks auf OmegaT.jnlp 
wird nach Aktualisierungen suchen. Falls Sie online sind, installieren Sie sie, wenn es welche gibt und starten Sie dann OmegaT. 

Alternative Wege und Möglichkeiten zum Herunterladen und Installieren von OmegaT werden unten aufgezeigt. 

Für Windows und Linux Benutzer: wenn Sie sicher sind, dass in Ihrem System eine passende Version von JRE installiert ist, können sie OmegaT ohne JRE installieren (darauf wird im Namen der Version mit "Without_JRE" hingewiesen). 
Wenn Sie unsicher sind, empfehlen wird, dass Sie die Version mit JRE benutzen. Das ist sicher, denn selbst wenn das JRE in Ihrem System schon installiert ist, wird es nicht damit in Konflikt geraten.

Linux Nutzer: 
OmegaT wird mit den freien/open-source Java Anwendungen laufen, die mit vielen Linux Auslieferungen (zum Beispiel Ubuntu) als Paket kommen, aber Sie können Bugs haben, Probleme bei der Anzeige oder fehlende Features. Wir empfehlen daher, 
dass Sie entweder das Oracle Java Runtime Environment (JRE) oder OmegaT Paket gebündelt mit JRE herunter und installieren es (das .tar.gz Bündel gekennzeichnet als "Linux") herunterladen und installieren.  Wenn Sie eine Java Version auf der Systemebene installieren, müssen Sie entweder sicherstellen, dass es in Ihrem Pfad zur Ausführung liegt oder es ausdrücklich aufrufen, wenn Sie OmegaT aufrufen. Wenn Sie mit Linux nicht sehr vertraut sind, empfehlen wird, dass Sie die OmegaT Version mit JRE installieren. Das ist sich, 
denn dieses lokale JRE wird mit anderen JREs, die auf Ihrem System installiert sind, nicht in Konflikt geraten.

Mac Nutzer: 
Das JRE ist schon installiert auf dem Mac OS X vor Mac OS X 10.7 
(Lion). Nutzer von Lion werden bei ersten Aufruf einer Anwendung, die Java benötigt, vom System aufmerksam gemacht und das System wird sie gegebenenfalls automatisch herunterladen und installieren.

Linux auf PowerPC Systemen: Benutzer werden IBM's JRE herunterladen müssen, da Sun kein JRE für PPC Systeme bereitstellt. Herunterladen in dem Fall von:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installation
* Windows Benutzer: Einfach das Installationsprogramm starten. Wenn Sie wünschen, kann das Installationsprogramm Verknüpfungen zum Start von OmegaT anlegen.

* Linux Nutzer:
Platzieren Sie das Archiv in irgendeinen angemessenen Ordner und entpacken sie es; OmegaT ist dann bereit für den Start. Sie können jedoch geschicktere und benutzerfreundlichere Installation erreichen, wenn Sie das Installationsskript (linux-install.sh) benutzen. Um dieses Skript zu nutzen, öffnen Sie ein Terminal (Konsole), wechseln zu dem Ordner mit OmegaT.jar und dem Skript linux-install.sh und führen das Skript mit ./linux-install.sh aus.

* Mac Nutzer:
Kopieren Sie das OmegaT.zip Archiv an einen geeigneten Ort und entpacken Sie es dort, um einen Ordner zu haben, in dem die Index Datei für die HTML Dokumentation und OmegaT.app, die Anwendungsdatei ist.

* Andere (z.B., Solaris, FreeBSD: 
Um OmegaT zu installieren legen Sie einen geeigneten Ordner für OmegaT an. Kopieren Sie das
OmegaT zip oder tar.bz2 Archiv in diesen Ordner und entpacken es hier.

3.3 OmegaT starten
Starten Sie OmegaT folgendermaßen.

* Windows Nutzer: 
Wenn Sie während der Installation eine Verknüpfung auf dem Desktop angelegt haben, 
führen Sie einen Doppelklick auf die Verknüpfung aus. Alternativ klicken Sie doppelt auf OmegaT.exe. Wenn Sie im Dateimanager (Windows Explorer) die Datei OmegaT sehen können, aber nicht OmegaT.exe, dann ändern Sie die Einstellungen so, dass Dateierweiterungen gezeigt werden. 

* Linux Nutzer:
Wenn Sie das mitgelieferte Installationsskript benutzt haben, sollten Sie in der Lage sein OmegaT mit:
Alt+F2
und dann:
omegat starten können

* Mac Nutzer:
Machen Sie einen Doppelklick auf die Datei OmegaT.app.

* Von Ihrem Dateimanager (alle Systeme):
Doppelklick auf die Datei OmegaT.jar. Das geht nur, wenn auf dem System der .jar Dateityp mit Java assoziiert ist. 

* Von der Befehlszeile (alle Systeme): 
Der Befehl zum Starten von OmegaT ist:

cd <Ordner mit der Datei OmegaT.jar>

<Name und Pfad für die ausführbare Java Datei>-jar OmegaT.jar

(Die ausführbare Java Datei ist java unter Linux und java.exe unter Windows.
Ist Java auf Systemebene installiert und im Pfad des Befehls vorhanden ist, muss der volle Pfad nicht angegeben werden.)

Anpassen der Erfahrung, OmegaT zu starten:

* Windows Benutzer: Das Installationsprogramm kann Verknüpfungen im Startmenü, auf dem Desktop und im Schnellstartbereich erzeugen. Sie können manuell mit drag-and-drop 
OmegaT.exe ins Startmenü, auf den Desktop oder in den Schnellstartbereich ziehen, um darauf zu verlinken.

* Linux Nutzer:
Für eine benutzerfreundlichere Art OmegaT zu starten, können Sie das gelieferte Kaptain 
Skript benutzen (omegat.kaptn). Um das Skript zu benutzen, müssen Sie erst
Kaptain installieren. Sie können das Startskript für Kaptain mit
Alt+F2
omegat.kaptn ausführen

Mehr Informationen zum Kaptain Skript und dem Hinzufügen von weiteren Menüelementen und Start Ikonen unter Linux, wenden Sie sich an OmegaT on Linux HowTo.

Mac Nutzer:
Ziehen Sie OmegaT.app auf Ihr Dock oder die tool bar im Suchfenster um in der Lage zu sein, es von jedem Ort aus zu starten. Sie können es auch 
im Spotlight Suchfeld aufrufen.

==============================================================================
 4. Beteiligung am OmegaT Projekt

Um an der OmegaT Entwicklung mitzuwirken, kann man mit den Entwicklern wie Kontakt aufnehmen unter:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Um die OmegaT Benutzeroberfläche, das Handbuch oder andere Dokumente zu übersetzen, bitte lesen:
      
      http://www.omegat.org/en/translation-info.html

und auf der Liste der Übersetzer eintragen:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Um in andere Weise etwas beizutragen, treten Sie erst der Nutzergruppe bei unter:
      http://tech.groups.yahoo.com/group/omegat/

und bekommen Sie ein Gefühl dafür, was in der OmegaT Welt so vor sich geht...

  OmegaT ist die ursprüngliche Arbeit von Keith Godfrey.
  Marc Prior ist der Koordinator des OmegaT Projektes. 

Vorherige Mitwirkende schließen ein:
(in alphabetischer Ordnung)

Code wurde beigesteuert von 
  Zoltan Bartko
  Volker Berlin
  Didier Briel (Entwicklungsmanager)
  Kim Bruning
  Alex Buloichik (führender Entwickler)
  Sandra Jean Chua
  Thomas Cordonnier
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
  Thomas Huriaux
  Hans-Peter Jacobs
  Kyle Katarn
  Ibai Lakunza Velasco
  Guido Leenders
  Aaron Madlon-Kay
  Fabián Mandelbaum
  John Moran
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Briac Pilpré
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Yu Tang
  Rashid Umarov  
  Antonio Vilei
  Martin Wunderlich
  Michael Zakharov

Andere Beiträge von
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (Lokalisierungsmanager)
  Vito Smolej (Manager der Dokumentation)
  Samuel Murray
  Marc Prior 
  und viele, viele andere sehr hilfsbereite Personen

(Wenn Sie der Meinung sind, zum OmegaT Projekt bedeutende Beiträge geleistet zu haben, aber Ihren Namen auf den Listen nicht sehen, treten Sie einfach in Kontakt mit uns.)

OmegaT verwendet die folgenden Bibliotheken:
  HTMLParser 1.6 von Somik Raha, Derrick Oswald und andere (LGPL Lizenz)
  MRJ Adapter 1.0.8 von Steve Roy (LGPL Lizenz)
  VLDocking Framework 2.0.6d von VLSolutions (CeCILL Lizenz)
  Hunspell von László Németh und anderen (LGPL Lizenz)
  JNA von Todd Fast, Timothy Wall und andere (LGPL Lizenz)
  Swing-Layout 1.0.2 (LGPL Lizenz)
  Jmyspell 2. 1. 4 (LGPL Lizenz)
  SVNKit 1.7.5 (TMate Lizenz)
  Sequence Library (Sequence Library Lizenz)
  ANTLR 3.4 (ANTLR 3 Lizenz)
  SQLJet 1.1.3 (GPL v2)
  JGit (Eclipse Distribution Lizenz)
  JSch (JSch Lizenz)
  Base64 (public domain)
  Diff (GPL)
  JSAP (LGPL)
  orion-ssh2-214 (Orion SSH für Java Lizenz)

==============================================================================
 5.  Ärger mit OmegaT? Hilfe benötigt?

Bevor ein Fehler gemeldet wird, stellen Sie sicher, dass Sie die Dokumentation gründlich eingesehen haben. Was Sie sehen, könnte stattdessen eine Eigenschaft von OmegaT sein, die Sie gerade entdeckt haben. Wenn Sie den Log von OmegaT log einsehen und Wörter wie "Error", "Warning", "Exception" oder "died unexpectedly" finden, dann haben Sie wahrscheinlich ein echtes Problem entdeckt (die log.txt Datei ist im Ordner Einstellungen des Benutzers, siehe das Handbuch für den Ort).

Als Nächstes von anderen Benutzern bestätigen lassen, was gefunden wurde, um sicher zu gehen, dass dies noch nicht berichtet wurde. Sie können den Fehlerbericht auch bei SourceForge verifizieren. Nur, wenn Sie sicher sind, dass Sie der Erste sind, der eine reproduzierbare Folge von Ereignissen gefunden haben, die zu etwas Unerwartetem führte, sollten Sie einen Fehler-Bericht einreichen. 

Jeder gute Fehler-Bericht braucht genau drei Dinge. 
  - nachvollziehbare Schritte,
  - was erwartet wurde und 
  - was Sie stattdessen sahen.

Sie können Kopien von Dateien, Teile des Logs, screen shots, und irgendwas, das den Entwicklern beim Finden und Ausbessern des Fehlers behilflich ist, hinzufügen.

Um die Archive der Benutzergruppe zu durchsuchen, gehen Sie zu:
     http://groups.yahoo.com/group/OmegaT/

Um die Fehler-Berichtsseite zu durchsuchen und notfalls einen neuen Fehler-Bericht einzureichen, gehen Sie zu:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Um zu verfolgen, was mit Ihrem Fehlerbericht geschieht, möchten Sie sich vielleicht als Source Forge Benutzer anmelden. 

==============================================================================
6.   Details zur Ausgabe

Für ausführliche Information über Änderungen in dieser und allen vorherigen Ausgaben bitte die Datei 'changes.txt' anschauen.


==============================================================================
