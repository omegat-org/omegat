Esta tradución é traballo de Francisco Diéguez, Enrique Estévez e Isaac Álvarez, copyright© 2013

==============================================================================
  OmegaT 3.0, ficheiro Léeme

  1.  Información sobre OmegaT
  2.  Que é OmegaT?
  3.  Instalar OmegaT
  4.  Colaboracións a OmegaT
  5.  Ten fallos o seu OmegaT? Precisa axuda?
  6.  Detalles da versión

==============================================================================
  1.  Información sobre OmegaT


Pode atopar información máis actualizada de OmegaT en
      http://www.omegat.org/

Pode atopar asistencia para o usuario no grupo de usuarios de Yahoo, onde se 
pode buscar nos arquivos sen precisar subscrición:
     http://groups.yahoo.com/group/OmegaT/

Se quere facer algunha petición de mellora (en inglés), pode facelo no sitio de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Para informar dalgún erro atopado (en inglés), pode facelo no sitio web de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Que é OmegaT?

OmegaT é unha ferramenta de tradución asistida por computador (CAT). É de balde, é dicir, 
non ten que pagar nada para poder empregala, aínda que sexa para uso 
profesional e é libre de modificala e/ou redistribuíla mentres 
respecte a licenza de usuario.

As principais características de OmegaT son:
  - posibilidade de executarse en calquera sistema operativo que admita Java
  - empregar calquera ficheiro TMX como referencia para a tradución
  - segmentación de frases flexíbel (empregando un método semellante ao SRX)
  - busca no proxecto e nas memorias de tradución de referencia
  - busca de ficheiros cos formatos compatíbeis en calquera cartafol 
  - coincidencia parcial
  - xestión intelixente de proxectos incluíndo xerarquías de cartafoles complexas
  - compatibilidade para glosarios (comprobación de terminoloxía) 
  - compatibilidade con correctores ortográficos de código aberto mentres escribes
  - compatibilidade con dicionarios StarDict
  - compatibilidade para servizos de tradución automática de Google Translate
  - documentación e titoriais claros e comprensíbeis
  - localización a moitos idiomas.

OmegaT admite os seguintes formatos de ficheiro:

- Formatos de ficheiro de texto plano

  - Texto ASCII (.txt, etc.)
  - Texto codificado (*.UTF8)
  - Paquetes de recursos Java (*.properties)
  - Ficheiros PO (*.po)
  - Ficheiros (clave=valor) INI (*.ini)
  - Ficheiros DTD (*.DTD)
  - Ficheiros DocuWiki (*.txt)
  - Ficheiros de título SubRip (*.srt)
  - Magento CE Locale CSV (*.csv)

- Formatos de ficheiros de texto etiquetados

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - Compilador de axuda HTML (*.hhc, *.hhk)
  - DocBook (*.xml)
  - XLIFF monolingüe (*.xlf, *.xliff, *.sdlxliff)
  - QuarkXPress CopyFlowGold (*.tag, *.xtg)
  - Ficheiros ResX (*.resx)
  - Recursos para Android (*.xml)
  - LaTex (*.tex, *.latex)
  - Ficheiros de Axuda (*.xml) e Manuais (*.hmxp)
  - Typo3 LocManager (*.xml)
  - Localización WiX (*.wxl)
  - Iceni Infix (*.xml)
  - Exportación a Flash XML (*.xml)
  - TXML de Wordfast (*.txml)
  - Camtasia para Windows (*.camproj)
  - Visio (*.vxd)

OmegaT pode ser personalizado para que admita tamén outros ficheiros.

OmegaT analizará automaticamente as xerarquías máis complexas do cartafol de orixe 
para acceder a todos os ficheiros compatíbeis e produce un ficheiro de destino con 
exactamente a mesma estrutura, incluíndo as copias dos ficheiros non compatíbeis.

Para un titorial de inicio rápido, inicie OmegaT e lea o titorial de Inicio rápido 
mostrado.

