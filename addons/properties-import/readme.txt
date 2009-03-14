.properties Import 1.4 ReadMe file

This import tool supplements OmegaT in the following way:

If there's an existing translation of Java application, usually done using 
Java(TM) Resource Bundles (.properties files), then in order not to loose the
existing work, while migrating to OmegaT, translator is given an opportunity 
to import his work into OmegaT-compatible TMX file.

To run from a terminal or DOS window, change to this directory
and type
  java -jar properties_import.jar

The sources are supplied inside the OmegaT sources distribution.

----------------------------------------------------------------------

version 1.4_1: Corrected the Japanese localization
version 1.4: Added the Japanese localization
Version 1.3: Fixed a filecase bug (occuring only on Unixes)
Version 1.2: Make it look nicer
Version 1.1: Some bugs fixed
Verison 1.0: Initial release
