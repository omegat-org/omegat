==============================================================================
  OmegaT 1.6.1 súbor Čítaj ma

  1.  Informácie o OmegaT
  2.  Čo je OmegaT?
  3.  Všeobecné poznámky o Jave a OmegaT
  4.  Príspevky do OmegaT
  5.  Máte s OmegaT problémy? Potrebujete pomoc?
  6.  Podrobnosti o vydaní

==============================================================================
  1.  Informácie o OmegaT

Najaktuálnejšie informácie o OmegaT môžete nájsť na:
      http://www.omegat.org/omegat/omegat.html

Viac informácií môžete získať na nasledujúcich stránkach:

Používateľská podpora, v používateľskej skupine na Yahoo:
     http://groups.yahoo.com/group/OmegaT/
     Kde sú k dispozícii archívy na prehliadanie bez potreby prihlásenia.

Požiadavky na zlepšenia, na stránke SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Hlásenia chýb, na stránke SourceForge:
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
Javy alebo obyčajné textové súbory.

OmegaT automaticky spracuje dokonca aj najkomplexnejšie hierarchie zdrojových adresárov,
pre prístup k všetkým podporovaným súborom, a vytvára cieľový adresár
s presne rovnakou štruktúrou, vrátane kópií akýchkoľvek nepodporovaných súborov.

Pre rýchly úvodný tutoriál, spustite OmegaT a prečítajte si Rýchly úvodný tutoriál, ktorý sa vám zobrazí.

Používateľská príručka je v balíčku, ktorý ste si práve stiahli, môžete ju zobraziť z
menu [Pomocník] po spustení OmegaT.

==============================================================================
 3. Všeobecné poznámky o Jave a OmegaT

OmegaT vyžaduje aby na vašom systéme bolo nainštalované prostredie Java Runtime Environment verzie 1.4 alebo vyššej. Dá sa získať z:
    http://java.com

Používatelia Windows a Linux možno budú potrebovať nainštalovať prostredie Java ak ho ešte nemajú.
Projekt OmegaT tiež ponúka verzie obsahujúce prostredie Java. Používatelia MacOSX už majú
na svojich strojoch prostredie Java nainštalované.

Na správne nainštalovanom stroji, by ste mali byť schopní spustiť OmegaT
dvojkliknutím na súbor OmegaT.jar.

Po inštalácii prostredia java možno budete potrebovať zmeniť systémovú premennú path, aby
obsahovala adresár, kde sa aplikácia 'java' nachádza.

Používatelia Linuxu by mali dávať pozor na fakt, že OmegaT nebude fungovať so slobodnými/
open-source implementáciami prostredia Java, ktoré možno nájsť v mnohých distribúciách Linuxu (napríklad,
Ubuntu), keďže tieto sú buď zastaralé, alebo neúplné. Stiahnite si a
nainštalujte Java Runtime Environment (JRE) firmy Sun cez vyššie uvedený odkaz, alebo si stiahnite
a nainštalujte OmegaT dodávané s JRE (balík .tar.gz označený "Linux").

Keď používate Linux na systémoch PowerPC, používatelia si budú musieť stiahnuť JRE od firmy IBM, keďže
Sun neposkytuje JRE pre systémy PPC. Stiahnite si ho z:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Príspevky do OmegaT

Ak chcete prispieť k vývoju OmegaT, spojte sa s vývojármi na:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Ak chcete pomôcť pri preklade používateľského rozhrania OmegaT, používateľskej príručky alebo iných príbuzných dokumentov,
prečítajte si:
      http://www.omegat.org/omegat/translation-info.html

A prihláste sa do konferencie pre prekladateľov:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Pre akýkoľvek iný príspevok sa najprv prihláste do používateľskej skupiny na:
      http://groups.yahoo.com/group/OmegaT/

A zistite čo sa deje vo svete OmegaT...

  OmegaT je pôvodne prácou Keitha Godfreyho.
  Marc Prior je koordinátorom projektu OmegaT.

Medzi predchádzajúcich prispievateľov patria:
(v abecednom poradí)

Do kódu prispeli
  Kim Bruning
  Sacha Chua
  Maxym Mykhalchuk (súčasný vedúci vývojár)
  Henry Pijffers (vedúci vydávania verzie 1.6)
  Benjamin Siband

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
  Elina Lagoudaki (gréčtina)
  Martin Lukáč (slovenčina)
  Samuel Murray (afrikánčina)
  Yoshi Nakayama (japončina)
  David Olveira (portugalčina)
  Ronaldo Radunz (brazílska portugalčina)
  Thelma L. Sabim (brazílska portugalčina)
  Juan Salcines (španielčina)
  Pablo Roca Santiagio (španielčina)
  Karsten Voss (poľština)
  Gerard van der Weyde (holandčina)
  Martin Wunderlich (nemčina)
  Hisashi Yanagida (japončina)
  Kunihiko Yokota (japončina)
  Erhan Yukselci (turečtina)

Ďalej prispeli
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (súčasný vedúci dokumentácie)
  Samuel Murray
  Marc Prior (súčasný vedúci lokalizácie)
  a mnoho, mnoho ďalších veľmi nápomocných ľudí

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

Prehliadať stránku hlásení o chybách a v prípade potreeby pridať nové hlásenie o chybe môžete na:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Aby ste boli informovaní o tom čo sa deje s vaším hlásením o chybe sa môžete zaregistrovať
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

Zmeny v jadre:
  - Flexibilná (vetná) segmentácia
  - Filtre formátov súborov môžu byť vytvárané ako doplnky/zásuvné moduly (pluginy)
  - Prerobený kód s viacerými komentármi
  - Inštalátor pre Windows
  - Možno prekladať atribúty HTML značiek (tagov)
  - Plná kompatibilita s TMX 1.1-1.4b Level 1 (tj. Úroveň 1)
  - Čiastočná podpora TMX 1.4b Level 2 (tj. Úroveň 2)

Nové vlastnosti používateľského rozhrania (v porovnaní so sériou OmegaT 1.4):
  - Vyhľadávacie rozhranie prepísané s rozšírenou funkčnosťou
  - Hlavné rozhranie vylepšené pomocou odpájateľných okien

==============================================================================

