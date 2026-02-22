# switch_colour_theme.js

## Feature Realization
This JavaScript script allows users to quickly change the color scheme of the OmegaT editor. It defines several theme profiles ('Default', 'Dark', and 'Trafficlight') and updates the corresponding application preferences for background, foreground, and various highlighting colors (e.g., untranslated, translated, fuzzy matches).

## Key APIs
- `org.omegat.util.Preferences.setPreference(name, color)`: Updates a specific OmegaT configuration setting.
- `org.omegat.util.Preferences.save()`: Persists the changes to the user's configuration file.
- `console.println()`: Provides feedback to the user in the Scripting Window.

## Important Constraints or Limitations
- **Restart Required**: The theme changes will only be visible after OmegaT is restarted.
- **JavaScript Engine**: Requires a JSR-223 compliant JavaScript engine (e.g., Nashorn or GraalJS).
- **Preference Names**: Relies on specific internal preference key names (e.g., `COLOR_BACKGROUND`, `COLOR_SOURCE`).
