Esta traducción es obra de Nacho Pacheco (nachopacheco arroba gmail.com),
copyright© 2010-2011.

==============================================================================
Archivo Léeme de OmegaT 2.3

  1.  Información acerca de OmegaT
  2.  ¿Qué es OmegaT?
  3.  Instalando OmegaT
  4.  Aportando a OmegaT
  5.  ¿OmegaT te está complicando la vida? ¿Necesita ayuda?
  6.  Detalles sobre esta versión

==============================================================================
  1.  Información acerca de OmegaT


Puedes encontrar la información más actualizada acerca de OmegaT en
      http://www.omegat.org/

Asistencia a usuarios en el grupo de usuarios de Yahoo (en varios idiomas),
donde puedes buscar en los archivos sin suscripción:
     http://groups.yahoo.com/group/OmegaT/

Solicitudes de mejoras (en Inglés), en el sitio de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Informe de fallos (en Inglés), en el sitio web de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  ¿Qué es OmegaT?

OmegaT es una herramienta de traducción asistida por ordenador (TAO). Es
libre, es decir no tienes que pagar nada para usarla, incluso para uso
profesional, y eres libre de modificar y/o redistribuir siempre y cuando
respetes la licencia de usuario.

Las principales características de OmegaT son:
  - la capacidad de funcionar en cualquier sistema operativo compatible con
    Java
  - usa cualquier archivo TMX válido como referencia para la traducción
  - flexibles declaraciones de segmentación (utilizando un método similar a
    SRX)
  - búsquedas en el proyecto y referencia en las memorias de traducción
  - búsquedas en archivos de formatos compatibles en cualquier directorio
  - coincidencias aproximadas
  - manejo inteligente de proyectos, incluyendo complejas jerarquías de
    directorios
  - apoyo para glosarios (comprobación de terminología)
  - apoyo a correctores ortográficos OpenSource sobre la marcha
  - apoyo a diccionarios StarDict
  - apoyo a servicios de traducción automática de Google Translate
  - documentación clara y completa y guías iniciales
  - localización a una serie de idiomas.

OmegaT es compatible con los siguientes formatos de archivo fuera de la caja:
  - texto plano
  - HTML y XHTML
  - Compilador de ayuda HTML
  - OpenDocument/OpenOffice.org
  - Paquetes de recursos Java (.properties)
  - Archivos INI (archivos con pares de clave/valor en cualquier codificación)
  - Archivos PO
  - Archivos de documentación en formato DocBook
  - Archivos de Microsoft OpenXML
  - Archivos monolingües Okapi XLIFF
  - QuarkXPress CopyFlowGold
  - Archivos de subtítulos (SRT)
  - ResX
  - Recursos Android
  - LaTeX
  - Typo3 LocManager
  - Ayuda y Manual
  - Recursos Windows RC
  - Mozilla DTD
  - DokuWiki
  - Wix
  - Infix
  - Flash XML export

También puedes personalizar OmegaT para que acepte otros formatos de archivo.

OmegaT automáticamente analizará incluso las jerarquías de directorios fuente
más complejas, para acceder a todos los archivos compatibles, y producir un
directorio destino, con exactamente la misma estructura, incluidas copias de
los archivos no compatibles.

Para ver una guía de inicio rápido, lanza OmegaT y lee la guía de inicio
rápido mostrada.

El manual del usuario está en el paquete que acabas de descargar, puedes
acceder a él desde el menú [Ayuda] después de iniciar OmegaT.

==============================================================================
 3. Instalando OmegaT

3.1 General
A fin de ejecutarlo, OmegaT requiere que tengas instalado en tu sistema el
Java Runtime Environment (JRE) versión 1.5 o superior. OmegaT ahora se
suministra de serie con el Java Runtime Environment para ahorrar a los
usuarios la molestia de seleccionar, obtener e instalarlo. 

