==============================================================================
  OmegaT 1.6.1 bertsioaren "Irakurri" fitxategia

  1.  OmegaT-ri buruzko informazioa
  2.  Zer da OmegaT?
  3.  Java eta OmegaT-ri buruzko ohar orokorrak
  4.  OmegaT-ri laguntzeko
  5.  OmegaT-k erroreren bat al dauka? Laguntzarik behar al duzu?
  6.  Bertsioaren xehetasunak

==============================================================================
  1.  OmegaT-ri buruzko informazioa

OmegaT-ri buruzko informaziorik eguneratuena hemen aurki daiteke:
      http://www.omegat.org/omegat/omegat.html

Informazio gehiagora, ondoko orrietara jo daiteke:

Erabiltzaileentzako laguntza, Yahoo-ren erabiltzaile taldean:
     http://groups.yahoo.com/group/OmegaT/
     Hemen ez da beharrezkoa harpidetzea fitxategiak arakatzeko.

Hobekuntza-eskaerak, SourceForge-ko gunean:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Programazio-erroreen jakinarazpena, SourceForge-ko gunean:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Zer da OmegaT?

OmegaT Ordenagailuz Lagundutako Itzulpenak edo OLI (ingelesez, CAT) motako tresna bat da. Librea da, hau da, ez da ezer ordaindu behar hura erabiltzeko, ezta modu profesionalean erabili nahi bada ere, eta hura moldatu edota birbanatzeko libre zara, erabiltzailearen lizentzia errespetatzen baduzu.

OmegaT-ren ezaugarri nagusiak ondokoak dira:
  - Java onartzen duen edozein sistema eragiletan erabili daiteke
  - edozein TMX fitxategi baliozko erabili dezake itzulpen-erreferentzia gisa
  - esaldien segmentazio malgua (SRX motako metodoa erabiliz)
  - proiektuan eta erreferentziako itzulpen-memorietan egin ditu bilaketak
  - OmegaT-k irakurri ditzakeen fitxategiak dituen edozein direktoriotan egiten ditu bilaketak
  - parekatze lausoa
  - proiektuen kudeaketa adimentsua, baita direktorioen hierarkia konplexuak dituztenean ere
  - glosarioak erabil daitezke (terminologiaren egiaztapena)
  - dokumentazio eta tutorial ulerterraza
  - lokalizazioa hainbat hizkuntzatara.

OmegaT-k ondoko formatuak onartzen ditu: OpenDocument fitxategiak, Microsoft Office fitxategiak (OpenOffice.org konbertsio-iragazki gisa erabiliz, edo HTMLra bihurtuz), OpenOffice.org edo StarOffice fitxategiak eta (x)HTML, Javaren lokalizazio-fitxategiak edo testu-fitxategi arruntak.

OmegaT-k automatikoki eskaneatuko ditu direktorio-hierarkia konplexuenak, onartzen dituen fitxategi guztiak atzituko ditu, eta egitura berdina duen "helburu" direktorio bat sortuko du, onartzen ez dituen fitxategien kopiak barne.

Tutorial azkar bat nahi izanez gero, ireki OmegaT eta irakurri pantailan ageri den Hasiera Azkarra.

Erabiltzailearen eskuliburua deskargatu berri duzun paketean dago. [Laguntza] menua erabiliz atzitu dezakezu, OmegaT ireki ondoren.

==============================================================================
 3. Java eta OmegaT-ri buruzko ohar orokorrak

OmegaT-k Java Runtime Environment 1.4 edo altuagoa behar du zure sisteman instalatu ahal izateko. Java ondoko helbidean eskuratu daiteke:
    http://java.com

Windows eta Linux erabiltzaileek Java instalatu beharko dute, jada instalaturik ez badaukate.
OmegaT proiektuak Java barnean hartzen duten bertsioak ere eskaintzen ditu. MacOSX erabiltzaileek Java jada instalaturik daukate beren ordenagailuetan.

OmegaT ongi instalaturik daukan makina batean, aski da klik bikoitza egitea OmegaT.jar fitxategian OmegaT abiarazteko.

Java instalatu ondoren, agian zure sistemaren path aldagaia aldatu beharko duzu, 'java' aplikazioa zein direktoriotan dagoen kontuan har dezan.

Linux erabiltzaileek kontuan hartu behar dute OmegaT-k ez duela funtzionatuko hainbat Linux banaketatan (esaterako, Ubuntun) aurki daitezkeen Java inplementazio libreetan, garapen hauek eguneratu edo osatuu gabe baitaude. Deskargatu eta instalatu Java Runtime Environment (JRE) goian duzun esteka erabiliz, edo deskargatu eta instalatu JRE-rekin paketaturik datorren OmegaT bertsioa ("Linux" izena daukan .tar.gz paketea).

Linux PowerPC sistematan exekutatzen bada, erabiltzaileek IBM-ren JRE-a deskargatu beharko dute, Sun-ek ez baitu PPC sistementzako JRE-rik eskaintzen. Deskargatu bat hemendik:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. OmegaT-ri laguntzeko

