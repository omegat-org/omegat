Gwaith Rhoslyn Prys yw'r cyfieithiad hwn, copyright© 2009

==============================================================================
  Ffeil Darllen OmegaT 1.8

  1.  Gwybodaeth am OmegaT
  2.  Beth yw OmegaT?
  3.  Gosod OmegaT
  4.  Cyfraniadau at OmegaT
  5.  Ydi OmegaT yn eich poeni chi? Ydych chi angen cymorth?
  6.  Manylion ryddhau

==============================================================================
  1.  Gwybodaeth am OmegaT


Mae'r wybodaeth ddiweddaraf am OmegaT i'w gael yn 
(yn Saesneg, Slofac, Is Almaeneg, Portiwgaleg):
      http://www.omegat.org/omegat/omegat.html

Cefnogaeth defnyddwyr yn grŵp defnyddwyr Yahoo (amlieithog) lle mae archifau chwiliadwy ar gael heb danysgrifiad:
     http://groups.yahoo.com/group/OmegaT/

Cais am Welliannau (yn Saesneg), ar safle SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Negeseuon gwall (yn Saesneg), safle SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Beth yw OmegaT?

Offeryn Cyfieithu gyda Chymorth Cyfrifiadur yw OmegaT (CAT - Computer Assisted Translation tool). Mae'n rhydd a rhad, sef does dim angen talu amdano, hyd yn oed ar gyfer gwaith proffesiynol a'ch bod yn rhydd i'w addasu a/neu ei addasu cyn belled â'ch bod yn parchu'r drwydded defnyddiwr.

Prif nodweddion OmegaT yw
  - gallu rhedeg ar unrhyw system weithredu sy'n cynnal Java
  - gallu defnyddio unrhyw ffeil TMX dilys fel cyfeirnod cyfieithu
  - segmentu brawddegau hyblyg (gan ddefnyddio dull SRX)
  - chwilio o fewn y project a'r cof cyfieithu penodol
  -chwilio o fewn unrhyw is-gyfeiriadur gan gynnwys ffeiliau mae OmegaT yn gallu eu darllen
  - cydweddu bras gyfieithiadau
  - trin projectau sylweddol gan gynnwys hierarchaethau cyfeiriaduron cymhleth
  - cynnal geirfaoedd (ar gyfer gwirio termau)
  - dogfennaeth a thiwtorial hawdd eu defnyddio
  - lleoleiddio mewn nifer o ieithoedd.

Mae OmegaT yn cynnal ffeiliau OpenDocument, Microsoft Office (gan ddefnyddio OpenOffice.org fel hidl trosi, neu drwy eu trosi i HTML), ffeiliau OpenOffice.org neu StarOffice.org, yn ogystal â (X)HTML, ffeiliau lleoleiddio Java, ffeiliau testun plaen a mwy.

Bydd OmegaT yn didoli'n awtomatig y cyfeiriaduron mwyaf cymhleth i gael mynediad at y ffeiliau sy'n cael eu cynnal a chynhyrchu cyfeiriadur targed gyda'r union yr un strwythur, gan gynnwys copïau o unrhyw ffeiliau sy ddim yn cael eu cynnal.

Er mwyn cychwyn yn gyflym, cychwynnwch OmegaT ac yna darllen y Tiwtorial Cychwyn Cyflym.

Mae'r llawlyfr defnyddiwr o fewn y pecynnau rydych newydd eu llwytho i lawr ac mae modd cael mynediad ato drwy'r ddewislen [Cymorth] wedi i chi gychwyn OmegaT.

==============================================================================
 3. Gosod OmegaT

