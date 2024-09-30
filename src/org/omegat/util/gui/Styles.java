/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Aaron Madlon-Kay
               2012 Aaron Madlon-Kay
               2014 Briac Pilpre
               2015 Aaron Madlon-Kay
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
import java.util.MissingResourceException;

import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(EditorColor.class);

    private Styles() {
    }

    public enum EditorColor {
        // Also used for EditorPane.background
        COLOR_BACKGROUND(UIManager.getColor("TextPane.background")),

        COLOR_FOREGROUND(UIManager.getColor("TextPane.foreground")),

        COLOR_ACTIVE_SOURCE(UIManager.getColor("OmegaT.activeSource")),

        COLOR_ACTIVE_SOURCE_FG((Color) null),

        COLOR_ACTIVE_TARGET((Color) null),

        COLOR_ACTIVE_TARGET_FG((Color) null),

        COLOR_SEGMENT_MARKER_FG((Color) null),

        COLOR_SEGMENT_MARKER_BG((Color) null),

        COLOR_SOURCE(UIManager.getColor("OmegaT.source")),

        COLOR_SOURCE_FG((Color) null),

        COLOR_NOTED(UIManager.getColor("OmegaT.noted")),

        COLOR_NOTED_FG((Color) null),

        COLOR_UNTRANSLATED(UIManager.getColor("OmegaT.untranslated")),

        COLOR_UNTRANSLATED_FG((Color) null),

        COLOR_TRANSLATED(UIManager.getColor("OmegaT.translated")),

        COLOR_TRANSLATED_FG((Color) null),

        COLOR_NON_UNIQUE(UIManager.getColor("OmegaT.nonUnique")),

        COLOR_NON_UNIQUE_BG((Color) null),

        COLOR_MOD_INFO((Color) null),

        COLOR_MOD_INFO_FG((Color) null),

        COLOR_PLACEHOLDER(UIManager.getColor("OmegaT.placeholder")),

        COLOR_REMOVETEXT_TARGET(UIManager.getColor("OmegaT.removeTextTarget")),

        COLOR_NBSP(UIManager.getColor("OmegaT.nbsp")),

        COLOR_WHITESPACE(UIManager.getColor("OmegaT.whiteSpace")),

        COLOR_BIDIMARKERS(UIManager.getColor("OmegaT.bidiMarkers")),

        COLOR_PARAGRAPH_START(UIManager.getColor("OmegaT.paragraphStart")),

        COLOR_MARK_COMES_FROM_TM(UIManager.getColor("OmegaT.markComesFromTm")),

        COLOR_MARK_COMES_FROM_TM_XICE(UIManager.getColor("OmegaT.markComesFromTmXice")),

        COLOR_MARK_COMES_FROM_TM_X100PC(UIManager.getColor("OmegaT.markComesFromTmX100pc")),

        COLOR_MARK_COMES_FROM_TM_XAUTO(UIManager.getColor("OmegaT.markComesFromTmXauto")),

        //COLOR_MARK_COMES_FROM_TM_XENFORCED(UIManager.getColor("OmegaT.markComesFromTmXenforced")),
        COLOR_LOCKED_SEGMENT(UIManager.getColor("OmegaT.markComesFromTmXenforced")),

        COLOR_MARK_ALT_TRANSLATION(UIManager.getColor("OmegaT.markAltTranslations")),

        COLOR_REPLACE(UIManager.getColor("OmegaT.replace")),

        COLOR_LANGUAGE_TOOLS(UIManager.getColor("OmegaT.languageTools")),

        COLOR_TRANSTIPS(UIManager.getColor("OmegaT.transTips")),

        COLOR_SPELLCHECK(UIManager.getColor("OmegaT.spellCheck")),

        COLOR_TERMINOLOGY(UIManager.getColor("OmegaT.terminology")),

        COLOR_MATCHES_CHANGED(UIManager.getColor("OmegaT.matchesChanged")),

        COLOR_MATCHES_UNCHANGED(UIManager.getColor("OmegaT.matchesUnchanged")),

        COLOR_GLOSSARY_SOURCE((Color) null),

        COLOR_GLOSSARY_TARGET((Color) null),

        COLOR_GLOSSARY_NOTE((Color) null),

        COLOR_MATCHES_DEL_ACTIVE((Color) null),

        COLOR_MATCHES_DEL_INACTIVE((Color) null),

        COLOR_MATCHES_INS_ACTIVE(UIManager.getColor("OmegaT.matchesInsActive")),

        COLOR_MATCHES_INS_INACTIVE(UIManager.getColor("OmegaT.matchesInsInactive")),

        COLOR_HYPERLINK(UIManager.getColor("OmegaT.hyperlink")),

        COLOR_SEARCH_FOUND_MARK(UIManager.getColor("OmegaT.searchFoundMark")),

        COLOR_SEARCH_REPLACE_MARK(UIManager.getColor("OmegaT.searchReplaceMark")),

        COLOR_NOTIFICATION_MIN(UIManager.getColor("OmegaT.notificationMin")),

        COLOR_NOTIFICATION_MAX(UIManager.getColor("OmegaT.notificationMax")),

        COLOR_ALIGNER_ACCEPTED(UIManager.getColor("OmegaT.alignerAccepted")),

        COLOR_ALIGNER_NEEDSREVIEW(UIManager.getColor("OmegaT.alignerNeedsReview")),

        COLOR_ALIGNER_HIGHLIGHT(UIManager.getColor("OmegaT.alignerHighlight")),

        COLOR_ALIGNER_TABLE_ROW_HIGHLIGHT(UIManager.getColor("OmegaT.alignerTableRowHighlight")),

        COLOR_MACHINETRANSLATE_SELECTED_HIGHLIGHT(
                UIManager.getColor("OmegaT.machinetranslateSelectedHighlight"));

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
                    LOGGER.atDebug().setMessage("Cannot set custom color for {}, default to {}.")
                            .addArgument(this::name).addArgument(prefColor).log();
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
                LOGGER.atInfo().log("", ex);
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

        MutableAttributeSet r = (MutableAttributeSet) createAttributeSet(foregroundColor, backgroundColor,
                bold, italic);

        if (strikethrough != null) {
            StyleConstants.setStrikeThrough(r, strikethrough);
        }
        if (underline != null) {
            StyleConstants.setUnderline(r, underline);
        }

        return r;
    }
}
