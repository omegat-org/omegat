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

For efficiency reason, the path to the DTD in the DocBook files (e.g., AboutOmegaT.xml) has been changed to a local path (file:/// instead of a http:// reference). Depending on the actual configuration, it might be changed to a standard reference ("http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd") or to another local reference.

The instructions below give an example of installation under Windows.

dbk and docbook-xml-4.5 must be installed at the root of c:\

c:\dbk\common
c:\dbk\docsrc
etc.

c:\docbook-xml-4.5\ent
c:\docbook-xml-4.5\calstblx.dtd
etc.

libxml2-2.7.7 and apache-ant must be installed where a path can find them, usually in Program Files.
C:\Program Files\libxml2-2.7.7.win32
C:\Program Files\apache-ant\bin

Corresponding path:
path=C:\Program Files\libxml2-2.7.7.win32\bin;C:\Program Files\apache-ant\bin

fop-0.95 can be installed anywhere, usually in Program Files.
C:\Program Files\fop-0.95

The location of fop-0.95 must be set in build.xml, in the fop.home property.
<property name="fop.home" value="C:\Program Files\fop-0.95" />

***Usage***

All the scripts require the language folder as argument.

E.g.: 
html en
PDF en
ant -Dlanguage=en

***Scripts***

Xincludes.bat: Creates the complete DocBook documentation (index.xml) by including the various chapters

classpath.bat: Sets the classpath for Saxon

fo.bat: Creates the intermediate fo format used to create a PDF in /language/pdf

HTML.bat: Calls Xincludes and then creates the HTML documentation in language/html

PDF.bat: Calls Xincludes and fo and then creates with ant the PDF documentation in language/pdf

Javahelp.bat (not yet operationnal): Calls Xincludes and then creates the Javahelp documentation in language/javahelp

build.xml: Creates the PDF in language/pdf

docbook-utf8.xsl: Allows to issue the Instant Start Guide in UTF-8. The path of DocBook must be set in this file (e.g., file:///c:\dbk\html\docbook.xsl)