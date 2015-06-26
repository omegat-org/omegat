OmegaT er oversatt av Ingeborg Hauge Clementsen, copyright© 2015

==============================================================================
  OmegaT 3.0, Viktig-fil

  1.  Informasjon om OmegaT
  2.  Hva er OmegaT?
  3.  Installere OmegaT
  4.  Bidrag til OmegaT
  5.  Har du problemer med OmegaT? Trenger du hjelp?
  6.  Utgivelsesdetaljer

==============================================================================
  1.  Informasjon om OmegaT


Oppdatert informasjon om OmegaT finner du her
      http://www.omegat.org/

Brukerstøtte i Yahoo-brukergruppen (flerspråklig). Du kan søke i arkivene uten å være medlem:
     http://tech.groups.yahoo.com/group/OmegaT/

Forespørsler om forbedringer (på engelsk) på SourceForge-siden:
     https://sourceforge.net/p/omegat/feature-requests/

Problemrapporter (på engelsk) på SourceForge-siden:
     https://sourceforge.net/p/omegat/bugs/

==============================================================================
  2.  Hva er OmegaT?

OmegaT er et CAT-verktøy (Computer Assisted Translation). Det er gratis; det betyr at du ikke må betale for å bruke programmet, til og med dersom du bruker det i jobbsammenheng, og du står fritt til å modifisere det og/eller gjendistribuere det så lenge du respekterer brukerlisensen.

OmegaTs hovedfunksjoner er:
  - det kan kjøres på et hvilket som helst operativsystem som støtter Java
  - det kan bruke enhver TMX-fil som oversettelsesreferanse
  - fleksibel setningsegmentering (OmegaT bruker en SRX-lignende metode)
  - det kan utføre søk i prosjektet og i oversettelsesminner som fungerer som referanser
  - det kan utføre søk i filer med støttede formater i hvilken som helst mappe 
  - tilnærmede søk
  - smart behandling av prosjekter, inkludert komplekse mappehierarkier
  - støtter glossarer (sjekking av terminologi) 
  - støtter fortløpende stavekontrollører med åpen kildekode
  - støtter StarDict-ordbøker
  - støtter Google Translates tjenester for maskinoversettelse
  - tydelig og grundig dokumentasjon og innføring
  - lokalisert på mange språk

OmegaT støtter de følgende filformater uten at du trenger å installere tilleggsprogrammer:

- Filformater i ren tekst

  - ASCII-tekst (.txt osv.)
  - Kodet tekst (*.UTF8)
  - Java ressurspakker (*.properties)
  - PO-filer (*.po)
  - INI (key=value)-filer (*.ini)
  - DTD-filer (*.DTD)
  - DocuWiki-filer (*.txt)
  - SubRip tittelfiler (*.srt)
  - Magento CE Locale CSV (*.csv)

- Filformater for kodet tekst

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - HTML Help Compiler (*.hhc, *.hhk)
  - DocBook (*.xml)
  - enspråklig XLIFF (*.xlf, *.xliff, *.sdlxliff)
  - QuarkXPress CopyFlowGold (*.tag, *.xtg)
  - ResX files (*.resx)
  - Android-ressurser (*.xml)
  - LaTex (*.tex, *.latex)
  - Hjelp- (*.xml) og manual- (*.hmxp) filer
  - Typo3 LocManager (*.xml)
  - WiX Localization (*.wxl)
  - Iceni Infix (*.xml)
  - Flash XML export (*.xml)
  - Wordfast TXML (*.txml)
  - Camtasia for Windows (*.camproj)
  - Visio (*.vxd)

OmegaT kan i tillegg tilpasses til å støtte andre filformater.

OmegaT analyserer automatisk selv de mest komplekse hierarkier for å få tilgang til alle støttede filer og oppretter en målmappe med samme struktur, inkludert kopier av eventuelle filer som ikke støttes.

For å få en rask innføring i OmegaT, starter du OmegaT og leser guiden som da vises.

Brukermanualen finner du i mappen du nettopp lastet ned. Du kan åpne den i Hjelp-menyen når du har startet OmegaT.

