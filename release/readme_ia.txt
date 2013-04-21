This translation is the work of Carmelo Serraino, copyright©2013.

==============================================================================
  OmegaT 2.0, File lege me

  1.  Information circa OmegaT
  2.  Quod es OmegaT?
  3.  Installar OmegaT
  4.  Contribuentes a OmegaT
  5.  Esque OmegaT ha te importunate? Esque tu necessita adjuta?
  6.  Detalios del edition

==============================================================================
  1.  Information circa OmegaT


On pote trovar le plus actual informationes circa OmegaT a
      http://www.omegat.org/

User support, at the Yahoo user group (multilingual), where the archives are
searchable without subscription:
     http://groups.yahoo.com/group/OmegaT/

Demandas pro augmentationes (in anglese), in le sito SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Reportos de defecto (in anglese), in le sito SourceForge:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  Quod es OmegaT?

OmegaT es un instrumento pro traduction adjutate ab computator (CAT). It is free, that is you 
don't have to pay anything to be able to use it, even for professional use, 
and you are free to modify it and/or re-distribute it as long as you respect 
the user license.

Le principal characteristicas de OmegaT's es:
  - capacitate de execution sur qualcunque systema operative que supporta Java
  - empleo de qualcunque file TMX valide como referentia de traduction
  - flexibile segmentation del phrases (usante un methodo SRX-simile)
  - recercas in le projecto e in le memorias de traduction de referentia
  - recercas de files in formatos supportate in qualcunque classificator 
  - concordantia confuse
  - argute maneamento de projectos includente complexe hierarchias de classificatores
  - supporto pro le glossarios (controlos del terminologia) 
  - supporto al volo pro le correctores orthographic OpenSource
  - supporto pro dictionarios StarDict
  - supporto pro le servicios de traduction a machina  de Google Translate
  - documentation e parve instruction clar e comprehensive 
  - localisation in multe linguas.

OmegaT supporta le formatos de file sequente:

- formato de file in texto plan

  - texto ASCII (.txt, etc.)
  - texto codificate (*.UTF8)
  - pacchettos  ressource Java (*.properties)
  - files PO (*.po)
  - files INI (key=value) (*.ini)
  - files DTD (*.DTD)
  - files DocuWiki (*.txt)
  - files titulo SubRip (*.srt)
  - Magento CE Locale CSV (*.csv)

- formato del file in texto etiquettate

  - OpenOffice.org / OpenDocument (*.odt, *.ott, *.ods, *.ots, *.odp, *.otp)
  - Microsoft Open XML (*.docx, *.xlsx, *.pptx)
  - (X)HTML (*.html, *.xhtml,*.xht)
  - HTML Help Compiler (*.hhc, *.hhk)
  - DocBook (*.xml)
  - Monolingual XLIFF (*.xlf, *.xliff, *.sdlxliff)
  - QuarkXPress CopyFlowGold (*.tag, *.xtg)
  - Files ResX (*.resx)
  - Ressources Android (*.xml)
  - LaTex (*.tex, *.latex)
  - Files Help (*.xml) e Manual (*.hmxp)
  - Typo3 LocManager (*.xml)
  - WiX localisation (*.wxl)
  - Iceni Infix (*.xml)
  - exportation Flash XML (*.xml)
  - Wordfast TXML (*.txml)
  - Camtasia pro Windows (*.camproj)
  - Visio (*.vxd)

OmegaT pote esser personalisate pro supportar anque altere formatos de file.

OmegaT will automatically parse even the most complex source folder
hierarchies, to access all the supported files, and produce a target folder
with exactly the same structure, including copies of any non-supported files.

For a quick-start tutorial, launch OmegaT and read the displayed Instant Start 
Tutorial.

The user manual is in the package you just downloaded, you can access it from
the [Help] menu after starting OmegaT.

==============================================================================
 3. Installar OmegaT

3.1 General
In order to run, OmegaT requires the Java Runtime Environment (JRE) version 
1.5 or higher to be installed on your system. OmegaT packages which include
the Java Runtime Environment are now available to save users the trouble of 
selecting, obtaining and installing it. 

If you have already Java, one way to install the current version of 
OmegaT is to use Java Web Start. 
Pro ce fin discarga le sequente file e pois lo exeque:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

