Deze vertaling is het werk van Dick Groskamp, copyright© 2008.

OmegaT 1.8.0, LeesMij-bestand

  1.  Informatie over OmegaT
  2.  Wat is OmegaT?
  3.  Installeren van OmegaT
  4.  Bijdragen aan OmegaT
  5.  Heeft u problemen met OmegaT ? Heeft u hulp nodig?
  6.  Uitgavedetails

==============================================================================
  1.  Informatie over OmegaT


De meest recente informatie over OmegaT kan worden gevonden op
      http://www.omegat.org/

Gebruikersondersteuning op de Yahoogebruikersgroep(meertalig), waar zonder abonnement de archieven kunnen worden doorzocht:
     http://groups.yahoo.com/group/OmegaT/

Verzoeken tot verbeteringen (in het Engels) op de SourceForge-website:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Foutrapportages (in het Engels) op de SourceForge-website:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Wat is OmegaT?

OmegaT is een computer-assisterend vertaalprogramma (CAT). Het is vrij, hetgeen betekent dat u niets hoeft te betalen om het te gebruiken, zelfs niet voor professioneel gebruik en dat het u vrij staat om het aan te passen en opnieuw te distribueren, zo lang als u zich houdt aan de gebruikerslicentie.

OmegaT's belangrijkste mogelijkheden zijn:
  - de mogelijkheid om op elk besturingssysteem, dat Java ondersteunt, te kunnen worden uitgevoerd
  - gebruik van elk geldig TMX-bestand als een vertalingsverwijzing
  - flexibele zinsegmentering (door middel van een SRX-achtige methode)
  - zoekacties in het project en de vertalingsgeheugens waarnaar verwezen wordt
  - zoekacties in bestanden in ondersteunde formaten in elke map
  - fuzzy overeenkomsten
  - slimme afhandeling van projecten inclusief complexe mappenhiërarchiën
  - ondersteuning voor woordenlijsten (terminologie-controles)
  - heldere en uitgebreide documentatie en handleiding
  - vertaald in een aantal talen.

OmegaT ondersteunt standaard de volgende bestandsformaten:
  - platte tekst
  - HTML en XHTML
  - HTML Help Compiler
  - OpenDocument/OpenOffice.org
  - Java resource bundles (.properties)
  - INI-bestanden (bestenanden met key=value paren in elke codering)
  - PO-bestanden
  - DocBook documentatie bestandsformaat
  - Microsoft OpenXML-bestanden
  - Okapi ééntalige XLIFF-bestanden

OmegaT kan ook worden aangepast voor andere bestandsformaten.

OmegaT zal automatisch zelfs de meest complexe bronmap-hiërarchieën parsen om toegang te krijgen tot alle ondersteunde bestanden en een doelmap maken met precies dezelfde structuur, inclusief kopieën van niet-ondersteunde bestanden.

Voor een handleiding om snel te kunnen beginnen, start OmegaT en lees de weergegeven handleiding Snel starten.

De gebruikershandleiding zit in het pakket dat u zojuist heeft gedownload. U kunt die starten vanuit het menu [Help] na het starten van OmegaT.

==============================================================================
 3. Installeren van OmegaT

3.1 Algemeen
OmegaT vereist dat een Java Runtime Environment (JRE) versie 
1.4 of hoger is geïnstalleerd op uw systeem om uitgevoerd te kunnen worden. OmegaT is nu standaard voorzien van de Java Runtime Environment om gebruikers de moeite van het
selecteren, verkrijgen en installeren te besparen. 

Windows- en Linuxgebruikers: als u er van overtuigd bent dat op uw systeem al een passende versie van de JRE is geïnstalleerd, dan kunt u de versie van OmegaT zonder de JRE installeren (dit wordt aangegeven in de naam van de versie, namelijk"Without_JRE"). 
Als u twijfelt raden wij u aan om de "standaard" versie te gebruiken, 
dus met de JRE. Dat is veilig omdat, zelfs als er al een JRE op uw systeem geïnstalleerd is, deze versie die niet zal beïnvloeden.

