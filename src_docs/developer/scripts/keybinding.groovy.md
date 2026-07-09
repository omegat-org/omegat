# keybinding.groovy

## Feature Realization
This script provides an example of how to intercept keyboard events globally within the OmegaT application. It demonstrates how to add a `KeyEventDispatcher` to the system's `KeyboardFocusManager`.

## Key APIs
- `java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager()`: Accesses the global focus and keyboard manager.
- `java.awt.KeyEventDispatcher`: The interface used to receive and process key events before they reach their intended components.
- `manager.addKeyEventDispatcher()`: Registers the dispatcher.

## Important Constraints or Limitations
- **Global Scope**: Intercepts ALL keyboard events across the entire application. If not handled carefully, it can prevent normal application function (e.g., typing in the editor).
- **Advanced Usage**: This uses low-level Java AWT/Swing APIs and is intended for developers who need to implement custom global shortcuts or specialized input handling.
- **Persistence**: Once added, the dispatcher remains active until it is explicitly removed or the application is restarted.
