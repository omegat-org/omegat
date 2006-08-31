==============================================================================
  OmegaT 1.6.0 RC12 súbor Read Me

  1.  Informácie o OmegaT
  2.  Čo je OmegaT?
  3.  Všeobecné poznámky o Jave & OmegaT
  4.  Príspevky do OmegaT
  5.  Máte s OmegaT problémy? Potrebujete pomoc?
  6.  Podrobnosti o vydaní

==============================================================================
  1.  Informácie o OmegaT

Najaktuálnejšie informácie o OmegaT môžete nájsť na:
      http://www.omegat.org/omegat/omegat.html

Viac informácií môžete získať na nasledujúcich stránkach:

Používateľská podpora, v používateľskej skupine Yahoo:
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

Hlavné vlastnosti OmegaT's sú
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
OpenOffice.org ako filtra pre konverziu), súbory OpenOffice.org alebo StarOffice, ako
aj (X)HTML, lokalizačné súbory Javy alebo súbory v obyčajnom texte.

OmegaT automaticky spracuje dokonca aj najkomplexnejšie hierarchie zdrojových adresárov,
pre prístup k všetkým podporovaným súborom, a vytvára cieľový adresár
s presne rovnakou štruktúrou, vrátane kópií akýchkoľvek nepodporovaných súborov.

Pre rýchly úvodný tutoriál, spustite OmegaT a prečítajte si Rýchly úvodný tutoriál, ktorý sa vám zobrazí.

Používateľská príručka je v balíčku, ktorý ste si práve stiahli, môžete ju zobraziť z
menu [Pomocník] po spustení OmegaT.

==============================================================================
 3. Všeobecné poznámky o Jave & OmegaT

OmegaT vyžaduje aby na vašom systéme bolo nainštalované prostredie Java Runtime Environment verzie 1.4 alebo vyššej. Dá sa získať z:
      http://java.com

Používatelia Windows a Linux možno budú potrebovať nainštalovať Javu ak ju ešte nemajú.
Používatelia MacOSX už na svojich strojoch majú Javu nainštalovanú.

Na správne nainštalovanom stroji, by ste mali byť schopní spustiť OmegaT
dvojkliknutím na súbor OmegaT.jar.

Po inštalácii javy možno budete potrebovať zmeniť systémovú premennú path, aby
obsahovala adresár, kde sa aplikácia 'java' nachádza.

==============================================================================
 4. Príspevky do OmegaT

Aby ste prispeli k vývoju OmegaT, spojte sa s vývojármi na:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Pre preklad používateľského rozhrania OmegaT, používateľskej príručky alebo iných príbuzných dokumentov,
si prečítajte:
      http://www.omegat.org/omegat/translation-info.html

A prihláste sa do zoznamu prekladateľov:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Pre akýkoľvek iný príspevok sa najprv prihláste do používateľskej skupiny na:
      http://groups.yahoo.com/group/OmegaT/

A zistite čo sa deje vo svete OmegaT...

  OmegaT je pôvodne prácou Keitha Godfreyho.
  Marc Prior je koordinátorom projektu OmegaT.

Predchádzajúci prispievatelia sú:
(v abecednom poradí)

Do kódu prispeli
  Sacha Chua
  Kim Bruning
  Maxym Mykhalchuk (súčasný vedúci vývojár)
  Henry Pijffers
  Benjamin Siband

K lokalizácii prispeli
  Alessandro Cattelan (Taliančina)
  Sabine Cretella (Nemčina)
  Cesar Escribano Esteban (Španielčina)
  Dmitri Gabinski (Bieloruština, Esperanto, a Ruština)
  Jean-Christophe Helary (Francúzština)
  Juan Salcines (Španielčina)
  Pablo Roca Santiagio (Španielčina)
  Martin Wunderlich (Nemčina)
  Hisashi Yanagida (Japončina)
  Yoshi Nakayama (Japončina)
  Takayuki Hayashi (Japončina)
  Kunihiko Yokota (Japončina)
  Yutaka Kachi (Japončina)

Ďalej prispeli
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (súčasný vedúci dokumentácie)
  Samuel Murray
  Marc Prior (súčasný vedúci lokalizácie)
  a mnoho, mnoho ďalších veľmi nápomocných ľudí

OmegaT používa nasledujúce knižnice:
  HTMLParser od Somika Rahu, Derricka Oswalda a iných.
  Steve Roy MRJ Adapter.
  InfoNode Docking Windows framework, GPL version.

==============================================================================
 5.  Máte s OmegaT problémy? Potrebujete pomoc?

Pred ohlásením akejkoľvek chyby sa uistite, že ste dôkladne skontrolovali
dokumentáciu. To čo vidíte môže byť vlastnosťou OmegaT
ktorú ste práve objavili. Ak sa pozriete do logu OmegaT a vidíte slová ako
"Error" ("Chyba"), "Warning" ("Varovanie"), "Exception" ("Výnimka"), alebo "died unexpectedly" ("neočakávané ukončenie") potom ste na stope
niečomu (log.txt sa nachádza v adresári predvolieb používateľa, jeho
umiestnenie nájdete v príručke).

Ďalšia vec, ktorú urobíte je overiť si to čo ste našli u ostatných používateľov, aby ste sa uistili,
že toto už niekedy nebolo hlásené. Môžete si to overiť na stránke hlásení chýb tiež na
SourceForge. Iba keď ste si istí, že ste prvý kto našiel nejakú
zopakovateľnú sekvenciu udalostí ktorá spustila niečo čo sa nemalo
stať tak by ste mali podať hlásenie o chybe.

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

Nové vlastnosti používateľského rozhrania (v porovnaní so sériou OmegaT 1.0):
  - Vyhľadávacie rozhranie prepísané s rozšírenou funkčnosťou
  - Hlavné rozhranie vylepšené
  - Schopnosť vybrať písmo zobrazenia
  - Plná podpora lokalizácií
  - Schopnosť prejsť na nasledujúci nepreložený segment
  - Bohaté prispôsobenie správania sa Filtrov formátov
  - Segmentácia upraviteľná používateľom
  - Okno zhody/slovníka je spojené pohyblivým oddeľovačom

Podporované formáty súborov:
  - Obyčajný text
  - HTML a XHTML
  - OpenDocument / OpenOffice.org
  - Zdrojové balíčky Java (.properties)
  - INI súbory (súbory s pármi kľúč=hodnota v akomkoľvek kódovaní)
  - PO súbory
  - formát dokumentačných súborov DocBook

Zmeny v jadre:
  - Flexibilná (vetná) segmentácia
  - Filtre formátov súborov môžu byť vytvárané ako zásuvné moduly (pluginy)
  - Prerobený kód s viacerými komentármi
  - Inštalátor pre Windows
  - Možno prekladať atribúty HTML značiek (tagov)
  - Plná kompatibilita s TMX 1.1-1.4b Level 1

==============================================================================