==============================================================================
 3. Installere OmegaT

3.1 Generelt
For å kunne kjøre OmegaT må Java Runtime Environment (JRE) versjon 1.5 eller nyere være installert i systemet ditt. OmegaT-pakker som inkluderer Java Runtime Environment er nå tilgjengelige for å spare brukerne for bryet med å velge, finne og installere det selv. 

Dersom du allerede har Java, er en måte å installere den nyeste versjonen av OmegaT å bruke Java Web Start. 
For å gjøre dette laster du ned og kjører denne filen:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Denne filen installerer både det korrekte miljøet for din datamaskin og programmet første gang den kjøres. Neste gang du bruker programmet, trenger du ikke internettilgang.

Avhenging av hvilket operativsystem du har, kan det hende du får flere sikkerhetsadvarsler mens programmet installeres. Sertifikatet er av "PnS Concept". 
Tillatelsene du gir til denne versjonen (som kan klassifiseres som "ubegrenset tilgang til datamaskinen") er identiske med de tillatelsene du gir den lokale versjonen (prosedyre for installasjon av lokal versjon finner du lenger ned): de gir tilgang til maskinens harddisk. Dersom du klikker på OmegaT.jnlp ved en senere anleding, vil den se etter oppdateringer og installere dem dersom de finnes (gitt at du har internettilgang), og så starte OmegaT.  

Alternative måter for å laste ned og installere OmegaT ser du nedenfor. 

Windows- og Linux-brukere: dersom du er sikker på at systemet ditt allerede har en passende versjon av JRE installert, kan du installere den versjonen av OmegaT som ikke inkluderer JRE (vises i navnet, "Without_JRE"). 
Dersom du er i tvil, anbefaler vi at du bruker den versjonen som inkluderer JRE. Dette er trygt; selv om du allerede har JRE installert i systemet ditt, vil ikke denne nye versjonen skape problemer.

Linx-brukere: OmegaT kjøres på den Javaen med åpen kildekode-implementering som følger med mange Linux-distribusjoner (som for eksempel Ubuntu), men du kan oppleve feil, visningsproblemer eller manglende funksjoner.
 Vi anbefaler derfor at du laster ned og installerer enten Oracle Java Runtime Environment (JRE) eller OmegaT-pakken som inneholder JRE (.tar.bz2) og som er merket "Linux". Dersom du installerer en versjon av Java på systemnivå, må du forsikre deg om at det er enten i kjørestien din eller at du henter den frem når du starter OmegaT. Dersom du ikke er godt kjent med Linux, anbefaler vi at du installerer en OmegaT-versjon som inneholder JRE. Dette er trygt, ettersom at en "lokal" JRE ikke vil påvirke andre JRE-er som er installert i systemet ditt.

Mac-brukere:
JRE er allerede installert på Mac OS X før Mac OS X 10.7 (Lion). Lion-brukere vil bli spurt av systemet første gang de kjører et program som trenger Java, og systemet vil til slutt laste ned Java og installere det automatisk.

Linux på PowerPC-systemer:
Brukere må laste ned IBMs JRE, ettersom at Sun ikke tilbyr et JRE for PPC-systemer. Last det ned her:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installasjon
*Windows-brukere:
Kjør installasjonsprogrammet. Om du ønsker det, kan installasjonsprogrammet opprette snarveier for kjøring av OmegaT.

*Linux-brukere:
Plasser arkivet i en passende mappe og pakk det ut; OmegaT er så klar til å kjøres. Du kan få en enklere og mer brukervennlig installasjon ved å bruke installasjonsskriptet (Linux-install.sh). For å bruke dette skriptet, åpner du et terminalvindu (konsoll), endrer mappen til den mappen som inneholder OmegaT.jar og skriptet linux-install.sh og kjører det med ./linux-install.sh.

*Mac-brukere:
Kopier OmegaT.zip-arkivet til en passende plassering og pakk det ut der. Da får du en mappe som inneholder en HTML-indeksfil for dokumentasjon og OmegaT.app, som er programfilen.