O manual de usuario está no paquete que descargou, pode acceder a el no 
menú de [Axuda] despois de iniciar OmegaT.

==============================================================================
 3. Instalar OmegaT

3.1 Xeral
Para executar OmegaT requírese que o contorno de execución Java (JRE) na versión 
1.5 ou superior estea instalado no seu sistema. Os paquetes de OmegaT que inclúen
o contorno de execución Java están dispoñíbeis para aforrar aos usuarios o traballo de
seleccionalos, obtelos e instalalos. 

Se xa ten instalado Java, un xeito de instalar a versión actual de
OmegaT é usar Java Web Start. 
Para este propósito descargue o seguinte ficheiro e despois execúteo:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Instalará o contorno correcto para o seu computador e executará a aplicación por
primeira vez. As seguintes chamadas non necesitan que estea conectado á rede.

Durante a instalación, dependendo do seu sistema operativo, pode recibir varios
avisos de seguridade. O certificado está autoasinado por «Didier Briel». 
Os permisos que vostede lle dá a esta versión (que se mencionan como un «acceso sen restrición ao computador») son idénticos aos permisos que lle dá a versión local, instalada por un procedemento descrito máis adiante: estes permítenlle o acceso ao disco ríxido do computador. Os clics seguintes en OmegaT.jnlp
comprobarán calquera actualización, e se está conectado á rede, instalaraas se existen,
para posteriormente arrincar OmegaT 

As formas alternativas de descargar e instalar OmegaT móstranse máis adiante: 

Usuarios de Windows e Linux: se está seguro que o seu sistema xa ten instalada 
unha versión axeitada de JRE, pode instalar a versión de OmegaT sen o JRE (que se 
indica no nome de versión, «Without_JRE»). 
Se ten calquera dúbida, recomendámoslle empregar a versión co JRE incluído. Isto é totalmente seguro, aínda que xa teña instalado o JRE no seu sistema, 
esta versión non interferirá con el.

Usuarios de Linux:
OmegaT executarase sobre a implementación de código aberto de Java 
que veñen de serie nalgunhas distribucións Linux (por exemplo en Ubuntu), mais pode 
experimentar erros, problemas na interface ou indispoñibilidade de accións. Polo tanto, recomendamos
que descargue e instale ben o contorno de execución Java de Oracle (JRE)
ou o paquete OmegaT que ten incluído o JRE (o .tar.bz2) marcado con «Linux». Se instala unha versión de Java no propio sistema, debe 
asegurarse de que está na ruta de inicio, ou ben facer unha chamada explícita cando inicie
o OmegaT. Se non está familiarizado con Linux, recomendámoslle que 
instale unha versión de OmegaT con JRE incluído. Isto é totalmente seguro,
pois o JRE «local» non interferirá con ningún outro JRE instalado 
no seu sistema.

Usuarios de Mac:
O JRE xa ven instalado de serie nos Mac OS X previos á versión 10.7 (Lion). Os usuarios de Lion recibirán unha notificación do sistema cando inicien 
un aplicativo que requira Java e o sistema
automaticamente descargará e instalará o mesmo.

Usuarios de Linux en sistemas PowerPC:
precisan descargar o JRE de IBM, xa que Sun 
non fornece un JRE para os sistemas PPC. Descárgueo neste caso desde:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Instalación
* Usuarios de Windows:
Simplemente iniciar o programa de instalación. Se así o desexa, a 
instalación do programa pode crear unha ligazón para executar OmegaT.

* Usuarios de Linux:
Sitúe o ficheiro en calquera cartafol e extráiao; OmegaT xa está
listo para iniciarse. Con todo, pode obter unha instalación máis limpa e sinxela
empregando o script de instalación (linux-install.sh). Para empregar
este script, abra unha ventá da terminal (consola), cambie o cartafol ao cartafol
que conteña o ficheiro OmegaT.jar e o script linux-install.sh, e execute o 
script con ./linux-install.sh

