# check_same_segments.groovy

## Feature Realization
This script scans the entire project to identify segments where the target translation is exactly identical to the source text (case-sensitive). It prints the results (segment number, source, and target) to the Scripting Window's console.

## Key APIs
- `project.projectFiles`: Used to iterate through all files and their entries in the project.
- `ste.getSrcText()`: Retrieves the source text of an entry.
- `project.getTranslationInfo(ste)`: Retrieves the translation text of an entry.
- `console.println()`: Logs the findings to the Scripting Window.

## Important Constraints or Limitations
- **Case-Sensitive**: The comparison is strictly case-sensitive.
- **Console Only**: Results are only printed to the console; there is no interactive GUI to jump to segments.
- **Full Scan**: Always scans the whole project, which might be slow on very large projects.
