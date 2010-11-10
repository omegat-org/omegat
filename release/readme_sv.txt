Denna översättning är gjord av Anders Warnqvist, copyright© 2010.

==============================================================================
  OmegaT 2.0, Read Me-fil

  1.  Information om OmegaT
  2.  Vad är OmegaT?
  3.  Installera OmegaT
  4.  Bidrag till OmegaT
  5.  Har OmegaT buggar? Behöver du hjälp?
  6.  Detaljer för versionen

==============================================================================
  1.  Information om OmegaT


Den mest uppdaterade informationen om OmegaT finns på
      http://www.omegat.org/

Användarsupport på den flerspråkiga Yahoo-gruppen, där arkiven är sökbara utan medlemskap:
     http://groups.yahoo.com/group/OmegaT/

Förslag på förbättringar (på engelska) på SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Buggrapportering (på engelska) på SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Vad är OmegaT?

OmegaT är ett CAT-verktyg (Computer-Assisted Translation). Programmet är fritt, vilket betyder att du inte behöver betala någonting för att använda det, inte ens för professionellt bruk. Du är också fri att ändra och/eller återdistributera det så länge som du respekterar användarlicensen.

OmegaT:s huvudsakliga funktioner är:
  - möjligheten att köras på alla operativsystem som stöder Java
  - användning av alla giltiga TMX-filer som översättningsreferens
  - flexibel meningssegmentering (genom en SRX-liknande metod)
  - sökning i projekt- och referensöversättningsminnen
  - sökning efter filer i stödda format i alla mappar 
  - luddiga träffar
  - smart hantering av projekt inklusive komplexa mapphierarkier
  - support för gloslistor (terminologikontroll) 
  - support för stavningskontroll med öppen källkod
  - support för StarDict ordböcker
  - support för maskinöversättning med Google översätt
  - tydlig och omfattande dokumentation och instruktion
  - lokalisering på ett flertal språk

OmegaT stöder följande format i grundutförande:
  - ren text
  - HTML och XHTML
  - HTML Help Compiler
  - OpenDocument/OpenOffice.org
  - Java resource bundles (.properties)
  - INI-filer (filer med key=value-par i valfri kodning)
  - PO-filer
  - DocBook dokument
  - Microsoft OpenXML-filer
  - Okapi monolingual XLIFF-filer
  - QuarkXPress CopyFlowGold
  - Undertextfiler (SRT)
  - ResX
  - Android resource
  - LaTeX
  - Typo3 LocManager
  - Help & Manual
  - Windows RC resources
  - Mozilla DTD
  - DokuWiki

OmegaT kan anpassas att stödja ytterligare filformat.

OmegaT kommer automatiskt analysera den mest komplexa mapphierarki för att tillgå alla filer som stöds, samt producera en målmapp med exakt samma struktur, inklusive kopior av eventuella filer som inte stöds.

För en snabb starthjälp, kör igång OmegaT och läs instruktionerna för Snabbstart som visas.

Användarmanualen finns i paketet du just laddat ner. Du kan nå det från [Hjälp]-menyn när du startat OmegaT.

==============================================================================
 3. Installera OmegaT

3.1 Allmänt
För att köra OmegaT krävs Java Runtime Environment (JRE) version 
1.5 eller högre. OmegaT kommer nu med Java Runtime Environment för att bespara användarna mödan att välja, skaffa och installera det. 

Om du redan har Java är det enklaste sättet att installera OmegaT att använda Java Web Start. 
För att göra detta, ladda ner följande fil och kör den:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Filen kommer att installera den rätta miljön på din dator och själva programmet första gången den körs. Senare användning måste inte ske online.

Under installationen, beroende på ditt operativsystem kan du få flera varningsmeddelanden. Certifikatet är självsignerat av "Didier Briel". 
Tillåtelserna du ger till den här versionen (vilket kan betecknas som 
"unrestricted access to the computer") är identiska till tillåtelserna du ger till den lokala versionen som installeras i en process som beskrivs nedan: de tillåter åtkomst till datorns hårddisk. Senare klick på OmegaT.jnlp kommer leta efter uppdateringar om du är online, och installera dem om det finns några, och sedan starta OmegaT. 

Alternativa sätt att ladda ner och installera OmegaT visas nedan. 

Användare av Windows och Linux: om du är säker på att ditt system redan har en stabil version av JRE installerad, kan du installera versionen av OmegaT utan JRE (detta är markerat i namnet på versionen; "Without_JRE"). 
Om du är tveksam rekommenderar vi att du använder dig av standardversionen, alltså med JRE. Detta är helt riskfritt, eftersom även om du redan har JRE installerat på ditt system, så kommer denna version inte störa den tidigare.

Linux: observera att OmegaT inte fungerar med fria Java versioner med öppen källkod som kommer med flera Linuxdistributioner (t.ex. Ubuntu) eftersom dessa antingen är föråldrade eller bristfälliga. Ladda ner och installera Suns Java Runtime environment (JRE) via länken ovan eller ladda ner och installera OmegaT paketet med JRE (.taz.gz-paketet märkt "Linux").

Mac: JRE finns installerat på Mac OS X.

Linux på PowerPC-system: användare kommer att behöva ladda ner IBM:s JRE, eftersom Sun inte tillhandahåller en JRE för PPC-system. Ladda i så fall ner från:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installation
* Windows: Kör installationsprogrammet. Om du vill kan installationsprogrammet skapa genvägar för att starta OmegaT.
* Mac: Öppna dmg-filen och dra sedan programmet till Programmappen. * Andra: För att installera OmegaT, skapa en lämplig mapp för OmegaT
(t.ex., /usr/local/lib on Linux). Kopiera OmegaT .zip- eller tar.gz-arkivet till denna mapp och packa upp det där.

