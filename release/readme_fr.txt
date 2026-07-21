Copyright© 2004-2026.
La version française d’OmegaT est le produit du travail des personnes suivantes :
Alexandra Anselmi, Sirine Ben Hadj Khalifa, Vincent Bidaux, Didier Briel, Marie-Louise Defray, Léo Delaune, Laurie Devos, Jean-Christophe Helary, Anand Kandeepan, Brigitte Legrand, Briac Pilpré, Clémentine Rouillard, Philippe Tourigny, Julie Zeisser.


Qu’est-ce qu’OmegaT ?
=====================

OmegaT est un outil de Traduction Assistée par Ordinateur libre, à code source ouvert et multiplateforme, avec recherche de correspondances, mémoire de traduction, recherche par mots-clés, glossaires et réutilisation des traductions dans des projets modifiés.



Informations sur la licence
===========================

OmegaT is available under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the License or
(at your option) any later version. Vous pouvez trouver le texte de la licence dans
/docs/OmegaT-license.txt.

OmegaT uses a number of libraries. La licence de chaque bibliothèque est mentionnée
dans /lib/licenses/Licenses.txt.



Prérequis pour l’installation d’OmegaT
======================================

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

Windows :
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



Installation d’OmegaT (Windows)
===============================

Exécutez simplement le programme d’installation.



Installation d’OmegaT (Mac)
===========================

Open the OmegaT .dmg package and drag the OmegaT application to the
Applications folder (or another location of your choice).



Installation d’OmegaT (Linux)
=============================

Using the deb or rpm package
----------------------------

Install the package for your architecture (amd64 or aarch64) using your
distribution's package manager, e.g.:

    sudo dpkg -i omegat_<version>_<arch>.deb

or

    sudo rpm -i omegat-<version>.<arch>.rpm

Using the tar.bz2 archive
-------------------------

Placez l’archive dans n’importe quel dossier approprié et décompactez-la. OmegaT est alors prêt à être exécuté.

You can, however, get a neater and more user-friendly installation by using the
installation script (linux-install.sh). Pour utiliser ce script, ouvrez un fenêtre de terminal (console), passez dans le dossier contenant OmegaT.jar et le script linux-install.sh et exécuter le script avec ./linux-install.sh.



Installing OmegaT (Solaris, FreeBSD, etc.)
==========================================

Placez l’archive dans n’importe quel dossier approprié et décompactez-la. OmegaT est alors prêt à être exécuté.



Exécution d’OmegaT (Windows)
============================

Si, durant l’installation, vous avez créé un raccourci sur le bureau, double-cliquez sur ce raccourci.

The installer can create shortcuts for you in the start menu, on the desktop, and
in the quick launch area. You can also manually drag the file OmegaT.exe to the
start menu, the desktop, or the quick launch area to link it from there.

Si vous pouvez voir le fichier OmegaT mais pas le fichier OmegaT.exe dans votre gestionnaire de fichiers (Explorateur Windows), modifiez la configuration afin que les extensions soient affichées.



Exécution d’OmegaT (Mac)
========================

Double-cliquez sur l’application OmegaT.

Vous pouvez faire glisser OmegaT.app sur votre dock ou sur la barre d’outils d’une fenêtre du Finder afin de pouvoir l’exécuter de n’importe quel emplacement. Vous pouvez également l’exécuter depuis le champ de recherche de Spotlight.



Exécution d’OmegaT (Linux)
==========================

If you installed OmegaT using the deb or rpm package, launch it from your
desktop environment's application menu, or run:

    omegat

from a terminal.

Si vous avez utilisé le script linux-install.sh, vous devriez pouvoir exécuter OmegaT avec :

    Alt+F2

suivi de :

    omegat

Pour pouvoir exécuter OmegaT de façon plus conviviale, vous pouvez utiliser le script Kaptain fourni (omegat.kaptn). Pour l’utiliser, vous devez tout d’abord installer Kaptain. Vous pouvez ensuite lancer le script d’exécution Kaptain avec :

    Alt+F2

suivi de :

    omegat.kaptn



Exécution d’OmegaT depuis la ligne de commande (tous systèmes)
==============================================================

La commande permettant d’exécuter OmegaT est :

    cd <dossier dans lequel le fichier OmegaT.jar est situé>

    <nom et chemin du fichier exécutable Java> -jar OmegaT.jar

(Le fichier exécutable Java est le fichier java sous Linux et java.exe sous Windows.  Si Java est installé au niveau système et est inclus dans le chemin, il n’est pas nécessaire d’entrer le chemin complet.)



Contributeurs
=============

OmegaT est l’œuvre initiale de Keith Godfrey.

Jean-Christophe Helary est le responsable du projet OmegaT.

Équipe actuelle :
(ordre alphabétique)

- Marco Cevoli (Telegram community manager)
- Jean-Christophe Helary (Twitter community manager)
- Kos Ivantsov (localisation manager, user group owner)
- Concepción Martin (Facebook community manager)
- Hiroshi Miura (lead developer and product integration manager)
- Briac Pilpré (webmaster)
- Philippe Tourigny (documentation manager)
- Lucie Vecerova (Facebook community manager)

Les contributions au code sont documentées dans /docs/contributors.txt.

Les personnes qui ont contribué incluent :
(ordre alphabétique)

- Anthony Baldwin (responsable de la localisation)
- Vincent Bidaux (responsable de la documentation)
- Didier Briel (responsable du projet)
- Alex Buloichik (développeur principal)
- Sabine Cretella
- Dmitri Gabinski
- Aaron Madlon-Kay (responsable du projet)
- Maxym Mykhalchuk (développeur principal)
- Samuel Murray
- Henry Pijffers (responsable des versions)
- Marc Prior (coordinateur du projet, webmestre)
- Vito Smolej (responsable de la documentation)

ainsi que beaucoup d’autres personnes.

(Si vous pensez avoir contribué à OmegaT de manière significative et si votre nom ne se trouve pas sur cette liste, n’hésitez pas à nous contacter.)



Liens utiles
============

Les informations les plus récentes au sujet d’OmegaT sont à :

  https://omegat.org/

Ressources diverses :

  https://omegat.org/support

Demandes d’améliorations (en anglais), sur le site de SourceForge :

  https://sourceforge.net/p/omegat/feature-requests/

Rapports de bogues (en anglais), sur le site de SourceForge :

  https://sourceforge.net/p/omegat/bugs/

Guide des contributions à OmegaT

  https://omegat.readthedocs.io/en/latest/