Itzulpen hau Asier Sarasua Garmendiaren lana da, copyright 2012.

============================================================================================================================================================
  OmegaT 2.2, Read Me fitxategia

  1.  OmegaT-ri buruzko informazioa
  2.  Zer da OmegaT?
  3.  OmegaT-ren instalazioa
  4.  OmegaT-ri laguntzeko
  5.  OmegaT-k erroreak al dauzka? Laguntzarik behar al duzu?
  6.  Bertsioaren xehetasunak

============================================================================================================================================================
  1.  OmegaT-ri buruzko informazioa


OmegaT-ri buruzko informaziorik eguneratuena hemen aurki daiteke:
      http://www.omegat.org/

Erabiltzaileentzako laguntza, Yahoo-ren erabiltzaile-taldean (eleanitza). Mezuak arakatzeko ez da harpidetu behar:
     http://groups.yahoo.com/group/OmegaT/

Hobekuntzen eskaera (ingelesez), SourceForge-ko gunean:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Hobekuntzen eskaera (ingelesez), SourceForge-ko gunean:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

============================================================================================================================================================
  2.  Zer da OmegaT?

OmegaT Ordenagailuz Lagundutako Itzulpenak edo OLI (ingelesez, CAT) motako tresna bat da. Librea da, hau da, ez da ezer ordaindu behar hura erabiltzeko, ezta modu profesionalean erabili nahi bada ere, eta hura moldatu edota birbanatzeko libre zara, erabiltzailearen lizentzia errespetatzen baduzu.

OmegaT-ren ezaugarri nagusiak hurrengoak dira:
  - Java onartzen duen edozein sistema eragiletan erabiltzeko aukera
  - itzulpenetarako erreferentzia modura edozein TMX fitxategi erabiltzeko aukera
  - esaldi mailako segmentazio zalua (SRXaren antzeko metodoa erabiliz)
  - bilaketak proiektuan eta erreferentziako itzulpen-memorietan egiten dira
  - bilaketak edozein karpetatako onartutako edozein fitxategitan egin daitezke 
  - parekatze lausoak
  - proiektuen kudeaketa adimentsua, karpeten hierarkia konplexuak barne
  - glosarioak (terminologia-bildumak) onartzen ditu 
  - kode irekiko ortografia-egiaztatzaileak zuzenean onartzen ditu
  - StarDict hiztegiak onartzen ditu
  - Google Translate-n itzulpen automatikoko zerbitzuak onartzen ditu
  - dokumentazio eta tutorial argi eta osoa
  - lokalizazioa hainbat hizkuntzatan.

OmegaT-k hurrengo fitxategi-formatuak onartzen ditu zuzenean:
  - testu arrunta
  - HTML eta XHTML
  - HTML laguntza konpilatzeko fitxategiak
  - OpenDocument/OpenOffice.org
  - Java-ren baliabide-paketeak (.properties)
  - INI fitxategiak ('gako=balio' bikoteak edozein kodeketarekin)
  - PO fitxategiak
  - Dokumentazioko DocBook fitxategi-formatua
  - Microsoft-en OpenXML fitxategiak
  - Okapi-ren XLIFF fitxategi elebakarrak
  - QuarkXPress-en CopyFlowGold
  - Azpitituluen fitxategiak (SRT)
  - ResX
  - Android-en baliabideak
  - LaTeX
  - Typo3 LocManager
  - Laguntzak eta eskuliburuak
  - Windows-en RC baliabideak
  - Mozillaren DTD formatua
  - DokuWiki
  - Wix  
  - Infix
  - Flash XML export
  - Wordfast TXML
  - Magento CE Locale CSV
  - Camtasia Windowserako

OmegaT beste formatu batzuk ere onartzeko pertsonalizatu daiteke.

OmegaT-k automatikoki eskaneatuko ditu karpeta-hierarkia konplexuenak, onartzen dituen fitxategi guztiak atzituko ditu, eta egitura berdina duen "helburu" karpeta bat sortuko du, onartzen ez dituen fitxategien kopiak barne.

Tutorial azkar bat nahi izanez gero, ireki OmegaT eta irakurri pantailan ageri den Hasiera Azkarra.

