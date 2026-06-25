The @language@ version of OmegaT has been translated by:
@TRANSLATION_NOTICE@


What is OmegaT?
===============

OmegaT is a free and open source multiplatform Computer Assisted Translation
tool with fuzzy matching, translation memory, keyword search, glossaries, and
translation leveraging into updated projects.



Licensing information
=====================

OmegaT is available under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the License or
(at your option) any later version. You can find the text of the license in
/docs/OmegaT-license.txt.

OmegaT uses a number of libraries. The license of each library is mentioned in
/lib/licenses/Licenses.txt.



Prerequisites for installing OmegaT
===================================

OmegaT requires the Java Runtime Environment (JRE) version 11 or higher to be
installed on your system. Bundled JRE packages currently ship with JRE 17 or 21,
and OmegaT has been verified to run correctly up to Java 21.

We recommend using OmegaT packages that include the JRE, to save you the trouble
of selecting, getting, and installing it.



Available packages
===================

OmegaT is distributed in several package formats, depending on your platform
and needs:

Linux:
  - deb and rpm packages are provided for the amd64 and aarch64
    architectures. Older 32-bit environments are not supported.
  - A tar.bz2 archive is also available.

Mac:
  - A notarized dmg package is provided, supporting both Apple Silicon and
    Intel architectures.

Windows:
  - Installer EXE files are provided in three variants: one bundled with an
    amd64 JRE, one bundled with an aarch64 JRE, and one without any JRE
    (for use with a JRE already installed on your system). A 32-bit
    JRE-bundled variant is not provided. Note that the installer program
    itself is a 32-bit Intel binary; this does not affect the architecture
    of OmegaT or the JRE that gets installed.

Source package:
  - The source package bundles all required library dependencies, so its
    size is relatively large. For normal development purposes, we recommend
    cloning the repository from GitHub instead. The source package is
    intended to support building OmegaT even in environments with limited
    or no internet access.

Simple ZIP archive:
  - A plain ZIP archive is also available. This is useful on Linux or
    Windows when you need a portable package and can supply your own JRE.
    It may also be used on Mac for self-notarization purposes.



Installing OmegaT (Windows)
===========================

Launch the installation program.



Installing OmegaT (Mac)
=======================

Open the OmegaT .dmg package and drag the OmegaT application to the
Applications folder (or another location of your choice).



Installing OmegaT (Linux)
=========================

Using the deb or rpm package
----------------------------

Install the package for your architecture (amd64 or aarch64) using your
distribution's package manager, e.g.:

    sudo dpkg -i omegat_<version>_<arch>.deb

or

    sudo rpm -i omegat-<version>.<arch>.rpm

Using the tar.bz2 archive
-------------------------

Place the archive in any suitable folder and unpack it. OmegaT is then ready
to be launched.

You can, however, get a neater and more user-friendly installation by using the
installation script (linux-install.sh). To use this script, open a terminal
window (console), change folder to the folder containing OmegaT.jar and the
linux-install.sh script, and execute the script with ./linux-install.sh.



Installing OmegaT (Solaris, FreeBSD, etc.)
==========================================

Place the archive in any suitable folder and unpack it. OmegaT is then ready to
be launched.



Launching OmegaT (Windows)
==========================

If, during installation, you have created a shortcut on the desktop,
double-click on that shortcut.

The installer can create shortcuts for you in the start menu, on the desktop, and
in the quick launch area. You can also manually drag the file OmegaT.exe to the
start menu, the desktop, or the quick launch area to link it from there.

If you can see the file OmegaT but not OmegaT.exe in your File Manager (Windows
Explorer), change the settings so that file extensions are displayed.



Launching OmegaT (Mac)
======================

Double-click on the OmegaT application.

You may drag the OmegaT application to your dock or to the toolbar of a Finder
window to be able to launch it from any location. You can also launch it from
the Spotlight search field.



Launching OmegaT (Linux)
========================

If you installed OmegaT using the deb or rpm package, launch it from your
desktop environment's application menu, or run:

    omegat

from a terminal.

If you used linux-install.sh script, you should be able to launch OmegaT with:

    Alt+F2

and then:

    omegat

For a more user-friendly way of launching OmegaT, you can use the Kaptain script
provided (omegat.kaptn). To use this script you must first install Kaptain. You
can then launch the Kaptain launch script with:

    Alt+F2

and then:

    omegat.kaptn



Launching OmegaT from the command line (all systems)
====================================================

The command to launch OmegaT is:

    cd <folder where the file OmegaT.jar is located>

    <name and path of the Java executable file> -jar OmegaT.jar

(The Java executable file is the file java on Linux and java.exe on Windows.  If
Java is installed at system level and is in the command path, the full path need
not be entered.)



Contributors
============

OmegaT is the original work of Keith Godfrey.

Jean-Christophe Helary is the OmegaT project manager.

Current team:
(alphabetical order)

- Marco Cevoli (Telegram community manager)
- Jean-Christophe Helary (Twitter community manager)
- Kos Ivantsov (localisation manager, user group owner)
- Concepción Martin (Facebook community manager)
- Hiroshi Miura (lead developer and product integration manager)
- Briac Pilpré (webmaster)
- Philippe Tourigny (documentation manager)
- Lucie Vecerova (Facebook community manager)

Contributions to the code are documented in /docs/contributors.txt.

Previous contributors include:
(alphabetical order)

- Anthony Baldwin (localisation manager)
- Vincent Bidaux (documentation manager)
- Didier Briel (project manager)
- Alex Buloichik (lead developer)
- Sabine Cretella
- Dmitri Gabinski
- Aaron Madlon-Kay (project manager)
- Maxym Mykhalchuk (lead developer)
- Samuel Murray
- Henry Pijffers (release manager)
- Marc Prior (project co-ordinator, webmaster)
- Vito Smolej (documentation manager)

and many, many more very helpful people

(If you think you have significantly contributed to the OmegaT Project but you
don't see your name on the lists, feel free to contact us.)



Useful links
============

The most current info about OmegaT can be found at:

  https://omegat.org/

User support resources:

  https://omegat.org/support

Requests for Enhancements (in English), at the SourceForge site:

  https://sourceforge.net/p/omegat/feature-requests/

Bug reports (in English), at the SourceForge site:

  https://sourceforge.net/p/omegat/bugs/

OmegaT Contribution Guide

  https://omegat.readthedocs.io/en/latest/