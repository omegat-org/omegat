Ce document a été traduit par Didier Briel et Jean-Christophe Helary, copyright© 2007-2013.

==============================================================================
  OmegaT 3.0, fichier « Lisez-moi »

  1.  Informations à propos d'OmegaT
  2.  Qu'est ce qu'OmegaT ?
  3.  Installer OmegaT
  4.  Contribuer à OmegaT
  5.  Est-ce qu'OmegaT vous pose problème ? Avez-vous besoin d'aide ?
  6.  Détails de la version

==============================================================================
  1.  Informations à propos d'OmegaT


Les informations les plus récentes au sujet d'OmegaT sont à :
      http://www.omegat.org/

Aide utilisateur sur le groupe Yahoo. Vous pouvez y consulter les archives sans vous inscrire au groupe :
     http://groups.yahoo.com/group/OmegaT/

Demandes d'améliorations (en anglais), sur le site de SourceForge :
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Rapports de bogues (en anglais), sur le site de SourceForge :
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Qu'est ce qu'OmegaT ?

OmegaT est un outil de Traduction assistée par ordinateur (TAO). OmegaT est distribué gratuitement, il n'est donc pas nécessaire de payer de licence d'utilisation même pour un usage professionnel. OmegaT est une application libre, vous avez donc le droit de la modifier et/ou de la redistribuer tant que vous respectez les termes de la licence utilisateur.

Les principales fonctions d'OmegaT sont :
  - capacité à fonctionner sur n'importe quel système d'exploitation compatible avec Java
  - utilisation de n'importe quel type de fichier TMX conforme comme mémoire de traduction de référence
  - système de segmentation de texte flexible (basé sur la norme SRX)
  - recherche de termes dans le projet et dans les mémoires de référence
  - recherche de fichiers dans les formats acceptés dans n'importe quel dossier 
  - correspondances partielles
  - gestion intelligente des projets incluant des structures complexes de dossiers
  - glossaires (vérifications terminologiques) 
  - prise en charge de vérificateurs orthographiques libres en temps réel
  - prise en charge des dictionnaires StarDict
  - prise en charge des services de traduction automatique Google Translate
  - documentation et tutoriel clairs et détaillés
  - localisation dans de nombreuses langues

OmegaT prend en charge directement les formats de fichier suivants :

- Formats de fichier texte brut

  - ASCII (.txt, etc.)
  - Unicode UTF-8 (.utf8)
  - Paquets de ressources Java (.properties)
  - PO (Portable Object) (.po)
  - INI (clé=valeur) (.ini)
  - DTD (.dtd)
  - DocuWiki (.txt)
  - Sous-titres SubRip (.srt)
  - Localisation Magento CE (*.csv)

- Formats de fichier texte balisé

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - HTML Help Compiler (*.hhc, *.hhk)
  - DocBook (*.xml)
  - XLIFF monolingue (.xlf, *.xliff, .sdlxliff)
  - CopyFlowGold pour QuarkXPress (.tag, .xtg)
  - ResX (.resx)
  - Ressource Android (.xml)
  - LaTeX (.tex, .latex)
  - Help & Manual (.xml .hmxp)
  - Typo3 LocManager (.xml)
  - Localisation WiX (*.wxl)
  - Iceni Infix (*.xml)
  - Exportation Flash XML (.xml)
  - TXML Wordfast (*.txml)
  - Camtasia pour Windows (*.camproj)
  - Visio (*.vxd)

Il est également possible de personnaliser OmegaT pour accepter d'autres formats.

OmegaT est capable d'analyser les structures de dossiers les plus complexes pour y retrouver tous les fichiers lisibles. OmegaT recréera la même structure de dossiers pour vos documents traduits et y inclura une copie de tous les fichiers qu'il ne peut pas lire.

Pour commencer à utiliser OmegaT tout de suite, lancez OmegaT et lisez le tutoriel qui s'affiche dans la fenêtre principale : « OmegaT : Tutoriel premiers pas ».

Le manuel utilisateur se trouve dans le paquet que vous avez téléchargé, vous y avez accès à partir du menu [Aide] après avoir lancé OmegaT.

==============================================================================
 3. Installer OmegaT

3.1 Informations générales
Pour fonctionner, OmegaT a besoin d'un environnement d'exécution Java (JRE) de version 1.5 ou supérieure. Les versions d'OmegaT incluant le JRE (Java Runtime Environment) sont maintenant disponibles afin déviter aux utilisateurs d'avoir à le sélectionner, l'obtenir et l'installer. 

Si vous disposez déjà de Java, l'une des façons d'installer la version actuelle d'OmegaT est d'utiliser Java Web Start. 
Pour ce faire, téléchargez le fichier suivant et exécutez-le :

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

Il installera l'environnement approprié pour votre ordinateur et l'application elle-même lors de la première exécution. Il ne sera pas nécessaire d'être en ligne pour les lancements suivants.