* Usuarios de Mac: 
Copie o arquivo OmegaT.zip nun lugar axeitado e extráiao 
para obter un cartafol que contén un ficheiro índice de documentación HTML e 
OmegaT.app, o ficheiro do aplicativo.

* Outros (por exemplo, Solarios, FreeBSD):
Para instalar OmegaT, soamente cree un cartafol axeitado para OmegaT. Copie o 
arquivo zip ou tar.bz2  de OmegaT neste cartafol e extráiao nel.

3.3 Iniciando OmegaT
Inicie OmegaT como se lle indique:

* Usuarios de Windows:
Se durante a instalación creou un atallo no escritorio, 
faga dobre clic nese atallo. Senón, tamén pode facer dobre clic no ficheiro 
OmegaT.exe. Se pode ver o ficheiro OmegaT mais non o ficheiro OmegaT.exe no seu 
xestor de ficheiros (Explorador de Windows), cambie a configuración das 
extensións de ficheiros para mostralas.

* Usuarios de Linux:
Se empregou o script de instalación, debería poder iniciar OmegaT con:
Alt+F2
e despois:
omegat

* Usuarios de Mac:
Faga dobre clic no ficheiro OmegaT.app.

* Desde o xestor de ficheiros (todos os sistemas):
Faga dobre clic no ficheiro OmegaT.jar. Isto só funcionará se o tipo de 
ficheiro .jar está asociado a Java no seu sistema.

* Dende a liña de ordes (todos os sistemas):
A orde para iniciar OmegaT é:

cd <cartafol que contén o ficheiro OmegaT.jar>

<nome e camiño do ficheiro executábel de Java> -jar OmegaT.jar

(O ficheiro executábel de Java é o ficheiro java en Linux e java.exe en Windows.
Se Java está instalado no seu sistema e está na ruta da orde, non necesitará 
inserir toda a ruta.)

Personalizando a súa experiencia de inicio de OmegaT:

* Usuarios de Windows:
O programa de instalación pode crear ligazóns no menú de inicio, 
no escritorio e na área de inicio rápido. Tamén pode arrastrar 
o ficheiro OmegaT ao menú inicio, escritorio ou área de inicio rápido para ligalo 
desde alí.

* Usuarios de Linux:
Un xeito máis sinxelo de iniciar OmegaT é empregar o script Kaptain 
ofrecido no cartafol (omegat.kaptn). Para empregar este script primeiro necesita
instalar Kaptain. Unha vez feito, pode iniciar o script de inicio Kaptain con
Alt+F2
omegat.kaptn

Para máis información sobre o script Kaptain, na adición de entradas no menú e 
a iconas de inicio en Linux, diríxase á sección OmegaT en Linux HowTo.

Usuarios de Mac:
Arrastre o ficheiro OmegaT.app ao seu dock ou á barra de ferramentas dunha xanela do Finder
para inicialo dende calquera lugar. Tamén pode inicialo mediante 
o campo de busca de Spotlight.

==============================================================================
 4. Colaborar co proxecto OmegaT

Para participar no desenvolvemento de OmegaT, contacte cos desenvolvedores en:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Para traducir a interface de usuario de OmegaT, o manual de usuario ou calquera 
documento relacionado, lea: 
      
      http://www.omegat.org/en/translation-info.html

e subscríbase á lista de tradutores:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Para contribuír de calquera outro modo, subscríbase primeiro ao grupo de usuarios en:
      http://tech.groups.yahoo.com/group/omegat/

E infórmese un pouco do que acontece no mundo de OmegaT...

  OmegaT é un traballo orixinal de Keith Godfrey.
  Marc Prior é o coordinador do proxecto OmegaT.

Entre os contribuidores anteriores están:
(orde alfabético)

