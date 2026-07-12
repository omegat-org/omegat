# open_current_file.groovy

## Feature Realization
This script identifies the source file that is currently being edited in OmegaT and opens it using the operating system's default application for that file type.

## Key APIs
- `editor.currentFile`: Retrieves the name of the file that contains the currently active segment.
- `project.projectProperties.sourceRoot`: Provides the path to the project's source directory.
- `org.omegat.util.Platform.osType`: Used to determine the correct system command (`open`, `explorer`, `xdg-open`).
- `java.awt.Desktop.desktop.open()`: Used on Windows to open the file.

## Important Constraints or Limitations
- **Active Selection**: A project and a specific file must be open in the editor.
- **System Association**: Relies on the operating system having a default application associated with the file's extension.
