# modify_segment.groovy

## Feature Realization
This script serves as an example and template for developers to understand how to iterate through segments and access their properties. It demonstrates how to retrieve the currently active entry, its source text, and its current translation.

## Key APIs
- `editor.currentEntry`: Accesses the `SourceTextEntry` of the currently active segment.
- `project.getTranslationInfo(ste)`: Retrieves translation information for a given `SourceTextEntry`.
- `console.println()`: Prints information (like segment number and text) to the Scripting Window.

## Important Constraints or Limitations
- **Example Only**: The script is intended for educational purposes and does not perform any meaningful project modification by default.
- **Limited Scope**: It primarily focuses on the current segment and demonstrating basic accessors.
