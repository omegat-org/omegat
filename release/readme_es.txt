Esta  traducción es obra de  Nacho Pacheco  (nachopacheco  arroba  gmail.com),
copyright © 2010-2012.



==============================================================================

  Archivo Léeme de OmegaT 2.0





  1.  Información acerca de OmegaT

  2.  ¿Qué es OmegaT?

  3.  Instalando OmegaT

  4.  Aportando a OmegaT

  5.  ¿OmegaT te está complicando la vida? ¿Necesitas ayuda?

  6.  Detalles sobre esta versión



==============================================================================

  1.  Información acerca de OmegaT





Puedes encontrar la información más actualizada acerca de OmegaT en

      http://www.omegat.org/es/omegat.html



Asistencia a  usuarios en el grupo  de usuarios de Yahoo  (en varios idiomas),

donde puedes buscar en los archivos sin suscripción:

     http://groups.yahoo.com/group/OmegaT/



Solicitudes de mejoras (en Inglés), en el sitio de SourceForge:

     http://sourceforge.net/tracker/?group_id=68187&atid=520350



Informe de fallos (en Inglés), en el sitio web de SourceForge:

     http://sourceforge.net/tracker/?group_id=68187&atid=520350



==============================================================================

  2.  ¿Qué es OmegaT?



OmegaT  es una  herramienta de  traducción  asistida por  ordenador (TAO).  Es

libre,  es decir  no  tienes que  pagar  nada para  usarla,  incluso para  uso

profesional,  y eres  libre de  modificar  y/o redistribuir  siempre y  cuando

respetes la licencia de usuario.



Las principales características de OmegaT son:

  - la  habilidad para correr  en cualquier  sistema operativo  compatible con

    Java

  - usa cualquier archivo TMX válido como referencia de traducción

  - flexibles segmentación por frases (usando un método similar al SRX)

  - Búsquedas en el proyecto y las memorias de traducción de referencia

  - Búsquedas de archivos en formatos compatibles en cualquier directorio 

  - Coincidencias parciales

  - Manejo  inteligente  de  proyectos,  incluyendo  complejas  jerarquías  de

    directorios

  - Compatibilidad con glosarios (para verificación de terminología) 

  - Compatibilidad al vuelo con correctores ortográficos de fuente abierta

  - Apoyo a los diccionarios StarDict

  - Apoyo para los servicios de traducción automática de Google Translate

  - Documentación clara, comprensible y una guía para principiantes

  - Interfaz de usuario en varios idiomas.



OmegaT es compatible con los siguientes formatos de archivo fuera de la caja:



- Formatos de archivo de texto plano



  - Texto ASCII (.txt, etc.)

  - texto codificado (*.UTF8)

  - Paquetes de recursos Java (*.properties)

  - Archivos PO (*.po)

  - archivos INI (clave=valor) (*.ini)

  - Archivos DTD (*.DTD)

  - Archivos DocuWiki (*.txt)

  - Archivos de títulos subRip (*.srt)

  - Magento CE Locale CSV (*.csv)



- Archivos de texto con formato etiquetado



  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp

  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)

  - (X)HTML (*.html, *.xhtml,*.xht)

  - Compilador de ayuda HTML (*.hhc, *.hhk)

  - DocBook (*.xml)

  - XLIFF monolingüe (*.xlf, *.xliff, *.sdlxliff)

  - QuarkXPress CopyFlowGold (*.tag, *.xtg)

  - Archivos ResX (*.resx)

  - Recursos Android (*.xml)

  - LaTex (*.tex, *.latex)

  - Archivos de ayuda (*.xml) y (*.hmxp)

  - Typo3 LocManager (*.xml)

  - Localización WiX (*.wxl)

  - Iceni Infix (*.xml)

  - Flash XML export (*.xml)

  - Wordfast TXML (*.txml)

  - Camtasia para Windows (*.camproj)



También puedes personalizar OmegaT para que acepte otros formatos de archivo.



OmegaT automáticamente analizará incluso  las jerarquías de directorios fuente

más complejas,  para acceder a todos  los archivos compatibles,  y producir un