3.1 Cyffredinol Er mwyn rhedeg, mae OmegaT angen y Java Runtime Environment fersiwn 1.4 (JRE) neu uwch ar eich cyfrifiadur. Mae OmegaT nawr yn cael ei ddarparu gyda'r Java Runtime Envirnment er mwyn arbed trafferth i ddefnyddwyr o ddewis, a'i osod. Defnyddwyr Windows a Linux: os ydych yn hyderus fod eich system yn cynnwys fersiwn addas o'r JRE, gallwch osod y fersiwn o OmegaT sydd heb y JRE (mae hwn yn cael ei ddynodi gan enw'r fersiwn, "Without_JRE"). Os oes unrhyw amheuaeth, rydym yn argymell eich bod yn defnyddio'r fersiwn "safonol". Mae hwn yn ddigon diogel gan na fydd y fersiwn yma yn effeithio ar y fersiwn o'r JRE ar eich system. 
Defnyddwyr Linux: sylwch nad yw OmegaT yn gweithio gyda'r fersiwn rhydd/cod agored o Java sydd o fewn amryw o ddosbarthiadau Linux (e.e., Ubuntu) gan eu bod un ai yn hen neu anghyflawn. Llwytho i lawr neu osod Sun's Java Runtime Environment (JRE) drwy'r cyswllt uchod, neu lwytho i lawr a gosod OmegaT wedi ei becynnu gyda JRE ( y bwndel .tar.gz wedi ei farcio "Linux")
Defnyddwyr Mac: mae'r JRE eisoes wedi ei osod ar y Mac OS X. Defnyddwyr PowerPC: bydd angen llwytho i lawr fersiwn IBM o'r JRE, gan nad yw Sun yn darparu JRE ar gyfer systemau PPC. Yn yr achos yma, llwytho o:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Gosod 
Er mwyn gosod OmegaT crëwch ffolder addas ar gyfer OmegaT (e.e. C:Program Files OmegaT yn Windows neu  /usr/local/lib yn Linux). Copïo’r archif OmegaT.zip i'r ffolder a'i datgywasgu yno.

3.3 Cychwyn OmegaT 
Gall OmegaT gael ei gychwyn mewn nifer o ffyrdd gwahanol.
* Defnyddwyr Windows: rhoi clic dwbl i'r ffeil OmegaT-JRE.exe, os ydych yn defnyddio'r fersiwn sy'n cynnwys y JRE, neu fel arall yr OmegaT.exe.

* Drwy rhoi clic dwbl ar OmegaT.bat . Os ydych yn gallu gweld y ffeil OmegaT ond nid OmegaT.bat yn eich Rheolwr Ffeiliau (Windows Explorer), newidiwch y gosodiadau fel bod estyniadau ffeiliau'n cael eu dangos.

* Drwy roi clic dwbl ar OmegaT.jar. Bydd hyn dim ond yn gweithio os yw'r math yma o ffeil wedi ei gysylltu â Java ar eich system.

* O'r llinell orchymyn. Y gorchymyn i gychwyn OmegaT yw:

cd <ffolder lle mae ffeil OmegaT.jar wedi ei leoli>

<enw a llwybr y ffeil Java gweithredol> -jar OmegaT.jar

(Y ffeil Java gweithredol yw;'r ffeil java ar Linux a java.exe ar Windows.
Os yw Java wedi ei osod ar lefel system, nid oes angen y llwybr llawn.)

Defnyddwyr Windows: gallwch lusgo'r ffeiliau OmegaT-JRE.exe, OmegaT.exe neu OmegaT.bat i'r bwrdd gwaith neu'r ddewislen Cychwyn i'w gysylltu o'r fan honno.

* Defnyddwyr Linux KDE: gallwch ychwanegu OmegaT i'ch dewislenni fel hyn:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Yna ar ôl dewis dewislen addas, ychwanegu is ddewislen/eitem gyda File - New 
Submenu and File - New Item. Rhowch OmegaT fel enw'r eitem newydd.

Yn y maes "Command", defnyddiwch y botwm symud i ddod o hyd i'ch sgript cychwyn OmegaT a'i dewis. 

