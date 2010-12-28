Ezt a fordítást Fábricz Károly készítette, copyright© 2010. 

==============================================================================
  OmegaT 2.0, Olvass el!

  1.  Tájékoztató az OmegaT-ről
  2.  Mi az OmegaT?
  3.  Az OmegaT telepítése
  4.  Hozzájárulás az OmegaT fejlesztéséhez
  5.  Gondot okoz az OmegaT használata? Segítségre van szüksége?
  6.  A jelen változatra vonatkozó adatok

==============================================================================
  1.  Tájékoztató az OmegaT-ről


A legfrissebb információk az OmegaT-ről megtalálhatók 
      http://www.omegat.org/

A felhasználók a Yahoo (többnyelvű) felhasználói csoportjától kaphatnak támogatást, ahol az archivált anyagok között feliratkozás nélkül tallózhat.
     http://groups.yahoo.com/group/OmegaT/

A program bővítésére vonatkozó kérések (angolul) a SourceForge portálján:
     http://sourceforge.net/tracker/?group_id=68187&amp;atid=520350

Hibák jelentése (angolul) a SourceForge portálján:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Mi az OmegaT?

Az OmegaT segédeszköz a számítógéppel segített fordításhoz. A program ingyenes abban az értelemben, hogy használatáért nem kell fizetnie, és abban az értelemben is, hogy szabadon módosíthatja és/vagy adhatja tovább, amennyiben betartja a felhasználói engedélyben foglaltakat.

Az OmegaT fő jellemzői a következők:
  - a Java használatát támogató bármely operációs rendszeren futtatható
  - fordítási referenciaként bármely érvényes szerkezetű TMX-állománnyal használható
  - rugalmas mondattagolást tesz lehetővé (egy SRX-hez hasonló módszert hasznosít)
  - a projektben és a referenciaként használt fordítási memóriákban végez keresést
  - bármely mappában rákeres a támogatott formájú állományokra 
  - bizonytalan mintaillesztésen alapuló egyeztetést végez
  - ügyesen kezel akár bonyolult könyvtárszerkezetű projekteket
  - támogatja szójegyzékek használatát (ellenőrzi a terminológia használatát) 
  - támogatja szójegyzékek használatát (ellenőrzi a terminológia használatát)
  - támogatja StarDict típusú szótárak használatát
  - támogatja a Google Translate gépi fordító szolgáltatás használatát
  - könnyen érthető dokumentációt és tájékoztatót foglal magában
  - számos nyelven elérhető.

Az OmegaT alaphelyzetben támogatja az alábbi típusú állományok fordítását:
  - egyszerű szöveg
  - HTML és XHTML
  - HTML Help Compiler állományok
  - OpenDocument/OpenOffice.org
  - Java nyelvi forrásszövegek (.properties)
  - INI-állományok (bármely kódolású kulcs=érték párok)
  - PO-állományok
  - DocBook dokumentációs formátumú állományok
  - Microsoft OpenXML-állományok
  - Okapi egynyelvű XLIFF-állományok
  - QuarkXPress CopyFlowGold
  - Feliratok (SRT)
  - ResX
  - Android forrásfájlok
  - LaTeX
  - Typo3 LocManager
  A Súgó tartalma
  - Windows RC források
  - Mozilla DTD
  - DokuWiki

Az OmegaT beállítható egyéb formájú állományok kezelésére is.

Az OmegaT automatikusan kezeli a legbonyolultabb szerkezetű forráskönyvtárakban elhelyezett és általa támogatott állományokat, az előállított célkönyvtárban pedig az állományok másodpéldányait - ideértve a nem támogatottakat is - ugyanabban a struktúrában helyezi el..

Ha azonnal neki szeretne látni a rendszer elsajátításának, indítsa le az OmegaT-t és olvassa el a megjelenő Oktatóanyag az azonnali kezdéshez c. írást.

A felhasználói kézikönyvet a letöltött programcsomagban találja, de elérheti az OmegaT elindítása után megjelenő Súgó menüből is.

==============================================================================
 3. Az OmegaT telepítése

3.1 Általános rész
Az OmegaT futtatásához számítógépén rendelkeznie kell a Java futtatható programkörnyezet (JRE) 1.5-ös vagy későbbi verziójával.. Az OmegaT a Java futtatási környezetével együtt tölthető le, így a felhasználónak nem kell a megfelelő változat kiválasztásával, letöltésével és telepítésével bajlódni. 

Ha már rendelkezik Javával, akkor az OmegaT jelenlegi változatát a legegyszerűbben a Java Web Start használatával telepítheti. 
Ehhez töltse le és futtassa az alábbi állományt:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Ez az első futtatás során létrehozza számítógépén a megfelelő környezetet. A program későbbi meghívásait nem szükséges online végezni.

