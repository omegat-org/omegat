Ezt a fordítást Fábricz Károly készítette, copyright© 2009. 

==============================================================================
  OmegaT 2.0, Olvass el!

  1.  Tájékoztató az OmegaT-ről
  2.  Mi az OmegaT?
  3.  Az OmegaT telepítése
  4.  Közreműködés az OmegaT fejlesztésében
  5.  Gondot okoz az OmegaT használata? Segítségre van szüksége?
  6.  A jelen változatra vonatkozó adatok

==============================================================================
  1.  Tájékoztató az OmegaT-ről


A legfrissebb információk az OmegaT-ről a http://www.omegat.org/ portálon találhatók. 
      http://www.omegat.org/

A felhasználók a Yahoo (többnyelvű) felhasználói csoportjától kaphatnak támogatást, ahol az archivált anyagok között 
feliratkozás nélkül kereshetnek a
     http://groups.yahoo.com/group/OmegaT/

A program bővítésére vonatkozó kérések (angolul) a SourceForge portálján jelezhetők itt:
     http://sourceforge.net/tracker/?group_id=68187&amp;atid=520350

Hibák (angolul) a SourceForge portálján jelezhetők itt:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Mi az OmegaT?

Az OmegaT segédeszköz a számítógéppel segített fordításhoz.  A program szabadon hozzáférhető abban az értelemben, hogy 
még szakemberként való használatáért sem kell fizetnie,  
és abban az értelemben is, hogy szabadon módosíthatja és/vagy adhatja tovább, amennyiben betartja a  
felhasználói engedélyben foglaltakat.

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
  - INI-állományok (bármilyen kódolású kulcs=érték párok)
  - PO-állományok
  - DocBook dokumentációs formátumú állományok
  - Microsoft OpenXML-állományok
  - Okapi egynyelvű XLIFF-állományok
  - QuarkXPress CopyFlowGold
  - Feliratok (SRT)
  - ResX
  - Android forrásfájlok
  - LaTeX

Az OmegaT beállítható egyéb formájú állományok kezelésére is.

Az OmegaT automatikusan kezeli a legbonyolultabb szerkezetű forráskönyvtárakban elhelyezett 
és általa támogatott állományokat, az előállított célkönyvtárban 
pedig az állományok másodpéldányait - ideértve a nem támogatottakat is - ugyanabban a struktúrában helyezi el.

Ha azonnal neki szeretne látni a rendszer elsajátításának, indítsa le az OmegaT-t és olvassa el a megjelenő Útmutató az azonnali kezdéshez  
című dokumentumot.

A felhasználói kézikönyvet a letöltött programcsomagban találja és az OmegaT elindítása után 
a Súgó menüből érheti el.

==============================================================================
 3. Az OmegaT telepítése

3.1 Általános tudnivalók
Az OmegaT futtatásához számítógépén rendelkeznie kell a Java futtatható programkörnyezet (JRE)  
1.5-ös vagy későbbi verziójával. Az OmegaT letölthető a Java futtatható programkörnyezettel,  
így a felhasználónak nem kell a megfelelő változat kiválasztásával, letöltésével és telepítésével vesződnie. 

Ha már rendelkezik Javával, akkor az OmegaT jelenlegi változatát a legegyszerűbben a Java Web Start alkalmazással telepítheti. 
Ehhez töltse le és futtassa az alábbi állományt:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Ez az első futtatás során létrehozza számítógépén a megfelelő környezetet. A program későbbi meghívásait nem szükséges online végezni.

A telepítés alatt az adott rendszertől függően különféle biztonsági figyelmeztetések jelenhetnek meg. A tanúsítványon "Didier Briel" aláírása szerepel. A jelen változathoz megadott engedélyei (melyek lehetnek "korlátlan hozzáférés a számítógéphez") azonosak a helyi változathoz hozzárendelt engedélyeivel, ha a telepítés egy alább leírt folyamattal hajtja végre: a hozzáférés a számítógép merevlemezére vonatkozik. Ha később az OmegaT.jnlp-re kattint, a rendszer ellenőrzi, jelentek-e meg frissítések, és online felhasználó esetén telepíti a frissítéseket, mielőtt elindítaná az OmegaT-t. 

