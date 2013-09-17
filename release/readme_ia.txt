Le  traduction presente es le labor de Carmelo Serraino, derecto de autor© 2012, 2013.

==============================================================================
  File lege me de OmegaT 3.0

  1.  Information re OmegaT
  2.  Quod es OmegaT?
  3.  Installar OmegaT
  4.  Contribuentes a OmegaT
  5.  Esque OmegaT es te importunante? Esque tu necessita adjuta?
  6.  Detalios del edition

==============================================================================
  1.  Information re OmegaT


On pote trovar le plus actual informationes circa OmegaT a
      http://www.omegat.org/

Supporto del usator, al gruppo del usator  Yahoo (multilingual), ubi on pote recercar in le archivos sin abonamento
     http://tech.groups.yahoo.com/group/OmegaT/

Demandas pro augmentationes (in anglese), in le sito SourceForge:
     https://sourceforge.net/p/omegat/feature-requests/

Reportos de defecto (in anglese), in le sito SourceForge:
     https://sourceforge.net/p/omegat/bugs/

==============================================================================
  2.  Quod es OmegaT?

OmegaT es un application pro traduction adjutate ab computator (CAT). Illo es gratuite, id es tu non habe a pagar alco pro poter usar lo, mesmo pro uso professional, e tu es libere de modificar lo o de distribuer lo a condition que tu respecta le licentia del usator.

Le principal characteristicas de OmegaT's es:
  - capacitate de execution sur qualcunque systema operative que supporta Java
  - empleo de qualcunque file TMX valide como un referentia de traduction
  - flexibile segmentation del phrases (usante un methodo SRX-simile)
  - recercas in le projecto e in le memorias de traduction de referentia
  - recercas de files in formatos supportate in qualcunque classificator 
  - concordantia partial
  - argute maneamento de projectos includente complexe hierarchias de plica
  - supporto pro le glossarios (controlos del terminologia) 
  - supporto al volo pro le correctores orthographic OpenSource
  - supporto pro dictionarios StarDict
  - supporto pro le servicios de traduction a machina  de Google Translate
  - documentation e parve instruction clar e comprehensive 
  - localisation in multe linguas.

OmegaT supporta le formatos de file sequente:

- formato de file in texto plan

  - Texto ASCII (.txt, etc.)
  - Texto codificate (*.UTF8)
  - Pacchettos  ressource Java (*.properties)
  - Files PO (*.po)
  - Files INI (key=value) (*.ini)
  - Files DTD (*.DTD)
  - Files DocuWiki (*.txt)
  - Files titulo SubRip (*.srt)
  - Magento CE Locale CSV (*.csv)

- Formatos del file in texto con tags

  - Formatos OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Formatos Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - Formatos (X)HTML (*.html, *.xhtml,*.xht)
  - Compilator de adjuta HTML (*.hhc, *.hhk)
  - Formato DocBook (*.xml)
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

OmegaT analysara automaticamente anque le plus complexe hierarchias de plicas de origine pro acceder omne files supportate, e producer le plicas final con exactemente le mesme structura, includente etiam le copias de omne files non supportate..

Pro un parve instruction de rapide initio, que tu lancea OmegaT e lege le "Initio instantanee" monstrate.

Le manual del usator es in le pacchetto que tu ha justo discargate, tu pote lo acceder ab le menu adjuta post le initio del OmegaT

==============================================================================
 3. Installar OmegaT

3.1 General
Pro exequer, OmegaT necessita que le Java Runtime Environment (JRE) es installate sur tu computator. Le pacchettos OmegaT que include le ambiente Runtime de Java (Java Runtime Environment o JRE), es ora disponibile pro salvar le usatores del difficultate de seliger e installar lo 

Si tu ja ha Java, un maniera pro installar le actual version de OmegaT es usar Java Web Start. 
Pro ce fin, discarga le sequente file e pois exeque lo:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Illo installara le ambiente correcte pro tu computator e le mesme application al prime lanceamento. Le ulterior appellos non habe necessitate de connexion in interrete.

Durante le installation, dependente de tu systema operative, tu pote reciper plure advertimentos de securitate. Le certificato es firmate per "Didier Briel". 
Le permissiones que tu da a iste version (le qual pote esser describite como un "accesso sin restriction al computator") es identic al permissiones que tu assigna al version local, como installate per un procedura, describite depost: illes permitte un accesso al disco dur de tu computator. Subsequente clicca sur OmegaT.jnlp controlara pro alicun renovationes, si tu ha le connexion a interrete, e installara illos que habe,
e deinde lancear OmegaT.. 

Le alternative manieras e medios pro discargar e installar OmegaT es monstrate in sequito. 

Usatores de Windows e Linux: si tu es confidente que sur tu systema es jam installate un convenibile version del JRE, tu pote discargar e installar un version sin le JRE de OmegaT (isto es indicate in le nomine del version,"Without_JRE"). 
Si tu ha alicun dubita, nos recommenda que tu usa le version supplite con JRE. Isto es secur, depois que si un JRE es jam installate sur tu systema, iste version non interferera con illo.

