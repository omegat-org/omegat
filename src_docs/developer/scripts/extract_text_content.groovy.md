# extract_text_content.groovy

## Feature Realization
This script extracts all source segments and their corresponding target translations from the current project and saves them into two plain text files: `project_source_content.txt` and `project_target_content.txt`. The output format is one segment per line, with file name headers between segments from different source files.

## Key APIs
- `project.projectFiles`: To iterate through all files and entries in the project.
- `ste.getSrcText()`: To retrieve the source text.
- `project.getTranslationInfo(ste)`: To retrieve the translation text.
- `project.projectProperties.projectRoot`: To determine the location where the output files will be saved.

## Important Constraints or Limitations
- **Overwrites Output**: The script will overwrite the output files if they already exist in the project root.
- **Plain Text**: The export is in plain text format and does not preserve any formatting or tag information from the original source files.
- **Large Projects**: Generating these files for very large projects may take some time.