Durant l'installation, en fonction de votre système d'exploitation, vous recevrez peut-être plusieurs avertissements de sécurité. Le certificat est auto-signé par « Didier Briel ». 
Les autorisations que vous accordez à cette version (qui peuvent être mentionnées comme étant un « accès sans restriction à l'ordinateur ») sont identiques aux autorisations que vous donnez à la version locale, telle qu'installée par une des procédures décrites plus loin : elles autorisent l'accès au disque dur de l'ordinateur. Les clics suivants sur OmegaT.jnlp vérifieront l'existence de mises à jour, si vous êtes en ligne, les installeront le cas échéant et démarreront ensuite OmegaT. 

Les méthodes et les moyens alternatifs pour télécharger et installer OmegaT sont indiqués ci-dessous. 

Utilisateurs de Windows et Linux : si vous êtes certain qu'une version convenable du JRE est déjà installée, vous pouvez installer une version d'OmegaT sans le JRE (cela est indiqué par le nom de la version, « Without_JRE »). 
Si vous avez un doute quelconque, nous vous recommandons d'utiliser la version fournie avec un JRE. Cela est sûr, puisque même si le JRE est déjà installé, cette version n'interférera pas avec la version système.

Utilisateurs de Linux : 
OmegaT fonctionnera avec les implémentations libres de Java 
incluses dans de nombreuses distributions Linux (par exemple Ubuntu), mais il est possible que vous rencontriez des bogues, des problèmes d'affichage ou des fonctionnalités manquantes. Par conséquent, nous recommandons de télécharger et d'installer soit le JRE (Java Runtime Environment) d'Oracle, soit une version d'OmegaT comportant le JRE (le paquet .tar.bz2) marqué « Linux ». Si vous installez une version de Java au niveau du système, vous devez faire en sorte que Java soit dans le chemin, ou l'appeler de façon explicite lorsque vous lancez OmegaT. Si vous n'êtes pas très habitués à Linux, nous vous recommandons donc d'installer une version d'OmegaT comportant le JRE. Cela est sûr, puisque ce JRE « local » n'interférera avec aucun autre JRE installé dans votre système.

Utilisateurs de Mac :
le JRE est déjà installé sur Mac OX X antérieur à Mac OS X 10.7 (Lion). Pour les utilisateurs de Lion, le système les avertira, lors du premier lancement d'une application nécessitant Java, et le téléchargera et l'installera automatiquement.

Linux sur des architectures Power PC :
les utilisateurs devront télécharger le JRE d'IBM, puisque Sun ne fournit pas de JRE pour les systèmes PPC. Dans ce cas, téléchargez à partir de :

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installation
* Utilisateurs de Windows :
exécutez simplement le programme d'installation. Si vous le souhaitez, le programme d'installation peut créer des raccourcis pour exécuter OmegaT.

* Utilisateurs de Linux :
placez l'archive dans n'importe quel dossier approprié et décompactez-la. OmegaT est alors prêt à être exécuté. Vous pouvez cependant bénéficier d'un installation plus orthodoxe et plus conviviale en utilisant le script d'installation (linux-install.sh). Pour utiliser ce script, ouvrez un fenêtre de terminal (console), passez dans le dossier contenant OmegaT.jar et le script linux-install.sh et exécuter le script avec ./linux-install.sh.

* Utilisateurs de Mac :
Copier l'archive OmegaT.zip à un emplacement approprié, et décompactez-la pour obtenir un dossier contenant un fichier d'index de la documentation et OmegaT.app, le fichier d'application.


* Autres (ex. : Solaris, FreeBSD : 
Pour installer OmegaT, créez simplement un dossier approprié pour OmegaT. Copiez l'archive zip ou tar.bz2 d'OmegaT dans ce dossier, et décompactez-la.

3.3 Lancement d'OmegaT
Exécutez OmegaT de la façon suivante.

* Utilisateurs de Windows : 
Si, durant installation, vous avez créé un raccourci sur le bureau, double-cliquez sur ce raccourci. Autrement, double-cliquez sur le fichier OmegaT.exe. Si vous pouvez voir le fichier OmegaT mais pas le fichier OmegaT.exe dans votre gestionnaire de fichier (Explorateur Windows), modifiez la configuration afin que les extensions soient affichées.

* Utilisateurs de Linux :
si vous avez utilisé le fichier d'installation fourni, vous devriez pouvoir exécuter OmegaT avec :
Alt+F2
suivi de :
omegat

* Utilisateurs de Mac : 
double-cliquez sur le fichier OmegaT.app.

* Depuis votre gestionnaire de fichiers (tous systèmes) :
Double-cliquez sur le fichier OmegaT.jar. Cela ne fonctionnera que le type de fichier .jar est associé à Java dans votre système.

* Depuis la ligne de commande (tous systèmes) : 
la commande pour exécuter OmegaT est ::

cd <dossier dans lequel le fichier OmegaT.jar est situé>

<nom et chemin du fichier exécutable Java> -jar OmegaT.jar

(Le fichier exécutable Java est le fichier java sous Linux et java.exe sous Windows.
Si Java est installé au niveau système et est inclus dans le chemin, il n'est pas nécessaire d'entrer le chemin complet.)

Personnaliser la méthode d'exécution d'OmegaT :

* Utilisateurs de Windows : 
le programme d'installation peut créer des raccourcis pour vous dans le menu démarrer, sur le bureau ou dans la zone de lancement rapide. Vous également pouvez faire glisser le fichier OmegaT.exe dans le menu démarrer, sur le bureau ou dans la zone de lancement rapide afin d'y créer un raccourci.

* Utilisateurs de Linux :
Pour pouvoir exécuter OmegaT de façon plus conviviale, vous pouvez utiliser le script Kaptain fourni (omegat.kaptn). Pour utiliser ce script, vous devez tout d'abord installer Kaptain. Vous pouvez ensuite lancer le script d'exécution Kaptain par
Alt+F2
omegat.kaptn

Pour plus d'informations sur le script Kaptain et sur l'ajout d'entrées de menu et d'icônes d'exécution sous Linux, veuillez vous référer au guide pratique OmegaT sous Linux.

Utilisateurs de Mac :
Faites glisser OmegaT.app sur votre dock ou sur la barre d'outils d'une fenêtre du Finder afin de pouvoir l'exécuter de n'importe quel emplacement. Vous pouvez également l'appeler dans le champ de recherche de Spotlight.

==============================================================================
 4. S'impliquer dans le projet OmegaT

Pour participer au développement d'OmegaT, prenez contact avec les développeurs à :
    http://lists.sourceforge.net/lists/listinfo/omegat-development

Pour traduire l'interface utilisateur, le manuel ou d'autres documents, lisez :
      
      http://www.omegat.org/en/translation-info.html

Et inscrivez vous à la liste des traducteurs (multilingue) :
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Pour d'autres types de contributions, inscrivez-vous d'abord au groupe des utilisateurs :
      http://tech.groups.yahoo.com/group/omegat/

Et voyez comment se passent les choses dans le monde d'OmegaT...

  OmegaT est l'œuvre de Keith Godfrey.
  Le coordinateur du projet OmegaT est Marc Prior.

Les personnes qui ont contribué incluent :
(ordre alphabétique)

Contributions au code :
  Zoltan Bartko
  Volker Berlin
  Didier Briel (responsable du développement)
  Kim Bruning
  Alex Buloichik (développeur principal)
  Sandra Jean Chua
  Thomas Cordonnier
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
  Thomas Huriaux
  Hans-Peter Jacobs
  Kyle Katarn
  Ibai Lakunza Velasco
  Guido Leenders
  Aaron Madlon-Kay
  Fabián Mandelbaum
  John Moran
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Briac Pilpré
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Yu Tang
  Rashid Umarov  
  Antonio Vilei
  Martin Wunderlich
  Michael Zakharov

Autres contributions par :
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (responsable localisations)
  Vito Smolej (responsable documentation)
  Samuel Murray
  Marc Prior 
  ainsi que beaucoup d'autres personnes.

(Si vous pensez avoir contribué à OmegaT de manière significative et si votre nom ne se trouve pas sur cette liste, n'hésitez pas à nous contacter.)

OmegaT utilise les bibliothèques suivantes :
  HTMLParser par Somik Raha, Derrick Oswald, etc. (licence LGPL)
  MRJ Adapter par Steve Roy (licence LGPL)
  VLDocking Framework 2.1.4 par VLSolutions (licence CeCILL)
  Hunspell par László Németh et d'autres personnes (licence LGPL)
  JNA par Todd Fast, Timothy Wall et d'autres personnes (licence LGPL)
  Swing-Layout 1.0.2 (licence LGPL)
  Jmyspell 2.1.4 (licence LGPL)
  SVNKit 1.7.5 (licence TMate)
  Sequence Library (licence Sequence Library)
  ANTLR 3.4 (licence ANTLR 3)
  SQLJet 1.1.3 (GPL v2)
  JGit (Eclipse Distribution License)
  JSch (licence JSch)
  Base64 (domaine public)
  Diff (GPL)
  JSAP (LGPL)
  orion-ssh2-214 (licence Orion SSH for Java)
  lucene-*.jar (licence Apache 2.0)
  Les lemmatiseurs anglais (org.omegat.tokenizer.SnowballEnglishTokenizer et
  org.omegat.tokenizer.LuceneEnglishTokenizer) utilisent des mots non significatifs d'Okapi
(http://okapi.sourceforge.net) (licence LGPL)
  tinysegmenter.jar (licence BSD modifiée)
  commons-*.jar (licence Apache 2.0)
  jWordSplitter (licence Apache 2.0)
  LanguageTool.jar (licence LGPL)
  morfologik-*.jar (licence Morfologik)
  segment-1.3.0.jar (licence Segment)

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

Consultez « changes.txt » pour avoir des informations détaillées sur les modifications incluses dans cette version ainsi que les précédentes.


==============================================================================
