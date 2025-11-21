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
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;

import org.omegat.util.Platform;
import org.omegat.util.Preferences;

public final class FontUtil {

    private FontUtil() {
    }

    /**
     * Get default unscaled font.
     * @return default font.
     */
    public static FontUIResource getDefaultFont() {
        int fontSize = Preferences.getPreferenceDefault(Preferences.TF_SRC_FONT_SIZE,
                Preferences.TF_FONT_SIZE_DEFAULT);
        return createCompositeFont(getPlatformDefaultFontFamily(), Font.PLAIN, fontSize);
    }

    public static String getConfiguredFontName() {
        return Preferences.getPreferenceDefault(Preferences.TF_SRC_FONT_NAME, getPlatformDefaultFontFamily());
    }

    /**
     * Get default font scaled.
     *
     * @return default scaled font as FontUIResource.
     */
    public static FontUIResource getScaledFont() {
        return getFont(UIScale.scale(getConfiguredFontSize()));
    }

    private static FontUIResource getFont(int fontSize) {
        String fontName = Preferences.getPreferenceDefault(Preferences.TF_SRC_FONT_NAME, getPlatformDefaultFontFamily());
        return createCompositeFont(fontName, Font.PLAIN, fontSize);
    }

    public static int getConfiguredFontSize() {
        return Preferences.getPreferenceDefault(Preferences.TF_SRC_FONT_SIZE, Preferences.TF_FONT_SIZE_DEFAULT);
    }

    public static FontUIResource createCompositeFont(String family, int style, int size) {
        // using StyleContext.getFont() here because it uses
        // sun.font.FontUtilities.getCompositeFontUIResource()
        // and creates a composite font that is able to display all Unicode
        // characters
        Font font = StyleContext.getDefaultStyleContext().getFont(family, style, size);
        return (font instanceof FontUIResource) ? (FontUIResource) font : new FontUIResource(font);
    }

    /**
     * Resolve a sensible default UI font family for the current platform.
     * Falls back to Preferences.TF_FONT_DEFAULT if none of the candidates are available.
     */
    private static String getPlatformDefaultFontFamily() {
        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        List<String> available = Arrays.asList(families);
        if (Platform.isWindows) {
            return pickFirstAvailable(available, Arrays.asList("Segoe UI", "Arial", Preferences.TF_FONT_DEFAULT));
        } else if (Platform.isMacOSX()) {
            // On macOS, the system UI font is San Francisco, exposed with ".SF NS Text" names.
            return pickFirstAvailable(available, Arrays.asList(".AppleSystemUIFont", ".SF NS Text", ".SF NS Display", "Helvetica Neue",
                    "Helvetica", "Arial", Preferences.TF_FONT_DEFAULT));
        } else if (Platform.isLinux() || Platform.isBSD() || Platform.isUnixLike()) {
            return pickFirstAvailable(available,
                    Arrays.asList("Noto Sans", "DejaVu Sans", "Ubuntu", "Cantarell", "Liberation Sans",
                            Preferences.TF_FONT_DEFAULT));
        } else {
            return Preferences.TF_FONT_DEFAULT;
        }
    }

    private static String pickFirstAvailable(List<String> available, List<String> candidates) {
        for (String name : candidates) {
            if (available.contains(name)) {
                return name;
            }
        }
        return Preferences.TF_FONT_DEFAULT;
    }
}
