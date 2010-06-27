Oversat af Tine Haurum, copyright© 2010

==============================================================================
  OmegaT 2.0, Read Me filen

  1.  Information om OmegaT
  2.  Hvad er OmegaT?
  3.  Installering af OmegaT
  4.  Bidrag til OmegaT
  5.  Giver OmegaT dig problemer? Har du brug for hjœlp?
  6.  Detaljer om udgivelsen

==============================================================================
  1.  Information om OmegaT


De nyeste informationer om OmegaT kan findes på
      http://www.omegat.org/

Brugerstøtte, på Yahoo brugergruppe (flersproget), hvor arkiverne er søgbare uden abonnement:
     http://groups.yahoo.com/group/OmegaT/

Anmodninger om ekstraudstyr (på engelsk), på SourceForge hjemmeside:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Fejlrapporter (på engelsk) på SourceForge site:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Hvad er OmegaT?

OmegaT er et computer-assisteret-oversætte (CAT) værktøj. Det er gratis, det vil sige at du ikke skal betale noget for brugen, selv til erhvervsmæssigt brug, og du er fri til at ændre det og/eller videredistribuere det, så længe du overholder brugerlicensen.

OmegaT's vigtigste funktioner er:
  - fungerer på alle operativsystemer der understøtter Java
  - anvendelse af en gyldig TMX fil som en oversættelse reference
  - fleksibel sœtnings segmentering (ved hjælp af en SRX-lignende metode)
  - kan søge i projektet, og i oversættelseshukommelserne
  - kan søge efter filer, i understøttede formater, i  mapper 
  - fuzzy matching
  - Intelligent håndtering af projekter, herunder komplekse mappe hierarkier
  - Støtte til glossarier (terminologi kontrol) 
  - Støtte til OpenSource on-the-fly stavekontrol
  - Støtte til StarDict ordbøger
  - Støtte til Google Oversæt maskinoversættelse tjenester
  - klar og omfattende dokumentation og tutorial
  - localisation i en rœkke sprog

OmegaT understøtter uden videre følgende filformater:
  - Almindelig tekst
  - HTML and XHTML
  - HTML Hjælp Compiler
  - OpenDocument/OpenOffice.org
  - Java resource bundles (.properties)
  - INI files (files with key=value pairs of any encoding)
  - PO files
  - DocBook dokumentation filformat
  - Microsoft OpenXML filer
  - Okapi ensprogede XLIFF filer
  - QuarkXPress CopyFlowGold
  - Undertekst files (SRT)
  - ResX
  - Android resource
  - LaTeX

Det er muligt at tilpasse OmegaT til at benytte andre filformater.

OmegaT vil automatisk tolke selv de mest komplekse kilde mappe hierarkier, for at få adgang til alle understøttede filer, og producere en destinationsmappe med nøjagtig den samme struktur, herunder kopier af alle ikke-understøttede filer.

For en hurtig-start tutorial, start OmegaT og læs den viste Hurtig-start tutorial.

Brugermanualen er i den pakke, du lige har downloadet, du kan få adgang til den fra menuen [Hjælp] efter at have startet OmegaT.

==============================================================================
 3. Installering af OmegaT

3.1 Generelt: OmegaT kræver Java Runtime Environment (JRE) version 1.5 eller højere for at blive installeret på dit system. OmegaT leveres nu med Java Runtime Environment som standard for at spare brugerne besværet med at finde, vælge og installere. 

Hvis du allerede har Java, er den enkleste måde at installere den aktuelle version af OmegaT at bruge Java Web Start. 
Til dette formål hentes følgende fil:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Den vil installere det korrekte miljø for din computer og selve programmet ved første start. Senere brug behøver ikke at være online.

Under installationen, afhængigt af dit operativsystem, vil du måske modtage flere sikkerhedsadvarsler. Certifikatet er selvsigneret af "Didier Briel". 
De tilladelser, du giver denne version (som kan nævnes som en "ubegrænset adgang til computeren") er identiske med de tilladelser, du giver den lokale version, som er installeret ved en procedure, beskrevet senere: de giver adgang til harddisken af computeren. Efterfølgende klik på OmegaT.jnlp vil tjekke for eventuelle opgraderinger; hvis du er online installér dem, hvis der er nogen; og start derefter OmegaT. 

Alternative måder og midler til at downloade og installere OmegaT er vist nedenfor. 

Windows og Linux-brugere: Hvis du er sikker på, at dit system allerede har en passende version af JRE installeret, kan du installere  en version af OmegaT uden JRE (dette er angivet i navnet, "Without_JRE"). 
Hvis du er i tvivl, anbefaler vi, at du bruger "standard" version, dvs med JRE. Dette er sikkert, for selv om JRE allerede er installeret på dit system, vil denne version ikke have noget betydning.

Linux-brugere: Bemærk, at OmegaT ikke virker med de gratis/open-source Java-implementeringer, der er pakket med mange Linux-distributioner (for eksempel Ubuntu), da disse er enten forældede eller ufuldstændige. Hent og installer Sun's Java Runtime Environment (JRE) via linket ovenfor, eller hent og installer OmegaT pakken pakket med JRE (et .tar.gz bundt mærket "Linux").

Mac-brugere: JRE er allerede installeret på Mac OS X.

Linux på PowerPC-systemer: brugere skal downloade IBM's JRE, da Sun ikke leverer et JRE til PPC-systemer. Download i dette tilfælde fra:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installation * Windows-brugere: Du skal bare starte installationen af programmet. Hvis du ønsker, kan installationsprogrammet oprette genveje til at lancere OmegaT.
* Andre: Hvis du vil installere OmegaT, skal du blot oprette en passende mappe til OmegaT (eg, / usr / local / lib på Linux). Kopier OmegaT zip eller tar.gz arkiv til denne mappe og åbn.