directorio destino,  con exactamente la misma estructura,  incluidas copias de

los archivos no compatibles.



Para ver  una guía  de inicio  rápido, lanza OmegaT  y lee  la guía  de inicio

rápido mostrada.



El  manual del  usuario está  en el  paquete que  acabas de  descargar, puedes

acceder a él desde el menú [Ayuda] después de iniciar OmegaT.



==============================================================================

 3. Instalando OmegaT



3.1 General

A fin  de ejecutarlo, OmegaT  requiere que tengas  instalado en tu  sistema el

Java Runtime Environment (JRE) versión 1.5 o superior. Los paquetes OmegaT que

incluyen el  Java Runtime Environment  están disponibles ahora para  ahorrar a

los usuarios la molestia de seleccionarlo, obtenerlo e instalarlo. 



Si ya tienes Java, una forma de instalar la versión actual de OmegaT es usando 

Java Web Start.  Para ello descarga y ejecuta el siguiente archivo:



   http://omegat.sourceforge.net/webstart/OmegaT.jnlp



Esto instalará un entorno correcto para tu equipo y la propia aplicación en la

primer ejecución.  En posteriores llamadas  no es necesario estar  conectado a

Internet.



Durante la  instalación, en  función de tu  sistema operativo,  puedes recibir

varias advertencias de seguridad.  El certificado está autofirmado por "Didier 

Briel". Los permisos que le des a esta versión (los cuales se pueden mencionar

como "acceso sin restricciones al equipo") son idénticos a los permisos que le

das a la versión local, la  cual se instala con un procedimiento, descrito más

adelante: permitiendo  acceso total al disco duro  del ordenador. Subsecuentes

clics en el  OmegaT.jnlp buscarán cualquier actualización, si  estás en línea,

se instalarán, si las hay, y luego iniciará OmegaT. 



La  manera  alternativa   de  descargar  e  instalar  OmegaT   se  muestran  a

continuación. 



Usuarios  de Windows  y Linux:  si estás  seguro de  que tu  sistema  ya tiene

instalada una versión  adecuada del JRE, puedes instalar  la versión de OmegaT

sin JRE (esto se indica en el nombre de la versión, "Without_JRE").  Si tienes 

alguna  duda,  te  recomendamos  que  utilices  la  versión  suministrada  con

JRE. Esto es seguro,  incluso si el JRE ya está instalado  en tu sistema, esta

versión no interferirá con aquella.



Usuarios de Linux:

OmegaT  se ejecutará en  la implementación  de Java  de código  fuente abierto

empacada con  muchas distribuciones  de Linux (por  ejemplo, Ubuntu),  pero es

posible  experimentar errores,  problemas de  visualización  o características

desaparecidas. Por lo tanto, te recomendamos que descargues e instales el Java

Runtime Environment  (JRE) de Oracle o  el paquete de OmegaT  con JRE incluido

(el  archivo .tar.bz2) marcado  "Linux"). Si  instalas una  versión de  Java a

nivel del sistema, te debes asegurar de que está en tu  ruta de lanzamiento, o

la  tendrás que invocar  explícitamente al  arrancar OmegaT.  Si no  estás muy

familiarizado con  Linux, te recomendamos  que instales una versión  de OmegaT

con JRE incluido. Esto  es seguro, ya que este JRE "local"  no va a interferir

con ningún otro JRE instalado en tu sistema.



Usuarios de Mac:

El JRE  se ha  instalado en  Mac OS X  antes de  Mac OS X  10.7 (Lion).  A los

usuarios de Lion  se les pide el sistema cuando se inicia por  primera vez una

aplicación que requiere Java y  el sistema eventualmente la descarga e instala

automáticamente.



Usuarios de  Linux en sistemas PowerPC:

Tendrán que descargar  JRE de IBM, ya  que Sun no ofrece un  JRE para sistemas

PPC. En este caso descarga:



    http://www.ibm.com/developerworks/java/jdk/linux/download.html 





3.2 Instalación

* Usuarios de Windows:

Basta con  lanzar el  programa de  instalación. Si lo  deseas, el  programa de

