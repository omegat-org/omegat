/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class FontFallbackManager {

    private FontFallbackManager() {
    }

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
    private static final Set<String> FONT_BLACKLIST = Collections.singleton("Apple Color Emoji");
    private static final Font FONT_UNAVAILABLE = new Font("", 0, 0);

    private static final Logger LOGGER = Logger.getLogger(FontFallbackManager.class.getName());

    private static final Font[] RECENT_FONTS = new Font[8];
    private static int lastFontIndex = 0;
    private static final Map<Integer, Font> CACHE = new ConcurrentHashMap<>();

    public static Font getCapableFont(int cp) {
        // Skip variation selectors
        if (cp >= '\uFE00' && cp <= '\uFE0F') {
            return null;
        }
        if (CACHE.isEmpty()) {
            // Prevent concurrent accesses just the first time, so that we don't
            // get multiple expensive full-searches of the font list.
            synchronized (CACHE) {
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
        for (int testIndex, i = 0; i < RECENT_FONTS.length; i++) {
            testIndex = (lastFontIndex - i + RECENT_FONTS.length) % RECENT_FONTS.length;
            Font font = RECENT_FONTS[testIndex];
            if (font != null && font.canDisplay(cp)) {
                lastFontIndex = testIndex;
                CACHE.put(cp, font);
                return font;
            }
        }
        // Try cache in case we've seen this codepoint before.
        Font cachedFont = CACHE.get(cp);
        if (cachedFont == FONT_UNAVAILABLE) {
            return null;
        }
        if (cachedFont != null) {
            addRecentFont(cachedFont);
            return cachedFont;
        }
        // All we can do now is do a brute-force full search of available fonts.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = ge.getAllFonts();
        LOGGER.fine(() -> String.format("Searching %d fonts for one supporting U+%h %s", allFonts.length, cp,
                String.valueOf(Character.toChars(cp))));
        long start = System.currentTimeMillis();
        Optional<Font> font = Stream.of(allFonts).parallel().filter(f -> {
            return f.canDisplay(cp) && !FONT_BLACKLIST.contains(f.getFamily());
        }).findFirst();
        CACHE.put(cp, font.orElse(FONT_UNAVAILABLE));
        font.ifPresent(FontFallbackManager::addRecentFont);
        LOGGER.fine(() -> font.isPresent()
                ? String.format("Search found %s in %d ms", font.get().getFamily(),
                        System.currentTimeMillis() - start)
                : String.format("Search failed to find a font; time: %d ms",
                        System.currentTimeMillis() - start));
        return font.orElse(null);
    }

    private static void addRecentFont(Font font) {
        lastFontIndex = (lastFontIndex + 1) % RECENT_FONTS.length;
        RECENT_FONTS[lastFontIndex] = font;
    }
}
