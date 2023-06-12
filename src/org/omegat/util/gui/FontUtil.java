/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                2022 FormDev Software GmbH
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.util.gui;

import java.awt.Font;

import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;

import org.omegat.util.Preferences;

public final class FontUtil {

    public static FontUIResource getDefaultFont() {
        int fontSize = Preferences.getPreferenceDefault(Preferences.TF_SRC_FONT_SIZE,
                Preferences.TF_FONT_SIZE_DEFAULT);
        return createCompositeFont(Preferences.TF_FONT_DEFAULT, Font.PLAIN, fontSize);
    }

    /**
     * Get default font.
     * 
     * @return default font as FontUIResource.
     */
    public static FontUIResource getFont() {
        FontUIResource font;

        String fontName = Preferences.getPreferenceDefault(Preferences.TF_SRC_FONT_NAME, null);
        if (fontName != null) {
            int fontSize = UIScale.scale(Preferences.getPreferenceDefault(Preferences.TF_SRC_FONT_SIZE,
                    Preferences.TF_FONT_SIZE_DEFAULT));
            font = new FontUIResource(new Font(fontName, Font.PLAIN, fontSize));
        } else {
            font = getDefaultFont();
        }
        return font;
    }

    private static FontUIResource createCompositeFont(String family, int style, int size) {
        // using StyleContext.getFont() here because it uses
        // sun.font.FontUtilities.getCompositeFontUIResource()
        // and creates a composite font that is able to display all Unicode
        // characters
        Font font = StyleContext.getDefaultStyleContext().getFont(family, style, size);
        return (font instanceof FontUIResource) ? (FontUIResource) font : new FontUIResource(font);
    }
}
