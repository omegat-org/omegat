/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class FontFallbackManager {

    /**
     * List of fonts that are not supported. All font names
     * must be suffixed with ";" (even the last one).
     * <p>
     * Apple Color Emoji is blacklisted because it requires Apple-specific
     * rendering techniques that Swing does not support.
     * <p>
     * Known working emoji fonts:
     * <ul><li>Segoe UI Emoji (bundled with Windows 7 and later)
     * <li><a href="https://github.com/googlei18n/noto-emoji">Noto Emoji</a>
     * </ul>
     */
    private static final String FONT_BLACKLIST = "Apple Color Emoji;";
    private static final Font FONT_UNAVAILABLE = new Font("", 0, 0);
    
    private static final Logger LOGGER = Logger.getLogger(FontFallbackManager.class.getName());
    
    private static Font[] recentFonts = new Font[8];
    private static int lastFontIndex = 0;
    private static Map<Integer, Font> cache = new ConcurrentHashMap<Integer, Font>();
    
    public static Font getCapableFont(int cp) {
        // Skip variation selectors
        if (cp >= '\uFE00' && cp <= '\uFE0F') {
            return null;
        }
        if (cache.isEmpty()) {
            // Prevent concurrent accesses just the first time, so that we don't
            // get multiple expensive full-searches of the font list.
            synchronized (cache) {
                return getCapableFontInternal(cp);
            }
        } else {
            return getCapableFontInternal(cp);
        }
    }
    
    private static Font getCapableFontInternal(int cp) {
        // Iterate backwards through recent fonts.
        // Presumably, most fallback chars in a given document will be the same
        // language/script/etc. so they are likely to be included in the same font.
        for (int testIndex, i = 0; i < recentFonts.length; i++) {
            testIndex = (lastFontIndex - i + recentFonts.length) % recentFonts.length;
            Font font = recentFonts[testIndex];
            if (font != null && font.canDisplay(cp)) {
                lastFontIndex = testIndex;
                cache.put(cp, font);
                return font;
            }
        }
        // Try cache in case we've seen this codepoint before.
        Font cachedFont = cache.get(cp);
        if (cachedFont == FONT_UNAVAILABLE) {
            return null;
        }
        if (cachedFont != null) {
            addRecentFont(cachedFont);
            return cachedFont;
        }
        // All we can do now is do a brute-force full search of available fonts.
        LOGGER.fine("Searching for font supporting U+" + Integer.toHexString(cp) + " " + String.valueOf(Character.toChars(cp)));
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (Font font : ge.getAllFonts()) {
            if (font.canDisplay(cp) && !FONT_BLACKLIST.contains(font.getFamily() + ";")) {
                LOGGER.fine("Search found " + font.getFamily());
                cache.put(cp, font);
                addRecentFont(font);
                return font;
            }
        }
        cache.put(cp, FONT_UNAVAILABLE);
        return null;
    }
    
    private static void addRecentFont(Font font) {
        lastFontIndex = (lastFontIndex + 1) % recentFonts.length;
        recentFonts[lastFontIndex] = font;
    }
}
