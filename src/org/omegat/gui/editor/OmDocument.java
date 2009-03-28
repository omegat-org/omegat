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

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.CompositeView;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for implement own document.
 * 
 * It's better way is to use own implementation instead standard
 * implementations, because we can create Elements much better for our editing,
 * then control they.
 * 
 * We are creating SegmentElement for each displayed segment, then create
 * OmElementParagraph for each line inside it, then create TextElement for each
 * different formatting inside one line. So, number of SegmentElements always
 * will be equals of displayed segments.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class OmDocument extends AbstractDocument implements StyledDocument {

    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(OmDocument.class
            .getName());

    enum ORIENTATION {
        /** Both segments is left aligned. */
        LTR,
        /** Both segments is right aligned. */
        RTL,
        /** Segments have different alignment, depends of language alignment. */
        DIFFER
    };

    /** Editor controller. */
    protected final EditorController controller;

    /** Root element for full document. */
    private OmElementMain root;

    private ORIENTATION currentOrientation;

    /**
     * 'Unflushed' text, i.e. text for newly created segments, which was not
     * added to OmContent yet.
     */
    protected final StringBuilder unflushedText = new StringBuilder();

    /**
     * Positions of begin and end translation. Since positions moved on each
     * text change, we can just use text inside these positions when we need new
     * translation text. Position setted BEFORE and AFTER space, because we
     * shouldn't be able to enter char outside these marks.
     */
    protected Position activeTranslationBegin, activeTranslationEnd;

    protected int activeSegmentIndex;

    /**
     * Constructor.
     */
    public OmDocument(EditorController controller) {
        super(new OmContent(), new StyleContext());
        getData().setDocument(this);
        this.controller = controller;
        root = new OmElementMain();
        putProperty("i18n", Boolean.TRUE);
    }

    /**
     * Initialize document, i.e. load text from segments, then create elements
     * for all segments.
     * 
     * @param text
     *            document text
     * @param segments
     *            all segments
     * @param descriptions
     *            segment's descriptions for create SegmentElements
     * @return list of segment's elements
     * @throws BadLocationException
     *             exception
     */
    OmElementSegment[] initialize(SegmentElementsDescription[] descriptions,
            ORIENTATION orientation) throws BadLocationException {

        OmElementSegment[] segElements = new OmElementSegment[descriptions.length];

        currentOrientation = orientation;
        try {
            writeLock();

            for (int i = 0; i < descriptions.length; i++) {
                segElements[i] = (OmElementSegment) descriptions[i]
                        .createSegmentElement(root, false,
                                controller.sourceLangIsRTL,
                                controller.targetLangIsRTL);
            }

            getData().flush(unflushedText, 0, 0);

            root.replace(0, 0, segElements);

            putProperty( TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_LTR );

            return segElements;
        } finally {
            writeUnlock();
        }
    }

    /**
     * Getter for our content.
     * 
     * @return content
     */
    public OmContent getData() {
        return (OmContent) getContent();
    }

    /**
     * Create info for undo/redo operations.
     * 
     * @return
     */
    public UndoableEdit createUndo() {
        return new UndoInfo(extractTranslation());
    }

    /**
     * Replace segment text, Elements and UI Views. It depends of segment active
     * or not.
     * 
     * @param segmentIndex
     *            segment index
     * @param kit
     *            EditorKit for create UI Views
     * @param isActive
     *            is segment active
     * @throws BadLocationException
     *             exception
     */
    void replaceSegment(int segmentIndex, boolean isActive) {
        try {
            writeLock();

            OmElementSegment seg = (OmElementSegment) root
                    .getElement(segmentIndex);

            activeTranslationBegin = null;
            activeTranslationEnd = null;
            getData().setEditableRange(0, 0);

            int startSegmentPos = seg.getStartOffset();
            int endSegmentPos = seg.getEndOffset();

            SegmentElementsDescription desc = new SegmentElementsDescription(
                    this, seg.ste, controller.getEntryNumber(segmentIndex));
            Element el = desc.createSegmentElement(root, isActive,
                    controller.sourceLangIsRTL, controller.targetLangIsRTL);

            if (isActive) {
                activeTranslationBegin = getData().createUnflushedPosition(
                        desc.activeTranslationBeginOffset);
                activeTranslationEnd = getData().createUnflushedPosition(
                        desc.activeTranslationEndOffset);
            }

            int updatedLen=Math.max(endSegmentPos-startSegmentPos, unflushedText.length());

            getData().flush(unflushedText, startSegmentPos,
                    endSegmentPos - startSegmentPos);
            // replace element
            root.replace(segmentIndex, 1, new Element[] { el });

            /*
             * We have to call 'postRemoveUpdate' for execute 'updateBidi'. It's
             * not so good hack, but there is no way to execute it directly.
             */
            DefaultDocumentEvent chng = new DefaultDocumentEvent(
                    startSegmentPos, updatedLen,
                    DocumentEvent.EventType.CHANGE);
            super.postRemoveUpdate(chng);

            // replace view
            View mainDocView = controller.editor.getUI().getRootView(
                    controller.editor).getView(0);
            mainDocView.replace(segmentIndex, 1,
                    new View[] { OmEditorKit.FACTORY.create(el) });

            if (isActive) {
                getData().setEditableRange(activeTranslationBegin.getOffset(),
                        activeTranslationEnd.getOffset());
                activeSegmentIndex = segmentIndex;
            } else {
                activeSegmentIndex = -1;
            }
        } finally {
            writeUnlock();
        }
    }

    /**
     * Replace elements for translation.
     * 
     * @param newTranslation
     *            new translation text, or null if we need to use text in ths
     *            OmContent
     */
    void replaceTranslationElements(String newTranslation) {
        try {
            writeLock();

            OmElementSegment seg = (OmElementSegment) root
                    .getElement(activeSegmentIndex);
            OmElementSegPart segPart = (OmElementSegPart) seg.getElement(1);

            View mainDocView = controller.editor.getUI().getRootView(
                    controller.editor).getView(0);
            View segView = mainDocView.getView(activeSegmentIndex);

            int startSegmentPos = segPart.getStartOffset();
            int endSegmentPos = segPart.getEndOffset();

            SegmentElementsDescription desc = new SegmentElementsDescription(
                    this, seg.ste, controller.getEntryNumber(activeSegmentIndex));

            Element el;
            if (newTranslation != null) {
                // need to use new text
                el = desc.createTranslationElement(seg, seg, newTranslation,
                        controller.targetLangIsRTL);
                getData().flushTranslationElements(unflushedText,
                        startSegmentPos, endSegmentPos);
            } else {
                // need to use exist text
                el = desc.createTranslationElement(seg, seg,
                        extractTranslation(), controller.targetLangIsRTL);
                getData().flushTranslationElements(null, startSegmentPos,
                        endSegmentPos);
            }
            unflushedText.setLength(0);

            // replace element
            seg.replace(1, 1, new Element[] { el });

            // replace view
            segView
                    .replace(1, 1,
                            new View[] { OmEditorKit.FACTORY.create(el) });
        } finally {
            writeUnlock();
        }
    }

    /**
     * Get text from document.
     */
    private String getTextBetween(int start, int end)
            throws BadLocationException {
        return getText(start, end - start);
    }

    /**
     * Extract translation from document, i.e. text between
     * activeTranslationBegin and activeTranslationEnd.
     * 
     * @return translation text
     * @throws BadLocationException
     */
    protected String extractTranslation() {
        if (activeTranslationBegin == null || activeTranslationEnd == null) {
            return null;
        }
        try {
            return getTextBetween(activeTranslationBegin.getOffset() + 1,
                    activeTranslationEnd.getOffset() - 1);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, "Error extract translation", ex);
            return null;
        }
    }

    /**
     * Handle user's document change for rebuild segments.
     */
    @Override
    protected void postRemoveUpdate(DefaultDocumentEvent chng) {
        UIThreadsUtil.mustBeSwingThread();

        super.postRemoveUpdate(chng);

        /*
         * we have to rebuild segment's elements each time, because we need to
         * check spelling, and possible rebuild paragraphs
         */
        replaceTranslationElements(null);
    }

    /**
     * Handle user's document change for rebuild segments.
     */
    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        UIThreadsUtil.mustBeSwingThread();

        super.insertUpdate(chng, attr);

        /*
         * we have to rebuild segment's elements each time, because we need to
         * check spelling, and possible rebuild paragraphs
         */
        replaceTranslationElements(null);
    }

    /**
     * Set new orientation.
     */
    void setOrientation(ORIENTATION newOrientation) {
        currentOrientation = newOrientation;
        rebuildViews();
    }

    /**
     * Rebuild all views for document.
     */
    private void rebuildViews() {
        // View rootView = controller.editor.getUI()
        // .getRootView(controller.editor);
        // rootView.replace(1, 1, new View[] { OmEditorKit.FACTORY.create(root)
        // });
        // controller.editor.repaint();

        // get root element view
        View mainView = controller.editor.getUI()
                .getRootView(controller.editor).getView(0);

        // create new views for segments
        View[] nv = new View[root.getElementCount()];
        for (int i = 0; i < nv.length; i++) {
            nv[i] = controller.editor.getEditorKit().getViewFactory().create(
                    root.getElement(i));
        }
        // replace view
        mainView.replace(0, mainView.getViewCount(), nv);
    }
    
    /**
     * Hide misspelled word in all elements, because we added it into local
     * dictionary or ignored words.
     * 
     * Travel by all elements and remove the same misspelled words.
     * 
     * @param word
     */
    void hideMisspelledWord(final String word) {
        for (int i = 0; i < root.getElementCount(); i++) {
            OmElementSegment seg = (OmElementSegment) root.getElement(i);
            for (int j = 0; j < seg.getElementCount(); j++) {
                if (!(seg.getElement(j) instanceof OmElementSegPart)) {
                    continue;
                }
                OmElementSegPart segPart = (OmElementSegPart) seg.getElement(j);
                for (int k = 0; k < segPart.getElementCount(); k++) {
                    OmElementParagraph par = (OmElementParagraph) segPart
                            .getElement(k);
                    for (int l = 0; l < par.getElementCount(); l++) {
                        if (!(par.getElement(l) instanceof OmElementText)) {
                            continue;
                        }
                        OmElementText tx = (OmElementText) par.getElement(l);
                        if (tx.misspelled == null) {
                            continue;
                        }
                        for (int m = 0; m < tx.misspelled.size(); m++) {
                            MisspelledRegion reg = tx.misspelled.get(m);
                            if (reg.len == word.length()) {
                                try {
                                    String ew = getData().getString(reg.off,
                                            reg.len);
                                    if (word.equalsIgnoreCase(ew)) {
                                        /*
                                         * the same word, need to remove from
                                         * misspelled list
                                         */
                                        tx.misspelled.remove(m);
                                        m--;
                                    }
                                } catch (BadLocationException ex) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculate segment index in specified location.
     * 
     * @return
     */
    public int getSegmentAtLocation(int offset) {
        return root.getElementIndex(offset);
    }

    /**
     * Check if specified position inside active segment, i.e. between segment's
     * marks.
     */
    protected boolean isInsideActiveSegPart(int pos) {
        if (activeSegmentIndex < 0) {
            // there is no active segment
            return false;
        }
        OmElementSegment seg = (OmElementSegment) root
                .getElement(activeSegmentIndex);
        OmElementSegPart segPart = (OmElementSegPart) seg.getElement(1);
        return segPart.getStartOffset() <= pos && segPart.getEndOffset() >= pos;
    }
    
    /**
     * Required implementation by AbstractDocument.
     */
    @Override
    public Element getDefaultRootElement() {
        return root;
    }

    /**
     * Required implementation by AbstractDocument.
     */
    @Override
    public Element getParagraphElement(int pos) {
        Element e= root.getElement(root.getElementIndex(pos));
        Element p=e.getElement(e.getElementIndex(pos));
        return p;
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public void removeStyle(String nm) {
        throw new UnsupportedOperationException();
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public void setParagraphAttributes(int offset, int length, AttributeSet s,
            boolean replace) {
        throw new UnsupportedOperationException();
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public void setCharacterAttributes(int offset, int length, AttributeSet s,
            boolean replace) {
        throw new UnsupportedOperationException();
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public Font getFont(AttributeSet attr) {
        StyleContext styles = (StyleContext) getAttributeContext();
        return styles.getFont(attr);
    }

    /**
     * Set new font. It changes font for root element only, then all childs will
     * use it.
     * 
     * @param newFont
     *            new font
     */
    public void setFont(Font newFont) {
        try {
            writeLock();
            
            // set font for root element
            root.addAttribute(StyleConstants.FontFamily, newFont.getFamily());
            root.addAttribute(StyleConstants.FontSize, newFont.getSize());
            rebuildViews();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public Style addStyle(String nm, Style parent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets a character element based on a position.
     */
    public Element getCharacterElement(int pos) {
        Element e = null;
        for (e = getDefaultRootElement(); ! e.isLeaf(); ) {
            int index = e.getElementIndex(pos);
            e = e.getElement(index);
        }
        return e;
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public Color getForeground(AttributeSet attr) {
        StyleContext styles = (StyleContext) getAttributeContext();
        return styles.getForeground(attr);
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public Color getBackground(AttributeSet attr) {
        StyleContext styles = (StyleContext) getAttributeContext();
        return styles.getBackground(attr);
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public Style getStyle(String nm) {
        StyleContext styles = (StyleContext) getAttributeContext();
        return styles.getStyle(nm);
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public Style getLogicalStyle(int p) {
        throw new UnsupportedOperationException();
    }

    /**
     * Required implementation by AbstractDocument.
     */
    public void setLogicalStyle(int pos, Style s) {
        throw new UnsupportedOperationException();
    }

    /**
     * Root element for document.
     */
    public class OmElementMain extends BranchElement {
        public OmElementMain() {
            super(null, null);
        }

        public String getName() {
            return "OmElementMain";
        }
    }

    /**
     * Element for one segment, which includes segment parts.
     */
    public class OmElementSegment extends BranchElement {
        SourceTextEntry ste;

        int segmentNumberInProject;

        public OmElementSegment(Element p, AttributeSet a, SourceTextEntry ste,
                int segmentNumberInProject) {
            super(p, a);
            this.ste = ste;
            this.segmentNumberInProject = segmentNumberInProject;
        }

        public String getName() {
            return "OmElementSegment";
        }
    }

    /**
     * Element for one segmet part. Segment part could be source or target
     * segment text, or segment key, etc. Only one segment part can be active
     * for change. Includes paragraphs.
     */
    public class OmElementSegPart extends BranchElement {
        private boolean langRTL;

        public OmElementSegPart(Element p, AttributeSet a, boolean langRTL) {
            super(p, a);
            this.langRTL = langRTL;
            addAttribute(TextAttribute.RUN_DIRECTION,
                    langRTL ? TextAttribute.RUN_DIRECTION_RTL
                            : TextAttribute.RUN_DIRECTION_LTR);
        }

        public String getName() {
            return "OmElementSegPart";
        }

        public boolean isLangRTL() {
            return langRTL;
        }

        public boolean isRightAligned() {
            switch (currentOrientation) {
            case LTR:
                return false;
            case RTL:
                return true;
            }
            return langRTL;
        }
    }

    /**
     * Element for paragraphs, i.e. lines inside "segment part" element.
     */
    public class OmElementParagraph extends BranchElement {

        public OmElementParagraph(Element p, AttributeSet a) {
            super(p, a);
        }

        public String getName() {
            return "OmElementParagraph";
        }
    }

    /**
     * Element for paragraphs, i.e. lines inside "segment part" element.
     */
    public class OmElementSegmentsSeparator extends AbstractElement {
        protected final Position p0, p1;

        public OmElementSegmentsSeparator(Element p, AttributeSet a) {
            super(p, a);
            p0 = getData().createUnflushedPosition(unflushedText.length());
            unflushedText.append('\n');
            p1 = getData().createUnflushedPosition(unflushedText.length());
        }

        public int getStartOffset() {
            return p0.getOffset();
        }

        public int getEndOffset() {
            return p1.getOffset();
        }

        public String getName() {
            return "OmElementSegmentsSeparator";
        }

        public int getElementIndex(int pos) {
            return -1;
        }

        public Element getElement(int index) {
            return null;
        }

        public int getElementCount() {
            return 0;
        }

        public boolean isLeaf() {
            return true;
        }

        public boolean getAllowsChildren() {
            return false;
        }

        public Enumeration<?> children() {
            return null;
        }
    }
    
    /**
     * Class for mark misspelled parts.
     */
    public static class MisspelledRegion {
        protected final int off, len;

        public MisspelledRegion(int off, int len) {
            this.off = off;
            this.len = len;
        }
    }

    /**
     * Implement own TextElement. We can't use standard LeafElement, because we
     * want to create "before/inside/after" positions.
     */
    public class OmElementText extends AbstractElement {
        protected final Position p0, p1;
        protected List<MisspelledRegion> misspelled;

        public OmElementText(Element parent, AttributeSet a, CharSequence text) {
            super(parent, a);
            p0 = getData().createUnflushedPosition(unflushedText.length());
            unflushedText.append(text);
            p1 = getData().createUnflushedPosition(unflushedText.length());
        }

        public String getName() {
            return "OmElementText";
        }

        public int getStartOffset() {
            return p0.getOffset();
        }

        public int getEndOffset() {
            return p1.getOffset();
        }

        public int getElementIndex(int pos) {
            return -1;
        }

        public Element getElement(int index) {
            return null;
        }

        public int getElementCount() {
            return 0;
        }

        public boolean isLeaf() {
            return true;
        }

        public boolean getAllowsChildren() {
            return false;
        }

        public Enumeration<?> children() {
            return null;
        }

        public void addMisspelled(final MisspelledRegion reg) {
            if (misspelled == null) {
                misspelled = new ArrayList<MisspelledRegion>();
            }
            misspelled.add(reg);
        }
    }

    /**
     * Implement own TextElement. We can't use standard LeafElement, because we
     * want to create "before/inside/after" positions.
     */
    public class OmElementSegmentMark extends AbstractElement {
        protected final Position p0, p1;
        protected final boolean isBeginMark;
        protected final String label;

        public OmElementSegmentMark(boolean isBeginMark, Element parent,
                AttributeSet a, String label) {
            super(parent, a);
            this.isBeginMark = isBeginMark;
            this.label = label;
            p0 = getData().createUnflushedPosition(unflushedText.length());
            unflushedText.append(label.replaceAll(".", "*"));
            p1 = getData().createUnflushedPosition(unflushedText.length());
        }

        public String getName() {
            return "OmElementSegmentMark";
        }

        public int getStartOffset() {
            return p0.getOffset();
        }

        public int getEndOffset() {
            return p1.getOffset();
        }

        public int getElementIndex(int pos) {
            return -1;
        }

        public Element getElement(int index) {
            return null;
        }

        public int getElementCount() {
            return 0;
        }

        public boolean isLeaf() {
            return true;
        }

        public boolean getAllowsChildren() {
            return false;
        }

        public Enumeration<?> children() {
            return null;
        }
    }

    protected static void dump(View v, int indentAmount) {
        if (v == null)
            return;

        indent(indentAmount);
        System.out.println("Class: " + v.getClass().getCanonicalName() + " "
                + v);
        if (v instanceof BoxView) {
            BoxView bv = (BoxView) v;
            indent(indentAmount);
            System.out
                    .println("   w:" + bv.getWidth() + " h:" + bv.getHeight());
        }
        if (v instanceof ViewLabel) {
            ViewLabel elv = (ViewLabel) v;
            dump(elv.getElement(), indentAmount + 2);
        }
        if (v instanceof CompositeView) {
            CompositeView cv = (CompositeView) v;
            for (int i = 0; i < cv.getViewCount(); i++) {
                dump(cv.getView(i), indentAmount + 2);
            }
        }
    }

    /**
     * Method for debugging purpose only. It shows element better than standard
     * Element.dump method.
     * 
     * @param el
     *            element
     * @param indentAmount
     */
    public static void dump(Element ell, int indentAmount) {
        AbstractElement el = (AbstractElement) ell;
        indent(indentAmount);
        if (el.getName() == null) {
            System.out.print("<??");
        } else {
            System.out.print("<" + el.getName());
        }
        if (el.getAttributeCount() > 0) {
            System.out.println();
            // dump the attributes
            Enumeration<?> names = el.getAttributeNames();
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                indent(indentAmount + 1);
                System.out.println(name + "=" + el.getAttribute(name));
            }
            indent(indentAmount);
        }
        System.out.print(">");

        if (el.isLeaf()) {
            indent(indentAmount + 1);

            if (el instanceof OmElementText) {
                Position p0 = ((OmElementText) el).p0;
                Position p1 = ((OmElementText) el).p1;

                System.out.print("[" + p0 + "," + p1 + "]");
            } else {
                System.out.print("[" + el.getStartOffset() + ","
                        + el.getEndOffset() + "]");
            }
            Content c = ((OmDocument) el.getDocument()).getContent();
            try {
                String contentStr = c.getString(el.getStartOffset(), el
                        .getEndOffset()
                        - el.getStartOffset())/* .trim() */;
                if (contentStr.length() > 40) {
                    contentStr = contentStr.substring(0, 40) + "...";
                }
                contentStr = contentStr.replace("\n", "'\\n'");
                System.out.println("[" + contentStr + "]");
            } catch (Exception e) {
                System.out.println("<unk>");
            }

        } else {
            System.out.println();
            int n = el.getElementCount();
            for (int i = 0; i < n; i++) {
                AbstractElement e = (AbstractElement) el.getElement(i);
                dump(e, indentAmount + 1);
            }
        }
    }

    private static final void indent(int n) {
        for (int i = 0; i < n; i++) {
            System.out.print("  ");
        }
    }

    /**
     * Undo information with previous text.
     */
    protected class UndoInfo extends AbstractUndoableEdit {
        protected final String oldText;
        protected String newText;

        public UndoInfo(String oldText) {
            this.oldText = oldText;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            newText = extractTranslation();
            replaceTranslationElements(oldText);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            replaceTranslationElements(newText);
        }
    }
}
