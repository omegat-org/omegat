Itzulpen hau Asier Sarasua Garmendiaren lana da, copyright 2007.

==============================================================================
  OmegaT 1.7.3, Read Me fitxategia

  1.  OmegaT-ri buruzko informazioa
  2.  Zer da OmegaT?
  3.  Java & OmegaT-ri buruzko ohar orokorrak
  4.  OmegaT-ri laguntzeko
  5.  OmegaT-k erroreak al dauzka? Laguntzarik behar al duzu?
  6.  Bertsioaren xehetasunak

==============================================================================
  1.  OmegaT-ri buruzko informazioa


OmegaT-ri buruzko informaziorik eguneratuena hemen aurki daiteke:
      http://www.omegat.org/

Erabiltzaileentzako laguntza, Yahoo-ren erabiltzaile-taldean (eleanitza). Mezuak arakatzeko ez da harpidetu behar:
     http://groups.yahoo.com/group/OmegaT/

Hobekuntzen eskaera (ingelesez), SourceForge-ko gunean:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Erroreen jakinarazpenak (ingelesez), SourceForge-ko gunean:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Zer da OmegaT?

OmegaT Ordenagailuz Lagundutako Itzulpenak edo OLI (ingelesez, CAT) motako tresna bat da. Librea da, hau da, ez da ezer ordaindu behar hura erabiltzeko, ezta modu profesionalean erabili nahi bada ere, eta hura moldatu edota birbanatzeko libre zara, erabiltzailearen lizentzia errespetatzen baduzu.

OmegaT-ren ezaugarri nagusiak ondokoak dira:
  - Java onartzen duen edozein sistema eragiletan erabili daiteke
  - edozein TMX fitxategi baliozko erabili dezake itzulpen-erreferentzia gisa
  - esaldien segmentazio malgua (SRX motako metodoa erabiliz)
  - proiektuan eta erreferentziako itzulpen-memorietan egiten ditu bilaketak
  - edozein karpetatan gordetako fitxategietan egin ditu bilaketak, fitxategiak OmegaT-k onartutakoak badira 
  - parekatze lausoa
  - proiektuen kudeaketa adimentsua, baita karpeten hierarkia konplexuak dituztenean ere
  - glosarioak erabili daitezke (terminologiaren egiaztapena)
  - dokumentazio eta tutorial argi eta zabalak
  - lokalizazioa hainbat hizkuntzatara

OmegaT-k ondoko formatuak onartzen ditu bere hartan:
  - testu hutsa
  - HTML eta XHTML
  - HTML laguntza konpilatzeko fitxategiak
  - OpenDocument / OpenOffice.org
  - Java-ren baliabide-paketeak (.properties)
  - INI fitxategiak (edozein kodeketarekin gordetako gako=balio bikotedun fitxategiak)
  - PO fitxategiak
  - DocBook dokumentazioko fitxategi-formatua
  - Microsoft OpenXML fitxategiak
  - Okapi-ren XLIFF fitxategi elebakarrak

OmegaT beste formatu batzuk ere onartzeko pertsonalizatu daiteke.

OmegaT-k automatikoki eskaneatuko ditu karpeta-hierarkia konplexuenak, onartzen dituen fitxategi guztiak atzituko ditu, eta egitura berdina duen "helburu" karpeta bat sortuko du, onartzen ez dituen fitxategien kopiak barne.

Tutorial azkar bat nahi izanez gero, ireki OmegaT eta irakurri pantailan ageri den Hasiera Azkarra.

Erabiltzailearen eskuliburua deskargatu berri duzun paketean dago. [Laguntza] menua erabiliz atzitu dezakezu, OmegaT ireki ondoren.

==============================================================================
 3. OmegaT-ren instalazioa

3.1 Orokorra. OmegaT-k Java Runtime Environment (JRE) 1.4 edo altuagoa behar du zure sisteman instalatu ahal izateko. Orain, OmegaT-ren bertsio estandarrarekin batera, Java Runtime Environment bat banatzen da, erabiltzaileak JREa hautatu, eskuratu eta instalatu behar izan ez dezan. 

Windows eta Linux erabiltzaileak: zuen sistemak jadanik JRE-ren bertsio egoki bat instalatuta daukala badakizue, JRE-rik gabe banatzen den OmegaT-ren bertsioa instalatu dezakezue (bertsio honen izenean "Without_JRE" jartzen du). 
Zalantzarik baduzue, bertsio "estandarra", JRE-a duena alegia, erabili dezazuen aholkatzen dizuegu. Zuen sistemak JRE bat instalatuta eduki arren, OmegaT-ren bertsio honek ez du bestea trabatuko.

