# check_rules.groovy

## Feature Realization
This is a powerful Quality Assurance (QA) script that checks for a wide range of common translation issues. It features a graphical user interface where users can enable or disable specific checks, including:
- Leading, trailing, and double spaces.
- Double words (repeated words).
- Segment length (shorter or longer than source by a certain percentage).
- Punctuation mismatches at the end of segments.
- Capitalization mismatches at the start of segments.
- Identical source and target content.
- Untranslated segments.
- Tag errors (number, order, or spacing of tags).
- Numeric mismatches.
- Spelling errors (using OmegaT's built-in spellchecker).
- LanguageTool integration for advanced grammar and style checks.

Results are displayed in an interactive table, allowing users to quickly jump to problematic segments in the editor.

## Key APIs
- `Core.getSpellChecker()`: Accesses the built-in spellchecker.
- `project.getTargetTokenizer()`: Retrieves the tokenizer for the target language.
- `LanguageToolNativeBridge`: Facilitates integration with the LanguageTool library.
- `project.projectFiles`: Iterates through the files and segments of the project.
- `editor.gotoEntry(entryNum)`: Jumps to the specific segment in the editor when a result is clicked.

## Important Constraints or Limitations
- **Performance**: Scanning the entire project for multiple rules can be time-consuming, especially for large projects.
- **Dependency**: LanguageTool checks require LanguageTool to be correctly configured and available to OmegaT.
- **Spellcheck Dependency**: The spellcheck rule relies on OmegaT having a spellchecker and dictionary configured for the target language.