instalación puede crear accesos directos para lanzar OmegaT.



* Usuarios de Linux:

Coloca el  archivo en cualquier  directorio apropiado y  descomprímelo; OmegaT

entonces  estará  listo para  ser  lanzado.  Sin  embargo, puedes obtener  una

instalación  más  ordenada  y  más  fácil  de usar  mediante  el  programa  de

instalación (linux-install.sh). Para utilizar  este programa, abre una ventana

de terminal  ("console"), ingresa al  directorio que contiene OmegaT.jar  y el

programa "linux-install.sh" y ejecútalo con "./linux-install.sh".



* Usuarios de Mac:

Copia el archivo  OmegaT.zip a un lugar adecuado  y descomprímelo para obtener

un  directorio que  contiene  el archivo del indice de la documentación HTML y

OmegaT.app, el archivo de la aplicación.



* Otros (por ejemplo, Solaris, FreeBSD):

Para   instalar  OmegaT,  basta   con  crear   un  directorio   adecuado  para

OmegaT. Copia el archivo OmegaT zip o tar.bz2 a ese directorio y descomprímelo

allí.



3.3 Lanzando OmegaT

Lanza OmegaT de la siguiente manera:



* Usuarios de Windows:

Si durante  la instalación,  creaste un acceso  directo en el  escritorio, haz

doble  clic  sobre  él.  Alternativamente,   haz  doble  clic  en  el  archivo

OmegaT.exe.  Si  puedes  ver el  archivo  OmegaT,  pero  no OmegaT.exe  en  tu

administrador  de archivos  (Windows Explorer),  cambia la  configuración para

mostrar esa extensión de archivo.



* Usuarios de Linux:

Si utilizas el  programa de  instalación suministrado,  deberías  poder lanzar

OmegaT con: Alt+F2 y, a continuación: OmegaT



* Usuarios de Mac:

Haz doble clic en el archivo OmegaT.app.



* Desde el administrador de archivos (todos los sistemas):

Haz doble clic en el archivo  OmegaT.jar.  Esto sólo funcionará  si el tipo de

archivo .jar está asociado con Java en tu sistema.



* Desde la línea de ordenes (todos los sistemas):

La orden para lanzar OmegaT es la siguiente:



        cd <directorio donde está el archivo OmegaT.jar>



        <nombre y ruta del archivo Java ejecutable> -jar OmegaT.jar



      (El archivo ejecutable de Java es el archivo java en Linux y java.exe en

      Windows.   Si Java  está instalado  a nivel  de sistema,  no  tienes que

      introducir la ruta completa).



Personalizando tu experiencia para lanzar OmegaT:



* Usuarios de Windows:

El programa de instalación puede crear  accesos directos para ti en el menú de

inicio,  en el  escritorio  y en  el  área de  inicio  rápido. También  puedes 

arrastrar manualmente el archivo OmegaT.exe al menú Inicio, al escritorio o al

área de inicio rápido para enlazarlo desde allí.



* Linux users:

Para  una manera  más  fácil de  lanzar  OmegaT, puedes  utilizar el  programa

Kaptain suministrado  (omegat.kaptn). Para  utilizar este guión  primero debes

instalar Kaptain.  A continuación, puede  ejecutar el programa  de lanzamiento

con Kaptain



    Alt+F2



    omegat.kaptn



Para más información sobre Kaptain y  la adición de elementos al menú para los

iconos de lanzamiento en Linux, consulta el "Cómo a la OmegaT en Linux".



Usuarios de Mac:

Arrastra OmegaT.app  a tu muelle o a  la barra de herramientas  de una ventana

del  Buscador para  poder lanzarlo  desde cualquier  lugar. También  lo puedes

llamar en el Campo de búsqueda de Spotlight.



==============================================================================

 4. Involúcrate en el proyecto OmegaT



Para  participar  en  el desarrollo  de  OmegaT,  ponte  en contacto  con  los

desarrolladores en:

    http://lists.sourceforge.net/lists/listinfo/omegat-development



Para traducir la  interfaz de usuario de OmegaT, el manual  de usuario u otros

