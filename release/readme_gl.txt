Esta tradución é traballo de Francisco Diéguez e Enrique Estévez, copyright© 2010

==============================================================================
  OmegaT 2.0, ficheiro Léeme

  1.  Información sobre OmegaT
  2.  Que é OmegaT?
  3.  Instalando OmegaT
  4.  Colaboracións a OmegaT
  5.  Ten fallos o seu OmegaT? Precisas axuda?
  6.  Detalles da versión

==============================================================================
  1.  Información sobre OmegaT


Pode encontrar información máis actualizada de OmegaT en
      http://www.omegat.org/

Pode encontrar asistencia para o usuario no grupo de usuarios de Yahoo, onde se 
pode buscar nos arquivos sen precisar subscrición:
     http://groups.yahoo.com/group/OmegaT/

Se quere facer algunha petición de mellora (en Inglés), pode facelo no sitio de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Para informar dun erro atopado (en Inglés), pode facelo no sitio web de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Que é OmegaT?

OmegaT é unha ferramenta de tradución asistida por ordenador (CAT). É de balde, vostede 
non ten que pagar nada para poder empregala, aínda que sexa para uso 
profesional e vostede é libre de modificala e/ou redistribuíla mentres que 
respete a licenza de usuario.

As principais características de OmegaT son:
  - posibilidade de executarse en calquera sistema operativo que admita Java
  - empregar calquera ficheiro TMX como referencia para a tradución
  - segmentación de sentenzas flexíbel (empregando un método semellante ao SRX)
  - busca no proxecto e nas memorias de tradución de referencia
  - busca de ficheiros cos formatos compatíbeis en calquera cartafol 
  - coincidencia dubidosa
  - xestión intelixente de proxectos incluíndo xerarquías de cartafoles complexas
  - compatibilidade para glosarios (comprobación de terminoloxía) 
  - compatibilidade con correctores ortográficos Open Source mentres escribes
  - compatibilidade con dicionarios de tipo StarDict
  - compatibilidade para servizos de tradución automática de Google translate
  - documentación e titoriais limpos e comprensíbeis
  - localización a moitos idiomas.