Az OmegaT letöltésének és telepítésének további módjait lásd alább. 

Windows- vagy Linux-felhasználó: ha biztos abban, hogy a JRE megfelelő változata fut számítógépén, akkor letöltheti az OmegaT JRE nélküli ("Without_JRE" megjelölésű) csomagját. 
Kétely esetén ajánlott a "standard", vagyis a JRE-t tartalmazó csomag letöltése. Használata biztonságos, mert ha telepítve van rendszerén a JRE, ez a változat nem okoz összeütközést.

Linux-felhasználó: ügyeljen arra, hogy az OmegaT nem működik olyan ingyenes/nyílt forráskódú Java rendszerekkel, amelyeket egy sor Linux-változatban (például az Ubuntuban) terjesztenek, mivel ezek vagy elavultak, vagy hiányosak. A fenti ugrópontról töltse le a Sun által előállított Java futtatható programkörnyezetet (JRE), vagy pedig töltse le és telepítse az OmegaT JRE-t tartalmazó változatát (a "Linux" jelzetű .tar.gz állományt).

Mac-felhasználók: Mac OS X esetén a 
JRE a gépen telepítve van.

PowerPC rendszereken Linuxot használók: az IBM által forgalmazott JRE-t kell letölteni, mivel a Sun PPC-rendszerekre nem bocsát rendelkezésre JRE-t. Ez esetben a letöltést indítsa el innen:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Telepítés
* Windowsx-felhasználó: indítsa el a telepítő programot. Ha kívánja, a telepítő létrehoz egy gyorsbillentyűt az OmegaT elindításához.
* Egyéb felhasználó: az OmegaT telepítéséhez hozzon létre egy mappát az OmegaT számára (pl. Linux esetén: /usr/local/lib). Másolja át az OmegaT zip vagy tar.gz
csomagot ebbe a mappába és csomagolja ki.

3.3 Az OmegaT elindítása
Az OmegaT többféleképpen elindítható.

* Windows-felhasználó: kattintson kétszer az OmegaT.exe fájlra. Ha a fájlkezelőben (Windows Explorer) látja az OmegaT állományt, de nem látja az OmegaT.exe-t, módosítsa a beállítást úgy, hogy a kiterjesztések megjelenjenek.

* kattintson kétszer az OmegaT.jar fájlra. Ez a módszer csak akkor működik, ha rendszerén a .jar kiterjesztés a Java programmal van társítva.

* A parancssorból. Az OmegaT indításához szükséges parancs:

cd <az OmegaT.jar fájlt tartalmazó mappa>

<a futtatható Java-állomány neve és útvonala> -jar OmegaT.jar

(A futtatható Java-állomány Linux esetén a java, Windows esetén a java.exe. Ha a Java rendszerszinten van telepítve, akkor a teljes útvonalat nem szükséges megadni.)

* Windows-felhasználó: telepítés közben a program megkérdezi, kíván-e létrehozni mappát a Start menüben és gyorsbillentyűt az asztalhoz és a gyorsindító sávhoz. A kapcsolatot létrehozhatja később is, ha az OmegaT.exe-t az asztalra húzza 
vagy a Start menühöz kapcsolja.

* Linux KDE-felhasználó: az OmegaT-t az alábbiak szerint adhatja hozzá menüihez:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New Submenu.

Ezt követően a megfelelő menü kiválasztása után hozzon létre egy almenüt/menüelemet: File - New Submenu és File - New Item. Az új elem neveként adja meg az OmegaT-t.

A "Command" mezőben a böngésző gombbal keresse meg az  OmegaT indító programkódját, majd válassza ki. 

Kattintson az ikongombra (a Name/Description/Comment fields-től jobbra) - Other Icons - Browse, majd térjen át az OmegaT alkalmazás mappájában az /images alkönyvtárra. Válassza ki az OmegaT.png ikont.