3.3 Lancering af OmegaT: OmegaT kan lanceret på en række forskellige måder.

* Windows-brugere: ved at dobbeltklikke på filen OmegaT.exe. Hvis du kan se filen OmegaT men ikke OmegaT.exe i din Fil Manager (Windows Explorer), ændrer indstillingerne, så filtypenavne vises.

* Ved at dobbeltklikke på filen OmegaT.jar. Dette fungerer kun, hvis .jar filtypen er associeret med Java på dit system.

* Fra kommandolinjen. Kommandoen til at lancere OmegaT er:

cd <folder where the file OmegaT.jar is located>

<name and path of the Java executable file> -jar OmegaT.jar

(Java eksekverbar fil er filen Java på Linux og java.exe på Windows.
Hvis Java er installeret på systemniveau, behøver den fulde sti ikke at tastes ind.)

* Windows-brugere: Installations programmet kan oprette genveje til dig i menuen Start, på skrivebordet og i hurtig start området. Du kan også manuelt trække filen OmegaT.exe til startmenuen på skrivebordet eller hurtig start området og kæde derfra.

* Linux KDE-brugere: du kan tilføje OmegaT til følgende menuer:

Kontrol Center - Desktop - Panels - Menuer - Rediger K Menu - Filer - Ny konto/Ny undermenu.

Efter du har valgt en passende menu, tilføj en undermenu/post med Filer - Ny undermenu og Filer - Ny konto. Indtast OmegaT som navnet på den nye konto.

I "kommando"-feltet, kan du bruge navigations-knappen for at finde OmegaT start scriptet, og vælge det. 

Klik på ikon knappen (til højre for Navn/Beskrivelse/Kommentar felter) - Andre Ikoner - Gennemse, og naviger til /billede-undermappen i OmegaT programmappen. Vælg OmegaT.png ikonet.

Til slut gem ændringerne med Filer - Gem.

* Linux GNOME-brugere: du kan tilføje OmegaT til dit panel (bjælken øverst på skærmen) som følger:

Højreklik på panelet - Tilføj ny starter. Indtast "OmegaT" i "Navn" feltet; i "kommando" feltet, skal du bruge navigations-knappen for at finde din OmegaT start script. Vælg, og bekræft med OK.

==============================================================================
 4. Involver dig i OmegaT projektet

For at deltage i udviklingen af OmegaT kontakt udviklerne på:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

For at oversætte OmegaT brugergrænseflade, brugermanual eller andre relaterede dokumenter læs:
      
      http://www.omegat.org/en/translation-info.html

Og abonner på oversætternes liste:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

For andre former for bidrag, abonner først på brugergruppen på:
      http://groups.yahoo.com/group/OmegaT/

Og få en fornemmelse af, hvad der foregår i OmegaT verdenen ...

  OmegaT er Keith Godfreys oprindelige arbejde.
  Marc Prior er koordinator for OmegaT projektet.

Forrige bidragydere omfatter: (alfabetisk rækkefølge)

Kode er bidraget af:
  Zoltan Bartko
  Didier Briel (udgivelses manager)
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

Other contributions by
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (lokaliserings manager)
  Vito Smolej (dokumentations manager)
  Samuel Murray
  Marc Prior 
  og mange, mange flere meget hjælpsomme folk

(Hvis du mener, du har bidraget betydeligt til OmegaT projektet, men ikke kan se dit navn på listen, er du velkommen til at kontakte os.)

OmegaT bruger følgende biblioteker:

  HTMLParser by Somik Raha, Derrick Oswald and others (LGPL License)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 by Steve Roy (LGPL License)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 by VLSolutions (CeCILL License)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell by László Németh and others (LGPL License)

  JNA by Todd Fast, Timothy Wall and others (LGPL License)

  Swing-Layout 1.0.2 (LGPL License)

  Jmyspell 2.1.4 (LGPL License)

  JAXB 2.1.7 (GPLv2 + classpath exception)

==============================================================================
 5.  Giver OmegaT dig problemer? Har du brug for hjœlp?

Før du rapporterer en fejl, skal du sørge for at du grundigt har kontrolleret dokumentationen. Det du observerer kan være noget der er karakteristisk for OmegaT; som du bare lige har opdaget. Hvis du søger på OmegaTs log og du ser ord som "Fejl", "Advarsel", "undtagelse", eller "døde uventet", så har du sikkert opdaget et reelt problem (Log.txt er placeret i brugerindstillinger mappen; se manualen for dens placering).

Det nœste skridt er at bekræfte hvad du fandt med andre brugere; for at sikre at dette ikke allerede er indberettet. Du kan også kontrollere fejlrapportsiden på SourceForge. Først når du er sikker på du er den første til at have fundet en reproducerbar sekvens af begivenheder, der udløste noget, der ikke skulle ske, skal du indsende en fejlrapport.

Enhver god fejlrapport har behov for nøjagtig tre ting.
  - Reproducerbare trin,
  - Hvad du forventede at se, og
  - Hvad du så i stedet for.

Du kan tilføje kopier af filer, dele af loggen, skærmbilleder, eller andet som du tror vil hjælpe udviklerne med at finde og rette din fejl.

For at gennemse arkiverne i brugergruppen, skal du gå til:
     http://groups.yahoo.com/group/OmegaT/

For at gennemse fejlrapportsiden og om nødvendigt indsende en ny fejlrapport, gå til:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

For at følge med i hvad der sker med din fejlrapport kan du vœlge at registrere dig som Source Forge bruger.

==============================================================================
6.   Detaljer om udgivelsen

Se filen 'CHANGES.txt' for detaljerede oplysninger om ændringer i denne og alle tidligere versioner.


==============================================================================