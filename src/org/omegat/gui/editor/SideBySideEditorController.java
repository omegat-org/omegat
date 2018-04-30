/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2009 Didier Briel
               2011 Alex Buloichik, Martin Fleurke, Didier Briel
               2012 Guido Leenders, Didier Briel
               2013 Zoltan Bartko, Alex Buloichik, Aaron Madlon-Kay
               2014 Aaron Madlon-Kay, Piotr Kulik
               2015 Aaron Madlon-Kay, Yu Tang
               2016 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import com.vlsolutions.swing.docking.DockingConstants;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.*;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.gui.main.DockablePanel;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.*;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIThreadsUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class for controlling both a translationEditor and a source editor/viewer for side by side editing.
 *
 * Extends the EditorController to avoid substantial code duplication while allowing side by side editing
 *
 * Additional refactoring of the IEditor and ISegmentBuilder classes

 */
public class SideBySideEditorController extends EditorController {

    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(SideBySideEditorController.class.getName());

    //private static final double PAGE_LOAD_THRESHOLD = 0.25;

    //private boolean dockableSelected;


    /** Dockable translationPane for the source, not editable by the user*/
    private DockablePanel sourcePane;
    private JScrollPane sourceScrollPane;

    // The "editor" for displaying source, which one can not actually edit but is kept in sync with any translations
    protected EditorTextArea3 sourceEditor;

    /** Currently displayed segments info. */
    protected ISegmentBuilder[] m_docSourceSegList;


    public SideBySideEditorController(final MainWindow mainWindow) {
        super(mainWindow);

        // base class will call initEditor(), createUI,
//
//        setFont(mainWindow.getApplicationFont());
//        // "Editor" for source, which we won't actually allow the user to edit
//        //sourceEditor = new JEditorPane();
//        //sourceEditor = new EditorTextArea3(this); // todo do we navigate from the source, translation, or both panes?
//
//
        sourceEditor.setEditable(false);
        sourceEditor.setVisible(false);
        sourceScrollPane.setVisible(false);
//
//
//        //createUI();
//
        // TODO load source pane?
//
//        lazyLoadTimer.addActionListener(e -> {
//            loadEditor(sourceScrollPane, sourceEditor);
//        });


        CoreEvents.registerProjectChangeListener(eventType -> {
            SHOW_TYPE xshowType;
            switch (eventType) {
                case CREATE:
                case LOAD:
                    // do we need to set these visible?
                    sourceEditor.setVisible(true);
                    sourceScrollPane.setVisible(true);
                    //mw.addDockable(sourcePane); // todo confirm only added once...
                    String title = StringUtil.format(OStrings.getString("GUI_SUBWINDOWTITLE_Source"), getCurrentFile());
                    sourcePane.setName(StaticUIUtils.truncateToFit(title, sourcePane, 70));
                    mw.addDockable(translationPane, sourcePane, DockingConstants.SPLIT_LEFT, 50);
                    if (!Core.getProject().getAllEntries().isEmpty()) {
                        // do something with first project entry?
                    } else {
                        // do something with an empty project?
                    }
                    // todo setInitialOrientation();
                    break;
                case CLOSE:
                    // Clear source segments
                    m_docSourceSegList = null;
                    mw.removeDockable(sourcePane);
                    sourceScrollPane.setVisible(false);
                    sourceEditor.setVisible(false);
                    break;
                default:
                    // no change
            }
        });

    }

    void initEditor(){
        sourceEditor = new EditorTextArea3(this, l -> getSegmentIndexAtLocationImpl(m_docSourceSegList, l));
        super.initEditor();
    }


    void createUI() {

        SwingUtilities.invokeLater(() -> {
            settings.setDisplayEditorSegmentSources(false);
            settings.setDisplayBoldSources(false);
        });

        super.createUI();

        sourcePane = new DockablePanel("SOURCE", "SOURCE", false);
        sourceScrollPane = new JScrollPane(sourceEditor);
        createPaneImpl(sourcePane, sourceScrollPane);
        // We have created the scorePane, but we will need to add our sourcePane to the MainWindow when a project is loaded

    }


