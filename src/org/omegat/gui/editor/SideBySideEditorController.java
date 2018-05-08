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
 * Class for controlling both a editor and a source editor/viewer for side by side editing.  Extends EditorController
 * to avoid substantial code duplication while allowing side by side editing.
 *
 */
public class SideBySideEditorController extends EditorController {

    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(SideBySideEditorController.class.getName());

    /** Dockable pane for the source, not editable by the user*/
    private DockablePanel sourcePane;
    private JScrollPane sourceScrollPane;

    // The "editor" for displaying source, which one can not actually edit but is kept in sync with any translations if that mode is enabled
    protected EditorTextArea3 sourceEditor;

    /** Currently displayed segments info. */
    protected ISegmentBuilder[] m_docSourceSegList;

    public SideBySideEditorController(final MainWindow mainWindow, EditorMode mode) {
        super(mainWindow, mode);

        // base class will call initEditor(), createUI,
//

        CoreEvents.registerProjectChangeListener(eventType -> {

            switch (eventType) {
                case CREATE:
                case LOAD:
                    // do we need to set these visible?
                    if(EditorMode.SIDE_BY_SIDE == mode) {
                        sourceEditor.setVisible(true);
                        sourceScrollPane.setVisible(true);
                        String title = StringUtil.format(OStrings.getString("GUI_SUBWINDOWTITLE_Source"), getCurrentFile());
                        sourcePane.setName(StaticUIUtils.truncateToFit(title, sourcePane, 70));
                        mw.addDockable(pane, sourcePane, DockingConstants.SPLIT_LEFT, 50);
                    }
                    break;
                case CLOSE:
                    if(null != sourceEditor) {
                        // Clear source segments
                        m_docSourceSegList = null;
                        mw.removeDockable(sourcePane);
                        sourceScrollPane.setVisible(false);
                        sourceEditor.setVisible(false);
                    }
                    break;
                default:
                    // no change
            }
        });

    }

    protected  void reloadEditor(){

        // clear the source segments (if any)
        if(null != sourceEditor ) {
            m_docSourceSegList = null;
            sourceEditor.setVisible(false);
            if(null != sourcePane) {
                mw.removeDockable(sourcePane);
                sourceScrollPane.setVisible(false);
            }

        }

        // re-load
        if(EditorMode.SIDE_BY_SIDE == editMode ) {
            createSideBySideUI();
            sourceEditor.setVisible(true);
            sourceScrollPane.setVisible(true);
            String title = StringUtil.format(OStrings.getString("GUI_SUBWINDOWTITLE_Source"), getCurrentFile());
            sourcePane.setName(StaticUIUtils.truncateToFit(title, sourcePane, 70));
            mw.addDockable(pane, sourcePane, DockingConstants.SPLIT_LEFT, 50);
        }

        updateDisplay();
        super.reloadEditor();
    }

    void initEditor(){

        //if(EditorMode.SIDE_BY_SIDE == editMode) {
            sourceEditor = new EditorTextArea3(this, l -> getSegmentIndexAtLocationImpl(m_docSourceSegList, l));
            sourceEditor.setEditable(false);
            sourceEditor.setVisible(false);
            //sourceScrollPane.setVisible(false);
        //}
        super.initEditor();
    }

    void updateDisplay(){
        if(EditorMode.SIDE_BY_SIDE == editMode) {
            SwingUtilities.invokeLater(() -> {
                settings.setDisplayEditorSegmentSources(false);
                settings.setDisplayBoldSources(false);
            });
        }
        else {
            SwingUtilities.invokeLater(() -> {
                settings.setDisplayEditorSegmentSources(true);
                settings.setDisplayBoldSources(true);
            });
        }
    }

    void createUI() {

        updateDisplay();
        super.createUI();

        if(EditorMode.SIDE_BY_SIDE == editMode) {
            createSideBySideUI();
            // We have created the scorePane, but we will need to add our sourcePane to the MainWindow when a project is loaded
        }

    }

    void createSideBySideUI(){
        sourcePane = new DockablePanel("SOURCE", "SOURCE", false);
        sourceScrollPane = new JScrollPane(sourceEditor);
        sourceScrollPane.setVisible(false);
        createPaneImpl(sourcePane, sourceScrollPane);
    }


    protected void updateSegmentElement(int indexNumber, boolean active, ISegmentBuilder[] builders){
        updateSegmentElementImpl(builders[indexNumber], active, Core.getProject().getTranslationInfo(builders[indexNumber].getSourceTextEntry()));
        if(EditorMode.SIDE_BY_SIDE == editMode) {
            // And update the source list
            updateSegmentElementImpl(m_docSourceSegList[indexNumber], active, Core.getProject().getTranslationInfo(m_docSourceSegList[indexNumber].getSourceTextEntry()));
        }
    }



