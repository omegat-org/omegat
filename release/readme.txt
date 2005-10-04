==============================================================================
OmegaT 1.4.6 RC Read Me file

I.   Where to get information about OmegaT
II.  General notes about Java & OmegaT
III. Contributions to OmegaT
IV.  Is OmegaT bugging you? Do you need help?
V.   Release details

==============================================================================
I.  Where to get information about OmegaT

The most actual info about OmegaT may be found at:
     http://www.omegat.org/omegat/omegat.html

OmegaT is a high performance Java based Computer Assisted Translation
tool with fuzzy matching, translation memory, keyword search, glossary
term identification, and translation leveraging into updated projects.

For a quick start tutorial, check:
     http://www.leuce.com/translate/omegat/en_Instant.html

The user manual is in the package you just downloaded, you can access
it from the [Help] menu after starting OmegaT.

==============================================================================
II. General notes about Java & OmegaT

OmegaT 1.4.x requires the Java Runtime Environment version 1.4 or
higher be installed on your system. It is available from:
     http://java.com:80/en/selectlanguage.jsp

Windows and Linux users may need to install Java if it is not already
done.
MacOSX users have Java already installed on their machines.

On a properly installed machine, you should be able to launch OmegaT by
double-clicking on the OmegaT.jar file.

After installing java modify your system path variable so that it
includes the directory where the 'java' application resides.

To run OmegaT from a terminal change to the install (this) directory and
- for Unixes (Linux, FreeBSD, MacOSX) type (case sensitive!)
     ./OmegaT
- for Windows type
     OmegaT.bat or OmegaT.exe

==============================================================================
III. Contributions to OmegaT

OmegaT is originally the work of Keith Godfrey

Code has been contributed by
   Benjamin Siband
   Maxym Mykhalchuk (lead developer at the time of this release)
   Sacha Chua
   Henry Pijffers
   Raymond Martin

Localization contributed by
   Alessandro Cattelan (Italian)
   Sabine Cretella and Martin Wunderlich (German)
   Dmitri Gabinski (Belorussian, Esperanto, and Russian)
   Jean-Christophe Helary (French)
   Pablo Roca Santiagio; Juan Salcines and Cesar Escribano Esteban (Spanish)
   Hisashi Yanagida (Japanese)
   Erhan Yukselci (Turkish)

Other contributions by
   Marc Prior (project coordinator and ASAD manual writer)
   Jean-Christophe Helary
   Samuel Murray
   Dmitry Gabinski
   Raymond Martin
   and many, many more very helpful people

To contribute to OmegaT development, get in touch with the developers
at:
     http://sourceforge.net/projects/omegat

To translate OmegaT's user interface, user manual or other related
documents, read:
     http://www.omegat.org/omegat/translation-info.html

And subscribe to the translators' list:
     http://lists.sourceforge.net/mailman/listinfo/omegat-l10n

For other kind of contributions, subscribe first to the user group at:
     http://groups.yahoo.com/group/OmegaT/

And get a feel of what is going on in the OmegaT world...

==============================================================================
IV.  Is OmegaT bugging you ? Do you need help ?

In the event of a system error (as evidenced by a stack dump in the
command line window or file 'log.txt'),
please note the error and a brief description of how the error occured
and file the bug for OmegaT project:
     http://sourceforge.net/tracker/?func=add&group_id=68187&atid=520347

Do not forget to attach the 'log.txt' file.

If your problem does not need a bug report, you can get user volunteer
support at:
     http://groups.yahoo.com/group/OmegaT/

==============================================================================
V.   Release details

New UI features (comparing to 1.0 OmegaT series):
    Find interface rewritten with enhanced functionality
    Main interface improved
    Font may be selected
    Full localisation support
    Belorussian, English, Esperanto, French, German, Italian, Japanese, 
                            Russian, Spanish, Turkish interface and manual
    Ability to jump to the next untranslated segment
    Rich customization of Format Filters behaviour
    User-customizable Segmentation
    Match/Glossary Window has a Split Pane
File formats supported:
    Plain text
    HTML and XHTML
    OpenDocument / OpenOffice
    Java resource bundles (.properties)
    NetBeans EDGE Newsletters
Core changes:
    Flexible (Sentence) Segmentation
    File format filters may be created as plugins
    Refactored code with more comments
    Windows installer
    Attributes of HTML tags are translatable

==============================================================================
Please see the file 'changes.txt' for detailed information about changes 
in this and all previous releases.
==============================================================================