Usatores de Linux: OmegaT currera super le open-implementation source Java impaccate con multe distributiones de Linux (per exemplo, Ubuntu), ma tu pote haber experientia de defectos, problemas de monstra o characteristicas perdite. Nos recommenda que tu discarga e installa anque le ambiente Oracle Java Runtime (JRE) o le pacchetto OmegaT impacchettate con le pacco JRE (le .tar.bz2) marcate "Linux". Si tu  installa un version de Java al nivello de systema, tu debe o assecurar te que illo es in tu percurso de lanceamento, o lancear lo explicitemente quando tu lanceara OmegaT. Si tu non es molto familiar con Linux, nos te recommenda pro seliger un version de OmegaT con le JRE includite. Isto es secur, proque iste "local" JRE non interferera con necun altere JRE installate in tu systema.

Usatores de Mac: Le JRE es ja installate sur Mac OS X ante que Mac OS X 10.7 (Lion).  Le usatores de Lion essera suggerite ab le systema quando isto prime lanceara un application que necessita Java e le systema eventualmente discargara lo e installara lo automaticamente.

Linux sur systemas PowerPC: Le usatores debera discargar le JRE de IBM, pois que Sun non provide un JRE pro systemas PPC. Discarga in iste caso ex:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installation
* Usatores de Windows: Simplemente lancea le programma de installation. Si tu vole, le programma de installation pote crear vias breve pro lancear OmegaT.

* usatores de Linux:
Pone le archivo in qualcunque commode classificator e lo discassa; OmegaT es alora preste a esser lanceate Comocunque tu pote obtener un installation plus nette e plus facile usante le script de installation (linux-install.sh). Pro usar ce script, displica un fenestra terminal (consola), cambia le plica in le plica continente le script OmegaT.jar e linux-install.sh, e exeque le script con ./linux-install.sh.

* Usatores de Mac: Copia le archivo OmegaT.zip in un plica commode e discassa lo ibi pro obtener un plica que contine un file indice del documentation HTML e OmegaT.app, le file application. 