Código contribuído por
  Zoltan Bartko
  Volker Berlin
  Didier Briel (coordinador do desenvolvemento)
  Kim Bruning
  Alex Buloichik (desenvolvedor xefe)
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
  Rashid Umarov  
  Antonio Vilei
  Martin Wunderlich
  Michael Zakharov

Outras contribucións feitas por
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (coordinador de localización)
  Vito Smolej (coordinador de documentación)
  Samuel Murray
  Marc Prior 
  e moita, moita máis xente que axudou

(Se pensa que colaborou dabondo co Proxecto OmegaT
mais non ve o seu nome nesta lista, por favor, contacta connosco.)

OmegaT usa as seguintes bibliotecas:
  HTMLParser por Somik Raha, Derrick Oswald e outros (Licenza LGPL)
  MRJ Adapter 1.0.8 por Steve Roy (Licenza LGPL)
  VLDocking Framework 2.1.4 por VLSolutions (Licenza CeCILL)
  Hunspell por László Németh e outros (Licenza LGPL)
  JNA by Todd Fast, Timothy Wall e outros (Licenza LGPL)
  Swing-Layout 1.0.2 (Licenza LGPL)
  Jmyspell 2.1.4 (Licenza LGPL)
  SVNKit 1.7.5 (Licenza TMate)
  Sequence Library (Licenza Sequence Library)
  ANTLR 3.4 (Licenza ANTLR 3)
  SQLJet 1.1.3 (GPL v2)
  JGit (Licenza da distribución Eclipse)
  JSch (Licenza JSch)
  Base64 (dominio público)
  Diff (GPL)
  JSAP (LGPL)
  orion-ssh2-214 (SSH Orion para a licenza Java)
  lucene-*.jar (Licenza Apache 2.0)
  Os tokenizers ingleses (org.omegat.tokenizer.SnowballEnglishTokenizer e
  org.omegat.tokenizer.LuceneEnglishTokenizer) empregan palabras baleiras de Okapi http://okapi.sourceforge.net) (Licenza LGPL)
  tinysegmenter.jar (Licenza BSD modificada)
  commons-*.jar (Licenza Apache 2.0)
  jWordSplitter (Licenza Apache 2.0)
  LanguageTool.jar (Licenza LGPL)
  morfologik-*.jar (Licenza Morfologik)
  segment-1.3.0.jar (Licenza Segment)

==============================================================================
 5.  Ten fallos o seu OmegaT? Precisa axuda?

Antes de informar dun erro, asegúrese que comprobou a documentación. O que veu pode ser unha característica de OmegaT que acaba de descubrir. Se comproba o rexistro de OmegaT pode ver palabras 
como «Error», «Warning», «Exception», ou «died unexpectedly» polo que probabelmente 
descubriu un problema xenuíno (o log.txt localizase no cartafol de preferencias do 
usuario, vexa o manual para a súa localización).

O seguinte é confirmalo con outros usuarios, para asegurarse 
de que non se informou do mesmo erro anteriormente. Pode comprobar a páxina de informe 
de erros en Sourceforge. Só cando estea seguro de que atopou a secuencia do evento que 
dispara algo que non se supón que ten que acontecer debería enviar un informe 
de erro.

Un bo informe de erro precisa exactamente 3 cousas.
  - Pasos para reproducilo,
  - que esperaba ver, e
  - que viu no seu lugar.

Pode engadir copias de ficheiros, porcións do rexistro, capturas de pantalla, calquera 
cousa que pense que pode axudar aos desenvolvedores a atopar e arranxar o erro.

Para navegar polos arquivos do grupo de usuarios, vaia a:
     http://groups.yahoo.com/group/OmegaT/

Para abrir a páxina de informe de erros e arquivar un novo informe dun erro se é necesario vaia a:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Para manterse informado do que está acontecendo no seu informe de erro pode rexistrarse como usuario de SourceForge.

==============================================================================
6.   Detalles da versión

Vexa o ficheiro «changes.txt» para unha información máis detallada sobre os 
cambios nesta e anteriores versións.


==============================================================================
