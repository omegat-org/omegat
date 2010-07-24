Esta traducción es obra de Nacho Pacheco, copyright© 2010.

==============================================================================
  Archivo «Léame» de OmegaT 2.1

  1.  Información acerca de OmegaT
  2.  ¿Qué es OmegaT?
  3.  Instalando OmegaT
  4.  Aportaciones a OmegaT
  5.  ¿OmegaT le está complicando la existencia? ¿Necesita ayuda?
  6.  Detalles sobre esta versión

==============================================================================
  1.  Información acerca de OmegaT


La información más reciente sobre OmegaT se encuentra en:
      http://www.omegat.org/es/omegat.html

Asistencia al usuario, en el grupo (multilingüe) de usuarios de Yahoo, donde
puede buscar en los archivos sin suscripción:
     http://groups.yahoo.com/group/OmegaT/

Solicitud de mejoras (en Inglés), en el sitio web de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Informes de fallos (en Inglés), en el sitio web de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

==============================================================================
  2.  ¿Qué es OmegaT?

OmegaT es una herramienta de traducción asistida por ordenador (CAT por
Computer-Assisted Translation). Es una herramienta libre, lo cual significa
que no es necesario pagar para utilizarla, ni siquiera para su uso
profesional, y también significa que usted es libre de modificarla y
redistribuirla, siempre que respete la licencia de usuario.

La principales características de OmegaT son:
  - funciona en cualquier sistema operativo compatible con Java;
  - usa cualquier archivo TMX válido como material de referencia para
    traducir;
  - segmentación flexible de oraciones (utilizando un método similar al
    estándar SRX);
  - busca en las memorias de traducción de referencia y del proyecto;
  - búsqueda de archivos en formatos compatibles en cualquier directorio 
  - coincidencias parciales
  - manejo inteligente de proyectos, incluso con jerarquías de directorios
    complejas
  - admite glosarios (comprobación de terminología) 
  - es compatible con correctores ortográficos OpenSource al vuelo
  - soporte para diccionarios StarDict
  - es compatible con los servicios de traducción automática de Apertium,
    Belazar y Google Translate 
  - documentación clara y comprensible además de una guía de inicio
    rápido
  - está traducido a diferentes idiomas.

OmegaT es compatible con los siguientes formatos de archivo:
  - Texto simple
  - HTML y XHTML
  - Compilador de Ayuda HTML
  - OpenDocument y OpenOffice.org
  - Paquetes de recursos de Java (.properties)
  - Archivos INI (archivos con pares clave=valor en cualquier codificación)
  - Archivos PO
  - Formato de archivo de documentación DocBook
  - archivos Microsoft OpenXML
  - archivos monolingües Okapi XLIFF
  - QuarkXPress CopyFlowGold
  - Archivos de subtítulos (SRT)
  - ResX
  - Recursos Android
  - LaTeX
  - Typo3 LocManager
  - Ayuda y Manual
  - recursos Windows RC
  - Mozilla DTD
  - DokuWiki

También puede personalizar OmegaT para que acepte otros formatos de archivo.

OmegaT automáticamente analiza incluso las jerarquías de directorios fuente
más complejas, para acceder a todos los archivos compatibles y generar un
directorio destino con una estructura exactamente igual, incluyendo copias
de cualquier archivo no compatible.


Si necesita una breve orientación para empezar a traducir de inmediato,
lance OmegaT y vea la Guía de inicio rápido.

El manual de usuario está en el paquete que acaba de descargar, también puede
encontrarlo dirigiéndose al menú [Ayuda] después de abrir OmegaT.

==============================================================================
 3. Instalando OmegaT

 3.1 General

Para ejecutar, OmegaT requiere tener instalado en su sistema el Java Runtime
Environment (JRE) versión 1.5 o superior. OmegaT ahora se suministra de
serie con el entorno de ejecución Java para evitar a los usuarios la molestia
de ubicarlo, obtenerlo e instalarlo por sí mismos. 

Si ya tiene Java, la forma más sencilla de instalar la versión actual de
OmegaT es usar Java Web Start.
 
Para ello descargue y ejecute el siguiente archivo:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Este instalará el entorno correcto para su equipo y la aplicación en sí misma
en la primer ejecución. En posteriores llamadas no es necesario estar
conectado a Internet.