    protected void generateSegments(FileInfo file, Document3 doc, boolean hasRTL){

        super.generateSegments(file, doc, hasRTL);

        if(EditorMode.SIDE_BY_SIDE == editMode) {
            // Add the source only segment builders...
            Document3 sourceDoc = new Document3(this);
            // Set the source document
            sourceDoc.setDocumentFilter(new DocumentFilter3());
            // Note set the source document now since the segments can be updated before we finish here
            sourceEditor.setDocument(sourceDoc);
            ArrayList<ISegmentBuilder> tmpSourceList = new ArrayList<>(file.entries.size());
            int builderIndex = 0;
            for (SourceTextEntry ste : file.entries) {
                if (entriesFilter == null || entriesFilter.allowed(ste)) {
                    SourceOnlySegmentBuilder sb = new SourceOnlySegmentBuilder(this, sourceDoc, settings, ste, ste.entryNum(), hasRTL, m_docSegList[builderIndex++]);
                    tmpSourceList.add(sb);
                }
            }
            m_docSourceSegList = tmpSourceList.toArray(new SourceOnlySegmentBuilder[tmpSourceList.size()]);

            // NOTE: below duplicates functionality in the base EditorController->loadDocument(), where firstLoaded and lastLoaded are set
            for (int i = 0; i < m_docSourceSegList.length; i++) {
                if (i >= firstLoaded && i <= lastLoaded) {
                    ISegmentBuilder sb = m_docSourceSegList[i];
                    sb.createSegmentElement(false, Core.getProject().getTranslationInfo(sb.getSourceTextEntry()));
                    sb.addSegmentSeparator();
                }
            }

            // add locate for target language to editor
            Locale targetLocale = Core.getProject().getProjectProperties().getTargetLanguage().getLocale();
            sourceEditor.setLocale(targetLocale);
        }
    }

    void setFont(final Font font) {
        super.setFont(font);
        if(null != sourceEditor)
            sourceEditor.setFont(font);
    }


    /**
     * Define editor's orientation by target language orientation.
     */
    void applyOrientationToEditor() {
        super.applyOrientationToEditor();
        // Set for the source editor as well
        if(null != sourceEditor)
            sourceEditor.setComponentOrientation(editor.getComponentOrientation());
    }


    /*
     * Activates the current entry and puts the cursor at the start of segment
     */
    public void activateEntry() {
        activateEntryImpl(CaretPosition.startOfEntry(), CaretPosition.startOfEntry());
    }

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

        // will call our updateSegmentElement() method when activating the
        super.activateEntry(transPos);

        if(EditorMode.SIDE_BY_SIDE == editMode) {
            ISegmentBuilder sb = m_docSourceSegList[displayedEntryIndex];
            //sb.createSegmentElement(true, currentTranslation);
            sb.resetTextAttributes();

            if (sb.hasBeenCreated()) {

                SwingUtilities.invokeLater(() -> {
                    Rectangle rect = getSegmentBounds(sb, sourceEditor);
                    scrollForDisplayNearestSegmentsImpl(rect, sourceScrollPane, sourceEditor);

                    // do we need to set caret position in translation pane: setCaretPosition(pos);
                });
            }

            sourceEditor.repaint();
        }
    }


    void commitAndDeactivate(ForceTranslation forceTranslation, String newTrans) {
        // shouldn't need to do anything here... Our updateSegmentElement will get called when
        super.commitAndDeactivate(forceTranslation, newTrans);
    }


    public int getCurrentPositionInEntrySource() {
        if(null == sourceEditor){
            return 0;
        }
        return getPositionInEntryImpl((Document3)sourceEditor.getDocument(), sourceEditor.getCaretPosition());
    }


    /**
     * Returns the relative caret position in the editable translation for a
     * given absolute index into the overall editor document.
     */
    public int getPositionInEntryTranslation(int pos) {
        return getPositionInEntryImpl(editor.getOmDocument(), pos);
    }

    /**
     * Returns the relative caret position in the editable translation for a
     * given absolute index into the overall editor document.
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
        if (null != data && EditorMode.SIDE_BY_SIDE == editMode) {
            if (UIManager.getBoolean("OmegaTDockablePanel.isProportionalMargins")) {
                int size = data.getFont().getSize() / 2;
                data.setBorder(new EmptyBorder(size, size, size, size));
            }
            sourceScrollPane.setViewportView(data);
        }
    }




    @Override
    public JEditorPane getEditor(EditorType type){ // RENAME?  WE REALLY ONLY HAVE ONE EDITOR
        switch (type){
            case SOURCE:
                if(EditorMode.SIDE_BY_SIDE == editMode)
                    return sourceEditor;
                return null;
            case TRANSLATION:
            default:
                return editor;
        }
    }

    public ISegmentBuilder getSegmentAtIndex(EditorType type, int index) {
        if(type == EditorType.SOURCE){
            return getSegmentAtIndexImpl(m_docSourceSegList, index);
        }
        return getSegmentAtIndexImpl(m_docSegList, index);
    }


}
