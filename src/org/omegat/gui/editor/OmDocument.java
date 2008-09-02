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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

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
class OmDocument extends AbstractDocument implements StyledDocument {

    /** Editor controller. */
    protected final EditorController controller;

    /** Root element for full document. */
    private OmElementMain root;

    /**
     * Positions of begin and end translation. Since positions moved on each
     * text change, we can just use text inside these positions when we need new
     * translation text.
     */
    protected Position activeTranslationBegin, activeTranslationEnd;

    /**
     * Constructor.
     */
    public OmDocument(EditorController controller) {
        super(new OmContent(), new StyleContext());
        this.controller = controller;
        root = new OmElementMain();
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
    OmElementSegment[] initialize(StringBuilder text,
            SegmentElementsDescription[] descriptions)
            throws BadLocationException {
        try {
            writeLock();
            getContent().insertString(0, text.toString());

            int offset = 0;
            OmElementSegment[] segments = new OmElementSegment[descriptions.length];
            for (int i = 0; i < segments.length; i++) {
                segments[i] = new OmElementSegment(root, null,
                        descriptions[i].ste,
                        descriptions[i].segmentNumberInProject);
                Element[] paragraphs = descriptions[i]
                        .createElementsForSegment(this, segments[i], text
                                .substring(offset, offset
                                        + descriptions[i].fullSegmentLength),
                                offset);
                offset += descriptions[i].fullSegmentLength;
                segments[i].replace(0, 0, paragraphs);
            }
            root.replace(0, 0, segments);

            return segments;
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
    void replaceSegment(int segmentIndex, OmEditorKit kit, boolean isActive)
            throws BadLocationException {
        try {
            writeLock();

            activeTranslationBegin = null;
            activeTranslationEnd = null;
            getData().setEditableRange(0, 0);

            OmElementSegment section = controller.m_docSegList[segmentIndex];

            int startSegmentPos = section.getStartOffset();
            int endSegmentPos = section.getEndOffset();

            // Prepare text and pre-elements
            StringBuilder newText = new StringBuilder();
            SegmentElementsDescription desc = new SegmentElementsDescription(
                    this, newText, section.ste, section.segmentNumberInProject,
                    isActive);

            /**
             * We are inserting new string into end of old segment, because
             * AbstractDocument resizes element in previous position. So, we
             * will have old segment element with both old and new text.
             */
            getContent().insertString(endSegmentPos, newText.toString());
            /**
             * Then we remove old text, and have only new text in context.
             */
            getContent().remove(startSegmentPos,
                    endSegmentPos - startSegmentPos);

            // Change document elements
            Element[] paragraphs = desc.createElementsForSegment(this, section,
                    newText.toString(), startSegmentPos);

            replaceSegmentElements(segmentIndex, kit, paragraphs);

            if (isActive) {
                int segStart = section.getStartOffset();

                activeTranslationBegin = getData().createPosition(
                        segStart + desc.translationBeginTagEnd,
                        OmContent.POSITION_TYPE.BEFORE_EDITABLE);
                activeTranslationEnd = getData().createPosition(
                        segStart + desc.translationEndTagStart,
                        OmContent.POSITION_TYPE.AFTER_EDITABLE);
                getData().setEditableRange(activeTranslationBegin.getOffset(),
                        activeTranslationEnd.getOffset());
            } else {
                activeTranslationBegin = null;
                activeTranslationEnd = null;
                getData().setEditableRange(0, 0);
            }
        } finally {
            writeUnlock();
        }
    }

    /**
     * Replace SegmentElement childs and child UI views.
     * 
     * @param segmentIndex
     *            segment index
     * @param kit
     *            EditorKit for create UI Views
     * @param newElements
     *            new ParagraphElements
     */
    private void replaceSegmentElements(int segmentIndex, EditorKit kit,
            Element[] newElements) {
        OmElementSegment seg = controller.m_docSegList[segmentIndex];
        seg.replace(0, seg.getElementCount(), newElements);

        // Change views
        View mainDocView = controller.editor.getUI().getRootView(
                controller.editor).getView(0);
        View segmentView = mainDocView.getView(segmentIndex);
        View[] nv = new View[seg.getElementCount()];
        for (int i = 0; i < nv.length; i++) {
            nv[i] = kit.getViewFactory().create(seg.getElement(i));
        }
        segmentView.replace(0, segmentView.getViewCount(), nv);
    }

    /**
     * Get text from document.
     */
    private String getTextBetween(int start, int end)
            throws BadLocationException {
        return getText(start, end - start);
    }

    /**
     * Get text from document inside element.
     */
    private String getTextInside(Element el) throws BadLocationException {
        return getTextBetween(el.getStartOffset(), el.getEndOffset());
    }

    /**
     * Extract translation from document, i.e. text between
     * activeTranslationBegin and activeTranslationEnd.
     * 
     * @return translation text
     * @throws BadLocationException
     */
    protected String extractTranslation() throws BadLocationException {
        return getTextBetween(activeTranslationBegin.getOffset(),
                activeTranslationEnd.getOffset());
    }

    /**
     * Handle user's document change for rebuild segments.
     */
    @Override
    protected void postRemoveUpdate(DefaultDocumentEvent chng) {
        UIThreadsUtil.mustBeSwingThread();

        super.postRemoveUpdate(chng);

        // we have to rebuild segment each time, because we need to check
        // spelling, and possible rebuild paragraphs
        try {
            int segmentIndex = root.getElementIndex(chng.getOffset());

            rebuildElementsForSegment(chng, segmentIndex);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Handle user's document change for rebuild segments.
     */
    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        UIThreadsUtil.mustBeSwingThread();

        super.insertUpdate(chng, attr);
        try {
            int segmentIndex = root.getElementIndex(chng.getOffset());

            // we have to rebuild segment each time, because we need to check
            // spelling, and possible rebuild paragraphs
            rebuildElementsForSegment(chng, segmentIndex);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Rebuild elements fro specified segment after user's change.
     * 
     * @param chng
     *            change
     * @param segmentIndex
     *            segment index
     * @throws BadLocationException
     */
    private void rebuildElementsForSegment(DefaultDocumentEvent chng,
            int segmentIndex) throws BadLocationException {
        try {
            writeLock();

            OmElementSegment segElement;
            if (chng != null) {
                segElement = (OmElementSegment) root.getElement(root
                        .getElementIndex(chng.getOffset()));
            } else {
                segElement = (OmElementSegment) root.getElement(segmentIndex);
            }
            int offsetFromDocumentBegin = segElement.getStartOffset();
            // OmDocument.dump(segElement, 0);

            String fullSegmentText = getTextInside(segElement);
            String currentTranslation = extractTranslation();

            SegmentElementsDescription desc = new SegmentElementsDescription(
                    this, new StringBuilder(), segElement.ste.getSrcText(),
                    currentTranslation, true, segElement.segmentNumberInProject);

            Element[] added = desc.createElementsForSegment(this, segElement,
                    fullSegmentText, offsetFromDocumentBegin);

            replaceSegmentElements(segmentIndex,
                    (OmEditorKit) controller.editor.getEditorKit(), added);

            segElement.replace(0, segElement.getElementCount(), added);
        } finally {
            writeUnlock();
        }
    }

    /**
     * Rebuild elements fro specified segment after user's change.
     * 
     * @param segmentIndex
     *            segment index
     */
    void rebuildElementsForSegment(int segmentIndex) {
        UIThreadsUtil.mustBeSwingThread();
        try {
            rebuildElementsForSegment(null, segmentIndex);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
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
        return root.getElement(root.getElementIndex(pos));
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

            // get root element view
            View mainView = controller.editor.getUI().getRootView(
                    controller.editor).getView(0);

            // create new views for segments
            View[] nv = new View[root.getElementCount()];
            for (int i = 0; i < nv.length; i++) {
                nv[i] = controller.editor.getEditorKit().getViewFactory()
                        .create(root.getElement(i));
            }
            // replace view
            mainView.replace(0, mainView.getViewCount(), nv);
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
     * Required implementation by AbstractDocument.
     */
    public Element getCharacterElement(int pos) {
        throw new UnsupportedOperationException();
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
            return "main";
        }
    }

    /**
     * Element for one segment, which includes paragraphs.
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
            return "segment";
        }
    }

    /**
     * Element for paragraphs, i.e. lines inside "segment" element.
     */
    public class OmElementParagraph extends BranchElement {
        public OmElementParagraph(Element p, AttributeSet a) {
            super(p, a);
        }

        public String getName() {
            return "paragraph";
        }
    }

    /**
     * Implement own TextElement. We can't use standard LeafElement, because we
     * want to create "before/inside/after" positions.
     */
    public class OmElementText extends AbstractElement {
        protected final Position p0, p1;

        public OmElementText(Element parent, AttributeSet a, int offs0,
                int offs1, OmContent.POSITION_TYPE positionType) {
            super(parent, a);
            p0 = getData().createPosition(offs0, positionType);
            p1 = getData().createPosition(offs1, positionType);
        }

        public OmElementText(Element parent, AttributeSet a, int offs) {
            super(parent, a);
            p0 = getData().createPosition(offs,
                    OmContent.POSITION_TYPE.BEFORE_EDITABLE);
            p1 = getData().createPosition(offs,
                    OmContent.POSITION_TYPE.AFTER_EDITABLE);
        }

        public String getName() {
            return "text";
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

    /**
     * Method for debugging purpose only. It shows element better than standard
     * Element.dump method.
     * 
     * @param el
     *            element
     * @param indentAmount
     */
    protected static void dump(AbstractElement el, int indentAmount) {
        PrintWriter out;
        try {
            out = new PrintWriter(
                    new OutputStreamWriter(System.out, "JavaEsc"), true);
        } catch (UnsupportedEncodingException e) {
            out = new PrintWriter(System.out, true);
        }
        indent(out, indentAmount);
        if (el.getName() == null) {
            out.print("<??");
        } else {
            out.print("<" + el.getName());
        }
        if (el.getAttributeCount() > 0) {
            out.println("");
            // dump the attributes
            Enumeration<?> names = el.getAttributeNames();
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                indent(out, indentAmount + 1);
                out.println(name + "=" + el.getAttribute(name));
            }
            indent(out, indentAmount);
        }
        out.print(">");

        if (el.isLeaf()) {
            indent(out, indentAmount + 1);

            Position p0 = ((OmElementText) el).p0;
            Position p1 = ((OmElementText) el).p1;

            out.print("[" + p0 + "," + p1 + "]");
            Content c = ((OmDocument) el.getDocument()).getContent();
            try {
                String contentStr = c.getString(el.getStartOffset(), el
                        .getEndOffset()
                        - el.getStartOffset())/* .trim() */;
                if (contentStr.length() > 40) {
                    contentStr = contentStr.substring(0, 40) + "...";
                }
                contentStr = contentStr.replace("\n", "'\\n'");
                out.println("[" + contentStr + "]");
            } catch (BadLocationException e) {
                ;
            }

        } else {
            out.println();
            int n = el.getElementCount();
            for (int i = 0; i < n; i++) {
                AbstractElement e = (AbstractElement) el.getElement(i);
                dump(e, indentAmount + 1);
            }
        }
    }

    private static final void indent(PrintWriter out, int n) {
        for (int i = 0; i < n; i++) {
            out.print("  ");
        }
    }
}
