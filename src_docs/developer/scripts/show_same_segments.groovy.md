# show_same_segments.groovy

## Feature Realization
This script identifies segments where source and target content are identical and displays them in a GUI window with a table. Each result in the table includes a button that, when clicked, automatically navigates the OmegaT editor to that specific segment.

## Key APIs
- `project.projectFiles`: To scan all files in the project.
- `ste.entryNum()`: To identify the segment number.
- `editor.gotoEntry(value)`: To navigate to the selected segment in the main OmegaT window.
- `groovy.swing.SwingBuilder`: To construct the results window and table.

## Important Constraints or Limitations
- **Full Scan**: Scans the entire project, which may take time depending on the project size.
- **GUI Dependency**: Relies on Java Swing for the display window.
