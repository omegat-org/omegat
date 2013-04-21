@@TRANSLATION_NOTICE@@

==============================================================================
  OmegaT 3.0, Readme fitxategia

  1.  OmegaT-ri buruzko informazioa
  2.  Zer da OmegaT?
  3.  OmegaT-ren instalazioa
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

Hobekuntzen eskaera (ingelesez), SourceForge-ko gunean:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

==============================================================================
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

- Testu arrunteko fitxategi-formatuak

  - ASCII testua (.txt, etab.)
  - Testu kodetua (*.UTF8)
  - Java-ren baliabide-paketeak (.properties)
  - PO fitxategiak (.po)
  - INI fitxategiak ('gako=balio' formatua, .ini)
  - DTD fitxategiak (*.DTD)
  - DocuWiki fitxategiak (*.txt)
  - SubRip azpitituluen fitxategiak (*.srt)
  - Magento CE Locale CSV  (*.csv)

- Etiketatutako testuko fitxategi-formatuak

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - HTML Help Compiler (*.hhc, *.hhk)
  - DocBook (*.xml)
  - XLIFF elebakarra (*.xlf, *.xliff, *.sdlxliff)
  - QuarkXPress CopyFlowGold (*.tag, *.xtg)
  - ResX fitxategiak (*.resx)
  - Android baliabideak (*.xml)
  - LaTex (*.tex, *.latex)
  - Laguntza (*.xml) eta eskuliburu (*.hmxp) fitxategiak
  - Typo3 LocManager (*.xml)
  - WiX Localization (*.wxl)
  - Iceni Infix (*.xml)
  - Flash XML export (*.xml)
  - Wordfast TXML (*.txml)
  - Camtasia Windowserako (*.camproj)
  - Visio (*.vxd)

OmegaT beste formatu batzuk ere onartzeko pertsonalizatu daiteke.

OmegaT-k automatikoki arakatuko ditu karpeta-hierarkia konplexuenak, onartzen dituen fitxategi guztiak atzituko ditu, eta egitura berdina duen "helburu" karpeta bat sortuko du, onartzen ez dituen fitxategien kopiak barne.

Tutorial azkar bat nahi izanez gero, ireki OmegaT eta irakurri pantailan ageri den Hasiera Azkarra.

Erabiltzailearen eskuliburua deskargatu berri duzun paketean dago. [Laguntza] menua erabiliz atzitu dezakezu, OmegaT ireki ondoren.

==============================================================================
 3. OmegaT-ren instalazioa

3.1 Orokorra
OmegaT-k Java Runtime Environment (JRE) 1.5 edo altuagoa behar du zure sisteman instalatu ahal izateko. Java Runtime Environment daukaten OmegaT paketeak existitzen dira, erabiltzaileek Java hautatu, eskuratu eta instalatu behar izan ez dezaten. 

Jadanik Java badaukazu, bertsiorik berriena instalatzeko modu bat Java Web Start erabiltzea da. 
Horretarako, deskargatu hurrengo fitxategia eta exekuta ezazu:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Zure ordenagailuak behar duen ingurune egokia eta aplikazioa bera instalatuko du. Geroagoko abioetan ez da beharrezkoa izango linean egotea. 

Instalazioan zehar, sistema eragilearen arabera, segurtasuneko hainbat ohar jaso ditzakezu. OmegaT Java Web Starten erabiltzeko behar den ziurtagiria "Didier Briel" izenarekin sinatuta dago jadanik. 
Bertsio honi emango dizkiozun baimenak ("ordenagailuaren atzitze mugagabea" hitzekin adierazita etor daitekeena) bertsio lokala instalatzean ematen diren berak dira: ordenagailuaren disko gogorra atzitzea baimentzen da. OmegaT.jnlp fitxategian klik egiten den hurrengoetan eguneraketak bilatuko dira, linean bazaude, eguneraketok instalatuko dira eta ondoren OmegaT abiatuko da. 

OmegaT deskargatu eta instalatzeko modu alternatiboak beherago erakusten dira. 

