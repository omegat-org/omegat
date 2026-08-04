/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 OmegaT contributors
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

import java.awt.Color;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;

import org.omegat.util.gui.Styles.EditorColor;

/**
 * Dumps the effective per-theme value of every {@link EditorColor} as CSV
 * lines {@code ENUM_NAME,uiManagerKey,rrggbb[aa]}. Input for
 * generate_editor_colors_svg.py (same directory; see its header for the
 * complete invocation).
 */
public final class ColorDump {

    private ColorDump() {
    }

    public static void main(String[] args) throws Exception {
        LookAndFeel laf = (LookAndFeel) Class.forName(args[0]).getDeclaredConstructor().newInstance();
        UIDefaults defaults = laf.getDefaults();
        for (EditorColor c : EditorColor.values()) {
            Color v = defaults.getColor(c.getUIManagerKey());
            String hex = v == null ? ""
                    : v.getAlpha() == 255
                            ? String.format("%02x%02x%02x", v.getRed(), v.getGreen(), v.getBlue())
                            : String.format("%02x%02x%02x%02x", v.getRed(), v.getGreen(), v.getBlue(),
                                    v.getAlpha());
            System.out.println(c.name() + "," + c.getUIManagerKey() + "," + hex);
        }
    }
}
