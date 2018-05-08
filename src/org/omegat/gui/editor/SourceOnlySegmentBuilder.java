package org.omegat.gui.editor;

import com.sun.javafx.binding.StringFormatter;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.*;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIThreadsUtil;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class to view only the source segments in a side by side editor arrangement.  Copied from
 * SegmentBuilder, attempted to refactor some common functionality via AbstractSegmentBuilder, however, not
 */
public class SourceOnlySegmentBuilder extends AbstractSegmentBuilder {


    static final Logger LOGGER = Logger.getLogger(SourceOnlySegmentBuilder.class.getName());

    SegmentBuilder translationBuilder;

    public SourceOnlySegmentBuilder(IEditor controller, Document3 doc, EditorSettings settings, SourceTextEntry ste, int segmentNumberInProject, boolean hasRTL, SegmentBuilder transBuilder) {
        super(controller, doc, settings, ste, segmentNumberInProject, hasRTL);
        this.translationBuilder = transBuilder;
    }

    public void createSegmentElement(final boolean isActive, TMXEntry trans, SegmentBuilder translationBuilder) {
        createSegmentElement(isActive, doc.getLength(), trans);
    }

    public void createSegmentElement(final boolean isActive, TMXEntry trans) {
        createSegmentElement(isActive, doc.getLength(), trans);
    }

    public void prependSegmentElement(final boolean isActive, TMXEntry trans) {
        createSegmentElement(isActive, 0, trans);
    }

    public void createSegmentElement(final boolean isActive, int initialOffset, TMXEntry trans) {
        UIThreadsUtil.mustBeSwingThread();

        displayVersion = globalVersions.incrementAndGet();  // CLG I THINK ONLY ONE SEGMENT BUILDER CAN UPDATE THIS OR IS THIS A DOC VERSION????
        this.active = isActive;

        doc.trustedChangesInProgress = true;
        StaticUIUtils.setCaretUpdateEnabled(controller.getEditor(IEditor.EditorType.SOURCE), false);
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
                    offset = initialOffset;
                }

                defaultTranslation = trans.defaultTranslation;
                if (!Core.getProject().getProjectProperties().isSupportDefaultTranslations()) {
                    defaultTranslation = false;
                }

                transExist = trans.isTranslated();
                noteExist = trans.hasNote();

                int beginOffset = offset;
                if (isActive) {
                    createActiveSegmentElement(trans, beginOffset);
                } else {
                    createInactiveSegmentElement(trans, beginOffset);  // Add one to beginOffset?
                }
                int endOffset = offset;

