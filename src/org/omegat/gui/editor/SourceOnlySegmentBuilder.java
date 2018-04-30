package org.omegat.gui.editor;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.*;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIThreadsUtil;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class to view only the source segments in a side by side translationEditor arrangement
 */
public class SourceOnlySegmentBuilder extends AbstractSegmentBuilder {


    static final Logger LOGGER = Logger.getLogger(SourceOnlySegmentBuilder.class.getName());

    public SourceOnlySegmentBuilder(IEditor controller, Document3 doc, EditorSettings settings, SourceTextEntry ste, int segmentNumberInProject, boolean hasRTL) {
        super(controller, doc, settings, ste, segmentNumberInProject, hasRTL);
        //TODO SINCE WE WON'T EDIT THE SOURCE, SHOULD hasRTL be set to false?
    }


    public void createSegmentElement(final boolean isActive, int initialOffset, TMXEntry trans) {
        UIThreadsUtil.mustBeSwingThread();

        displayVersion = globalVersions.incrementAndGet();  // TODO CLG FIX THIS... I THINK ONLY ONE SEGMENT BUILDER CAN UPDATE THIS OR IS THIS A DOC VERSION????
        this.active = isActive;

        doc.trustedChangesInProgress = true;
        StaticUIUtils.setCaretUpdateEnabled(controller.getEditor(IEditor.EditorType.SOURCE), false); // TODO SHOULD HAVE EDITOR
        try {
            try {
                if (beginPosP1 != null && endPosM1 != null) {// CLG note this removes translation (current) from the Doc via DocumentFilter3
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
    void createActiveSegmentElement(TMXEntry trans) throws BadLocationException {

        try {

            // We don't display modification info in a source only translationEditor

            /*if (EditorSettings.DISPLAY_MODIFICATION_INFO_ALL.equals(settings.getDisplayModificationInfo())
                    || EditorSettings.DISPLAY_MODIFICATION_INFO_SELECTED.equals(settings
                    .getDisplayModificationInfo())) {
                addModificationInfoPart(trans);
            }*/

            int prevOffset = offset;

            //sourceText = addActiveSourceSegPart( ste.getSrcText());
            sourceText = ste.getSrcText();
            //LOGGER.warning("CLG active source text: " + sourceText + ", translation: " + trans.translation);

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

            // TODO what if they have a very long source/translation string or the window is scrunched?

            // should we add extra line spacing to keep the editor rows in sync?
            insert("\n", null);

            posSourceBeg = doc.createPosition(prevOffset + (hasRTL ? 1 : 0));
            posSourceLength = sourceText.length();

            // Nor do we show translations
            /*if (trans.isTranslated()) {
                //translation exist
                translationText = trans.translation;
            } else {

            }*/

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
    void createInactiveSegmentElement(TMXEntry trans) throws BadLocationException {

        // We don't display modification info in a source only translationEditor

        translationText = null;
        sourceText = ste.getSrcText();

        int prevOffset = offset;
        sourceText = addInactiveSegPart(true, sourceText);
        posSourceBeg = doc.createPosition(prevOffset + (hasRTL ? 1 : 0));
        posSourceLength = sourceText.length();

    }

//    public void resetTextAttributes() {
//        doc.trustedChangesInProgress = true;
//        try {
//            if (posSourceBeg != null) {
//                // Set only the source text
//                int sBeg = posSourceBeg.getOffset();
//                int sLen = posSourceLength;
//                AttributeSet attrs = attrs(true, false, false, false);
//                //AttributeSet attrs = attrs(true, false, false, false);
//                doc.setCharacterAttributes(sBeg, sLen, attrs, true);
//            }
//        } finally {
//            doc.trustedChangesInProgress = false;
//        }
//    }

}
