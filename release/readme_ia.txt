This translation is the work of Carmelo SERRAINO copyright© 2013, 2015.
Iste traduction es obra de Carmelo SERRAINO, copyright© 2013, 2015.

==============================================================================
  OmegaT 3.0, File lege me

  1.  Informationes re OmegaT
  2.  OmegaT, que es?
  3.  Installar OmegaT
  4.  Contributiones a OmegaT
  5.  Esque OmegaT es te importunante? Necessita tu adjuta?
  6.  Detalios del edition

==============================================================================
  1.  Informationes re OmegaT


On pote trovar le plus actual informationes re OmegaT a iste ligamine:
      http://www.omegat.org/

Supporto al usator, al Gruppo del usatores Yahoo (multilingual), ubi le archivos es
cercabile sin subscription:
     http://tech.groups.yahoo.com/group/OmegaT/

Demandas pro meliorar functiones (in anglese), in le sito SourceForge:
     https://sourceforge.net/p/omegat/feature-requests/

Reportos de defecto (in anglese), in le sito SourceForge:
     https://sourceforge.net/p/omegat/bugs/

==============================================================================
  2.  OmegaT, que es?

OmegaT es un application pro Traduction Assistite per le Computator (Computer Aided Tanslation - CAT) Illo es libere, id es tu 
non debe pagar alicun cosa pro ser capace de usar lo, mesmo pro uso professional, 
e tu es libere de lo modificar o de lo distribuer a condition que tu respecta 
le licentia del usator.

Le principal characteristicas de OmegaT es:
  - capacitate de fluer sur qualcunque systema operative que supporta Java
  - uso de qualcunque file TMX valide como referentia pro le traduction
  - flexibile segmentation del phrases (per un methodo simile al SRX)
  - recercas in le projecto e in le memorias de traduction de referentia
  - recercas del files in le formatos supportate in qualcunque plica 
  - concordantia partial
  - tractamento intelligente del projectos includente complexe hierarchias de plicas
  - supporto pro le glossarios (controlos del terminologia) 
  - supporto al volo pro le correctores orthographic OpenSource
  - supporto pro le dictionarios StarDict
  - supporto pro le servicios de Google Translate e altere traductores automatic
  - documentation e parve instruction clar e comprehensive
  - localisation in multe linguas.

OmegaT supporta le formatos de file sequente:

- Formato de file in texto simple

  - Texto ASCII (.txt, etc.)
  - Texto codificate (*.UTF8)
  - Pacchettos de ressources Java (*.properties)
  - Files PO (*.po)
  - Files INI (key=value) (*.ini)
  - Files DTD (*.DTD)
  - Files DocuWiki (*.txt)
  - Files titulo SubRip (*.srt)
  - Magento CE Locale CSV (*.csv)

- Formatos del file in texto taggate

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - Compilator de adjuta HTML (*.hhc, *.hhk)
  - DocBook (*.xml)
  - Monolingual XLIFF (*.xlf, *.xliff, *.sdlxliff)
  - QuarkXPress CopyFlowGold (*.tag, *.xtg)
  - Files ResX (*.resx)
  - Ressources Android (*.xml)
  - LaTex (*.tex, *.latex)
  - Files Help (*.xml) e Manual (*.hmxp)
  - Typo3 LocManager (*.xml)
  - Localisation WiX (*.wxl)
  - Iceni Infix (*.xml)
  - Exportation Flash XML (*.xml)
  - Wordfast TXML (*.txml)
  - Camtasia pro Windows (*.camproj)
  - Visio (*.vxd)

OmegaT pote esser personalisate pro supportar anque altere formatos de file.

OmegaT analysara automaticamente etiam le plus complexe hierarchias de plicas fonte, pro acceder omne files supportate, e producer un plica final
con exactemente le mesme structura del files rendite, includente etiam le copias de omne le files fonte non supportate.

Pro un parve instruction de initio rapide, lancea OmegaT e lege le "Initio instantanee" exponite.

Le manual del usator es in le pacchetto que tu ha justo discargate, tu pote lo acceder per le menu [Adjuta] post lanceate OmegaT.

