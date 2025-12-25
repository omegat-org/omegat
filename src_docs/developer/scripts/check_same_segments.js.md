# check_same_segments.js

## Feature Realization
This JavaScript script performs the same function as its Groovy counterpart: it identifies segments where the translation is identical to the source text and logs them to the Scripting Window.

## Key APIs
- `project.projectFiles`: To iterate over project files.
- `ste.getSrcText()`: To get source text.
- `project.getTranslationInfo(ste)`: To get translation text.
- `console.println()`: To log results.

## Important Constraints or Limitations
- **JavaScript Engine**: Requires a JSR-223 compliant JavaScript engine (like Nashorn or GraalJS).
- **Case-Sensitive**: Performes a strict case-sensitive comparison.
- **Console Output**: Does not provide a GUI for interactive segment jumping.