A telepítés alatt az adott rendszertől függően különféle biztonsági figyelmeztetések jelenhetnek meg. A tanúsítványon "Didier Briel" aláírása szerepel. 
A jelen változathoz megadott engedélyei (melyek lehetnek "korlátlan hozzáférés a számítógéphez") azonosak a helyi változathoz hozzárendelt engedélyeivel, vagyis a hozzáférés a számítógép merevlemezére vonatkozik. Ha később az <s4>OmegaT.jnlp</s4>-re kattint, a rendszer ellenőrzi a frissítéseket és telepíti őket, mielőtt elindítaná az OmegaT-t. 

Az OmegaT letöltésének és telepítésének további módjait lásd alább. 

Windows- vagy Linux-felhasználó: ha biztos abban,  hogy a JRE megfelelő változata fut számítógépén, akkor letöltheti az OmegaT JRE  nélküli ("Without_JRE" megjelölésű) csomagját. 
Kétely esetén ajánlott a "standard", vagyis a JRE-t tartalmazó csomag használata. Használata biztonságos, mert ha telepítve van rendszerén a JRE, ez a változat nem okoz összeütközést.

Linux-felhasználó: ügyeljen arra, hogy az OmegaT nem működik olyan ingyenes/nyílt forráskódú Java rendszerekkel, amelyeket egy sor Linux-változatban (például Ubuntu) talál. a fenti ugrópontról a Sun által előállított Java futtatható programkörnyezetet (JRE), vagy pedig töltse le és telepítse az OmegaT JRE-t tartalmazó változatát (a "Linux" feliratú .tar.gz csomagot).

Mac-felhasználók: Mac OS X esetén a 
JRE a gépen telepítve van.

PowerPC rendszereken Linuxot használók: az IBM által forgalmazott JRE-t kell letölteni, mivel a Sun PPC-rendszerekre nem bocsát rendelkezésre JRE-t. Ez esetben a letöltést indítsa el innen:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Telepítés
* Windows-felhasználó: indítsa el a telepítő programot. Ha kívánja, a telepítő létrehoz egy gyorsbillentyűt az OmegaT elindításához.
* Egyéb felhasználó: az OmegaT telepítéséhez hozzon létre egy mappát az OmegaT számára (pl. Linux esetén: /usr/local/lib). Másolja át az OmegaT zip vagy tar.gz
csomagot ebbe a mappába és csomagolja ki.

3.3 Az OmegaT elindítása
Az OmegaT többféleképpen elindítható.

* Windows-felhasználó: kattintson kétszer az OmegaT.exe fájlra. Ha látja  az OmegaT állományt a fájlkezelőben, de nem látja az OmegaT.exe-t, módosítsa a beállítást úgy, hogy a kiterjesztések megjelenjenek.

* kattintson kétszer az OmegaT.jar fájlra. Ez a módszer csak akkor működik, ha rendszerén a .jar kiterjesztés a Java programmal társítva van.

* A parancssorból. Az OmegaT indításához szükséges parancs:

cd <az OmegaT.jar fájlt tartalmazó mappa>

<a futtatható Java-állomány neve és útvonala> -jar OmegaT.jar

(A futtatható Java-állomány Linux esetén a java, Windows esetén a java.exe.
Ha a Java rendszerszinten van telepítve, akkor a teljes útvonalat nem szükséges megadni.)

* Windows-felhasználó: telepítés közben a program létrehoz egy mappát a Startmenüben és gyorsbillentyűt az asztalhoz és a gyorsindító sávhoz. A kapcsolatot létrehozhatja kézzel is, ha az OmegaT.exe-t az asztalra húzza vagy a Startmenühöz vagy a gyorsindító sávhoz kapcsolja.

* Linux KDE-felhasználó: az OmegaT-t az alábbiak szerint adhatja hozzá menüihez:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Ezt követően a megfelelő menü kiválasztása után hozzon létre egy almenüt/menüelemet: File - New Submenu és File - New Item. Az új elem neveként adja meg az OmegaT-t.

A "Command" mezőben a böngésző gombbal keresse meg az  OmegaT indító programkódját, majd válassza ki. 

Kattintson az ikongombra (a Name/Description/Comment fields-től jobbra) - Other Icons - Browse, majd térjen át az OmegaT alkalmazás mappájában az /images alkönyvtárra. Válassza ki az OmegaT.png ikont.

Végezetül a File - Save segítségével mentse el a változtatásokat.

* Linux GNOME felhasználók: Az OmegaT-t az alábbiak szerint adhatja hozzá paneljéhez (a képernyő tetején):

