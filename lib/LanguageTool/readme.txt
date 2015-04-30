Upgrading LangaugeTool for OmegaT
=================================

1. Get the new ZIP from https://languagetool.org/download/

2. Replace old JARs with new ones in `lib`. It's probably not clear
which are newer, so it may help to annotate the POM from the version
you're upgrading to and seeing which dependencies have changed since
the version previously in use. Ex:
https://github.com/languagetool-org/languagetool/blame/8063ca08947be6830082a70380b4ae21518d9c08/languagetool-core/pom.xml

Use the CHANGES file to figure out the relevant dates:
https://www.languagetool.org/download/CHANGES.txt

3. Prepare the LanguageTool-data.jar:
  a. Delete all `hunspell` folders under `org`.
  b. Zip `META-INF` and `org` together to create the JAR.

4. Update classpaths, readmes, etc. with the new filenames.
