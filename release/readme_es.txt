This translation is the work of Richard Holt, copyright© 2009.

==============================================================================
  Archivo «Léame» de OmegaT 2.0.2

  1.  Información acerca de OmegaT
  2.  ¿Qué es OmegaT?
  3.  Instalando OmegaT
  4.  Contribuciones a OmegaT
  5.  ¿Está OmegaT complicándole la vida? ¿Necesita ayuda?
  6.  Detalles sobre esta versión

==============================================================================
  1.  Información acerca de OmegaT


La información más actualizada sobre OmegaT se encuentra en:
      http://www.omegat.org/

Asistencia al usuario en el grupo de usuarios (multilenguas) albergado en Yahoo, donde los archivos estan acesible sin suscripción:
     http://groups.yahoo.com/group/OmegaT/

Solicitudes de mejora (en Inglés) en la página de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Informes de errores (en Inglés) en la página de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  ¿Qué es OmegaT?

OmegaT es una herramienta de traducción asistida por ordenador. Es una herramienta libre, lo que significa que no es necesario pagar para utilizarla, ni siquiera para su uso profesional, pero también significa que usted es libre de modificarla y redistribuirla, siempre que respete la licencia de usuario.

La principales prestaciones de OmegaT son:
  - funciona en cualquier sistema operativo compatible con Java;
  - usa cualquier archivo TMX válido como material de referencia para traducir;
  - segmenta oraciones de forma flexible (según un método similar al estándar SRX);
  - busca en las memorias de traducción del proyecto y en las de referencia;
  - busca en cualquier carpeta en archivos de formatos soportados 
  - indica coincidencias parciales;
  - maneja proyectos inteligentemente, incluso con jerarquías complejas de directorios;
  - admite glosarios (consultas terminológicas);
  - su documentación y tutorial son fáciles de entender;
  - está traducido a diferentes idiomas.

OmegaT admite los siguientes formatos de archivos:
  - Texto simple
  - HTML y XHTML
  - Compilador de ayuda de HTML
  - OpenDocument/OpenOffice.org
  - Paquetes de recursos de Java (.properties)
  - Archivos INI (pares de claves y valores en cualquier codificación)
  - Archivos PO
  - Formato de archivo de documentación DocBook
  - Archivos OpenXML de Microsoft
  Archivos XLIFF de Okapi monolingua

OmegaT también puede ser personalizada para soportar otras formatos de archivos.

OmegaT analiza automáticalmente las jerarquías de directorios más complejas para acceder a todos los archivos compatibles y generar un directorio de destino con una estructura exactamente igual, incluidas las copias de cualquier archivo no compatible.

Si desea consultar un tutorial breve, abra Omega T y lea la Guía rápida.

En el paquete que acaba de descargar también puede encontrar el manual de usuario dirigiéndose al menú [Ayuda] después de abrir OmegaT.

==============================================================================
 3. Instalando OmegaT

OmegaT exige que instale el entorno de ejecución Java 1.4 o superior
en su ordenador. OmegaT está suministrada como estándar con el Ambiente Java de Ejecución (JRE) para evitar que los usuarios tendra que buscar, seleccionar, bajar e instalarlo. 

Usuarios de Windows y Linux: si está segura que el sistema ya tiene una versión apto del JRE instalado, puede instalar la versión sin el JRE, que esta indicado en el nombre de la versión, "Without_JRE"). 
Si tiene duda, recommendamos que seleccionar la versión estándar, con el JRE incluido. Esta es seguro, aunque el JRE ya es instalado en el sistema, este versión no lo interferir.

Usuarios de Linux: nota que el OmegaT no funciona con la implementación de la versión libre/opensource de Java que esta empaquetado con muchos distros de Linux, como por ejemplo el Ubuntu, porque muchos de estas están viejo o incompleto. Bajar e instalar el Java Runtime Environment (JRE) de Sun a través del enlace arriba, o bajar e instalar el paquete de OmegaT que incluye "with JRE" en su titulo del paquete tar.gz marcado "Linux").

Usuarios del Mac: el JRE ya esta instalado en el Mac OS X.

Linux para sistemas PowerPC: usuarios necesitan bajar el JRE de IBM en vista de que Sun no provee una versión del JRE para sistemas PPC. En este caso, bajarlo desde aquí:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 

3.2 Instalación
* Usuarios de Windows: Solo ejecuta el programa de instalación. Si desea, el programa de instalación puede crear atajos para ejecutar el OmegaT.
* Otras: Para instalar OmegaT, solamente crear un directorio para el OmegaT (por ejemplo, /usr/local/lib en Linux). Copiar el zip o el tar.gz del OmegaT a este directorio y desempacarlo allí.

3.3 Ejecutando OmegaT
OmegaT puede ser ejecutado en varios maneras.

* Usuarios de Windows: por hacer doble clic en el archivo OmegaT.exe. Si puede ver el archivo OmegaT pero no OmegaT.exe en el gestor de archivos (Windows Explorer), cambiar la configuración para mostrar extensiones de archivos.

* Por hacer doble clic en el archivo OmegaT.jar. Este solo funciona si el tipo de .jar esta asociado con Java en el sistema.

* Desde la línea de comandos. El comando para ejecutar OmegaT es:

cd <carpeta donde esta ubicado el archivo OmegaT.jar>

<nombre y ruta del ejecutable de Java> -jar OmegaT.jar

(El ejecutable de Java es el archivo "java" en Linux y java.exe en Windows.
Si el Java esta instalado a nivel de sistema, la ruta completa no tiene que ser entrado.)

* Usuarios Windows: El programa de instalación puede crear atajos para el menú de arranque, en el escritorio y en la área de ejecución rápido. También puede arastrar el archivo OmegaT.exe al menú de arranque, el escritorio o a la área de ejecución rápida para crear en enlace desde alli.

* Usuarios de Linux con KDE: puede agregue OmegaT a el menú como sigue:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Entonces, después de escoger el menú apropriado, agregue un submenú/item con el File - New 
Submenu and File - New Item. Entra OmegaT como el nombre del nuevo item.

En el campo de "Command", utilza el botón de navegación para buscar el script de ejecución, y seleccionarlo. 

Haga click en el icono (a la derecho de los campos de Name/Description/Comment) 
- Other Icons - Browse, y navegar al subcarpeta /images en la carpeta de la aplicación OmegaT. Seleccionar el icono OmegaT.png.

Al fin, guardar los cambios con File - Save.

* Usuarios de Linux con GNOME: puede agregar OmegaT al panel (la barra en el superior del pantalla) como sigue:

Haga clic derecho en el panel - Agregue Nueva Lanzador (Add New Launcher). Entra "OmegaT" en el campo "Nombe"; en el campo de "Comando", utiliza el botón de navegación para ubicar el script de ejecución de OmegaT. Seleccionarlo y confirmarlo con "OK".

==============================================================================
 4. Involucrandose con el proyecto de OmegaT

Para participar en el desarrollo de OmegaT, haga contacto con los desarrolladores en:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Para traducir el interfaz del usuario de OmegaT, el manual de usuario o documentos relacionados,
leer:
      
      http://www.omegat.org/en/translation-info.html

Y suscribirse en la lista de traductores:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Para otros tipos de contribuciones, primeramente, suscribirse al grupo de usuarios en:
      http://tech.groups.yahoo.com/group/omegat/

Para conocer el sentir de lo que esta sucediendo en el mundo de OmegaT....

  OmegaT es un trabajo original de Keith Godfrey.
  Marc Prior es el coordinador del proyecto OmegaT.

Contribuidores previos incluye:
(en orden alfabetica)

Código ha sido contribuido por:
  Zoltan Bartko
  Didier Briel (gerente de lanzamiento)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

Otras contribuciones por:
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary
  Vito Smolej (gerente de documentación)
  Samuel Murray
  Marc Prior (gerente de localización)
  y muchos, muchos más gente de mucho ayuda

(Si piensas que ha contribuido significantemente al proyecto OmegaT pero no veo su nombre en las listas, por favor contactarnos.)

OmegaT utiliza las siguientes bibliotecas:

  HTMLParser por Somik Raha, Derrick Oswald y otras (Licencia LGPL)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 por Steve Roy (Licencia LGPL)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 por VLSolutions (Licencia CeCILL)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell por László Németh y otros (Licencia LGPL)

  JNA por Todd Fast, Timothy Wall y otras (Licencia LGPL)

  Swing-Layout 1.0.2 (Licencia LGPL)

  Jmyspell 2.1.4 (Licencia LGPL)

  JAXB 2.1.7 (GPLv2 + classpath exception)

==============================================================================
 5.  ¿Está OmegaT complicándole la vida? ¿Necesita ayuda?

Antes que reportar un error, asegurar que ha revisado cuidadosamente la documentación. Lo que vea, puede ser una carácteristica de OmegaT ha acaba de descubrir. Si revisa el registro de OmegaT log y vea palabras como "Error", "Precaución", "Excepción", "murio inesperadamente" entonces es probable que ha descubierto una problema genuina (el log.txt esta ubicado en el carpeta de preferencias del usuario, ver el manual para el locación).

La próxima cosa de hacer es confirmar lo que ha encontrado con otros, para ver igual si no ha sido reportado antes. También puede verificar la página de reportes de errores en SourceForge. Solamente cuando está segura de que eres la primero a encontrar una secuencia reproducible de un evento que inicia algo que no debe suceder, entonces debe iniciar un reporte de error.

Cada buen reporte de error necesita contener estos tres cosas:
  - Pasos para reproducir,
  - Lo que esperaba ver, y
  - Lo que vio en su vez.

Puede agregar copias de archivos, porciones de registros, instantaneos de pantallas, cualquier cosa que pensa puede ayudar los desarrolladores en encontrar y corregir el error.

Para navegar los archivos del grupo de usuarios, vayase al:
     http://groups.yahoo.com/group/OmegaT/

Para navegar la página de reportes de errores y iniciar una nuevo reporte de error, si es necessario, vayase al:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Para seguir lo que sucede con el reporte suyo de error, puede registrar en Source Forge como usuario.

==============================================================================
6.   Detalles sobre esta versión

Si desea obtener información pormenorizada sobre los cambios efectuados a ésta y a todas las versiones anteriores del programa, consulte el archivo «changes.txt».


==============================================================================