Durante la instalación, dependiendo de su sistema operativo, puede recibir
varias advertencias de seguridad. El certificado está autofirmado por "Didier
Briel". 

Los permisos que le de a esta versión (mismos que se pueden mencionar como
acceso sin restricciones "al ordenador") son idénticos a los permisos que le
da a la versión local, instalado por un procedimiento, se describen más
adelante: permiten un acceso a la unidad de disco duro del ordenador. Los clics
subsecuentes en OmegaT.jnlp
comprobarán si hay alguna actualización, si usted está en línea, la instalará,
en caso de que hubiese, y luego lanzará OmegaT. 

Las alternativas y medios para descargar e instalar OmegaT se muestran a
continuación. 

Usuarios de Windows y Linux: si usted está seguro de que su sistema ya tiene
instalada una versión adecuada de JRE, puede instalar la versión de OmegaT
sin JRE (esto se indica en el nombre de la versión, "Without_JRE"). 
Si tiene alguna duda, le recomendamos que utilice la versión "estándar", es
decir, con JRE. Esto es seguro, ya que incluso si el JRE está instalado en su
sistema, esta versión no va a interferir con ella.

Usuarios de Linux: tenga en cuenta que OmegaT no funciona con
implementaciones de Java gratuitas/open-source que se empaquetan con muchas
distribuciones de Linux (por ejemplo, Ubuntu), debido a que no están
actualizadas o están incompletas. Descargue e instale Sun Java Runtime
Environment (JRE) a través del enlace de arriba, o descargue e instalar el
paquete OmegaT con JRE (el paquete tar.gz. marcado "Linux").

Usuarios de Mac: el JRE ya está instalado en Mac OS X.

Usuarios de Linux en sistemas PowerPC: tendrán que descargar el JRE de IBM,
debido a que Sun no proporciona un JRE para sistemas PPC. En este caso
descargue:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


 3.2 Instalación

    * Usuarios de Windows: Simplemente lance el programa de instalación. Si lo
      desea, el programa de instalación puede crear accesos directos para
      lanzar OmegaT.

    * Otros: Para instalar OmegaT, basta con crear un directorio adecuado
      para OmegaT (por ejemplo, /usr/local/lib en Linux). Copie el archivo zip
      o tar.gz de OmegaT en ese directorio y descomprimirlo allí.

 3.3 Lanzando OmegaT

Puede lanzar OmegaT de varias maneras.


    * Usuarios de Windows: haciendo doble clic en el archivo OmegaT.exe. Si
      usted puede ver el archivo OmegaT pero no OmegaT.exe en el
      Administrador de archivos (Windows Explorer), cambie la
      configuración para que se muestren las extensiones de archivo.

    * Haciendo doble clic sobre el archivo OmegaT.jar. Esto sólo funcionará
      si el tipo de archivo .jar está asociado con Java en su sistema.

    * Desde la línea de comandos. El comando para lanzar OmegaT es:

        cd <directorio donde está el archivo OmegaT.jar>

        <nombre y ruta del archivo Java ejecutable> -jar OmegaT.jar

      (El archivo ejecutable de Java es el archivo java en Linux y java.exe en
      Windows.
      Si Java está instalado a nivel de sistema, no tiene que introducir la
      ruta completa).

    * Usuarios de Windows: El programa de instalación puede crear accesos
      directos para usted en el menú Inicio, en el escritorio y en el área de
      inicio rápido. También puede arrastrar manualmente el archivo OmegaT.exe
      al menú Inicio, al escritorio o al área de inicio rápido para enlazarlo
      desde allí.

    * Usuarios KDE de Linux: usted puede agregar OmegaT a sus menús de la
      siguiente manera:


      Centro de Control - Escritorio - Paneles - Menús - Editar Menú K
      - Archivo - Nuevo Elemento/Nuevo submenú

 
      Entonces, después de seleccionar un menú adecuado, agregue un submenú y
      tema con Archivo - Nuevo submenú y Archivo - Nuevo elemento. 
      Introduzca OmegaT como nombre del nuevo elemento.

      En el campo "Comando", utilice el botón de navegación para encontrar el
      script de lanzamiento de OmegaT y selecciónelo. 

      Haga clic en el botón con el icono (a la derecha de Nombre/Descripción
      /Campo de comentarios) - Otros iconos - Examinar y navegue hasta el
      subdirectorio /images en el directorio de la aplicación OmegaT. Seleccione
      el icono OmegaT.png.

      Por último, guarde los cambios con Archivo - Guardar.

    * Usuarios Gnome de Linux: Usted puede añadir OmegaT a su panel (la barra
      en la parte superior de la pantalla) de la siguiente manera:

      Haga clic con el botón derecho en el panel - Añadir nuevo lanzador. En el
      campo "Nombre" escriba "OmegaT"; en el campo "Comando", utilice el botón
      de navegación para ubicar el script de lanzamiento de OmegaT. Selecciónelo
      y confirme con Aceptar.

