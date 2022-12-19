/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2012 Martin Fleurke, Hans-Peter Jacobs
               2015 Aaron Madlon-Kay
               2019 Briac Pilpre
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

package org.omegat.gui.editor;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.text.AttributeSet;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry.DUPLICATE;
import org.omegat.core.spellchecker.SpellCheckerMarker;
import org.omegat.util.Preferences;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Editor behavior control settings.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Hans-Peter Jacobs
 * @author Aaron Madlon-Kay
 */
public class EditorSettings implements IEditorSettings {
    private final EditorController parent;

    private boolean useTabForAdvance;
    private boolean markTranslated;
    private boolean markUntranslated;
    private boolean markAutoPopulated;
    private boolean displaySegmentSources;
    private boolean markNonUniqueSegments;
    private boolean markNoted;
    private boolean markNBSP;
    private boolean markWhitespace;
    private boolean markParagraphDelimitations;
    private boolean markBidi;
    private String displayModificationInfo;
    private boolean autoSpellChecking;
    private boolean viewSourceBold;
    private boolean viewActiveSourceBold;
    private boolean markFirstNonUnique;
    private boolean markGlossaryMatches;
    private boolean markLanguageChecker;
    private boolean doFontFallback;

    public static final String DISPLAY_MODIFICATION_INFO_NONE = "none";
    public static final String DISPLAY_MODIFICATION_INFO_SELECTED = "selected";
    public static final String DISPLAY_MODIFICATION_INFO_ALL = "all";

    private static final boolean MARK_NON_UNIQUE_SEGMENTS_DEFAULT = true;
    private static final boolean MARK_PARA_DELIMITATIONS_DEFAULT = true;
    private static final boolean MARK_AUTOPOPULATED_DEFAULT = true;
    private static final boolean MARK_GLOSSARY_MATCHES_DEFAULT = true;

    protected EditorSettings(final EditorController parent) {
        this.parent = parent;

        //options from menu 'view'
        useTabForAdvance = Preferences.isPreference(Preferences.USE_TAB_TO_ADVANCE);
        markTranslated = Preferences.isPreference(Preferences.MARK_TRANSLATED_SEGMENTS);
        markUntranslated = Preferences.isPreference(Preferences.MARK_UNTRANSLATED_SEGMENTS);
        displaySegmentSources = Preferences.isPreference(Preferences.DISPLAY_SEGMENT_SOURCES);
        markNonUniqueSegments = Preferences.isPreferenceDefault(Preferences.MARK_NON_UNIQUE_SEGMENTS,
               MARK_NON_UNIQUE_SEGMENTS_DEFAULT);
        markNoted = Preferences.isPreference(Preferences.MARK_NOTED_SEGMENTS);
        markNBSP  = Preferences.isPreference(Preferences.MARK_NBSP);
        markParagraphDelimitations = Preferences.isPreferenceDefault(Preferences.MARK_PARA_DELIMITATIONS,
                MARK_PARA_DELIMITATIONS_DEFAULT);
        markWhitespace  = Preferences.isPreference(Preferences.MARK_WHITESPACE);
        markBidi  = Preferences.isPreference(Preferences.MARK_BIDI);
        displayModificationInfo = Preferences.getPreferenceDefault(Preferences.DISPLAY_MODIFICATION_INFO,
                DISPLAY_MODIFICATION_INFO_SELECTED);
        autoSpellChecking = Preferences.isPreference(Preferences.ALLOW_AUTO_SPELLCHECKING);
        markAutoPopulated = Preferences.isPreferenceDefault(Preferences.MARK_AUTOPOPULATED,
                MARK_AUTOPOPULATED_DEFAULT);

        // options from preferences 'view' pane
        viewSourceBold = Preferences.isPreferenceDefault(Preferences.VIEW_OPTION_SOURCE_ALL_BOLD,
                Preferences.VIEW_OPTION_SOURCE_ALL_BOLD_DEFAULT);
        viewActiveSourceBold = Preferences.isPreferenceDefault(Preferences.VIEW_OPTION_SOURCE_ACTIVE_BOLD,
                Preferences.VIEW_OPTION_SOURCE_ACTIVE_BOLD_DEFAULT);
        markFirstNonUnique = Preferences.isPreference(Preferences.VIEW_OPTION_UNIQUE_FIRST);
        markGlossaryMatches = Preferences.isPreferenceDefault(Preferences.MARK_GLOSSARY_MATCHES,
                MARK_GLOSSARY_MATCHES_DEFAULT);
        markLanguageChecker = !Preferences.isPreferenceDefault(Preferences.LT_DISABLED,
                Preferences.LT_DISABLED_DEFAULT);
        doFontFallback = Preferences.isPreference(Preferences.FONT_FALLBACK);
    }

