***Licentie***

Alle bestanden in deze map en daaronder zijn onderworpen aan de termen van de GNU General Public License zoals die zijn gepubliceerd door de Free Software Foundation; ofwel versie 2 van de License, of (naar uw eigen inzicht) een willekeurige latere versie.

***Installeren****

Het najen van de documentatie veriest een aantal programma's:

- DocBook XSL Stylesheets 1.75.2 ("dbk")
- DocBook XML 4.5
- fop 0.95
- libxml2 2-2.7.7
- Saxon 6-5-5
- Ant 1.7.1 of hoger

Om redenen van efficiëntie is het pad naar de DTD in de bestanden van DocBook (bijv., AboutOmegaT.xml) is gewijzigd naar een lokaal pad (../../../docbook-xml-4.5/docbookx.dtd in plaats van een http://-verwijzing). Afhankelijk van de actuele configuratie kan die worden gewijzigd naar een standaard verwijzing ("http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd") of naar een andere lokale verwijzing.

***Windows***

dbk moet zijn geïnstalleerd in de root van c:\

c:\dbk\common
c:\dbk\docsrc
etc.

Docbook-xml-4.5 moet zijn geïnstalleerd op hetzelfde niveau als de map van uw OmegaT ontwikkeling.

Dus als u hebt 

c:\dev\omegat-trunk
c:\dev\omegat-trunk\src
c:\dev\omegat-trunk\doc_src
etc.

zou u moeten hebben

c:\dev\docbook-xml-4.5\ent
c:\dev\docbook-xml-4.5\calstblx.dtd
etc.

libxml2-2.7.7 en apache-ant moeten zijn geïnstalleerd waar zij door een pad kunnen worden gevonden, normala gesproken in Program Files.
C:\Program Files\libxml2-2.7.7.win32
C:\Program Files\apache-ant

Overeenkomende pad:
path=C:\Program Files\libxml2-2.7.7.win32\bin;C:\Program Files\apache-ant\bin

fop-0.95 kan overal worden geïnstalleerd, normaal gesproken in Program Files.
C:\Program Files\fop-0.95

De lokatie van fop-0.95, dbk en Saxon moet worden ingesteld in doc_src_paths.xml, in de overenkomend eeigenschappen:
    <property name="fop.home" value="C:\Program Files\fop-0.95" />
    <property name="dbk" value="c:\dbk" />
    <property name="saxon" value="C:\Program Files\saxon6-5-5\saxon.jar" />

De lokatie van dbk moet ook wordne ingesteld in docbook-utf8.xsl:
<xsl:import href="file:///c:\dbk\html\docbook.xsl"/>

Zowel doc_src_paths als docbook-utf8.xsl moeten op hetzelfde niveau gekopieerd zijn als uw map OmegaT ontwikkeling.

c:\dev\doc_src_paths.xml
c:\dev\docbook-utf8.xsl

Die welke beschikbaar zijn in doc_src zijn alleen ter verwijzing.

***Mac OSX***

Alle afhankelijkheden kunnen worden geïnstalleerd via Macports behalve Saxon 6-5-5

Macports:
http://www.macports.org/

Saxon 6-5-5
https://sourceforge.net/projects/saxon/files/saxon6/6.5.5/
Oak het gedownloade pakket uit en plaats het op de door u gewenste lokatie. Het voorbeeld hieronder gebruikt de map /Applications/.

Als u Macports gebruikt zijn de instellingen voor doc_src_paths.xml:

    <property name="fop.home" value="/opt/local/share/java/fop/1.0" />
    <property name="dbk" value="/opt/local/share/xsl/docbook-xsl" />
    <property name="saxon" value="/Applications/saxon6-5-5/saxon.jar" />

en de instellingen voor docbook-utf8.xsl zijn:

    <xsl:import href="file:///opt/local/share/xsl/docbook-xsl/html/docbook.xsl"/>

De DocBook 4.5 DTD is hier geplaatst:
/opt/local/var/macports/software/docbook-xml-4.5/4.5_0/opt/local/share/xml/docbook/4.5/

Kopieer de map /4.5/ naar de map die 3 mappen "hoger" ligt dan de bronbestanden voor de documenattie in Docbook in uw boomstructuur en hernoem die naar /docbook-xml-4.5/.

Dus als u bronbestanden voor de documentatie in Docbook staat in:
/pad/naar/omegat/branches/release-2-3/doc_src/hu/

Zou de map /docbook-xml-4.5/ moeten worden gekopieerd in /branches/:
/pad/naar/omegat/branches/docbook-xml-4.5/

Zowel doc_src_paths als docbook-utf8.xsl moeten op hetzelfde niveau gekopieerd zijn als  /docbook-xml-4.5/.

Die welke beschikbaar zijn in doc_src zijn alleen ter verwijzing.



***Gebruik***

Alle scripts vereisen de taalmap als as argument.

Bijv.: 
Bouwen van alleen Instant Start
ant -Dlanguage=en instant-start

Bouwen van alles behalve Javahelp:
ant -Dlanguage=en

Bouwen van HTML
ant -Dlanguage=en html

Bouwen van PDF
ant -Dlanguage=en pdf

Bouwen Javahelp
ant -Dlanguage=en javahelp

***Scripts***

build.xml: Hoofdscript.
doc_src_paths.xml: Stelt de paden in voor de utilities.

docbook-utf8.xsl: Maakt het mogelijk de Instant Start Guide uit te geven in UTF-8.