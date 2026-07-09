# nbsp.groovy

## Feature Realization
This script automatically replaces regular spaces with non-breaking spaces (`\u00A0`) in the target text of a translation project, specifically following French typography rules.

It targets:
- Spaces before certain punctuation marks: `:`, `?`, `!`, `;`, and `»`.
- Spaces before the percent sign `%`.
- Spaces after the opening French quotation mark `«`.
- Optionally, it can also replace straight apostrophes `'` with curly ones `’` (disabled by default in the script).

## Key APIs
- `editor.getCurrentEntry()`: To get information about the currently active entry and store its position.
- `project.allEntries`: To iterate over every segment in the translation project.
- `ste.getSrcText()`: To retrieve the source text of a specific entry.
- `project.getTranslationInfo(ste)`: To check if an entry is translated and retrieve its current translation.
- `editor.gotoEntry(entryNum)`: To move the editor's focus to a specific segment for modification.
- `editor.replaceEditText(target)`: To update the translation of the currently active segment in the editor.
- `console.println()`: To log the changed segments and the final count to the Scripting Window.

## Important Constraints or Limitations
- **Project-wide Iteration**: The script iterates through all entries in the project. For very large projects, this may take some time.
- **Visual Disruption**: Because it uses `editor.gotoEntry()` and `editor.replaceEditText()`, the editor will jump through each modified segment.
- **French-Centric**: The regular expressions used are specifically tailored for French typography and may not be suitable for other languages.
- **Threading**: The logic is wrapped in a `gui()` function, ensuring it runs on the Event Dispatch Thread (EDT), which is necessary because it interacts with the editor UI.
