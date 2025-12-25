# strip_bidi_marks.groovy

## Feature Realization
This script removes bidirectional (BiDi) control characters (such as LRM, RLM, LRE, RLE, etc.) from the currently selected text in the editor or the entire translation of the active segment.

## Key APIs
- `editor.selectedText`: Checks for text selection in the editor.
- `editor.getCurrentTranslation()`: Retrieves the full translation text of the active segment.
- `editor.insertText(target)`: Replaces selection with cleaned text.
- `editor.replaceEditText(target)`: Replaces entire segment translation with cleaned text.

## Important Constraints or Limitations
- **Scope**: Operates only on the currently active segment or selection.
- **Specific Characters**: It targets a specific range of Unicode characters: `\u200E`, `\u200F`, `\u202A` to `\u202E`, and `\u2066` to `\u2069`.
