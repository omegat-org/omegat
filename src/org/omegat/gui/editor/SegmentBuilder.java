/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik, Martin Fleurke
               2010 Alex Buloichik, Didier Briel
               2012 Martin Fleurke, Hans-Peter Jacobs
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.editor;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for store information about displayed segment, and for show segment in
 * editor.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Hans-Peter Jacobs
 */
public class SegmentBuilder {

    /** Attributes for show text. */
    public static final String SEGMENT_MARK_ATTRIBUTE = "SEGMENT_MARK_ATTRIBUTE";
    public static final String SEGMENT_SPELL_CHECK = "SEGMENT_SPELL_CHECK";
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0000");
    private static final DateFormat dateFormat = DateFormat.getDateInstance();
    private static final DateFormat timeFormat = DateFormat.getTimeInstance();

    final SourceTextEntry ste;
    final int segmentNumberInProject;

    /**
     * Version of displayed variant of segment. Required for check in delayed
     * thread, like spell checking. Version changed(in Swing thread only) each
     * time when entry drawn, and when user edits it (for active entry).
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
    /** True if entry has a note attached. */
    private boolean noteExist;
    /** True if translation is default, false - is multiple. */
    private boolean defaultTranslation;

    private final Document3 doc;
    private final EditorController controller;
    private final EditorSettings settings;
    /**
     * Offset of first c.q. last character in active source text
     */
    protected int activeTranslationBeginOffset, activeTranslationEndOffset;

    /** Boundary of full entry display. */
    protected Position beginPosP1, endPosM1;

    /** Source start position - for marks. */
    protected Position posSourceBeg;
    /** Translation start position - for marks. */
    protected Position posTranslationBeg;

    /** current offset in document to insert new stuff*/
    protected int offset;

    /**
     * True if source OR target languages is RTL. In this case, we will insert
     * RTL/LTR embedded direction chars. Otherwise - will not insert, since JDK
     * 1.6 has bug with performance with embedded directions chars.
     */
    protected boolean hasRTL;

    public SegmentBuilder(final EditorController controller, final Document3 doc,
            final EditorSettings settings, final SourceTextEntry ste, final int segmentNumberInProject) {
        this.controller = controller;
        this.doc = doc;
        this.settings = settings;
        this.ste = ste;
        this.segmentNumberInProject = segmentNumberInProject;

        hasRTL = controller.sourceLangIsRTL || controller.targetLangIsRTL || EditorUtils.localeIsRTL()
                || controller.currentOrientation != Document3.ORIENTATION.ALL_LTR;
        Map<Language,ProjectTMX> otherLanguageTMs = Core.getProject().getOtherTargetLanguageTMs();
        for (Map.Entry<Language,ProjectTMX> entry : otherLanguageTMs.entrySet()) {
            hasRTL = hasRTL || EditorUtils.isRTL(entry.getKey().getLanguageCode().toLowerCase());
        }
    }

    public boolean isDefaultTranslation() {
        return defaultTranslation;
    }

