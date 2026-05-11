# replace_strip_tags.groovy

## Feature Realization
This script removes HTML-like tags (e.g., `<b>`, `</b>`, `<div/>`) from either the currently selected text in the editor or the entire translation of the active segment.

## Key APIs
- `editor.selectedText`: Checks if any text is currently selected in the editor.
- `editor.getCurrentTranslation()`: Retrieves the full translation text of the active segment.
- `editor.insertText(target)`: Inserts the cleaned text back into the selection.
- `editor.replaceEditText(target)`: Replaces the entire segment translation with the cleaned text.

## Important Constraints or Limitations
- **Regex Simplicity**: It uses a simple regular expression `<\/?[a-z]+[0-9]* ?\/?>` to identify tags. This may not correctly identify all valid HTML/XML tags, especially those with complex attributes.
- **Scope**: Operates only on the currently active segment or selection.
