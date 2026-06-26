# open_folder.groovy

## Feature Realization
This script opens the root directory of the currently active OmegaT project in the operating system's file manager.

## Key APIs
- `project.projectProperties.projectRoot`: Retrieves the absolute path to the project's root folder.
- `org.omegat.util.Platform.osType`: Used to determine the system-specific command for opening a folder.
- `command.execute()`: Executes the system command.

## Important Constraints or Limitations
- **Active Project Required**: Does nothing and shows an error message if no project is open.
- **Platform Dependent**: Uses different commands depending on whether it's running on Windows, macOS, or Linux.
