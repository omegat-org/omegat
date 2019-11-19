/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Aaron Madlon-Kay
               2012 Aaron Madlon-Kay
               2014 Briac Pilpre
               2015 Aaron Madlon-Kay
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

import java.awt.Color;
import java.util.MissingResourceException;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Static attributes for text.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Briac Pilpre
 */
public final class Styles {
    private static final Logger LOGGER = Logger.getLogger(EditorColor.class.getName());

    private Styles() {
    }

    public enum EditorColor {
        COLOR_BACKGROUND(UIManager.getColor("TextPane.background")), // Also used for EditorPane.background
        COLOR_FOREGROUND(UIManager.getColor("TextPane.foreground")),

        COLOR_ACTIVE_SOURCE("#c0ffc0"),
        COLOR_ACTIVE_SOURCE_FG((Color) null),
        COLOR_ACTIVE_TARGET((Color) null),
        COLOR_ACTIVE_TARGET_FG((Color) null),
        COLOR_SEGMENT_MARKER_FG((Color) null),
        COLOR_SEGMENT_MARKER_BG((Color) null),
        COLOR_SOURCE("#c0ffc0"),
        COLOR_SOURCE_FG((Color) null),
        COLOR_NOTED("#c0ffff"),
        COLOR_NOTED_FG((Color) null),
        COLOR_UNTRANSLATED("#c0c0ff"),
        COLOR_UNTRANSLATED_FG((Color) null),
        COLOR_TRANSLATED("#ffff99"),
        COLOR_TRANSLATED_FG((Color) null),
        COLOR_NON_UNIQUE("#808080"),
        COLOR_NON_UNIQUE_BG((Color) null),
        COLOR_MOD_INFO((Color) null),
        COLOR_MOD_INFO_FG((Color) null),
        COLOR_PLACEHOLDER("#969696"),
        COLOR_REMOVETEXT_TARGET("#ff0000"),
        COLOR_NBSP("#c8c8c8"),
        COLOR_WHITESPACE("#808080"),
        COLOR_BIDIMARKERS("#c80000"),
        COLOR_PARAGRAPH_START("#aeaeae"),
        COLOR_MARK_COMES_FROM_TM("#fa8072"), // Salmon red
        COLOR_MARK_COMES_FROM_TM_XICE("#af76df"), // Purple
        COLOR_MARK_COMES_FROM_TM_X100PC("#ff9408"), // Dark Orange
        COLOR_MARK_COMES_FROM_TM_XAUTO("#ffd596"), // Orange
        COLOR_MARK_COMES_FROM_TM_XENFORCED("#ffccff"), // Pink
        COLOR_REPLACE("#0000ff"), // Blue
        COLOR_LANGUAGE_TOOLS("#0000ff"),
        COLOR_TRANSTIPS("#0000ff"),
        COLOR_SPELLCHECK("#ff0000"),
        COLOR_TERMINOLOGY(Color.ORANGE),
        COLOR_MATCHES_CHANGED("#0000ff"),
        COLOR_MATCHES_UNCHANGED("#00ff00"),
        COLOR_MATCHES_DEL_ACTIVE((Color) null),
        COLOR_MATCHES_DEL_INACTIVE((Color) null),
        COLOR_MATCHES_INS_ACTIVE("#0000ff"),
        COLOR_MATCHES_INS_INACTIVE("#808080"), // Color.gray
        COLOR_HYPERLINK("#0000ff"), // Blue
        COLOR_SEARCH_FOUND_MARK(Color.BLUE),
        COLOR_SEARCH_REPLACE_MARK("#ff9900"), // Dark orange
        COLOR_NOTIFICATION_MIN("#fff2d4"), // Light orange
        COLOR_NOTIFICATION_MAX("#ff9900"), // Dark orange
        COLOR_ALIGNER_ACCEPTED("#15bb45"), // Green
        COLOR_ALIGNER_NEEDSREVIEW(Color.RED),
        COLOR_ALIGNER_HIGHLIGHT(Color.YELLOW),
        COLOR_ALIGNER_TABLE_ROW_HIGHLIGHT("#c8c8c8"); // Gray

        private static final String DEFAULT_COLOR = "__DEFAULT__";
        private Color color;
        private Color defaultColor;

        EditorColor(Color defaultColor) {
            this.color = defaultColor;
            this.defaultColor = defaultColor;

            String prefColor = Preferences.getPreferenceDefault(name(), null);
            if (prefColor != null && !DEFAULT_COLOR.equals(prefColor)) {
                try {
                    this.color = Color.decode(prefColor);
                } catch (NumberFormatException e) {
                    Log.logDebug(LOGGER, "Cannot set custom color for {0}, default to {1}.", name(),
                            prefColor);
                }
            }
        }

        EditorColor(String defaultColor) {
            this(Color.decode(defaultColor));
        }

        public String toHex() {
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        }

        public Color getColor() {
            return color;
        }

        public Color getDefault() {
            return defaultColor;
        }

        public String getDisplayName() {
            try {
                return OStrings.getString(name());
            } catch (MissingResourceException ex) {
                Log.log(ex);
                return name();
            }
        }

        public void setColor(Color newColor) {
            if (newColor == null || newColor.equals(defaultColor)) {
                color = defaultColor;
                Preferences.setPreference(name(), DEFAULT_COLOR);
            } else {
                color = newColor;
                Preferences.setPreference(name(), toHex());
            }
        }
    }

    /**
     * Construct required attributes set.
     *
     * Since we need many attributes combinations, it's not good idea to have
     * variable to each attributes set. There is no sense to store created
     * attributes in the cache, because calculate hash for cache require about
     * 2-3 time more than just create attributes set from scratch.
     *
     * 1000000 attributes creation require about 305 ms - it's enough fast.
     */
    public static AttributeSet createAttributeSet(Color foregroundColor, Color backgroundColor, Boolean bold,
            Boolean italic) {
        MutableAttributeSet r = new SimpleAttributeSet();
        if (foregroundColor != null) {
            StyleConstants.setForeground(r, foregroundColor);
        }

        if (backgroundColor != null) {
            StyleConstants.setBackground(r, backgroundColor);
        }
        if (bold != null) {
            StyleConstants.setBold(r, bold);
        }
        if (italic != null) {
            StyleConstants.setItalic(r, italic);
        }
        return r;
    }

    public static AttributeSet createAttributeSet(Color foregroundColor, Color backgroundColor, Boolean bold,
            Boolean italic, Boolean strikethrough, Boolean underline) {

        MutableAttributeSet r = (MutableAttributeSet) createAttributeSet(foregroundColor, backgroundColor, bold,
                italic);

        if (strikethrough != null) {
            StyleConstants.setStrikeThrough(r, strikethrough);
        }
        if (underline != null) {
            StyleConstants.setUnderline(r, underline);
        }

        return r;
    }
}
