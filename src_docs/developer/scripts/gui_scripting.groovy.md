# gui_scripting.groovy

## Feature Realization
This script is a comprehensive example showing how to build an interactive graphical user interface (GUI) within an OmegaT script using Groovy's `SwingBuilder`. It scans the project for segments where source and target are identical and displays them in a table. Users can click on the segment number in the table to jump directly to that segment in the OmegaT editor.

## Key APIs
- `groovy.swing.SwingBuilder`: Used to declaratively build the Java Swing UI (frame, scroll pane, table).
- `project.projectFiles`: To iterate through the project's files and entries.
- `editor.gotoEntry(value)`: Called from the table's cell editor to navigate the main OmegaT window.
- `TableCellEditor` & `TableCellRenderer`: Custom Java Swing components implemented in Groovy to make the table interactive.

## Important Constraints or Limitations
- **Educational Example**: Designed primarily to demonstrate GUI building capabilities in scripts.
- **Project Scan**: Performs a full project scan, which might be slow for large projects.
- **Swing Knowledge**: Requires understanding of Java Swing and Groovy's `SwingBuilder` for modification.