    protected void updateSegmentElement(int indexNumber, boolean active, ISegmentBuilder[] builders){
        updateSegmentElementImpl(builders[indexNumber], active, Core.getProject().getTranslationInfo(builders[indexNumber].getSourceTextEntry()));
        // And update the source list
        updateSegmentElementImpl(m_docSourceSegList[indexNumber], active, Core.getProject().getTranslationInfo(m_docSourceSegList[indexNumber].getSourceTextEntry()));
    }


//    protected void updateSegmentElement(ISegmentBuilder builder, boolean active, TMXEntry currentTranslation){
//
//        super.updateSegmentElement(builder, active, currentTranslation);
//
//        // Also update our source translationEditor
//        //ISegmentBuilder  sourceBuilder = m_docSourceSegList[displayedEntryIndex];
////        builder.createSegmentElement(active, currentTranslation);
////
////        builder.resetTextAttributes();
//    }


    protected void generateSegments(FileInfo file, Document3 doc, boolean hasRTL){

        super.generateSegments(file, doc, hasRTL);

        // Add the source only segment builders...
        Document3 sourceDoc = new Document3(this);
        // Set the source document
        sourceDoc.setDocumentFilter(new DocumentFilter3());
        ArrayList<ISegmentBuilder> tmpSourceList = new ArrayList<>(file.entries.size());
        for (SourceTextEntry ste : file.entries) {
            if (entriesFilter == null || entriesFilter.allowed(ste)) {
                SourceOnlySegmentBuilder sb = new SourceOnlySegmentBuilder(this, sourceDoc, settings, ste, ste.entryNum(), hasRTL);
                tmpSourceList.add(sb);
            }
        }
        m_docSourceSegList = tmpSourceList.toArray(new SourceOnlySegmentBuilder[tmpSourceList.size()]);
//        if(m_docSourceSegList.length > 0) {
//            // would be better if handled as part of a state change...
//            sourceEditor.setVisible(true);
//            sourceScrollPane.setVisible(true);
//
//        }


        // ...but only display the ones in [firstLoaded, lastLoaded]

        // NOTE: below duplicates functionality in the base EditorController->loadDocument(), where firstLoaded and lastLoaded are set
        for (int i = 0; i < m_docSourceSegList.length; i++) {
            if (i >= firstLoaded && i <= lastLoaded) {
                ISegmentBuilder sb = m_docSourceSegList[i];
                sb.createSegmentElement(false, Core.getProject().getTranslationInfo(sb.getSourceTextEntry()));
                sb.addSegmentSeparator();
            }
        }

        sourceDoc.setDocumentFilter(new DocumentFilter3());
        sourceEditor.setDocument(sourceDoc);


        // add locate for target language to translationEditor
        Locale targetLocale = Core.getProject().getProjectProperties().getTargetLanguage().getLocale();
        sourceEditor.setLocale(targetLocale);
    }

//    protected void generateSegments(FileInfo file, Document3 doc, boolean hasRTL){
//
//        super.generateSegments(file, doc, hasRTL);
//
//        // Create the source only segment builders...
//        Document3 sourceDoc = new Document3(this);
//        // Set the source document
//        sourceDoc.setDocumentFilter(new DocumentFilter3());
//        ArrayList<ISegmentBuilder> tmpSourceList = new ArrayList<>(file.entries.size());
//        for (SourceTextEntry ste : file.entries) {
//            if (entriesFilter == null || entriesFilter.allowed(ste)) {
//                SourceOnlySegmentBuilder sb = new SourceOnlySegmentBuilder(this, sourceDoc, settings, ste, ste.entryNum(), hasRTL);
//                tmpSourceList.add(sb);
//            }
//        }
//        m_docSourceSegList = tmpSourceList.toArray(new SourceOnlySegmentBuilder[tmpSourceList.size()]);
//
//        // Add the source document to our source "editor"
//        sourceEditor.setDocument(sourceDoc);
//
//    }


    void setFont(final Font font) {
        super.setFont(font);
        if(null != sourceEditor)
            sourceEditor.setFont(font);
    }


    /**
     * Define translationEditor's orientation by target language orientation.
     */
    void applyOrientationToEditor() {
        super.applyOrientationToEditor();
        // Set for the source translationEditor as well
        sourceEditor.setComponentOrientation(translationEditor.getComponentOrientation());
    }