3.3 Att köra OmegaT
OmegaT kan startas på flera sätt.

* Windows: dubbelklicka på filen OmegaT.exe. Om du kan se filen OmegaT i Utforskaren, men inte OmegaT.exe, ändra inställningarna så att filtilläggen visas.

* Dubbelklicka på filen OmegaT.jar. Detta kommer bara fungera om .jar filer är associerade med Java i ditt system.

* I kommandotolken. Kommandot för att köra OmegaT är:

cd <mapp där OmegaT.jar finns>

<namn och sökväg till Java-hanterbara filen> -jar OmegaT.jar

(Den Java-hanterbara filen är filen java på Linux och java.exe i Windows.
Om Java är installerat på systemnivå, behöver inte hela sökvägen anges.)

* Windows: Installationsprogrammet kan skapa genvägar åt dig på Skrivbordet och i snabbstartsområdet. Du kan också dra filen OmegaT.exe manuellt till startmenyn, skrivbordet eller snabbstartsområdet för att länka därifrån.

* Linux KDE: du kan lägga till OmegaT i dina menyer så här:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Sedan, efter att du valt en en lämplig meny, lägg till en undermeny/post med File - New 
Submenu and File - New Item. Ange OmegaT som namn på den nya posten.

Använd navigeringsknappen i "Command"-fältet för att hitta OmegaT:s startskript, och välj det. 

Klicka på ikonknappen (till höger om fälten Name/Description/Comment fields) 
- Other Icons - Browse, och navigera till undermappen /images OmegaT:s 
programmapp. Välj ikonen OmegaT.png.

Spara ändringarna med File - Save.

* Linux GNOME: du kan lägga till OmegaT till dina paneler (högst upp på skärmbilden) så här:

Högerklicka på panelen - Add New Launcher. Ange "OmegaT" i "Name"-fältet; använd navigeringsknappen i "Command"-fältet för att hitta OmegaT:s startskript. Välj det och bekräfta med OK.

==============================================================================
 4. Gå med i OmegaT-projektet

För att delta i utvecklingen av OmegaT, kontakta utvecklarna på:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

För att översätta OmegaT:s gränssnitt, användarmanual eller andra relaterade dokument, läs:
      
      http://www.omegat.org/en/translation-info.html

Prenumerera på översättarnas lista:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

För andra sätt att bidra, gå först med i användargruppen på:
      http://tech.groups.yahoo.com/group/omegat/

Och få en känsla av vad som händer runt OmegaT...

  OmegaT är skapat av Keith Godfrey.
  Marc Prior är koordinator för OmegaT-projektet.

Tidigare medarbetare inkluderar (i alfabetisk ordning): 

Kod har bidragits av
  Zoltan Bartko
  Volker Berlin
  Didier Briel (utvecklingsansvarig)
  Kim Bruning
  Alex Buloichik (huvudutvecklare)
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Ibai Lakunza Velasco
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Antonio Vilei
  Martin Wunderlich

Andra bidrag av
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (lokaliseringsansvarig)
  Vito Smolej (dokumentationsansvarig)
  Samuel Murray
  Marc Prior 
  och många, många fler oerhört hjälpsamma personer.

(Om du tycker att du har bidragit till OmegaT-projektet och inte ser ditt namn här, hör av dig till oss.)

OmegaT använder följande bibliotek:

  HTMLParser av Somik Raha, Derrick Oswald och andra (LGPL License)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 av Steve Roy (LGPL License)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 av VLSolutions (CeCILL License)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell av László Németh med flera (LGPL License)

  JNA av Todd Fast, Timothy Wall med flera (LGPL License)

  Swing-Layout 1.0.2 (LGPL License)

  Jmyspell 2.1.4 (LGPL License)

  JAXB 2.1.7 (GPLv2 + classpath exception)

==============================================================================
 5.  Har OmegaT buggar? Behöver du hjälp?

Innan du rapporterar en bugg, se till att du har kontrollerat dokumentationen noggrant. Det kan istället vara en del av OmegaT som du har upptäckt. Om du kontrollerar OmegaT-loggen och du ser ord som "Error", "Warning", "Exception" eller "died unexpectedly", så har du antagligen upptäckt ett genuint problem (log.txt ligger i mappen för användarinställningar, se manualen för dess plats).

Bekräfta vad du hittat med andra användare för att vara säker på att detta inte redan har rapporterats. Du kan verifiera buggrapporten på SourceForge också. Först när du är säker på att du är först med att hitta ett återskapningbart händelseförlopp som triggar något som inte skulle ha hänt, ska du skapa en buggrapport.

En bra buggrapport behöver tre saker.
  - Steg att återskapa,
  - Det du förväntade dig att se, och
  - Vad du såg istället.

Du kan bifoga kopior av filer, delar av loggen, skärmdumpar, allt du kan tänka dig skulle kunna hjälpa utvecklarna att hitta och fixa din bugg.

För att leta i användargruppens arkiv, gå till:
     http://groups.yahoo.com/group/OmegaT/

För att söka bland buggrapporter, och eventuellt skicka in en buggrapport, gå till:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

För att vara uppdaterad om vad som händer med din buggrapport, kan du registrera dig som Source Forge-användare.

==============================================================================
6.   Detaljer för versionen

Läs filen "changes.txt" för detaljerad information om ändringar i denna och alla tidigare versioner.


==============================================================================
