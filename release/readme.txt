@@TRANSLATION_NOTICE@@

==============================================================================
  OmegaT 2.0, Read Me file

  1.  Information about OmegaT
  2.  What is OmegaT?
  3.  Installing OmegaT
  4.  Contributions to OmegaT
  5.  Is OmegaT bugging you? Do you need help?
  6.  Release details

==============================================================================
  1.  Information about OmegaT


The most current info about OmegaT can be found at
      http://www.omegat.org/

User support, at the Yahoo user group (multilingual), where the archives are
searchable without subscription:
     http://groups.yahoo.com/group/OmegaT/

Requests for Enhancements (in English), at the SourceForge site:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Bug reports (in English), at the SourceForge site:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  What is OmegaT?

OmegaT is a computer-assisted translation (CAT) tool. It is free, that is you 
don't have to pay anything to be able to use it, even for professional use, 
and you are free to modify it and/or re-distribute it as long as you respect 
the user license.

OmegaT's main features are:
  - ability to run on any operating system supporting Java
  - use of any valid TMX file as a translation reference
  - flexible sentence segmenting (using an SRX-like method)
  - searches in the project and the reference translation memories
  - searches of files in supported formats in any folder 
  - fuzzy matching
  - smart handling of projects including complex folder hierarchies
  - support for glossaries (terminology checks) 
  - support for OpenSource on-the-fly spell checkers
  - support for StarDict dictionaries
  - support for the Google Translate machine translation services
  - clear and comprehensive documentation and tutorial
  - localization in a number of languages.

OmegaT supports the following file formats out of the box:
  - plain text
  - HTML and XHTML
  - HTML Help Compiler
  - OpenDocument/OpenOffice.org
  - Java resource bundles (.properties)
  - INI files (files with key=value pairs of any encoding)
  - PO files
  - DocBook documentation file format
  - Microsoft OpenXML files
  - Okapi monolingual XLIFF files
  - QuarkXPress CopyFlowGold
  - Subtitle files (SRT)
  - ResX
  - Android resource
  - LaTeX
  - Help & Manual
  - Windows RC resources

OmegaT can be customized to support other file formats as well.

OmegaT will automatically parse even the most complex source folder
hierarchies, to access all the supported files, and produce a target folder
with exactly the same structure, including copies of any non-supported files.

For a quick-start tutorial, launch OmegaT and read the displayed Instant Start 
Tutorial.

The user manual is in the package you just downloaded, you can access it from
the [Help] menu after starting OmegaT.

==============================================================================
 3. Installing OmegaT

3.1 General
In order to run, OmegaT requires the Java Runtime Environment (JRE) version 
1.5 or higher to be installed on your system. OmegaT is now supplied as 
standard with the Java Runtime Environment to save users the trouble of 
selecting, obtaining and installing it. 

If you have already Java, the simplest way to install the current version of 
OmegaT is to use Java Web Start. 
For this purpose download the following file and then execute it:

   http://omegat.sourceforge.net/webstart/OmegaT.jnlp

It will install the correct environment for your computer and the application 
itself on the first run. Later calls do not need to be online.

During the installation, depending on your operating system, you may receive 
several security warnings. The certificate is self-signed by "Didier Briel". 
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
If you are in any doubt, we recommend that you use the "standard" version, 
i.e. with JRE. This is safe, since even if the JRE is already installed on 
your system, this version will not interfere with it.

Linux users: note that OmegaT does not work with the free/open-source Java 
implementations that are packaged with many Linux distributions (for example, 
Ubuntu), as these are either outdated or incomplete. Download and install 
Sun's Java Runtime Environment (JRE) via the link above, or download and and 
install the OmegaT package bundled with JRE (the .tar.gz bundle marked 
"Linux").

Mac users: the JRE is already installed on Mac OS X.

Linux on PowerPC systems: users will need to download IBM's JRE, as Sun does 
not provide a JRE for PPC systems. Download in this case from:

    http://www-128.ibm.com/developerworks/java/jdk/linux/download.html 


3.2 Installation
* Windows users: Simply launch the installation program. If you want, the 
installation program can create shortcuts to launch OmegaT.
* Others: To install OmegaT, simply create a suitable folder for OmegaT 
(e.g., /usr/local/lib on Linux). Copy the OmegaT zip or tar.gz
archive to this folder and unpack it there.

3.3 Launching OmegaT
OmegaT can be launched in a number of ways.