    /**
     * Displays the {@link Preferences#EDITOR_INITIAL_SEGMENT_LOAD_COUNT}
     * segments surrounding the entry with index {@link #displayedEntryIndex}.
     */
/*
    public void loadDocument() {

        super.loadDocument();

*/
/*        for (int i = 0; i < m_docSegList.length; i++) {
            if (i >= firstLoaded && i <= lastLoaded) {
                // Update the source builders too
                ISegmentBuilder sb = m_docSourceSegList[i];
                LOGGER.info("CLG load document source: " + sb.getSource());
                sb.createSegmentElement(false, Core.getProject().getTranslationInfo(sb.getSource()));
                sb.addSegmentSeparator();

            }
        }*//*


        //sourceEditor.repaint();
    }
*/

    /*
     * Activates the current entry and puts the cursor at the start of segment
     */
    public void activateEntry() {
        activateEntryImpl(CaretPosition.startOfEntry(), CaretPosition.startOfEntry());
    }

    /**
     * Activates the current entry (if available) by displaying source text and embedding displayed text in
     * markers.
     * <p>
     * Also moves document focus to current entry, and makes sure fuzzy info displayed if available.
     */
    /*protected void activateEntry(CaretPosition pos) {
        UIThreadsUtil.mustBeSwingThread();

        SourceTextEntry ste = getCurrentEntry();
        if (ste == null) {
            return;
        }

        if (translationScrollPane.getViewport().getView() != translationEditor) {
            // translationEditor not displayed
            return;
        }

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        SegmentBuilder builder = m_docSegList[displayedEntryIndex];
        ISegmentBuilder  sourceBuilder = m_docSourceSegList[displayedEntryIndex];

        // If the builder has not been created then we are trying to jump to a
        // segment that is in the current document but not yet loaded. To avoid
        // loading large swaths of the document at once, we then re-load the
        // document centered at the destination segment.
        if (!builder.hasBeenCreated()) {
            loadDocument();
            activateEntry(pos);
            return;
        }

        previousTranslations = Core.getProject().getAllTranslations(ste);
        TMXEntry currentTranslation = previousTranslations.getCurrentTranslation();
        // forget about old marks
        builder.createSegmentElement(true, currentTranslation);
        sourceBuilder.createSegmentElement(true, currentTranslation);

        Core.getNotes().setNoteText(currentTranslation.note);

        // then add new marks
        markerController.reprocessImmediately(builder);

        translationEditor.undoManager.reset();

        history.insertNew(builder.segmentNumberInProject);

        setMenuEnabled();

        showStat();

        showLengthMessage();

        if (Preferences.isPreference(Preferences.EXPORT_CURRENT_SEGMENT)) {
            segmentExportImport.exportCurrentSegment(ste);
        }

        int te = translationEditor.getOmDocument().getTranslationEnd();
        int ts = translationEditor.getOmDocument().getTranslationStart();

        //
        // Navigate to entry as requested.
        //
        if (pos.position != null) { // check if outside of entry
            pos.position = Math.max(0, pos.position);
            pos.position = Math.min(pos.position, te - ts);
        }
        if (pos.selectionStart != null && pos.selectionEnd != null) { // check if outside of entry
            pos.selectionStart = Math.max(0, pos.selectionStart);
            pos.selectionEnd = Math.min(pos.selectionEnd, te - ts);
            if (pos.selectionStart >= pos.selectionEnd) { // if end after start
                pos.selectionStart = null;
                pos.selectionEnd = null;
            }
        }

        scrollForDisplayNearestSegments(pos);
        // check if file was changed
        if (previousDisplayedFileIndex != displayedFileIndex) {
            previousDisplayedFileIndex = displayedFileIndex;
            CoreEvents.fireEntryNewFile(Core.getProject().getProjectFiles().get(displayedFileIndex).filePath);
        }

        translationEditor.autoCompleter.setVisible(false);
        translationEditor.repaint();

        // Do the same for the source panel

        // fire event about new segment activated
        CoreEvents.fireEntryActivated(ste);
    }*/


    public void activateEntry(CaretPosition pos) {
        CaretPosition sourcePos;
        if(pos.position == 0){
             sourcePos = CaretPosition.startOfEntry();
        }
        else {
            sourcePos = new CaretPosition(getCurrentPositionInEntrySource());
        }
        activateEntryImpl(pos, sourcePos);

    }

