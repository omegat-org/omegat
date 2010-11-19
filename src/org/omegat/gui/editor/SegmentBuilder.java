/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.editor;

import java.awt.Color;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TransEntry;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for store information about displayed segment, and for show segment in
 * editor.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SegmentBuilder {

    /** Attributes for show text. */
    protected static final AttributeSet ATTR_SEGMENT_MARK = Styles.createAttributeSet(null, null, true, false);
    protected static final AttributeSet ATTR_INFO = Styles.createAttributeSet(null, null, null, true);
    public static final String SEGMENT_MARK_ATTRIBUTE = "SEGMENT_MARK_ATTRIBUTE";
    public static final String SEGMENT_SPELL_CHECK = "SEGMENT_SPELL_CHECK";
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0000");
    private static final DateFormat dateFormat = DateFormat.getDateInstance();
    private static final DateFormat timeFormat = DateFormat.getTimeInstance();

    final SourceTextEntry ste;
    final int segmentNumberInProject;
    
    /**
     * Version of displayed variant of segment. Required for check in delayed
     * thread, like skell checking. Version changed(in Swing thread only) each
     * time when entry drawed, and when user edit it(for active entry).
     */
    private long displayVersion;
    /** Source text of entry, or null if not displayed. */
    private String sourceText;
    /** Translation text of entry, or null if not displayed. */
    private String translationText;
    /** True if entry is active. */
    private boolean active;
    /** True if translation exist for entry. */
    private boolean transExist;

    private final Document3 doc;
    private final EditorController controller;
    private final EditorSettings settings;

    protected int activeTranslationBeginOffset, activeTranslationEndOffset;

    /** Boundary of full entry display. */
    protected Position beginPosP1, endPosM1;

    /** Source start position - for marks. */
    protected Position posSourceBeg;
    /** Translation start position - for marks. */
    protected Position posTranslationBeg;    

    protected int offset;
    
    /**
     * True if source OR target languages is RTL. In this case, we will insert
     * RTL/LTR embedded direction chars. Otherwise - will not insert, since JDK
     * 1.6 has bug with performance with embedded directions chars.
     */
    protected boolean hasRTL;

    public SegmentBuilder(final EditorController controller,
            final Document3 doc, final EditorSettings settings,
            final SourceTextEntry ste, final int segmentNumberInProject) {
        this.controller = controller;
        this.doc = doc;
        this.settings = settings;
        this.ste = ste;
        this.segmentNumberInProject = segmentNumberInProject;
        
        hasRTL = controller.sourceLangIsRTL || controller.targetLangIsRTL;
    }

    /**
     * Create element for one segment.
     * 
     * @param doc
     *            document
     * @return OmElementSegment
     */
    public void createSegmentElement(final boolean isActive) {
        UIThreadsUtil.mustBeSwingThread();
        
        displayVersion++;
        this.active = isActive;

        doc.trustedChangesInProgress = true;
        try {
            try {
                if (beginPosP1 != null && endPosM1 != null) {
                    // remove old segment
                    int beginOffset = beginPosP1.getOffset() - 1;
                    int endOffset = endPosM1.getOffset() + 1;
                    doc.remove(beginOffset, endOffset - beginOffset);
                    offset = beginOffset;
                } else {
                    // there is no segment in document yet - need to add
                    offset = doc.getLength();
                }

                TransEntry trans = Core.getProject().getTranslation(ste);
                transExist = trans != null;

                int beginOffset = offset;
                if (isActive) {
                    createActiveSegmentElement(trans);
                } else {
                    createInactiveSegmentElement(trans);
                }
                int endOffset = offset;

                beginPosP1 = doc.createPosition(beginOffset + 1);
                endPosM1 = doc.createPosition(endOffset - 1);
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            doc.trustedChangesInProgress = false;
        }
    }

    /**
     * Add separator between segments - one empty line.
     * 
     * @param doc
     */
    public static void addSegmentSeparator(final Document3 doc) {
        doc.trustedChangesInProgress = true;
        try {
            try {
                doc.insertString(doc.getLength(), "\n", null);
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            doc.trustedChangesInProgress = false;
        }
    }

    /**
     * Create method for active segment.
     */
    private void createActiveSegmentElement(TransEntry trans)
            throws BadLocationException {
        try {
        if (  EditorSettings.DISPLAY_MODIFICATION_INFO_ALL.equals(settings.getDisplayModificationInfo())
           || EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED.equals(settings.getDisplayModificationInfo())
           ) {
            addModificationInfoPart(trans, ATTR_INFO);
        }
        
        int prevOffset = offset;
        sourceText = ste.getSrcText();
        addInactiveSegPart(true, sourceText, attrs(true));
        posSourceBeg = doc.createPosition(prevOffset + (hasRTL ? 1 : 0));

        if (trans != null) {
            // translation exist
            translationText = trans.translation;
        } else if (!Preferences
                .isPreference(Preferences.DONT_INSERT_SOURCE_TEXT)) {
            // need to insert source text on empty translation
            translationText = ste.getSrcText();
        } else {
            // empty text on non-exist translation
            translationText = "";
        }

        addActiveSegPart(translationText, attrs(false));
        posTranslationBeg = null;

        doc.activeTranslationBeginM1 = doc
                .createPosition(activeTranslationBeginOffset - 1);
        doc.activeTranslationEndP1 = doc
                .createPosition(activeTranslationEndOffset + 1);
        } catch (OutOfMemoryError oome) {
            // Oh shit, we're all out of storage space!
            // Of course we should've cleaned up after ourselves earlier,
            // but since we didn't, do a bit of cleaning up now, otherwise
            // we can't even inform the user about our slacking off.
            doc.remove(0, doc.getLength());

            // Well, that cleared up some, GC to the rescue!
            System.gc();

            // There, that should do it, now inform the user
            Object[] args = {Runtime.getRuntime().maxMemory()/1024/1024};
            Log.logErrorRB("OUT_OF_MEMORY", args);
            Log.log(oome);
            Core.getMainWindow().showErrorDialogRB(
                    "OUT_OF_MEMORY",args,
                    "TF_ERROR");
            // Just quit, we can't help it anyway
            System.exit(0);
            
        }
    }

    /**
     * Create method for inactive segment.
     */
    private void createInactiveSegmentElement(TransEntry trans)
            throws BadLocationException {
        if (EditorSettings.DISPLAY_MODIFICATION_INFO_ALL.equals(settings.getDisplayModificationInfo())) {
            addModificationInfoPart(trans, ATTR_INFO);
        }

        if (trans != null) {
            // translation exist
            translationText = trans.translation;
        } else {
            if (sourceText == null) {
                // translation not exist, but source display disabled also -
                // need to display source
                sourceText = ste.getSrcText();
            }
        }

        if (sourceText != null) {
            int prevOffset = offset;
            addInactiveSegPart(true, sourceText, attrs(true));
            posSourceBeg = doc.createPosition(prevOffset + (hasRTL ? 1 : 0));
        } else {
            posSourceBeg = null;
        }

        if (translationText != null) {
            int prevOffset = offset;
            addInactiveSegPart(false, translationText, attrs(false));
            posTranslationBeg = doc.createPosition(prevOffset + (hasRTL ? 1 : 0));
        } else {
            posTranslationBeg = null;
        }
    }

    public SourceTextEntry getSourceTextEntry() {
        return ste;
    }
    
    public long getDisplayVersion() {
        return displayVersion;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public String getSourceText() {
        return sourceText;
    }
    
    public String getTranslationText() {
        return translationText;
    }
    
    public int getStartSourcePosition() {
        if (posSourceBeg != null) {
            return posSourceBeg.getOffset();
        } else {
            return -1;
        }
    }

    public int getStartTranslationPosition() {
        if (posTranslationBeg != null) {
            return posTranslationBeg.getOffset();
        } else {
            return -1;
        }
    }

    /**
     * Get segment's start position.
     * 
     * @return start position
     */
    public int getStartPosition() {
        return beginPosP1.getOffset() - 1;
    }

    /**
     * Get segment's end position.
     * 
     * @return end position
     */
    public int getEndPosition() {
        return endPosM1.getOffset() + 1;
    }

    /**
     * Set attributes for created paragraphs for better RTL support.
     * 
     * @param begin
     *            paragraphs begin
     * @param end
     *            paragraphs end
     * @param isSource
     *            is source segment part
     */
    private void setAttributes(int begin, int end, boolean isSource) {
        boolean rtl = false;
        switch (controller.currentOrientation) {
        case LTR:
            rtl = false;
            break;
        case RTL:
            rtl = true;
            break;
        case DIFFER:
            rtl = isSource ? controller.sourceLangIsRTL
                    : controller.targetLangIsRTL;
            break;
        }
        doc.setAlignment(begin, end, rtl);
    }

    /**
     * Check if location inside segment.
     */
    public boolean isInsideSegment(int location) {
        return beginPosP1.getOffset() - 1 <= location
                && location < endPosM1.getOffset() + 1;
    }

    /**
     * Add inactive segment part, without segment begin/end marks.
     * @param isSource
     * @param text segment part text
     * @param attrs attributes
     * @throws BadLocationException
     */
    private void addInactiveSegPart(boolean isSource, String text,
            AttributeSet attrs) throws BadLocationException {
        int prevOffset = offset;
        boolean rtl = isSource ? controller.sourceLangIsRTL
                : controller.targetLangIsRTL;
        if (hasRTL) {
            insert(rtl ? "\u202b" : "\u202a", null); // LTR- or RTL- embedding
        }
        insert(text, attrs);
        if (hasRTL) {
            insert("\u202c", null); // end of embedding
        }
        insert("\n", null);
        setAttributes(prevOffset, offset, isSource);
    }

    /**
     * Adds a string that displays the modification info (author and date). 
     * Does nothing if the translation entry is null.
     * @param trans The translation entry (can be null)
     * @param attrs Font attributes
     * @throws BadLocationException
     */
    private void addModificationInfoPart(TransEntry trans, AttributeSet attrs) throws BadLocationException {
        if (trans == null ) return;

        String author = (trans.changeId==null?OStrings.getString("TF_CUR_SEGMENT_UNKNOWN_AUTHOR"):trans.changeId);
        String template;
        String text;
        if (trans.changeDate != 0) {
            template = OStrings.getString("TF_CUR_SEGMENT_AUTHOR_DATE");
            Date changeDate = new Date(trans.changeDate);
            String changeDateString = dateFormat.format(changeDate);
            String changeTimeString = timeFormat.format(changeDate);
            Object[] args = {author, changeDateString, changeTimeString};
            text = StaticUtils.format(template, args);
        } else {
            template = OStrings.getString("TF_CUR_SEGMENT_AUTHOR");
            Object[] args = {author};
            text = StaticUtils.format(template, args);
        }
        int prevOffset = offset;
        boolean rtl = localeIsRTL();
        if (hasRTL) {
            insert(rtl ? "\u202b" : "\u202a", null); // LTR- or RTL- embedding
        }
        insert(text, attrs);
        if (hasRTL) {
            insert("\u202c", null); // end of embedding
        }
        insert("\n", null);
        setAttributes(prevOffset, offset, false);
    }

    /**
     * Add active segment part, with segment begin/end marks.
     * @param text segment part text
     * @param attrs attributes
     * @throws BadLocationException
     */
    private void addActiveSegPart(String text, AttributeSet attrs)
            throws BadLocationException {
        int prevOffset = offset;
        boolean rtl = controller.targetLangIsRTL;

        insert(createSegmentMarkText(true), ATTR_SEGMENT_MARK);
        insert(" ", null);

        if (hasRTL) {
            insert(rtl ? "\u202b" : "\u202a", null); // LTR- or RTL- embedding
        }

        activeTranslationBeginOffset = offset;
        insert(text, attrs);
        activeTranslationEndOffset = offset;

        if (hasRTL) {
            insert("\u202c", null); // end of embedding
        }

        insert(" ", null);
        insert(createSegmentMarkText(false), ATTR_SEGMENT_MARK);

        insert("\n", null);

        setAttributes(prevOffset, offset, false);
    }

    private void insert(String text, AttributeSet attrs)
            throws BadLocationException {
        doc.insertString(offset, text, attrs);
        offset += text.length();
    }

    /**
     * Make some changes of segment mark from resource bundle for display
     * correctly in editor.
     * 
     * @param startMark
     *            true if tart mark, false if end mark
     * @return changed mark text
     */
    private String createSegmentMarkText(boolean startMark) {
        String text = startMark ? OConsts.segmentStartString
                : OConsts.segmentEndString;

        boolean markIsRTL = localeIsRTL();

        // trim and replace spaces to non-break spaces
        text = text.trim().replace(' ', '\u00A0');
        if (text.indexOf("0000") >= 0) {
            // Start mark - need to put segment number
            text = text.replace("0000", NUMBER_FORMAT
                    .format(segmentNumberInProject));
        }

        if (hasRTL) {
            // add LTR/RTL embedded chars
            text = (markIsRTL ? '\u202b' : '\u202a') + text + '\u202c';
        }

        return text;
    }

    /**
     * Returns whether the current locale is a Right-to-Left language or not.
     * @return true when current locale is a RTL language, false if not.
     */
    private boolean localeIsRTL() {
        boolean markIsRTL;
        String language = Locale.getDefault().getLanguage().toLowerCase();
        /*
         * Hardcode for future - if somebody will translate marks to RTL
         * language.
         */
        if ("some_RTL_language_code".equals(language)) {
            markIsRTL = true;
        } else {
            markIsRTL = false;
        }
        return markIsRTL;
    }
    
    /**
     * Called on the active entry changed. Required for update translation text.
     */
    void onActiveEntryChanged() {
        translationText = doc.extractTranslation();
        displayVersion++;
    }
    
    /**
     * Choose segment's attributes based on rules.
     */
    private AttributeSet attrs(boolean isSource) {
        Color fg;
        if (settings.isMarkUniqueSegments() && ste.isDublicate()) {
            fg = Styles.COLOR_LIGHT_GRAY;
        } else {
            fg = null;
        }
        if (active) {
            if (isSource) {
                return Styles.createAttributeSet(fg, Styles.COLOR_GREEN, true, null);
            } else {
                return Styles.createAttributeSet(fg, null, null, null);
            }
        } else {
            if (isSource) {
                if (transExist || settings.isDisplaySegmentSources()) {
                    return Styles.createAttributeSet(fg, Styles.COLOR_GREEN, true, null);
                } else {
                    Color b = settings.isMarkUntranslated() ? Styles.COLOR_UNTRANSLATED : null;
                    return Styles.createAttributeSet(fg, b, null, null);
                }
            } else {
                Color b = settings.isMarkTranslated() ? Styles.COLOR_TRANSLATED : null;
                return Styles.createAttributeSet(fg, b, null, null);
            }
        }
    }
}