It will install the correct environment for your computer and the application 
itself on the first run. Le ulterior appellos non habe necessitate de connexion a Interrete.

During the installation, depending on your operating system, you may receive 
several security warnings. Le certificato es firmate per "Didier Briel". 
The permissions you give to this version (which might be mentioned as an 
"unrestricted access to the computer") are identical  to permissions you give 
to the local version, as installed by a procedure, described later: they allow 
an access to the hard drive of the computer. Subsequent clicks on  OmegaT.jnlp 
will check for any upgrades, if you are online, install them if there are any, 
and then start OmegaT. 

The alternative ways and means of dowloading and installing OmegaT are
shown below. 

Windows and Linux users: if you are confident that your system already has a 
suitable version of the JRE installed, you can install the version of OmegaT 
without the JRE (this is indicated in the name of the version,"Without_JRE"). 
If you are in any doubt, we recommend that you use the version supplied with 
JRE. This is safe, since even if the JRE is already installed on your system, 
this version will not interfere with it.

Linux users: 
OmegaT will run on the open-implementation source Java 
packaged with many Linux distributions (for example, Ubuntu), but you may
experience bugs, display problems or missing features. We therefore recommend
that you download and install either the Oracle Java Runtime Environment (JRE) 
or the OmegaT package bundled with JRE (the .tar.bz2) bundle marked 
"Linux"). If you install a version of Java at system level, you must either 
ensure that it is in your launch path, or call it explicitly when launching 
OmegaT. If you are not very familiar with Linux, we therefore recommend 
that you install an OmegaT version with JRE included. This is safe, 
since this "local" JRE will not interfere with any other JRE installed 
on your system.

Mac users: 
The JRE is already installed on Mac OS X before Mac OS X 10.7 
(Lion). Lion users will be prompted by the system when they first launch 
an application that requires Java and the system will eventually 
automatically download and install it.

Linux on PowerPC systems: 
Users will need to download IBM's JRE, as Sun does 
not provide a JRE for PPC systems. Discarga in iste caso ab:

    http://www.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installation
* Windows users: 
Simply launch the installation program. If you wish, the 
installation program can create shortcuts to launch OmegaT.

* Linux users:
Place the archive in any suitable folder and unpack it; OmegaT is then 
ready to be launched. You can however obtain a neater and more user-friendly 
installation by using the installation script (linux-install.sh). To use 
this script, open a terminal window (console), change folder to the folder 
containing OmegaT.jar and the linux-install.sh script, and execute the 
script with ./linux-install.sh.

* Mac users:
Copy the OmegaT.zip archive to a suitable location and unpack it there 
to obtain a folder that contains an HTML documentation index file and 
OmegaT.app, the application file.