OmegaT admite os seguintes formatos de ficheiro:
  - texto plano
  - HTML e XHTML
  - Compilador de axuda HTML
  OpenDocument/OpenOffice.org
  - Paquetes de recursos Java (.propierties)
  - Ficheiros Ini (ficheiros con pares chave=valor en calquera codificación=
  - Ficheiros PO
  - Formato de ficheiro de documentación DocBook
  - Ficheiros OpenXML de Microsoft
  - Ficheiros XLIFF monolingüaxe de Okapi
  - QuarkXPress CopyFlowGold
  Ficheiros de subtítulo (SRT)
  - ResX
  - Recurso Android
  - LaTeX
  - Typo3 LocManager
  - Axuda e manuais
  - Ficheiros de recursos de Windows RC
  - Mozilla DTD
  - DokuWiki

OmegaT pode ser personalizado para que admita outros ficheiros do mesmo modo.

OmegaT analizará automaticamente as xerarquías máis complexas do cartafol orixe , 
para acceder a todos os ficheiros compatíbeis, e produce un ficheiro destino con 
exactamente a mesma estrutura, incluíndo as copias dos ficheiros non compatíbeis.

Para un titorial de inicio rápido, lance OmegaT e lea o titorial de Inicio rápido 
mostrado.

O manual de usuario está no paquete que descargou, vostede pode acceder a el no 
menú de [Axuda] logo de iniciar OmegaT.

==============================================================================
 3. Instalando OmegaT

3.1 Xeral
Para executar OmegaT requírese que o contorno de execución Java (JRE) na versión 
1.5 ou superior estea instalado no seu sistema. OmegaT agora proporciona 
o contorno de execución de Java (JRE) para evitarlle aos usuarios os problemas 
de elixilo, obtelo e instalalo. 

Se xa ten instalado Java, a forma máis simple para instalar a versión actual de
OmegaT é usar Java Web Start. 
Para este propósito descarga o seguinte ficheiro e logo execútao:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Instalará o contorno correcto para o seu computador e executará o aplicativo por
primeira vez. As seguintes chamadas necesitan que este en liña

Durante a instalación, dependendo do seu sistema operativo, pode recibir varios
avisos de seguridade. O certificado está autoasinado por "Didier Briel". 
Os permisos que vostede lle da a esta versión (que se mencionan como un "acceso sen restrición ao computador") son identicos aos permisos que lle da a versión local, instalada por un procedemento descrito máis tarde: eles permítenlle o acceso ao disco ríxido do computador. Os clics seguintes en OmegaT.jnlp comprobarán calquera anovación, e se está conectado instalaraas se existen, para posteriormente arrincar OmegaT 

As formas alternativas de descargar e instalar OmegaT móstranse debaixo: 

Usuarios de Windows e Linux: se está seguro que o seu sistema xa ten instalada 
unha versión axeitada do JRE, pode instalar a versión de OmegaT sen o JRE (que se 
indica no nome de versión, "Without_JRE"). 
Se ten dúbidas, recomendámoslle que empregue a versión "estándar", 
p.ex. con JRE. Isto é totalmente seguro, aínda que xa teña instalado o JRE 
no seu sistema, esta versión non interferirá con el.

Usuarios de Linux: asegúrese que OmegaT non se executa coas implementacións 
libres/fontes-abertas de Java que veñen de serie en algunhas distribucións de Linux 
(por exemplo en Ubuntu), xa que estas normalmente están desactualizadas ou incompletas. Descargar e instalar
o contorno de execución Java de Sun (JRE) coa ligazón de arriba, ou descargue e 
instale a versión de OmegaT que inclúe o JRE (o paquete .tar.gz marcado con 
"Linux").

Usuarios de Mac: O JRE xa ven instalado en Mac OS X de serie.

Usuarios de Linux en sistemas PowerPC: precisan descargar o JRE de IBM, 
xa que Sun non fornece un JRE para os sistemas PPC. Descárgueo neste caso desde:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Instalación
* Usuarios de Windows: Simplemente iniciar o programa de instalación. Se quere, a 
instalación do programa pode crear unha ligazón para executar OmegaT.
* Outros: para instalar OmegaT, simplemente cree un cartafol para OmegaT 
(p.ex. /usr/local/lib en Linux). Copie o arquivo zip ou tar.gz 
de OmegaT neste cartafol e descomprímao nel.

3.3 Iniciando OmegaT
OmegaT pódese iniciar de distintas formas.

* Usuarios de Windows: facendo dobre clic no ficheiro OmegaT.exe Se pode 
ver o ficheiro OmegaT pero non o ficheiro OmegaT.exe no seu xestor de ficheiros (Explorador 
de Windows), cambie a configuración das extensións de ficheiros para mostralas.

* Facendo dobre clic no ficheiro OmegaT.jar. Isto só funcionará se o tipo de 
ficheiro .jar o ten asociado a Java no seu sistema.

* Desde a liña de ordes. A orde para iniciar OmegaT é:

cd <cartafol que contén o ficheiro OmegaT.jar

<nome e camiño do ficheiro executábel de Java) -jar OmegaT.jar

(O ficheiro executábel de java é o ficheiro java en Linux e java.exe en Windows.
Se Java está instalado a nivel do sistema, non se precisa especificar o camiño completo.)

* Usuarios de Windows: o programa de instalación pode crear ligazóns no 
menú de inicio, no escritorio e na área de inicio rápido. Tamén pode arrastrar 
o ficheiro OmegaT ao menú inicio, escritorio ou área de inicio rápido para ligalo 
desde alí.

* Usuarios de KDE en Linux: pode engadir OmegaT aos seus menús do seguinte xeito:

Centro de control - Escritorio - Paneis - Menús - Editar menú de KDE - Ficheiro - Novo elemento/Novo submenú.

Entón, logo de seleccionar o menú axeitado, engada un submenú/elemento con 
Ficheiro - Novo submenú e Ficheiro - Novo elemento. Insira OmegaT como nome do novo elemento.

No campo "Orde", empregue o botón de navegación para buscar o script de 
lanzamento de Omegat, e seleccióneo. 