==============================================================================
 3. Installar OmegaT

3.1 General
Pro fluer, OmegaT necessita le Java Runtime Environment (JRE), version 
1.6 o superior installate sur tu systema. Le pacchettos de OmegaT includente Java Runtime Environment es nunc disponibile pro sparniar al usatores le difficultate de  
eliger, obtener e installar lo. 

Si tu ja ha Java, un maniera pro installar le actual version de OmegaT es usar le Java Web Start. 
Pro tal proposito, discarga le sequente file e pois exeque lo:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Illo installara le ambiente correcte pro tu computator e le applicationes al prime fluer. Le ulterior appellos non habe necessitate de connexion al rete.

Durante le installation, dependente de tu systema operative, tu pote reciper 
plure advertimentos de securitate. Le certificato es per "PnS Concept". 
Le permissiones tu da a iste version (le qual pote apparer como "accesso libere al computator") es identic al permissiones que tu da al version local del OmegaT, id es, permitter le accesso al disco dur del computator. Le chiches successive sur OmegaT.jnlp 
controlara pro cata ajornamento, si tu es in rete, installara los si il habe alcun, 
e pois lanceara OmegaT. 

Le manieras alternative de discargar e installar OmegaT es
monstrate in basso. 

Usatores de Windows e de Linuxsi tu es confidente que tu systema ha ja un 
version convenibile del JRE installate, tu pote installar le version de OmegaT 
sin JRE (isto es indicate in le nomine del version,"Without_JRE"). 
Si tu es dubitose, nos recommenda que tu usa le version supplite con
JRE. Isto es secur, pois que mesmo si JRE es jam installate sur tu systema,
iste version non interferera con illo.

Usatores de Linux: 
OmegaT fluera sur le open-implementation fonte Java impaccate con plure distributiones de Linux (per exemplo Ubuntu), ma tu pote
experir defectos, problemas de schermo o characteristicas mancante. Ergo nos recommenda
que tu discarga e installa o le Oracle Java Runtime Environment (JRE) 
o le pacchetto OmegaT impacchettate in le pacco JRE (le .tar.bz2) marcate 
"Linux"). Si tu  installa un version de Java al nivello de systema, tu debe o 
assecurar te que illo es in tu percurso de lanceamento, o lancear lo explicitemente al lanceamento de
OmegaT. Si tu non es multo familiar con Linux, eo nos recommenda
que tu installa un version de OmegaT includente JRE. Isto es secur,
proque iste "local" JRE non interferera con necun altere JRE installate
sur tu systema.

Usatores de Mac: 
Le JRE es ja installate sur Mac OS X ante que Mac OS X 10.7 Le usatores de Lion essera suggerite per le systema quando isto lancea
un application que necessita Java e le systema eventualmente 
automaticamente discarga e installa lo.

Systemas Linux sur PowerPC: 
Le usatores debera discargar le JRE de IBM, pois que Sun non
provide un JRE pro systemas PPC. Discarga in iste caso ab:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installation
* Usatores de Windows: 
lancea simplemente le programma de installation. Si tu desira,
le programma de installation pote crear vias breve pro lancear OmegaT.

* usatores de Linux:
Placia le archivo in qualcunque commode plica e discassa lo; OmegaT es alora prompte a ser lanceate. Comocunque Tu pote obtener un installation plus nette e plus facile per medio del script de installation (linux-install.sh). Pro usar
ce script, aperi un fenestra terminal (consola), cambia le plica in le plica
continente le scriptes OmegaT.jar e linux-install.sh, e exeque le
script per ./linux-install.sh.

* Usatores de Mac:
Copia le archivo OmegaT.zip in un location commode e discassa lo ibi 
pro obtener un plica que contine un file indice del documentation HTML e
OmegaT.app, le file del application.