                beginPosP1 = doc.createPosition(beginOffset + 1);
                endPosM1 = doc.createPosition(endOffset - 1);
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            doc.trustedChangesInProgress = false;
            StaticUIUtils.setCaretUpdateEnabled(controller.getEditor(IEditor.EditorType.SOURCE), true);
        }
    }

    /**
     *
     * @param trans the new segment entry
     *
     * @throws BadLocationException the given insert position is not a valid
     *   position within the document
     */
    void createActiveSegmentElement(TMXEntry trans, int beginOffset) throws BadLocationException {

        try {

            // We don't display modification info in a source only editor

            /*if (EditorSettings.DISPLAY_MODIFICATION_INFO_ALL.equals(settings.getDisplayModificationInfo())
                    || EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED.equals(settings
                    .getDisplayModificationInfo())) {
                addModificationInfoPart(trans);
            }*/

            int prevOffset = offset;

            //sourceText = addActiveSourceSegPart( ste.getSrcText());
            sourceText = ste.getSrcText();

            int transLength = trans.translation == null ? 0 : trans.translation.length();

            // Add a newline to offset translation tag (if enabled...) Are there other markers I need to be aware of?
            if ( (EditorSettings.DISPLAY_MODIFICATION_INFO_ALL.equals(settings.getDisplayModificationInfo())
                    || EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED.equals(settings.getDisplayModificationInfo()))
                    && transLength > 0)
            {
                insert("\n", null);
            }

            activeTranslationBeginOffset = offset;
            AttributeSet normal = attrs(true, false, false, false);
            insert(sourceText, normal);
            activeTranslationEndOffset = offset;

            // Add a segment marker (we may not want to keep this, but does seem to improve readability
            AttributeSet attrSegmentMark = settings.getSegmentMarkerAttributeSet();
            insert(createSegmentMarkText(), attrSegmentMark);

            adjustSourcePosition(trans, beginOffset);

            // add extra line spacing to keep the editor rows in sync?
            insert("\n", null);

            posSourceBeg = doc.createPosition(prevOffset + (hasRTL ? 1 : 0));
            posSourceLength = sourceText.length();

            // Nor do we show translations

            // we always insert the source text
            String srcText = ste.getSrcText();
            if (Preferences.isPreference(Preferences.GLOSSARY_REPLACE_ON_INSERT)) {
                srcText = EditorUtils.replaceGlossaryEntries(srcText);
            }
            translationText = srcText;

            posTranslationBeg = null;

            doc.activeTranslationBeginM1 = doc.createPosition(activeTranslationBeginOffset);
            doc.activeTranslationEndP1 = doc.createPosition(activeTranslationEndOffset /*+ 1*/);

        } catch (OutOfMemoryError oome) {

            doc.remove(0, doc.getLength());

            System.gc();

            // There, that should do it, now inform the user
            long memory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
            Log.logErrorRB("OUT_OF_MEMORY", memory);
            Log.log(oome);
            Core.getMainWindow().showErrorDialogRB("TF_ERROR", "OUT_OF_MEMORY", memory);
            // Just quit, we can't help it anyway
            System.exit(0);

        }
    }

    /**
     * Create method for inactive segment.
     * @param trans TMX entry with translation
     * @throws BadLocationException
     */
    void createInactiveSegmentElement(TMXEntry trans, int beginOffset) throws BadLocationException {

        // We don't display modification info in a source only editor

        translationText = null;
        sourceText = ste.getSrcText();

        int prevOffset = offset;

        //sourceText = addInactiveSegPart(true, sourceText);
        int srcLength = sourceText.length();
        boolean rtl = controller.isSourceLangRTL();
        insertDirectionEmbedding(rtl);
        sourceText = insertTextWithTags(sourceText, true);
        insertDirectionEndEmbedding();

        // Adjust the position of this segment, if the translation text is longer than this source
        adjustSourcePosition(trans, beginOffset);

        insert("\n", null);
        setAlignment(prevOffset, offset, rtl);

        posSourceBeg = doc.createPosition(prevOffset + (hasRTL ? 1 : 0));
        posSourceLength = sourceText.length();

    }

    private void adjustSourcePosition(TMXEntry trans, int beginOffset){
        try{
            // First get the location of the translation text
            Rectangle startT = controller.getEditor(IEditor.EditorType.TRANSLATION).modelToView(translationBuilder.getStartPosition());  // is this actually the offset of the translation.... o
            Rectangle endT = controller.getEditor(IEditor.EditorType.TRANSLATION).modelToView(translationBuilder.getEndPosition()-1);  // Assume translation contains an extra newline
            //Rectangle unionT = startT.union(endT);

            // Second, get the location of the source text
            Rectangle startS = controller.getEditor(IEditor.EditorType.SOURCE).modelToView(beginOffset);
            Rectangle endS = controller.getEditor(IEditor.EditorType.SOURCE).modelToView(offset );
            //Rectangle unionS = startS.union(endS);


           /* LOGGER.warning(String.format("aligned source: %s, \nTrans start/end: (%d, %d), Source start/end: (%d, %d). \nTrans union (%f, %f) height: %f, Source union (%f, %f) height: %f",
                    sourceText, translationBuilder.getStartPosition(), translationBuilder.getEndPosition(), beginOffset, offset+1,
                    unionT.getX(), unionT.getY(), unionT.getHeight(), unionS.getX(), unionS.getY(), unionS.getHeight() ));*/

            int editorWdith = (int)controller.getEditor(IEditor.EditorType.SOURCE).getSize().getWidth();
            int rowHeight = (int)startS.getHeight();
            // Not adjusting for border...

            // Get the number of rows the source and translation span
            int sourceRows = ((int)endS.getY() - (int)startS.getY())/rowHeight + 1;
            int transRows = ((int)endT.getY() - (int)startT.getY())/rowHeight + 1;
            // Now calculate the actual row wrapping for our segment
            double sourceDocLength  = Math.ceil( (sourceRows*editorWdith - (startS.getX()) - (editorWdith-endS.getX()))/editorWdith );
            double transDocLength   = Math.ceil( (transRows*editorWdith - (startT.getX()) - (editorWdith-endT.getX()))/editorWdith) ;

            // Insert extra newlines for each row in the tranlation pane, but not in the source pane
            for(; sourceDocLength  < transDocLength; sourceDocLength++){
                insert("\n", null);
            }


            if(transDocLength < sourceDocLength){
                translationBuilder.adjustSideBySidePosition((transDocLength), (sourceDocLength));
            }

        }
        catch (Exception e){
            // TODO ADD BETTER ERROR HANDLING TO AVOID THESE.. deal with later?
            LOGGER.warning("INVALID location");
        }

    }

}