Si ya tienes Java, la forma más sencilla de instalar la versión actual de
OmegaT es usando el Java Web Start. 
Para ello descarga y ejecuta el siguiente archivo:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Esto instalará un entorno correcto para tu equipo y la propia aplicación en
la primer ejecución. En posteriores llamadas no es necesario estar conectado
a Internet.

Durante la instalación, en función de tu sistema operativo, puedes recibir
varias advertencias de seguridad. El certificado está autofirmado por "Didier
Briel". 
Los permisos que le des a esta versión (los cuales se pueden mencionar como
"acceso sin restricciones al equipo") son idénticos a los permisos que le das
a la versión local, la cual se instaló con un procedimiento, descrito más
adelante: permitiendo acceso total al disco duro del ordenador. Subsecuentes
clics en el OmegaT.jnlp buscarán cualquier actualización, si estás en línea,
se instalarán, si las hay, y luego iniciará OmegaT. 

La manera alternativa de descargar e instalar OmegaT se muestran a
continuación. 

Usuarios de Windows y Linux: si estás seguro de que tu sistema ya tiene
instalada una versión adecuada del JRE, puedes instalar la versión de OmegaT
sin JRE (esto se indica en el nombre de la versión, "Without_JRE"). 
Si tienes alguna duda, te recomendamos que utilices la versión "estándar",
es decir, con JRE. Esto es seguro, ya que incluso si el JRE está instalado en
tu sistema, esta versión no va a interferir con ella.

Los usuarios de Linux: Ten en cuenta que  OmegaT no funciona con
implementaciones Java libre/código abierto que se incluyen con muchas
distribuciones de Linux (por ejemplo, Ubuntu), ya que son obsoletas o están
incompletas. Descarga e instale el Java Sun Runtime Environment (JRE) a través
del enlace anterior, o descargar e instala OmegaT empacado con JRE (el paquete
.tar.gz marcado "Linux").

Usuarios de Mac: el JRE ya está instalado en Mac OS X.

Usuarios de Linux en sistemas PowerPC: tendrán que descargar JRE de IBM, ya
que Sun no ofrece un JRE para sistemas PPC. En este caso descarga:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Instalación
* Usuarios de Windows: Basta con abrir el programa de instalación. Si lo
  deseas, el programa de instalación puede crear accesos directos para
  lanzar OmegaT.
* Otros: Para instalar OmegaT, simplemente crea un directorio adecuado para
  OmegaT (por ejemplo, /usr/local/lib en Linux). Copia el archivo zip o
  tar.gz de OmegaT a esta carpeta y extáelo allí.

3.3 Lanzando OmegaT
OmegaT se puede lanzar en una serie de maneras.

* Usuarios de Windows: haciendo doble clic sobre el archivo OmegaT.exe. Si
puedes ver el archivo OmegaT, pero no OmegaT.exe en el Administrador de
archivos (Windows Explorer), cambia la configuración para mostrar la extensión
de los archivos.

* Haciendo doble clic en el archivo OmegaT.jar. Esto sólo funcionará si el
  tipo de archivo .jar está asociado con Java en tu sistema.

* Desde la línea de ordenes. La orden para lanzar OmegaT es:

        cd <directorio donde está el archivo OmegaT.jar>

        <nombre y ruta del archivo Java ejecutable> -jar OmegaT.jar

      (El archivo ejecutable de Java es el archivo java en Linux y java.exe en Windows.
      Si Java está instalado a nivel de sistema, no tienes que introducir la
      ruta completa).

* Usuarios de Windows: El programa de instalación puede crear accesos directos
  en el menú de Inicio, en el escritorio y en el área de inicio rápido. También
  puedes arrastrar manualmente el archivo OmegaT.exe al menú Inicio, al
  escritorio o al área de inicio rápido para enlazarlo desde allí.