    protected void activateEntryImpl( CaretPosition transPos, CaretPosition sourcePos) {

        //LOGGER.warning("CLG SbS activateEntryImpl translate pos: " + transPos.position + ", source pos: " + sourcePos.position + " source index: " + displayedEntryIndex);
        // will cal our updateSegmentElement() method when activating the
        super.activateEntry(transPos);


        //TMXEntry currentTranslation = previousTranslations.getCurrentTranslation();
        ISegmentBuilder sb = m_docSourceSegList[displayedEntryIndex];
        //sb.createSegmentElement(true, currentTranslation);
        sb.resetTextAttributes();
        // TODO CONFIRM THIS IS CORRECT FOR SIDE BY SIDE... SEEMS TO SET THE EDITOR CARRET POS, WHICH WE DON'T NEED TO DO
        //navigateToEntry((Document3)sourceEditor.getDocument(), sourcePos, sb, sourceScrollPane, sourceEditor);

        if(sb.hasBeenCreated()){

            SwingUtilities.invokeLater(() -> {
                Rectangle rect = getSegmentBounds(sb, sourceEditor);
                scrollForDisplayNearestSegmentsImpl(rect, sourceScrollPane, sourceEditor);

                // do we need to set caret position in translation pane: setCaretPosition(pos);
            });
        }

        sourceEditor.repaint();
    }


    void commitAndDeactivate(ForceTranslation forceTranslation, String newTrans) {

        super.commitAndDeactivate(forceTranslation, newTrans);

        // Also update the source panel
        m_docSourceSegList[displayedEntryIndex].createSegmentElement(false,
                Core.getProject().getTranslationInfo(m_docSourceSegList[displayedEntryIndex].getSourceTextEntry()));

    }





 /*   public void refreshView(boolean doCommit) {
        UIThreadsUtil.mustBeSwingThread();

        if (!doCommit) {
            deactivateWithoutCommit();
        }
        int currentEntry = getCurrentEntryNumber();
        int caretTransPosition = getCurrentPositionInEntryTranslation();
        int caretsourcePosition = getCurrentPositionInEntrySource();
        gotoFile(displayedFileIndex);
        gotoEntry(currentEntry, new CaretPosition(caretTransPosition), new CaretPosition(caretsourcePosition));
    }
*/

    public int getCurrentPositionInEntrySource() {
        return getPositionInEntryImpl((Document3)sourceEditor.getDocument(), sourceEditor.getCaretPosition());
    }


    /**
     * Returns the relative caret position in the editable translation for a
     * given absolute index into the overall translationEditor document.
     */
    public int getPositionInEntryTranslation(int pos) {
        return getPositionInEntryImpl(translationEditor.getOmDocument(), pos);
       /* UIThreadsUtil.mustBeSwingThread();

        if (!translationEditor.getOmDocument().isEditMode()) {
            return -1;
        }
        int beg = translationEditor.getOmDocument().getTranslationStart();
        int end = translationEditor.getOmDocument().getTranslationEnd();
        if (pos < beg) {
            pos = beg;
        }
        if (pos > end) {
            pos = end;
        }
        return pos - beg;*/
    }

    /**
     * Returns the relative caret position in the editable translation for a
     * given absolute index into the overall translationEditor document.
     */
     int getPositionInEntryImpl(Document3 doc, int pos) {
        UIThreadsUtil.mustBeSwingThread();

        if (!doc.isEditMode()) {
            return -1;
        }
        int beg = doc.getTranslationStart();
        int end = doc.getTranslationEnd();
        if (pos < beg) {
            pos = beg;
        }
        if (pos > end) {
            pos = end;
        }
        return pos - beg;
    }

    protected void updateState(SHOW_TYPE showType) {
         super.updateState(showType);

        JComponent data = null;

        switch (showType) {
            case INTRO:
                break;
            case EMPTY_PROJECT:
                break;
            case FIRST_ENTRY:
                data = sourceEditor;

                break;
            case NO_CHANGE:
               // title = StringUtil.format(OStrings.getString("GUI_SUBWINDOWTITLE_Editor"), getCurrentFile());
                data = sourceEditor;
                break;
        }

        // Set the boarder around the source editor
        if (null != data) {
            if (UIManager.getBoolean("OmegaTDockablePanel.isProportionalMargins")) {
                int size = data.getFont().getSize() / 2;
                data.setBorder(new EmptyBorder(size, size, size, size));
            }
            sourceScrollPane.setViewportView(data);
        }
    }




    @Override
    public JEditorPane getEditor(EditorType type){
        switch (type){
            case SOURCE:
                return sourceEditor;
            case TRANSLATION:
            default:
                return translationEditor;
        }
    }


}