Cliciwch ar y botwm eicon (i'r dde o'r meysydd Name/Description/Comment - Other Icons - Browse, a symud i is ffolder /images yn ffolder rhaglen OmegaT  Dewis eicon OmegaT.png

Yna, dewis y newid drwy File - Save.

Defnyddwyr Linux GNOME: gallwch ychwanegu OmegaT i'ch panel (y bar ar frig y sgrin) fel hyn:

Clic de i'r panel - - Ychwanegu at y Panel... Rhowch "OmegaT" yn y maes "Enw"; yn y maes "Gorchymyn" defnyddiwch y botwm symud i ddod o hyd i sgript cychwyn OmegaT. Dewiswch hwnnw a chlicio Iawn

==============================================================================
 4. Cyfraniadau at OmegaT

 I gyfrannu at ddatblygiad OmegaT, cysylltwch â'r datblygwyr yn:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

I gyfieithu rhyngwyneb, llawlyfr defnyddiwr neu ddogfennau perthnasol eraill OmegaT,
      http://www.omegat.org/omegat/omegat_en/translation-info.html

Tanysgrifiwch i'r rhestr cyfieithwyr:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Am gyfraniadau eraill, tanysgrifiwch i'r grŵp defnyddwyr:
      http://tech.groups.yahoo.com/group/omegat/

Ac i ddeall beth sy'n digwydd ym myd OmegaT...

  Gwaith gwreiddiol Keith Godfrey yw OmegaT.
  Marc Prior yw cyd-drefnydd project OmegaT.

Mae cyfranwyr blaenorol yn cynnwys:
(trefn yr wyddor)

Cyfrannwyd cod gan
  Zoltan Bartko
  Didier Briel (rheolwr rhyddhau)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Thomas Huriaux
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

Cyfraniadau eraill gan
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (rheolwr dogfennau)
  Samuel Murray
  Marc Prior (rheolwr lleoleiddio)
  a llawer iawn o bobl gymwynasgar eraill

(Os ydych yn credu eich bod wedi cyfrannu at broject OmegaT mewn ffordd arwyddocaol ond nid yw eich enw ar y rhestr, cysylltwch â ni.)

Mae OmegaT yn defnyddio'r llyfrgelloedd canlynol:

  HTMLParser gan Somik Raha, Derrick Oswald ac eraill (Trwydded LGPL)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 gan Steve Roy (Trwydded LGPL)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.0.6d gan VLSolutions (Trwydded CeCILL)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell 1.1.12 gan László Németh ac eraill (Trwydded LGPL)

  JNA gan Todd Fast, Timothy Wall ac eraill (Trwydded LGPL)

  Swing-Layout 1.0.2 (Trwydded LGPL)

  Backport-util-concurrent (Parth Cyhoeddus)

  Retroweaver 2.0.1 (Trwydded Retroweaver)

  Jmyspell 2.1.4 (Trwydded LGPL)

==============================================================================
 5.  Ydi OmegaT yn eich poeni chi? Ydych chi angen cymorth?

Cyn adrodd ar wall, gwnewch yn siŵr eich bod wedi darllen y ddogfennaeth. Efallai eich bod wedi dod ar draws yr hyn sy'n nodwedd o OmegaT. Os ydych yn gweld y geiriau "Error", "Warning", "Exception", neu "died unexpectedly" yn log OmegaT, yna mae lle i gredu eich bod ar y trywydd iawn (Mae'r log.txt wedi ei leoli yng nghyfeiriadur dewisiadau defnyddwyr, gweler y canllaw am ei leoliad).

Y peth nesaf i'w wneud yw cadarnhau'r hyn rydych wedi ei ganfod gyda defnyddwyr eraill i wneud yn siŵr nad yw wedi ei gofnodi ynghynt.  Mae modd gwirio'r dudalen cofnod gwall yn SourceForge hefyd. Dim ond pan rydych yn siwr mai chi yw'r cyntaf i ganfod cyfres o ddigwyddiadau mae modd eu hail greu sy'n cychwyn  rhywbeth sydd ddim i fod i ddigwydd y dylech anfon cofnod gwall.

Mae pob adroddiad gwall angen tri pheth.
  - Camau i'w atgynhyrchu
  - Beth i ddisgwyl ei weld, a
  - Beth welwyd yn lle hynny.

Gallwch ychwanegu copïau o ffeiliau, darnau o'r log, lluniau sgrin, unrhyw beth ydych yn ei feddwl bydd o gymorth i'r datblygwyr i ganfod a chywiro eich gwall

I bori archifau'r grŵp defnyddwyr ewch i:
     http://groups.yahoo.com/group/OmegaT/

I bori'r dudalen cofnodi gwall ac i gofnodi adroddiad gwall os oes angen, ewch i:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

I gadw cofnod o'r hyn sy'n digwydd i'ch adroddiad gwall, cofrestrwch fel defnyddiwr SourceForge

==============================================================================
6.   Manylion ryddhau

Gwelwch y ffeil 'changes.txt'  am wybodaeth fanwl am newidiadau yn hwn a phob rhyddhad blaenorol.

==============================================================================

