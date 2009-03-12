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

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;

/**
 * Class for segment's description. It required only for convert OmegaT's
 * segment into AbstractDocument.Element representation.
 * 
 * 
 * 
 * Show source rule: we always show source(bold black/green) for active segment,
 * and we show source(bold black/green) if "View/Display source segments" is
 * checked.
 * 
 * Show translation rule: we always show translation if it exist, or display
 * source as translation if translation doesn't exists and "View/Display source
 * segments" is unchecked.
 * 
 * If "View/Mark translated segments" is checked, then we show exist translation
 * as black/yellow. If "View/Mark untranslated segments" is checked, then we
 * show non-exist source as translation in black/violet.
 * 
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SegmentElementsDescription {

    /** Attributes for show text. */
    protected static final AttributeSet ATTR_SOURCE = Styles.GREEN;
    protected static final AttributeSet ATTR_SEGMENT_MARK = Styles.BOLD;
    protected static final AttributeSet ATTR_TRANS_TRANSLATED = Styles.TRANSLATED;
    protected static final AttributeSet ATTR_TRANS_UNTRANSLATED = Styles.UNTRANSLATED;
    protected static final AttributeSet ATTR_MISSPELLED = Styles.MISSPELLED;
    protected static final AttributeSet ATTR_NONE = new SimpleAttributeSet();

    final SourceTextEntry ste;
    final int segmentNumberInProject;
    boolean needToCheckSpelling;

    private final OmDocument doc;

    private final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0000");

    protected int activeTranslationBeginOffset, activeTranslationEndOffset;

    public SegmentElementsDescription(final OmDocument doc,
            final SourceTextEntry ste, final int segmentNumberInProject) {
        this.doc = doc;
        this.ste = ste;
        this.segmentNumberInProject = segmentNumberInProject;
    }

    /**
     * Create element for one segment.
     * 
     * @param doc
     *            document
     * @return OmElementSegment
     */
    protected Element createSegmentElement(final OmDocument.OmElementMain root,
            final boolean isActive, boolean sourceLangIsRTL,
            boolean targetLangIsRTL) {
        ElementWithChilds segElement = new ElementWithChilds();
        segElement.el = doc.new OmElementSegment(root, null, ste,
                segmentNumberInProject);

        boolean translationExists = ste.getTranslation() != null
                && ste.getTranslation().length() > 0;

        EditorSettings settings = doc.controller.settings;
        if (isActive) {
            /** Create for active segment. */
            segElement.addChild(addInactiveSegPart(segElement.el, ste
                    .getSrcText(), ATTR_SOURCE, sourceLangIsRTL));

            String markBeg = OConsts.segmentStartString.trim().replace("0000",
                    NUMBER_FORMAT.format(segmentNumberInProject));

            String markEnd = OConsts.segmentEndString.trim();

            String activeText;
            if (translationExists) {
                // translation exist
                activeText = ste.getTranslation();
                if (settings.isAutoSpellChecking()) {
                    // spell it
                    needToCheckSpelling = true;
                    doc.controller.spellCheckerThread.addForCheck(ste
                            .getTranslation());
                }
            } else if (!Preferences
                    .isPreference(Preferences.DONT_INSERT_SOURCE_TEXT)) {
                // need to insert source text on empty translation
                activeText = ste.getSrcText();
                if (settings.isAutoSpellChecking()) {
                    // spell it
                    needToCheckSpelling = true;
                    doc.controller.spellCheckerThread.addForCheck(ste
                            .getSrcText());
                }
            } else {
                // empty text on non-exist translation
                activeText = "";
            }

            segElement.addChild(addActiveSegPart(segElement.el, activeText,
                    ATTR_NONE, markBeg, markEnd, targetLangIsRTL));
        } else {
            /** Create for inactive segment. */
            if (settings.isDisplaySegmentSources()) {
                segElement.addChild(addInactiveSegPart(segElement.el, ste
                        .getSrcText(), ATTR_SOURCE, sourceLangIsRTL));
            }
            if (translationExists) {
                // translation exist
                if (settings.isAutoSpellChecking()) {
                    // spell it
                    needToCheckSpelling = true;
                    doc.controller.spellCheckerThread.addForCheck(ste
                            .getTranslation());
                }
                segElement.addChild(addInactiveSegPart(segElement.el, ste
                        .getTranslation(), settings.getTranslatedAttributeSet(), targetLangIsRTL));
            } else if (!settings.isDisplaySegmentSources()) {
                segElement.addChild(addInactiveSegPart(segElement.el, ste
                        .getSrcText(), settings.getUntranslatedAttributeSet(), targetLangIsRTL));
            }
        }

        ElementWithChilds emptyLine = new ElementWithChilds(
                doc.new OmElementSegmentsSeparator(segElement.el, null));
        segElement.addChild(emptyLine);

        segElement.setChilds();

        return segElement.el;
    }

    /**
     * Create element for active translation.
     * 
     * @param doc
     *            document
     * @return OmElementSegment
     */
    protected Element createTranslationElement(Element parent,
            final OmDocument.OmElementSegment seg, final String activeText,
            boolean targetLangIsRTL) {
        String markBeg = OConsts.segmentStartString.trim().replace("0000",
                NUMBER_FORMAT.format(segmentNumberInProject));

        String markEnd = OConsts.segmentEndString.trim();

        if (doc.controller.settings.isAutoSpellChecking()) {
            // spell it
            needToCheckSpelling = true;
            doc.controller.spellCheckerThread.addForCheck(ste.getTranslation());
        }

        ElementWithChilds result = addActiveSegPart(parent, activeText,
                ATTR_NONE, markBeg, markEnd, targetLangIsRTL);

        result.setChilds();
        return result.el;
    }

    /**
     * Add inactive segment part, without segment begin/end marks.
     * 
     * @param parent
     *            parent element
     * @param text
     *            segment part text
     * @param attrs
     *            attributes
     * @param langIsRTL
     * @return true if language is RTL
     */
    private ElementWithChilds addInactiveSegPart(Element parent, String text,
            AttributeSet attrs, boolean langIsRTL) {
        ElementWithChilds segPartElement = new ElementWithChilds();
        segPartElement.el = doc.new OmElementSegPart(parent, attrs, langIsRTL);
        addLines(segPartElement, text, needToCheckSpelling);
        addEOL(segPartElement);
        return segPartElement;
    }

    /**
     * Add active segment part, with segment begin/end marks.
     * 
     * @param parent
     *            parent element
     * @param text
     *            segment part text
     * @param attrs
     *            attributes
     * @param markBeg
     *            text of begin mark
     * @param markEnd
     *            text of end mark
     * @param langIsRTL
     *            true if language is RTL
     * @return segment part element
     */
    private ElementWithChilds addActiveSegPart(Element parent, String text,
            AttributeSet attrs, String markBeg, String markEnd,
            boolean langIsRTL) {
        ElementWithChilds segPartElement = new ElementWithChilds();
        segPartElement.el = doc.new OmElementSegPart(parent, attrs, langIsRTL);

        ElementWithChilds segMarkB = new ElementWithChilds();
        String smTextB = ' ' + OConsts.segmentStartString.trim().replace(
                "0000", NUMBER_FORMAT.format(segmentNumberInProject)) + ' ';
        segMarkB.el = doc.new OmElementSegmentMark(true, segPartElement.el,
                ATTR_SEGMENT_MARK, smTextB);

        activeTranslationBeginOffset = doc.unflushedText.length() - 1;
        if (StringUtil.isEmpty(text)) {
            addLines(segPartElement, "", false);
        } else {
            addLines(segPartElement, text, needToCheckSpelling);
        }
        activeTranslationEndOffset = doc.unflushedText.length() + 1;

        ElementWithChilds segMarkE = new ElementWithChilds();
        String smTextE = ' ' + OConsts.segmentEndString.trim() + ' ';
        segMarkE.el = doc.new OmElementSegmentMark(false, segPartElement.el,
                ATTR_SEGMENT_MARK, smTextE);

        if (segPartElement.getChilds() == null) {
            // Add empty line if there is no translated text. Required for
            // create segment marks.
            ElementWithChilds line = new ElementWithChilds();
            line.el = doc.new OmElementParagraph(segPartElement.el, null);
            segPartElement.addChild(line);
        }
        ElementWithChilds lnFirst = segPartElement.getChilds().get(0);
        
        lnFirst.addFirstChild(segMarkB);

        ElementWithChilds lnLast = segPartElement.getChilds().get(
                segPartElement.getChilds().size() - 1);
        lnLast.addChild(segMarkE);

        addEOL(segPartElement);

        return segPartElement;
    }

    /**
     * Add end-of-line element.
     * 
     * @param segPartElement
     *            parent element
     */
    private void addEOL(ElementWithChilds segPartElement) {
        int lastParIndex = segPartElement.getChilds().size() - 1;
        ElementWithChilds lastPar = segPartElement.getChilds()
                .get(lastParIndex);

        ElementWithChilds eolText = new ElementWithChilds();
        eolText.el = doc.new OmElementText(lastPar.el, null, "\n");
        lastPar.addChild(eolText);
    }

    /**
     * Add text which can contains many lines. This method splits text to lines
     * add add each line inside own element.
     * 
     * @param segPartElement
     *            segment part element
     * @param partText
     *            full text
     * @param needSpellCheck
     *            true if need to check spelling
     */
    private void addLines(ElementWithChilds segPartElement, String partText,
            boolean needSpellCheck) {
        if (partText.length() == 0) {
            return;
        }
        int pos, prevPos = 0;
        while (true) {
            pos = partText.indexOf('\n', prevPos);
            if (pos < 0)
                break;
            addLine(segPartElement, partText.substring(prevPos, pos + 1),
                    needSpellCheck);

            prevPos = pos + 1;
        }
        addLine(segPartElement, partText.substring(prevPos), needSpellCheck);
    }

    /**
     * Add elements for one line text.
     * 
     * @param segPartElement
     *            segment part element
     * @param partText
     *            line text
     * @param needSpellCheck
     *            true if need to check spelling
     */
    private void addLine(ElementWithChilds segPartElement, String partText,
            boolean needSpellCheck) {
        if (partText.length() == 0) {
            return;
        }
        ElementWithChilds line = new ElementWithChilds();
        line.el = doc.new OmElementParagraph(segPartElement.el, null);
        ElementWithChilds text;

        if (needSpellCheck) {
            // add elements with check spelling
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
                        text = new ElementWithChilds(doc.new OmElementText(
                                line.el, null, partText.substring(prevFinished,
                                        tokBeg)));
                        line.addChild(text);
                    }
                    text = new ElementWithChilds(
                            doc.new OmElementText(line.el, ATTR_MISSPELLED,
                                    partText.substring(tokBeg, tokEnd)));
                    line.addChild(text);
                    prevFinished = tokEnd;
                }
            }
            if (prevFinished < partText.length()) {
                // there is unhandled text before token
                text = new ElementWithChilds(doc.new OmElementText(line.el,
                        null, partText.substring(prevFinished)));
                line.addChild(text);
            }
        } else {
            // don't need to spell check. just add element
            text = new ElementWithChilds(doc.new OmElementText(line.el, null,
                    partText));
            line.addChild(text);
        }

        segPartElement.addChild(line);
        return;
    }

    /**
     * Object for store elements with childs. Used to add childs dynamically.
     */
    protected static class ElementWithChilds {
        Element el;
        private List<ElementWithChilds> childs;

        public ElementWithChilds() {
        }

        public ElementWithChilds(Element el) {
            this.el = el;
        }

        public void addFirstChild(ElementWithChilds ch) {
            ensureChilds();
            childs.add(0, ch);
        }

        public void addChild(ElementWithChilds ch) {
            ensureChilds();
            childs.add(ch);
        }

        public boolean isChildsExist() {
            return childs != null;
        }

        public List<ElementWithChilds> getChilds() {
            return childs;
        }

        public void ensureChilds() {
            if (childs == null) {
                childs = new ArrayList<ElementWithChilds>();
            }
        }

        public void setChilds() {
            if (childs == null) {
                return;
            }
            Element[] arr = new Element[childs.size()];
            for (int i = 0; i < arr.length; i++) {
                ElementWithChilds ch = childs.get(i);
                ch.setChilds();
                arr[i] = ch.el;
            }
            ((AbstractDocument.BranchElement) el).replace(0, 0, arr);
        }
    }
}