* Others (e.g., Solaris, FreeBSD: 
To install OmegaT, simply create a suitable folder for OmegaT. Copy the 
OmegaT zip or tar.bz2 archive to this folder and unpack it there.

3.3 Launching OmegaT
Launch OmegaT as follows.

* Windows users: 
If, during installation, you have created a shortcut on the desktop, 
double-click on that shortcut. Alternatively, double-click on the file 
OmegaT.exe. If you can see the file OmegaT but not OmegaT.exe in your 
File Manager (Windows Explorer), change the settings so that file 
extensions are displayed.

* Linux users:
If you used the installation script supplied, you should be able to launch OmegaT with:
Alt+F2
and then:
omegat

* Mac users:
Double-click on the file OmegaT.app.

* From your file manager (all systems):
Double-click on the file OmegaT.jar. This will work only if the .jar
file type is associated with Java on your system.

* From the command line (all systems): 
The command to launch OmegaT is:

cd <classificator ubi le file OmegaT.jar es localisate>

<nomine e via del file executabile Java> -jar OmegaT.jar

(Le file executabile Java es le file java sur Linux e java.exe sur Windows.
If Java is installed at system level and is in the command path, the full 
path need not be entered.)

Adapta tu experientia de lancha de OmegaT:

* Windows users: 
The install program can create shortcuts for you in the start 
menu, on the desktop and in the quick launch area. You can also manually drag 
the file OmegaT.exe to the start menu, the desktop or the quick launch area
to link it from there.

* Linux users:
For a more user-friendly way of launching OmegaT, you can use the Kaptain 
script provided (omegat.kaptn). To use this script you must first install 
Kaptain. You can then launch the Kaptain launch script with
Alt+F2
omegat.kaptn

For more information on the Kaptain script and on adding menu items and 
launch icons on Linux, refer to the OmegaT on Linux HowTo.

Mac users:
Drag OmegaT.app to your dock or to the tool bar of a Finder window to be 
able to launch it  from any location. You can also call it in the 
Spotlight search field.

==============================================================================
 4. Ingagiar se in le projecto OmegaT

Pro participar al disveloppamento de OmegaT, continge le disveloppatores a:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

To translate OmegaT's user interface, user manual or other related documents,
read:
      
      http://www.omegat.org/en/translation-info.html

E abona te al lista del traductores:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

Pro contribuer in altere manieras, subscribe primo al gruppo del usuarios  a:
      http://tech.groups.yahoo.com/group/omegat/

E senti lo que va in le mundo de OmegaT...

  OmegaT es le opera original de Keith Godfrey.
  Marc Prior es le coordinator del projecto OmegaT.

Previous contributors include:
(alphabetical order)

Le codice esseva contribuite ab
  Zoltan Bartko
  Volker Berlin
  Didier Briel (director del disveloppamento)
  Kim Bruning
  Alex Buloichik (conducer disveloppator)
  Sandra Jean Chua
  Thomas Cordonnier
  Martin Fleurke  
  Wildrich Fourie
  Phillip Hall
  Jean-Christophe Helary
  Thomas Huriaux
  Hans-Peter Jacobs
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

altere contributiones per
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (director del localisation)
  Vito Smolej (director del documentation)
  Samuel Murray
  Marc Prior 
  e multe, multe magis maxime adjuvante gente

(If you think you have significantly contributed to the OmegaT Project 
but you don't see your name on the lists, feel free to contact us.)

OmegaT usa le sequente librerias:
  HTMLParser 1.6 by Somik Raha, Derrick Oswald and others (LGPL License)
  MRJ Adapter 1.0.8 by Steve Roy (LGPL License)
  VLDocking Framework 2.1.4 by VLSolutions (CeCILL License)
  Hunspell by László Németh and others (LGPL License)
  JNA by Todd Fast, Timothy Wall and others (LGPL License)
  Swing-Layout 1.0.2 (LGPL License)
  Jmyspell 2.1.4 (LGPL License)
  JAXB (GPLv2 + classpath exception)
  SJXP 1.0.2 (GPL v2)
  SVNKit 1.7.5 (TMate License)
  Sequence Library (Sequence Library License)
  ANTLR 3.4 (ANTLR 3 license)
  SQLJet 1.1.3 (GPL v2)
  JGit (Eclipse Distribution License)
  JSch (JSch License)
  Base64 (public domain)
  Diff (GPL)

==============================================================================
 5.  Esque OmegaT ha te importunate? Esque tu necessita adjuta?

Before reporting a bug, make sure that you have thoroughly checked the
documentation. What you see may instead be a characteristic of OmegaT that
you have just discovered. If you check the OmegaT log and you see words like
"Error", "Warning", "Exception", or "died unexpectedly" then you have probably
discovered a genuine problem (the log.txt is located in the user preferences
folder, see the manual for its location).

The next thing to do is to confirm what you found with other users, to make 
sure this has not already been reported. You can verify the bug report page at
SourceForge too. Only when you are sure you are the first to have found some
reproducible sequence of event that triggered something not supposed to
happen should you file a bug report.

Recorda que cata bon reporto de defecto, necessita justo tres cosas:
  - Passos pro lo reproducer
  - Lo que tu expecta vider
  - Que tu vide in vice

You can add copies of files, portions of the log, screen shots, anything that
you think will help the developers with finding and fixing your bug.

Pro foliar le archivos del gruppo del usuario, vade a:
     http://groups.yahoo.com/group/OmegaT/

Pro foliar le pagina del reporto de defecto e archivar un nove reporto de defecto si necessari, vade a:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

To keep track of what is happening to your bug report you may want to register
as a Source Forge user.

==============================================================================
6.   Detalios del edition

Please see the file 'changes.txt' for detailed information about changes in
this and all previous releases.


==============================================================================
