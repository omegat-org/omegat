Aquesta traducció és obra d'en Jordi Serratosa, copyright© 2007.

==============================================================================
  OmegaT 1.6.2, fitxer README

  1.  Informació sobre l'OmegaT
  2.  Què és l'OmegaT?
  3.  Notes generals sobre el Java i l'OmegaT
  4.  Contribucions a l'OmegaT
  5.  Teniu problemes amb l'OmegaT? Necessiteu ajuda?
  6.  Detalls de la versió

==============================================================================
  1.  Informació sobre l'OmegaT


Podeu trobar la informació més recent sobre l'OmegaT (en anglès, eslovac,
holandès i portuguès) a:
      http://www.omegat.org/omegat/omegat.html

Si us cal assistència, consulteu el grup d'usuaris de Yahoo (multilingüe),
on podeu fer cerques en l'arxiu de missatges sense necessitat de
subscriure-us-hi:
     http://groups.yahoo.com/group/OmegaT/

Sol·licituds de millores (en anglès), al lloc web de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Informes d'errors (en anglès), al lloc web de SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Què és l'OmegaT?

L'OmegaT és una eina de Traducció Assistida per Ordinador (TAO). És
gratuït, que vol dir que no us cal pagar per a utilitzar-lo (fins i tot per a
l'ús professional) i és lliure, que vol dir que teniu tota la llibertat de
modificar-lo i redistribuir-lo sempre que en respecteu la llicència de
l'usuari.

Les principals característiques de l'OmegaT són:
  - Possibilitat d'executar-lo en qualsevol sistema operatiu que admeti el
    Java.
  - Ús de qualsevol fitxer TMX vàlid com a referència de traducció.
  - Segmentació flexible per frases (mitjançant un mètode similar a l'SRX).
  - Cerques a les memòries de traducció del projecte i a les de referència.
  - Cerques en qualsevol directori que contingui fitxers compatibles amb
    l'OmegaT.
  - Cerca de coincidències parcials.
  - Gestió intel·ligent de projectes, fins i tot quan contenen jerarquies de
    directoris complexes.
  - Ús de glossaris (per a la comprovació de terminologia).
  - Documentació i guia d'aprenentatge fàcils d'entendre.
  - Localització en diverses llengües.

L'OmegaT admet fitxers OpenDocument, fitxers del Microsoft Office (mitjançant
l'ús de l'OpenOffice.org com a filtre de conversió o bé mitjançant la
conversió a HTML), fitxers de l'OpenOffice.org o de l'StarOffice, fitxers
(X)HTML, fitxers de localització Java i fitxers de text net, entre d'altres.

L'OmegaT analitzarà automàticament la jerarquia de directoris dels fitxers
de partida (per defecte, "source"), per complexa que sigui, processarà tots
els fitxers admesos i generarà un directori per als fitxers d'arribada (per
defecte, "target") amb exactament la mateixa estructura (fins i tot hi
inclourà còpies de tots els fitxers no admesos).

Per a veure una guia d'aprenentatge, inicieu l'OmegaT i llegiu la Guia d'inici
ràpid que es mostrarà.

El manual d'usuari es troba dins del paquet que heu baixat; podeu accedir-hi
des del menú Ajuda un cop iniciat l'OmegaT.

==============================================================================
 3. Notes generals sobre el Java i l'OmegaT

L'OmegaT requereix que l'entorn d'execució de Java (JRE) versió 1.4 o
superior estigui instal·lat al sistema. El podeu obtenir a:
    http://java.com

És possible que els usuaris del Windows i del Linux hagin d'instal·lar el
Java si no el tenen instal·lat.
El projecte OmegaT també ofereix versions que inclouen el Java. Els usuaris
del MacOSX ja tenen instal·lat el Java al sistema.

En un sistema correctament instal·lat, l'OmegaT s'iniciarà si feu doble clic
al fitxer OmegaT.jar.

Després d'instal·lar el Java, és possible que hagueu de modificar la
variable de camí del sistema per tal d'incloure-hi el directori on es troba
l'aplicació "java".

