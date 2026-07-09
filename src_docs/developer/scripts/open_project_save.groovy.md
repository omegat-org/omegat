# open_project_save.groovy

## Feature Realization
This script opens the `project_save.tmx` file (the main working translation memory of the current project) in an external text editor for manual inspection or editing.

## Key APIs
- `project.projectProperties.projectInternal`: Used to construct the path to the internal `project_save.tmx` file.
- `org.omegat.util.Platform.osType`: Used to determine OS-specific execution commands.
- `java.awt.Desktop.desktop.open()`: Fallback method to open the file with the default system application on Windows.

## Important Constraints or Limitations
- **Manual Edit Risk**: Editing `project_save.tmx` manually while the project is open in OmegaT can lead to data loss or corruption, as OmegaT frequently writes to this file.
- **Customization**: Users can specify a preferred text editor by modifying the `textEditor` variable in the script.
- **Active Project Required**: Requires a project to be open to locate the file.