* Usuarios de Linux KDE: puedes agregar OmegaT a los menús de la siguiente
  manera:

  Centro de control - Escritorio - Paneles - Menús - Edición de menú K
  - Archivo - Nuevo elemento/Nuevo submenú.

  Entonces, después de seleccionar un menú adecuado, añade un submenú/elemento
  con Archivo - Nuevo submenú y Archivo - Nuevo elemento. Escribe OmegaT como
  nombre del nuevo elemento.

  En el campo "orden", utiliza el botón de navegación para encontrar el guión
  de inicio de OmegaT, y selecciónalo. 

  Haz clic en el botón con el icono (a la derecha de los campos
  Nombre/Descripción/Comentario) - Otros iconos - Examinar y navega al
  subdirectorio /images en el directorio de la aplicación OmegaT. Selecciona
  el icono OmegaT.png.

  Por último, guarda los cambios con Archivo - Guardar.

* Usuarios de Linux GNOME: puedes agregar OmegaT a tu panel (la barra en la
  parte superior de la pantalla) de la siguiente manera:

  Haz clic con el botón derecho en el panel - Añadir nuevo lanzador.   Escribe
  "OmegaT" en el campo "Nombre", en el campo "orden", utiliza el botón de
  navegación para encontrar guión de lanzamiento de OmegaT. Selecciónalo y
  confirma con Aceptar.

==============================================================================
 4. Involúcrate en el proyecto OmegaT

Para participar en el desarrollo de OmegaT, ponte en contacto con los
desarrolladores:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Para traducir la interfaz de usuario de OmegaT, el manual de usuario u otros
documentos relacionados, consulta:
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

El código ha sido aportado por
  Zoltan Bartko
  Volker Berlin
  Didier Briel (administrador de desarrollo)
  Kim Bruning
  Alex Buloichik (líder de desarrollo)
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
  Rashid Umarov  
  Antonio Vilei
  Martin Wunderlich

Otras aportaciones por
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (administrador de localización)
  Vito Smolej (administrador de documentación)
  Samuel Murray
  Marc Prior 
  y muchas, muchas más personas de gran ayuda

(Si crees que haz contribuido significativamente al proyecto OmegaT pero no
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
 5.  ¿OmegaT te está complicando la vida? ¿Necesita ayuda?

Antes de reportar un error, asegúrate de que haz comprobado a fondo la
documentación. Lo que ves en su lugar puede ser una característica de OmegaT
que acabas de descubrir. Si compruebas el registro de OmegaT y ves palabras
como "Error", "Advertencia", "Excepción", o "murió inesperadamente", entonces
probablemente hayas descubierto un problema real (el log.txt se encuentra en
el directorio de preferencias de usuario, consulta el manual para su
localización).

El siguiente paso es confirmar lo que encontraste con otros usuarios, para
asegurarte de que esto no se ha reportado. También puedes verificar la página
de informe de errores en SourceForge. Sólo cuando estés seguro de que eres el
primero que ha encontrado una secuencia de eventos reproducible que ha
activado algo que no debe suceder en ese caso crea un informe de error.

Cada buen informe de fallo necesita tres cosas exactamente.
  - Pasos para reproducirlo,
  - Lo que esperabas ver, y
  - Lo que viste en su lugar.

Puedes agregar copias de archivos, las partes del registro, capturas de
pantalla, cualquier cosa que creas que va a ayudar a los desarrolladores a
encontrar y corregir tu error.

Para ver los archivos del grupo de usuarios, ve a:
     http://groups.yahoo.com/group/OmegaT/

Para ver la página de informe de errores y presentar un nuevo informe de
errores si es necesario, ve a:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Para mantenerte al tanto de lo que está sucediendo a tu informe de error
posiblemente quieras registrarte como usuario de Source Forge.

==============================================================================
6.   Detalles sobre esta versión

Por favor ve el archivo "CHANGES.txt" para obtener información detallada
acerca de los cambios en esta y en todas las versiones anteriores.


==============================================================================