Linuxgebruikers: let er op dat OmegaT niet werkt met de vrije/open-source Java-implementaties die zijn opgenomen in vele Linux-distributies (bijvoorbeeld Ubuntu), omdat ze ofwel gedateerd of incompleet zijn. Download en installeer 
Sun's Java Runtime Environment (JRE) via bovenstaande koppeling of download en installeer het OmegaTpakket met de gebundelde JRE (de .tar.gz bundel genaamd "Linux").

Mac-gebruikers: de JRE is al geïnstalleerd op Mac OS X.

Linux op PowerPC-systemen: gebruikers moeten IBM's JRE downloaden omdat Sun geen JRE voor PPC-systemen levert. Download in dit geval van:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Installatie
* Windows-gebruikers: Start eenvoudigweg het installatieprogramma. Het installatieprogramma kan snelkoppelingen maken om OmegaT te starten, als u dat wilt.
* Anderen: Maak eenvoudigweg een passende map aan voor OmegaT om OmegaT te installeren 
(bijvoorbeeld /usr/local/lib op Linux). Kopieer het OmegaT zip- of tar.gz-archief naar die map en pak het daar uit.

3.3 OmegaT starten
OmegaT kan op een aantal manieren worden gestart.

* Windows-gebruikers: door te klikken op het bestand OmegaT.exe. Als u het bestand OmegaT wel in uw bestandsbeheer (Windows Verkenner) kunt zien, maar niet OmegaT.exe, wijzig dan de instellingen zodat bestandsextensies worden weergegeven.

* Door te dubbelklikken op het bestand OmegaT.jar. Dit zal alleen werken als het bestandstype .jar
op uw systeem is gekoppeld aan Java.

* Vanaf de opdrachtregel. De opdracht om OmegaT te starten is:

cd <map waar het bestand OmegaT.jar is opgeslagen>

<naam en pad van het uitvoerbare Java-bestand> -jar OmegaT.jar

(Het uitvoerbare Java-bestand is het bestand java op Linux en java.exe op Windows.
Indien Java is geïnstalleerd op systeemniveau hoeft niet het volledige pad te worden ingevoerd.)

* Windows-gebruikers: Het installatieprogramma kan voor u snelkoppelingen maken in het start 
menu, op het bureablad en in het gebied voor snel starten. U kunt ook handmatig het bestand OmegaT.exe naar het startmenu, op het bureablad en in het gebied voor snel starten slepen om het van daaruit te koppelen.

* Linux KDE-gebruikers: u kunt OmegaT als volgt aan uw menu's toevoegen:

Controlecentrum - Bureaublad - Panelen - Menu's - Bewerken K Menu - Bestand - Nieuw item/Nieuw submenu

Dan, na het selecteren van een passend menu, voeg een submenu/item toe met <c0>Bestand - Nieuw submenu en Bestand - Nieuw item</c0>. Voer OmegaT in als de naam van het nieuwe item.

Gebruik, in het veld "Commando", de navigatieknop om uw OmegaT startscript te vinden en selecteer dat. 

Klik op de pictogrammenknop (rechts van de velden Naam/Beschrijving/Commentaar) - Andere pictogrammen - Bladeren en navigeer naar de submap /images in de OmegaT-toepassingsmap. Selecteer het picotgram OmegaT.png.

Sla tenslotte de wijzigingen op met Bestand - Opslaan.

* Linux GNOME-gebruikers: u kunt OmegaT als volgt toevoegen aan uw paneel (de balk boven in het scherm):

Rechtsklik op het paneel - Nieuwe starter. Voer "OmegaT" in in het veld "Naam", gebruik, in het veld "Commando", de navigatieknop om uw OmegaT-startscript te vinden. Selecteer dat en bevestig met OK.

==============================================================================
 4. Deelnemen aan het OmegaT-project

