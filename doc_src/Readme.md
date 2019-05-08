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

The creation of the documentation requires a number of tools:

- DocBook XSL Stylesheets 1.75.2 ("dbk")
- DocBook XML 4.5
- fop 1.1
- libxml2 2-2.7.7
- Saxon 6-5-5
- Ant 1.7.1 or above

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

All the dependencies can be installed through MacPorts except for Saxon 6-5-5:

- [MacPorts](https://www.macports.org/)
- [Saxon 6-5-5](https://sourceforge.net/projects/saxon/files/saxon6/6.5.5/)

Unzip the downloaded package and put it in your prefered location. The example
below uses the `/Applications` folder.

If you use Macports, the `doc_src_paths.xml` settings are:

    <property name="fop.home" value="/opt/local/share/java/fop/1.0" />
    <property name="dbk" value="/opt/local/share/xsl/docbook-xsl" />
    <property name="saxon" value="/Applications/saxon6-5-5/saxon.jar" />

and the `docbook-utf8.xsl` settings are:

    <xsl:import href="file:///opt/local/share/xsl/docbook-xsl/html/docbook.xsl"/>

The DocBook 4.5 DTD is located here:

    /opt/local/var/macports/software/docbook-xml-4.5/4.5_0/opt/local/share/xml/docbook/4.5/

Copy the `4.5` folder to the folder that is three folders "higher" than the
DocBook documentation source files in your folder tree and rename it
`/docbook-xml-4.5`.

So, if you have your DocBook documentation source files in:

    /path/to/omegat/branches/release-2-3/doc_src/hu/

The `/docbook-xml-4.5` folder should be copied inside `branches`:

    /path/to/omegat/branches/docbook-xml-4.5/

Both `doc_src_paths.xml` and `docbook-utf8.xsl` must be copied at the same level
as `docbook-xml-4.5`.

The ones available in `doc_src` are only there for reference.

# Fonts
The following fonts must be installed:

- Latin: [DejaVu Sans](https://dejavu-fonts.github.io/)
- Japanese: [IPA P
  Gothic](https://ipafont.ipa.go.jp/old/ipafont/download.html#en)
- Chinese: [WenQuanYi Micro
  Hei](http://wenq.org/wqy2/index.cgi?action=browse&id=Home&lang=en)

# Usage

All the scripts require the language folder as argument.

Note: Replace `ant` with `docgen` if you are using the `docgen` script.

## Building Instant Start only

    ant -Dlanguage=en instant-start

## Building everything except Javahelp:

    ant -Dlanguage=en

## Building HTML

    ant -Dlanguage=en html

## Building PDF

    ant -Dlanguage=en pdf

## Building Javahelp

    ant -Dlanguage=en javahelp

# Scripts

- `build.xml`: Main script
- `doc_src_paths.xml`: Set the paths for utilities
- `docbook-utf8.xsl`: Allows to issue the Instant Start Guide in UTF-8
