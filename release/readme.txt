@@TRANSLATION_NOTICE@@

==============================================================================
  OmegaT 1.7.2, Read Me file

  1.  Information about OmegaT
  2.  What is OmegaT?
  3.  General notes about Java & OmegaT
  4.  Contributions to OmegaT
  5.  Is OmegaT bugging you? Do you need help?
  6.  Release details

==============================================================================
  1.  Information about OmegaT


The most current info about OmegaT can be found at
(in English, Slovak, Dutch, Portugese):
      http://www.omegat.org/omegat/omegat.html

User support, at the Yahoo user group (multilingual), where the archives are
searchable without subscription:
     http://groups.yahoo.com/group/OmegaT/

Requests for Enhancements (in English), at the SourceForge site:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Bug reports (in English), at the SourceForge site:
     http://sourceforge.net/tracker/?group_id=68187&atid=520347

==============================================================================
  2.  What is OmegaT?

OmegaT is a Computer Assisted Translation tool. It is free, in the meaning
that you don't have to pay anything to be able to use it, even for
professional use, and in the meaning that you are free to modify it and/or
re-distribute it as long as you respect the user license.

OmegaT's main features are
  - ability to run on any operating system supporting Java
  - use of any valid TMX file as translation reference
  - flexible sentence segmenting (using an SRX-like method)
  - searches in the project and the reference translation memories
  - searches in any directory including OmegaT-readable files
  - fuzzy matching
  - smart handling of projects including complex directory hierarchies
  - support for glossaries (terminology checks)
  - easy to understand documentation and tutorial
  - localization in a number of languages.

OmegaT supports OpenDocument files, Microsoft Office files (using
OpenOffice.org as a conversion filter, or by conversion to HTML),
OpenOffice.org or StarOffice files, as well as (X)HTML, Java localization
files, plain text files, and more.

OmegaT will automatically parse even the most complex source directory
hierarchies, to access all the supported files, and produce a target directory
with exactly the same structure, including copies of any non-supported files.

For a quick-start tutorial, launch OmegaT and read the displayed Instant Start 
Tutorial.

The user manual is in the package you just downloaded, you can access it from
the [Help] menu after starting OmegaT.

==============================================================================
 3. Installing OmegaT

3.1 General
In order to run, OmegaT requires the Java Runtime Environment (JRE) version 
1.4 or higher to be installed on your system. OmegaT is now supplied as 
standard with the Java Runtime Environment to save users the trouble of 
selecting, obtaining and installing it. Windows and Linux users: if you are 
confident that your system already has a suitable version of the JRE 
installed, you can install the version of OmegaT without the JRE (this is 
indicated in the name of the version, "Without_JRE"). If you are in any 
doubt, we recommend that you use the "standard" version, i.e. with JRE. This 
is safe, since even if the JRE is already installed on your system, this 
version will not interfere with it.
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
To install OmegaT, simply create a suitable folder for OmegaT (e.g. C:\Program 
Files\OmegaT on Windows or /usr/local/lib on Linux). Copy the OmegaT zip 
archive to this folder and unpack it there.

3.3 Launching OmegaT
OmegaT can be launched in a number of ways.

* Windows users: by double-clicking on the file OmegaT-JRE.exe, if you are
using the version with the JRE included, or on OmegaT.exe otherwise.

* By double-clicking on the file OmegaT.bat. If you can see the file
OmegaT but not OmegaT.bat in your File Manager (Windows Explorer), change
the settings so that file extensions are displayed.

* By double-clicking on the file OmegaT.jar. This will work only if the .jar
file type is associated with Java on your system.

* From the command line. The command to launch OmegaT is:

cd <folder where the file OmegaT.jar is located>

<name and path of the Java executable file> -jar OmegaT.jar

(The Java executable file is the file java on Linux and java.exe on Windows.
If Java is installed at system level, the full path need not be entered.)

* Windows users: you can drag the files OmegaT-JRE.exe, OmegaT.exe or
OmegaT.bat to the desktop or Start menu to link it from there.

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
 4. Contributions to OmegaT

To contribute to OmegaT development, get in touch with the developers at:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

To translate OmegaT's user interface, user manual or other related documents,
read:
      http://www.omegat.org/omegat/omegat_en/translation-info.html

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
  Didier Briel
  Kim Bruning
  Sacha Chua
  Thomas Huriaux
  Maxym Mykhalchuk (lead developer)
  Henry Pijffers (release manager)
  Benjamin Siband
  Martin Wunderlich

Other contributions by
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (documentation manager)
  Samuel Murray
  Marc Prior (localization manager)
  and many, many more very helpful people

(If you think you have significantly contributed to the OmegaT Project 
but you don't see your name on the lists, feel free to contact us.)

OmegaT uses the following libraries:
  HTMLParser by Somik Raha, Derrick Oswald and others (LGPL License).
  http://sourceforge.net/projects/htmlparser

  MRJ Adapter by Steve Roy (LGPL License).
  http://homepage.mac.com/sroy/mrjadapter/

  VLDocking Framework by VLSolutions (CeCILL License).
  http://www.vlsolutions.com/en/products/docking/

==============================================================================
 5.  Is OmegaT bugging you ? Do you need help ?

Before reporting a bug, make sure that you have thoroughly checked the
documentation. What you see may instead be a characteristic of OmegaT that
you have just discovered. If you check the OmegaT log and you see words like
"Error", "Warning", "Exception", or "died unexpectedly" then you are on to
something (the log.txt is located in the user preferences directory, see the
manual for its location).

The next thing you do is confirm what you found with other users, to make sure
this has not already been reported. You can verify the bug report page at
SourceForge too. Only when you are sure you are the first to have found some
reproductible sequence of event that triggered something not supposed to
happen should you file a bug report.

Every good bug report needs exactly three things.
  - Steps to reproduce,
  - What you expected to see, and
  - What you saw instead.

You can add copies of files, portions of the log, screenshots, anything that
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

File formats supported:
  - Plain text
  - HTML and XHTML
  - HTML Help Compiler
  - OpenDocument / OpenOffice.org
  - Java resource bundles (.properties)
  - INI files (files with key=value pairs of any encoding)
  - PO files
  - DocBook documentation file format
  - Microsoft OpenXML files
  - Okapi monolingual XLIFF files

Core changes:
  -

New UI features (compared to 1.6 OmegaT series):
  -

==============================================================================

