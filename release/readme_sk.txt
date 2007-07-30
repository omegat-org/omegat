Tento preklad vytvoril Martin Lukáč, copyright© 2007.


==============================================================================
  OmegaT 1.7.1, súbor Čítaj ma

  1.  Informácie o OmegaT
  2.  Čo je OmegaT?
  3.  Všeobecné poznámky o Jave a OmegaT
  4.  Príspevky do OmegaT
  5.  Máte s OmegaT problémy? Potrebujete pomoc?
  6.  Podrobnosti o vydaní

==============================================================================
  1.  Informácie o OmegaT


Najaktuálnejšie informácie o OmegaT môžete nájsť
(v angličtine, slovenčine, holandčine, portugalčine) na:
      http://www.omegat.org/omegat/omegat.html

Používateľská podpora, v používateľskej skupine na Yahoo (viacjazyčná), kde sa dajú prehľadávať
archívy bez prihlasovania:
     http://groups.yahoo.com/group/OmegaT/

Požiadavky na zlepšenia (v angličtine), na stránke SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Hlásenia chýb (v angličtine), na stránkach SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Čo je OmegaT?

OmegaT je nástroj na preklad podporovaný počítačom (CAT). Je slobodný, v tom zmysle,
že za jeho používanie nemusíte nič platiť, dokonca ani na
profesionálne používanie, a vo význame, že máte slobodu pre jeho modifikáciu a/alebo
šírenie pokiaľ rešpektujete používateľskú licenciu.

Hlavné vlastnosti OmegaT sú
  - schopnosť bežať na akomkoľvek operačnom systéme podporujúcom Javu
  - používanie akéhokoľvek platného TMX súboru ako prekladovej príručky
  - flexibilné segmentovanie viet (využívaním metódy podobnej SRX)
  - vyhľadávanie v projekte a v referenčných prekladových pamätiach
  - vyhľadávanie v akomkoľvek adresári vrátane súborov čitateľných pre OmegaT
  - vyhľadávanie približných prekladov
  - elegantné zaobchádzanie s projektami vrátane kompexných adresárových hierarchií
  - podpora pre slovníky (kontroly terminológie)
  - ľahko pochopiteľná dokumentácia a tutoriál
  - lokalizácia do množstva jazykov.

OmegaT podporuje súbory OpenDocument, súbory Microsoft Office (pomocou
OpenOffice.org ako filtra pre konverziu, alebo konverziou do HTML),
súbory OpenOffice.org alebo StarOffice, ako aj (X)HTML, lokalizačné súbory
Javy, obyčajné textové súbory a ďalšie.

OmegaT automaticky spracuje dokonca aj najkomplexnejšie hierarchie zdrojových adresárov,
pre prístup k všetkým podporovaným súborom, a vytvára cieľový adresár
s presne rovnakou štruktúrou, vrátane kópií akýchkoľvek nepodporovaných súborov.

Pre rýchly úvodný tutoriál, spustite OmegaT a prečítajte si Rýchly úvodný tutoriál, ktorý sa vám zobrazí.

Používateľská príručka je v balíčku, ktorý ste si práve stiahli, môžete ju zobraziť z
menu [Pomocník] po spustení OmegaT.

==============================================================================
 3. Inštalácia OmegaT

3.1 Všeobecné
OmegaT pre svoje spustenie vyžaduje mať na vašom systéme nainštalované prostredie 
Java Runtime Environment (JRE) verzie 1.4 alebo vyššej. OmegaT sa teraz štadnardne 
dodáva s prostredím Java Runtime Environment aby používatelia nemali problémy 
pri jeho výbere, získavaní a inštalácii. Používatelia Windows a Linux: ak ste si 
istí, že váš systém už má nainštalovanú vhodnú verziu 
JRE, môžete inštalovať verziu OmegaT bez JRE (to je 
označené v názve verzie, "Without_JRE"). Ak máte akékoľvek 
pochybnosti, odporúčame použiť "štandardnú" verziu, tj. s JRE. Toto 
je bezpečné pretože ak už aj je JRE nainštalované na vašom systéme, táto 
verzia s ním nebude v konflikte.
Používatelia Linuxu: všimnite si, že OmegaT nefunguje so slobodnými/open-source implementáciami 
Javy, ktoré sa dodávajú s mnohými distribúciami Linuxu (napríklad, 
Ubuntu), keďže tieto sú buď zastaralé alebo neúplné. Stiahnite si a nainštalujte 
Java Runtime Environment (JRE) od firmy Sun cez vyššie uvedený odkaz, alebo si stiahnite a 
nainštalujte balík OmegaT, ktorý obsahuje JRE (balík .tar.gz označený ako 
"Linux").
Používatelia Macu: JRE je už na Mac OS X nainštalované.
Linux na systémoch PowerPC: používatelia si budú musieť stiahnuť JRE od firmy IBM, keďže Sun 
neposkytuje JRE pre systémy PPC. V tomto prípade si ho stiahnite z:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Inštalácia
Pre inštaláciu OmegaT jednoducho vytvorte vhodný priečinok pre OmegaT (napr. C:\Program 
Files\OmegaT na Windows alebo /usr/local/lib na Linuxe). Skopírujte zip archív 
OmegaT do toho priečinka a rozbaľte ho tam.

