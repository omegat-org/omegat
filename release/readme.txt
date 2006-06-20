==============================================================================
  OmegaT 1.6.0 RC10 Read Me file

  1.  Information about OmegaT
  2.  What is OmegaT?
  3.  General notes about Java & OmegaT
  4.  Contributions to OmegaT
  5.  Is OmegaT bugging you? Do you need help?
  6.  Release details

==============================================================================
  1.  Information about OmegaT

The most current info about OmegaT can be found at:
      http://www.omegat.org/omegat/omegat.html

More information can be found on the following pages:

User support, at the Yahoo user group:
     http://groups.yahoo.com/group/OmegaT/
     Where the archives are searchable without subscription.

Requests for Enhancements, at the SourceForge site:
     http://sourceforge.net/tracker/?group_id=68187&atid=520350

Bug reports, at the SourceForge site:
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
OpenOffice.org as a conversion filter), OpenOffice.org or StarOffice files, as
well as (X)HTML, Java localization files or plain text files.

OmegaT will automatically parse even the most complex source directory
hierarchies, to access all the supported files, and produce a target directory
with exactly the same structure, including copies of any non-supported files.

For a quick-start tutorial, launch OmegaT and read the displayed Instant Start 
Tutorial.

The user manual is in the package you just downloaded, you can access it from
the [Help] menu after starting OmegaT.

==============================================================================
 3. General notes about Java & OmegaT

OmegaT requires the Java Runtime Environment version 1.4 or higher be
installed on your system. It is available from:
      http://java.com

Windows and Linux users may need to install Java if it is not already done.
MacOSX users have Java already installed on their machines.

On a properly installed machine, you should be able to launch OmegaT by
double-clicking the OmegaT.jar file.

After installing java you may need to modify your system path variable so that
it includes the directory where the 'java' application resides.

==============================================================================
 4. Contributions to OmegaT

To contribute to OmegaT development, get in touch with the developers at:
    http://lists.sourceforge.net/lists/listinfo/omegat-development

To translate OmegaT's user interface, user manual or other related documents,
read:
      http://www.omegat.org/omegat/translation-info.html

And subscribe to the translators' list:
      http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

For other kind of contributions, subscribe first to the user group at:
      http://groups.yahoo.com/group/OmegaT/

And get a feel of what is going on in the OmegaT world...

  OmegaT is originally the work of Keith Godfrey.
  Marc Prior is the coordinator of the OmegaT project.

Previous contributors include:
(alphabetical order)

Code has been contributed by
  Sacha Chua
  Kim Bruning
  Maxym Mykhalchuk (current lead developer)
  Henry Pijffers
  Benjamin Siband

Localization contributed by
  Alessandro Cattelan (Italian)
  Sabine Cretella (German)
  Cesar Escribano Esteban (Spanish)
  Dmitri Gabinski (Belarusian, Esperanto, and Russian)
  Jean-Christophe Helary (French)
  Juan Salcines (Spanish)
  Pablo Roca Santiagio (Spanish)
  Martin Wunderlich (German)
  Hisashi Yanagida (Japanese)
  Yoshi Nakayama (Japanese)
  Takayuki Hayashi (Japanese)
  Kunihiko Yokota (Japanese)
  Yutaka Kachi (Japanese)

Other contributions by
  Sabine Cretella
  Dmitri Gabinski
  Jean-Christophe Helary (current documentation manager)
  Samuel Murray
  Marc Prior (current localization manager)
  and many, many more very helpful people

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

New UI features (comparing to 1.0 OmegaT series):
  - Find interface rewritten with enhanced functionality
  - Main interface improved
  - Ability to select a display font
  - Full localization support
  - Ability to jump to the next untranslated segment
  - Rich customization of Format Filters behaviour
  - User-customizable Segmentation
  - Match/Glossary Window is united by a draggable split pane

File formats supported:
  - Plain text
  - HTML and XHTML
  - OpenDocument / OpenOffice.org
  - Java resource bundles (.properties)
  - INI files (files with key=value pairs of any encoding)
  - PO files
  - DocBook documentation file format

Core changes:
  - Flexible (Sentence) Segmentation
  - File format filters may be created as plugins
  - Refactored code with more comments
  - Windows installer
  - Attributes of HTML tags are translatable
  - Full TMX 1.1-1.4b Level 1 compatibility

==============================================================================