==============================================================================
 4. Involúcrese en el proyecto OmegaT

Para participar en el desarrollo de OmegaT, póngase en contacto con los
desarrolladores en:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Para traducir la interfaz de usuario de OmegaT u otros documentos
relacionados, lea:
      
      http://www.omegat.org/es/translation-info.html

Y suscríbase a la lista de traductores:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Para otro tipo de aportaciones, primero suscríbase en el grupo de usuarios en:
      http://tech.groups.yahoo.com/group/omegat/

Y dese una idea de lo que está pasando en el mundo de OmegaT...

  OmegaT es el trabajo original de Keith Godfrey.
  Marc Prior es el coordinador del proyecto OmegaT.

contribuyentes anteriores incluyen a:
(en orden alfabético)

Partes del código han sido aportadas por
  Zoltan Bartko
  Volker Berlin
  Didier Briel (encargado del lanzamiento)
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

Otras aportaciones por
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (administrador de la localización)
  Vito Smolej (administrador de la documentación)
  Samuel Murray
  Marc Prior 
  y muchas, muchas personas más que han aportado gran ayuda

(Si usted cree que ha contribuido de manera significativa al
proyecto OmegaT pero no ve su nombre en las listas, no dude en contactarnos).

OmegaT utiliza las siguientes bibliotecas:

  HTMLParser por Somik Raha, Derrick Oswald y otros (Licencia LGPL)
  http://sourceforge.net/projects/htmlparser

  MRJ Adaptador 1.0.8 por Steve Roy (Licencia LGPL)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 por VLSolutions (Licencia CeCILL)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell por László Németh y otros (Licencia LGPL)

  JNA por Todd Fast, Timothy Wall y otros (Licencia LGPL)

  Swing-Layout 1.0.2 (Licencia LGPL)

  Jmyspell 2.1.4 (Licencia LGPL)

  JAXB 2.1.7 (GPLv2 + classpath exception)

==============================================================================
 5.  ¿OmegaT le está complicando la existencia? ¿Necesita ayuda?

Antes de reportar un fallo, asegúrese de que ha revisado minuciosamente la
documentación. Lo que ve en cambio puede ser una característica de OmegaT que
acaba de descubrir. Si comprueba el registro de OmegaT y ve palabras como
"Error", "Warning", "Exception", o "died unexpectedly", entonces
probablemente usted ha descubierto un problema real (el log.txt se encuentra
en el directorio de las preferencias del usuario, consulte el manual para su
localización).

El siguiente paso es confirmar lo que encontró con otros usuarios, para
asegurarse de que esto no ha sido reportado. También puede verificar la página
de informe de fallos en SourceForge. Únicamente cuando está seguro de que es
el primero que ha encontrado alguna secuencia de eventos reproducible que
provocó algo que se supone no debería ocurrir, usted debe presentar un
informe de fallo.

Cada buen informe de fallo necesita tres cosas exactamente.
  - Los pasos para reproducirlo
  - Qué esperaba ver, y
  - Lo que vio en su lugar

Puede agregar copias de los archivos, porciones del registro, capturas de
pantalla, todo lo que crea que va a ayudar a los desarrolladores a buscar y
corregir el fallo.

Para navegar por los archivos del grupo de usuarios, vaya a:
     http://groups.yahoo.com/group/OmegaT/

Para navegar por la página de informe de fallos y presentar un nuevo informe
de fallo si es necesario, vaya a:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Para mantenerse al tanto de lo que está sucediendo a su informe de fallo
posiblemente quiera registrarse como usuario de Source Forge.

==============================================================================
6.   Detalles sobre esta versión

Por favor vea el archivo 'changes.txt' para información detallada sobre
cambios en esta y todas las versiones anteriores.


==============================================================================