3.3 Spustenie OmegaT
OmegaT sa dá spustiť niekoľkými spôsobmi.

* Používatelia Windows: dvojkliknutím na súbor OmegaT-JRE.exe, ak
používate verziu obsahujúcu priložené JRE, alebo ináč na OmegaT.exe.

* Dvojkliknutím na súbor OmegaT.bat. Ak vo vašom správcovi
súborov (Prieskumník Windows) vidíte súbor OmegaT ale nie OmegaT.bat, zmeňte
nastavenia tak aby sa zobrazovali prípony súborov.

* Dvojkliknutím na súbor OmegaT.jar. Toto bude fungovať iba ak je typ
súborov .jar na vašom systéme priradený aplikácii Java.

* Z príkazového riadku. Príkaz pre spustenie OmegaT je:

cd <priečinok kde sa nachádza súbor OmegaT.jar>

<názov a cesta spustiteľného súboru Java> -jar OmegaT.jar

(Spustiteľný súbor Java je súbor java na Linuxe a java.exe na Windows.
Ak je prostredie Java nainštalované na systémovej úrovni, úplnú cestu nie je potrebné zadávať.)

* Používatelia Windows: môžete pretiahnuť súbor OmegaT-JRE.exe, OmegaT.exe alebo
OmegaT.bat na pracovnú plochu alebo do menu Štart a urobiť tam na ne odkaz.

* Používatelia KDE v Linuxe: OmegaT môžete pridať do svojich menu nasledujúcim spôsobom:

Ovládacie centrum - Plocha - Panely - Menu - Upraviť K menu - Súbor - Nová položka/Nové 
podmenu.

Then, after selecting a suitable menu, add a submenu/item with File - New 
Submenu and File - New Item. Zadajte OmegaT ako názov novej položky.

V položke "Príkaz" použijte navigačné tlačidlo pre nájdenie vášho spúšťacieho skriptu 
OmegaT a vyberte ho. 

Kliknite na tlačidlo ikony (napravo od položiek Názov/Popis/Komentár) 
- Ďalšie ikony - Prehliadať, a presuňte sa do podpriečinka /images v priečinku 
aplikácie OmegaT. Vyberte ikonu OmegaT.png.

Nakoniec uložte zmeny cez Súbor - Uložiť.

* Pužívatelia GNOME v Linuxe: OmegaT môžete pridať do svojho panelu (horná lišta vo vrchnej časti 
obrazovky) nasledujúcim spôsobom:

Kliknite pravým tlačidlom na panel - Pridať nový spúšťač. Do položky "Názov" zadajte 
"OmegaT"; v položke "Príkaz" použijte navigačné tlačidlo pre nájdenie vášho spúšťacieho 
skriptu OmegaT. Vyberte ho a potvrďte pomocou OK.

==============================================================================
 4. Príspevky do OmegaT

Ak chcete prispieť k vývoju OmegaT, spojte sa s vývojármi na:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Ak chcete pomôcť pri preklade používateľského rozhrania OmegaT, používateľskej príručky alebo iných príbuzných dokumentov,
prečítajte si:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

A prihláste sa do konferencie pre prekladateľov:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Pre akýkoľvek iný príspevok sa najprv prihláste do používateľskej skupiny na:
      http://tech.groups.yahoo.com/group/omegat/

A zistite čo sa deje vo svete OmegaT...

  OmegaT je pôvodnou prácou Keitha Godfreya.
  Marc Prior je koordinátorom projektu OmegaT.

Medzi predchádzajúcich prispievateľov patria:
(v abecednom poradí)

Do kódu prispeli
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk (vedúci vývojár)
  Henry Pijffers (vedúci vydávania)
  Benjamin Siband
  Martin Wunderlich

