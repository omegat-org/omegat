

==============================================================================
  OmegaT 1.8.0 Skedari Më Lexo

  1.  Informata për OmegaT
  2.  Çfarë është OmegaT?
  3.  Instalimi i OmegaT-së
  4.  Kontributet te OmegaT
  5.  A është OmegaT duke ju bërë gabime juve? A ju duhet ndihmë?
  6.  Detaje të plasimit

==============================================================================
  1.  Informata për OmegaT

  
Informatat më të fundme për OmegaT mund t'i gjeni te:
(në Anglisht, Sllovakisht, Holandisht, Portugeze):
      http://www.omegat.org/omegat/omegat.html

Përkrahja për shfrytëzues, në grupin e shfrytëzuesve në Yahoo (shumë gjuhë), 
ku arkivat janë janë të kërkueshme pa abonim:
     http://groups.yahoo.com/group/OmegaT/
     
Kërkesat për përmirësime, te faqja e SourceForge-it:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Raportet e gabimeve, te faqja e SourceForge-it:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Çfarë është OmegaT?

OmegaT është një mjet Përkthimi I Asistuar Nga Kompjuteri. Është falas, në 
kuptim që ju nuk duhet të paguani asgjë që ta jeni në gjendje ta shfrytëzoni 
atë, madje edhe për shfrytëzim profesional, dhe në kuptimin që ju jeni të lirë
 ta ndryshoni atë dhe/ose ta rishpërndani atë përderisa ju i respektoni licencat 
 e shfrytëzuesit.

Tiparet kryesore të OmegaT-së janë
  - në gjendje të ekzekutohet në çfarëdor sistemi operative që përkrah Java-n
  - shfrytëzimi i çdo TMX valide si referencë përkthimi
  - segmentim fleksibil i fjalive (duke përdorur metodë të ngjashme me SRX)
  - kërkon në projekt  dhe memoriet referente të përkthimit
  - kërkon në çfarëdo dosje duke përfshirë skedarët e lexueshëm nga OmegaT
  - përputhje e trubullt
  - manipulim i mençur i projekteve duke përfshirë hierarkitë komplekse të dosjeve
  - përkrahje për fjalorthët (kontrollet terminologjike)
  - e lehtë për ta kuptuar dokumentimin dhe tutorialin
  - lokalizimi në një numër të gjuhëve.

OmegaT përkrah skedarët OpenDocument, skedarët Microsoft Office (duke përdorur
OpenOffice.org si një filtër konvertimi, ose duke konvertuar në HTML),
skedarët OpenOffice.org ose StarOffice, si dhe (X)HTML, skedarët e lokalizimit 
të Java-s ose skedarët e thjeshtë tekstual.

OmegaT do të parsoj automatikisht madje edhe hierarki më komplekse të 
dosjeve burimore, për tu qasur në të gjithë skedarët e përkrahur, dhe të prodhoj një dosje 
shënjestër me strukturë të njëjtëm duke përfshirë kopje të çdo skedari që nuk përkrahet.

Për një tutorial të nisjes së shpejtë, nise OmegaT dhe lexo Tutorialin e nisjes Së çastit.

Doracaku i shfryëzuesit është në pakon që sapo e shkarkuat, ju mund t'i qaseni asaj nga menyja
[Ndihma] pasi që ta nisni OmegaT-në.

==============================================================================
 3. Instalimi i OmegaT-së