    public char getAdvancerChar() {
        if (useTabForAdvance) {
            return KeyEvent.VK_TAB;
        } else {
            return KeyEvent.VK_ENTER;
        }
    }

    public boolean isUseTabForAdvance() {
        return useTabForAdvance;
    }

    public void setUseTabForAdvance(boolean useTabForAdvance) {
        this.useTabForAdvance = useTabForAdvance;
        Preferences.setPreference(Preferences.USE_TAB_TO_ADVANCE, useTabForAdvance);
    }

    public boolean isMarkTranslated() {
        return markTranslated;
    }

    public void setMarkTranslated(boolean markTranslated) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markTranslated = markTranslated;
        Preferences.setPreference(Preferences.MARK_TRANSLATED_SEGMENTS, markTranslated);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    public boolean isMarkUntranslated() {
        return markUntranslated;
    }

    public boolean isMarkAutoPopulated() {
        return markAutoPopulated;
    }

    public void setMarkAutoPopulated(boolean val) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markAutoPopulated = val;
        Preferences.setPreference(Preferences.MARK_AUTOPOPULATED, markAutoPopulated);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    public void setMarkUntranslated(boolean markUntranslated) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markUntranslated = markUntranslated;
        Preferences.setPreference(Preferences.MARK_UNTRANSLATED_SEGMENTS, markUntranslated);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    /** display the segment sources or not */
    public boolean isDisplaySegmentSources() {
        return displaySegmentSources;
    }

    public boolean isMarkNonUniqueSegments() {
        return markNonUniqueSegments;
    }

    public boolean isHideDuplicateSegments() {
        return true;
    }

    public boolean isMarkNotedSegments() {
        return markNoted;
    }

    /**
     * mark non-breakable spaces?
     *
     * @return true when set, false otherwise
     */
    public boolean isMarkNBSP() {
        return markNBSP;
    }
    /**
     * mark whitespace?
     * @return true when set, false otherwise
     */
    public boolean isMarkWhitespace() {
        return markWhitespace;
    }
    /**
     * mark Bidirectional control characters
     * @return true when set, false otherwise
     */
    public boolean isMarkBidi() {
        return markBidi;
    }

    public boolean isDoFontFallback() {
        return doFontFallback;
    }