*Andre (f.eks Solaris, FreeBSD):
For å installere OmegaT, oppretter du en passende mappe for OmegaT. Kopier OmegaT.zip eller tar.bz2-filen til denne mappen og pakk den ut der.

3.3 Starte OmegaT
Start OmegaT på følgende måte.

*Windows-brukere:
Dersom du under installasjonen opprettet en snarvei på skrivebordet, dobbeltklikker du på denne. En annen måte er å dobbeltklikke på filen OmegaT.exe. Dersom du kan se filen OmegaT men ikke OmegaT.exe i filbehandleren din (Windows Explorer) må du endre innstillingene dine slik at filtypen vises.

*Linux-brukere:
Dersom du brukte installasjonsskriptet, burde du kunne starte OmegaT med 
Alt+F2
og så
omegat

Mac-brukere:
Dobbeltklikk på filen OmegaT.app.

*Fra filbehandleren din (alle systemer):
Dobbeltklikk på filen OmegaT.jar. Dette fungerer bare dersom filtypen .jar assossieres med Java i systemet ditt.

*Fra kommandolinjen (alle systemer):
Kommandoen for å starte OmegaT er:

cd <mappen der filen OmegaT.jar er>

<navn og sti til den kjørbare Java-filen> -jar OmegaT.jar

(Den kjørbare Java-filen er filen 'java' i Linux og 'java.exe' i Windows.
Dersom Java allerede er installert på systemnivå og er i kommandostien, er det ikke nødvendig å angi hele stien.)

Hvordan tilpasse oppstart av OmegaT:

*Windows-brukere:
Installasjonsprogrammet kan opprette snarveier i startmenyen, på skrivebordet og i hurtigstartområdet. Du kan også dra filen OmegaT.exe til startmenyen, skrivebordet eller hurtigstartområdet for å opprette en snarvei.

*Linux-brukere:
For en mer brukervennlig måte å starte OmegaT på, kan du bruke Kaptain-skriptet (omegat.kaptn). For å bruke dette skriptet på du først installere Kaptain. Du kan så kjøre Kaptains startskript med 
Alt+F2 
omegat.kaptn

For mer informasjon om Kaptain-skriptet og hvordan legge til menyelementer og kjøre ikoner i Linux, se OmegaTs Linux-guide.

Mac-brukere:
Dra OmegaT.app til docken din eller til verktøylinjen i Finder for å kunne starte programmet fra en hvilken som helst plassering. Du kan også kjøre programmet fra 
Spotlight-søkefeltet.

==============================================================================
 4. Hvordan bli involvert i OmegaT-prosjektet

For å ta del i utviklingen av OmegaT, ta kontakt med utviklerne her:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

For å oversette OmegaTs brukergrensesnitt, brukermanual eller andre relaterte dokumenter, les:
      
      http://www.omegat.org/en/howtos/localizing_omegat.php

Og meld deg inn i oversetterlisten:
      https://lists.sourceforge.net/lists/listinfo/omegat-l10n

For å bidra på andre måter, meld deg først inn i brukergruppen her:
      http://tech.groups.yahoo.com/group/omegat/

Og få en følelse av hva som skjer i OmegaTs verden...

  OmegaT er Keith Godfreys originale verk.
  Didier Briel er OmegaTs prosjektleder.

Tidligere bidragsytere inkluderer:
(i alfabetisk rekkefølge)

Disse har bidratt med koder
  Zoltan Bartko
  Volker Berlin
  Didier Briel
  Kim Bruning
  Alex Buloichik (hovedutvikler)
  Sandra Jean Chua
  Thomas Cordonnier
  Enrique Estévez Fernández
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
  Thomas Huriaux
  Hans-Peter Jacobs
  Kyle Katarn
  Piotr Kulik
  Ibai Lakunza Velasco
  Guido Leenders
  Aaron Madlon-Kay
  Fabián Mandelbaum
  Manfred Martin
  Adiel Mittmann
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