3.1 Gjenerale
Në mënyrë që ta nisni, OmegaT kërkon Java hapësirën e veprimit (JRE) versionin
1.4 ose të lartë që të jetë i instaluar në sistemin tuaj. OmegaT tani ofrohet
standarde me JRE për t'i ruajut shfrytëzuesit nga telashe, për të marrë atë dhe
për të instluar. Shfrytëzuesit e Windows-it dhe Linux-it: nëse ju jeni të sigurt
që sistemi juaj tani më një verzion të përshtatshëm të JRE-së të instaluar, ju 
mund ta instaloni OmegaT-në pa JRE (kjo tregohet me emrin e versionit "Without_JRE"). 
Nëse ju keni çfarëdo dyshime, atëherë ne ju rekomandojmë të përdorni versionin
"standard" p.sh., "with_JRE". Kjo është e sigurt edhe nëse në sistemin tuaj tanimë
e keni të instaluar këtë verzion, dhe kjo nuk interferon  me të.
Shfrytëzuesit e Linux-it: vini re se OmegaT nuk vjen me zbatime të Java-s 
falas/me kod të hapur që janë paketuara me distribuimet të shumte taë Linux-it
(për shembull, Ubuntu), pasi që ato janë ose të vjetëruara ose jo kompete. Shkarko
dhe instalo Java Hapësrin e veprimit (JRE) nga Sun-i pëmres vjegëzës më lart, ose 
shkarko dhe instalo OmegaT pakon që përfshinë JRE-në (tufa .tar.gz e shënuar me
"Linux").
Shfrytëzuesit e Mac-uk: JRE-ja tanimë e instaluar në Mac OS X.
Linux-i në sistemet PowerPC: shfryrëzuesit duhet ta shkarkojnë JRE-në nga IBM-i, pasi
që Sun-i nuk ofren JRE për sistemet PPC. Shkarko këtë nga:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Instalimi
Për të instaluar OmegaT-në, ju thjeshtë krijoni një dosje për OmegaT-në (p.sh.,
"C:\Program File\OmegaT" në platformën Windows ose "/usr/local/lib në Linux). 
Kopjo arkivën ZIP OmegaT në këtë dosje dhe përftoje atë aty.

3.3 Nisja e OmegaT-së
OmegaT mund të niset në disa mënyra.

* Shfrytëzuesit e Windows-it: duke klikuar dyherë në skedarin OmegaT-JRE.exe, 
nëse ju jeni duke e përdoruzr verzionin me JRE-në të përfshirë ose në OmegaT.exe.

* Duke klikuar dyherë në skedarin OmegaT.bat. Nëse ju e shihni atë si OmegaT por 
jo si OmegaT.bat në Drejtuesin e skedarëve (Windows Explorer), ndërro përcaktimet 
ashtu që ju mund të shihni prapashtesat e skedarëve.

* Duke shtypur dyherë në skedarin OmegaT.jar. Kjo do të punoj vetëm nëse lloji 
i skedarit .jar është i shoqëruar me Java në sistemin tuaj.

* Nga linja e komandës. Komanda për ta nisur OmegaT-në është:

cd <dojsa ku gjendet skedari OmegaT.jar>

<emri dhe shtegu i skedarit Java të ekzekutueshëm> -jar OmegaT.jar

(Skedari i ekzekutueshëm Java është në Linux është java ndërsa në Windows java.exe.
Nëse java është e instaluar në nivelin e sistemint, nuk ka nevoj të shkruhet shtegu i plotë.)

* Shfrytëzuesit e Windows-it: Ju mund të tërheqni skedarët OmegaT-JRE.exe, OmegaT.exe ose
OmegaT.bat në tryezë ose në menynë Nis për të vjegëzuar atë nga aty.

* Shfrytëzuesit e Linux KDE-së: ju mund ta shtoni OmegaT në menytë tuaja si në vijim:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Atëherë, pasi që përzgjedhni menyënë e përshtatshme, shtoni një nënmeny/element me File - New 
Submenu dhe File - New Item. Shkruani OmegaT emrin e elementit të ri.

Në fushën "Command", përdor pullën e navigimit për të gjetur skriptën për nisjen e OmegaT-së,
dhe përzgjedhni atë. 

Shty në pullën ikonë (në të djathtë të fushave Name/Description/Comment)
- Other Icons - Browse, dhe navigo te nëndosja /images në dojen e aplikacionit
OmegaT. Përzgjedh ikonën OmegaT.png

Përfundimisht, ruaj ndryshimet me File - Save.

* Shfrytëzuesit e Linux GNOME: ju mund ta shtoni OmegaT në panon tuaj (shiriti në krye të ekranit) 
si në vijim:

Kliko me të djathtën në panel - Add New Launcher. Shkruani "OmegaT" në fushën "Name" ; 
fushën "Command", përdor pullën e navigimit për të gjetur use the navigation skriptën tuaj të nisjes
të OmegaT. Përzgjedh atë dhe mirato me OK.

==============================================================================
 4. Kontributet te OmegaT

Për të kontribuar në zhvillimin e OmegaT, kontakto me zhvilluesit tjerë në:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Për ta përkthyer ndërfaqësin e shfrytëzuesit të OmegaT-së, doracakut të shfrytëzuesit ose 
dokumente të tjera në lidhje me to, lexo:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

Dhe abonohu te lista e përthyesëve:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Për kontribute të tjera, abonohu së pari të grupi i shfrytëzuesve në:
      http://groups.yahoo.com/group/OmegaT/

Dhe për të ndjerë se çfarë po ndodhë në botën OmegaT...

  OmegaT është punë origjinale e Keith Godfrey.
  Marc Prior është koordinator i projektit OmegaT.

Kontrubuesit paraprak përfshijnë:
(në renditje alfabetike)

Kodi është kontribuar nga
  Zoltan Bartko
  Didier Briel (drejtues i plasimit)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Thomas Huriaux
  Maxym Mykhalchuk 
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

Kontribute të tjera nga
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (menagjer i dokumentimit)
  Samuel Murray
  Marc Prior (menagjer i lokalizimit)
  dhe shumë e shumë njerëz që kanë ndihmuar shumë

OmegaT përdor bibliotekat në vijim:
  HTMLParser nga Somik Raha, Derrick Oswald dhe të tjerë (Licenca LPGL).
 http://sourceforge.net/projects/htmlparser
  
  MRJ Adapter 1.0.8 nga Steve Roy (Licenca LPGL).
  http://homepage.mac.com/sroy/mrjadapter/
  
  VLDocking Framework 2.0.6d nga VLSolutions (CeCILL Licenca).
  http://www.vlsolutions.com/en/products/docking/

  Hunspell 1.1.12 nga László Németh si dhe të tjerë (Licenca LPGLt)

  JNA nga Todd Fast, Timothy Wall si dhe të tjerë (Licenca LPGL)

  Swing-Layout 1.0.2 (Licenca LPGL)

  Backport-util-concurrent (Domeni publik)

  Retroweaver 2.0.1 (Licenca Retroweaver)

  Jmyspell 2.1.4 (LGPL License)

==============================================================================
 5.  A është OmegaT duke ju bërë gabime juve? A ju duhet ndihmë?

Para se të raportoni një gabim, bëhuni të sigurtë që ju jeni kontrolluar në tërësi 
dokumentacionin. Çfarë ju shihni mund të jetë një karakteristikë e OmegaT që
ju sapo e keni zbuluar. Nëse kontrolloni OmegaT llog dhe ju i shihni fjalët si
"Error", "Warning", "Exception", ose "died unexpectedly" atëhetë ju  jeni në
diçka si (log.txt gjendet të dosje të parapëlqyerave të shfrytëzuesit, shiko doracakun
për këtë vend).

Gjëja tjerët që ju bëni është të miratoni atë që ju e keni gjetur me shfrytëzuesit tjerë,
për të qenë e sigurtë që nuk është raportuar paraprakisht. Ju gjithashut mund ta 
vërtetoni faqen e raporteve të gabimeve te SourceForge. Vetëm kur ju jeni të sigurtë që 
ju jeni i pari që ju keni gjetur disa sekuenca të riprodhueshme të ngjarjeve që kanë 
shkaktuar diçka që nuk ësht dashur të ndodh atëherë ju do të dërgoni një raport gabimi.

Çdo raport i mirë i gabimit duhet t'i përmbaj saktësisht tri gjëra.
  - Hapat se si do të ribëhet,
  - Çfarë keni pritur të shikoni, si dhe
  - Çfarë keni parë në vend të saj.

Ju mund të shtoni kopje të skedarëve, pjesë të llogut, pamje të kapura, ose çfarëdo që ju
mendoni që do tu ndihmone zhvilluesve të gjejn dhe të përmirësojnë gabimin tuaj.

Për të shfletuar arkivat e grupit të shfrytëzuesve, shko te:
     http://groups.yahoo.com/group/OmegaT/

Për ta shfletuar faqen e raportimit të gabimeve dhe për të dërguar një raport të ri 
të gabim nëse është e nevojshme, shko te:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Për të përcjellë se çfarë po ndodh me raportin tuaj të gabimin ju mund të 
doni të regjistroheni si një shfrytëzues i Source Forge.

==============================================================================
6.   Detaje të plasimit

Ju lutemi shihni skedarin 'changes.txt' për informatat e detajuara për ndryshimet 
këtë dhe të gjitha plasimet paraprake.

==============================================================================

