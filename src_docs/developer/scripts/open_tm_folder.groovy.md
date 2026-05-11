# open_tm_folder.groovy

## Feature Realization
This script provides a quick way to open the project's `tm` (translation memory) folder using the operating system's default file manager (File Explorer on Windows, Finder on macOS, or the default file manager on Linux via `xdg-open`).

## Key APIs
- `project.projectProperties.TMRoot`: Retrieves the absolute path to the project's translation memory folder.
- `org.omegat.util.Platform.osType`: Identifies the current operating system to determine the appropriate command to open the folder.
- `command.execute()`: Runs the system command to open the folder.

## Important Constraints or Limitations
- **Active Project Required**: A project must be currently open in OmegaT.
- **System Dependency**: Relies on the availability of system-level commands like `explorer.exe`, `open`, or `xdg-open`.
