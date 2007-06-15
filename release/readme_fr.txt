Ce document a été traduit par Didier Briel et Jean-Christophe Helary, copyright© 2007.

==============================================================================
  OmegaT 1.6.2 Fichier « Lisez-moi »

  1.  Informations à propos d'OmegaT
  2.  Qu'est ce qu'OmegaT ?
  3.  Notes à propos de Java et d'OmegaT
  4.  Contribuer à OmegaT
  5.  Est-ce qu'OmegaT vous pose problème ? Avez-vous besoin d'aide ?
  6.  Détails de la version

==============================================================================
  1.  Informations à propos d'OmegaT


Les informations les plus récentes au sujet d'OmegaT sont (en anglais, hollandais, portugais, slovaque) à :
      http://www.omegat.org/omegat/omegat.html

Aide utilisateur sur le groupe Yahoo. Vous pouvez y consulter les archives sans vous inscrire au groupe :
     http://groups.yahoo.com/group/OmegaT/

Demandes d'améliorations (en anglais), sur le site de SourceForge :
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Rapports de bogues (en anglais), sur le site de SourceForge :
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Qu'est-ce qu'OmegaT ?

OmegaT est un outil d'aide à la traduction. OmegaT est distribué gratuitement, il n'est donc pas nécessaire de payer de licence d'utilisation même pour un usage professionnel. OmegaT est une application libre, vous avez donc le droit de la modifier et/ou de la redistribuer tant que vous respectez les termes de la licence utilisateur.

Les principales fonctions d'OmegaT sont :
  - capacité à fonctionner sur n'importe quel système d'exploitation compatible avec Java
  - utilisation de n'importe quel type de fichier TMX conforme comme mémoire de traduction de référence
  - système de segmentation de texte flexible (basé sur la norme SRX)
  - recherche de termes dans le projet et dans les mémoires de référence
  - recherche de termes dans n'importe quel répertoire incluant des fichiers lisibles par OmegaT
  - correspondances automatiques
  - gestion intelligente des projets incluant des structures complexes de répertoires
  - glossaires (vérifications terminologiques)
  - documentation facile à lire et tutoriel simple
  - localisation dans de nombreuses langues

OmegaT peut vous aider à traduire des fichiers aux formats suivants : OpenDocument, Microsoft Office (à l'aide d'OpenOffice.org utilisé comme filtre de conversion, ou après conversion en HTML), OpenOffice.org ou StarOffice, (X)HTML, fichiers de localisation Java, fichiers textes, DocBook et PO.

OmegaT est capable d'analyser les structures de répertoires les plus complexes pour y retrouver tous les fichiers lisibles. OmegaT recréera la même structure de répertoire pour vos documents traduits et y inclura une copie de tous les fichiers qu'il ne peut pas lire.

Pour commencer à utiliser OmegaT tout de suite, lancez OmegaT et lisez le tutoriel qui s'affiche dans la fenêtre principale : « OmegaT : Tutoriel premiers pas ».

Le manuel utilisateur se trouve dans le paquet que vous avez téléchargé, vous y avez accès à partir du menu [Aide] après avoir lancé OmegaT.

==============================================================================
 3. Notes à propos de Java et d'OmegaT

Pour fonctionner, OmegaT a besoin d'un environnement d'exécution Java (JRE) de version 1.4 ou supérieure. Celui-ci est disponible à :
    http://java.com

Les utilisateurs sur Windows ou Linux peuvent avoir besoin de l'installer si ce n'est pas déjà fait.
Le projet OmegaT propose aussi des versions qui incluent Java. Les utilisateurs de Mac OS X ont déjà un JRE installé par défaut sur leur machine.

Si le JRE est installé sur votre ordinateur, il vous est possible de lancer OmegaT en double-cliquant sur le fichier « OmegaT.jar ».

Après avoir installé Java il peut vous être nécessaire de modifier la variable « PATH » de votre système pour qu'elle inclue le répertoire où l'application « java » a été installée.