Linux erabiltzaileek: kontuan hartu OmegaT-k ez duela funtzionatuko hainbat Linux banaketekin (esaterako, Ubunturekin) banatzen diren Java inplementazio libreekin, garapen hauek eguneratu edo osatu gabe baitaude. Deskargatu eta instalatu JRE-a goian duzuen esteka erabiliz, edo deskargatu eta instalatu JRE-rekin paketaturik datorren OmegaT bertsioa ("Linux" izena daukan .tar.gz paketea).

Mac erabiltzaileak: JRE-a jadanik instalatuta dator Mac OS X-en.

Linux PowerPC sistematan: erabiltzaileek IBM-ren JRE-a deskargatu beharko dute, Sun-ek ez baitu PPC sistementzako JRE-rik eskaintzen. Kasu honetan, deskargatu JRE-a hemendik:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Instalazioa. OmegaT instalatzeko, sortu OmegaT gordetzeko karpeta egoki bat (esaterako, C:\Program Files\OmegaT Windows-en edo /usr/local/lib Linux-en). Kopiatu OmegaT-ren ZIP fitxategia karpeta horretan eta destrinkotu bertan.

OmegaT abiaraztea

3.3.1 Windows erabiltzaileak

OmegaT hainbat modutan abiarazi daiteke.

OmegaT-JRE.exe fitxategian birritan klikatuz, JRE-a barne hartzen duen bertsioa erabiltzen ari bazarete, edo bestela OmegaT.exe fitxategian birritan klikatuz.

* OmegaT.bat fitxategian birritan klikatuz. Zure fitxategi kudeatzailean (Windows Explorer) OmegaT fitxategia ikusi badezakezu, eta ez OmegaT.bat fitxategia, aldatu Windows-en hobespenak fitxategien luzapenak erakutsi ditzan.

* OmegaT.bat fitxategian birritan klikatuz. Komando horrek funtzionatzeko, .jar fitxategi-motak Javari lotuta egon behar du zure sisteman.

* Komando-lerrotik. OmegaT abiarazteko komandoa ondoko hau da:

  cd <OmegaT.jar kokatuta dagoen karpeta>

  <Java fitxategi exekutagarriaren izena eta bidea> -jar OmegaT.jar

(Java fitxategi exekutagarria java.exe deitzen da.
Java sistema mailan instalatuta badago, ez da beharrezkoa bide osoa sartzea.)

OmegaT-JRE.exe, OmegaT.exe edo OmegaT.bat fitxategietako bat mahaigainera edo hasiera menura arrastatu daiteke aplikazioa bertatik abiarazteko.

3.3.2 Linux erabiltzaileak

* Komando-lerrotik, exekutatu:

  cd <OmegaT.jar kokatuta dagoen karpeta>

  <Java fitxategi exekutagarriaren izena eta bidea> -jar OmegaT.jar

(Java fitxategi exekutagarria java deitzen da. Java sistema mailan instalatuta badago, ez da beharrezkoa bide osoa sartzea.)


3.3.2.1 Linux KDE erabiltzaileak

OmegaT zuen menuari gehitzeko, egin ondoko hau:

Kontrol gunea - Mahaigaina - Panelak - Menuak - Editatu K menua - Fitxategia - Elementu berria/Berria

Gero, menu egoki bat hautatu ondoren, gehitu azpimenu/elementu bat Fitxategia - Azpimenu berria eta Fitxategia -  Elementu berria erabiliz. Sartu OmegaT elementu berriaren izen gisa.

"Komandoa" eremuan, erabili nabigazio-botoia OmegaT-ren abioko script-a bilatzeko, eta hautatu ezazue. 

Klikatu ikono-botoian (Izena/Deskribapena/Iruzkinak eremuen eskuinaldean) - Beste ikonoak - Arakatu, eta joan OmegaT-ren /images azpikarpetara. Hautatu OmegaT.png ikonoa.

Azkenik, gorde aldaketak Fitxategia - Gorde aukera erabiliz.

3.3.2.2 Linux GNOME erabiltzaileak

OmegaT zuen menuari (pantailaren goialdean dagoen barrari alegia) gehitzeko, egin ondoko hau:

Eskuineko botoiarekin klikatu "Panelari gehitu - Abiarazle berezitua". "Izena" eremuan sartu "OmegaT" ; "Komandoa" eremuan, erabili arakatze-botoia OmegaT-ren abioko script-a bilatzeko. Hautatu script-a eta onartu OK sakatuz.

==============================================================================
 4. OmegaT proiektuan parte hartzeko

OmegaT-ren garapenean parte hartzeko, jarri harremanetan garatzaileekin ondoko helbidean:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT-ren interfazea, erabiltzailearen eskuliburua edo beste edozein dokumentu itzultzeko, irakurri:
      
      http://www.omegat.org/en/translation-info.html

Eta harpidetu itzultzaileen zerrendara:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Beste mota bateko ekarpenak egiteko, harpidetu erabiltzaile-taldera lehenengo:
      http://tech.groups.yahoo.com/group/omegat/

Eta hasi begiratzen OmegaT-ren munduan zer ari den gertatzen...

  OmegaT Keith Godfreyren lana da jatorriz.
  Marc Prior OmegaT proiektuaren koordinatzailea da.

Ondoko hauek (ordena alfabetikoan) ekarpenak egin dituzte:

Kodearen garatzaileak
  Zoltan Bartko
  Didier Briel (bertsio kudeatzailea)
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk
  Henry Pijffers
  Tiago Saboga
  Benjamin Siband
  Martin Wunderlich

Beste ekarpen batzuk
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary
  Vito Smolej (dokumentazio kudeatzailea)
  Samuel Murray
  Marc Prior (lokalizazio kudeatzailea)
  eta beste lagun asko eta asko

(OmegaT proiektuari ekarpen esanguratsua egin diozula uste baduzu eta zure izena hemen agertzen ez bada, mesedez hitz egin gurekin.)

OmegaT-k ondoko liburutegiak erabiltzen ditu:
  HTMLParser: Somik Raha, Derrick Oswald eta beste batzuk (LGPL lizentzia).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter: Steve Roy (LGPL lizentzia).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework: VLSolutions (CeCILL lizentzia).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  OmegaT-k erroreak al dauzka? Laguntzarik behar al duzu?

Errore baten berri eman baino lehen, ziurtatu dokumentazioa sakonki aztertu duzula. Beharbada, ikusi duzuna OmegaT-k daukan ezaugarri bat besterik ez da. OmegaT-ren egunkaria aztertzen baduzu eta "Error", "Warning", "Exception" edo "died unexpectedly" bezalako hitzak ikusten badituzu, orduan agian benetako arazoren bat aurkitu duzu (log.txt fitxategia erabiltzaile-hobespenen direktorioan dago, ikusi eskuliburua bere kokapena ezagutzeko).

Egin behar duzun hurrengo gauza, beste erabiltzaile batzuekin aurkitu duzuna baieztatzea da, arazoa jada jakinarazi ez dela ziurtatzeko. Erroreak jakinarazteko orria ere SourceForge-n aurkituko duzu. Gertatu behar izango ez lukeen zerbait eragin duen sekuentzia edo gertaera bat aurkitu duzula seguru dakizunean, soilik orduan bete beharko zenuke errore-jakinarazpen bat.

Errore-jakinarazpen egokiek hiru gauza behar dituzte.
  - Urratsak errorea errepikatzeko,
  - Zer espero zenuen ikustea, eta
  - Zer ikusi zenuen.

Fitxategien kopiak, egunkariaren zatiak, pantaila-argazkiak... edozer gauza gehitu diezaiokezu errore-jakinarazpenari, horrela garatzaileek errorea errazago aurkitu eta konponduko dutela uste baduzu.

Erabiltzaile-taldearen fitxategiak arakatzeko, joan hona:
     http://groups.yahoo.com/group/OmegaT/

Errore-jakinarazpenen orria arakatzeko eta, beharrezkoa bada, errore bat jakinarazteko, joan hona:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Zure errore-jakinarazpenarekin zer ari den gertatzen jakin nahi baduzu, SourceForge-ko erabiltzaile gisa erregistratu zaitezke.

==============================================================================
6.   Bertsioaren xehetasunak

Mesedez, ikusi 'changes.txt' fitxategia bertsio honetan zein aurrekoetan egon diren aldaketei buruzko informazio zehatza nahi baduzu.


==============================================================================

