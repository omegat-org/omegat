@@TRANSLATION_NOTICE@@

==============================================================================
  Ffeil Darllennwch Fi OmegaT 3.0

  1.  Gwybodaeth am OmegaT
  2.  Beth yw OmegaT?
  3.  Gosod OmegaT
  4.  Cyfraniadau i OmegaT
  5.  A yw OmegaT yn eich poeni chi? Ydych chi angen cymorth?
  6.  Manylion ryddhau

==============================================================================
  1.  Gwybodaeth am OmegaT


Mae'r wybodaeth ddiweddaraf am OmegaT i'w gael yn
      http://www.omegat.org/

Mae cefnogaeth i'r defnyddiwr ar gael yn y gr?p defnyddiwr Yahoo (amlieithog),
lle mae archif chwiliadwy ar gael heb danysgrifiad:
     http://groups.yahoo.com/group/OmegaT/

Cais am Welliannau (yn Saesneg), ar wefan SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Negeseuon gwall (yn Saesneg), gwefan SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Beth yw OmegaT?

Offeryn Cyfieithu gyda Chymorth Cyfrifiadur yw OmegaT (CAT). Mae am ddim, hynny yw
does dim rhaid talu i'w ddefnyddio, hyd yn oed ar gyfer defnydd proffesiynol,
ac rydych yn rhydd i'w newid a neu ei ailddosbarthu cyn belled â'ch bod yn
parchu trwydded y defnyddiwr.

Prif nodweddion OmegaT yw:
   - gallu rhedeg ar unrhyw system weithredu sy'n cynnal Java
   - gallu defnyddio unrhyw ffeil TMX dilys fel cyfeirnod cyfieithu
   - segmentu brawddegau hyblyg (gan ddefnyddio dull SRX)
   - chwilio o fewn y project a'r cof cyfieithu penodol
   - chwilio am ffeiliau fformatau sy'n cael eu cynnal, o fewn unrhyw ffolder 
   - cyfatebiad bras cyfieithiadau
   - trin projectau sylweddol gan gynnwys hierarchaethau cyfeiriaduron cymhleth
   - cynnal geirfaoedd (ar gyfer gwirio termau) 
   - cynnal gwirwyr sillafu Cod Agored
   - cynnal geiriaduron StatDict
   - cynnal gwasanaethau cyfieithu peirianyddol Google Translate
   - dogfennaeth a thiwtorial clir a chynhwysfawr
   - wedi ei leoleiddio i nifer o ieithoedd.

Mae OmegaT yn cynnal y fformatau ffeil canlynol:

- fformatau ffeiliau testun plaen

  - testun AsCII (.txt, etc.)
  - testun wedi ei amgodio (*.UTF8)
  - bwndeli adnoddau Java (*.properties)
  - ffeiliau PO (*.po)
  - ffeiliau INI (key=value) (*.ini)
  - ffeiliau DTD (*.DTD)
  - ffeiliau DocuWiki (*.txt)
  - ffeiliau teitl SubRip (*.srt)
  - ffeiliau Magento CE Locale CSV  (*.csv)

- fformatau ffeiliau testu wedi eu tagio

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - HTML Help Compiler (*.hhc, *.hhk)
  - DocBook (*.xml)
  - monolingual XLIFF (*.xlf, *.xliff, *.sdlxliff)
  - QuarkXPress CopyFlowGold (*.tag, *.xtg)
  - ffeiliau ResX (*.resx)
  - Android resource (*.xml)
  - LaTex (*.tex, *.latex)
  - ffeiliau Cymorth (*.xml) a Llawlyfr (*.hmxp)
  - Typo3 LocManager (*.xml)
  - WiX Localization (*.wxl)
  - Iceni Infix (*.xml)
  - Flash XML export (*.xml)
  - Wordfast TXML (*.txml)
  - Camtasia for Windows (*.camproj)
  - Visio (*.vxd)

Mae modd addasu OmegaT i gynnal ffeiliau eraill hefyd.

Gall OmegaT ddidoli'r hierarchaeth fwyaf cymhleth,  yn awtomatig, i gael
mynediad at y ffeiliau sy'n cael eu cynnal a chynhyrchu cyfeiriadur targed gyda'r
union un strwythur, gan gynnwys copïau o ffeiliau sydd ddim yn cael eu cynnal.

Mae tiwtorial ar gael wrth gychwyn OmegaT pan fydd yn dangos y Tiwtorial
Cychwyn Cyflym.

Mae'r llawlyfr defnyddiwr o fewn y pecynnau rydych newydd eu llwytho i lawr ac mae
modd cael mynediad atynt drwy'r ddewislen [Cymorth] ar ôl cychwyn OmegaT.

