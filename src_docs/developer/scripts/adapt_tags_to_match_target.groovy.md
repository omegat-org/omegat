# adapt_tags_to_match_target.groovy

## Feature Realization
This advanced script automatically adjusts OmegaT's standard tags when a user invokes the "Replace with Match" command. It ensures that the tags in the inserted translation match the tag numbering and structure of the current source segment, even if the fuzzy match comes from a project or TM with different tag numbering.

For example, if the current segment is `<a1>foo</a1>` and the match is `<a9>foo</a9>` with translation `<a9>bar</a9>`, the script will adapt the translation to `<a1>bar</a1>` upon insertion.

## Key APIs
- `Core.matcher.activeMatch`: Retrieves the fuzzy match currently selected in the user interface.
- `mainWindow.mainMenu.editOverwriteTranslationMenuItem`: Accesses the "Replace with Match" menu item to add a custom action listener.
- `Core.editor.replaceEditText()`: Inserts the adapted translation into the editor.
- `org.omegat.util.PatternConsts.OMEGAT_TAG`: Regular expression used to identify and manipulate OmegaT's internal tag representation.

## Important Constraints or Limitations
- **Standard Tags Only**: The script only handles OmegaT's standard tags (e.g., `<a1>`, `<s2/>`) and does not adapt user-defined custom tags.
- **Session-based**: The script must be run once per session (or via `application_startup`) to hook into the menu item's action.
- **Complexity**: It uses complex heuristics to map tags between segments, which might not always produce the desired result in very complex or ambiguous tag scenarios.