OmegaT-ren garapenean laguntzeko, jarri harremanetan garatzaileekin ondoko helbidean:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

OmegaT-ren interfazea, erabiltzailearen eskuliburua edo beste edozein dokumentu itzultzeko, irakurri:
      http://www.omegat.org/omegat/translation-info.html

Eta harpidetu itzultzaileen zerrendara:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Beste mota bateko ekarpenak egiteko, harpidetu erabiltzaile-taldera lehenengo:
      http://groups.yahoo.com/group/OmegaT/

Eta hasi begiratzen OmegaT-ren munduan zer ari den gertatzen...

  OmegaT, jatorriz, Keith Godfreyren lana da.
  Marc Prior OmegaT proiektuaren koordinatzailea da.

Ondoko hauek ekarpenak egin dituzte:

Kodea ondoko hauek garatu dute

  Sacha Chua
  Kim Bruning
  Maxym Mykhalchuk (egungo garatzaile nagusia)
  Henry Pijffers
  Benjamin Siband

Lokalizazioan ondoko hauek lagundu dute
  Alessandro Cattelan (Italiera)
  Sabine Cretella (Alemaniera)
  Cesar Escribano Esteban (Espainiera)
  Dmitri Gabinski (Belarusiera, Esperantoa, eta Errusiera)
  Jean-Christophe Helary (Frantsesa)
  Juan Salcines (Espainiera)
  Pablo Roca Santiagio (Espainiera)
  Martin Wunderlich (Alemaniera)
  Hisashi Yanagida (Japoniera)
  Yoshi Nakayama (Japoniera)
  Takayuki Hayashi (Japoniera)
  Kunihiko Yokota (Japoniera)
  Yutaka Kachi (Japoniera)

Beste ekarpen batzuk
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (egungo dokumentazio kudeatzailea)
  Samuel Murray
  Marc Prior (egungoo lokalizazio kudeatzailea)
  eta beste lagun asko eta asko

OmegaT-k ondoko liburutegiak erabiltzen ditu:
  HTMLParser: Somik Raha, Derrick Oswald eta beste batzuk, GNU Lesser Public License, 2.1. bertsioa
  Steve Roy MRJ Adapter.
  VLDocking Framework.

==============================================================================
 5.  OmegaT-k erroreren bat al dauka? Laguntzarik behar al duzu?

Errore baten berri eman baino lehen, ziurtatu dokumentazioa sakonki aztertu duzula. Beharbada, ikusi duzuna OmegaT-k daukan ezaugarri bat besterik ez da. OmegaT-ren egunkaria aztertzen baduzu eta "Error", "Warning", "Exception" edo "died unexpectedly" bezalako hitzak ikusten badituzu, orduan erroreren bat daukazu (log.txt fitxategia erabiltzaile-hobespenen direktorioan dago, ikusi eskuliburua bere kokapena ezagutzeko).

Egin behar duzun hurrengo gauza, beste erabiltzaile batzuekin aurkitu duzuna baieztatzea da, arazoa jada jakinarazi ez dela ziurtatzeko. Erroreak jakinarazteko orria ondoko helbidean ikus dezakezu: Soilik seguru dakizunean gertatu behar izango ez lukeen zerbait eragin duen sekuentzia edo gertaera bat aurkitu duzula bete beharko zenuke errore-jakinarazpen bat.

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

Erabiltzaile-interfazearen ezaugarri berriak (OmegaT 1.0 seriearekin konparatuz)
  - Bilaketak egiteko interfazea berridatzi da eta funtzionalitate gehiago eman zaio
  - Interfaze nagusia hobetu da
  - Testuak bistaratzeko letra-tipoa hautatzeko aukera
  - Lokalizazioetarako laguntza osoa
  - Itzuli gabeko hurrengo segmentura jauzi egiteko aukera
  - Fitxategi-formatuen iragazkiak pertsonalizatzeko aukera ugari
  - Erabiltzaileak pertsonalizatu dezakeen segmentazioa
  - Parekatzeen eta Glosarioen leihoak panel zatitu arrastagarri baten bidez elkarri lotuta daude

Onartutako fitxategi-formatuak:
  - Testu arrunta
  - HTML eta XHTML
  - OpenDocument / OpenOffice.org
  - Javaren baliabide-paketeak (.properties)
  - INI fitxategiak (edozein kodeketarekin gordetako gako=balio bikotedun fitxategiak)
  - PO fitxategiak
  - DocBook dokumentazioko fitxategi-formatua

Nukleoaren aldaketak:
  - (Esaldien) segmentazio malgua
  - Fitxategi-formatuen iragazkiak plugin gisa sortu daitezke
  - Birsortutako kodea, iruzkin gehiago duena
  - Windowserako instalatzailea
  - HTML etiketen atributuak itzuli egin daitezke
  - TMX 1.1-1.4b Level 1 formatuarekin erabateko bateragarritasuna
  - TMX 1.4b Level 2 formatuaren onarpen partziala

==============================================================================