==============================================================================
 3. Gosod OmegaT

3.1 Cyffredinol
Mae OmegaT angen fersiwn 1.5 neu uwch o'r Java Runtime Environmenter
ar eich system. Mae OmegaT nawr ar gael gyda 'r
Java Runtime Environment yn rhan o'r pecyn er mwyn arbed trafferthion i ddefnyddwyr
ei ddewis, a'i osod. 

Os yw Java gennych eisoes, un ffordd i osod y fersiwn yma o OmegaT
yw drwy ddefnyddio Java Web Start. 
Er mwyn gwneud hynn llwythwch i lawr y ffeil ganlynol a'i gweithredu:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Bydd yn gosod yr amgylchedd cywir ar gyfer eich cyfrifiadur a'r rhaglen. Nid oes rhaid bod ar-lein wedyn.

Yn ystod y gosod, yn ddibynnol ar eich system weithredu, efallai y byddwch yn
derbyn nifer o rybuddion diogelwch. Mae'r dystysgrif wedi ei lofnodi gan "Didier Briel" ei hun. 
Mae'r caniatâd rydych yn rhoi i'r fersiwn hon (fydd efallai yn cael ei nodi fel
"mynediad dilyffethair i'r cyfrifiadur") yn union yr un peth â'r caniatâd rydych yn eu
rhai yn y fersiwn lleol, sy'n cael ei osod gan drefn, fydd yn cael ei ddisgrifio’n
hwyrach; maent yn caniatáu mynediad i ddisg caled cyfrifiadur. Bydd clicio canlynol ar OmegaT.jnlp 
yn gwirio am ddiweddariadau, os ydych ar-lein eu gosod ac yna cychwyn
OmegaTwill check for any upgrades, if you are online, install them if there are any, 
and then start OmegaT. 

Mae'r ffyrdd eraill o lwytho i lawr a gosod OmegaT yn cael eu dangos isod. 

Defnyddwyr Windows a Linux: os ydych yn si?r fod fersiwn addas o'r JRE wedi
ei osod ar eich system, gallwch osod y fersiwn o OmegaT heb JRE. Mae hyn yn
cael ei ddynodi drwy enw'r fersiwn, "Without _JRE") 
Os oes amheuaeth, defnyddiwch y fersiwn "safonol", h.y. gyda'r JRE. Mae hyn yn ddigon diogel oherwydd hyd yn oed os yw'r JRE ar y system
ni fydd y fersiwn yma'n ymyrryd ag ef.

Defnyddwyr Linux:
Mae OmegaT yn gweithio gyda'r fersiwn rhydd/cod agored o Java sydd wedi ei
gynnwys gydag amryw o ddosbarthiadau o Linux (e.e., Ubuntu) ond efallai y
profwch wallau, anhawsterau arddangos neu nodweddion coll. Rydym felly yn argymell
eich bod yn llwytho i lawr a gosod Java Runtime Environment (JRE) Oracle
neu'r pecyn OmegaT sy'n cynnwys y bwndel JRE (.tar.gz ar gyfer Linux)  Os fyddwch yn gosod fersien o Java ar lefel system rhaid un ai
sicrhau ei fod o fewn eich llwybr cychwyn neu ei alw yn benodol
wrth gychwyn OmegaT. Os nad ydych yn gyfarwydd â Linux rydym yn argymell eich bod yn gosod
y fersiwn o OmegaT sy;n cynnwys y JRE. Mae hyn yn ddiogel,
gan na fydd y fersiwn "lleol" o JRE yn ymyrryd gydag unrhyw JRE arall
gall fod ar eich system.

Defnyddwyr Mac
Mae'r JRE eisoes wedi ei osod ar y MAC OS X cyn Mac OS X 10.7 Bydd defnyddwyr Lion yn cael eu hannog gan y system pa fyddant
yn cychwyn rhaglen sydd angen Java a bydd y system yn ei
lwytho i lawr a'i osod.

Defnyddwyr Linux ar systemau PPC:
Bydd rhaid i ddefnyddwyr lwytho i lawr JRE IBM gan nad
yw Sun yn darparu JRE ar gyfer systemau PPC. Yn yr achos yma, llwytho i lawr o:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Gosod
* Defnyddwyr Windows:
Cychwynnwch y rhaglen osod. Os hoffech chi
gall y rhaglen osod greu llwybrau byr i gychwyn OmegaT