K lokalizácii prispeli
 Roberto Argus (brazílska portugalčina)
  Alessandro Cattelan (taliančina)
  Sabine Cretella (nemčina)
  Suzanne Bolduc (esperanto)
  Didier Briel (francúzština)
  Frederik De Vos (holandčina)
  Cesar Escribano Esteban (španielčina)
  Dmitri Gabinski (bieloruština, esperanto, a ruština)
  Takayuki Hayashi (japončina)
  Jean-Christophe Helary (francúzština a japončina)
  Yutaka Kachi (japončina)
  Dragomir Kovacevic (srbochorvátčina)
  Elina Lagoudaki (gréčtina)
  Martin Lukáč (slovenčina)
  Ahmet Murati (albánčina)
  Samuel Murray (afrikánčina)
  Yoshi Nakayama (japončina)
  Claudio Nasso (taliančina)
  David Olveira (portugalčina)
  Ronaldo Radunz (brazílska portugalčina)
  Thelma L. Sabim (brazílska portugalčina)
  Juan Salcines (španielčina)
  Pablo Roca Santiagio (španielčina)
  Sonja Tomaskovic (nemčina)
  Karsten Voss (poľština)
  Gerard van der Weyde (holandčina)
  Martin Wunderlich (nemčina)
  Hisashi Yanagida (japončina)
  Kunihiko Yokota (japončina)
  Erhan Yükselci (turečtina)
  Mikel Forcada Zubizarreta (katalánčina) 

Ďalej prispeli
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (vedúci dokumentácie)
  Samuel Murray
  Marc Prior (vedúci lokalizácie)
  a mnoho, mnoho ďalších veľmi nápomocných ľudí

(Ak si myslíte, že ste významne prispeli k Projektu OmegaT 
ale svoje meno nevidíte v týchto zoznamoch, pokojne nás môžete kontaktovať.)

OmegaT používa nasledujúce knižnice:
  HTMLParser od Somika Rahu, Derricka Oswalda a iných (licencia LGPL).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter od Steva Roya (licencia LGPL).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework od VLSolutions (licencia CeCILL).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  Máte s OmegaT problémy ? Potrebujete pomoc ?

Pred ohlásením akejkoľvek chyby sa uistite, že ste dôkladne skontrolovali
dokumentáciu. To čo vidíte môže byť vlastnosťou OmegaT
ktorú ste práve objavili. Ak sa pozriete do logu OmegaT a vidíte slová ako
"Error" ("Chyba"), "Warning" ("Varovanie"), "Exception" ("Výnimka"), alebo "died unexpectedly" ("neočakávané ukončenie") potom ste na stope
niečomu (log.txt sa nachádza v adresári predvolieb používateľa, jeho
umiestnenie nájdete v príručke).

Ďalšia vec, ktorú urobíte je overiť si to čo ste našli u ostatných používateľov, aby ste sa uistili,
že toto už niekedy nebolo hlásené. Môžete si to overiť aj na stránke hlásení chýb na
SourceForge. Iba keď ste si istí, že ste prvý kto našiel nejakú
zopakovateľnú sekvenciu udalostí, ktorá spustila niečo čo sa nemalo
stať, tak by ste mali podať hlásenie o chybe.

Každé dobré hlásenie o chybe potrebuje presne tri veci.
  - Kroky, ktoré treba zopakovať,
  - Čo ste čakali, že uvidíte, a
  - Čo ste videli namiesto toho.

Môžete pridať kópie súborov, časti logu, snímky obrazovky, čokoľvek o čom
si myslíte, že pomôže vývojárom nájsť a opraviť vašu chybu.

Archívy používateľskej skupiny môžete prehliadať na:
     http://groups.yahoo.com/group/OmegaT/

Prehliadať stránku hlásení o chybách a v prípade potreby pridať nové hlásenie o chybe môžete na:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Aby ste boli informovaní o tom čo sa deje s vaším hlásením o chybe môžete sa zaregistrovať
ako používateľ Source Forge.

==============================================================================
6.   Podrobnosti o vydaní

Podrobné informácie o zmenách v tomto
a všetkých predchádzajúcich vydaniach nájdete v súbore 'changes.txt'.

Podporované formáty súborov:
  - Obyčajný text
  - HTML a XHTML
  - HTML Help Compiler (HCC)
  - OpenDocument / OpenOffice.org
  - Zdrojové balíčky Java (.properties)
  - INI súbory (súbory s pármi kľúč=hodnota v akomkoľvek kódovaní)
  - PO súbory
  - Formát dokumentačných súborov DocBook
  - Súbory Microsoft OpenXML
  - jednojazyčné Okapi súbory XLIFF

Zmeny v jadre:
  -

Nové vlastnosti používateľského rozhrania (v porovnaní so sériou OmegaT 1.6):
  -

==============================================================================