documentos relacionados consulta:

      

      http://www.omegat.org/en/translation-info.html



Y suscríbete a la lista de traductores:

      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n



Para otro tipo de aportación, suscríbete primero al grupo de usuarios en:

      http://tech.groups.yahoo.com/group/omegat/



Y date una idea de lo que está pasando en el mundo de OmegaT...



  OmegaT es el trabajo original de Keith Godfrey.

  Marc Prior es el coordinador del proyecto OmegaT.



Colaboradores anteriores incluyen a:

(en orden alfabético)



Las siguientes personas han aportado código

  Zoltan Bartko

  Volker Berlin

  Didier Briel (Director de desarrollo)

  Kim Bruning

  Alex Buloichik (Líder de desarrollo)

  Sandra Jean Chua

  Thomas Cordonnier

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



Otras aportaciones por

  Sabine Cretella

  Dmitri Gabinski

  Jean-Christophe Helary (Director de internacionalización)

  Vito Smolej (Director de documentación)

  Samuel Murray

  Marc Prior 

  y muchas, muchas muy útiles personas más



(Si crees  que has contribuido  significativamente al proyecto OmegaT  pero no

ves tu nombre en las listas, no dudes en contactarnos).



OmegaT utiliza las siguientes bibliotecas:



  HTMLParser por Somik Raha, Derrick Oswald y otros (Licencia LGPL)

  http://sourceforge.net/projects/htmlparser



  MRJ Adapter 1.0.8 por Steve Roy (Licencia LGPL)

  http://homepage.mac.com/sroy/mrjadapter/



  VLDocking Framework 2.1.4 por VLSolutions (Licencia CeCILL)

  http://www.vlsolutions.com/en/products/docking/



  Hunspell por László Németh y otros (Licencia LGPL)



  JNA por Todd Fast, Timothy Wall y otros (Licencia LGPL)



  Swing-Layout 1.0.2 (Licencia LGPL)



  Jmyspell 2.1.4 (Licencia LGPL)



  JAXB 2.1.7 (GPLv2 + classpath exception)



==============================================================================

 5.  ¿OmegaT te está complicando la vida? ¿Necesitas ayuda?



Antes  de  reportar  un  error, asegúrate  de  que has  revisado  a  fondo  la

documentación. Lo que  ves en su lugar puede ser  una característica de OmegaT

que acabas  de descubrir. Si compruebas  el registro de OmegaT  y ves palabras

como "Error", "Advertencia",  "Excepción", o "murió inesperadamente", entonces

probablemente hayas descubierto  un problema real (el log.txt  se encuentra en

el  directorio  de  preferencias  de  usuario,  consulta  el  manual  para  su

localización).



El siguiente  paso es  confirmar lo que  encontraste con otros  usuarios, para

asegurarte de que esto no se  ha reportado. También puedes verificar la página

de informe de errores en SourceForge.  Sólo cuando estés seguro de que eres el

primero  que  ha encontrado  una  secuencia  de  eventos reproducible  que  ha

activado algo que no debe suceder en ese caso crea un informe de error.



Cada buen informe de fallo necesita tres cosas exactamente.

  - Pasos para reproducirlo

  - Lo que esperabas ver, y

  - Lo que viste en su lugar



Puedes  agregar copias  de  archivos,  las partes  del  registro, capturas  de

pantalla, cualquier  cosa que creas  que va a  ayudar a los  desarrolladores a

encontrar y corregir tu error.



Para ver los archivos del grupo de usuarios, ve a:

     http://groups.yahoo.com/group/OmegaT/



Para ver la página de informes de  error y presentar un nuevo informe de error

si es necesario, ve a:

     http://sourceforge.net/tracker/?group_id=68187&atid=520350



Para  mantenerte al tanto  de lo  que está  sucediendo a  tu informe  de error

posiblemente quieras registrarte como usuario de Source Forge.



==============================================================================

6.   Detalles sobre esta versión



Por  favor ve  el  archivo "CHANGES.txt"  para  obtener información  detallada

acerca de los cambios en esta y en todas las versiones anteriores.





==============================================================================