*Defnyddwyr Linux:
Rhowch yr archif mewn unrhyw ffolder addas a'i ddadbacio: Mae OmegaT
wedyn yn barod i'w gychwyn. Mae modd creu gosodiad mwy twt a chyfeillgar drwy
ddefnyddio sgript gosod (linux-install.sh). I ddefnyddio'r 
sgript, agorwch ffenestr terfynell (consol), newid ffolder i'r un sy'n cynnwys
OmegaT.jar a'r sgript linux-install.sh a gweithredu'r sgript gyda  ./linux-install.sh.

*Defnyddwyr Mac:
Copïwch yr archif OmegaT.zip i leoliad addas a'i ddadbacio er mwyn cael
ffolder sy'n cynnwys ffeil mynegai dogfen HTML ac OmegaT.app,
y ffeil rhaglen. 

*Eraill (e.e. Solaris, FreeBSD):
I osod OmegaT crëwch ffolder addas ar gyfer OmegaT. Copïwch archif  zip neu tar.gz OmegaT i'r ffolder yma a'i ddadbacio.

3.3 Cychwyn OmegaT
Cychwynnwch OmegaT fel a ganlyn.

*Defnyddwyr Windows:
Os ydych wedi creu llwybr byr ar y bwrdd gwaith wrth osod,
cliciwch ar hwnnw. Fel arall rhowch glic dwbl i'r ffeil
OmegaT.exe Os ydych yn gallu gweld OmegaT ond nid OmegaT.exe yn eich
Rheolwr Ffeiliau (Windows Explorer), newidiwch y gosodiadau
fel bo estyniadau'r ffeiliau'n cael eu dangos.

*Defnyddwyr Linux:
Os ydych wedi defnyddio'r sgript gosod, dylech fod yn gallu cychwyn OmegaT gyda:
Alt+F2
ac yna:
omegat

*Defnyddwyr Mac:
Clic dwbl ar y ffeil OmegaT.app.

*O'ch rheolwr ffeiliau(pob system):
Clic dwbl ar ffeil OmegaT.jar. Dim os yw'r ffeil .jar wedi ei chysylltu
â Java yn eich system y bydd hwn yn gweithio.

*O'r linell orchymyn (pob system):
Y gorchymyn i gychwyn OmegaT yw:

cd <ffolder lle mae ffeil OmegaT.jar wedi ei lleoli>

<enw a llwybr ffeil weithredol Java> -jar Omega.jar

(Ffeil weithredol Java yw'r ffeil java ar Linux a java.exe yn Windows.
Os yw Java wedi ei osod ar lefel y system, nid oes angen gosod
y llwybr llawn)

Cyfaddasu eich profiad o gychwyn OmegaT:

*Defnyddwyr Windows:
Mae'r rhaglen osod yn gallu creu llwybraubyr ar eich cyfer yn y
ddewislen cychwyn, ar y bwrdd gwaith ac yn y maes cychwyn cyflym Mae modd llusgo ffeil
OmegaT.exe i'r ddewislen cychwyn, y bwrdd gwaith neu'r ardal cychwyn cyflym
i gysylltu iddi o'r mannau hynny.

*Defnyddwyr Linux:
Am ffordd mwy cyfeillgar o gychwyn OmegaT, mae modd defnyddio sgript
Kaptain, sy'n cael ei ddarparu (omegat.kaptn). I ddefnyddio'r sgript rhaid gosod
Kaptain yn gyntaf. Yna mae modd cychwyn sgript cychwyn Kaptain gyda
Alt+F2
omegat.kaptn

Am ragor o wybodaeth ar sgript Kaptain ac ychwanegu eitemau dewislen
a chychwyn eiconau yn Linux, ewch i OmegaT ar Linux HowTo.

*Defnyddwyr Mac:
Llusgwch OmegaT.app i'ch doc neu i far offer ffenestr Finder er mwyn gallu
ei gychwyn o unrhyw leoliad. Mae hefyd modd ei alw ym
maes chwilio Spotlight.

==============================================================================
 4. Ymuno â phroject OmegaT

I gyfrannu at ddatblygiad OmegaT, cysylltwch â'r datblygwyr yn:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

I gyfieithu rhyngwyneb, llawlyfr defnyddiwr neu ddogfennau perthnasol eraill OmegaT, darllenwch:
      
      http://www.omegat.org/en/translation-info.html

Tanysgrifiwch i'r rhestr cyfieithwyr:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

I gyfrannu mewn ffyrdd eraill, tanysgrifiwch yn gyntaf i'r gr?p defnyddwyr yn:
      http://tech.groups.yahoo.com/group/omegat/

