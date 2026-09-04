/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.awt.Color;

import org.jspecify.annotations.Nullable;

/**
 * A configurable application color: the extension point behind the colors
 * preferences table and color scheme export/import. Core entries are the
 * {@link Styles.EditorColor} constants; plugins contribute additional entries
 * via {@link ColorRegistry#registerPluginColor}.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public interface ColorEntry {

    /**
     * Stable identifier of this entry: the preference key it persists under
     * and the key used in exported color schemes. Core entries use the
     * {@code EditorColor} constant name; plugin entries are namespaced as
     * {@code pluginId:colorKey}.
     */
    String getId();

    /** Human-readable name shown in the preferences table. */
    String getDisplayName();

    /**
     * The color currently in effect: the user-configured color if one is
     * set, otherwise {@link #getDefault()}.
     */
    Color getColor();

    /**
     * The default for the installed look and feel, falling back to the
     * entry's built-in color when the theme defines nothing.
     */
    Color getDefault();

    /**
     * Set and persist the user-configured color. Passing {@code null} — or a
     * color equal to the current default — resets the entry, so it keeps
     * following the (theme-dependent) default from then on. Callers are
     * responsible for broadcasting
     * {@link org.omegat.core.CoreEvents#fireColorsChanged()} once a batch of
     * changes is complete.
     */
    void setColor(@Nullable Color newColor);
}
