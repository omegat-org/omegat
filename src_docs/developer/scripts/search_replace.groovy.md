# search_replace.groovy

## Feature Realization
This script provides a user-friendly way to perform project-wide search and replace operations using regular expressions. It opens a GUI window where the user can enter the search pattern and the replacement string. It then iterates through all translated segments in the project and applies the replacement.

## Key APIs
- `groovy.swing.SwingBuilder`: Used to create the search and replace dialog window.
- `project.allEntries`: To iterate through every segment in the translation project.
- `target.replaceAll(search_string, replace_string)`: Performs the actual regular expression replacement on the translation text.
- `editor.gotoEntry(ste.entryNum())`: Navigates to the modified segment.
- `editor.replaceEditText(target)`: Updates the segment's translation with the new text.

## Important Constraints or Limitations
- **Regular Expressions**: The search string is treated as a regular expression.
- **Translated Segments Only**: The script only processes segments that already have a translation.
- **Sequential Execution**: It jumps to and modifies each segment one by one in the editor, which can be visually busy and may take time for many replacements.
- **No Undo**: Changes made by the script cannot be globally undone with a single "undo" command; each segment modification would need to be undone individually.