I glywed am beth sy'n digwydd ym myd OmegaT...

  Gwaith gwreiddiol Keith Godfrey yw OmegaT.
  Marc Prior yw cyd-drefnydd project OmegaT.

Mae'r cyfranwyr blaenorol yn cynnwys:

Cyfrannwyd cod gan
  Zoltan Bartko
  Volker Berlin
  Didier Briel (rheolwr datblygu)
  Kim Bruning
  Alex Buloichik (datblygwr arweiniol)
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
  FabiÃ¡n Mandelbaum
  John Moran
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Briac PilprÃ©
  Tiago Saboga
  Andrzej SawuÅ‚a
  Benjamin Siband
  Yu Tang
  Rashid Umarov  
  Antonio Vilei
  Martin Wunderlich
  Michael Zakharov

Cyfraniadau eraill gan
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (rheolwr lleoleiddio)
  Vito Smolej (rheolwr dogfennau)
  Samuel Murray
  Marc Prior 
  a llawer iawn o bobl gymwynasgar

(Os ydych yn credu eich bod wedi cyfrannu mewn ffordd arwyddocaol i Broject OmegaT a heb gael eich enwi ar y rhestr, cysylltwch â ni.)

Mae OmegaT yn defnyddio'r llyfrgelloedd canlynol:
  HTMLParser 1.6 gan Somik Raha, Derrick Oswald ac eraill (Trwydded LGPL)
  MRJ Adapter 1.0.8 gan Steve Roy (Trwydded LGPL)
  VLDocking Framework 2.0.6d gan VLSolutions (Trwydded CeCILL)
  Hunspell by LÃ¡szlÃ³ NÃ©meth ac eraill (Trwydded LGPL)
  JNA gan Todd Fast, Timothy Wall ac eraill (Trwydded LGPL)
  Swing-Layout 1.0.2 (Trwydded LGPL)
  Jmyspell 2.1.4 (Trwydded LGPL)
  SVNKit 1.7.5 (Trwydded TMate)
  Sequence Library (Trwydded Sequence Library)
  ANTLR 3.4 (Trwydded ANTLR 3)
  SQLJet 1.1.3 (GPL v2)
  JGit (Trwydded Eclipse Distribution)
  JSch (Trwydded JSch)
  Base64 (parth cyhoeddus)
  Diff (GPL)
  JSAP (LGPL)
  orion-ssh2-214 (Trwydded Java Orion SSH)

==============================================================================
 5.  A yw OmegaT yn eich poeni chi? Ydych chi angen cymorth?

Cyn adrodd ar wall, gwnewch yn si?r eich bod wedi darllen y ddogfennaeth yn fanwl. Efallai bod yr hyn rydych yn ei weld yn nodwedd o OmegaT rydych newydd ei ddarganfod. Os ewch i edrych ar gofnod OmegaT a gweld geiriau fel
"Error", "Warning", "Exception", neu "died unexpectedly" yna rydych wedi darganfod problem go iawn (mae log.txt i'w weld yn ffolder dewisiadau defnyddiwr, gw. y llawlyfr am ei leoliad).

Y peth nesaf i'w wneud yw cadarnhau'r hyn rydych wedi ei ganfod gyda defnyddwyr i wneud yn si?r nad yw hyn wedi ei adrodd o'r blaen. Mae modd gwirio'r dudalen cofnod gwall yn SourceForge hefyd. Dim ond pan rydych yn si?r mai chi yw'r cyntaf i ganfod cyfres o ddigwyddiadau mae modd eu hail greu sydd wedi cychwyn rhywbeth nad yw i fod i ddigwydd y dylech anfon adroddiad gwall.

Mae pob adroddiad gwall da angen tri peth.
  - Camau i'w hatgynhyrchu
   - Beth i ddisgwyl ei weld, a
   -Beth welwyd yn lle hynny.

Gallwch ychwanegu copïau o ffeiliau, darnau o'r log, lluniau sgrin, unrhyw beth rydych yn ei feddwl bydd o gymorth i'r datblygwyr i ganfod a chywiro eich gwall.

I bori archifau y gr?p defnyddwyr ewch i:
     http://groups.yahoo.com/group/OmegaT/

I bori'r dudalen cofnodi gwall ac i gofnodi adroddiad gwall os oes angen, ewch i:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

I gadw cofnod o'r hyn sy'n digwydd i'ch adroddiad gwall, cofrestrwch fel defnyddiwr SourceForge.

==============================================================================
6.   Manylion ryddhau

Gwelwch y ffeil 'changes.txt'  am wybodaeth fanwl am newidiadau yn y ryddhad yma a'r rhai blaenorol.


==============================================================================
