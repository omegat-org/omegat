# open_glossary.groovy

## Feature Realization
This script opens the project's writable glossary file in an external text editor. If no specific editor is configured in the script, it attempts to use the system's default application for the file.

## Key APIs
- `project.projectProperties.writeableGlossary`: Retrieves the path to the current project's writable glossary file.
- `java.awt.Desktop.desktop.open()`: Used on Windows as a fallback to open the file with the default associated application.
- `org.omegat.util.Platform.osType`: Used for OS-specific execution logic.

## Important Constraints or Limitations
- **File Existence**: The glossary file must exist for the script to open it.
- **Customization**: Users can edit the script to specify a preferred text editor by uncommenting and setting the `textEditor` variable.
- **Active Project Required**: A project must be open to identify the glossary file.
