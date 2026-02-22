# external_spellcheck.groovy

## Feature Realization
This script leverages external word processors (like Microsoft Word or LibreOffice) to perform spell checking or grammar checking. It exports all current translations into a temporary `.doc` file and then triggers the system to open that file with the default associated application.

## Key APIs
- `project.projectProperties.projectRoot`: To determine where to save the temporary output.
- `project.projectFiles`: To collect translations from the project.
- `project.getTranslationInfo(ste)`: To retrieve translation text for each segment.
- `org.omegat.util.Platform.osType`: To determine the OS command to open the resulting file.

## Important Constraints or Limitations
- **External Tool Required**: The user must have a word processor installed that can open `.doc` files.
- **Format**: The script creates a UTF-8 text file and simply gives it a `.doc` extension. It is not a native binary `.doc` or `.docx` file.
- **One-Way**: Changes made in the external word processor are not automatically imported back into OmegaT.
