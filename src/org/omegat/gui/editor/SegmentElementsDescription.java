/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.OmDocument.OmElementSegment;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;

/**
 * Class for segment's description. It required only for convert OmegaT's
 * segment into AbstractDocument.Element representation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SegmentElementsDescription {

    /** Attributes for show text. */
    protected static final AttributeSet ATTR_SOURCE = Styles.GREEN;
    protected static final AttributeSet ATTR_SEGMENT_MARK = Styles.BOLD;
    protected static final AttributeSet ATTR_TRANS_TRANSLATED = Styles.TRANSLATED;
    protected static final AttributeSet ATTR_TRANS_UNTRANSLATED = Styles.UNTRANSLATED;

    SourceTextEntry ste;
    int segmentNumberInProject;
    boolean needToCheckSpelling;

    int sourceTextEnd;
    int translationBeginTagStart, translationBeginTagEnd;
    int translationEndTagStart, translationEndTagEnd;
    int fullSegmentLength;

    /**
     * Alex Buloichik:
     * 
     * Show source rule: we always show source(bold black/green) for active
     * segment, and we show source(bold black/green) if
     * "View/Display source segments" is checked.
     * 
     * Show translation rule: we always show translation if it exist, or display
     * source as translation if translation doesn't exists and
     * "View/Display source segments" is unchecked.
     * 
     * If "View/Mark translated segments" is checked, then we show exist
     * translation as black/yellow. If "View/Mark untranslated segments" is
     * checked, then we show non-exist source as translation in black/violet.
     */
    AttributeSet translationAttrs, translationMisspelledAttrs;

    private final OmDocument doc;

    private final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0000");

    enum SEGMENT_PART {
        SOURCE, MARK1, MARK2, TRANSLATION, NONE
    };

    public SegmentElementsDescription(OmDocument doc, StringBuilder text,
            SourceTextEntry ste, int segmentNumberInProject, boolean isActive) {
        this(doc, text, ste.getSrcText(), ste.getTranslation(), isActive,
                segmentNumberInProject, ste.getTranslation() != null
                        && ste.getTranslation().length() > 0);
        this.ste = ste;
        this.segmentNumberInProject = segmentNumberInProject;
    }

    /**
     * Create description for active segment.
     * 
     * @param doc
     * @param text
     * @param sourceText
     * @param translationText
     * @param segmentNumber
     */
    public SegmentElementsDescription(OmDocument doc, StringBuilder text,
            String sourceText, String translationText, int segmentNumber) {
        this(doc, text, sourceText, translationText, true, segmentNumber, true);
    }

    /**
     * Create description for display segment.
     */
    private SegmentElementsDescription(OmDocument doc, StringBuilder text,
            String sourceText, String translationText, boolean isActive,
            int segmentNumber, boolean translationExists) {
        this.doc = doc;

        int offset = 0;

        EditorSettings settings = doc.controller.settings;
        if (isActive) {
            /** Create for active segment. */

            // add source text
            text.append(sourceText).append('\n');
            offset += sourceText.length() + 1;

            // add start translation tag
            translationBeginTagStart = offset;
            String tx = OConsts.segmentStartString.trim().replace("0000",
                    NUMBER_FORMAT.format(segmentNumber));
            text.append(tx);
            offset += tx.length();
            translationBeginTagEnd = offset;

            // add exist translation
            if (translationExists) {
                text.append(translationText);
                offset += translationText.length();
                if (doc.controller.settings.isAutoSpellChecking()) {
                    // spell it
                    needToCheckSpelling = true;
                    doc.controller.spellCheckerThread
                            .addForCheck(translationText);
                }
            } else if (!Preferences
                    .isPreference(Preferences.DONT_INSERT_SOURCE_TEXT)) {
                text.append(sourceText);
                offset += sourceText.length();
                if (doc.controller.settings.isAutoSpellChecking()) {
                    // spell it
                    needToCheckSpelling = true;
                    doc.controller.spellCheckerThread.addForCheck(sourceText);
                }
            }

            // add end translation tag
            translationEndTagStart = offset;
            tx = OConsts.segmentEndString.trim();
            text.append(tx).append('\n');
            offset += tx.length() + 1;
            translationEndTagEnd = offset;
        } else {
            /** Create for inactive segment. */

            if (settings.isDisplaySegmentSources()) {
                // add source text
                text.append(sourceText).append('\n');
                offset += sourceText.length() + 1;
            }
            sourceTextEnd = offset;
            translationBeginTagStart = offset;
            translationBeginTagEnd = offset;
            if (translationExists) {
                // add exist translation
                text.append(translationText).append('\n');
                offset += translationText.length() + 1;
                translationAttrs = settings.isMarkTranslated() ? Styles.TRANSLATED
                        : null;
                if (doc.controller.settings.isAutoSpellChecking()) {
                    // spell it
                    needToCheckSpelling = true;
                    doc.controller.spellCheckerThread
                            .addForCheck(translationText);
                }
            } else if (!doc.controller.settings.isDisplaySegmentSources()) {
                // translation not exist - add source instead
                text.append(sourceText).append('\n');
                offset += sourceText.length() + 1;
                translationAttrs = settings.isMarkUntranslated() ? Styles.UNTRANSLATED
                        : null;
            }
            translationEndTagStart = offset;
            translationEndTagEnd = offset;
        }

        text.append('\n');
        offset++;
        fullSegmentLength = offset;

        translationMisspelledAttrs = translationAttrs != null ? Styles
                .applyStyles(translationAttrs, Styles.MISSPELLED)
                : Styles.MISSPELLED;
    }

    List<OmDocument.OmElementParagraph> paragraphElements = new ArrayList<OmDocument.OmElementParagraph>(
            32);
    List<OmDocument.OmElementText> textElements = new ArrayList<OmDocument.OmElementText>(
            64);

    /**
     * Create SegmentElement's child elements by segment description.
     * 
     * @param seg
     *            info
     * @param description
     *            segment's description
     * @param text
     *            segment's text
     * @param offsetFromDocumentBegin
     *            segment offset from document begin
     * @return OmElementParagraph[]
     */
    Element[] createElementsForSegment(OmDocument doc,
            OmElementSegment segElement, String text,
            int offsetFromDocumentBegin) {
        paragraphElements.add(doc.new OmElementParagraph(segElement, null));

        // add sources
        addLines(segElement, ATTR_SOURCE, text.substring(0,
                translationBeginTagStart), offsetFromDocumentBegin, false,
                OmContent.POSITION_TYPE.BEFORE_EDITABLE);
        // add <segment 0000>
        addLines(segElement, ATTR_SEGMENT_MARK, text.substring(
                translationBeginTagStart, translationBeginTagEnd),
                offsetFromDocumentBegin + translationBeginTagStart, false,
                OmContent.POSITION_TYPE.BEFORE_EDITABLE);
        // add translation
        addLines(segElement, translationAttrs, text.substring(
                translationBeginTagEnd, translationEndTagStart),
                offsetFromDocumentBegin + translationBeginTagEnd,
                needToCheckSpelling, OmContent.POSITION_TYPE.INSIDE_EDITABLE);
        // add <end segment>
        addLines(segElement, ATTR_SEGMENT_MARK, text.substring(
                translationEndTagStart, translationEndTagEnd),
                offsetFromDocumentBegin + translationEndTagStart, false,
                OmContent.POSITION_TYPE.AFTER_EDITABLE);
        // add <new lines segments separator>
        addLines(segElement, null, text.substring(translationEndTagEnd),
                offsetFromDocumentBegin + translationEndTagEnd, false,
                OmContent.POSITION_TYPE.AFTER_EDITABLE);

        paragraphElements.remove(paragraphElements.size() - 1);
        return paragraphElements.toArray(new Element[paragraphElements.size()]);
    }

    /**
     * Add lines elements.
     * 
     * @param section
     *            segment element
     * @param attrs
     *            attributes
     * @param partText
     *            lines text
     * @param offsetFromDocumentBegin
     *            offset from document's begin
     * @param needSpellCheck
     *            true if need to check spelling
     * @param positionType
     *            position types for marks
     */
    private void addLines(OmDocument.OmElementSegment section,
            AttributeSet attrs, String partText, int offsetFromDocumentBegin,
            boolean needSpellCheck, OmContent.POSITION_TYPE positionType) {
        if (partText.length() == 0) {
            return;
        }
        int pos, prevPos = 0;
        while (true) {
            pos = partText.indexOf('\n', prevPos);
            if (pos < 0)
                break;
            addLine(attrs, partText.substring(prevPos, pos + 1),
                    offsetFromDocumentBegin + prevPos, needSpellCheck,
                    positionType);
            last(paragraphElements).replace(0, 0,
                    textElements.toArray(new Element[textElements.size()]));
            textElements.clear();

            paragraphElements.add(doc.new OmElementParagraph(section, null));
            prevPos = pos + 1;
        }
        addLine(attrs, partText.substring(prevPos), offsetFromDocumentBegin
                + prevPos, needSpellCheck, positionType);
    }

    /**
     * Add elements for one line.
     * 
     * @param attrs
     *            attributes
     * @param partText
     *            line text
     * @param offsetFromDocumentBegin
     *            offset from document's begin
     * @param needSpellCheck
     *            true if need to check spelling
     * @param positionType
     *            position types for marks
     */
    private void addLine(AttributeSet attrs, String partText,
            int offsetFromDocumentBegin, boolean needSpellCheck,
            OmContent.POSITION_TYPE positionType) {
        if (!needSpellCheck) {
            // don't need to spell check. just add element
            textElements.add(doc.new OmElementText(last(paragraphElements),
                    attrs, offsetFromDocumentBegin, offsetFromDocumentBegin
                            + partText.length(), positionType));
            return;
        }

        try {
            int prevFinished = 0;
            for (Token tok : Core.getTokenizer().tokenizeWordsForSpelling(
                    partText)) {
                String word = partText.substring(tok.getOffset(), tok
                        .getOffset()
                        + tok.getLength());
                if (doc.controller.spellCheckerThread.isIncorrect(word)) {
                    int tokBeg = tok.getOffset();
                    int tokEnd = tok.getOffset() + tok.getLength();
                    if (tokBeg > prevFinished) {
                        // there is unhandled text before token
                        textElements
                                .add(doc.new OmElementText(
                                        last(paragraphElements), attrs,
                                        prevFinished + offsetFromDocumentBegin,
                                        tokBeg + offsetFromDocumentBegin,
                                        positionType));
                    }
                    textElements.add(doc.new OmElementText(
                            last(paragraphElements),
                            translationMisspelledAttrs, tokBeg
                                    + offsetFromDocumentBegin, tokEnd
                                    + offsetFromDocumentBegin, positionType));
                    prevFinished = tokEnd;
                }
            }
            if (prevFinished < partText.length()) {
                // there is unhandled text before token
                textElements.add(doc.new OmElementText(last(paragraphElements),
                        attrs, prevFinished + offsetFromDocumentBegin, partText
                                .length()
                                + offsetFromDocumentBegin, positionType));
            }
        } catch (IndexOutOfBoundsException ex) {
            Log.log(ex);
        }
    }

    private static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }
}