Erabiltzailearen eskuliburua deskargatu berri duzun paketean dago. [Laguntza] menua erabiliz atzitu dezakezu, OmegaT ireki ondoren.

============================================================================================================================================================
 3. OmegaT-ren instalazioa

3.1 Orokorra
OmegaT-k Java Runtime Environment (JRE) 1.5 edo altuagoa behar du zure sisteman instalatu ahal izateko. Orain, OmegaT-ren bertsio estandarrarekin batera, Java Runtime Environment bat banatzen da, erabiltzaileak JREa hautatu, eskuratu eta instalatu behar izan ez dezan. 

Java dagoeneko instalatuta badaukazu, OmegaT-ren egungo bertsioa instalatzeko modurik errazena Java Web Start erabiltzea da. 
Horretarako, deskargatu hurrengo fitxategia eta exekuta ezazu:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Zure ordenagailuak behar duen ingurune egokia eta aplikazioa bera instalatuko du. Geroagoko abioetan ez da beharrezkoa izango linean egotea. 

Instalazioan zehar, sistema eragilearen arabera, segurtasuneko hainbat ohar jaso ditzakezu. OmegaT Java Web Start-en erabiltzeko behar den ziurtagiria "Didier Briel" izenarekin sinatuta dago jadanik. 
Bertsio honi emango dizkiozun baimenak ("ordenagailuaren atzitze mugagabea" hitzekin adierazita etor daitekeena) bertsio lokala instalatzean ematen diren berak dira: ordenagailuaren disko gogorra atzitzea baimentzen da. OmegaT.jnlp fitxategian klik egiten den hurrengoetan eguneraketak bilatuko dira, linean bazaude, eguneraketok instalatuko dira eta ondoren OmegaT abiatuko da. 

OmegaT deskargatu eta instalatzeko modu alternatiboak beherago erakusten dira. 

Windows eta Linux erabiltzaileak: zuen sistemak jadanik JRE-ren bertsio egoki bat instalatuta daukala badakizue, JRE-rik gabe banatzen den OmegaT-ren bertsioa instalatu dezakezue (bertsio honen izenean "Without_JRE" jartzen du). 
Zalantzarik baduzue, bertsio "estandarra", JRE-a duena alegia, erabili dezazuen aholkatzen dizuegu. Zuen sistemak JRE bat instalatuta eduki arren, OmegaT-ren bertsio honek ez du bestea trabatuko.

Linux erabiltzaileek: kontuan hartu OmegaT-k ez duela funtzionatuko hainbat Linux banaketekin (esaterako, Ubunturekin) banatzen diren Java inplementazio libreekin, garapen hauek eguneratu edo osatu gabe baitaude. Deskargatu eta instalatu Sun-en Java Runtime Environment (JRE) goian duzun esteka erabiliz, edo deskargatu eta instalatu JRE-rekin paketaturik datorren OmegaT bertsioa ("Linux" izena daukan .tar.gz paketea).

Mac erabiltzaileak: JRE-a jadanik instalatuta dator Mac OS X-en.

Linux PowerPC sistematan: erabiltzaileek IBM-ren JRE-a deskargatu beharko dute, Sun-ek ez baitu PPC sistementzako JRE-rik eskaintzen. Kasu honetan, deskargatu JRE-a hemendik:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Instalazioa
* Windows erabiltzaileak: Abiarazi instalazio-programa. Nahi izanez gero, instalazio-programak lasterbideak sor ditzake OmegaT abiarazteko.
Besteak: OmegaT instalatzeko, sortu OmegaT gordetzeko karpeta egoki bat (esaterako, /usr/local/lib Linux-en). Kopiatu OmegaT-ren zip edo tar.gz fitxategia karpeta horretan eta destrinkotu bertan.

3.3 OmegaT-ren abioa
OmegaT hainbat modutan abiarazi daiteke.

* Windows erabiltzaileak: egin klik bi aldiz OmegaT.exe fitxategian. Zure fitxategi-kudeatzailean (Windows Explorer) OmegaT fitxategia ikus badezakezu, eta ez OmegaT.exe fitxategia, aldatu Windows-en hobespenak fitxategien luzapenak erakuts ditzan.

