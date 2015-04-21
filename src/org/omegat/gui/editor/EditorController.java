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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.LastSegmentManager;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.SourceTextEntry.DUPLICATE;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.gui.editor.mark.CalcMarkersThread;
import org.omegat.gui.editor.mark.ComesFromTMMarker;
import org.omegat.gui.editor.mark.EntryMarks;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.help.HelpFrame;
import org.omegat.gui.main.DockablePanel;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.main.MainWindowUI;
import org.omegat.gui.tagvalidation.ITagValidation;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.StaticUIUtils;
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
 * @author Martin Fleurke
 * @author Guido Leenders
 * @Author Aaron Madlon-Kay
 * @author Piotr Kulik
 * @author Yu Tang
 */
public class EditorController implements IEditor {

    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(EditorController.class.getName());

    /** Some predefined translations that OmegaT can assign by popup. */
    enum ForceTranslation {
        UNTRANSLATED, EMPTY, EQUALS_TO_SOURCE;
    }

    /** Dockable pane for editor. */
    private DockablePanel pane;
    private JScrollPane scrollPane;

    private String title;

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

    volatile IEditorFilter entriesFilter;
    private Component entriesFilterControlComponent;

    private SegmentExportImport segmentExportImport;
    
    /**
     * Indicates, in nanoseconds, the last time a keypress was input.
     * This is reset to -1 upon commit or entering a segment.
     * Used by {@link ForceCommitTimer} to tell if the user is still
     * typing or not.
     */
    private long dirtyTime = -1;