Andre bidrag fra
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (lokaliseringsmanager)
  Vincent Bidaux (dokumentasjonsmanager)
  Samuel Murray
  Marc Prior (webmaster)
  og mange, mange flere hjelpsomme folk

(Dersom du mener du har bidratt til OmegaT-prosjektet, men ikke ser navnet ditt på listene, ta gjerne kontakt med oss.)

OmegaT bruker følgende bibliotek:
  HTMLParser 1.6 av Somik Raha, Derrick Oswald m.fl. (LGPL-lisens)
  VLDocking Framework 3.0.4 (LGPL-lisens)
  Hunspell av László Németh m.fl. (LGPL-lisens)
  JNA av Todd Fast, Timothy Wall m.fl. (LGPL-lisens)
  Swing-Layout 1.0.4 (LGPL-lisens)
  Jmyspell 2.1.4 (LGPL-lisens)
  SVNKit 1.8.5 (TMate-lisens)
  Sequence Library (Sequence Library-lisens)
  ANTLR 3.4 (ANTLR 3-lisens)
  SQLJet 1.1.10 (GPL v2)
  JGit (Eclipse Distribution-lisens)
  JSch (JSch-lisens)
  Base64 (offentlig domene)
  Diff (GPL)
  trilead-ssh2-1.0.0-build217 (Trilead SSH-lisens)
  lucene-*.jar (Apache License 2.0)
  De engelske tokenisererne (org.omegat.tokenizer.SnowballEnglishTokenizer og
  org.omegat.tokenizer.LuceneEnglishTokenizer) bruker stoppord fra Okapi
(http://okapi.sourceforge.net) (LGPL-lisens)
  tinysegmenter.jar (Modified BSD-lisens)
  commons-*.jar (Apache-lisens 2.0)
  jWordSplitter (Apache License 2.0)
  LanguageTool.jar (LGPL-lisens)
  morfologik-*.jar (Morfologik-lisens)
  segment-1.4.1.jar (Segment-lisens)
  pdfbox-app-1.8.1.jar (Apache-lisens 2.0)
  KoreanAnalyzer-3x-120223.jar (Apache-lisens 2.0)
  SuperTMXMerge-for_OmegaT.jar (LGPL-lisens)
  groovy-all-2.2.2.jar(Apache-lisens 2.0)
  slf4j (MIT-lisens)

==============================================================================
 5.  Har du problemer med OmegaT? Trenger du hjelp?

Før du rapporterer en feil, forsikre deg om at du har sett grundig igjennom dokumentasjonen. Det du ser kan være et trekk ved OmegaT du nettopp har oppdaget, i stedet for en feil. Dersom du sjekker OmegaTs logg og ser ord som "Error", "Warning", "Exception" eller "died unexpectedly" har du mest sannsynlig oppdaget en feil (log.txt-filen finner du i mappen for brukerinnstillinger. Se manualen for plassering).

Det neste du må gjøre er å bekrefte det du har funnet med andre brukere for å forsikre deg om at ikke den samme feilen allerede har blitt rapportert. Du kan også sjekke feilrapportsiden på SourceForge. Du bør først sende en feilrapport når du er sikker på at du er den første som har funnet et gjenkallelig hendelsesforløp som utløste noe som ikke skal høre til å skje. 

Enhver god feilrapport må inneholde tre ting.
  - Steg for å gjenkalle,
  - Hva du forventet å se, og
  - Hva du så i stedet.

Du kan inkludere kopier av filer, deler av loggen, skjermdumper og alt annet du tror kan hjelpe utviklerne med å finne og fikse feilen.

For å søke i brukergruppens arkiver, gå til:
     http://tech.groups.yahoo.com/group/OmegaT/

For å søke i feilrapportsiden og sende inn en ny feilrapport om nødvendig, gå til:
     https://sourceforge.net/p/omegat/bugs/

For å kunne følge med på hva som skjer med feilrapporten din, bør du registrere deg som bruker på SourceForge.

==============================================================================
6.   Utgivelsesdetaljer

Vennligst se filen 'changes.txt' for detaljert informasjon om endringer i denne og alle tidligere utgaver.


==============================================================================
