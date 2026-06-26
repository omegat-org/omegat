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

import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jspecify.annotations.Nullable;
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

    private Styles() {
    }

    public enum EditorColor {
        /**
         * Background color.
         * <p>
         * Also used for EditorPane.background
         */
        COLOR_BACKGROUND(OStrings.getString("COLOR_BACKGROUND"), UIManager.getColor("TextPane.background")),
        /**
         * Foreground color.
         */
        COLOR_FOREGROUND(OStrings.getString("COLOR_FOREGROUND"), UIManager.getColor("TextPane.foreground")),
        /**
         * Active source text background.
         */
        COLOR_ACTIVE_SOURCE(OStrings.getString("COLOR_ACTIVE_SOURCE"),
                UIManager.getColor("OmegaT.activeSource")),
        /**
         * Active source text foreground.
         */
        COLOR_ACTIVE_SOURCE_FG(OStrings.getString("COLOR_ACTIVE_SOURCE_FG")),
        /**
         * Active target text background.
         */
        COLOR_ACTIVE_TARGET(OStrings.getString("COLOR_ACTIVE_TARGET")),
        /**
         * Active target text foreground.
         */
        COLOR_ACTIVE_TARGET_FG(OStrings.getString("COLOR_ACTIVE_TARGET_FG")),
        /**
         * Segment marker foreground color.
         */
        COLOR_SEGMENT_MARKER_FG(OStrings.getString("COLOR_SEGMENT_MARKER_FG")),
        /**
         * SEgment marker background color.
         */
        COLOR_SEGMENT_MARKER_BG(OStrings.getString("COLOR_SEGMENT_MARKER_BG")),
        /**
         * source text background.
         */
        COLOR_SOURCE(OStrings.getString("COLOR_SOURCE"), UIManager.getColor("OmegaT.source")),
        /**
         * source text foreground.
         */
        COLOR_SOURCE_FG(OStrings.getString("COLOR_SOURCE_FG")),
        /**
         * noted segment background.
         */
        COLOR_NOTED(OStrings.getString("COLOR_NOTED"), UIManager.getColor("OmegaT.noted")),
        /**
         * noted segment foreground.
         */
        COLOR_NOTED_FG(OStrings.getString("COLOR_NOTED_FG")),
        /**
         * untranslated segment background.
         */
        COLOR_UNTRANSLATED(OStrings.getString("COLOR_UNTRANSLATED"),
                UIManager.getColor("OmegaT.untranslated")),
        /**
         * untranslated segment foreground.
         */
        COLOR_UNTRANSLATED_FG(OStrings.getString("COLOR_UNTRANSLATED_FG")),
        /**
         * translated segment background.
         */
        COLOR_TRANSLATED(OStrings.getString("COLOR_TRANSLATED"), UIManager.getColor("OmegaT.translated")),
        /**
         * translated segment text.
         */
        COLOR_TRANSLATED_FG(OStrings.getString("COLOR_TRANSLATED_FG")),
        /**
         * non unique entry text.
         */
        COLOR_NON_UNIQUE(OStrings.getString("COLOR_NON_UNIQUE"), UIManager.getColor("OmegaT.nonUnique")),
        /**
         * non unique entry background.
         */
        COLOR_NON_UNIQUE_BG(OStrings.getString("COLOR_NON_UNIQUE_BG")),
        /**
         * Modification information background.
         */
        COLOR_MOD_INFO(OStrings.getString("COLOR_MOD_INFO")),
        /**
         * Modification information text.
         */
        COLOR_MOD_INFO_FG(OStrings.getString("COLOR_MOD_INFO_FG")),
        /**
         * Tags placeholder color.
         */
        COLOR_PLACEHOLDER(OStrings.getString("COLOR_PLACEHOLDER"), UIManager.getColor("OmegaT.placeholder")),
        /**
         * Flagged text target color.
         */
        COLOR_REMOVETEXT_TARGET(OStrings.getString("COLOR_REMOVETEXT_TARGET"),
                UIManager.getColor("OmegaT.removeTextTarget")),
        /**
         * Non-breakable space character background.
         */
        COLOR_NBSP(OStrings.getString("COLOR_NBSP"), UIManager.getColor("OmegaT.nbsp")),
        /**
         * White space marker background color.
         */
        COLOR_WHITESPACE(OStrings.getString("COLOR_WHITESPACE"), UIManager.getColor("OmegaT.whiteSpace")),
        /**
         * Bidirectional control characters background color.
         */
        COLOR_BIDIMARKERS(OStrings.getString("COLOR_BIDIMARKERS"), UIManager.getColor("OmegaT.bidiMarkers")),
        /**
         * Paragraph start delimitation background color.
         */
        COLOR_PARAGRAPH_START(OStrings.getString("COLOR_PARAGRAPH_START"),
                UIManager.getColor("OmegaT.paragraphStart")),
        /**
         * The background color of a segment comes from MT memory.
         */
        COLOR_MARK_COMES_FROM_TM_MT(OStrings.getString("COLOR_MARK_COMES_FROM_TM_MT"),
                UIManager.getColor("OmegaT.markComesFromTmMt")),
        /**
         * The background color of a segment comes from ICE memory.
         */
        COLOR_MARK_COMES_FROM_TM_XICE(OStrings.getString("COLOR_MARK_COMES_FROM_TM_XICE"),
                UIManager.getColor("OmegaT.markComesFromTmXice")),
        /**
         * The background color of a segment comes from 100% memory.
         */
        COLOR_MARK_COMES_FROM_TM_X100PC(OStrings.getString("COLOR_MARK_COMES_FROM_TM_X100PC"),
                UIManager.getColor("OmegaT.markComesFromTmX100pc")),
        /**
         * The background color of a segment comes from auto memory.
         */
        COLOR_MARK_COMES_FROM_TM_XAUTO(OStrings.getString("COLOR_MARK_COMES_FROM_TM_XAUTO"),
                UIManager.getColor("OmegaT.markComesFromTmXauto")),
        /**
         * The background color of a segment comes from enforced memroy.
         */
        COLOR_MARK_COMES_FROM_TM_XENFORCED(OStrings.getString("COLOR_MARK_COMES_FROM_TM_XENFORCED"),
                UIManager.getColor("OmegaT.markComesFromTmXenforced")),
        /**
         * Alternative translation highlight color.
         */
        COLOR_MARK_ALT_TRANSLATION(OStrings.getString("COLOR_MARK_ALT_TRANSLATION"),
                UIManager.getColor("OmegaT.markAltTranslations")),
        /**
         * Replace background color.
         */
        COLOR_REPLACE(OStrings.getString("COLOR_REPLACE"), UIManager.getColor("OmegaT.replace")),
        /**
         * Language checker suggestion highlight color.
         */
        COLOR_LANGUAGE_TOOLS(OStrings.getString("COLOR_LANGUAGE_TOOLS"),
                UIManager.getColor("OmegaT.languageTools")),
        /**
         * Glossary matches highlight color.
         */
        COLOR_TRANSTIPS(OStrings.getString("COLOR_TRANSTIPS"), UIManager.getColor("OmegaT.transTips")),
        /**
         * Spellcheck suggestion highlight color.
         */
        COLOR_SPELLCHECK(OStrings.getString("COLOR_SPELLCHECK"), UIManager.getColor("OmegaT.spellCheck")),
        /**
         * Terminology suggestion highlight color.
         */
        COLOR_TERMINOLOGY(OStrings.getString("COLOR_TERMINOLOGY"), UIManager.getColor("OmegaT.terminology")),
        /**
         * Matches changed words background color.
         */
        COLOR_MATCHES_CHANGED(OStrings.getString("COLOR_MATCHES_CHANGED"),
                UIManager.getColor("OmegaT.matchesChanged")),
        /**
         * Matches unchanged words background color.
         */
        COLOR_MATCHES_UNCHANGED(OStrings.getString("COLOR_MATCHES_UNCHANGED"),
                UIManager.getColor("OmegaT.matchesUnchanged")),
        /**
         * Glossary source background color.
         */
        COLOR_GLOSSARY_SOURCE(OStrings.getString("COLOR_GLOSSARY_SOURCE")),
        /**
         * Glossary target background color.
         */
        COLOR_GLOSSARY_TARGET(OStrings.getString("COLOR_GLOSSARY_TARGET")),
        /**
         * Glossary note background color.
         */
        COLOR_GLOSSARY_NOTE(OStrings.getString("COLOR_GLOSSARY_NOTE")),
        /**
         * Matches deleted active background color.
         */
        COLOR_MATCHES_DEL_ACTIVE(OStrings.getString("COLOR_MATCHES_DEL_ACTIVE")),
        /**
         * Matches deleted inactive background color.
         */
        COLOR_MATCHES_DEL_INACTIVE(OStrings.getString("COLOR_MATCHES_DEL_INACTIVE")),
        /**
         * Matches inserted active background color.
         */
        COLOR_MATCHES_INS_ACTIVE(OStrings.getString("COLOR_MATCHES_INS_ACTIVE"),
                UIManager.getColor("OmegaT.matchesInsActive")),
        /**
         * Matches inserted inactive background color.
         */
        COLOR_MATCHES_INS_INACTIVE(OStrings.getString("COLOR_MATCHES_INS_INACTIVE"),
                UIManager.getColor("OmegaT.matchesInsInactive")),
        /**
         * Hyperlink highlight color.
         */
        COLOR_HYPERLINK(OStrings.getString("COLOR_HYPERLINK"), UIManager.getColor("OmegaT.hyperlink")),
        /**
         * Search found mark highlight color.
         */
        COLOR_SEARCH_FOUND_MARK(OStrings.getString("COLOR_SEARCH_FOUND_MARK"),
                UIManager.getColor("OmegaT.searchFoundMark")),
        /**
         * Search replace mark highlight color.
         */
        COLOR_SEARCH_REPLACE_MARK(OStrings.getString("COLOR_SEARCH_REPLACE_MARK"),
                UIManager.getColor("OmegaT.searchReplaceMark")),
        /**
         * Notification (steady) color.
         */
        COLOR_NOTIFICATION_MIN(OStrings.getString("COLOR_NOTIFICATION_MIN"),
                UIManager.getColor("OmegaT.notificationMin")),
        /**
         * Notification (flash) color.
         */
        COLOR_NOTIFICATION_MAX(OStrings.getString("COLOR_NOTIFICATION_MAX"),
                UIManager.getColor("OmegaT.notificationMax")),
        /**
         * Aligner "accepted" group color.
         */
        COLOR_ALIGNER_ACCEPTED(OStrings.getString("COLOR_ALIGNER_ACCEPTED"),
                UIManager.getColor("OmegaT.alignerAccepted")),
        /**
         * Aligner "needs review" group color.
         */
        COLOR_ALIGNER_NEEDSREVIEW(OStrings.getString("COLOR_ALIGNER_NEEDSREVIEW"),
                UIManager.getColor("OmegaT.alignerNeedsReview")),
        /**
         * Aligner highlight color.
         */
        COLOR_ALIGNER_HIGHLIGHT(OStrings.getString("COLOR_ALIGNER_HIGHLIGHT"),
                UIManager.getColor("OmegaT.alignerHighlight")),
        /**
         * Aligner table row highlight color.
         */
        COLOR_ALIGNER_TABLE_ROW_HIGHLIGHT(OStrings.getString("COLOR_ALIGNER_TABLE_ROW_HIGHLIGHT"),
                UIManager.getColor("OmegaT.alignerTableRowHighlight")),
        /**
         * Aligner table selected row highlight.
         */
        COLOR_MACHINETRANSLATE_SELECTED_HIGHLIGHT(
                OStrings.getString("COLOR_MACHINETRANSLATE_SELECTED_HIGHLIGHT"),
                UIManager.getColor("OmegaT.machinetranslateSelectedHighlight"));

        private static final String DEFAULT_COLOR = "__DEFAULT__";

        private final String displayName;
        private final Color defaultColor;
        private Color color;

        EditorColor(String displayName, Color defaultColor) {
            this.displayName = displayName;
            this.color = defaultColor;
            this.defaultColor = defaultColor;
            setColorFromPreference();
        }

        EditorColor(String displayName, String defaultColor) {
            this(displayName, Color.decode(defaultColor));
        }

        EditorColor(String displayName) {
            this.displayName = displayName;
            this.color = null;
            this.defaultColor = null;
            setColorFromPreference();
        }

        private void setColorFromPreference() {
            String prefColor = Preferences.getPreferenceDefault(name(), null);
            if (prefColor != null && !DEFAULT_COLOR.equals(prefColor)) {
                try {
                    this.color = Color.decode(prefColor);
                } catch (NumberFormatException e) {
                    Log.logWarningRB("PREFS_COLOR_VALUE_PARSE_ERROR", displayName, prefColor);
                }
            }
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
            return displayName;
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
     * <p>
     * Since we need many attributes combinations, it's not a good idea to have
     * variable to each attribute set. There is no sense to store created
     * attributes in the cache, because calculate hash for cache requires about
     * 2-3 time more than create attributes set from scratch.
     * <p>
     * 1000000 attributes creation requires about 305 ms - it's enough fast.
     */
    public static AttributeSet createAttributeSet(@Nullable Color foregroundColor, @Nullable Color backgroundColor,
                                                  @Nullable Boolean bold, @Nullable Boolean italic) {
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

    public static AttributeSet createAttributeSet(@Nullable Color foregroundColor, @Nullable Color backgroundColor,
                                                  @Nullable Boolean bold, @Nullable Boolean italic,
                                                  @Nullable Boolean strikethrough, @Nullable Boolean underline) {

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