Végezetül a File - Save segítségével mentse el a változtatásokat.

Az OmegaT-t az alábbiak szerint adhatja hozzá paneljéhez (a képernyő tetején lévő sávhoz):

A jobb gombbal kattintson az Add New Launcher panelre. A "Name" mezőbe írja be az OmegaT-t; a "Command" mezőben a böngésző gombbal keresse meg az  OmegaT indító programkódját. Nyomja meg az OK gombot.

==============================================================================
 4. Bekapcsolódás az OmegaT projektbe

Ha hozzá szeretne járulni az OmegaT fejlesztéséhez, itt léphet kapcsolatba a fejlesztőkkel:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Az OmegaT felhasználói felületének, kézikönyvének és egyéb dokumentumainak a lefordításához olvassa el 
ezt az oldalt:
      
      http://www.omegat.org/en/translation-info.html

Emellett iratkozzon fel a fordítói levelezőlistára:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Egyéb jellegű hozzájárulás céljából először iratkozzon fel a felhasználói csoportba:
      http://tech.groups.yahoo.com/group/omegat/

és ismerkedjen meg az OmegaT világával...

  Az OmegaT eredetileg Keith Godfrey munkája.
  Az OmegaT projektet Marc Prior koordinálja.

Korábbi közreműködők
(betűrendben):

A kódírásban részt vevők:
  Bartkó Zoltán
  Didier Briel (változatokat jóváhagyó vezető)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Fabián Mandelbaum
  Makszim Mihalcsuk 
  Arno Peters
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

Egyéb közreműködők
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (honosítási vezető)
  Vito Smolej (dokumentációvezető)
  Samuel Murray
  Marc Prior 
  és még számos igen segítőkész közreműködő.

(Ha úgy véli, számottevően hozzájárult az OmegaT projekthez, de neve nem szerepel a felsorolásban, forduljon bizalommal hozzánk.)

Az OmegaT az alábbi programkönyvtárakat használja:

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

Hibajelentés előtt alaposan olvassa végig a dokumentációt. Előfordulhat, hogy amit tapasztal, az az OmegaT olyan jellegzetessége, amelyet pont most fedezett fel. Ha ellenőrzi az OmegaT naplóját és benne "Error", "Warning", "Exception" vagy "died unexpectedly" kifejezés szerepel, akkor feltehetőleg rálelt valamilyen hiányosságra (a log.txt a felhasználói beállítások könyvtárában található, ennek helyét lásd a kézikönyvben).

Ezután ellenőrizze, hogy más felhasználók nem jelezték-e már a hibát. A hibajelentéseket tartalmazó oldalt megtekintheti a 
SourceForge portálján is. Ha már biztos abban, hogy Ön az első, aki olyan 
reprodukálható eseménysorra lelt, amely valami rendkívüli és nem kívánatos eredménnyel járt, akkor mindenképpen jelezze a hibát.

A megfelelő hibajelzés három dologra szorítkozik.
  - Az ismétléshez végrehajtandó lépésekre,
  - Az ezektől várt eredményre, valamint 
  - Az eredmény helyett tapasztaltak leírására.

Ehhez csatolhat állományokat, naplórészleteket, képernyőképeket, bármit, amivel véleménye szerint segíti a fejlesztőket a hiba megtalálásában és kijavításában.

A felhasználói csoport archívumában való kereséshez látogasson el ide:
     http://groups.yahoo.com/group/OmegaT/

A hibajelentés oldalán való keresésre és szükség esetén új hibajelentés készítésére itt nyílik lehetősége:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Hibajelentése sorsának nyomon követéséhez regisztráltathatja magát felhasználóként a SourceForge portálján.

==============================================================================
6.   A jelen változatra vonatkozó adatok

A jelen változatban és a korábbiakban történt módosításokról részletesen olvashat a 'changes.txt' állományban.


==============================================================================