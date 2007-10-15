Aquesta traducció és obra d'en Jordi Serratosa, copyright© 2007.
Han participat en la localització de l'OmegaT al català:
Mikel Forcada Zubizarreta (versió 1.4)
Jordi Serratosa Quintana (versió 1.6 a l'actual)

==============================================================================
  OmegaT 1.7.2, fitxer README

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
 3. Instal·lació de l'OmegaT

3.1 General
Per a poder executar l'OmegaT, és necessari tenir instal·lat al sistema
l'entorn d'execució de Java (Java Runtime Environment, JRE) versió 1.4 o
superior. Actualment, la versió estàndard de l'OmegaT inclou l'entorn
d'execució de Java per tal d'estalviar als usuaris la necessitat de
seleccionar-lo, de baixar-lo i d'instal·lar-lo. Usuaris del Windows i Linux:
si esteu segur que el sistema ja té instal·lada una versió correcta del
JRE, podeu instal·lar la versió de l'OmegaT sense el JRE (que s'indica al
nom de la versió: "Without_JRE"). Si teniu dubtes, és recomanable que
utilitzeu la versió "estàndard", que inclou el JRE. No patiu: encara que ja
tingueu una versió del JRE instal·lada al sistema, aquesta versió no
interferirà amb l'altra.
Usuaris del Linux: tingueu en compte que l'OmegaT no funciona amb les
implementacions del Java lliures o de codi obert que s'inclouen en moltes
distribucions del Linux (per exemple, Ubuntu), ja que no són actuals o són
incompletes. Podeu baixar i instal·lar l'entorn d'execució de Java (JRE) de
Sun des de l'enllaç anterior, o bé podeu baixar i instal·lar l'OmegaT amb
el JRE inclòs (el paquet .tar.gz que conté el text "Linux").
Usuaris del Mac: el Mac OS X ja té instal·lat el JRE.
Usuaris del Linux en sistemes PowerPC: cal baixar el JRE d'IBM, ja que Sun no
ofereix cap JRE per a sistemes PPC. En aquest cas, el podeu baixar des
d'aquí:
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

3.2 Instal·lació
Per a instal·lar l'OmegaT, simplement cal que creeu una carpeta per a
l'OmegaT (per exemple, C:\Archivos de programa\OmegaT al Windows o bé
/usr/local/lib al Linux). Copieu l'arxiu zip de l'OmegaT a aquesta carpeta i
descomprimiu-lo.

3.3 Execució de l'OmegaT
L'OmegaT es pot executar de diverses maneres.

* Usuaris del Windows: feu doble clic al fitxer OmegaT-JRE.exe, si utilitzeu
la versió amb el JRE inclòs, o bé al fitxer OmegaT.exe si no inclou el JRE.

* Feu doble clic al fitxer OmegaT.bat. Si al gestor de fitxers (l'Explorador
del Windows) hi veieu el fitxer OmegaT però no l'OmegaT.bat, canvieu la
configuració per tal que es mostrin les extensions dels fitxers.

* Feu doble clic al fitxer OmegaT.jar. Només funcionarà si el tipus de
fitxer .jar està associat amb el Java al sistema.

* Des de la línia d'ordres. L'ordre per a executar l'OmegaT és:

cd <carpeta on es troba el fitxer OmegaT.jar>

<nom i camí del fitxer executable del Java> -jar OmegaT.jar

El fitxer executable del Java s'anomena java al Linux i java.exe al Windows.
Si teniu instal·lat el Java a nivell del sistema, el camí complet no és
necessari.

* Usuaris del users: podeu arrossegar els fitxers OmegaT-JRE.exe, OmegaT.exe o
OmegaT.bat a l'escriptori o al menú d'Inici per tal de crear-hi un enllaç.

* Usuaris del KDE (Linux): podeu afegir l'OmegaT als menús seguint aquestes
instruccions:

Centre de control - Escriptori - Plafons - Menús - Edita el menú K - Fitxer
- Element nou/Submenú nou.

A continuació, després de seleccionar un menú adient, afegiu-hi un
submenú/element seleccionant Fitxer - Submenú nou i Fitxer - Element nou.
Escriviu OmegaT com a nom del element nou.

Al camp "Ordre", utilitzeu el botó de navegació per a buscar l'script
d'execució de l'OmegaT i seleccioneu-lo.

Feu clic al botó de la icona (a la part dreta dels camps
Nom/Descripció/Comentari) - Altres icones - Navega, i navegueu fins a la
supcarpeta /images de la carpeta d'instal·lació de l'OmegaT. Seleccioneu la
icona OmegaT.png.

Finalment, deseu els canvis seleccionant Fitxer - Desa.

* Usuaris del GNOME (Linux): podeu afegir l'OmegaT al quadre (la barra de la
part de dalt de la pantalla) seguint aquestes instruccions:

Feu clic amb el botó dret al quadre - Afegeix un nou llançador. Escriviu
"OmegaT" al camp "Nom"; al camp "Ordre", utilitzeu el botó de navegació per
a buscar l'script d'execució de l'OmegaT. Seleccioneu-lo i feu clic al botó
D'acord.

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

Altres contribucions:
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (cap de documentació)
  Samuel Murray
  Marc Prior (cap de localització)
  i moltes altres persones que han aportat una ajuda inestimable.

(Si creieu que heu contribuït considerablement al projecte OmegaT i el vostre
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

Consulteu el fitxer 'changes.txt' per a veure informació detallada sobre els
canvis realitzats en aquesta versió i en les anteriors.

Formats de fitxer compatibles:
  - Text net
  - HTML i XHTML
  - Compilador d'ajuda HTML
  - OpenDocument/OpenOffice.org
  - Paquets de recursos Java (.properties)
  - Fitxers INI (fitxers amb parelles clau=valor en qualsevol codificació)
  - Fitxers PO
  - Fitxers de documentació en format DocBook
  - Fitxers Microsoft OpenXML
  - Fitxers XLIFF monolíngües de l'Okapi

Canvis principals:
  -

Noves característiques de la interfície d'usuari (respecte les versions 1.6
de l'OmegaT):
  -

==============================================================================

