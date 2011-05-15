***License***

All the files in this directory and below are under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

***Installation***

The creation of the documentation requires a number of tools:

- DocBook XSL Stylesheets 1.75.2 (“dbk”)
- DocBook XML 4.5
- fop 0.95
- libxml2 2-2.7.7
- Saxon 6-5-5
- Ant 1.7.1 or above

For efficiency reason, the path to the DTD in the DocBook files (e.g., AboutOmegaT.xml) has been changed to a local path (../../../docbook-xml-4.5/docbookx.dtd instead of an http:// reference). Depending on the actual configuration, it might be changed to a standard reference ("http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd") or to another local reference.

The instructions below give an example of installation under Windows.

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
C:\Program Files\apache-ant\bin

Corresponding path:
path=C:\Program Files\libxml2-2.7.7.win32\bin;C:\Program Files\apache-ant\bin

fop-0.95 can be installed anywhere, usually in Program Files.
C:\Program Files\fop-0.95

The location of fop-0.95, dbk and Saxon must be set in build.xml, in the corresponding properties:
    <property name="fop.home" value="C:\Program Files\fop-0.95" />
    <property name="dbk" value="c:\dbk" />
    <property name="saxon" value="C:\Program Files\saxon6-5-5\saxon.jar" />

The location of dbk must also be set in docbook-utf8.xsl:
<xsl:import href="file:///c:\dbk\html\docbook.xsl"/> 

***Usage***

All the scripts require the language folder as argument.

E.g.: 
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

docbook-utf8.xsl: Allows to issue the Instant Start Guide in UTF-8.