* Windows users: by double-clicking on the file OmegaT.exe. If you can 
see the file OmegaT but not OmegaT.exe in your File Manager (Windows Explorer), 
change the settings so that file extensions are displayed.

* By double-clicking on the file OmegaT.jar. This will work only if the .jar
file type is associated with Java on your system.

* From the command line. The command to launch OmegaT is:

cd <folder where the file OmegaT.jar is located>

<name and path of the Java executable file> -jar OmegaT.jar

(The Java executable file is the file java on Linux and java.exe on Windows.
If Java is installed at system level, the full path need not be entered.)

* Windows users: The install program can create shortcuts for you in the start 
menu, on the desktop and in the quick launch area. You can also manually drag 
the file OmegaT.exe to the start menu, the desktop or the quick launch area
to link it from there.

* Linux KDE users: you can add OmegaT to your menus as follows:

Control Center - Desktop - Panels - Menus - Edit K Menu - File - New Item/New 
Submenu.

Then, after selecting a suitable menu, add a submenu/item with File - New 
Submenu and File - New Item. Enter OmegaT as the name of the new item.

In the "Command" field, use the navigation button to find your OmegaT launch 
script,and select it. 

Click on the icon button (to the right of the Name/Description/Comment fields) 
- Other Icons - Browse, and navigate to the /images subfolder in the OmegaT 
application folder. Select the OmegaT.png icon.

Finally, save the changes with File - Save.

* Linux GNOME users: you can add OmegaT to your panel (the bar at the top of 
the screen) as follows:

Right-click on the panel - Add New Launcher. Enter "OmegaT" in the "Name" 
field; in the "Command" field, use the navigation button to find your OmegaT 
launch script. Select it and confirm with OK.

==============================================================================
 4. Getting involved in the OmegaT project

To participate in the OmegaT development, get in touch with the developers at:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

To translate OmegaT's user interface, user manual or other related documents,
read:
      
      http://www.omegat.org/en/translation-info.html

And subscribe to the translators' list:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

For other kind of contributions, subscribe first to the user group at:
      http://tech.groups.yahoo.com/group/omegat/

And get a feel of what is going on in the OmegaT world...

  OmegaT is the original work of Keith Godfrey.
  Marc Prior is the coordinator of the OmegaT project.

Previous contributors include:
(alphabetical order)

Code has been contributed by
  Zoltan Bartko
  Didier Briel (release manager)
  Kim Bruning
  Alex Buloichik
  Sandra Jean Chua
  Martin Fleurke  
  Wildrich Fourie
  Thomas Huriaux
  Fabián Mandelbaum
  Maxym Mykhalchuk 
  Arno Peters
  Henry Pijffers 
  Tiago Saboga
  Andrzej Sawuła
  Benjamin Siband
  Martin Wunderlich

Other contributions by
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (localization manager)
  Vito Smolej (documentation manager)
  Samuel Murray
  Marc Prior 
  and many, many more very helpful people

(If you think you have significantly contributed to the OmegaT Project 
but you don't see your name on the lists, feel free to contact us.)

OmegaT uses the following libraries:

  HTMLParser by Somik Raha, Derrick Oswald and others (LGPL License)
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter 1.0.8 by Steve Roy (LGPL License)
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework 2.1.4 by VLSolutions (CeCILL License)
  http://www.vlsolutions.com/en/products/docking/

  Hunspell by László Németh and others (LGPL License)

  JNA by Todd Fast, Timothy Wall and others (LGPL License)

  Swing-Layout 1.0.2 (LGPL License)

  Jmyspell 2.1.4 (LGPL License)

  JAXB 2.1.7 (GPLv2 + classpath exception)

==============================================================================
 5.  Is OmegaT bugging you? Do you need help?

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

Every good bug report needs exactly three things.
  - Steps to reproduce,
  - What you expected to see, and
  - What you saw instead.

You can add copies of files, portions of the log, screen shots, anything that
you think will help the developers with finding and fixing your bug.

To browse the archives of the user group, go to:
     http://groups.yahoo.com/group/OmegaT/

To browse the bug report page and file a new bug report if necessary, go to:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

To keep track of what is happening to your bug report you may want to register
as a Source Forge user.

==============================================================================
6.   Release details

Please see the file 'changes.txt' for detailed information about changes in
this and all previous releases.


==============================================================================