* Altere (e.g., Solaris, FreeBSD: 
Pro installar OmegaT, crea simplemente un plica convenibile pro OmegaT. Copia le archivo OmegaT zip o tar.bz2 in iste plica e dispacchetta lo ibi.

3.3 Lancear OmegaT
Lancea OmegaT como seque.

* Usatores de Windows: 
Si, durante le installation, tu creava un via breve sur le scriptorio,
clicca duo vices sur iste via breve. Alternativemente, clicca duo vices sur le file
OmegaT.exe. Si tu vide la file OmegaT ma non vide OmegaT.exe in tu administrator de files (Windows Explorer), cambia le preparationes a fin que le extensiones esserea monstrate.

* Usatores de Linux: Si tu usa le script de installation supplite, tu potera lancear OmegaT con:
Alt+F2 e alora:
omegat

* Usatores de Mac: clicca duo vices sur le file OmegaT.app.

* A partir de tu administrator de files (omne systemas): clicca duo vices sur le file OmegaT.jar. Isto functionara solmente si le file de typo .jar es associate con Java sur tu systema.

* A partir de tu linea de commando (omne systemas): 
le commando pro lancear OmegaT es:

cd <plica ubi le file OmegaT.jar es localisate>

<nomine e via del file executabile Java> -jar OmegaT.jar

(Le file executabile Java es le file java sur Linux e java.exe sur Windows.
Si Java es installate a nivello de systema e es in le percurso del commando, non necessita inscriber le percurso complete.)

Adapta vostre experientia de lanceamento de OmegaT:

* Usatores de Windows: 
Le programma de installation pote vos crear vias breve in le menu de initio, sur le scriptorio e in le area de lanceamento rapide. Vos pote alsi trainar manualmente le file OmegaT.exe in le menu de initio, le scriptorio o le area de lancha rapide pro ligar lo ex illac.

* Usatores de Linux: pro un modo plus facile de lancear OmegaT, vos pote usar le script Kaptain fornite (omegat.kaptn). Pro usar iste script vos debe in prime loco installar Kaptain. Deinde tu pote lancear le script de lanceamento de Kaptain con Alt+F2 omegat.kaptn

Pro magis informationes re le scripturas Kaptain e re le menu pro adder articulos e lancear icones sur Linux, refere al documento "OmegaT sur Linux".

Usatores de Mac: Traina le file OmegaT.app sur tu dock o sur le barra instrumento de un fenestra trovator pro esser
habile a lancear lo ex omne position. Tu pote alsi lo appellar in le campo de recerca Spotlight.

==============================================================================
 4. Ingagiar se in le projecto OmegaT

Pro participar al disveloppamento de OmegaT, continge le disveloppatores a:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Pro traducer le interfacie del usator de OmegaT, le manual del usator o altere documentos pertinente,
lege:
      
      http://www.omegat.org/en/howtos/localizing_omegat.php

E abona te al lista del traductores:
      https://lists.sourceforge.net/lists/listinfo/omegat-l10n

Pro contribuer in altere manieras, subscribe primo al gruppo del usuarios  a:
      http://tech.groups.yahoo.com/group/omegat/

E senti lo que va in le mundo de OmegaT...

  OmegaT es le opera original de Keith Godfrey.
  OmegaT es le opera original de Keith Godfrey.

Le previe contributores include:
(ordine alphabetic)

Le codice esseva contribuite ab
  Zoltan Bartko
  Volker Berlin
  Didier Briel (director del disveloppamento)
  Kim Bruning
  Alex Buloichik (conducer disveloppator)
  Sandra Jean Chua
  Thomas Cordonnier
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
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
  Yu Tang  
  Antonio Vilei
  Martin Wunderlich
  Michael Zakharov

altere contributiones per
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (director del localisation)
  Vito Smolej (director del documentation)
  Samuel Murray
  Marc Prior 
  e multe, multe magis maxime adjuvante gente

(si tu puta que tu ha significativemente contribuite al projecto OmegaT
ma tu non vide tu nomine in le listas, senti te libere de nos continger.)

OmegaT usa le sequente librerias:
  HTMLParser 1.6 ex Somik Raha, Derrick Oswald e alteres (Licentia LGPL)
  MRJ Adapter 1.0.8 ex Steve Roy (Licentia LGPL)
  VLDocking Framework 2.1.4 ex VLSolutions (Licentia CeCILL)
  Hunspell ex László Németh e alteres (Licentia LGPL)
  JNA ex Todd Fast, Timothy Wall e alteres (Licentia LGPL)
  Swing-Layout 1.0.2 (Licentia LGPL)
  Jmyspell 2.1.4 (Licentia LGPL)
  SVNKit 1.7.5 (Licentia TMate)
  Sequence Library (Licentia Sequence Library)
  ANTLR 3.4 (Licentia ANTLR 3)
  SQLJet 1.1.3 (GPL v2)
  JGit (Licentia Eclipse Distribution)
  JSch (Licentia JSch)
  Base64 (public dominio)
  Diff (GPL)
  orion-ssh2-214 (Licentia Orion SSH pro Java)
  lucene-*.jar (Apache License 2.0)
  Le Tokenizers anglese (org.omegat.tokenizer.SnowballEnglishTokenizer and
  org.omegat.tokenizer.LuceneEnglishTokenizer) usa stop words ex Okapi
(http://okapi.sourceforge.net) (LGPL license)
  tinysegmenter.jar (Modified BSD license)
  TinySegmenter (dominio public)
  jWordSplitter (Apache License 2.0)
  LanguageTool.jar (LGPL License)
  morfologik-*.jar (Morfologik license)
  segment-1.4.1.jar (Segment License)
  pdfbox-app-1.8.1.jar (Apache License 2.0)

==============================================================================
 5.  Esque OmegaT es te importunante? Esque tu necessita adjuta?

Ante que tu reporta un defecto,assecura te que tu ha verificate a fundo le documentation. Lo que tu vide pote esser in vice un characteristic de OmegaT que tu ha justo discoperte. Si tu controla le bloco OmegaT e tu vide parolas como "Error", "Warning", "Exception", o "died unexpectedly" alora tu ha probabilemente discoperte un genuin problema (le log.txt es localisate in le plica del preferentias del usator, vide le manual pro su ubication)

Le proxime cosa a facer es confirmar lo que tu ha discoperte con altere usatores, pro tranquillisar te que isto non esseva ja reportate. Tu pote verificar le pagina de reporto defecto a SourceForge. Sol quando tu es secur que tu es le prime que ha discoperte alicun sequentia reproducibile de eventos que ha discatenate alicun cosa impreviste tu deberea archivar un reporto de defecto.

Recorda que cata bon reporto de defecto, necessita justo tres cosas:
  - Passos pro lo reproducer
  - Lo que tu expecta vider
  - Que tu vide in vice

Tu pote adder copias de files, portiones del bloco, paginas del schermo, e alicun altere cosas que tu pensa adjutara le disveloppatores a trovar e reparar tu defecto.

Pro foliar le archivos del gruppo del usator, vade a:
     http://tech.groups.yahoo.com/group/OmegaT/

Pro foliar le pagina del reporto de defecto e archivar un nove reporto de defecto si necessari, vade a:
     https://sourceforge.net/p/omegat/bugs/

Pro conservar tracia de lo que occurre a tu reporto de defecto tu pote registrar te como usator de Source Forge.

==============================================================================
6.   Detalios del edition

Per favor vide le file 'changes.txt' per un information detaliate circa le cambios in iste e omne previe editiones.


==============================================================================