    public void setDisplaySegmentSources(boolean displaySegmentSources) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.displaySegmentSources = displaySegmentSources;
        Preferences.setPreference(Preferences.DISPLAY_SEGMENT_SOURCES, displaySegmentSources);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    public void setMarkNonUniqueSegments(boolean markNonUniqueSegments) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markNonUniqueSegments = markNonUniqueSegments;
        Preferences.setPreference(Preferences.MARK_NON_UNIQUE_SEGMENTS, markNonUniqueSegments);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    public void setMarkNotedSegments(boolean markNotedSegments) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markNoted = markNotedSegments;
        Preferences.setPreference(Preferences.MARK_NOTED_SEGMENTS, markNoted);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    public void setMarkNBSP(boolean markNBSP) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markNBSP = markNBSP;
        Preferences.setPreference(Preferences.MARK_NBSP, markNBSP);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }
    public void setMarkWhitespace(boolean markWhitespace) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markWhitespace = markWhitespace;
        Preferences.setPreference(Preferences.MARK_WHITESPACE, markWhitespace);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }
    
    public void setMarkParagraphDelimitations(boolean delimitations) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markParagraphDelimitations = delimitations;
        Preferences.setPreference(Preferences.MARK_PARA_DELIMITATIONS, delimitations);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    public boolean isMarkParagraphDelimitations() {
        return markParagraphDelimitations;
    }

    public void setMarkBidi(boolean markBidi) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markBidi = markBidi;
        Preferences.setPreference(Preferences.MARK_BIDI, markBidi);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    public void setDoFontFallback(boolean doFontFalback) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.doFontFallback = doFontFalback;
        Preferences.setPreference(Preferences.FONT_FALLBACK, doFontFalback);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    @Override
    public boolean isMarkGlossaryMatches() {
        return markGlossaryMatches;
    }

    @Override
    public void setMarkGlossaryMatches(boolean markGlossaryMatches) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markGlossaryMatches = markGlossaryMatches;
        Preferences.setPreference(Preferences.MARK_GLOSSARY_MATCHES, markGlossaryMatches);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    @Override
    public boolean isMarkLanguageChecker() {
        return markLanguageChecker;
    }

    @Override
    public void setMarkLanguageChecker(boolean markLanguageChecker) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markLanguageChecker = markLanguageChecker;
        Preferences.setPreference(Preferences.LT_DISABLED, !markLanguageChecker);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    /**
     * returns the setting for display the modification information or not
     * Either DISPLAY_MODIFICATION_INFO_NONE,
     * DISPLAY_MODIFICATION_INFO_SELECTED, DISPLAY_MODIFICATION_INFO_ALL
     */
    public String getDisplayModificationInfo() {
        return displayModificationInfo;
    }

    /**
     * Sets the setting for display the modification information or not
     *
     * @param displayModificationInfo
     *            Either DISPLAY_MODIFICATION_INFO_NONE ,
     *            DISPLAY_MODIFICATION_INFO_SELECTED ,
     *            DISPLAY_MODIFICATION_INFO_ALL
     */
    public void setDisplayModificationInfo(String displayModificationInfo) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.displayModificationInfo = displayModificationInfo;
        Preferences.setPreference(Preferences.DISPLAY_MODIFICATION_INFO, displayModificationInfo);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    /** need to check spell or not */
    public boolean isAutoSpellChecking() {
        return autoSpellChecking;
    }

    public void setAutoSpellChecking(boolean autoSpellChecking) {
        UIThreadsUtil.mustBeSwingThread();
        if (Core.getProject().isProjectLoaded()) {
            parent.commitAndDeactivate();
        }

        this.autoSpellChecking = autoSpellChecking;

        if (Core.getProject().isProjectLoaded()) {
            // parent.loadDocument();
            parent.activateEntry();
            parent.remarkOneMarker(SpellCheckerMarker.class.getName());
        }
    }

    /**
     * repaint segments in editor according to new view options. Use when options change to make them effective
     * immediately.
     */
    public void updateViewPreferences() {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        //update variables
        viewSourceBold = Preferences.isPreference(Preferences.VIEW_OPTION_SOURCE_ALL_BOLD);
        viewActiveSourceBold = Preferences.isPreference(Preferences.VIEW_OPTION_SOURCE_ACTIVE_BOLD);
        markFirstNonUnique = Preferences.isPreference(Preferences.VIEW_OPTION_UNIQUE_FIRST);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    /**
     * repaint segments in editor according to new view tag validation options. Use when options change to make them
     * effective immediately.
     */
    public void updateTagValidationPreferences() {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        // nothing special to do: tags/placeholders are determined by segment builder and info is passed as argument to
        // getattributeSet.

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    /**
     * Choose segment's attributes based on rules.
     *
     * @param isSource
     *            is it a source segment or a target segment
     * @param isPlaceholder
     *            is it for a placeholder (OmegaT tag or sprintf-variable etc.) or regular text inside the segment?
     * @param isRemoveText
     *            is it text that should be removed from translation?
     * @param duplicate
     *            is the sourceTextEntry a duplicate or not? values: DUPLICATE.NONE, DUPLICATE.FIRST or DUPLICATE.NEXT.
     *            See sourceTextEntryste.getDuplicate()
     * @param active
     *            is it an active segment?
     * @param translationExists
     *            does a translation already exist
     * @param isNBSP
     *            is the text a non-breakable space
     * @return proper AttributeSet to use on displaying the segment.
     */
    public AttributeSet getAttributeSet(boolean isSource, boolean isPlaceholder, boolean isRemoveText,
            DUPLICATE duplicate, boolean active, boolean translationExists, boolean hasNote, boolean isNBSP) {
        // determine foreground color
        Color fg = null;

        // Custom foreground colors
        if (active) {
            if (isSource) {
                fg = Styles.EditorColor.COLOR_ACTIVE_SOURCE_FG.getColor();
            } else {
                fg = Styles.EditorColor.COLOR_ACTIVE_TARGET_FG.getColor();
            }
        } else {
            if (isSource) {
                if (isMarkNotedSegments() && hasNote && !translationExists) {
                    fg = Styles.EditorColor.COLOR_NOTED_FG.getColor();
                } else if (markUntranslated && !translationExists) {
                    fg = Styles.EditorColor.COLOR_UNTRANSLATED_FG.getColor();
                } else if (isDisplaySegmentSources()) {
                    fg = Styles.EditorColor.COLOR_SOURCE_FG.getColor();
                }
            } else {
                if (isMarkNotedSegments() && hasNote) {
                    fg = Styles.EditorColor.COLOR_NOTED_FG.getColor();
                } else if (markTranslated) {
                    fg = Styles.EditorColor.COLOR_TRANSLATED_FG.getColor();
                }
            }
        }
        if (markNonUniqueSegments) {
            switch (duplicate) {
            case NONE:
                break;
            case FIRST:
                if (markFirstNonUnique) {
                    fg = Styles.EditorColor.COLOR_NON_UNIQUE.getColor();
                }
                break;
            case NEXT:
                fg = Styles.EditorColor.COLOR_NON_UNIQUE.getColor();
                break;
            }
        }
        if (isPlaceholder) {
            fg = Styles.EditorColor.COLOR_PLACEHOLDER.getColor();
        }
        if (isRemoveText && !isSource) {
            fg = Styles.EditorColor.COLOR_REMOVETEXT_TARGET.getColor();
        }

        //determine background color
        Color bg = null;
        if (active) {
            if (isSource) {
                bg = Styles.EditorColor.COLOR_ACTIVE_SOURCE.getColor();
            } else {
                bg = Styles.EditorColor.COLOR_ACTIVE_TARGET.getColor();
            }
        } else {
            if (isSource) {
                if (isMarkNotedSegments() && hasNote && !translationExists) {
                    bg = Styles.EditorColor.COLOR_NOTED.getColor();
                } else if (markUntranslated && !translationExists) {
                    bg = Styles.EditorColor.COLOR_UNTRANSLATED.getColor();
                } else if (isDisplaySegmentSources()) {
                    bg = Styles.EditorColor.COLOR_SOURCE.getColor();
                }
            } else {
                if (isMarkNotedSegments() && hasNote) {
                    bg = Styles.EditorColor.COLOR_NOTED.getColor();
                } else if (markTranslated) {
                    bg = Styles.EditorColor.COLOR_TRANSLATED.getColor();
                }
            }
        }

        Color nonUniqueBg = Styles.EditorColor.COLOR_NON_UNIQUE_BG.getColor();
        if (markNonUniqueSegments && nonUniqueBg != null) {
            switch (duplicate) {
            case NONE:
                break;
            case FIRST:
                if (markFirstNonUnique) {
                    bg = nonUniqueBg;
                }
                break;
            case NEXT:
                bg = nonUniqueBg;
                break;
            }
        }
        if (isNBSP && isMarkNBSP()) { //overwrite others, because space is smallest.
            bg = Styles.EditorColor.COLOR_NBSP.getColor();
        }

        //determine bold
        Boolean bold = false;
        if (isSource) {
            if (viewSourceBold || (active && viewActiveSourceBold)) {
                bold = true;
            }
        }

        //determine italic
        Boolean italic = false;
        if (isRemoveText && isSource) {
            italic = true;
        }

        return Styles.createAttributeSet(fg, bg, bold, italic);
    }

    /**
     * Returns font attributes for paragraph start
     * @return
     */
    public AttributeSet getParagraphStartAttributeSet() {
        return Styles.createAttributeSet(Styles.EditorColor.COLOR_PARAGRAPH_START.getColor(),
                null, false, true);
    }

    /**
     * Returns font attributes for the modification info line.
     * @return
     */
    public AttributeSet getModificationInfoAttributeSet() {
        return Styles.createAttributeSet(Styles.EditorColor.COLOR_MOD_INFO_FG.getColor(),
                Styles.EditorColor.COLOR_MOD_INFO.getColor(), false, true);
    }

    /**
     * Returns font attributes for the segment marker.
     *
     * @return
     */
    public AttributeSet getSegmentMarkerAttributeSet() {
        return Styles.createAttributeSet(Styles.EditorColor.COLOR_SEGMENT_MARKER_FG.getColor(),
                Styles.EditorColor.COLOR_SEGMENT_MARKER_BG.getColor(), true, false);
    }

    /**
     * Returns font attributes for other languages translation.
     *
     * @return
     */
    public AttributeSet getOtherLanguageTranslationAttributeSet() {
        return Styles.createAttributeSet(Styles.EditorColor.COLOR_SOURCE_FG.getColor(),
                Styles.EditorColor.COLOR_SOURCE.getColor(), false, true);
    }
}