    public EditorController(final MainWindow mainWindow) {
        this.mw = mainWindow;

        segmentExportImport = new SegmentExportImport(this);

        editor = new EditorTextArea3(this);
        setFont(Core.getMainWindow().getApplicationFont());

        markerController = new MarkerController(this);

        createUI();

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
                    markerController.removeAll();
                    setInitialOrientation();
                    break;
                case CLOSE:
                    history.clear();
                    removeFilter();
                    markerController.removeAll();
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

    private void createUI() {
        pane = new DockablePanel("EDITOR", " ", false);
        pane.setComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        pane.setMinimumSize(new Dimension(100, 100));
        pane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTitle();
            }
        });

        scrollPane = new JScrollPane(editor);
        Border panelBorder = UIManager.getBorder("OmegaTDockablePanel.border");
        if (panelBorder != null) { 
            scrollPane.setBorder(panelBorder);
        }
        Border viewportBorder = UIManager.getBorder("OmegaTDockablePanelViewport.border");
        if (viewportBorder != null) {
            scrollPane.setViewportBorder(viewportBorder);
        }
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pane.setLayout(new BorderLayout());
        pane.add(scrollPane, BorderLayout.CENTER);

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
    }

    private void updateState(SHOW_TYPE showType) {
        UIThreadsUtil.mustBeSwingThread();

        JComponent data = null;

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
                    LastSegmentManager.restoreLastSegment(EditorController.this);
                }
            });
            break;
        case NO_CHANGE:
            title = StaticUtils.format(OStrings.getString("GUI_SUBWINDOWTITLE_Editor"), getCurrentFile());
            data = editor;
            break;
        }

        updateTitle();
        pane.setToolTipText(title);

        if (scrollPane.getViewport().getView() != data) {
            if (UIManager.getBoolean("OmegaTDockablePanel.isProportionalMargins")) {
                int size = data.getFont().getSize() / 2;
                data.setBorder(new EmptyBorder(size, size, size, size));
            }
            scrollPane.setViewportView(data);
        }
    }

    private void updateTitle() {
        pane.setName(StaticUIUtils.truncateToFit(title, pane, 70));
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

        if (sourceLangIsRTL != targetLangIsRTL || sourceLangIsRTL != EditorUtils.localeIsRTL()) {
            currentOrientation = Document3.ORIENTATION.DIFFER;
        } else {
            if (sourceLangIsRTL) {
                currentOrientation = Document3.ORIENTATION.ALL_RTL;
            } else {
                currentOrientation = Document3.ORIENTATION.ALL_LTR;
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
        case ALL_LTR:
            targetOrientation = ComponentOrientation.LEFT_TO_RIGHT;
            break;
        case ALL_RTL:
            targetOrientation = ComponentOrientation.RIGHT_TO_LEFT;
            break;
        case DIFFER:
            if (targetLangIsRTL) { //using target lang direction gives better result when user starts editing.
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
        case ALL_LTR:
            newOrientation = Document3.ORIENTATION.ALL_RTL;
            break;
        case ALL_RTL:
            if (sourceLangIsRTL != targetLangIsRTL || sourceLangIsRTL != EditorUtils.localeIsRTL()) {
                newOrientation = Document3.ORIENTATION.DIFFER;
            } else {
                newOrientation = Document3.ORIENTATION.ALL_LTR;
            }
            break;
        case DIFFER:
            newOrientation = Document3.ORIENTATION.ALL_LTR;
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
     * returns the orientation of the document
     * (so we can decide what way of tag colouring we need;
     * if that has been fixed in an other way, this method can be removed again.).
     * @return
     */
    public Document3.ORIENTATION getOrientation() {
        return currentOrientation;
    }

    /**
     * {@inheritDoc}
     */
    public void requestFocus() {
        scrollPane.getViewport().getView().requestFocusInWindow();
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

        // remove old segments
        if (m_docSegList != null) {
            markerController.removeAll();
        }

        // check if RTL support required for document
        boolean hasRTL = sourceLangIsRTL || targetLangIsRTL || EditorUtils.localeIsRTL()
                || currentOrientation != Document3.ORIENTATION.ALL_LTR;
        Map<Language, ProjectTMX> otherLanguageTMs = Core.getProject().getOtherTargetLanguageTMs();
        for (Map.Entry<Language, ProjectTMX> entry : otherLanguageTMs.entrySet()) {
            hasRTL = hasRTL || EditorUtils.isRTL(entry.getKey().getLanguageCode().toLowerCase());
        }

        Document3 doc = new Document3(this);

        ArrayList<SegmentBuilder> temp_docSegList2 = new ArrayList<SegmentBuilder>(file.entries.size());
        for (int i = 0; i < file.entries.size(); i++) {
            SourceTextEntry ste = file.entries.get(i);
            if (entriesFilter == null || entriesFilter.allowed(ste)) {
                SegmentBuilder sb = new SegmentBuilder(this, doc, settings, ste, ste.entryNum(), hasRTL);
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
        editor.undoManager.reset();

        doc.addDocumentListener(new DocumentListener() {
            //we cannot edit the document here, only other stuff.
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

        markerController.process(m_docSegList);

        editor.repaint();
    }

    /*
     * Activates the current entry and puts the cursor at the start of segment
     */
    public void activateEntry() {
        activateEntry(CaretPosition.startOfEntry());
    }

    /**
     * Activates the current entry (if available) by displaying source text and embedding displayed text in
     * markers.
     * <p>
     * Also moves document focus to current entry, and makes sure fuzzy info displayed if available.
     */
    public void activateEntry(CaretPosition pos) {
        UIThreadsUtil.mustBeSwingThread();

        SourceTextEntry ste = getCurrentEntry();
        if (ste == null) {
            return;
        }

        if (scrollPane.getViewport().getView() != editor) {
            // editor not displayed
            return;
        }

        if (!Core.getProject().isProjectLoaded())
            return;

        // forget about old marks
        m_docSegList[displayedEntryIndex].createSegmentElement(true);
        
        Core.getNotes().setNoteText(Core.getProject().getTranslationInfo(ste).note);

        // then add new marks
        markerController.reprocessImmediately(m_docSegList[displayedEntryIndex]);

        editor.undoManager.reset();

        history.insertNew(m_docSegList[displayedEntryIndex].segmentNumberInProject);
        
        setMenuEnabled();

        showStat();

        showLengthMessage();

        if (Preferences.isPreference(Preferences.EXPORT_CURRENT_SEGMENT)) {
            segmentExportImport.exportCurrentSegment(ste);
        }

        int te = editor.getOmDocument().getTranslationEnd();
        int ts = editor.getOmDocument().getTranslationStart();
        //
        // Navigate to entry as requested.
        //
        if (pos.position!=null) { // check if outside of entry
            pos.position = Math.max(0, pos.position);
            pos.position = Math.min(pos.position, te-ts);
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

        editor.autoCompleter.hidePopup();
        editor.repaint();

        // fire event about new segment activated
        CoreEvents.fireEntryActivated(ste);
        
        dirtyTime = -1;
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
        if (doc.trustedChangesInProgress || doc.textBeingComposed) {
            return;
        }
        if (doc.isEditMode()) {
            dirtyTime = System.nanoTime();
            m_docSegList[displayedEntryIndex].onActiveEntryChanged();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    markerController.reprocessImmediately(m_docSegList[displayedEntryIndex]);
                    editor.autoCompleter.updatePopup();
                }
            });
        }
    }

    /**
     * Display some segments before and after when user on the top or bottom of page.
     */
    private void scrollForDisplayNearestSegments(final CaretPosition pos) {
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
                                        setCaretPosition(pos);
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
     * Calculate statistic for file, request statistic for project and display in status bar.
     */
    public void showStat() {
        IProject project = Core.getProject();
        IProject.FileInfo fi = project.getProjectFiles().get(displayedFileIndex);
        int translatedInFile = 0;
        int translatedUniqueInFile = 0;
        int uniqueInFile = 0;
        boolean isUnique;
        for (SourceTextEntry ste : fi.entries) {
            isUnique = ste.getDuplicate() != SourceTextEntry.DUPLICATE.NEXT;
            if (isUnique) {
                uniqueInFile++;
            }
            if (project.getTranslationInfo(ste).isTranslated()) {
                translatedInFile++;
                if (isUnique) {
                    translatedUniqueInFile++;
                }
            }
        }

        StatisticsInfo stat = project.getStatistics();
        StringBuilder pMsg = new StringBuilder(1024).append(" ");
        final MainWindowUI.STATUS_BAR_MODE progressMode =
                Preferences.getPreferenceEnumDefault(Preferences.SB_PROGRESS_MODE,
                        MainWindowUI.STATUS_BAR_MODE.DEFAULT);
        
        if (progressMode == MainWindowUI.STATUS_BAR_MODE.DEFAULT) {
            pMsg.append(translatedInFile).append("/").append(fi.entries.size()).append(" (")
                    .append(stat.numberofTranslatedSegments).append("/").append(stat.numberOfUniqueSegments)
                    .append(", ").append(stat.numberOfSegmentsTotal).append(") ");
        } else {
            /*
             * Percentage mode based on idea by Yu Tang
             * http://dirtysexyquery.blogspot.tw/2013/03/omegat-custom-progress-format.html
             */
            java.text.NumberFormat nfPer = java.text.NumberFormat.getPercentInstance();
            nfPer.setRoundingMode(java.math.RoundingMode.DOWN);
            nfPer.setMaximumFractionDigits(1);
            if (translatedUniqueInFile == 0) {
                pMsg.append("0%");
            } else {
                pMsg.append(nfPer.format((double)translatedUniqueInFile / uniqueInFile));
            }
            pMsg.append(" (").append(uniqueInFile - translatedUniqueInFile)
                    .append(OStrings.getString("MW_PROGRESS_LEFT_LABEL")).append(") / ");

            if (stat.numberofTranslatedSegments == 0) {
                pMsg.append("0%");
            } else {
                pMsg.append(nfPer.format((double)stat.numberofTranslatedSegments / stat.numberOfUniqueSegments));
            }
            pMsg.append(" (").append(stat.numberOfUniqueSegments - stat.numberofTranslatedSegments)
                    .append(OStrings.getString("MW_PROGRESS_LEFT_LABEL")).append(") ")
                    .append(", ").append(stat.numberOfSegmentsTotal).append(" ");
        }

        Core.getMainWindow().showProgressMessage(pMsg.toString());
    }

    /**
     * Go to segment at specified location.
     * 
     * @param location
     *            location
     * @return true if segment changed, false if location inside current segment
     */
    protected boolean goToSegmentAtLocation(int location) {
        // clicked segment

        int segmentAtLocation = getSegmentIndexAtLocation(location);
        if (displayedEntryIndex != segmentAtLocation) {
            commitAndDeactivate();
            displayedEntryIndex = segmentAtLocation;
            activateEntry();
            return true;
        } else {
            return false;
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
     * Refresh some entries. Usually after external translation changes replacement.
     */
    public void refreshEntries(Set<Integer> entryNumbers) {
        for (int i = 0; i < m_docSegList.length; i++) {
            if (entryNumbers.contains(m_docSegList[i].ste.entryNum())) {
                // the same source text - need to update
                m_docSegList[i].createSegmentElement(false);
            }
        }
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

        String newTrans = doc.extractTranslation();
        if (newTrans != null) {
            commitAndDeactivate(null, newTrans);
        }
        dirtyTime = -1;
    }

    void commitAndDeactivate(ForceTranslation forceTranslation, String newTrans) {
        UIThreadsUtil.mustBeSwingThread();

        Document3 doc = editor.getOmDocument();
        doc.stopEditMode();

        // segment was active
        SegmentBuilder sb = m_docSegList[displayedEntryIndex];
        SourceTextEntry entry = sb.ste;

        TMXEntry oldTE = Core.getProject().getTranslationInfo(entry);

        PrepareTMXEntry newen = new PrepareTMXEntry();
        newen.source = sb.getSourceText();
        newen.note = Core.getNotes().getNoteText();
        boolean defaultTranslation = sb.isDefaultTranslation();
        if (forceTranslation != null) { // there is force translation
            switch (forceTranslation) {
            case UNTRANSLATED:
                newen.translation = null;
                break;
            case EMPTY:
                newen.translation = "";
                break;
            case EQUALS_TO_SOURCE:
                newen.translation = newen.source;
                break;
            }
        } else { // translation from editor
            if (newTrans.isEmpty()) {// empty translation
                if (oldTE.isTranslated() && "".equals(oldTE.translation)) {
                    // It's an empty translation which should remain empty
                    newen.translation = "";
                } else {
                    newen.translation = null;// will be untranslated
                }
            } else if (newTrans.equals(newen.source)) {// equals to source
                if (Preferences.isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC)) {
                    // translation can be equals to source
                    newen.translation = newTrans;
                } else {
                    // translation can't be equals to source
                    if (oldTE.source.equals(oldTE.translation)) {
                        // but it was equals to source before
                        newen.translation = oldTE.translation;
                    } else {
                        // set untranslated
                        newen.translation = null;
                    }
                }
            } else {
                // new translation is not empty and not equals to source - just change
                newen.translation = newTrans;
            }
        }

        if (StringUtil.equalsWithNulls(oldTE.translation, newen.translation)) {
            // translation wasn't changed
            if (!StringUtil.nvl(oldTE.note, "").equals(StringUtil.nvl(newen.note, ""))) {
                // note was changed
                Core.getProject().setNote(entry, oldTE, newen.note);
            }
        } else {
            Core.getProject().setTranslation(entry, newen, defaultTranslation, null);
        }

        m_docSegList[displayedEntryIndex].createSegmentElement(false);

        // find all identical sources and redraw them
        for (int i = 0; i < m_docSegList.length; i++) {
            if (i == displayedEntryIndex) {
                // current entry, skip
                continue;
            }
            if (m_docSegList[i].ste.getSrcText().equals(entry.getSrcText())) {
                // the same source text - need to update
                m_docSegList[i].createSegmentElement(false);
                // then add new marks
                markerController.reprocessImmediately(m_docSegList[i]);
            }
        }

        Core.getNotes().clear();

        // then add new marks
        markerController.reprocessImmediately(m_docSegList[displayedEntryIndex]);

        editor.undoManager.reset();

        // validate tags if required
        if (entry != null && Preferences.isPreference(Preferences.TAG_VALIDATE_ON_LEAVE)) {
            final SourceTextEntry ste = entry;
            new SwingWorker<Object, Void>() {
                protected Object doInBackground() throws Exception {
                    ITagValidation tv = Core.getTagValidation();
                    if (!tv.checkInvalidTags(ste)) {
                        tv.displayTagValidationErrors(tv.listInvalidTags(), null);
                    }
                    return null;
                }
            }.execute();
        }
        
        synchronized (this) {
            notifyAll();
        }
        dirtyTime = -1;
    }

    /**
     * Deactivate active translation without save. Required on project close postprocessing, for example.
     */
    protected void deactivateWithoutCommit() {
        UIThreadsUtil.mustBeSwingThread();

        segmentExportImport.exportCurrentSegment(null);

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
        if (Core.getProject().getAllEntries().isEmpty()) {
            return; // empty project
        }
    //
    // Memorize current position of cursor.
    // After deactivating and activating with shrinking and expanding text, we might
    // be able to position the current at this position again.
    //
        int currentPosition = getCurrentPositionInEntryTranslation();
        commitAndDeactivate();
        activateEntry(new CaretPosition(currentPosition));
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
        if (files.isEmpty()) {
            return;
        }
        SourceTextEntry ste;
        int startFileIndex = displayedFileIndex;
        int startEntryIndex = displayedEntryIndex;
        boolean looped = false;
        while (true) {
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
            if (ste != null) {
            	// We found an entry
            	break;
            }
            if (looped && displayedFileIndex == startFileIndex) {
                if (displayedEntryIndex >= startEntryIndex) {
                    // We have looped back to our starting point
                    break;
                }
                if (m_docSegList.length == 0) {
                    // We have looped back to our starting point
                    // and there were no hits in any files
                    break;
                }
            }
        }

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
        if (files.isEmpty()) {
            return;
        }
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
     * Find the next (un)translated entry.
     * @param findTranslated should the next entry be translated or not.
     */
    private void nextTranslatedEntry(boolean findTranslated) {
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
        if (files.isEmpty()) {
            return;
        }
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
            
            if (!findTranslated) {
                if (!Core.getProject().getTranslationInfo(ste).isTranslated()) {
                    break;// non-translated
                }
            } else {
                if (Core.getProject().getTranslationInfo(ste).isTranslated()) {
                    break;// translated
                }
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
     * Finds the next untranslated entry in the document.
     */
    public void nextUntranslatedEntry() {
        nextTranslatedEntry(false);
    }

    /**
     * Finds the next translated entry in the document.
     */
    public void nextTranslatedEntry() {
        nextTranslatedEntry(true);
    }

    /**
     * Finds the next entry with a non-empty note.
     */
    public void nextEntryWithNote() {
        UIThreadsUtil.mustBeSwingThread();

        // Check if a document is loaded.
        if (Core.getProject().isProjectLoaded() == false)
            return;

        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = this.editor.getCursor();
        this.editor.setCursor(hourglassCursor);

        // Save the current entry.
        commitAndDeactivate();

        List<FileInfo> files = Core.getProject().getProjectFiles();
        if (files.isEmpty()) {
            return;
        }
        SourceTextEntry ste;
        int startFileIndex = displayedFileIndex;
        int startEntryIndex = displayedEntryIndex;
        do {
            // Navigate to next entry in the order: segment -> project file -> project.
            displayedEntryIndex++;
            if (displayedEntryIndex >= m_docSegList.length) {
                // File finished - need new.
                displayedFileIndex++;
                displayedEntryIndex = 0;
                if (displayedFileIndex >= files.size()) {
                    displayedFileIndex = 0;
                }
                // To get proper EntryIndex when filter active.
                loadDocument(); 
            }
            ste = getCurrentEntry();

            if (ste == null) {
                // Filtered file has no entries.
                break;
            }
            if (displayedFileIndex == startFileIndex && displayedEntryIndex == startEntryIndex) {
                // Found no segment with a note. Cursor remains at starting position.
                break; 
            }
            if (Core.getProject().getTranslationInfo(ste).hasNote()) {
                // Non-translated.
                break;
            }
        } while (true);

        activateEntry();

        this.editor.setCursor(oldCursor);
    }

    /**
     * Finds the previous entry with a non-empty note.
     */
    public void prevEntryWithNote() {
        UIThreadsUtil.mustBeSwingThread();

        // Check if a document is loaded.
        if (!Core.getProject().isProjectLoaded())
            return;

        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = this.editor.getCursor();
        this.editor.setCursor(hourglassCursor);

        // Save the current entry.
        commitAndDeactivate();

        List<FileInfo> files = Core.getProject().getProjectFiles();
        if (files.isEmpty()) {
            return;
        }
        SourceTextEntry ste;
        int startFileIndex = displayedFileIndex;
        int startEntryIndex = displayedEntryIndex;
        do {
            displayedEntryIndex--;
            if (displayedEntryIndex < 0) {
                displayedFileIndex--;
                if (displayedFileIndex < 0) {
                    displayedFileIndex = files.size() - 1;
                }
                loadDocument();
                displayedEntryIndex = m_docSegList.length - 1;
            }
            ste = getCurrentEntry();
            if (ste == null) {
                // Filtered file has no entries.
                break;
            }
            if (displayedFileIndex == startFileIndex && displayedEntryIndex == startEntryIndex) {
                // Found no segment with a note. Cursor remains at starting position.
                break; 
            }
            if (Core.getProject().getTranslationInfo(ste).hasNote()) {
                // Non-translated.
                break;
            }
        } while (true);

        activateEntry();

        this.editor.setCursor(oldCursor);
    }

    /**
     * Find the next unique entry.
     * @param findTranslated should the next entry be translated or not.
     */
    public void nextUniqueEntry() {
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
        if (files.isEmpty()) {
            return;
        }
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
            
            if (ste.getDuplicate() != DUPLICATE.NEXT){
                break;
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
        gotoEntry(entryNum, CaretPosition.startOfEntry());
    }    
    
    public void gotoEntry(final int entryNum, final CaretPosition pos) {
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
        activateEntry(pos);
        this.editor.setCursor(oldCursor);
    }

    public void gotoEntry(String srcString, EntryKey key) {
        UIThreadsUtil.mustBeSwingThread();
        
        /*
         * Goto segment with contains matched source. Since it enough rarely executed code, it
         * will be better to find this segment each time, instead use additional memory storage.
         */
        List<SourceTextEntry> entries = Core.getProject().getAllEntries();
        for (int i = 0; i < entries.size(); i++) {
            SourceTextEntry ste = entries.get(i);
            if (srcString != null && !ste.getSrcText().equals(srcString)) {
                // source text not equals - there is no sense to checking this entry
                continue;
            }
            if (key != null) {
                // multiple translation
                if (!ste.getKey().equals(key)) {
                    continue;
                }
            } else {
                // default translation - multiple shouldn't exist for this entry
                TMXEntry trans = Core.getProject().getTranslationInfo(entries.get(i));
                if (!trans.isTranslated() || !trans.defaultTranslation) {
                    // we need exist alternative translation
                    continue;
                }
            }
            gotoEntry(i + 1);
            break;
        }
    }

    public void gotoEntryAfterFix(final int entryNum, final String fixedSource) {
        UIThreadsUtil.mustBeSwingThread();

        // Don't commit the current translation text if we fixed this entry
        // or one of its duplicates (the fixed version will be clobbered).
        if (entryNum == getCurrentEntryNumber() || getCurrentEntry().getSrcText().equals(fixedSource)) {
            deactivateWithoutCommit();
        }
        gotoFile(displayedFileIndex);
        gotoEntry(entryNum);
    }

    public void refreshViewAfterFix(List<Integer> fixedEntries) {
        // Don't commit the current translation text if we fixed this entry
        // or one of its duplicates (the fixed version will be clobbered).
        boolean doCommit = fixedEntries != null && fixedEntries.contains(getCurrentEntryNumber());
        refreshView(doCommit);
    }
    
    public void refreshView(boolean doCommit) {
        UIThreadsUtil.mustBeSwingThread();
        
        if (!doCommit) {
            deactivateWithoutCommit();
        }
        int currentEntry = getCurrentEntryNumber();
        int caretPosition = getCurrentPositionInEntryTranslation();
        gotoFile(displayedFileIndex);
        gotoEntry(currentEntry, new CaretPosition(caretPosition));
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
    @Override
    public void replaceEditText(final String text) {
        UIThreadsUtil.mustBeSwingThread();

        // build local offsets
        int start = editor.getOmDocument().getTranslationStart();
        int end = editor.getOmDocument().getTranslationEnd();

        CalcMarkersThread thread = markerController.markerThreads[markerController
                .getMarkerIndex(ComesFromTMMarker.class.getName())];
        ((ComesFromTMMarker) thread.marker).setMark(null, null);

        // remove text
        editor.select(start, end);
        editor.replaceSelection(text);
    }

    public void replacePartOfText(final String text, int start, int end) {
        UIThreadsUtil.mustBeSwingThread();

        CalcMarkersThread thread = markerController.markerThreads[markerController
                .getMarkerIndex(ComesFromTMMarker.class.getName())];
        ((ComesFromTMMarker) thread.marker).setMark(null, null);

        int off = editor.getOmDocument().getTranslationStart();
        // remove text
        editor.select(start + off, end + off);
        editor.replaceSelection(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceEditTextAndMark(String text) {
        replaceEditText(text);

        // mark as comes from TM
        SegmentBuilder sb = m_docSegList[displayedEntryIndex];
        CalcMarkersThread thread = markerController.markerThreads[markerController
                .getMarkerIndex(ComesFromTMMarker.class.getName())];
        ((ComesFromTMMarker) thread.marker).setMark(sb.getSourceTextEntry(), text);
        markerController.reprocessImmediately(sb);
    }

    public void replacePartOfTextAndMark(String text, int start, int end) {
        replacePartOfText(text, start, end);

        // mark as comes from TM
        SegmentBuilder sb = m_docSegList[displayedEntryIndex];
        CalcMarkersThread thread = markerController.markerThreads[markerController
                .getMarkerIndex(ComesFromTMMarker.class.getName())];
        ((ComesFromTMMarker) thread.marker).setMark(sb.getSourceTextEntry(), text);
        markerController.reprocessImmediately(sb);
    }

    public String getCurrentTranslation() {
        UIThreadsUtil.mustBeSwingThread();

        return editor.getOmDocument().extractTranslation();
    }

    /**
     * Returns current caret position in the editable translation.
     */
    public int getCurrentPositionInEntryTranslation() {
        UIThreadsUtil.mustBeSwingThread();

        return getPositionInEntryTranslation(editor.getCaretPosition());
    }

    /**
     * Returns the relative caret position in the editable translation for a
     * given absolute index into the overall editor document.
     */
    public int getPositionInEntryTranslation(int pos) {
        UIThreadsUtil.mustBeSwingThread();

        if (!editor.getOmDocument().isEditMode()) {
            return -1;
        }
        int beg = editor.getOmDocument().getTranslationStart();
        int end = editor.getOmDocument().getTranslationEnd();
        if (pos < beg) {
            pos = beg;
        }
        if (pos > end) {
            pos = end;
        }
        return pos - beg;
    }

    public void setCaretPosition(CaretPosition pos) {
        UIThreadsUtil.mustBeSwingThread();

        if (!editor.getOmDocument().isEditMode()) {
            return;
        }
        int off = editor.getOmDocument().getTranslationStart();

        try {
            if (pos.position != null) {
                editor.setCaretPosition(off + pos.position);
            } else if (pos.selectionStart != null && pos.selectionEnd != null) {
                editor.select(off + pos.selectionStart, off + pos.selectionEnd);
            }
        } catch (IllegalArgumentException iae) {
            // ignore; document has changed in the mean time.
        }
        editor.checkAndFixCaret();
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

        editor.undoManager.undo();
    }

    /**
     * {@inheritDoc}
     */
    public void redo() {
        UIThreadsUtil.mustBeSwingThread();

        editor.undoManager.redo();
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
        markerController.reprocess(m_docSegList, mi);
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
        EntryMarks ev = new EntryMarks(m_docSegList[displayedEntryIndex],
                m_docSegList[displayedEntryIndex].getDisplayVersion(), mi);
        ev.result = marks;
        markerController.queueMarksOutput(ev);
    }

    public void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {
        editor.registerPopupMenuConstructors(priority, constructor);
    }

    @Override
    public IEditorFilter getFilter() {
        return entriesFilter;
    }

    /**
     * {@inheritdoc} Document is reloaded to immediately have the filter being effective.
     */
    public void setFilter(IEditorFilter filter) {
        UIThreadsUtil.mustBeSwingThread();

        if (entriesFilterControlComponent != null) {
            pane.remove(entriesFilterControlComponent);
        }

        entriesFilter = filter;
        entriesFilterControlComponent = filter.getControlComponent();
        pane.add(entriesFilterControlComponent, BorderLayout.NORTH);
        pane.revalidate();

        SourceTextEntry curEntry = getCurrentEntry();
        Document3 doc = editor.getOmDocument();
        IProject project = Core.getProject();
        if (doc != null && project != null && project.getProjectFiles() != null // prevent
                && curEntry != null) {                                          // nullpointererrors
                                                                                // in
                                                                                // loadDocument.
                                                                                // Only
                                                                                // load
                                                                                // if
                                                                                // there
                                                                                // is
                                                                                // a
                                                                                // document.
            int curEntryNum = curEntry.entryNum();
            loadDocument(); // rebuild entrylist
            if (entriesFilter == null || entriesFilter.allowed(curEntry)) {
                gotoEntry(curEntry.entryNum());
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
        UIThreadsUtil.mustBeSwingThread();
        
        entriesFilter = null;
        if (entriesFilterControlComponent != null) {
            pane.remove(entriesFilterControlComponent);
            pane.revalidate();
            entriesFilterControlComponent = null;
        }

        int curEntryNum = getCurrentEntryNumber();
        Document3 doc = editor.getOmDocument();
        IProject project = Core.getProject();
        // `if` check is to prevent NullPointerErrors in loadDocument.
        // Only load if there is a document and the project is loaded.
        if (doc != null && project != null && project.isProjectLoaded()) {
            List<FileInfo> files = project.getProjectFiles();
            if (files != null && !files.isEmpty()) {
                loadDocument();
                gotoEntry(curEntryNum);
            }
        }
    }
    
    /**
     * {@inheritdoc}
     */
    public void setAlternateTranslationForCurrentEntry(boolean alternate) {
        SegmentBuilder sb = m_docSegList[displayedEntryIndex];

        if (!alternate) {
            // remove alternative translation from project
            SourceTextEntry ste = sb.getSourceTextEntry();
            PrepareTMXEntry en = new PrepareTMXEntry();
            en.source = ste.getSrcText();
            Core.getProject().setTranslation(ste, en, false, null);

            // switch to default translation
            sb.setDefaultTranslation(true);
        } else {
            // switch to alternative translation
            sb.setDefaultTranslation(false);
        }
        setMenuEnabled();
    }

    @Override
    public void registerUntranslated() {
        UIThreadsUtil.mustBeSwingThread();

        commitAndDeactivate(ForceTranslation.UNTRANSLATED, null);
        activateEntry();
    }

    @Override
    public void registerEmptyTranslation() {
        UIThreadsUtil.mustBeSwingThread();

        commitAndDeactivate(ForceTranslation.EMPTY, null);
        activateEntry();
    }

    @Override
    public void registerIdenticalTranslation() {
        UIThreadsUtil.mustBeSwingThread();

        commitAndDeactivate(ForceTranslation.EQUALS_TO_SOURCE, null);
        activateEntry();
    }

    @Override
    public void windowDeactivated() {
        editor.autoCompleter.hidePopup();
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

    /**
     * Storage for caret position and selection.
     */
    public static class CaretPosition {
        Integer position;
        Integer selectionStart, selectionEnd;

        public CaretPosition(int position) {
            this.position = position;
            this.selectionStart = null;
            this.selectionEnd = null;
        }

        public CaretPosition(int selectionStart, int selectionEnd) {
            this.position = null;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }

        /**
         * We can't define it once since 'position' can be changed later.
         */
        public static CaretPosition startOfEntry() {
            return new CaretPosition(0);
        }
    }
    
    @Override
    public void waitForCommit(int timeoutSeconds) {
        ForceCommitTimer timer;
        if (dirtyTime == -1) {
            return;
        } else {
            timer = new ForceCommitTimer(timeoutSeconds);
            timer.start();
        }
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            timer.cancel();
        }
    }

    public AlphabeticalMarkers getAlphabeticalMarkers() {
        return new AlphabeticalMarkers(scrollPane) {

            @Override
            protected Map<Integer, Point> getViewableSegmentLocations() {
                final int UPPER_GAP = 5;
                Map<Integer, Point> map = new LinkedHashMap<Integer, Point>(); // keep putting order

                // no segments
                if (m_docSegList == null) {
                    return map;
                }

                JViewport viewport = scrollPane.getViewport();
                int x = sourceLangIsRTL
                        ? editor.getWidth() - editor.getInsets().right
                        : editor.getInsets().left;
                Rectangle viewRect = viewport.getViewRect();

                // expand a bit rect for the segment at the upper end of the editor.
                viewRect.setBounds(viewRect.x, viewRect.y - UPPER_GAP,
                                    viewRect.width, viewRect.height + UPPER_GAP);

                Point viewPosition = viewport.getViewPosition();
                for (SegmentBuilder sb : m_docSegList) {
                    try {
                        Point location = editor.modelToView(sb.getStartPosition()).getLocation();
                        if (viewRect.contains(location)) { // location is viewable
                            int segmentNo = sb.segmentNumberInProject;
                            location.translate(0, -viewPosition.y); // adjust to vertically view position
                            location.x = x;                          // align in the left or right border
                            map.put(segmentNo, location);
                        }
                    } catch (BadLocationException ex) {
                        // Eat exception silently
                    }
                }
                return map;
            }
        };
    }

    private class ForceCommitTimer extends Thread {
        
        private final long limit;
        private boolean isCanceled = false;
        
        public ForceCommitTimer(int limit) {
            this.limit = limit * 1000000000L;
        }
        
        @Override
        public void run() {
            while (!isCanceled) {
                long t = System.nanoTime() - dirtyTime;
                if (t >= limit) {
                    UIThreadsUtil.executeInSwingThread(new Runnable() {
                        @Override
                        public void run() {
                            commitAndLeave();
                        }
                    });
                    Core.getMainWindow().showStatusMessageRB("TEAM_SYNCHRONIZE");
                    break;
                } else if (t >= limit - 5000000000L) {
                    Core.getMainWindow().showStatusMessageRB("TEAM_SYNCHRONIZE_COUNTDOWN", (limit - t) / 1000000000L);
                } else {
                    Core.getMainWindow().showStatusMessageRB("TEAM_SYNCHRONIZE_WAITING");
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void cancel() {
            this.isCanceled = true;
        }
    }
}
