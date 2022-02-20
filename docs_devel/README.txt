Building OmegaT
===============

OmegaT is built with Gradle. Run `gradlew tasks` from the top level to see the
available tasks. Run `gradlew build` to build all possible distfiles and run the
main tests. Tasks skipped due to missing requirements will be noted in the
output.

OmegaT will run on the latest Java, but is required to be compatible with Java
1.8.

Eclipse and NetBeans are recommended IDEs for working with OmegaT source
code. NetBeans is required to modify *.form-based GUI layouts (install the
Gradle Support plugin to open the OmegaT project in NetBeans.)

Check the other files in this directory for documentation. You can
produce Javadoc by running `gradlew javadoc`, or browse online:

    https://omegat.sourceforge.io/javadoc-latest/


Configuring Build Tasks
=======================

Some build tasks, such as signed installers, require additional configuration
via a `local.properties` file placed at the root of the source tree. See
`local.properties.example`.


Build Assets
============

Some build tasks require the user to supply additional files not included in the
OmegaT source. These files should be placed in the assets directory, which by
default is `..`, i.e. one level up from the OmegaT source root. You can specify
a different directory by supplying the argument `-PassetDir=/path/to/wherever`.

In particular, with-JRE builds require a JRE tarball for the appropriate
platform, e.g. `jre-*-macosx-x64.tar.gz`.


Containerized Tasks
===================

Some build tasks require additional software to be installed:

- Windows installers require InnoSetup with all optional language files
  http://www.jrsoftware.org/isinfo.php

- Signed Windows installers require osslsigncode
  https://sourceforge.net/projects/osslsigncode/

- Docs generation requires a whole ecosystem. See /doc_src/Readme.txt

To alleviate the pain of installing and configuring things, these tools have
been containerized and automated via Docker. If you want to run these build
tasks, you are strongly recommended to install Docker:

    https://www.docker.com/


Working with Dependencies
=========================

OmegaT uses Gradle to manage and fetch dependencies automatically. To add,
change, or remove a dependency, see the dependencies block in build.gradle.

Dependency JARs are provided in the "source" distribution of OmegaT under
/lib/provided, as insurance in case any dependencies should become unavailable
online. When /lib/provided is present, compile-time dependencies will be
resolved locally, minimizing network access (network access is still required
for initializing the Gradle wrapper, fetching Gradle plugins, and fetching test
dependencies).


Customized Dependencies
=======================

OmegaT uses unmodified packages as much as possible, but when modifications are
required and the upstream project is abandoned or unresponsive, custom-patched
libraries are also used.

Patched forks can be found on GitHub:
    https://github.com/omegat-org

In some cases (especially defunct projects) OmegaT forks are also published for
general use on Maven Central:
    https://search.maven.org/search?q=g:org.omegat*

Dependencies not available for consumption via Maven/Gradle are committed to
/lib/manual.


Versioning
==========

OmegaT versions loosely follow semver (https://semver.org/). The version number
consists of three parts: MAJOR.MINOR.PATCH. When PATCH is zero it can be elided
from documentation or casual references, but not in code.

- MAJOR: The distinction between "standard" and "latest" flavors (see below)
- MINOR: "Large" changes that affect the UI or core functionality
- PATCH: Bug fixes and small changes that aren't "large"

In the past there was also an "update" number following PATCH, but this has been
deprecated.

There are two "flavors" of OmegaT:

- Standard: Stable, with a complete manual (sometimes "beta" in the code)
- Latest: May have breaking changes, incomplete manual


Contributing to OmegaT
======================

Main development is performed in the `master` branch of the SourceForge-hosted
Git repository:
    https://sourceforge.net/p/omegat/code/ci/master/tree/

A GitHub mirror is also provided:
    https://github.com/omegat-org/omegat

If you would like to submit a patch, the recommended procedure is as follows:

0. Consider opening a discussion on the developers' list first:
     http://lists.sourceforge.net/lists/listinfo/omegat-development

1. Open a ticket for your change. If it's a bug fix, create a Bug ticket:
     https://sourceforge.net/p/omegat/bugs/

   If it's a new feature or enhancement, open a Request For Enhancement (RFE):
     https://sourceforge.net/p/omegat/feature-requests/

2. If you prefer to work with file-based patches, attach your patch to the
   ticket.  If you prefer to work with pull requests, fork the OmegaT project on
   GitHub and open a pull request.

3. Patches/PRs will be reviewed by a core developer. If accepted, the changes
   will be committed to master by the reviewer.

4. OmegaT project use git for source code management, and core developers prefers
   a strategy of rebase-and-merge or squash-and-merge for git history.
   Developers can examine and bisect a single line of change history easily.

Note: Because core developers don't use github merge feature, your PR will marked as
close-without-merge on github but it is not a problem.

About Files in This Directory
=============================

Multiple translation fields for filter.ods

This documents how different filters identify "alternate" translations. You can
view it with LibreOffice or any ODF-compatible viewer.

OmegaT developer's guide.odt

This describes the overall architecture of OmegaT as well as some technical
details of its implementation. You can view it with LibreOffice or any
ODF-compatible viewer.

OmegaT.vpp

This document is the source for the UML diagrams in the developer's guide; it
contains no content not also viewable within the developer's guide.  Visual
Paradigm Community Edition (free) is required to edit the file
(http://www.visual-paradigm.com)
