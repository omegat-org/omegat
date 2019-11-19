Ce document a été traduit par Didier Briel et Jean-Christophe Helary, copyright© 2007-2019.


Qu'est-ce qu'OmegaT ?
===============

OmegaT est un outil de Traduction Assistée par Ordinateur libre, à code source ouvert et multiplateforme, avec recherche de correspondances, mémoire de traduction, recherche par mots clefs, glossaires et réutilisation des traductions dans des projets modifiés.



Informations sur la licence
=====================

OmegaT est disponible suivant les termes de la GNU General Public License telle que publiée par la Free Software Foundation, soit la version 3 de la Licence, soit (à votre gré) toute version ultérieure. Vous pouvez trouver le texte de la licence dans
/docs/OmegaT-license.txt.

OmegaT utilise un certain nombre de bibliothèques. La licence de chaque bibliothèque est mentionnée
dans /lib/licenses/Licenses.txt.



Prérequis pour l'installation d'OmegaT
===================================

OmegaT a besoin qu'un environnement d'exécution Java (JRE) de version 1.8 soit installé dans votre système.

Nous recommandons d'utiliser les versions d'OmegaT incluant le JRE pour vous éviter d'avoir à le sélectionner, l'obtenir et l'installer.



Installation d'OmegaT (Windows)
===========================

Exécutez simplement le programme d'installation.



Installation d'OmegaT (Mac)
===========================

Décompactez l'archive .zip d'OmegaT pour obtenir un dossier contenant un fichier de documentation et l'application OmegaT. Déplacez le dossier dans un emplacement approprié comme le dossier Applications.



Installation d'OmegaT (Linux)
===========================

Placez l'archive dans n'importe quel dossier approprié et décompactez-la. OmegaT est alors prêt à être exécuté.

Vous pouvez cependant bénéficier d'une installation plus orthodoxe et plus conviviale en utilisant le script d'installation (linux-install.sh). Pour l'utiliser, ouvrez une fenêtre de terminal (console), passez dans le dossier contenant OmegaT.jar et le script linux-install.sh et exécuter le script avec ./linux-install.sh.



Installation d'OmegaT (Solaris, FreeBSD, ...)
=========================================

Placez l'archive dans n'importe quel dossier approprié et décompactez-la. OmegaT est alors prêt à être exécuté.



Utilisation de Java Web Start pour l'installation d'OmegaT (toutes plates-formes)
===========================================================

Si vous disposez déjà de Java, l'une des façons d'installer OmegaT est d'utiliser Java Web Start.

Pour ce faire, téléchargez le fichier suivant et exécutez-le :

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Il installera l'environnement approprié pour votre ordinateur et l'application elle-même lors de la première exécution. Il ne sera pas nécessaire d'être en ligne pour les lancements suivants.



Exécution d'OmegaT (Windows)
===========================

Si, durant l'installation, vous avez créé un raccourci sur le bureau, double-cliquez sur ce raccourci.

Le programme d'installation peut créer des raccourcis pour vous dans le menu démarrer, sur le bureau ou dans la zone de lancement rapide. Vous également pouvez faire glisser le fichier OmegaT.exe dans le menu démarrer, sur le bureau ou dans la zone de lancement rapide afin d'y créer un raccourci.

Si vous pouvez voir le fichier OmegaT mais pas le fichier OmegaT.exe dans votre gestionnaire de fichier (Explorateur Windows), modifiez la configuration afin que les extensions soient affichées.



Exécution d'OmegaT (Mac)
======================

Double-cliquez sur l'application OmegaT.

Vous pouvez faire glisser OmegaT.app sur votre dock ou sur la barre d'outils d'une fenêtre du Finder afin de pouvoir l'exécuter de n'importe quel emplacement. Vous pouvez également l'exécuter depuis le champ de recherche de Spotlight.



Exécution d'OmegaT (Linux)
========================

Si vous avez utilisé le script linux-install.sh, vous devriez pouvoir exécuter OmegaT avec :

  Alt+F2

suivi de :

  omegat

Pour pouvoir exécuter OmegaT de façon plus conviviale, vous pouvez utiliser le script Kaptain fourni (omegat.kaptn). Pour l'utiliser, vous devez tout d'abord installer Kaptain. Vous pouvez ensuite lancer le script d'exécution Kaptain avec :

  Alt+F2

suivi de :

  omegat.kaptn



Exécution d'OmegaT depuis la ligne de commande (tous systèmes)
====================================================

La commande permettant d'exécuter OmegaT est :

cd <dossier dans lequel le fichier OmegaT.jar est situé>

<nom et chemin du fichier exécutable Java> -jar OmegaT.jar

(Le fichier exécutable Java est le fichier java sous Linux et java.exe sous Windows.
Si Java est installé au niveau système et est inclus dans le chemin, il n'est pas nécessaire d'entrer le chemin complet.)



Contributeurs
============

OmegaT est l'œuvre initiale de Keith Godfrey.

Aaron Madlon-Kay est le responsable du projet OmegaT.

Équipe actuelle :
(ordre alphabétique)

  Vincent Bidaux (responsable documentation)
  Marco Cevoli (gestionnaire de la communauté Telegram)
  Jean-Christophe Helary (gestionnaire de la communauté Twitter) 
  Kos Ivantsof (responsable de la localisation)
  Concepción Martin (gestionnaire de communauté Facebook)
  Briac Pilpré (webmestre)
  Lucie Vecerova (gestionnaire de communauté Facebook)

Les contributions au code sont documentées dans /docs/contributors.txt.

Les personnes qui ont contribué incluent :
(ordre alphabétique)

  Anthony Baldwin (responsable de la localisation)
  Didier Briel (responsable du projet OmegaT)
  Alex Buloichik (développeur principal)
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (gestionnaire du groupe d'assistance, responsable de la localisation)
  Maxym Mykhalchuk (développeur principal)
  Samuel Murray
  Henry Pijffers (responsable des versions)
  Marc Prior (coordinateur du projet, webmestre)
  Vito Smolej (responsable documentation)
  ainsi que beaucoup d'autres personnes.

(Si vous pensez avoir contribué à OmegaT de manière significative et si votre nom ne se trouve pas sur cette liste, n'hésitez pas à nous contacter.)



Liens utiles
============

Les informations les plus récentes au sujet d'OmegaT sont à :

   http://www.omegat.org/

Aide utilisateur sur le groupe Yahoo. Vous pouvez y consulter les archives sans vous inscrire au groupe :

   https://omegat.org/support

Demandes d'améliorations (en anglais), sur le site de SourceForge :

   https://sourceforge.net/p/omegat/feature-requests/

Rapports de bogues (en anglais), sur le site de SourceForge :

   https://sourceforge.net/p/omegat/bugs/