Les utilisateurs de Linux doivent savoir qu'OmegaT ne fonctionnera pas avec les implémentations libres de Java disponibles dans de nombres distributions. Celles-ci sont obsolètes ou incomplètes. Téléchargez un JRE de Sun à partir du lien ci-dessus, ou téléchargez et installez OmegaT fourni avec son JRE (le paquet .tar.gz marqué « Linux »).

Sur des architectures Power PC, Linux demandera le JRE d'IBM, puisque Sun ne fournit pas de JRE pour les systèmes PPC. À télécharger à partir de :
    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html

==============================================================================
 4. Contribuer à OmegaT

Pour contribuer au développement d'OmegaT, prenez contact avec les développeurs à :
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Pour traduire l'interface utilisateur, le manuel ou d'autres documents, lisez :
      http://www.omegat.org/omegat/omegat_en/translation-info.html

Et inscrivez vous à la liste des traducteurs (multilingue) :
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Pour d'autres types de contributions, inscrivez vous d'abord au groupe des utilisateurs :
      http://tech.groups.yahoo.com/group/omegat/

Et voyez comment se passent les choses dans le monde d'OmegaT...

  OmegaT est l'œuvre de Keith Godfrey.
  Le coordinateur du projet OmegaT est Marc Prior.

Les personnes qui ont contribué incluent :
(ordre alphabétique)

Contributions au code :
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk (responsable du développement)
  Henry Pijffers (contrôle de version)
  Benjamin Siband
  Martin Wunderlich

Contributions aux traductions :
  Roberto Argus (portugais-Brésil)
  Alessandro Cattelan (italien)
  Sabine Cretella (allemand)
  Suzanne Bolduc (espéranto)
  Didier Briel (français)
  Frederik De Vos (néerlandais)
  Cesar Escribano Esteban (espagnol)
  Dmitri Gabinski (biélorusse, espéranto et russe)
  Takayuki Hayashi (japonais)
  Jean-Christophe Helary (français et japonais)
  Yutaka Kachi (japonais)
  Elina Lagoudaki (grec)
  Martin Lukáč (slovaque)
  Samuel Murray (afrikaans)
  Yoshi Nakayama (japonais)
  David Olveira (portugais)
  Ronaldo Radunz (portugais-Brésil)
  Thelma L. Sabim (portugais-Brésil)
  Juan Salcines (espagnol)
  Pablo Roca Santiagio (espagnol)
  Karsten Voss (polonais)
  Gerard van der Weyde (néerlandais)
  Martin Wunderlich (allemand)
  Hisashi Yanagida (japonais)
  Kunihiko Yokota (japonais)
  Erhan Yükselci (turc)
  Dragomir Kovacevic (serbo-croate)
  Claudio Nasso (italien)
  Ahmet Murati (albanais)
  Sonja Tomaskovic (allemand)

Autres contributions par :
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (actuel responsable documentation)
  Samuel Murray
  Marc Prior (actuel responsable localisations)
  ainsi que beaucoup d'autres personnes.

(Si vous pensez avoir contribué à OmegaT de manière significative et si votre nom ne se trouve pas sur cette liste, n'hésitez pas à nous contacter.)

OmegaT utilise les bibliothèques suivantes :
  HTMLParser par Somik Raha, Derrick Oswald, etc. (Licence LGPL).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter par Steve Roy (licence LGPL).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.0 par VLSolutions (licence CeCILL).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  Est-ce qu'OmegaT vous pose problème ? Avez-vous besoin d'aide ?

Avant de faire un rapport de bogue, prenez la précaution de bien lire le manuel utilisateur. Ce que vous considérez comme un problème peut en fait être une fonction d'OmegaT. Si les mots suivants se trouvent dans le journal : « Erreur », « Avertissement », ou bien « a été avortée », vous avez probablement trouvé un problème. Le journal est le fichier log.txt situé dans le répertoire des préférences utilisateur (consulter le manuel pour déterminer son l'emplacement).

