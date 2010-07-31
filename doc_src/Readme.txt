***License***

All the files in this directory and below are under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

***Installation***

The creation of the documentation requires a number of tools:

- DocBook XSL Stylesheets 1.75.2 (“dbk”)
- DocBook XML 4.5
- fop 0.95
- libxml2 2.6.27
- Saxon 6-5-5

For efficiency reason, the path to the DTD in the DocBook files (e.g., 01_AboutOmegaT.xml) has been changed to a local path (file:/// instead of a http:// reference). Depending on the actual configuration, it might be changed to a standard reference ("http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd") or to another local reference.

The instructions below give an example of installation under Windows.

dbk and docbook-xml-4.5 must be installed at the root of c:\

c:\dbk\common
c:\dbk\docsrc
etc.

c:\docbook-xml-4.5\ent
c:\docbook-xml-4.5\calstblx.dtd
etc.

libxml2-2.6.27 and fop-0.95 must be installed where a path can find them, usually in Program Files.
C:\Program Files\libxml2-2.6.27.win32
C:\Program Files\fop-0.95

Corresponding path:
path=C:\Program Files\fop-0.95;C:\Program Files\libxml2-2.6.27.win32\bin

saxon6-5-5.zip must be installed where a classpath can find it, usually in Program Files.
C:\Program Files\saxon6-5-5

Corresponding classpath:
classpath=C:\Program Files\saxon6-5-5\saxon.jar;C:\Program Files\saxon6-5-5\saxon-xml-apis.jar

***Usage***

All the scripts require the language folder as argument.

E.g.: html en

***Scripts***

Xincludes.bat: Creates the complete DocBook documentation (index.xml) by including the various chapters

fo.bat: Creates the intermediate fo format used to create a PDF in /language/pdf

HTML.bat: Calls Xincludes and then creates the HTML documentation in language/html

PDF.bat: Calls Xincludes and fo and then creates the PDF documentation in language/html

