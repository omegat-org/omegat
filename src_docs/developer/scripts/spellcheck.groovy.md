# spellcheck.groovy

## Feature Realization
This script provides a project-wide spell checking utility with a dedicated GUI. It tokenizes the target text and checks each word against OmegaT's configured spellchecker. It offers advanced options such as:
- Checking the whole project or just the current file.
- Using the project's glossary as a source of "correct" terms to ignore.
- Cleaning up the text before checking (removing tags, BiDi marks, mnemonic characters, and custom tags).
- Filtering out segments where source and target are identical.

The results window allows users to jump to segments, ignore specific words, or "learn" them (add to the user dictionary).

## Key APIs
- `Core.getSpellChecker()`: Accesses the active spellchecking engine.
- `Core.getGlossaryManager()`: Retrieves terms from the project glossaries.
- `Core.getProject().getTargetTokenizer()`: Used to correctly split the translation into individual words.
- `editor.gotoEntry(value)`: Navigates to a segment from the results table.
- `spellchecker.ignoreWord()` & `spellchecker.learnWord()`: Updates the spellchecker's word lists.

## Important Constraints or Limitations
- **Configuration Required**: OmegaT must have a spellchecker and appropriate dictionaries installed and enabled.
- **Preprocessing**: While it attempts to clean tags and marks, complex formatting might still lead to false positives.
- **Performance**: Tokenizing and spellchecking a large project can be resource-intensive.