    public void setDefaultTranslation(boolean defaultTranslation) {
        this.defaultTranslation = defaultTranslation;
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

                TMXEntry trans = Core.getProject().getTranslationInfo(ste);
                defaultTranslation = trans.defaultTranslation;
                if (!Core.getProject().getProjectProperties().isSupportDefaultTranslations()) {
                    defaultTranslation = false;
                }
                transExist = trans.isTranslated();
                noteExist  = trans.hasNote();

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
     * Create active segment for given entry
     */
    private void createActiveSegmentElement(TMXEntry trans) throws BadLocationException {
        try {
            if (EditorSettings.DISPLAY_MODIFICATION_INFO_ALL.equals(settings.getDisplayModificationInfo())
                    || EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED.equals(settings
                            .getDisplayModificationInfo())) {
                addModificationInfoPart(trans);
            }

            int prevOffset = offset;
            sourceText = ste.getSrcText();
            addInactiveSegPart(true, sourceText);

            Map<Language,ProjectTMX> otherLanguageTMs = Core.getProject().getOtherTargetLanguageTMs();
            for (Map.Entry<Language,ProjectTMX> entry : otherLanguageTMs.entrySet()) {
                TMXEntry altTrans = entry.getValue().getDefaultTranslation(sourceText);
                if (altTrans!=null && altTrans.isTranslated()) {
                    Language language = entry.getKey();
                    addOtherLanguagePart(altTrans.translation, language);
                }
            }

            posSourceBeg = doc.createPosition(prevOffset + (hasRTL ? 1 : 0));

            if (trans.isTranslated()) {
                //translation exist
                translationText = trans.translation;
            } else if (!Preferences.isPreference(Preferences.DONT_INSERT_SOURCE_TEXT)) {
                // need to insert source text on empty translation
                translationText = ste.getSrcText();
            } else {
                // empty text on non-exist translation
                translationText = "";
            }

            addActiveSegPart(translationText);
            posTranslationBeg = null;

            doc.activeTranslationBeginM1 = doc.createPosition(activeTranslationBeginOffset - 1);
            doc.activeTranslationEndP1 = doc.createPosition(activeTranslationEndOffset + 1);
        } catch (OutOfMemoryError oome) {
            // Oh shit, we're all out of storage space!
            // Of course we should've cleaned up after ourselves earlier,
            // but since we didn't, do a bit of cleaning up now, otherwise
            // we can't even inform the user about our slacking off.
            doc.remove(0, doc.getLength());

            // Well, that cleared up some, GC to the rescue!
            System.gc();

            // There, that should do it, now inform the user
            Object[] args = { Runtime.getRuntime().maxMemory() / 1024 / 1024 };
            Log.logErrorRB("OUT_OF_MEMORY", args);
            Log.log(oome);
            Core.getMainWindow().showErrorDialogRB("OUT_OF_MEMORY", args, "TF_ERROR");
            // Just quit, we can't help it anyway
            System.exit(0);

        }
    }

    /**
     * Create method for inactive segment.
     * @param trans TMX entry with translation
     * @throws BadLocationException
     */
    private void createInactiveSegmentElement(TMXEntry trans) throws BadLocationException {
        if (EditorSettings.DISPLAY_MODIFICATION_INFO_ALL.equals(settings.getDisplayModificationInfo())) {
            addModificationInfoPart(trans);
        }

        sourceText = null;
        translationText = null;

        if (settings.isDisplaySegmentSources()) {
            sourceText = ste.getSrcText();
        }

        if (trans.isTranslated()) {
            // translation exist
            translationText = trans.translation;
            if (StringUtil.isEmpty(translationText)) {
                translationText = OStrings.getString("EMPTY_TRANSLATION");
            }
        } else {
            if (sourceText == null) {
                // translation not exist, but source display disabled also -
                // need to display source
                sourceText = ste.getSrcText();
            }
        }

        if (sourceText != null) {
            int prevOffset = offset;
            addInactiveSegPart(true, sourceText);
            posSourceBeg = doc.createPosition(prevOffset + (hasRTL ? 1 : 0));
        } else {
            posSourceBeg = null;
        }

        if (translationText != null) {
            int prevOffset = offset;
            addInactiveSegPart(false, translationText);
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
     * @param isRTL
     *            is text right-to-left?
     */
    private void setAlignment(int begin, int end, boolean isRTL) {
        boolean rtl = false;
        switch (controller.currentOrientation) {
        case ALL_LTR:
            rtl = false;
            break;
        case ALL_RTL:
            rtl = true;
            break;
        case DIFFER:
            rtl = isRTL;
            break;
        }
        doc.setAlignment(begin, end, rtl);
    }

    /**
     * Check if location inside segment.
     */
    public boolean isInsideSegment(int location) {
        return beginPosP1.getOffset() - 1 <= location && location < endPosM1.getOffset() + 1;
    }

    /**
     * Add inactive segment part, without segment begin/end marks.
     *
     * @param isSource is text the source text (true) or translation text (false)
     * @param text
     *            segment part text
     * @throws BadLocationException
     */
    private void addInactiveSegPart(boolean isSource, String text)
            throws BadLocationException {
        int prevOffset = offset;
        boolean rtl = isSource ? controller.sourceLangIsRTL : controller.targetLangIsRTL;
        insertDirectionEmbedding(rtl);
        insertTextWithTags(text, isSource);
        insertDirectionEndEmbedding();
        insert("\n", null);
        setAlignment(prevOffset, offset, rtl);
    }

    /**
     * Add inactive segment part, without segment begin/end marks.
     *
     * @param text other language translation text
     * @throws BadLocationException
     */
    private void addOtherLanguagePart(String text, Language language)
            throws BadLocationException {
        int prevOffset = offset;
        boolean rtl = EditorUtils.isRTL(language.getLanguageCode());
        insertDirectionEmbedding(false);
        AttributeSet normal = attrs(true, false, false, false);
        insert(language.getLanguage()+": ", normal);
        insertDirectionEndEmbedding();

        insertDirectionEmbedding(rtl);
        AttributeSet attrs = settings.getOtherLanguageTranslationAttributeSet();
        insert(text, attrs);
        insertDirectionEndEmbedding();
        insert("\n", null);
        setAlignment(prevOffset, offset, rtl);
    }

    /**
     * Adds a string that displays the modification info (author and date). Does
     * nothing if the translation entry is null.
     *
     * @param trans
     *            The translation entry (can be null)
     * @throws BadLocationException
     */
    private void addModificationInfoPart(TMXEntry trans) throws BadLocationException {
        if (!trans.isTranslated())
            return;

        String author = (trans.changer == null ? OStrings.getString("TF_CUR_SEGMENT_UNKNOWN_AUTHOR")
                : trans.changer);
        String template;
        String text;
        if (trans.changeDate != 0) {
            template = OStrings.getString("TF_CUR_SEGMENT_AUTHOR_DATE");
            Date changeDate = new Date(trans.changeDate);
            String changeDateString = dateFormat.format(changeDate);
            String changeTimeString = timeFormat.format(changeDate);
            Object[] args = { author, changeDateString, changeTimeString };
            text = StaticUtils.format(template, args);
        } else {
            template = OStrings.getString("TF_CUR_SEGMENT_AUTHOR");
            Object[] args = { author };
            text = StaticUtils.format(template, args);
        }
        int prevOffset = offset;
        boolean rtl = EditorUtils.localeIsRTL();
        insertDirectionEmbedding(rtl);
        AttributeSet attrs = settings.getModificationInfoAttributeSet();
        insert(text, attrs);
        insertDirectionEndEmbedding();
        insert("\n", null);
        setAlignment(prevOffset, offset, rtl);
    }

    /**
     * Add active (translation) segment part, with segment begin/end marks.
     *
     * @param text
     *            segment part text
     * @throws BadLocationException
     */
    private void addActiveSegPart(String text) throws BadLocationException {
        int prevOffset = offset;

        //write translation part
        boolean rtl = controller.targetLangIsRTL;

        insertDirectionEmbedding(rtl);

        activeTranslationBeginOffset = offset;
        insertTextWithTags(text, false);
        activeTranslationEndOffset = offset;

        insertDirectionEndEmbedding();

        //write segment marker
        //we want the marker AFTER the translated text, so use same direction as target text.
        insertDirectionMarker(rtl);

        //the marker itself is in user language
        insertDirectionEmbedding(EditorUtils.localeIsRTL()); 
        AttributeSet attrSegmentMark = settings.getSegmentMarkerAttributeSet();
        insert(createSegmentMarkText(), attrSegmentMark);
        insertDirectionEndEmbedding();

        //we want the marker AFTER the translated text, so embed in same direction as target text.
        insertDirectionMarker(rtl);

        insert("\n", null);

        setAlignment(prevOffset, offset, rtl);
    }

    void createInputAttributes(Element element, MutableAttributeSet set) {
        set.addAttributes(attrs(false, false, false, false));
    }

    private void insert(String text, AttributeSet attrs) throws BadLocationException {
        doc.insertString(offset, text, attrs);
        offset += text.length();
    }

    /**
     * Make some changes of segment mark from resource bundle for display
     * correctly in editor.
     *
     * @return changed mark text
     */
    private String createSegmentMarkText() {
        String text = OConsts.segmentMarkerString;

        // trim and replace spaces to non-break spaces
        text = text.trim().replace(' ', '\u00A0');
        //replace placeholder with actual segment number
        if (text.indexOf("0000") >= 0) {
            text = text.replace("0000", NUMBER_FORMAT.format(segmentNumberInProject));
        }

        return text;
    }

    /**
     * Called on the active entry changed. Required for update translation text.
     */
    void onActiveEntryChanged() {
        translationText = doc.extractTranslation();
        displayVersion++;
    }

    /**
     * Choose segment part attributes based on rules.
     * @param isSource is it a source segment or a target segment
     * @param isPlaceholder is it for a placeholder (OmegaT tag or sprintf-variable etc.) or regular text inside the segment?
     * @param isRemoveText is it text that should be removed in the translation?
     * @param isNBSP is the text a non-breakable space?
     * @return the attributes to format the text
     */
    public AttributeSet attrs(boolean isSource, boolean isPlaceholder, boolean isRemoveText, boolean isNBSP) {
        return settings.getAttributeSet(isSource, isPlaceholder, isRemoveText, ste.getDuplicate(), active, transExist, noteExist, isNBSP);
    }

    /**
     * Inserts the texts and formats the text
     * @param text source or translation text
     * @param isSource true if it is a source text, false if it is a translation
     * @throws BadLocationException
     */
    private void insertTextWithTags(String text, boolean isSource) throws BadLocationException {
        AttributeSet normal = attrs(isSource, false, false, false);
        int start = offset;
        int end = start + text.length();
        insert(text, normal);
        formatText(text, start, end, isSource);
    }

    /**
     * formats source or target segment to highlight placeholders, tags, non-breakable spaces etc.
     * @param text the text to format
     * @param start start position in editor
     * @param end end position in editor
     * @param isSource is the text a source segment or not
     */
    public void formatText(String text, int start, int end, boolean isSource) {
        if (controller.currentOrientation != Document3.ORIENTATION.ALL_LTR) {
            //workaround for the issue that in RTL-embedded text, the formatting somehow
            //splits up the text, and the first parts then can get rendered confusingly.
            //E.g. "Blah %s, Blah." gets formatted, and thus split into "blah |<em>%s</em>|, Blah."
            //and that is shown partly in RTL as " Blah|<em>%s</em>|, Blah.".
            //Note the first space is displayed to the left of the word.
            //To prevent this, the formatting is not done when the editor is in RTL.
            //(markers are used for formatting in this case, which is a little less beautiful)
            return;
        }
        //first remove any formatting
        AttributeSet attrNormal = attrs(isSource, false, false, false);
        doc.setCharacterAttributes(start, end-start, attrNormal, true);
        //format placeholders
        AttributeSet attrPlaceholder = attrs(isSource, true, false, false);
        Pattern placeholderPattern = PatternConsts.getPlaceholderPattern();
        Matcher placeholderMatch = placeholderPattern.matcher(text);
        while (placeholderMatch.find()) {
            doc.setCharacterAttributes(start+placeholderMatch.start(), placeholderMatch.end()-placeholderMatch.start(), attrPlaceholder, true);
        }
        //format text-to-remove
        AttributeSet attrRemove = attrs(isSource, false, true, false);
        Pattern removePattern = PatternConsts.getRemovePattern();
        if (removePattern != null) {
            Matcher removeMatcher = removePattern.matcher(text);
            while (removeMatcher.find()) {
                doc.setCharacterAttributes(start+removeMatcher.start(), removeMatcher.end()-removeMatcher.start(), attrRemove, true);
            }
        }
    }

    /**
     * Writes (if necessary) an RTL or LTR marker. Use it before writing text in some language.
     * @param isRTL is the language that has to be written a right-to-left language?
     * @throws BadLocationException
     */
    private void insertDirectionEmbedding(boolean isRTL) throws BadLocationException {
        if (this.hasRTL) {
            insert(isRTL ? "\u202b" : "\u202a", null); // RTL- or LTR- embedding
        }
    }

    /**
     * Writes (if necessary) an end-of-embedding marker. Use it after writing text in some language.
     * @throws BadLocationException
     */
    private void insertDirectionEndEmbedding() throws BadLocationException {
        if (this.hasRTL) {
            insert("\u202c", null); // end of embedding
        }
    }

    /**
     * Writes (if necessary) an RTL or LTR marker. Use it before writing text in some language.
     * @param isRTL is the language that has to be written a right-to-left language?
     * @throws BadLocationException
     */
    private void insertDirectionMarker(boolean isRTL) throws BadLocationException {
        if (this.hasRTL) {
            insert(isRTL ? "\u200f" : "\u200e", null); // RTL- or LTR- marker
        }
    }
}