Om deel te nemen aan de ontwikkeling van OmegaT neemt u kontakt op met de ontwikkelaars via:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Voor het vertalen van OmegaT's gebruikersinterface, gebruikershandleiding of andere gerelateerde documenten leest u:
      
      http://www.omegat.org/en/translation-info.html

En abonneert u zich op de vertalerslijst:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Voor andere soorten van bijdragen abonneert u zich eerst op de gebruikersgroep op:
      http://tech.groups.yahoo.com/group/omegat/

en krijg daar een indruk van wat er gaande is in de wereld van OmegaT...

  OmegaT is van origine het werk van Keith Godfrey.
  Marc Prior is de coördinator van het OmegaT-project.

Eerdere bijdragen van:
(alfabetische volgorde)

Code is bijgedragen door
  Zoltan Bartko
  Didier Briel (uitgavemanager)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Thomas Huriaux
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

Andere bijdragen door
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary
  Vito Smolej (documentatiemanager)
  Samuel Murray
  Marc Prior (localisatiemanager)
  en veel, veel meer zeer hulpvaardige mensen

(Als u denkt dat u een significante bijdrage heeft geleverd aan het OmegaT-project, maar ziet u uw naam niet op deze lijst, neem dan alstublieft contact met ons op.)

OmegaT gebruikt de volgende bibliotheken:

  HTMLParser van Somik Raha, Derrick Oswald en anderen (LGPL-licentie)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter van Steve Roy (LGPL-licentie)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.0.6d van VLSolutions (CeCILL-licentie)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell 1.1.12 van László Németh en anderen (LGPL-licentie)

  JNA van Todd Fast, Timothy Wall en anderen (LGPL-licentie)

  Swing-Layout 1.0.2 (LGPL-licentie)

  Backport-util-concurrent (Publiek Domein)

  Retroweaver 2.0.1 (Retroweaver-licentie)

  Jmyspell 2.1.4 (LGPL-licentie)

==============================================================================
 5.  Heeft u problemen met OmegaT ? Heeft u hulp nodig?

Overtuig u ervan dat u de documentatie zorgvuldig heeft doorgenomen voordat u een probleem rapporteert. Wat u meemaakt kan in feite een karakteristiek iets van OmegaT zijn dat u net heeft ontdekt. Als u het OmegaT-log controleert  en u ziet worden zoals "Fout", "Waarschuwing", "Uitzondering" of "onverwacht afgebroken" dan heeft u waarschijnlijk een echt probleem ontdekt (het bestand log.txt is geplaatst in de map met gebruikersvoorkeuren. Zie de handleiding voor de juiste locatie).

Het volgende wat u zou moeten doen is dat wat u gevonden heeft bevestigd te krijgen van andere gebruikers om er zeker van te zijn dat dit al niet reeds gerapporteerd is. U kunt de foutrapportage ook verifiëren op de pagina van SourceForge. Alleen als u er zeker van bent dat u de eerste bent die een opnieuw te produceren reeks van gebeurtenissen heeft ontdekt die leidt tot iets wat niet zou moeten gebeuren zou u een foutrapportage moeten indienen.

Elk goed foutenrapport heeft exact drie dingen nodig.
  - Stappen om het te reproduceren,
  - Wat u verwachtte te zien en
  - Wat u in plaats daarvan zag.

U kunt kopieën van bestanden, delen uit het logbestand, schermafdrukken of iets waarvan u denkt dat dat de ontwikkelaars zal helpen bij het oplossen van uw probleem, toevoegen.

Bladeren door de archieven van de gebruikersgroep kunt u via:
     http://groups.yahoo.com/group/OmegaT/

Bladeren door de pagina met foutrapportages en, indien nodig, indienen van een nieuw foutenrapport via:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

U wilt zich misschien registreren als gebruiker van Sourceforge om te kunnen zien wat er met uw foutenrapport gebeurd.

==============================================================================
6.   Uitgavedetails

Bekijk alstublieft het bestand 'changes.txt' voor gedetailleerde informatie over wijzigingen in
deze en alle eerdere uitgaven.


==============================================================================