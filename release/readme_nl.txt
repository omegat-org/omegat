De versie @language@ van OmegaT is vertaald door:
@TRANSLATION_NOTICE@


Wat is OmegaT?
==============

OmegaT is een vrij en open source multiplatform Computer Assisted Translation-programma met fuzzy overeenkomsten, vertaalgeheugen, zoeken op sleutelwoorden, woordenlijsten en vertalinguitbreiding in bijgewerkte projecten.



Informatie over Licentie
========================

OmegaT is available under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the License or
(at your option) any later version. U vindt de tekst van de licentie in
/docs/OmegaT-license.txt.

OmegaT uses a number of libraries. De licentie van elke bibliotheek is vermeld
in /lib/licenses/Licenses.txt.



Vereisten voor installeren van OmegaT
=====================================

OmegaT requires the Java Runtime Environment (JRE) version 11 or higher to be
installed on your system. Bundled JRE packages currently ship with JRE 17.

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



Installeren van OmegaT (Windows)
================================

Start het installatieprogramma.



Installeren van OmegaT (Mac)
=======================

Open the OmegaT .dmg package and drag the OmegaT application to the
Applications folder (or another location of your choice).



Installeren van OmegaT (Linux)
==============================

Using the deb or rpm package
----------------------------

Install the package for your architecture (amd64 or aarch64) using your
distribution's package manager, e.g.:

    sudo dpkg -i omegat_<version>_<arch>.deb

of

    sudo rpm -i omegat-<version>.<arch>.rpm

Using the tar.bz2 archive
-------------------------

Plaats het archief in een geschikte map en pak het uit. OmegaT is then ready
to be launched.

You can, however, get a neater and more user-friendly installation by using the
installation script (linux-install.sh). Open, om dit script te gebruiken, een venster voor de terminal
(console), wijzig de folder naar de folder die OmegaT.jar bevat en het script linux-install.sh, en voer het script uit met ./linux-install.sh.



Installing OmegaT (Solaris, FreeBSD, etc.)
==========================================

Plaats het archief in een geschikte map en pak het uit. OmegaT is dan gereed om te worden opgestart.



Starten van OmegaT (Windows)
============================

Als u, gedurende de installatie, een snelkoppeling op het bureaublad hebt gemaakt,
dubbelklik dan op die snelkoppeling.

The installer can create shortcuts for you in the start menu, on the desktop, and
in the quick launch area. You can also manually drag the file OmegaT.exe to the
start menu, the desktop, or the quick launch area to link it from there.

Als u het bestand OmegaT wel in uw bestandsbeheer (Windows Verkenner) kunt zien
maar niet OmegaT.exe, wijzig dan de instellingen zodat de bestandsextensies worden weergegeven.



Starten van OmegaT (Mac)
======================

Dubbelklik op de toepassing OmegaT.

Sleep de toepassing OmegaT naar uw dock of naar de werkbalk van een Finder-venster
om het vanaf elke locatie te kunnen starten. U kunt het ook starten
 in het zoekveld van Spotlight.



Starten van OmegaT (Linux)
==========================

If you installed OmegaT using the deb or rpm package, launch it from your
desktop environment's application menu, or run:

    omegat

from a terminal.

Indien u linux-install.sh script gebruikte, zou u OmegaT moeten kunnen opstarten met:

    Alt+F2

en dan:

    omegat

Voor een meer gebruikersvriendelijker manier om OmegaT op te starten, kunt u het meegeleverde script van Kaptain gebruiken (omegat.kaptn). U moet eerst Kaptain installeren om dit script te kunnen gebruiken. U kunt dan het Kaptain opstartscript starten met:

    Alt+F2

en dan:

    omegat.kaptn



Starten van OmegaT vanaf de opdrachtregel (alle systemen)
=========================================================

De opdracht om OmegaT te starten is:

    cd <map waar het bestand OmegaT.jar is opgeslagen>

    <naam en pad van het uitvoerbare Java-bestand> -jar OmegaT.jar

(Het uitvoerbare Java-bestand is het bestand java op Linux en java.exe op Windows.  Indien Java is geïnstalleerd op systeemniveau en in het pad voor de opdracht staat,
hoeft niet het volledige pad te worden ingevoerd.)



Bijdragen
=========

OmegaT is van origine het werk van Keith Godfrey.

Jean-Christophe Helary is de OmegaT projectmanager.

Huidige team:
(alfabetische volgorde)

- Marco Cevoli (Telegram community manager)
- Jean-Christophe Helary (Twitter community manager)
- Kos Ivantsov (localisation manager, user group owner)
- Concepción Martin (Facebook community manager)
- Hiroshi Miura (lead developer and product integration manager)
- Briac Pilpré (webmaster)
- Philippe Tourigny (documentation manager)
- Lucie Vecerova (Facebook community manager)

Bijdragen aan de code zijn gedocumenteerd in /docs/contributors.txt.

Eerdere bijdragen van:
(alfabetische volgorde)

- Anthony Baldwin (localisatie-manager)
- Vincent Bidaux (documentatie-manager)
- Didier Briel (projectmanager)
- Alex Buloichik (hoofdontwikkelaar)
- Sabine Cretella
- Dmitri Gabinski
- Maxym Mykhalchuk (hoofdontwikkelaar)
- Samuel Murray
- Henry Pijffers (uitgave-manager)
- Marc Prior (projectcoördinator, webmaster)
- Vito Smolej (documentatie-manager)

en vele, vele andere zeer behulpzame mensen

(Als u denkt dat u een significante bijdrage heeft geleverd aan het OmegaT-project,
maar ziet u uw naam niet op deze lijst, neem dan alstublieft contact met ons op.)



Nuttige links
============

De meest recente informatie over OmegaT is te vinden op:

  https://omegat.org/

Bronnen gebruikersondersteuning:

  https://omegat.org/support

Verzoeken tot verbeteringen (in het Engels) op de website van SourceForge:

  https://sourceforge.net/p/omegat/feature-requests/

Foutrapportages (in het Engels) op de website van SourceForge:

  https://sourceforge.net/p/omegat/bugs/

OmegaT Contribution Guide

  https://omegat.readthedocs.io/en/latest/