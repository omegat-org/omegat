***License***

All the files in this directory and below are under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

***Installation***

The creation of the documentation requires a number of tools:

- DocBook XSL Stylesheets 1.75.2 ("dbk")
- DocBook XML 4.5
- fop 0.95
- libxml2 2-2.7.7
- Saxon 6-5-5
- Ant 1.7.1 or above

For efficiency reason, the path to the DTD in the DocBook files (e.g., AboutOmegaT.xml) has been changed to a local path (../../../docbook-xml-4.5/docbookx.dtd instead of an http:// reference). Depending on the actual configuration, it might be changed to a standard reference ("http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd") or to another local reference.

***Windows***

dbk must be installed at the root of c:\

c:\dbk\common
c:\dbk\docsrc
etc.

Docbook-xml-4.5 must be installed at the same level as your OmegaT development folder.

So, if you have 

c:\dev\omegat-trunk
c:\dev\omegat-trunk\src
c:\dev\omegat-trunk\doc_src
etc.

you would have

c:\dev\docbook-xml-4.5\ent
c:\dev\docbook-xml-4.5\calstblx.dtd
etc.

libxml2-2.7.7 and apache-ant must be installed where a path can find them, usually in Program Files.
C:\Program Files\libxml2-2.7.7.win32
C:\Program Files\apache-ant

Corresponding path:
path=C:\Program Files\libxml2-2.7.7.win32\bin;C:\Program Files\apache-ant\bin

fop-0.95 can be installed anywhere, usually in Program Files.
C:\Program Files\fop-0.95

The location of fop-0.95, dbk and Saxon must be set in doc_src_paths.xml, in the corresponding properties:
    <property name="fop.home" value="C:\Program Files\fop-0.95" />
    <property name="dbk" value="c:\dbk" />
    <property name="saxon" value="C:\Program Files\saxon6-5-5\saxon.jar" />

The location of dbk must also be set in docbook-utf8.xsl:
<xsl:import href="file:///c:\dbk\html\docbook.xsl"/>

Both doc_src_paths and docbook-utf8.xsl must be copied at the same level as your OmegaT development folder.

c:\dev\doc_src_paths.xml
c:\dev\docbook-utf8.xsl

The ones available in doc_src are only there for reference.

***Mac OSX***

All the dependencies can be installed through Macports except for Saxon 6-5-5

Macports:
http://www.macports.org/

Saxon 6-5-5
https://sourceforge.net/projects/saxon/files/saxon6/6.5.5/
Unzip the downloaded package and put it in your prefered location. The example below uses the /Applications/ folder.

If you use Macports, the doc_src_paths.xml settings are:

    <property name="fop.home" value="/opt/local/share/java/fop/1.0" />
    <property name="dbk" value="/opt/local/share/xsl/docbook-xsl" />
    <property name="saxon" value="/Applications/saxon6-5-5/saxon.jar" />

and the docbook-utf8.xsl settings are:

    <xsl:import href="file:///opt/local/share/xsl/docbook-xsl/html/docbook.xsl"/>

The DocBook 4.5 DTD is located here:
/opt/local/var/macports/software/docbook-xml-4.5/4.5_0/opt/local/share/xml/docbook/4.5/

Copy the /4.5/ folder to the folder that is three folders "higher" than the DocBook documentation source files in your folder tree and rename it /docbook-xml-4.5/.

So, if you have your DocBook documentation source files in:
/path/to/omegat/branches/release-2-3/doc_src/hu/

The /docbook-xml-4.5/ folder should be copied inside /branches/:
/path/to/omegat/branches/docbook-xml-4.5/

Line 28 of build.xml should read:

    <include file="./doc_src_paths.xml"/>   

Line 133 of build.xml should read:

     <arg value="./docbook-utf8.xsl" />


***Usage***

All the scripts require the language folder as argument.

E.g.: 
Building Instant Start only
ant -Dlanguage=en instant-start

Building everything except Javahelp:
ant -Dlanguage=en

Building HTML
ant -Dlanguage=en html

Building PDF
ant -Dlanguage=en pdf

Building Javahelp
ant -Dlanguage=en javahelp

***Scripts***

build.xml: Main script.
doc_src_paths.xml: Set the paths for utilities.

docbook-utf8.xsl: Allows to issue the Instant Start Guide in UTF-8.