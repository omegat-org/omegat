Ezt a fordítást [Fábricz Károly] készítette, copyright© [2007]. 

==============================================================================
  OmegaT 1.7.3, Olvass el!

  1.  Tájékoztató az OmegaT-ről
  2.  Mi az OmegaT?
  3.  Általános megjegyzések a Java programnyelvről és az OmegaT-ről
  4.  Hozzájárulás az OmegaT fejlesztéséhez
  5.  Gondot okoz az OmegaT használata? Segítségre van szüksége?
  6.  A jelen változatra vonatkozó adatok

==============================================================================
  1.  Tájékoztató az OmegaT-ről


A legfrissebb információk az OmegaT-ről megtalálhatók 
(angolul, szlovákul, hollandul, portugálul) :
      http://www.omegat.org/omegat/omegat.html

A felhasználók a Yahoo (többnyelvű) felhasználói csoportjától kaphatnak támogatást, ahol az archivált anyagok között 
feliratkozás nélkül kereshet:
     http://groups.yahoo.com/group/OmegaT/

A program bővítésére vonatkozó kérések (angolul) a SourceForge portálján:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Hibák jelentése (angolul) a SourceForge portálján:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Mi az OmegaT?

Az OmegaT segédeszköz a számítógéppel segített fordításhoz. Ez a program szabad szoftver
abban az értelemben, hogy még szakemberként való használatáért sem kell fizetnie,
és abban az értelemben is, hogy szabadon módosíthatja és/vagy
adhatja tovább, amennyiben betartja a felhasználói engedélyben foglaltakat.

Az OmegaT fő jellemzői a következők:
  - a Java használatát támogató bármely operációs rendszeren futtatható
  - fordítási referenciaként bármely helyes szerkezetű TMX-állománnyal használható
  - rugalmas mondattagolást tesz lehetővé (egy SRX-hez hasonló módszert hasznosít)
  - a projektben és a referenciaként használt fordítási memóriákban végez keresést
  - bármely könyvtárban keres, ideértve az OmegaT által olvasható állományokat
  - bizonytalan mintaillesztésen alapuló egyeztetést végez
  - ügyesen kezel akár bonyolult könyvtárszerkezetű projekteket
  - támogatja szójegyzékek használatát (ellenőrzi a terminológia használatát)
  - könnyen érthető dokumentációt és oktatóanyagot foglal magában
  - számos nyelven elérhető.

Az OmegaT támogatja OpenDocument állományok, Microsoft Office állományok(
OpenOffice.org átalakítóval vagy HTML-re való átalakításon keresztüli) használatát, továbbá
OpenOffice.org vagy StarOffice állományok, továbbá (X)HTML, Java lokalizációs 
fájlok, egyszerű szöveges állományok és más típusok használatát.

Az OmegaT automatikusan kezeli a legbonyolultabb szerkezetű 
forráskönyvtárakban elhelyezett és általa támogatott állományokat,
az előállított célkönyvtárban pedig az állományok másodpéldányait - ideértve a nem támogatottakat is - ugyanabban a struktúrában helyezi el.

Ha azonnal neki szeretne látni a rendszer elsajátításának, indítsa le az OmegaT-t és olvassa el a megjelenő Oktatóanyag  
azonnali kezdéshez című dokumentumot.

A felhasználói kézikönyvet a letöltött programcsomagban találja 
és az OmegaT elindítása után a [Súgó] menüből érheti el.

==============================================================================
 3. Általános megjegyzések a Java programnyelvről és az OmegaT-ről

Az OmegaT futtatásához gépén rendelkeznie kell a Java 1.4 futtatható programkörnyezet 1.4-es 
vagy későbbi verziójával. A program letölthető innen:
    http://java.com

Ha még nem telepítették a rendszert, a Windowst és Linuxot használóknak le kell tölteni a Javát.
Az OmegaT elérhető olyan változatokban is, amelyek a Javát is tartalmazzák. MacOSX-felhasználók esetén
a 
Java telepítve van a gépen.

Ha a rendszer megfelelően van teleptíve, az OmegaT-t az 
OmegaT.jar nevű állományra kétszer kattintva indíthatja el.

A Java telepítése után előfordulhat, hogy módosítania kell az elérési útvonalat úgy, 
hogy az magában foglalja a Java alkalmazás helyét is.

Linux-felhasználóknak ügyelniük kell arra, hogy az OmegaT nem működik 
olyan ingyenes/nyílt forráskódú Java rendszerekkel, amelyeket egy sor Linux-változatban (például az 
Ubuntuban) terjesztenek, mivel ezek vagy elavultak, vagy hiányosak. A csomagot letöltheti 
a fenti ugrópontról a Sun által előállított Java futtatható programkörnyezetet (JRE) formájában, vagy pedig töltse le és telepítse
 az OmegaT JRE-vel összecsomagolt változatát (a "Linux" jelzetű .tar.gz állományt).

PowerPC rendszereken Linuxot használóknak az IBM által forgalmazott JRE-t kell letölteni, 
mivel a Sun ezen rendszerekre nem bocsát rendelkezésre JRE-t. A csomagot letöltheti innen:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Hozzájárulás az OmegaT fejlesztéséhez

Ha hozzá szeretne járulni az OmegaT fejlesztéséhez, itt léphet kapcsolatba a fejlesztőkkel:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Az OmegaT felhasználói felületének, kézikönyvének és egyéb dokumentumainak a lefordításához olvassa el 
ezt a szöveget:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

Emellett iratkozzon fel a fordítói levelezőlistára:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Egyéb jellegű hozzájárulás céljából először iratkozzon fel a felhasználói csoporta:
      http://tech.groups.yahoo.com/group/omegat/

