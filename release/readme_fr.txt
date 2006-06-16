==============================================================================
  OmegaT 1.6.0 RC10 Fichier « Lisez-moi »

  1.  À propos de OmegaT
  2.  Qu'est-ce que OmegaT ?
  3.  Au sujet de Java & de OmegaT
  4.  Participation à OmegaT
  5.  Est-ce que OmegaT vous pose des problèmes ? Avez-vous besoin d'aide ?
  6.  Informations sur cette version

==============================================================================
  1.  À propos de OmegaT

Les informations les plus à jour au sujet de OmegaT sont disponibles à :
      http://www.omegat.org/omegat/omegat.html
(anglais et portugais)

Les pages suivantes aussi peuvent vous fournir des informations :

Aide utilisateur sur le groupe Yahoo:
     http://groups.yahoo.com/group/OmegaT/
(multilingue)
     Vous pouvez faire des recherches dans les archives sans vous abonner.

Demandes d'améliorations, site de développement sur SourceForge :
     http://sourceforge.net/tracker/?group_id=68187&atid=520350
(anglais)

Rapports de bogues, site de développement sur SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347
(anglais)

==============================================================================
  2.  Qu'est-ce que OmegaT ?

OmegaT est un outil d'aide à la traduction. OmegaT est distribué gratuitement, il n'est donc pas nécessaire de payer de licence d'utilisation même pour un usage professionnel. OmegaT est une application libre, vous avez donc le droit de la modifier et/ou de la redistribuer tant que vous respectez les termes de la licence utilisateur.

Les principales fonctions de OmegaT sont
  - la capacité à fonctionner sur tous les systèmes acceptant Java
  - la capacité à utiliser n'importe quelle traduction de référence sous format TMX
  - l'utilisation d'un système flexible de segmentation en phrases
  - la capacité à faire des recherches dans la mémoire du projet et dans les mémoires de référence
  - mais aussi dans n'importe quel répertoire contenant des fichiers lisibles par OmegaT
  - un système de correspondances approximatives
  - la capacité à traiter des projets qui incluent des hiérarchies complexes de répertoires
  - un système de vérification de glossaire
  - une documentation facile à comprendre et un tutoriel efficace
  - sa localisation dans un grand nombre de langues

