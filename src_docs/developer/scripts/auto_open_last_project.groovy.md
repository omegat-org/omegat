# auto_open_last_project.groovy

## Feature Realization
This script automatically opens the most recently used OmegaT project when the application starts. It achieves this by programmatically triggering a click on the first item of the "Open Recent" menu.

## Key APIs
- `mainWindow.mainMenu.projectOpenRecentMenuItem`: Accesses the "Open Recent" menu in the OmegaT main window.
- `item.doClick()`: Programmatically clicks the menu item to trigger the project opening logic.
- `org.omegat.Main.projectLocation`: Checks if a project was already specified via command-line arguments to avoid conflicts.

## Important Constraints or Limitations
- **Event-Driven**: To be effective, this script should be placed in the `application_startup` subdirectory of the user's scripts folder.
- **Menu Dependency**: Relies on the GUI menu structure; if the menu is empty or the item is disabled, it will log a message and do nothing.
- **Command-line Priority**: If OmegaT is started with a specific project path in the command line, this script will respect that and not attempt to open the last project.