* Alteres (e.g., Solaris, FreeBSD: 
Pro installar OmegaT, simplemente crea un commode plica pro OmegaT. Copia le 
archivo OmegaT zip o tar.bz2  in iste plica e discassa lo ibi.

3.3 Lancear OmegaT
Lancea OmegaT como seque.

* Usatores de Windows: 
Si, durante le installation, tu creava un via breve sur le scriptorio,clicca duo vices sur iste via breve. In alternativa, clicca duo vices sur le file Tu pote vider le file OmegaT, ma non OmegaT.exe in tu 
gerente de files (Windows Explorer), cambia le configurationes assi que le extensiones del files 
es monstrate.

* Usatores de Linux:
Si tu ha usate le script de installation supplite, tu deberea ser habile a lancear OmegaT con:
Alt+F2
e pois:
omegat

* Usatores de Mac:
Clicca duo vices sur le file OmegaT.app.

* Ab tu gerente de files (tote le systemas):
Clicca duo vices sur le file OmegaT.jar. Isto functionara solmente si le file de 
typo  .jar es associate con Java sur tu systema.

* Ab tu linea de commando (omne systemas):
le commando pro lancear OmegaT es:

cd <plica ubi le file OmegaT.jar es locate>

<nomine e percurso del file executabile Java> -jar OmegaT.jar

(Le file executabile Java es le file java sur Linux e java.exe sur Windows.
Si Java es installate a nivello de systema e es in le percurso del commando,  
non necessita inserer le percurso integral.)

Personalisar tu experientia de lancea de OmegaT:

* Usatores de Windows: 
Le programma de installation pote crear vias breve pro te, in le menu  initio, sur le scriptorio e in le riga de lanceamento rapide. Tu pote alsi traher manualmente
le file OmegaT.exe in le menu de initio, le scriptorio o le area de lanceamento rapide
pro ligar lo ex ibi.

* Usatores de Linux:
Pro un modo plus facile de lancear OmegaT, tu pote usar le 
script Kaptain (omegat.kaptn) providite. Pro usar iste script tu debe in prime loco installar Kaptain. Deinde tu pote lancear le script Kaptain de lanceamento con
Alt+F2
omegat.kaptn

Pro plus de informationes re le script Kaptain e re le menu pro adder elementos e 
lancear icones sur Linux, referer se al "Como facer: OmegaT sur Linux".

Usatores de Mac:
Traher OmegaT.app a tu dock o al barra del instrumentos bar de un fenestra de recerca pro ser 
habile al lancear lo ab qualcunque location. Tu pote anque appellar lo in le 
campo de recerca de Spotlight.

==============================================================================
 4. Ingagiar se in le projecto OmegaT

Pro participar al disveloppamento de OmegaT, continge le disveloppatores a:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Pro traducer le interfacie del usator de OmegaT, le manual del usator o altere documentos pertinente,
lege:
      
      http://www.omegat.org/en/howtos/localizing_omegat.php

E abona te al lista del traductores:
      https://lists.sourceforge.net/lists/listinfo/omegat-l10n

Pro contribuer in altere manieras, antea subscribe te al Gruppo del usatores al:
      http://tech.groups.yahoo.com/group/omegat/

E senti lo que va accider in le mundo de OmegaT...

  OmegaT is the original work of Keith Godfrey.
OmegaT es le opera original de Keith Godfrey.
  Didier Briel is the OmegaT project manager.
Didier Briel es le Director del Projecto OmegaT.

Le previe contributores include:
(in ordine alphabetic)

Le codice esseva contribuite per
  Zoltan Bartko
  Volker Berlin
  Didier Briel
  Kim Bruning
  Alex Buloichik (capo disveloppator)
  Sandra Jean Chua
  Thomas Cordonnier
  Enrique Estévez Fernández
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
  Chihiro Hio
  Thomas Huriaux
  Hans-Peter Jacobs
  Kyle Katarn
  Piotr Kulik
  Ibai Lakunza Velasco
  Guido Leenders
  Aaron Madlon-Kay (gerente del integration)
  Fabián Mandelbaum
  Manfred Martin
  Adiel Mittmann
  Hiroshi Miura 
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
  Ilia Vinogradov
  Martin Wunderlich
  Michael Zakharov

Other contributions by
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (gerente del localisation)
  Vincent Bidaux (gerente del documentation)
  Samuel Murray
  Marc Prior (webmaster)
  e multe, multe plus maxime adjuvante gente

(si tu pensa que tu ha significativemente contribuite al projecto OmegaT 
ma tu non vide tu nomine in le listas, senti te libere de nos continger.)

OmegaT usa le sequente librerias:
  HTMLParser 1.6 ex Somik Raha, Derrick Oswald e alteres (Licentia LGPL)
  VLDocking Framework 3.0.5-SNAPSHOT (Licentia LGPL)
  Hunspell ex László Németh e alteres (Licentia LGPL)
  JNA ex Todd Fast, Timothy Wall e alteres (Licentia LGPL)
  Swing-Layout 1.0.4 (Licentia LGPL)
  Jmyspell 2.1.4 (Licentia LGPL)
  SVNKit 1.8.5 (Licentia TMate)
  Sequence Library (Licentia Sequence Library)
  ANTLR 3.4 (Licentia ANTLR 3)
  SQLJet 1.1.10 (GPL v2)
  JGit (Licentia Eclipse Distribution)
  JSch (Licentia JSch)
  Base64 (public dominio)
  Diff (GPL)
  trilead-ssh2-1.0.0-build217 (Licentia Trilead SSH)
  lucene-*.jar (Licentia Apache 2.0)
  Le tokenizeres anglese (org.omegat.tokenizer.SnowballEnglishTokenizer e
org.omegat.tokenizer.LuceneEnglishTokenizer) usa parolas de stop originari ex
Okapi (http://okapi.sourceforge.net) (Licentia LGPL)
  tinysegmenter.jar (Licentia BSD modificate)
  commons-*.jar (Licentia Apache 2.0)
  jWordSplitter (Licentia Apache 2.0)
  LanguageTool.jar (Licentia LGPL)
  morfologik-*.jar (Licentia Morfologik)
  segment-1.4.1.jar (Licentia Segment)
  pdfbox-app-1.8.1.jar (Licentia Apache 2.0)
  KoreanAnalyzer-3x-120223.jar (Licentia Apache 2.0)
  SuperTMXMerge-for_OmegaT.jar (Licentia LGPL)
  groovy-all-2.2.2.jar (Licentia Apache 2.0)
  slf4j (Licentia MIT)
  juniversalchardet-1.0.3.jar (GPL v2)
  DictZip ex JDictd (GPL v2)

==============================================================================
 5.  Esque OmegaT es te importunante? Necessita tu adjuta?

Ante que tu reporta un defecto,assecura te que tu ha verificate a fundo le
documentation. Lo que tu vide pote esser in vice un characteristica de OmegaT que
tu ha justo discoperte Si tu controla le registro del eventos de OmegaT tu vide parolas qual
"Error", "Warning", "Exception", o "died unexpectedly" alora tu ha probabilemente
discoperte un problema genuin (le file log.txt es locate in le plica del preferentias,
vide le manual pro su location).

Le cosa sequente a facer es confirmar lo que tu ha discoperte con altere usatores, pro
tranquillisar te que isto non ha essite ja reportate. Tu pote alsi verificar le pagina de reporto de defecto a
SourceForge. Sol quando tu es secur que tu es le prime que ha discoperte alicun
sequentia reproducibile de eventos que ha discatenate alicun cosa impreviste
accider, tu deberea archivar un reporto de defecto.

Cata bon reporto de defecto, necessita justo tres cosas:
  - Le grados pro lo reproducer
  - Lo que tu expecta vider, e
  - Lo que tu vide in vice

Tu pote adder copias del files, portiones del diario, instantanees, tote le cosas que
tu pensa poterea adjutar le developpatores a trovar e fixar le defecto tu ha revelate.

Pro navigar in le archivos del gruppo del usatores, vide:
     http://tech.groups.yahoo.com/group/OmegaT/

Pro navigar in le paginas del reporto de defecto e archivar un nove reporto de defecto si necessari, vade a:
     https://sourceforge.net/p/omegat/bugs/

Pro tener tracia de lo que accide a tu reporto de defecto tu pote registrar te 
como usator de Source Forge.

==============================================================================
6.   Detalios del edition

Per favor vide le file 'changes.txt' pro informationes detaliate re le modificationes in
iste e tote le editiones precedente.


==============================================================================
