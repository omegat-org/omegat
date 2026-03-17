# toolbar.groovy

## Feature Realization
This script creates a custom, dockable toolbar within the OmegaT main window. The toolbar is populated with buttons for each Groovy script found in the user's scripts directory. This provides a way to have one-click access to frequently used scripts directly from the main interface.

## Key APIs
- `javax.swing.JToolBar` & `javax.swing.JButton`: Standard Java Swing components used to build the toolbar.
- `mainWindow.addDockable()`: Adds the newly created toolbar as a dockable component to the OmegaT main window.
- `org.omegat.gui.main.DockableScrollPane`: A wrapper that allows the toolbar to be integrated into OmegaT's dockable UI system.
- `org.omegat.gui.scripting.ScriptItem`: Used to retrieve script names and descriptions for button labels and tooltips.

## Important Constraints or Limitations
- **Incomplete Action**: In the provided version, the action listener for the buttons is not fully implemented; clicking the buttons will not yet execute the associated script.
- **Redundancy**: Running the script multiple times will add multiple toolbars.
- **UI Persistence**: The toolbar may not persist across application restarts unless the script is re-run (e.g., via `application_startup`).