Windows eta Linux erabiltzaileak: zuen sistemak jadanik JREaren bertsio egoki bat instalatuta daukala badakizue, JRErik gabe banatzen den OmegaT-ren bertsioa instalatu dezakezue (bertsio honen izenean "Without_JRE" jartzen du). 
Zalantzarik baduzue, JREa duen bertsioa erabili dezazuen aholkatzen dugu. Zuen sistemak JRE bat instalatuta eduki arren, OmegaT-ren bertsio honek ez du bestea trabatuko.

Linux erabiltzaileok: Linux banaketa askok (esaterako Ubuntuk) integraturik daukaten Java inplementazio irekian ere badabil OmegaT, baina akatsak, bistaratze-arazoak edo funtzionaltasun-galerak gerta daitezke. Gure gomendioa da Oracle Java Runtime Environment (JRE) instala dezazuen edo JRE integraturik duen OmegaT paketea eerabil dezazuen ("Linux" izena duen .tar.bz2 paketea). Java bertsioa sistema-mailan instalatu baduzue, zuen abio-bidean dagoela edo OmegaT abiarazten duzuenean esplizituki deitzen diozuela segurtatu behar duzue. Linux gehiegi ezagutzen ez baduzue, JRE integraturik duen OmegaT bertsioa instala dezazuen gomendatzen dizuegu. Hau segurua da, JRE "lokal" horrek ez baitu inolako interferentziarik zure sisteman instalatutako beste edozein JRErekin.

Mac erabiltzaileok: JREa jadanik instalatuta dator Mac OS X 10.7 bertsioa baino lehen. Lion erabiltzaileok sistemaren oharra jasoko duzue Java behar duen aplikazio bat lehen aldiz abiarazten duzuenean, eta sistemak berak automatikoki deskargatu eta instalatuko du.

Linux PowerPC sistematan: erabiltzaileek IBMren JREa deskargatu beharko dute, Sunek ez baitu PPC sistementzako JRErik eskaintzen. Kasu honetan, deskargatu JREa hemendik:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Instalazioa
* Windows erabiltzaileak: Abiarazi instalazio-programa. Nahi izanez gero, instalazio-programak lasterbideak sor ditzake OmegaT abiarazteko.

* Linux erabiltzaileok: Jarri fitxategia edozein karpeta egokitan eta destrinkotu; horren ondoren, OmegaT prest dago abiarazia izateko. Hala ere, instalazio txukunago eta lagungarriagoa egin dezakezu instalazio-script-a erabilita (linux-install.sh). Script hori erabiltzeko, ireki terminala, joan OmegaT.jar eta linux-install.sh script-a dauden karpetara eta exekutatu hura ./linux-install.sh erabilita. 

* Mac erabiltzaileok: Kopiatu OmegaT.zip fitxategia kokapen egoki batera eta destrinkotu; karpeta bat agertuko zaizue, barruan HTML dokumentazioaren aurkibide-fitxategia eta Omega.app aplikazio-fitxategia dituela.

* Beste batzuk (Solaris, FreeBSD): OmegaT instalatzeko, sortu harentzako karpeta egokia. Kopiatu zip edo tar.bz2 fitxategia karpeta horretara eta destrinkotu bertan.

3.3 OmegaT abiaraztea
OmegaT honela abiarazten da.

* Windows erabiltzaileok: Instalazioan zehar mahaigainean lasterbide bat sortu baduzue, egin klik bi aldiz haren gainean. Bestela, egin klik bi aldiz OmegaT.exe fitxategian. Zure fitxategi-kudeatzailean (Windows Explorer) OmegaT fitxategia ikus badezakezu, eta ez OmegaT.exe fitxategia, aldatu Windowsen hobespenak fitxategien luzapenak erakuts ditzan.

* Linux erabiltzaileok: Emandako instalazio-script-a erabili baduzue, OmegaT abiarazteko hau egin dezakezue: Alt+F2, eta gero: omegat

* Mac erabiltzaileok: Egin klik bi aldiz OmegaT.app fitxategian.

* Zure fitxategi-kudeatzailetik (sistema guztiak): Klik bi aldiz OmegaT.jar fitxategian. Komando horrek funtzionatzeko, .jar fitxategi-motak Javari lotuta egon behar du zure sisteman.

* Komando-lerrotik (sistema guztiak): OmegaT abiarazteko komandoa hau da:

cd <OmegaT.jar kokatuta dagoen karpeta>

<Java fitxategi exekutagarriaren izena eta bidea> -jar OmegaT.jar

(Java fitxategi exekutagarria java.exe deitzen da Windowsen eta java Linuxen.
Java sistema mailan instalatuta badago eta komando-bidean badago, ez da beharrezkoa bide osoa sartzea.)

OmegaT-ren abio-esperientzia pertsonalizatzea:

* Windows erabiltzaileak: Instalazio-programak lasterbideak sor ditzake hasiera-menuan, mahaigainean eta abiarazte bizkorreko arean. OmegaT.exe fitxategia mahaigainera edo hasiera-menura arrastatu daiteke aplikazioa bertatik abiarazteko.

* Linux erabiltzaileok: OmegaT abiarazteko modu errazagoa edukitzeko, Kaptain script-a erabil dezakezue (omegat.kaptn). Script hau erabiltzeko Kaptain instalatu behar da lehenengo. Ondoren, Kaptain abio-script-a abiaraz dezakezue Alt-F2 eta ondoren omegat.kaptn erabilita.

Kaptain script-ari eta Linuxen abio-ikonoak eta menu-elementuak gehitzeari buruzko informazio gehiagorako, begiratu Omegat on Linux HowTo dokumentua.

Mac erabiltzaileok: Arrastatu OmegaT.app zuen dock-era edo Finder leiho baten tresna-barrara, hura edozein kokapenetik abiarazi ahal izateko. Spotlight-en bilaketa-kutxan ere bila dezakezu.

==============================================================================
 4. OmegaT proiektuan parte hartzeko

OmegaT-ren garapenean parte hartzeko, jarri harremanetan garatzaileekin ondoko helbidean:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT-ren interfazea, erabiltzailearen eskuliburua edo beste edozein dokumentu itzultzeko, irakurri:
      
      http://www.omegat.org/en/translation-info.html

Eta harpidetu itzultzaileen zerrendara:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Beste modu batzuetan laguntzeko, eman izena erabiltzaile-taldean, helbide honetan:
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
  Thomas Cordonnier
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary (lokalizazio-kudeatzailea)
  Thomas Huriaux
  Hans-Peter Jacobs
  Kyle Katarn
  Ibai Lakunza Velasco
  Guido Leenders
  Aaron Madlon-Kay
  Fabián Mandelbaum
  John Moran
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Briac Pilpré
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Yu Tang
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
  eta lagundu duten beste jende asko

(OmegaT proiektuari ekarpen esanguratsua egin diozula uste baduzu eta zure izena hemen agertzen ez bada, mesedez hitz egin gurekin.)

OmegaT-k ondoko liburutegiak erabiltzen ditu:
  HTMLParser 1.6: Somik Raha, Derrick Oswald eta beste batzuk (LGPL lizentzia)
  MRJ Adapter 1.0.8: Steve Roy (LGPL lizentzia)
  VLDocking Framework 2.1.4: VLSolutions (CeCILL lizentzia)
  Hunspell: László Németh eta beste batzuk (LGPL lizentzia)
  JNA: Todd Fast, Timothy Wall eta beste batzuk (LGPL lizentzia)
  Swing-Layout 1.0.2 (LGPL lizentzia)
  Jmyspell 2.1.4 (LGPL lizentzia)
  SVNKit 1.7.5 (TMate lizentzia)
  Sequence Library (Sequence Library lizentzia)
  ANTLR 3.4 (ANTLR 3 lizentzia)
  SQLJet 1.1.3 (GPL v2)
  JGit (Eclipse Distribution lizentzia)
  JSch (JSch lizentzia)
  Base64 (jabego publikoa)
  Diff (GPL)
  JSAP (LGPL)
  orion-ssh2-214 (Orion SSH for Java lizentzia)

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
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Zure errore-jakinarazpenarekin zer ari den gertatzen jakin nahi baduzu, SourceForge-ko erabiltzaile gisa erregistratu zaitezke.

==============================================================================
6.   Bertsioaren xehetasunak

Mesedez, ikusi 'changes.txt' fitxategia bertsio honetan zein aurrekoetan egon diren aldaketei buruzko informazio zehatza nahi baduzu.


==============================================================================
