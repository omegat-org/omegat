# currency_translate.groovy

## Feature Realization
This script converts currency representations in the target text from the source locale format to the target locale format. For example, it can transform `$123,399.99` (US format) into `123 399,99 USD` (French format).

## Key APIs
- `java.text.NumberFormat`: Used to parse and format currencies based on locales.
- `project.projectProperties.sourceLanguage.locale`: Retrieves the locale for the project's source language.
- `project.projectProperties.targetLanguage.locale`: Retrieves the locale for the project's target language.
- `project.allEntries`: Iterates through all project segments.
- `editor.gotoEntry(ste.entryNum())`: Moves the editor to the segment being modified.

## Important Constraints or Limitations
- **Disabled by Default**: The actual replacement line `editor.replaceEditText(target)` is commented out in the script for safety. Users must manually uncomment it after verifying the logs.
- **USD-Centric Regex**: The source currency detection is based on a regex specifically looking for the `$` symbol and US-style number formatting. It may not work for other source currencies without modification.
- **Performance**: Iterates through all segments, which can be slow on large projects.