L'étape suivante est d'avoir votre découverte confirmée par d'autres utilisateurs pour s'assurer qu'un rapport n'a pas déjà été rempli. Vous pouvez aussi consulter la page des bogues sur SourceForge. Une fois que vous êtes sûr d'avoir découvert une séquence reproductible d'événements qui aboutit à un résultat non souhaitable, il est possible de remplir un rapport de bogue.

Un bon rapport de bogue comporte exactement 3 parties.
  - la séquence à reproduire,
  - le résultat que vous attendiez et
  - ce que vous avez vu à la place.

Il est possible d'ajouter des copies de fichiers, des portions du journal, des copies d'écran à votre rapport, tout ce qui vous semble qui aidera les développeurs à résoudre votre problème.

Les archives du groupe utilisateur sont à :
     http://groups.yahoo.com/group/OmegaT/

Pour consulter la page des rapports de bogue et pour remplir un nouveau rapport rendez-vous à :
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Il sera nécessaire de procéder à un enregistrement en tant qu'utilisateur de SourceForge si vous souhaitez recevoir des informations sur les suites données à votre rapport.

==============================================================================
6.   Détails de la version

5.  Est-ce qu'OmegaT vous pose problème ? Avez-vous besoin d'aide ?

Avant de faire un rapport de bogue, prenez la précaution de bien lire le manuel utilisateur. Ce que vous considérez comme un problème peut en fait être une fonction d'OmegaT. Si les mots suivants se trouvent dans le journal : « Erreur », « Avertissement », ou bien « a été avortée », vous avez probablement trouvé un problème. Le journal est le fichier log.txt situé dans le répertoire des préférences utilisateur (consulter le manuel pour déterminer son l'emplacement).

L'étape suivante est d'avoir votre découverte confirmée par d'autres utilisateurs pour s'assurer qu'un rapport n'a pas déjà été rempli. Vous pouvez aussi consulter la page des bogues sur SourceForge. Une fois que vous êtes sûr d'avoir découvert une séquence reproductible d'événements qui aboutit à un résultat non souhaitable, il est possible de remplir un rapport de bogue.

Un bon rapport de bogue comporte exactement 3 parties.
  - la séquence à reproduire,
  - le résultat que vous attendiez et
  - ce que vous avez vu à la place.

Il est possible d'ajouter des copies de fichiers, des portions du journal, des copies d'écran à votre rapport, tout ce qui vous semble qui aidera les développeurs à résoudre votre problème.

Les archives du groupe utilisateur sont à :
     http://tech.groups.yahoo.com/group/omegat/

Pour consulter la page des rapports de bogue et pour remplir un nouveau rapport rendez-vous à :
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

Il sera nécessaire de procéder à un enregistrement en tant qu'utilisateur de SourceForge si vous souhaitez recevoir des informations sur les suites données à votre rapport.

==============================================================================
6.   Détails de la version

Consultez « changes.txt » pour avoir des informations détaillées sur les modifications incluses dans cette version ainsi que les précédentes.

Formats de fichiers acceptés:
  - texte pur
  - HTML et XHTML
  - HTML Help Compiler (HCC)
  - OpenDocument / OpenOffice.org
  - ensemble de ressources Java (Bundle.properties)
  - fichiers INI (fichiers dans un encodage quelconque constitués de paires clé=valeur)
  - fichiers PO monolingues (après passage par msgcat)
  - fichiers de documentation format DocBook
  - Fichiers Microsoft Open XML

Modification principales :
  - segmentation flexible par phrase (expressions régulières)
  - capacité à créer des filtres de fichiers sous forme de plug-ins
  - réorganisation du code et ajout de commentaires
  - installateur pour Windows
  - il est possible de traduire les attributs HTML
  - compatibilité TMX 1.1-1.4b niveau 1
  - compatibilité partielle TMX 1.4b niveau 2

Nouvelles fonctions de l'interface utilisateur (comparaison avec la série 1.4) :
  - interface de recherche ré-écrite avec de nouvelles fonctions
  - interface principale améliorée avec des fenêtres ancrables

==============================================================================