Els usuaris del Linux han de tenir en compte que l'OmegaT no funcionarà amb
aquelles implementacions del Java lliures o de codi obert incloses en moltes
distribucions del Linux (per exemple, Ubuntu), ja que no són actuals o són
incompletes. Podeu baixar i instal·lar l'entorn d'execució de Java (JRE) de
Sun mitjançant l'enllaç anterior, o bé podeu baixar i instal·lar l'OmegaT
amb el JRE inclòs (el paquet .tar.gz que conté el text "Linux").

Els usuaris que executin el Linux en sistemes PowerPC, han de baixar el JRE
d'IBM, ja que Sun no ofereix un JRE per a sistemes PPC. Podeu baixar-lo des
de:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Contribucions a l'OmegaT

Per a contribuir al desenvolupament de l'OmegaT, poseu-vos en contacte amb els
desenvolupadors a:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Per a traduir la interfície d'usuari de l'OmegaT, el manual d'usuari o els
documents relacionats, llegiu:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

I subscriviu-vos a la llista dels traductors:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Per a contribuir d'altres maneres, primer subscriviu-vos al grup d'usuaris:
      http://tech.groups.yahoo.com/group/omegat/

I així us fareu una idea d'allò que passa al món de l'OmegaT...

  L'OmegaT és una obra original de Keith Godfrey.
  Marc Prior és el coordinador del projecte OmegaT.

Contribucions prèvies:
(en ordre alfabètic)

Han participat en el codi:
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk (desenvolupador principal)
  Henry Pijffers (cap de llançament de versions)
  Benjamin Siband
  Martin Wunderlich

Han participat en la localització:
  Roberto Argus (portuguès de Brasil)
  Alessandro Cattelan (italià)
  Sabine Cretella (alemany)
  Suzanne Bolduc (esperanto)
  Didier Briel (francès)
  Frederik De Vos (holandès)
  Cesar Escribano Esteban (espanyol)
  Mikel Forcada Zubizarreta (català)
  Dmitri Gabinski (bielorús, esperanto i rus)
  Takayuki Hayashi (japonès)
  Jean-Christophe Helary (francès i japonès)
  Yutaka Kachi (japonès)
  Elina Lagoudaki (grec)
  Martin Lukáč (eslovac)
  Samuel Murray (afrikaans)
  Yoshi Nakayama (japonès)
  David Olveira (portuguès)
  Ronaldo Radunz (portuguès de Brasil)
  Thelma L. Sabim (portuguès de Brasil)
  Juan Salcines (espanyol)
  Jordi Serratosa Quintana (català)
  Pablo Roca Santiagio (espanyol)
  Karsten Voss (polonès)
  Gerard van der Weyde (holandès)
  Martin Wunderlich (alemany)
  Hisashi Yanagida (japonès)
  Kunihiko Yokota (japonès)
  Erhan Yükselci (turc)
  Dragomir Kovacevic (serbocroat)
  Claudio Nasso (italià)
  Ahmet Murati (albanès)
  Sonja Tomaskovic (alemany)

Altres contribucions:
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (actual cap de documentació)
  Samuel Murray
  Marc Prior (actual cap de localització)
  i moltes altres persones que han aportat una ajuda inestimable.

(Si penseu que heu contribuït considerablement al projecte OmegaT i el vostre
nom no apareix a la llista, no dubteu en posar-vos en contacte amb nosaltres.)

L'OmegaT utilitza les biblioteques següents:
  HTMLParser, creada per Somik Raha, Derrick Oswald i altres (llicència
  LGPL).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter, creada per Steve Roy (llicència LGPL).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework, creada per VLSolutions (llicència CeCILL).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  Teniu problemes amb l'OmegaT? Necessiteu ajuda?

Abans de notificar un error, consulteu la documentació amb deteniment. És
possible que, allò que considereu un error, de fet sigui una característica
de l'OmegaT que acabeu de descobrir. Si al registre de l'OmegaT hi veieu
paraules com ara "Error", "Avís", "Excepció" o "ha finalitzat de manera
inesperada", pot ser que hagueu trobat un problema. El fitxer de registre
log.txt es troba al directori de preferències de l'usuari (consulteu el
manual d'usuari per a obtenir més informació sobre la ubicació).