Prema no botón da icona (á dereita dos campos Nome/Descrición/Comentario) 
- Outras Iconas - Examinar, e navegue ao subcartafol  /images no cartafol do 
aplicativo OmegaT. Seleccione a icona OmegaT.png

E por último, garde os cambios con Ficheiro - Gardar.

* Usuarios de GNOME en Linux: pode engadir OmegaT ao sus panel (a barra de 
arriba da pantalla) do seguinte xeito:

Prema no botón dereito no panel e logo Engadir novo lanzador. Insira "OmegaT" no campo
"Nome"; no campo "Orde", empregue o botón de navegación para buscar o script de 
lanzar OmegaT. Seleccióneo e confirme con Aceptar.

==============================================================================
 4. Colaborar no proxecto OmegaT

Para participar no desenvolvemento de OmegaT contacte cos desenvolvedores en:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Para traducir a interface de usuario de OmegaT, o manual de usuario ou calquera 
documento relacionado, lea: 
      
      http://www.omegat.org/en/translation-info.html

e subscríbase á lista de tradutores:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Para calquera tipo de contribución, subscríbase primeiro ao grupo de usuarios en:
      http://tech.groups.yahoo.com/group/omegat/

E infórmese un pouco do que acontece no mundo de OmegaT...

  OmegaT é un traballo orixinal de Keith Godfrey.
  Marc Prior é o coordinador do proxecto OmegaT.

Entre os contribuidores anteriores están:
(orde alfabético)

Código contribuído por
  Zoltan Bartko
  Volker Berlin
  Didier Briel (coordinador de lanzamento)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Ibai Lakunza Velasco
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Antonio Vilei
  Martin Wunderlich

Outras contribucións feitas por
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (coordinador de localización)
  Vito Smolej (coordinador de documentación)
  Samuel Murray
  Marc Prior 
  e moita, moita máis xente que axudou

(Se pensas que colaboraches dabondo ao Proxecto OmegaT
pero non ves o teu nome nesta lista, por favor contacta connosco.)

OmegaT usa as seguintes bibliotecas:

  HTMLParser por Somik Raha, Derrick Oswald e outros (Licenza LGPL)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 por Steve Roy (Licenza LGPL)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 por VLSolutions (Licenza CeCILL)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell por László Németh e outros (Licenza LGPL)

  JNA by Todd Fast, Timothy Wall e outros (Licenza LGPL)

  Swing-Layout 1.0.2 (Licenza LGPL)

  Jmyspell 2.1.4 (Licenza LGPL)

  JAXB 2.1.7 (GPLv2 + excepción de classpath)

==============================================================================
 5.  Ten fallos o seu OmegaT? Precisas axuda?

Antes de informar dun erro, asegúrese que comprobou a documentación. O que veu pode ser unha característica de OmegaT que acaba de descubrir. Se comproba o rexistro de OmegaT pode ver palabras 
como "Erro", "Aviso", "Excepción", ou "died unexpectedly" polo que probabelmente 
descubriu un problema xenuíno (o log.txt localizase no cartafol de preferencias do 
usuario, vexa o manual para a súa localización).

A seguinte cousa a facer é confirmalo con outros usuarios, para asegurarse 
que non se informou do mesmo erro anteriormente. Pode comprobar a páxina de informe 
de erros en Sourceforge. So cando estes seguro de que atopaches a secuencia do evento que 
dispara algo que non se supón que ten que acontecer deberías enviar un informe 
de erro.

Un bo informe de erro precisa exactamente 3 cousas.
  - Pasos para reproducilo,
  - que esperaba ver, e
  - que viu no seu lugar.

Pode engadir copias de ficheiros, porcións do rexistro, capturas de pantalla, calquera 
cousa que pense que pode axudar aos desenvolvedores a encontrar e arranxar o erro.

Para navegar polos arquivos do grupo de usuarios, vaia a:
     http://groups.yahoo.com/group/OmegaT/

Para abrir a páxina de informe de erros e arquivar un novo informe dun erro se é necesario vaia a:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Para manterse informado do que está acontecendo no seu informe de erro pode rexistrarse como usuario de Source Forge.

==============================================================================
6.   Detalles da versión

Olle o ficheiro "changes.txt" para unha información máis detallada sobre os 
cambios nesta e anteriores versións.


==============================================================================