OmegaT peut vous aider à traduire des fichiers aux formats suivants : OpenDocument, Microsoft Office (à l'aide de OpenOffice.org utilisé comme filtre de conversion), OpenOffice.org ou StarOffice, (X)HTML, fichiers de localisation Java, fichiers textes, DocBook et PO.

OmegaT est capable d'analyser les structures de répertoires les plus complexes pour y retrouver tous les fichiers lisibles. OmegaT recréera la même structure de répertoires pour vos documents traduits et y inclura une copie de tous les fichiers qu'il n'a pas pu lire.

Pour commencer à utiliser OmegaT tout de suite, lancez OmegaT et lisez le tutoriel qui s'affiche dans la fenêtre principale : « OmegaT : Pour commencer tout de suite ».

Le manuel utilisateur se trouve dans le paquet que vous avez téléchargé, vous y avez accès à partir du menu [Aide] après avoir lancé OmegaT.

==============================================================================
 3. Au sujet de Java & de OmegaT

Pour fonctionner, OmegaT a besoin d'un environnement d'exécution Java (JRE) de version 1.4 ou supérieure. Le JRE est disponible à :
      http://java.com

Les utilisateurs de Windows et de Linux auront besoin d'installer le JRE si ce n'est pas déjà fait.
Les utilisateurs de MacOSX ont déjà un JRE installé sur leur machine par défaut.

Si le JRE est installé sur votre ordinateur il vous est possible de lancer OmegaT en double-cliquant sur le fichier « OmegaT.jar ».

Après avoir installé Java il peut vous être nécessaire de modifier la variable « PATH » de votre système pour qu'elle inclue le répertoire où l'application « java » a été installée.

==============================================================================
 4. Participation à OmegaT

Pour participer au développement d'OmegaT vous pouvez rentrer en contact avec les développeurs sur la liste :
    http://lists.sourceforge.net/lists/listinfo/omegat-development
(anglais)

Pour traduire l'interface utilisateur, le manuel ou d'autres documents, lisez :
      http://www.omegat.org/omegat/translation-info.html
(anglais)

Pour s'abonner à la liste des traducteurs :
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n
(multilingue)

Pour d'autres types de contributions, inscrivez vous d'abord au groupe utilisateur à :
      http://groups.yahoo.com/group/OmegaT/
(multilingue)

Et voyez ce qui se passe dans le monde d'OmegaT...

  OmegaT est l'œuvre de Keith Godfrey.
  Marc Prior est le coordinateur du projet OmegaT.

Les personnes qui ont contribué incluent :
(ordre alphabétique)

Contributions au code :
  Sacha Chua
  Kim Bruning
  Maxym Mykhalchuk (responsable du développement)
  Henry Pijffers
  Benjamin Siband

Contributions aux localisations :
  Didier Briel (français)
  Alessandro Cattelan (italien)
  Sabine Cretella (allemand)
  Cesar Escribano Esteban (espagnol)
  Dmitri Gabinski (belorusse, espéranto, russe)
  Jean-Christophe Helary (français)
  Juan Salcines (espagnol)
  Pablo Roca Santiagio (espagnol)
  Martin Wunderlich (allemand)
  Hisashi Yanagida (japonais)
  Yoshi Nakayama (japonais)
  Takayuki Hayashi (japonais)
  Kunihiko Yokota (japonais)
  Yutaka Kachi (japonais)

Autres contributions :
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (responsable de la documentation)
  Samuel Murray
  Marc Prior (responsable des localisations)
  ainsi que beaucoup d'autres personnes.

==============================================================================
 5.  Est-ce que OmegaT vous pose des problèmes ? Avez-vous besoin d'aide ?

Avant de faire un rapport de bogue, prenez la précaution de bien lire le manuel utilisateur. Ce que vous considérez comme un problème peut en fait être une fonction de OmegaT. Si les mots suivants se trouvent dans le journal : « Error », « Warning », « Exception », ou bien « died unexpectedly », vous avez probablement trouvé un problème. Le journal est le fichier log.txt situé dans le répertoire de préférences utilisateur (consulter le manuel pour déterminer son l'emplacement).

L'étape suivante est d'avoir votre découverte confirmée par d'autres utilisateurs pour s'assurer qu'un rapport n'a pas déjà été rempli. Vous pouvez aussi consulter la page des bogues sur SourceForge. Une fois que vous êtes sûr d'avoir découvert une séquence reproductible d'événements qui aboutit à un résultat non souhaitable, il est possible de remplir un rapport de bogue.

Un rapport de bogue doit inclure les 3 informations suivantes :
  - les étapes pour reproduire le bogue,
  - ce que vous pensiez obtenir,
  - ce que vous avez obtenu à la place.
  
Il est possible d'ajouter des copies de fichiers, des portions du journal, des copies d'écrans à votre rapport, tout ce qui vous semble qui aidera les développeurs à résoudre votre problème.

Les archives du groupe utilisateur sont à :
     http://groups.yahoo.com/group/OmegaT/
(multilingue)

Pour consulter la page de rapports de bogues et remplir un rapport, aller à :
     http://sourceforge.net/tracker/?group_id=68187&atid=520347
(anglais)

Il sera nécessaire de procéder à un enregistrement en tant qu'utilisateur de SourceForge si vous souhaitez recevoir des informations sur les suites apportées à votre rapport.

==============================================================================
6.   Informations sur cette version

Consulter « changes.txt » pour avoir des informations détaillées au sujet des modifications inclues dans cette version ainsi que les précédentes.

Nouvelle fonctions de l'interface utilisateur (IU), en comparaison avec la série OmegaT 1.0 :
  - interface de recherche ré-écrite avec de nouvelles fonctions
  - interface principale améliorée
  - possibilité de choisir une police d'affichage
  - possibilité de localiser OmegaT intégralement
  - capacité à se déplacer vers le segment non traduit suivant
  - personnalisation du comportement des filtres de fichiers
  - personnalisation des règles de segmentation
  [db]- la zone des correspondances et la zone des glossaires sont séparées par une section mobile

Formats de fichiers lisibles :
  - texte pur
  - HTML et XHTML
  - OpenDocument / OpenOffice.org
  - ensemble de ressources Java (.properties)

Modifications de fond :
  - segmentation par phrase flexible
  - capacité à créer des filtres de fichiers sous forme de plug-ins
  - réorganisation du code et ajout de commentaires
  - installateur pour Windows
  - traitement des attributs HTML comme des séquences à traduire
  - compatibilité TMX 1.1-1.4b niveau 1

==============================================================================