* OmegaT.jar fitxategian birritan klik eginez. Komando horrek funtzionatzeko, .jar fitxategi-motak Javari lotuta egon behar du zure sisteman.

* Komando-lerrotik. OmegaT abiarazteko komandoa ondoko hau da:

cd <OmegaT.jar kokatuta dagoen karpeta>

<Java fitxategi exekutagarriaren izena eta bidea> -jar OmegaT.jar

(Java fitxategi exekutagarria java.exe deitzen da Windows-en eta java Linux-en.
Java sistema mailan instalatuta badago, ez da beharrezkoa bide osoa sartzea.)

* Windows erabiltzaileak: Instalazio-programak lasterbideak sor ditzake hasiera-menuan, mahaigainean eta abiarazte bizkorreko arean. OmegaT.exe fitxategia mahaigainera edo hasiera-menura arrastatu daiteke aplikazioa bertatik abiarazteko.

* Linux KDE erabiltzaileak: OmegaT zuen menuari gehitzeko, egin ondoko hau:

Kontrol gunea - Mahaigaina - Panelak - Menuak - Editatu K menua - Fitxategia - Elementu berria/Berria

Gero, menu egoki bat hautatu ondoren, gehitu azpimenu/elementu bat Fitxategia - Azpimenu berria eta Fitxategia -  Elementu berria erabiliz. Sartu OmegaT elementu berriaren izen gisa.

"Komandoa" eremuan, erabili nabigazio-botoia OmegaT-ren abioko script-a bilatzeko, eta hautatu ezazue. 

Egin klik ikono-botoian (Izena/Deskribapena/Iruzkinak eremuen eskuinaldean) - Beste ikonoak - Arakatu, eta joan OmegaT-ren /images azpikarpetara. Hautatu OmegaT.png ikonoa.Hautatu OmegaT.png ikonoa.

Azkenik, gorde aldaketak Fitxategia - Gorde aukera erabiliz.

* Linux GNOME erabiltzaileak: OmegaT zuen panelari (pantailaren goialdean dagoen barrari alegia) gehitzeko, egin ondoko hau:

Egin klik eskuineko botoiarekin "Panelari gehitu - Abiarazle berezitua". "Izena" eremuan sartu "OmegaT" ; "Komandoa" eremuan, erabili arakatze-botoia OmegaT-ren abioko script-a bilatzeko. Hautatu script-a eta onartu OK sakatuz.Hautatu script-a eta onartu OK sakatuz.

============================================================================================================================================================
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

Kodea hurrengo lagunek idatzi dute:
  Zoltan Bartko
  Volker Berlin
  Didier Briel (garapen-kudeatzailea)
  Kim Bruning
  Alex Buloichik (garatzaile nagusia)
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Jean-Christophe Helary
  Thomas Huriaux
  Hans-Peter Jacobs
  Guido Leenders
  Ibai Lakunza Velasco
  Fabián Mandelbaum
  John Moran
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Briac Pilpré
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Rashid Umarov  
  Antonio Vilei
  Martin Wunderlich
  Michael Zakharov

Beste ekarpen batzuk:
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (lokalizazio-kudeatzailea)
  Vito Smolej (dokumentazio-kudeatzailea)
  Samuel Murray
  Marc Prior 
  eta lagundu duen beste jende asko

(OmegaT proiektuari ekarpen esanguratsua egin diozula uste baduzu eta zure izena hemen agertzen ez bada, mesedez hitz egin gurekin.)

OmegaT-k ondoko liburutegiak erabiltzen ditu:

  HTMLParser: Somik Raha, Derrick Oswald eta beste batzuk (LGPL lizentzia)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8: Steve Roy (LGPL lizentzia)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4: VLSolutions (CeCILL lizentzia)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell: László Németh eta beste batzuk (LGPL lizentzia)

  JNA: Todd Fast, Timothy Wall eta beste batzuk (LGPL lizentzia)

  Swing-Layout 1.0.2 (LGPL lizentzia)

  Jmyspell 2.1.4 (LGPL lizentzia)

  JAXB 2.1.7 (GPLv2 + classpath salbuespena)

============================================================================================================================================================
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


============================================================================================================================================================
