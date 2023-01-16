# License

All the files in this directory and below are under the terms of the GNU General
Public License as published by the Free Software Foundation; either version 3 of
the License, or (at your option) any later version.

# Installation

The easy way is to install
[Docker](https://www.docker.com/products/docker-desktop) and run the `docgen`
script provided in `doc_src`. Arguments to `docgen` are the same as the
arguments to `ant` described below in the Usage section.

Get Docker without needing to make an account:

- [Docker Desktop for Windows](https://docs.docker.com/docker-for-windows/install/#install-docker-desktop-for-windows-desktop-app)
- [Docker Desktop for Mac](https://docs.docker.com/docker-for-mac/release-notes/)
- [Docker CE for Linux](https://docs.docker.com/install/#supported-platforms)

See also:

- [docgen-docker](https://github.com/omegat-org/docgen-docker): source for the
  docgen container

## The hard way

### Dependencies

The creation of the documentation requires a number of tools:

- DocBook XSL Stylesheets 1.75.2 ("dbk") or above
  [https://github.com/docbook/xslt10-stylesheets](https://github.com/docbook/xslt10-stylesheets)
- DocBook XML 4.5 ("docbook-xml-4.5")
  [https://docbook.org/xml/4.5/](https://docbook.org/xml/4.5/)
- fop 1.1 ("fop-1.1")
  [https://xmlgraphics.apache.org/fop/1.1/](https://xmlgraphics.apache.org/fop/1.1/)
- libxml2 2-2.7.7 ("libxml2-2.7.7")
  [http://xmlsoft.org](http://xmlsoft.org)
- Saxon 6-5-5 ("saxon")
  [https://sourceforge.net/projects/saxon/files/saxon6/](https://sourceforge.net/projects/saxon/files/saxon6/)
- XMLmind Web Help Compiler ("whc")
  [https://www.xmlmind.com/ditac/whc.shtml](https://www.xmlmind.com/ditac/whc.shtml)
- Ant 1.7.1 or above ("apache-ant")
  [https://ant.apache.org](https://ant.apache.org)

### Path to the DTD

For efficiency reason, the path to the DTD in the DocBook files (e.g.,
`AboutOmegaT.xml`) has been changed to a local path
(`../../../docbook-xml-4.5/docbookx.dtd` instead of an `http://`
reference). Depending on the actual configuration, it might be changed to a
standard reference (`http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd`) or
to another local reference.

### Windows

dbk must be installed at the root of `C:`

    C:\dbk\common
    C:\dbk\docsrc
    ⋮

docbook-xml-4.5 must be installed at the same level as your OmegaT development
folder.

So, if you have

    C:\dev\omegat-trunk
    C:\dev\omegat-trunk\src
    C:\dev\omegat-trunk\doc_src
    ⋮

you would have

    C:\dev\docbook-xml-4.5\ent
    C:\dev\docbook-xml-4.5\calstblx.dtd
    ⋮

libxml2-2.7.7 and apache-ant must be installed where a path can find them,
usually in Program Files.

    C:\Program Files\libxml2-2.7.7.win32
    C:\Program Files\apache-ant

Corresponding `path`:

    path=C:\Program Files\libxml2-2.7.7.win32\bin;C:\Program Files\apache-ant\bin

fop-1.1 can be installed anywhere, usually in Program Files.

    C:\Program Files\fop-1.1

The location of fop-1.1, dbk and Saxon must be set in `doc_src_paths.xml`,
in the corresponding properties:

    <property name="fop.home" value="C:\Program Files\fop-1.1" />

Alternatively, it is possible to set the values of `fop.home.lib` and
`fop.home.build` separately:

    <property name="fop.home.lib" value="C:\Program Files (x86)\fop-1.1\lib" />
    <property name="fop.home.build" value="C:\Program Files (x86)\fop-1.1\build" />

    <property name="dbk" value="c:\dbk" />
    <property name="saxon" value="C:\Program Files\saxon6-5-5\saxon.jar" />

The location of dbk must also be set in `docbook-utf8.xsl`:

    <xsl:import href="file:///c:\dbk\html\docbook.xsl"/>

Both `doc_src_paths.xml` and `docbook-utf8.xsl` must be copied at the same level
as your OmegaT development folder.

    C:\dev\doc_src_paths.xml
    C:\dev\docbook-utf8.xsl

The ones available in `doc_src` are only there for reference.

### macOS

#### Dependencies

All the dependencies can be installed through
[MacPorts](https://www.macports.org/) except for saxon and whc.

    sudo port install apache-ant docbook-xml-4.5 docbook-xsl-nons fop libxml2

- Saxon 6-5-5 ("saxon")
  [https://sourceforge.net/projects/saxon/files/saxon6/](https://sourceforge.net/projects/saxon/files/saxon6/)

- XMLmind Web Help Compiler ("whc")
  [https://www.xmlmind.com/ditac/whc.shtml](https://www.xmlmind.com/ditac/whc.shtml)

Unzip the downloaded packages and put them in your prefered location. The
example below uses the `/Applications` folder.

#### Paths

If you use Macports, the `doc_src_paths.xml` settings are:

    <project>
        <property name="fop.home" value="/opt/local/share/java/fop/1.1" />
        <property name="dbk" value="/opt/local/share/xsl/docbook-xsl-nons" />
        <property name="saxon" value="/Applications/saxon6-5-5/saxon.jar" />
	    <property name="whc" value="/Application/whc-3_3_0/lib/whc.jar" />
    </project>

and the `docbook-utf8.xsl` settings are:

    <xsl:import href="file:///opt/local/share/xsl/docbook-xsl-nons/html/docbook.xsl"/>

#### DocBook 4.5 installation

Create a symbolic link from the installed DocBook 4.5 files to the location
where the build process will look for the DocBook DTD: the folder "above"
`doc_src` in the OmegaT source tree:

    ln -s /opt/local/share/xml/docbook/4.5 /path/to/omegat/docbook-xml-4.5

#### Settings files

The modified `doc_src_paths.xml` and `docbook-utf8.xsl` must be copied to the
same location as `docbook-xml-4.5`.

    /path/to/omegat/doc_src/en/
       └────/docbook-xml-4.5/
       └────/doc_src_paths.xml
	   └────/docbook-utf8.xsl

# Fonts
The following fonts must be installed:

- Latin: [DejaVu Sans](https://dejavu-fonts.github.io/)
- Japanese: [IPA P
  Gothic](https://ipafont.ipa.go.jp/old/ipafont/download.html#en)
- Chinese: [WenQuanYi Micro
  Hei](http://wenq.org/wqy2/index.cgi?action=browse&id=Home&lang=en)

# Usage

All the scripts require the language folder as argument and must be run from the
`doc_src` directory.

Note: Replace `ant` with `docgen` if you are using the `docgen` script.

## Building First Steps only

    ant -Dlanguage=en first-steps

## Building everything except Javahelp:

    ant -Dlanguage=en

## Building HTML

    ant -Dlanguage=en html5

## Building PDF

    ant -Dlanguage=en pdf

## Building Javahelp

    ant -Dlanguage=en javahelp

# Scripts

- `build.xml`: Main script
- `doc_src_paths.xml`: Set the paths for utilities
- `docbook-utf8.xsl`: Allows to issue the First Steps in UTF-8