El següent pas és compartir la informació de l'error que heu trobat amb la
resta dels usuaris, per a comprovar que no s'hagi notificat prèviament.
També podeu consultar la pàgina d'informes d'errors a SourceForge. Quan
estigueu segur que sou la primera persona que ha descobert alguna seqüència
reproduïble d'accions que desencadena un resultat inesperat, envieu un
informe d'error.

Per a crear un informe d'error que sigui realment útil, cal que hi
especifiqueu exactament aquestes tres coses:
  - Els passos per a reproduir l'error.
  - El resultat que esperàveu obtenir.
  - El resultat que obteniu (en comptes de l'esperat).

Podeu afegir-hi còpies de fitxers, fragments del registre, captures de
pantalla o qualsevol altra cosa que considereu que pot ajudar els
desenvolupadors a trobar i a solucionar l'error.

Per a navegar per l'arxiu de missatges del grup d'usuaris, visiteu:
     http://groups.yahoo.com/group/OmegaT/

Per a navegar per la pàgina d'informes d'errors i, si cal, enviar un informe
d'error nou, visiteu:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Per a fer un seguiment de l'informe d'error, cal que us registreu com a usuari
de SourceForge.

==============================================================================
6.   Detalls de la versió

5.  Teniu problemes amb l'OmegaT? Necessiteu ajuda?

Abans de notificar un error, consulteu la documentació amb deteniment. És
possible que, allò que considereu un error, de fet sigui una característica
de l'OmegaT que acabeu de descobrir. Si al registre de l'OmegaT hi veieu
paraules com ara "Error", "Avís", "Excepció" o "ha finalitzat de manera
inesperada", pot ser que hagueu trobat un problema. El fitxer de registre
log.txt es troba al directori de preferències de l'usuari (consulteu el
manual d'usuari per a obtenir més informació sobre la ubicació).

El següent pas és compartir la informació de l'error que heu trobat amb la
resta dels usuaris, per a comprovar que no s'hagi notificat prèviament.
També podeu consultar la pàgina d'informes d'errors a SourceForge. Quan
estigueu segur que sou la primera persona que ha descobert alguna seqüència
reproduïble d'accions que desencadena un resultat inesperat, envieu un
informe d'error.

Per a crear un informe d'error que sigui realment útil, cal que hi
especifiqueu exactament aquestes tres coses:
  - Els passos per a reproduir l'error.
  - El resultat que esperàveu obtenir.
  - El resultat que obteniu (en comptes de l'esperat).

Podeu afegir-hi còpies de fitxers, fragments del registre, captures de
pantalla o qualsevol altra cosa que considereu que pot ajudar els
desenvolupadors a trobar i a solucionar l'error.

Per a navegar per l'arxiu de missatges del grup d'usuaris, visiteu:
     http://tech.groups.yahoo.com/group/omegat/

Per a navegar per la pàgina d'informes d'errors i, si cal, enviar un informe
d'error nou, visiteu:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Per a fer un seguiment de l'informe d'error, cal que us registreu com a usuari
de SourceForge.

==============================================================================
6.   Detalls de la versió

Consulteu el fitxer 'changes.txt' per a veure informació detallada sobre els
canvis realitzats en aquesta versió i en les anteriors.

Formats de fitxer compatibles:
  - Text net
  - HTML i XHTML
  - Compilador d'ajuda HTML (HCC)
  - OpenDocument/OpenOffice.org
  - Paquets de recursos Java (.properties)
  - Fitxers INI (fitxers amb parelles clau=valor en qualsevol codificació)
  - Fitxers PO
  - Fitxers de documentació en format DocBook
  - Fitxers Microsoft OpenXML

Canvis principals:
  - Segmentació flexible (per frases)
  - Creació de filtres de fitxers en forma de connectors
  - Reconstrucció del codi amb més comentaris
  - Instal·lador del Windows
  - Els atributs de les etiquetes HTML són traduïbles
  - Compatibilitat total amb TMX 1.1-1.4b nivell 1
  - Compatibilitat parcial amb TMX 1.4b nivell 2

Noves característiques de la interfície d'usuari (respecte les versions 1.4
de l'OmegaT):
  - S'ha reescrit el diàleg de cerca amb més funcionalitat
  - La interfície principal s'ha millorat mitjançant l'ús de finestres
    acoblables

==============================================================================

