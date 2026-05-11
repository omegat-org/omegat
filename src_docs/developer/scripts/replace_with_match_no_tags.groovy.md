# replace_with_match_no_tags.groovy

## Feature Realization
This script replaces the current segment's translation with the text from the currently selected fuzzy match in the Fuzzy Matches pane, but with all tags automatically removed from that match.

## Key APIs
- `Core.getMatcher()`: Retrieves the Matcher component of OmegaT.
- `match.getActiveMatch()`: Gets the fuzzy match currently selected by the user.
- `editor.replaceEditText(matchtranslation)`: Replaces the translation in the editor with the tag-free match text.

## Important Constraints or Limitations
- **Active Match Required**: The user must have a fuzzy match selected in the Fuzzy Matches pane for this script to work.
- **Regex Simplicity**: Like `replace_strip_tags`, it uses a basic regex to strip tags, which may have limitations with complex tag structures.
- **Project Context**: Requires an open project to function.
