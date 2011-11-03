/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2009 Didier Briel
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

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.help.HelpFrame;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.UIThreadsUtil;

import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.event.DockableSelectionEvent;
import com.vlsolutions.swing.docking.event.DockableSelectionListener;

/**
 * Class for control all editor operations.
 * 
 * You can find good description of java text editor working at
 * http://java.sun.com/products/jfc/tsc/articles/text/overview/
 * 
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public class EditorController implements IEditor {

    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(EditorController.class.getName());

    /** Dockable pane for editor. */
    private final DockableScrollPane pane;

    private boolean dockableSelected;

    /** Editor instance. */
    protected final EditorTextArea3 editor;

    /** Class for process marks for editor. */
    protected MarkerController markerController;

    private String introPaneTitle, emptyProjectPaneTitle;
    private JTextPane introPane, emptyProjectPane;
    protected final MainWindow mw;

    /** Currently displayed segments info. */
    protected SegmentBuilder[] m_docSegList;

    /** Current displayed file. */
    protected int displayedFileIndex, previousDisplayedFileIndex;
    /**
     * Current active segment in current file, if there are segments in file (can be fale if filter active!)
     */
    protected int displayedEntryIndex;

    /** Object which store history of moving by segments. */
    private SegmentHistory history = new SegmentHistory();

    protected final EditorSettings settings;

    protected Font font, fontb, fonti, fontbi;

    private enum SHOW_TYPE {
        INTRO, EMPTY_PROJECT, FIRST_ENTRY, NO_CHANGE
    };

    Document3.ORIENTATION currentOrientation;
    protected boolean sourceLangIsRTL, targetLangIsRTL;

    private List<Integer> entryFilterList;

    public EditorController(final MainWindow mainWindow) {
        this.mw = mainWindow;

        editor = new EditorTextArea3(this);
        setFont(Core.getMainWindow().getApplicationFont());

        markerController = new MarkerController(this);

        pane = new DockableScrollPane("EDITOR", " ", editor, false);
        pane.setComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setMinimumSize(new Dimension(100, 100));

        Core.getMainWindow().addDockable(pane);

        Container c = pane;
        while (c != null && !(c instanceof DockingDesktop)) {
            c = c.getParent(); // find dockable desktop
        }
        DockingDesktop desktop = (DockingDesktop) c;
        desktop.addDockableSelectionListener(new DockableSelectionListener() {
            public void selectionChanged(DockableSelectionEvent dockableselectionevent) {
                dockableSelected = pane == dockableselectionevent.getSelectedDockable();
            }
        });

        settings = new EditorSettings(this);

        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                SHOW_TYPE showType;
                switch (eventType) {
                case CREATE:
                case LOAD:
                    history.clear();
                    removeFilter();
                    if (!Core.getProject().getAllEntries().isEmpty()) {
                        showType = SHOW_TYPE.FIRST_ENTRY;
                    } else {
                        showType = SHOW_TYPE.EMPTY_PROJECT;
                    }
                    markerController.reset(0);
                    setInitialOrientation();
                    break;
                case CLOSE:
                    history.clear();
                    removeFilter();
                    markerController.reset(0);
                    showType = SHOW_TYPE.INTRO;
                    deactivateWithoutCommit();
                    break;
                default:
                    showType = SHOW_TYPE.NO_CHANGE;
                }
                if (showType != SHOW_TYPE.NO_CHANGE) {
                    updateState(showType);
                }
            }
        });

        // register entry changes callback
        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            public void onNewFile(String activeFileName) {
                updateState(SHOW_TYPE.NO_CHANGE);
            }

            public void onEntryActivated(SourceTextEntry newEntry) {
            }
        });

        createAdditionalPanes();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateState(SHOW_TYPE.INTRO);
                pane.requestFocus();
            }
        });

        // register font changes callback
        CoreEvents.registerFontChangedEventListener(new IFontChangedEventListener() {
            public void onFontChanged(Font newFont) {
                setFont(newFont);
                ViewLabel.fontHeight = 0;
                editor.revalidate();
                editor.repaint();

                // fonts have changed
                emptyProjectPane.setFont(font);
            }
        });

        // register Swing error logger
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.log(Level.SEVERE, "Uncatched exception in thread [" + t.getName() + "]", e);
            }
        });

        EditorPopups.init(this);
    }

    private void updateState(SHOW_TYPE showType) {
        UIThreadsUtil.mustBeSwingThread();

        Component data = null;
        String title = null;

        switch (showType) {
        case INTRO:
            data = introPane;
            title = introPaneTitle;
            break;
        case EMPTY_PROJECT:
            data = emptyProjectPane;
            title = emptyProjectPaneTitle;
            break;
        case FIRST_ENTRY:
            displayedFileIndex = 0;
            displayedEntryIndex = 0;
            title = StaticUtils.format(OStrings.getString("GUI_SUBWINDOWTITLE_Editor"), getCurrentFile());
            data = editor;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // need to run later because some other event listeners
                    // should be called before
                    loadDocument();
                    activateEntry();
                }
            });
            break;
        case NO_CHANGE:
            title = StaticUtils.format(OStrings.getString("GUI_SUBWINDOWTITLE_Editor"), getCurrentFile());
            data = editor;
            break;
        }

        pane.setName(title);
        if (pane.getViewport().getView() != data) {
            pane.setViewportView(data);
        }
    }

    private void setFont(final Font font) {
        this.font = font;
        this.fontb = new Font(font.getFontName(), Font.BOLD, font.getSize());
        this.fonti = new Font(font.getFontName(), Font.ITALIC, font.getSize());
        this.fontbi = new Font(font.getFontName(), Font.BOLD | Font.ITALIC, font.getSize());

        editor.setFont(font);
    }

    /**
     * Decide what document orientation should be default for source/target languages.
     */
    private void setInitialOrientation() {
        String sourceLang = Core.getProject().getProjectProperties().getSourceLanguage().getLanguageCode();
        String targetLang = Core.getProject().getProjectProperties().getTargetLanguage().getLanguageCode();

        sourceLangIsRTL = EditorUtils.isRTL(sourceLang);
        targetLangIsRTL = EditorUtils.isRTL(targetLang);

        if (sourceLangIsRTL != targetLangIsRTL) {
            currentOrientation = Document3.ORIENTATION.DIFFER;
        } else {
            if (sourceLangIsRTL) {
                currentOrientation = Document3.ORIENTATION.RTL;
            } else {
                currentOrientation = Document3.ORIENTATION.LTR;
            }
        }
        applyOrientationToEditor();
    }

    /**
     * Define editor's orientation by target language orientation.
     */
    private void applyOrientationToEditor() {
        ComponentOrientation targetOrientation = null;
        switch (currentOrientation) {
        case LTR:
            targetOrientation = ComponentOrientation.LEFT_TO_RIGHT;
            break;
        case RTL:
            targetOrientation = ComponentOrientation.RIGHT_TO_LEFT;
            break;
        case DIFFER:
            if (targetLangIsRTL) {
                targetOrientation = ComponentOrientation.RIGHT_TO_LEFT;
            } else {
                targetOrientation = ComponentOrientation.LEFT_TO_RIGHT;
            }
        }
        // set editor's orientation by target language
        editor.setComponentOrientation(targetOrientation);
    }

    /**
     * Toggle component orientation: LTR, RTL, language dependent.
     */
    protected void toggleOrientation() {
        commitAndDeactivate();
        
        Document3.ORIENTATION newOrientation = currentOrientation;
        switch (currentOrientation) {
        case LTR:
            newOrientation = Document3.ORIENTATION.RTL;
            break;
        case RTL:
            if (sourceLangIsRTL != targetLangIsRTL) {
                newOrientation = Document3.ORIENTATION.DIFFER;
            } else {
                newOrientation = Document3.ORIENTATION.LTR;
            }
            break;
        case DIFFER:
            newOrientation = Document3.ORIENTATION.LTR;
            break;
        }
        LOGGER.info("Switch document orientation from " + currentOrientation + " to " + newOrientation);
        currentOrientation = newOrientation;
        
        applyOrientationToEditor();

        int activeSegment = displayedEntryIndex;
        loadDocument();
        displayedEntryIndex = activeSegment;
        activateEntry();
    }

    /**
     * {@inheritDoc}
     */
    public void requestFocus() {
        pane.getViewport().getView().requestFocusInWindow();
    }

    /**
     * {@inheritDoc}
     */
    public SourceTextEntry getCurrentEntry() {
        if (m_docSegList == null || displayedEntryIndex < 0 || m_docSegList.length <= displayedEntryIndex) {
            // there is no current entry
            return null;
        }
        if (m_docSegList[displayedEntryIndex] == null) {
            return null;
        }
        return m_docSegList[displayedEntryIndex].ste;
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentFile() {
        if (Core.getProject().getProjectFiles().isEmpty()) {
            // there is no files yet
            return null;
        }

        if (displayedFileIndex < Core.getProject().getProjectFiles().size()) {
            return Core.getProject().getProjectFiles().get(displayedFileIndex).filePath;
        } else {
            return null;
        }
    }

    /**
     * Displays all segments in current document.
     * <p>
     * Displays translation for each segment if it's available, otherwise displays source text. Also stores
     * length of each displayed segment plus its starting offset.
     */
    protected void loadDocument() {
        UIThreadsUtil.mustBeSwingThread();

        // Currently displayed file
        IProject.FileInfo file;
        try {
            file = Core.getProject().getProjectFiles().get(displayedFileIndex);
        } catch (IndexOutOfBoundsException ex) {
            // there is no displayedFileIndex file in project - load first file
            file = Core.getProject().getProjectFiles().get(0);
        }

        Document3 doc = new Document3(this);

        ArrayList<SegmentBuilder> temp_docSegList2 = new ArrayList<SegmentBuilder>(file.entries.size());
        for (int i = 0; i < file.entries.size(); i++) {
            SourceTextEntry ste = file.entries.get(i);
            if (isInFilter(new Integer(ste.entryNum()))) {
                SegmentBuilder sb = new SegmentBuilder(this, doc, settings, ste, ste.entryNum());
                temp_docSegList2.add(sb);

                sb.createSegmentElement(false);

                SegmentBuilder.addSegmentSeparator(doc);
            }
        }
        m_docSegList = temp_docSegList2.toArray(new SegmentBuilder[temp_docSegList2.size()]);
        doc.setDocumentFilter(new DocumentFilter3());

        // add locate for target language to editor
        Locale targetLocale = Core.getProject().getProjectProperties().getTargetLanguage().getLocale();
        editor.setLocale(targetLocale);

        editor.setDocument(doc);

        doc.addUndoableEditListener(editor.undoManager);

        doc.addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                showLengthMessage();
                onTextChanged();
            }

            public void insertUpdate(DocumentEvent e) {
                showLengthMessage();
                onTextChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                showLengthMessage();
                onTextChanged();
            }
        });

        markerController.reset(m_docSegList.length);

        // call all markers
        markerController.process(m_docSegList);

        editor.repaint();
    }

    /**
     * Activates the current entry (if available) by displaying source text and embedding displayed text in
     * markers.
     * <p>
     * Also moves document focus to current entry, and makes sure fuzzy info displayed if available.
     */
    public void activateEntry() {
        UIThreadsUtil.mustBeSwingThread();

        SourceTextEntry ste = getCurrentEntry();
        if (ste == null) {
            return;
        }

        if (pane.getViewport().getView() != editor) {
            // editor not displayed
            return;
        }

        if (!Core.getProject().isProjectLoaded())
            return;

        // forget about old marks
        markerController.resetEntryMarks(displayedEntryIndex);

        m_docSegList[displayedEntryIndex].createSegmentElement(true);
        
        Core.getNotes().setNoteText(Core.getProject().getTranslationInfo(ste).note);

        // then add new marks
        markerController.process(displayedEntryIndex, m_docSegList[displayedEntryIndex]);

        editor.cancelUndo();

        history.insertNew(m_docSegList[displayedEntryIndex].segmentNumberInProject);
        
        setMenuEnabled();

        showStat();

        showLengthMessage();

        if (Preferences.isPreference(Preferences.EXPORT_CURRENT_SEGMENT)) {
            exportCurrentSegment(ste);
        }

        scrollForDisplayNearestSegments(editor.getOmDocument().getTranslationStart());

        // check if file was changed
        if (previousDisplayedFileIndex != displayedFileIndex) {
            previousDisplayedFileIndex = displayedFileIndex;
            CoreEvents.fireEntryNewFile(Core.getProject().getProjectFiles().get(displayedFileIndex).filePath);
        }

        editor.repaint();

        // fire event about new segment activated
        CoreEvents.fireEntryActivated(ste);
    }
    
    private void setMenuEnabled() {
        // update history menu items
        mw.menu.gotoHistoryBackMenuItem.setEnabled(history.hasPrev());
        mw.menu.gotoHistoryForwardMenuItem.setEnabled(history.hasNext());
        mw.menu.editMultipleDefault.setEnabled(!m_docSegList[displayedEntryIndex].isDefaultTranslation());
        mw.menu.editMultipleAlternate.setEnabled(m_docSegList[displayedEntryIndex].isDefaultTranslation());
    }

    /**
     * Display length of source and translation parts in the status bar.
     */
    void showLengthMessage() {
        Document3 doc = editor.getOmDocument();
        String trans = doc.extractTranslation();
        if (trans != null) {
            SourceTextEntry ste = m_docSegList[displayedEntryIndex].ste;
            String lMsg = " " + ste.getSrcText().length() + "/" + trans.length() + " ";
            Core.getMainWindow().showLengthMessage(lMsg);
        }
    }

    /**
     * Called on the text changed in document. Required for recalculate marks for active segment.
     */
    void onTextChanged() {
        Document3 doc = editor.getOmDocument();
        if (doc.isEditMode()) {
            m_docSegList[displayedEntryIndex].onActiveEntryChanged();
            markerController.process(displayedEntryIndex, m_docSegList[displayedEntryIndex]);
        }
    }

    /**
     * Display some segments before and after when user on the top or bottom of page.
     */
    private void scrollForDisplayNearestSegments(final int requiredPosition) {
        int lookNext, lookPrev;
        try {
            SegmentBuilder prev = m_docSegList[displayedEntryIndex - 3];
            lookPrev = prev.getStartPosition();
        } catch (IndexOutOfBoundsException ex) {
            lookPrev = 0;
        }
        try {
            SegmentBuilder next = m_docSegList[displayedEntryIndex + 4];
            lookNext = next.getStartPosition() - 1;
        } catch (IndexOutOfBoundsException ex) {
            lookNext = editor.getOmDocument().getLength();
        }

        final int p = lookPrev;
        final int n = lookNext;
        //scroll a little up and down and then back, in separate thread, else 
        // gui is not updated!
        // It can happen that this method is called multiple time in short 
        // period E.g. on project reload, the first entry is activated, and 
        // then the last active segment. In between the file can be changed. 
        // When the first thread starts scrolling (for go to first 
        // segment), the scrolling could be done for the wrong document, 
        // possibly causing IllegalArgumentExceptions. They can be ignored.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    editor.setCaretPosition(n);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                editor.setCaretPosition(p);
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        try {
                                            editor.setCaretPosition(requiredPosition);
                                        } catch (IllegalArgumentException iae) {
                                            //ignore; document has changed in the mean time.
                                        }
                                    }
                                });
                            } catch (IllegalArgumentException iae) {
                            } // eating silently
                        }
                    });
                } catch (IllegalArgumentException iae) {
                } // eating silently
            }
        });
    }

    /**
     * Export the current source and target segments in text files.
     */
    private void exportCurrentSegment(final SourceTextEntry ste) {
        String s1 = ste.getSrcText();
        TMXEntry te = Core.getProject().getTranslationInfo(ste);
        String s2 = te.isTranslated() ? te.translation : "";

        FileUtil.writeScriptFile(s1, OConsts.SOURCE_EXPORT);
        FileUtil.writeScriptFile(s2, OConsts.TARGET_EXPORT);
    }

    /**
     * Calculate statistic for file, request statistic for project and display in status bar.
     */
    private void showStat() {
        IProject project = Core.getProject();
        IProject.FileInfo fi = project.getProjectFiles().get(displayedFileIndex);
        int translatedInFile = 0;
        for (SourceTextEntry ste : fi.entries) {
            if (project.getTranslationInfo(ste).isTranslated()) {
                translatedInFile++;
            }
        }

        StatisticsInfo stat = project.getStatistics();

        String pMsg = " " + Integer.toString(translatedInFile) + "/" + Integer.toString(fi.entries.size())
                + " (" + Integer.toString(stat.numberofTranslatedSegments) + "/"
                + Integer.toString(stat.numberOfUniqueSegments) + ", "
                + Integer.toString(stat.numberOfSegmentsTotal) + ") ";
        Core.getMainWindow().showProgressMessage(pMsg);
    }

    protected void goToSegmentAtLocation(int location) {
        // clicked segment

        int segmentAtLocation = getSegmentIndexAtLocation(location);
        if (displayedEntryIndex != segmentAtLocation) {
            commitAndDeactivate();
            displayedEntryIndex = segmentAtLocation;
            activateEntry();
        }
    }

    protected int getSegmentIndexAtLocation(int location) {
        int segmentAtLocation = m_docSegList.length - 1;
        for (int i = 0; i < m_docSegList.length; i++) {
            if (location < m_docSegList[i].getStartPosition()) {
                segmentAtLocation = i - 1;
                break;
            }
        }
        return segmentAtLocation;
    }

    /**
     * Commits the translation. Reads current entry text and commit it to memory if it's changed. Also clears
     * out segment markers while we're at it.
     * <p>
     * Since 1.6: Translation equal to source may be validated as OK translation if appropriate option is set
     * in Workflow options dialog.
     * <p>
     * All displayed segments with the same source text updated also.
     * 
     * @param forceCommit
     *            If false, the translation will not be saved
     */
    public void commitAndDeactivate() {
        UIThreadsUtil.mustBeSwingThread();

        Document3 doc = editor.getOmDocument();

        if (doc == null) {
            // there is no active doc, it's empty project
            return;
        }

        if (!doc.isEditMode()) {
            return;
        }

        // forget about old marks
        markerController.resetEntryMarks(displayedEntryIndex);

        String newTrans = doc.extractTranslation();
        doc.stopEditMode();

        if (newTrans != null) {
            // segment was active
            SegmentBuilder sb = m_docSegList[displayedEntryIndex];
            SourceTextEntry entry = sb.ste;

            TMXEntry oldTE = Core.getProject().getTranslationInfo(entry);
            String old_translation = oldTE.isTranslated() ? oldTE.translation : "";
            
            String note = Core.getNotes().getNoteText();

            // update memory
            if (newTrans.equals(entry.getSrcText())
                    && !Preferences.isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC)) {
                Core.getProject().setTranslation(entry, "", note, sb.isDefaultTranslation());
                newTrans = "";
            } else {
                Core.getProject().setTranslation(entry, newTrans, note, sb.isDefaultTranslation());
            }

            m_docSegList[displayedEntryIndex].createSegmentElement(false);

            if (!newTrans.equals(old_translation)) {
                // find all identical strings and redraw them

                for (int i = 0; i < m_docSegList.length; i++) {
                    if (i == displayedEntryIndex) {
                        // commited entry, skip
                        continue;
                    }
                    if (m_docSegList[i].ste.getSrcText().equals(entry.getSrcText())) {
                        // the same source text - need to update
                        m_docSegList[i].createSegmentElement(false);
                    }
                }
            }
        }
        Core.getNotes().clear();

        // then add new marks
        markerController.process(displayedEntryIndex, m_docSegList[displayedEntryIndex]);

        editor.cancelUndo();
    }

    /**
     * Deactivate active translation without save. Required on project close postprocessing, for example.
     */
    protected void deactivateWithoutCommit() {
        UIThreadsUtil.mustBeSwingThread();

        Document3 doc = editor.getOmDocument();

        if (doc == null) {
            // there is no active doc, it's empty project
            return;
        }

        doc.stopEditMode();
    }

    /**
     * {@inheritDoc}
     */
    public void commitAndLeave() {
        commitAndDeactivate();
        activateEntry();
    }

    public void nextEntry() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded())
            return;

        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = this.editor.getCursor();
        this.editor.setCursor(hourglassCursor);

        commitAndDeactivate();

        List<FileInfo> files = Core.getProject().getProjectFiles();
        SourceTextEntry ste;
        int startFileIndex = displayedFileIndex;
        int startEntryIndex = displayedEntryIndex;
        boolean looped = false;
        do {
            displayedEntryIndex++;
            if (displayedEntryIndex >= m_docSegList.length) {
                displayedFileIndex++;
                displayedEntryIndex = 0;
                if (displayedFileIndex >= files.size()) {
                    displayedFileIndex = 0;
                    looped = true;
                }
                loadDocument();
            }
            ste = getCurrentEntry();
        } while (ste == null // filtered file has no entries
                && (!looped || !(displayedFileIndex == startFileIndex && displayedEntryIndex >= startEntryIndex) // and
                                                                                                                 // we
                                                                                                                 // have
                                                                                                                 // not
                                                                                                                 // had
                                                                                                                 // all
                                                                                                                 // entries
                ));

        activateEntry();
        this.editor.setCursor(oldCursor);
    }

    public void prevEntry() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded())
            return;

        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = this.editor.getCursor();
        this.editor.setCursor(hourglassCursor);

        commitAndDeactivate();

        List<FileInfo> files = Core.getProject().getProjectFiles();
        SourceTextEntry ste;
        int startFileIndex = displayedFileIndex;
        int startEntryIndex = displayedEntryIndex;
        boolean looped = false;
        do {
            displayedEntryIndex--;
            if (displayedEntryIndex < 0) {
                displayedFileIndex--;
                if (displayedFileIndex < 0) {
                    displayedFileIndex = files.size() - 1;
                    looped = true;
                }
                loadDocument();
                displayedEntryIndex = m_docSegList.length - 1;
            }
            ste = getCurrentEntry();
        } while (ste == null // filtered file has no entries
                && (!looped || !(displayedFileIndex == startFileIndex && displayedEntryIndex <= startEntryIndex) // and
                                                                                                                 // we
                                                                                                                 // have
                                                                                                                 // not
                                                                                                                 // had
                                                                                                                 // all
                                                                                                                 // entries
                ));

        activateEntry();

        this.editor.setCursor(oldCursor);
    }

    /**
     * Finds the next untranslated entry in the document.
     */
    public void nextUntranslatedEntry() {
        UIThreadsUtil.mustBeSwingThread();

        // check if a document is loaded
        if (Core.getProject().isProjectLoaded() == false)
            return;

        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = this.editor.getCursor();
        this.editor.setCursor(hourglassCursor);

        // save the current entry
        commitAndDeactivate();

        List<FileInfo> files = Core.getProject().getProjectFiles();
        SourceTextEntry ste;
        int startFileIndex = displayedFileIndex;
        int startEntryIndex = displayedEntryIndex;
        do {
            displayedEntryIndex++;
            if (displayedEntryIndex >= m_docSegList.length) {
                // file finished - need new
                displayedFileIndex++;
                displayedEntryIndex = 0;
                if (displayedFileIndex >= files.size()) {
                    displayedFileIndex = 0;
                }
                loadDocument(); // to get proper EntryIndex when filter active
            }
            ste = getCurrentEntry();

            if (ste == null) {
                break;// filtered file has no entries
            }
            if (displayedFileIndex == startFileIndex && displayedEntryIndex == startEntryIndex) {
                break; // not found
            }
            if (!Core.getProject().getTranslationInfo(ste).isTranslated()) {
                break;// non-translated
            }
            if (Preferences.isPreference(Preferences.STOP_ON_ALTERNATIVE_TRANSLATION)) {
                // when there is at least one alternative translation, then we can consider that segment is
                // not translated
                HasMultipleTranslations checker = new HasMultipleTranslations(ste.getSrcText());
                Core.getProject().iterateByMultipleTranslations(checker);
                if (checker.found) {
                    // stop - alternative translation exist
                    break;
                }
            }
        } while (true);

        activateEntry();

        this.editor.setCursor(oldCursor);
    }

    /**
     * {@inheritDoc}
     */
    public int getCurrentEntryNumber() {
        SourceTextEntry e = getCurrentEntry();
        return e != null ? e.entryNum() : 0;
    }

    /**
     * {@inheritDoc}
     */
    public void gotoFile(int fileIndex) {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded())
            return;

        if (m_docSegList == null) {
            // document didn't loaded yet
            return;
        }

        commitAndDeactivate();

        displayedFileIndex = fileIndex;
        displayedEntryIndex = 0;
        loadDocument();

        activateEntry();
    }

    /**
     * {@inheritDoc}
     */
    public void gotoEntry(final int entryNum) {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded())
            return;

        if (m_docSegList == null) {
            // document didn't loaded yet
            return;
        }
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = this.editor.getCursor();
        this.editor.setCursor(hourglassCursor);
        commitAndDeactivate();

        if (entryNum == 0) {
            // it was empty project, need to display first entry
            displayedFileIndex = 0;
            displayedEntryIndex = 0;
            loadDocument();
        } else {
            IProject dataEngine = Core.getProject();
            for (int i = 0; i < dataEngine.getProjectFiles().size(); i++) {
                IProject.FileInfo fi = dataEngine.getProjectFiles().get(i);
                SourceTextEntry firstEntry = fi.entries.get(0);
                SourceTextEntry lastEntry = fi.entries.get(fi.entries.size() - 1);
                if (firstEntry.entryNum() <= entryNum && lastEntry.entryNum() >= entryNum) {
                    // this file
                    if (i != displayedFileIndex) {
                        // it's other file than displayed
                        displayedFileIndex = i;
                        loadDocument();
                    }
                    // find correct displayedEntryIndex
                    for (int j = 0; j < m_docSegList.length; j++) {
                        if (m_docSegList[j].segmentNumberInProject >= entryNum) { //
                            displayedEntryIndex = j;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        activateEntry();
        this.editor.setCursor(oldCursor);
    }

    /**
     * Change case of the selected text or if none is selected, of the current word.
     * 
     * @param toWhat
     *            : lower, title, upper or cycle
     */
    public void changeCase(CHANGE_CASE_TO toWhat) {
        UIThreadsUtil.mustBeSwingThread();

        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();

        int caretPosition = editor.getCaretPosition();

        int translationStart = editor.getOmDocument().getTranslationStart();
        int translationEnd = editor.getOmDocument().getTranslationEnd();

        // both should be within the limits
        if (end < translationStart || start > translationEnd)
            return; // forget it, not worth the effort

        // adjust the bound which exceeds the limits
        if (start < translationStart && end <= translationEnd)
            start = translationStart;

        if (end > translationEnd && start >= translationStart)
            end = translationEnd;

        try {
            // no selection? make it the current word
            if (start == end) {
                start = EditorUtils.getWordStart(editor, start);
                end = EditorUtils.getWordEnd(editor, end);

                // adjust the bound again
                if (start < translationStart && end <= translationEnd)
                    start = translationStart;

                if (end > translationEnd && start >= translationStart)
                    end = translationEnd;
            }

            editor.setSelectionStart(start);
            editor.setSelectionEnd(end);

            String selectionText = editor.getText(start, end - start);
            // tokenize the selection
            Token[] tokenList = Core.getProject().getTargetTokenizer()
                    .tokenizeWordsForSpelling(selectionText);

            StringBuffer buffer = new StringBuffer(selectionText);

            if (toWhat == CHANGE_CASE_TO.CYCLE) {
                int lower = 0;
                int upper = 0;
                int title = 0;
                int other = 0;

                for (Token token : tokenList) {
                    String word = token.getTextFromString(selectionText);
                    if (StringUtil.isLowerCase(word)) {
                        lower++;
                        continue;
                    }
                    if (StringUtil.isTitleCase(word)) {
                        title++;
                        continue;
                    }
                    if (StringUtil.isUpperCase(word)) {
                        upper++;
                        continue;
                    }
                    other++;
                }

                if (lower == 0 && title == 0 && upper == 0 && other == 0)
                    return; // nothing to do here

                if (lower != 0 && title == 0 && upper == 0)
                    toWhat = CHANGE_CASE_TO.TITLE;

                if (lower == 0 && title != 0 && upper == 0)
                    toWhat = CHANGE_CASE_TO.UPPER;

                if (lower == 0 && title == 0 && upper != 0)
                    toWhat = CHANGE_CASE_TO.LOWER;

                if (other != 0)
                    toWhat = CHANGE_CASE_TO.UPPER;
            }

            int lengthIncrement = 0;

            for (Token token : tokenList) {
                // find out the case and change to the selected
                String result = doChangeCase(token.getTextFromString(selectionText), toWhat);

                // replace this token
                buffer.replace(token.getOffset() + lengthIncrement, token.getLength() + token.getOffset()
                        + lengthIncrement, result);

                lengthIncrement += result.length() - token.getLength();
            }

            // ok, write it back to the editor document
            editor.replaceSelection(buffer.toString());

            editor.setCaretPosition(caretPosition);

            editor.setSelectionStart(start);
            editor.setSelectionEnd(end);
        } catch (BadLocationException ble) {
            // highly improbable
            Log.log("bad location exception when changing case");
            Log.log(ble);
        }
    }

    /**
     * perform the case change. Lowercase becomes titlecase, titlecase becomes uppercase, uppercase becomes
     * lowercase. if the text matches none of these categories, it is uppercased.
     * 
     * @param input
     *            : the string to work on
     * @param toWhat
     *            : one of the CASE_* values - except for case CASE_CYCLE.
     */
    private String doChangeCase(String input, CHANGE_CASE_TO toWhat) {
        Locale locale = Core.getProject().getProjectProperties().getTargetLanguage().getLocale();

        switch (toWhat) {
        case LOWER:
            return input.toLowerCase(locale);
        case UPPER:
            return input.toUpperCase(locale);
        case TITLE:
            // TODO: find out how to get a locale-aware title case
            return Character.toTitleCase(input.charAt(0)) + input.substring(1).toLowerCase(locale);
        }
        // if everything fails
        return input.toUpperCase(locale);
    }

    /**
     * {@inheritDoc}
     */
    public void replaceEditText(final String text) {
        UIThreadsUtil.mustBeSwingThread();

        // build local offsets
        int start = editor.getOmDocument().getTranslationStart();
        int end = editor.getOmDocument().getTranslationEnd();

        // remove text
        editor.select(start, end);
        editor.replaceSelection(text);
    }

    /**
     * {@inheritDoc}
     */
    public void insertText(final String text) {
        UIThreadsUtil.mustBeSwingThread();

        editor.checkAndFixCaret();

        editor.replaceSelection(text);
    }

    /**
     * {@inheritDoc}
     */
    public void gotoHistoryBack() {
        UIThreadsUtil.mustBeSwingThread();

        int prevValue = history.back();
        if (prevValue != -1) {
            gotoEntry(prevValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void gotoHistoryForward() {
        UIThreadsUtil.mustBeSwingThread();

        int nextValue = history.forward();
        if (nextValue != -1) {
            gotoEntry(nextValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    public EditorSettings getSettings() {
        return settings;
    }

    /**
     * {@inheritDoc}
     */
    public void undo() {
        UIThreadsUtil.mustBeSwingThread();

        try {
            if (editor.undoManager.canUndo()) {
                editor.undoManager.undo();
            }
        } catch (CannotUndoException cue) {
            Log.log(cue);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void redo() {
        UIThreadsUtil.mustBeSwingThread();

        try {
            if (editor.undoManager.canRedo()) {
                editor.undoManager.redo();
            }
        } catch (CannotRedoException cue) {
            Log.log(cue);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getSelectedText() {
        UIThreadsUtil.mustBeSwingThread();

        return dockableSelected ? editor.getSelectedText() : null;
    }

    /** Loads Instant start article */
    private void createAdditionalPanes() {
        introPaneTitle = OStrings.getString("DOCKING_INSTANT_START_TITLE");
        try {
            String language = detectInstantStartLanguage();
            introPane = new JTextPane();
            introPane
                    .setComponentOrientation(EditorUtils.isRTL(language) ? ComponentOrientation.RIGHT_TO_LEFT
                            : ComponentOrientation.LEFT_TO_RIGHT);
            introPane.setEditable(false);
            introPane.setPage(HelpFrame.getHelpFileURL(language, OConsts.HELP_INSTANT_START));
        } catch (IOException e) {
            // editorScroller.setViewportView(editor);
        }

        emptyProjectPaneTitle = OStrings.getString("TF_INTRO_EMPTYPROJECT_FILENAME");
        emptyProjectPane = new JTextPane();
        emptyProjectPane.setEditable(false);
        emptyProjectPane.setText(OStrings.getString("TF_INTRO_EMPTYPROJECT"));
        emptyProjectPane.setFont(Core.getMainWindow().getApplicationFont());
    }

    /**
     * Detects the language of the instant start guide (checks if present in default locale's language).
     * 
     * If there is no instant start guide in the default locale's language, "en" (English) is returned,
     * otherwise the acronym for the default locale's language.
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    private String detectInstantStartLanguage() {
        // Get the system language and country
        String language = Locale.getDefault().getLanguage().toLowerCase(Locale.ENGLISH);
        String country = Locale.getDefault().getCountry().toUpperCase(Locale.ENGLISH);

        // Check if there's a translation for the full locale (lang + country)
        if (HelpFrame.getHelpFileURL(language + "_" + country, OConsts.HELP_INSTANT_START) != null) {
            return language + "_" + country;
        }

        // Check if there's a translation for the language only
        if (HelpFrame.getHelpFileURL(language, OConsts.HELP_INSTANT_START) != null) {
            return language;
        }
        // Default to English, if no translation exists
        return "en";
    }

    /**
     * {@inheritDoc}
     */
    public void remarkOneMarker(final String markerClassName) {
        int mi = markerController.getMarkerIndex(markerClassName);
        markerController.process(m_docSegList, mi);
    }

    /**
     * {@inheritDoc}
     */
    public void markActiveEntrySource(final SourceTextEntry requiredActiveEntry, final List<Mark> marks,
            final String markerClassName) {
        UIThreadsUtil.mustBeSwingThread();

        for (Mark m : marks) {
            if (m.entryPart != Mark.ENTRY_PART.SOURCE) {
                throw new RuntimeException("Mark must be for source only");
            }
        }

        SourceTextEntry realActive = m_docSegList[displayedEntryIndex].ste;
        if (realActive != requiredActiveEntry) {
            return;
        }

        int mi = markerController.getMarkerIndex(markerClassName);
        markerController.setEntryMarks(displayedEntryIndex, m_docSegList[displayedEntryIndex], marks, mi);
    }

    public void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {
        editor.registerPopupMenuConstructors(priority, constructor);
    }

    /**
     * {@inheritdoc} Document is reloaded to immediately have the filter being effective.
     */
    public void addFilter(List<Integer> entryList) {
        this.entryFilterList = entryList;

        int curEntryNum = getCurrentEntryNumber();
        Document3 doc = editor.getOmDocument();
        IProject project = Core.getProject();
        if (doc != null && project != null && project.getProjectFiles() != null) { // prevent
                                                                                   // nullpointererrors
                                                                                   // in
                                                                                   // loadDocument.
                                                                                   // Only
                                                                                   // load
                                                                                   // if
                                                                                   // there
                                                                                   // is
                                                                                   // a
                                                                                   // document.
            loadDocument(); // rebuild entrylist
            if (isInFilter(curEntryNum)) {
                gotoEntry(curEntryNum);
            } else {
                // go to next (available) segment. But first, we need to reset
                // the
                // displayedEntryIndex to the number where the current but
                // filtered
                // entry could have been if it was not filtered.
                for (int j = 0; j < m_docSegList.length; j++) {
                    if (m_docSegList[j].segmentNumberInProject >= curEntryNum) { //
                        displayedEntryIndex = j - 1;
                        break;
                    }
                }
                nextEntry();
            }
        }
    }

    /**
     * {@inheritdoc} Document is reloaded if appropriate to immediately remove the filter;
     */
    public void removeFilter() {
        this.entryFilterList = null;
        int curEntryNum = getCurrentEntryNumber();
        Document3 doc = editor.getOmDocument();
        IProject project = Core.getProject();
        if (doc != null && project != null) { // prevent nullpointererrors in
                                              // loadDocument. Only load if
                                              // there is a document.
            List<FileInfo> files = project.getProjectFiles();
            if (files != null && !files.isEmpty()) {
                loadDocument();
                gotoEntry(curEntryNum);
            }
        }
    }

    /**
     * Returns if the given entry is part of the filtered entries.
     * 
     * @param entry
     *            project-wide entry number
     * @return true if entry belongs to the filtered entries, or if there is no filter in place, false
     *         otherwise.
     */
    public boolean isInFilter(Integer entry) {
        if (this.entryFilterList == null)
            return true;
        else
            return this.entryFilterList.contains(entry);
    }
    
    /**
     * {@inheritdoc}
     */
    public void setAlternateTranslationForCurrentEntry(boolean alternate) {
        SegmentBuilder sb = m_docSegList[displayedEntryIndex];

        if (!alternate) {
            // remove alternative translation from project
            SourceTextEntry ste = sb.getSourceTextEntry();
            Core.getProject().setTranslation(ste, null, null, false);

            // switch to default translation
            sb.setDefaultTranslation(true);
        } else {
            // switch to alternative translation
            sb.setDefaultTranslation(false);
        }
        setMenuEnabled();
    }
    
    /**
     * Class for checking if alternative translation exist.
     */
    protected static class HasMultipleTranslations implements IProject.MultipleTranslationsIterator {
        final String sourceEntryText;
        IProject project;
        boolean found;

        public HasMultipleTranslations(String sourceEntryText) {
            this.sourceEntryText = sourceEntryText;
            project = Core.getProject();
        }

        public void iterate(EntryKey source, TMXEntry trans) {
            if (found) {
                return;
            }
            if (sourceEntryText.equals(source.sourceText)) {
                found = true;
            }
        }
    }
}