és ismerkedjen meg az OmegaT világával...

  Az OmegaT eredetileg Keith Godfrey munkája.
  Az OmegaT projektet Marc Prior koordinálja.

A fejlesztésben korábban közreműködtek:

(betűrendben)

A kódírásban részt vevők
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Makszim Mihalcsuk (vezető fejlesztő)
  Henry Pijffers (változatokat jóváhagyó vezető)
  Benjamin Siband
  Martin Wunderlich

A lokalizációban közreműködnek
  Roberto Argus (portugál - brazil)
  Alessandro Cattelan (olasz)
  Sabine Cretella (német)
  Suzanne Bolduc (eszperantó)
  Didier Briel (francia)
  Frederik De Vos (holland)
  Cesar Escribano Esteban (spanyol)
  Dmitri Gabinski (fehérorosz, eszperantó és orosz)
  Takayuki Hayashi (japán)
  Jean-Christophe Helary (francia és japán)
  Yutaka Kachi (japán)
  Elina Lagudaki (görög)
  Martin Lukáč (szlovák)
  Samuel Murray (afrikaans)
  Yoshi Nakayama (japán)
  David Olveira (portugál)
  Ronaldo Radunz (portugál - brazil)
  Thelma L. Sabim (portugál - brazil)
  Juan Salcines (spanyol)
  Pablo Roca Santiagio (spannyol)
  Karsten Voss (lengyel)
  Gerard van der Weyde (holland)
  Martin Wunderlich (német)
  Hisashi Yanagida (japán)
  Kunihiko Yokota (japán)
  Erhan Yükselci (török)
  Dragomir Kovacevic (szerb és horvát)
  Claudio Nasso (olasz)
  Ahmet Murati (albán)
  Sonja Tomaskovic (német)

Egyéb hozzájárulók
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (jelenlegi dokumentációvezető)
  Samuel Murray
  Marc Prior (jelenlegi lokalizációs vezető)
  és még számos igen segítőkész közreműködő

(Ha úgy véli, számottevően hozzájárult az OmegaT projekthez, de neve nem szerepel a felsorolásban, forduljon bizalommal hozzánk.)

Az OmegaT az alábbi könyvtárakat használja:
  Somik Raha, Derrick Oswald és mások HTMLParser programja (LGPL-engedély).
  http://sourceforge.net/projects/htmlparser

  Steve Roy MRJ-adaptere (LGPL-engedély).
  http://homepage.mac.com/sroy/mrjadapter/

  A VLSolutions VLDocking Framework nevű programja (CeCILL-engedély).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  Gondot okoz az OmegaT használata? Segítségre van szüksége?

Hibajelentés előtt alaposan olvassa végig a 
dokumentációt. Előfordulhat, hogy amit tapasztal, az az OmegaT olyan jellegzetessége, 
amelyet pont most fedezett fel. Ha az OmegaT naplóját ellenőrzi és olyan szavakkal találkozik, mint
"Hiba", "Figyelmeztetés", "Kivétel" vagy "váratlan lefagyás", akkor valami fontossal találta szemben magát
(a log.txt a felhasználói beállítások könyvtárában található, 
ennek helyét lásd a kézikönyvben).

Ezután ellenőrizze, hogy más felhasználók nem 
jelezték-e már a hibát. A hibajelentéseket tartalmazó oldalt megtekintheti a 
SourceForge portálján is. Ha már biztos abban, hogy Ön az első, aki 
olyan reprodukálható eseménysorra lelt, 
amely valami rendkívüli és nem kívánatos eredménnyel járt, akkor mindenképpen jelezze a hibát.

A megfelelő hibajelzés három dologra szorítkozik.
  - Az ismétléshez végrehajtandó lépésekre,
  - Az ezektől várt eredményre, valamint 
  - Az eredmény helyett tapasztaltak leírására.

Ehhez csatolhat állományokat, naplórészleteket, képernyőképeket, bármit, 
amivel véleménye szerint segíti a fejlesztőket a hiba megtalálásában és kijavításában.

A felhasználói csoport archívumában való kereséshez látogasson el ide:
     http://groups.yahoo.com/group/OmegaT/

A hibajelentés oldalán való keresésre és szükség esetén új hibajelentés készítésére itt nyílik lehetősége:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Hibajelentése sorsának nyomon követéséhez regisztráltathatja magát 
felhasználóként a SourceForge portálján.

==============================================================================
6.   A jelen változatra vonatkozó adatok

A jelen változatban és a korábbiakban történt módosításokról részletesen olvashat 
Please see the file 'changes.txt' for detailed information about changes in

Támogatott állományformátumok:
  - Egyszerű szöveg
  - HTML és XHTML
  - HTML Help Compiler (HCC)
  - OpenDocument / OpenOffice.org
  - Java nyelvi forrásszövegek (.properties)
  - INI-állományok (bármely kódolású kulcs=érték párok)
  - PO-állományok
  - DocBook dokumentációs formátumú állományok
  - Microsoft OpenXML-állományok

Alapvető módosítások:
  - Rugalmas (mondat) darabolás
  - A fájlformátum szűrői közvetlenül használható kódként használhatók
  - Újraírt kód több megjegyzéssel
  - Windows telepítő
  - A HTML jelölői lefordíthatók
  - Teljes 1. szintű TMX 1.1-1.4b kompatibilitás
  - Részleges 2. szintű TMX 1.4b támogatás

Új felületfunkciók (az 1.4 OmegaT sorozathoz képest):
  - Kibővített funkcionalitással újraírt felhasználói felület
  - A fő felület javítása rögzíthető ablakok használatával

==============================================================================