A jobb gombbal kattintson az Add New Launcher panelre. A "Name" mezőbe írja be az OmegaT-t; a "Command" mezőben a böngésző gombbal keresse meg az  OmegaT indító programkódját. Válassza ki, majd nyomja meg az OK-t.

==============================================================================
 4. Bekapcsolódás az OmegaT projektbe

Ha hozzá szeretne járulni az OmegaT fejlesztéséhez, itt léphet kapcsolatba a fejlesztőkkel:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Az OmegaT felhasználói felületének, kézikönyvének és egyéb dokumentumainak a lefordításához olvassa el:
      
      http://www.omegat.org/en/translation-info.html

Emellett iratkozzon fel a fordítói levelezőlistára:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Egyéb jellegű hozzájárulás céljából először iratkozzon fel a felhasználói csoporta:
      http://tech.groups.yahoo.com/group/omegat/

és ismerkedjen meg az OmegaT világával...

  Az OmegaT eredetileg Keith Godfrey munkája.
  Az OmegaT projektet Marc Prior koordinálja.

A fejlesztésben korábban közreműködtek (betűrendben):

A kódírásban részt vevők
  Bartkó Zoltán
  Volker Berlin
  Didier Briel (fejlesztési vezető)
  Kim Bruning
  Alex Buloichik (vezető fejlesztő)
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Ibai Lakunza Velasco
  Fabián Mandelbaum
  Makszim Mihalcsuk 
  Arno Peters
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Antonio Vilei
  Martin Wunderlich

Egyéb hozzájárulók
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (honosítási vezető)
  Vito Smolej (dokumentációvezető)
  Samuel Murray
  Marc Prior 
  és még számos igen segítőkész közreműködő

(Ha úgy véli, számottevően hozzájárult az OmegaT projekthez, de neve nem szerepel a felsorolásban, forduljon bizalommal hozzánk.)

Az OmegaT az alábbi könyvtárakat használja:

  Somik Raha, Derrick Oswald és mások HTMLParser programja (LGPL-engedély).
  http://sourceforge.net/projects/htmlparser

  Az MRJ-adapter 1.0.8 Steve Roy munkája (LGPL-engedély).
  http://homepage.mac.com/sroy/mrjadapter/

  A VLDocking Framework 2.1.4 nevű programja a VLSolutions cégtől (CeCILL-engedély).
  http://www.vlsolutions.com/en/products/docking/

  A Hunspell 1.1.12 Németh Lászlótól és másoktól (LGPL-engedély)

  A JNA Todd Fast, Timothy Wall és mások munkája (LGPL-engedély)

  Swing-Layout 1.0.2 (LGPL-engedély)

  Jmyspell 2.1.4 (LGPL-engedély)

  JAXB 2.1.7 (GPLv2 + classpath exception)

==============================================================================
 5.  Gondot okoz az OmegaT használata? Segítségre van szüksége?

Hibajelentés előtt alaposan olvassa végig a dokumentációt. Előfordulhat, hogy amit tapasztal, az az OmegaT olyan jellegzetessége, amelyet csak most fedezett fel. Az OmegaT naplójában "Error", "Warning", "Exception" vagy "died unexpectedly" bejegyzés esetén feltehetőleg eredendő prolémával találta magát szemben (a napló [log.txt] a felhasználó beállításainak mappájában található, l. a kézikönyvet).

Ezután ellenőrizze, hogy más felhasználók nem jelezték-e már a hibát.. A hibajelentéseket tartalmazó oldalt megtekintheti a SourceForge-on is. Ha már biztos abban, hogy Ön az első, aki reprodukálható eseménysorra lelt, amely valami rendkívüli és nem kívánatos eredménnyel járt, akkor készítsen erről egy hibajelentést.

A megfelelő hibajelzés három dologra szorítkozik.
  - Az ismétléshez végrehajtandó lépésekre,
  - Az ezektől várt eredményre, valamint 
  - Az eredmény helyett tapasztaltak leírására.

A jelentéshez csatoljon állományokat, naplórészleteket, képernyőképeket, bármit, amivel véleménye szerint segíti a fejlesztőket a hiba megtalálásában és kijavításában.

A felhasználói csoport archívumában való kereséshez látogasson el ide:
     http://groups.yahoo.com/group/OmegaT/

A hibajelentés oldalán való keresésre és szükség esetén új hibajelentés készítésére itt nyílik lehetősége:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Hibajelentése sorsának nyomon követéséhez regisztráltathatja magát felhasználóként a Source Forge-on.

==============================================================================
6.   A jelen változatra vonatkozó adatok

A jelen változatban és a korábbiakban történt módosításokról részletesen olvashat a 'changes.txt' állományban.


==============================================================================
