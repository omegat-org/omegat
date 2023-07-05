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
               2019 Thomas Cordonnier, Briac Pilpre
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
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
import org.omegat.core.data.IProject.OptimisticLockingFail;
import org.omegat.core.data.LastSegmentManager;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.SourceTextEntry.DUPLICATE;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.gui.dialogs.ConflictDialogController;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
import org.omegat.gui.editor.mark.CalcMarkersThread;
import org.omegat.gui.editor.mark.ComesFromMTMarker;
import org.omegat.gui.editor.mark.EntryMarks;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.main.DockablePanel;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.main.MainWindowUI;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.help.Help;
import org.omegat.util.BiDiUtils;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXProp;
import org.omegat.util.gui.DragTargetOverlay;
import org.omegat.util.gui.DragTargetOverlay.IDropInfo;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIDesignManager;
import org.omegat.util.gui.UIThreadsUtil;

import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * Class for control all editor operations.
 * <p>
 * You can find good description of java text editor working at
 * <a href="https://www.comp.nus.edu.sg/~cs3283/ftp/Java/swingConnect/text/text/text.html">Using the Swing Text Package</a>
 * that was originally found at http://java.sun.com/products/jfc/tsc/articles/text/overview/
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
 * @author Aaron Madlon-Kay
 * @author Piotr Kulik
 * @author Yu Tang
 */
public class EditorController implements IEditor {

    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(EditorController.class.getName());

    private static final double PAGE_LOAD_THRESHOLD = 0.25;

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

    private String introPaneTitle;
    private String emptyProjectPaneTitle;
    private JTextPane introPane;
    private JTextPane emptyProjectPane;
    protected final MainWindow mw;

    /** Currently displayed segments info. */
    protected SegmentBuilder[] m_docSegList;

    protected int firstLoaded;
    protected int lastLoaded;

    protected Timer lazyLoadTimer = new Timer(200, null);

    /** Current displayed file. */
    protected int displayedFileIndex;
    protected int previousDisplayedFileIndex;
    /**
     * Current active segment in current file, if there are segments in file (can be fale if filter active!)
     */
    protected int displayedEntryIndex;

    /** Object which store history of moving by segments. */
    private SegmentHistory history = new SegmentHistory();

    protected final EditorSettings settings;

    protected Font font;

    private enum SHOW_TYPE {
        INTRO, EMPTY_PROJECT, FIRST_ENTRY, NO_CHANGE
    };

    BiDiUtils.ORIENTATION currentOrientation;
    protected boolean sourceLangIsRTL;
    protected boolean targetLangIsRTL;

    volatile IEditorFilter entriesFilter;
    private Component entriesFilterControlComponent;

    private SegmentExportImport segmentExportImport;

    protected String currentEntryOrigin;
    protected String translationFromOrigin;

    /**
     * Previous translations. Used for optimistic locking.
     */
    private IProject.AllTranslations previousTranslations;

    public EditorController(final MainWindow mainWindow) {
        this.mw = mainWindow;

        segmentExportImport = new SegmentExportImport(this);

        editor = new EditorTextArea3(this);
        DragTargetOverlay.apply(editor, dropInfo);
        setFont(mainWindow.getApplicationFont());

        markerController = new MarkerController(this);

        createUI();

        settings = new EditorSettings(this);

        CoreEvents.registerProjectChangeListener(eventType -> {
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
                m_docSegList = null;
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

        SwingUtilities.invokeLater(() -> {
            updateState(SHOW_TYPE.INTRO);
            pane.requestFocus();
        });

        // register font changes callback
        CoreEvents.registerFontChangedEventListener(newFont -> {
            setFont(newFont);
            ViewLabel.fontHeight = 0;
            editor.revalidate();
            editor.repaint();

            // fonts have changed
            emptyProjectPane.setFont(font);
        });

        // register Swing error logger
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOGGER.log(Level.SEVERE, "Uncatched exception in thread [" + t.getName() + "]", e);
        });

        EditorPopups.init(this);

        lazyLoadTimer.setRepeats(false);
        lazyLoadTimer.addActionListener(e -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            double scrollPercent = bar.getValue() / (double) bar.getMaximum();
            int unitsPerSeg = (bar.getMaximum() - bar.getMinimum()) / (lastLoaded - firstLoaded + 1);
            if (firstLoaded > 0 && scrollPercent <= PAGE_LOAD_THRESHOLD) {
                int docSize = editor.getDocument().getLength();
                int visiblePos = editor.viewToModel2D(scrollPane.getViewport().getViewPosition());
                // Try to load enough segments to restore scrollbar value to
                // the range (PAGE_LOAD_THRESHOLD, 1 - PAGE_LOAD_THRESHOLD).
                // Formula is obtained by solving the following equations for loadCount:
                //   PAGE_LOAD_THRESHOLD = newVal / newMax
                //   newVal = curVal + loadCount * unitsPerSeg
                //   newMax = curMax + loadCount * unitsPerSeg
                double loadCount = (PAGE_LOAD_THRESHOLD * bar.getMaximum() - bar.getValue())
                        / (unitsPerSeg * (1 - PAGE_LOAD_THRESHOLD));
                loadUp((int) Math.ceil(loadCount));
                // If we leave the viewport at the same location then we are
                // not looking at the same content, because what we were
                // looking at is now further down the document. Calculate
                // the correct location and scroll there.
                int sizeDelta = editor.getDocument().getLength() - docSize;
                try {
                    scrollPane.getViewport()
                            .setViewPosition(editor.modelToView2D(visiblePos + sizeDelta).getBounds().getLocation());
                } catch (BadLocationException ex) {
                    Log.log(ex);
                }
            } else if (lastLoaded < m_docSegList.length - 1 && scrollPercent >= 1 - PAGE_LOAD_THRESHOLD) {
                // Load enough segments to restore scrollbar value to the
                // range (PAGE_LOAD_THRESHOLD, 1 - PAGE_LOAD_THRESHOLD).
                // Formula is obtained by solving the following equations for loadCount:
                //   (1 - PAGE_LOAD_THRESHOLD) = curVal / newMax
                //   newMax = curMax + loadCount * unitsPerSeg
                double loadCount = (bar.getValue() / (1 - PAGE_LOAD_THRESHOLD) - bar.getMaximum()) / unitsPerSeg;
                loadDown((int) Math.ceil(loadCount));
            }
        });
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
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(scrollListener);

        pane.setLayout(new BorderLayout());
        pane.add(scrollPane, BorderLayout.CENTER);

        mw.addDockable(pane);

        DockingDesktop desktop = UIDesignManager.getDesktop(pane);
        if (desktop != null) {
            desktop.addDockableSelectionListener(e -> dockableSelected = pane == e.getSelectedDockable());
        }
    }

    private final AdjustmentListener scrollListener = (AdjustmentEvent e) -> {
        if (m_docSegList == null) {
            return;
        }

        if (e.getValueIsAdjusting()) {
            return;
        }

        if (lazyLoadTimer.isRunning()) {
            return;
        }

        double pos = e.getValue() / (double) scrollPane.getVerticalScrollBar().getMaximum();

        if (pos <= PAGE_LOAD_THRESHOLD || pos >= 1.0 - PAGE_LOAD_THRESHOLD) {
            lazyLoadTimer.restart();
        }
    };

    private synchronized void loadDown(int count) {
        if (lastLoaded < 0 || lastLoaded >= m_docSegList.length - 1) {
            return;
        }
        int loadFrom = lastLoaded + 1;
        int loadTo = Math.min(m_docSegList.length - 1, loadFrom + count - 1);
        Document3 doc = editor.getOmDocument();
        for (int i = loadFrom; i <= loadTo; i++) {
            SegmentBuilder builder = m_docSegList[i];
            insertStartParagraphMark(doc, builder, doc.getLength());
            builder.createSegmentElement(false, Core.getProject().getTranslationInfo(builder.ste));
            builder.addSegmentSeparator();
        }
        lastLoaded = loadTo;
        SegmentBuilder[] loaded = Arrays.copyOfRange(m_docSegList, loadFrom, loadTo + 1);
        markerController.process(loaded);
    };

    private synchronized void loadUp(int count) {
        if (firstLoaded <= 0 || firstLoaded >= m_docSegList.length) {
            return;
        }
        int loadFrom = firstLoaded - 1;
        int loadTo = Math.max(0, loadFrom - count + 1);
        for (int i = loadFrom; i >= loadTo; i--) {
            SegmentBuilder builder = m_docSegList[i];
            builder.prependSegmentSeparator();
            builder.prependSegmentElement(false, Core.getProject().getTranslationInfo(builder.ste));
            // We need to re-mark each segment immediately as it's added or else
            // the marks are placed incorrectly. This probably has to do with
            // offsets changing as content is prepended, but I (AMK) have not
            // properly investigated.
            markerController.reprocessImmediately(builder);
            insertStartParagraphMark(editor.getOmDocument(), builder, 0);
        }
        firstLoaded = loadTo;
    };

    private void updateState(SHOW_TYPE showType) {
        UIThreadsUtil.mustBeSwingThread();

        JComponent data = null;

        String updatedTitle = null;
        switch (showType) {
        case INTRO:
            data = introPane;
            updatedTitle = introPaneTitle;
            break;
        case EMPTY_PROJECT:
            data = emptyProjectPane;
            updatedTitle = emptyProjectPaneTitle;
            break;
        case FIRST_ENTRY:
            displayedFileIndex = 0;
            displayedEntryIndex = 0;
            updatedTitle = StringUtil.format(OStrings.getString("GUI_SUBWINDOWTITLE_Editor"), getCurrentFile());
            data = editor;
            SwingUtilities.invokeLater(() -> {
                // need to run later because some other event listeners
                // should be called before
                loadDocument();
                gotoEntry(LastSegmentManager.getLastSegmentNumber());
                updateTitleCurrentFile();
            });
            break;
        case NO_CHANGE:
            updatedTitle = StringUtil.format(OStrings.getString("GUI_SUBWINDOWTITLE_Editor"), getCurrentFile());
            data = editor;
            break;
        }

        updateTitle(updatedTitle);

        if (scrollPane.getViewport().getView() != data) {
            if (UIManager.getBoolean("OmegaTDockablePanel.isProportionalMargins")) {
                int size = data.getFont().getSize() / 2;
                data.setBorder(new EmptyBorder(size, size, size, size));
            }
            scrollPane.setViewportView(data);
        }
    }

    private final IDropInfo dropInfo = new IDropInfo() {

        @Override
        public DataFlavor getDataFlavor() {
            return DataFlavor.javaFileListFlavor;
        }

        @Override
        public int getDnDAction() {
            return DnDConstants.ACTION_COPY;
        }

        @Override
        public boolean handleDroppedObject(Object dropped) {
            final List<?> files = (List<?>) dropped;

            // Only look at first file to determine intent to open project
            File firstFile = (File) files.get(0);
            if (firstFile.getName().equals(OConsts.FILE_PROJECT)) {
                firstFile = firstFile.getParentFile();
            }
            if (StaticUtils.isProjectDir(firstFile)) {
                return handleDroppedProject(firstFile);
            }
            return handleDroppedFiles(files);
        }

        private boolean handleDroppedProject(final File projDir) {
            // Opening/closing might take a long time for team projects.
            // Invoke later so we can return successfully right away.
            SwingUtilities.invokeLater(() -> ProjectUICommands.projectOpen(projDir, true));
            return true;
        }

        private boolean handleDroppedFiles(final List<?> files) {
            if (!Core.getProject().isProjectLoaded()) {
                return false;
            }
            // The import might take a long time if there are collision dialogs.
            // Invoke later so we can return successfully right away.
            SwingUtilities.invokeLater(() -> ProjectUICommands.projectImportFiles(
                    Core.getProject().getProjectProperties().getSourceRoot(),
                    files.toArray(new File[files.size()])));
            return true;
        }

        @Override
        public Component getComponentToOverlay() {
            return scrollPane;
        }

        @Override
        public String getOverlayMessage() {
            return Core.getProject().isProjectLoaded() ? OStrings.getString("DND_ADD_SOURCE_FILE")
                    : OStrings.getString("DND_OPEN_PROJECT");
        }

        @Override
        public boolean canAcceptDrop() {
            return true;
        }
    };

    private void updateTitle() {
        pane.setName(StaticUIUtils.truncateToFit(title, pane, 70));
        pane.setToolTipText(title);
    }

    private void updateTitleCurrentFile() {
        updateTitle(StringUtil.format(OStrings.getString("GUI_SUBWINDOWTITLE_Editor"), getCurrentFile()));
    }

    private void updateTitle(String title) {
        this.title = title;
        updateTitle();
    }

    private void setFont(final Font font) {
        this.font = font;
        editor.setFont(font);
    }

    /**
     * Decide what document orientation should be default for source/target languages.
     */
    private void setInitialOrientation() {
        sourceLangIsRTL = BiDiUtils.isSourceLangRtl();
        targetLangIsRTL = BiDiUtils.isTargetLangRtl();
        currentOrientation = BiDiUtils.getOrientationType();
        // Define editor's orientation by target language orientation.
        editor.setComponentOrientation(BiDiUtils.getOrientation(currentOrientation));
    }

    /**
     * The orientation of the document is all LtR.
     * @return true when the orientation is all RtL. otherwise false.
     */
    public boolean isOrientationAllLtr() {
        return currentOrientation.equals(BiDiUtils.ORIENTATION.ALL_LTR);
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
        SegmentBuilder builder = getCurrentSegmentBuilder();
        return builder == null ? null : builder.ste;
    }

    public SegmentBuilder getCurrentSegmentBuilder() {
        if (m_docSegList == null || displayedEntryIndex < 0 || m_docSegList.length <= displayedEntryIndex) {
            // there is no current entry
            return null;
        }
        return m_docSegList[displayedEntryIndex];
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentFile() {
        IProject proj = Core.getProject();
        if (proj == null || !proj.isProjectLoaded()) {
            return null;
        }
        if (proj.getProjectFiles().isEmpty()) {
            // there is no files yet
            return null;
        }

        if (displayedFileIndex < proj.getProjectFiles().size()) {
            return proj.getProjectFiles().get(displayedFileIndex).filePath;
        } else {
            return null;
        }
    }

    @Override
    public String getCurrentTargetFile() {
        String currentSource = getCurrentFile();
        if (currentSource == null) {
            return null;
        }
        return Core.getProject().getTargetPathForSourceFile(currentSource);
    }

    /**
     * Displays the {@link Preferences#EDITOR_INITIAL_SEGMENT_LOAD_COUNT}
     * segments surrounding the entry with index {@link #displayedEntryIndex}.
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
        boolean hasRTL = sourceLangIsRTL || targetLangIsRTL || BiDiUtils.isLocaleRtl()
                || currentOrientation != BiDiUtils.ORIENTATION.ALL_LTR;
        Map<Language, ProjectTMX> otherLanguageTMs = Core.getProject().getOtherTargetLanguageTMs();
        for (Map.Entry<Language, ProjectTMX> entry : otherLanguageTMs.entrySet()) {
            hasRTL = hasRTL || BiDiUtils.isRtl(entry.getKey().getLanguageCode().toLowerCase(Locale.ENGLISH));
        }

        Document3 doc = new Document3(this);

        // Create all SegmentBuilders now...
        ArrayList<SegmentBuilder> tmpSegList = new ArrayList<>(file.entries.size());
        for (SourceTextEntry ste : file.entries) {
            if (entriesFilter == null || entriesFilter.allowed(ste)) {
                SegmentBuilder sb = new SegmentBuilder(this, doc, settings, ste, ste.entryNum(), hasRTL);
                tmpSegList.add(sb);
            }
        }
        m_docSegList = tmpSegList.toArray(new SegmentBuilder[tmpSegList.size()]);

        // Clamp displayedSegment to actually available entries.
        displayedEntryIndex = Math.max(0, Math.min(m_docSegList.length - 1, displayedEntryIndex));
        // Calculate start, end indices of a span of initialSegCount segments
        // centered around displayedEntryIndex and clamped to [0, m_docSegList.length).
        final int initialSegCount = Preferences.getPreferenceDefault(Preferences.EDITOR_INITIAL_SEGMENT_LOAD_COUNT,
                Preferences.EDITOR_INITIAL_SEGMENT_LOAD_COUNT_DEFAULT);
        firstLoaded = Math.max(0, displayedEntryIndex - initialSegCount / 2);
        lastLoaded = Math.min(file.entries.size() - 1, firstLoaded + initialSegCount - 1);

        // ...but only display the ones in [firstLoaded, lastLoaded]
        for (int i = 0; i < m_docSegList.length; i++) {
            if (i >= firstLoaded && i <= lastLoaded) {
                SegmentBuilder sb = m_docSegList[i];
                insertStartParagraphMark(doc, sb, doc.getLength());
                sb.createSegmentElement(false, Core.getProject().getTranslationInfo(sb.ste));
                sb.addSegmentSeparator();
            }
        }

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

    private void insertStartParagraphMark(Document3 doc, SegmentBuilder sb, int startOffset) {
        if (Preferences.isPreferenceDefault(Preferences.MARK_PARA_DELIMITATIONS, false)) {
            if (sb.getSourceTextEntry().isParagraphStart()) {
                doc.trustedChangesInProgress = true;
                StaticUIUtils.setCaretUpdateEnabled(editor, false);
                try {
                    doc.insertString(startOffset, Preferences.getPreferenceDefault(
                            Preferences.MARK_PARA_TEXT, Preferences.MARK_PARA_TEXT_DEFAULT) + "\n\n",
                            settings.getParagraphStartAttributeSet());
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    doc.trustedChangesInProgress = false;
                    StaticUIUtils.setCaretUpdateEnabled(editor, true);
                }
            }
        }
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

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        SegmentBuilder builder = m_docSegList[displayedEntryIndex];

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

        Core.getNotes().setNoteText(currentTranslation.note);

        // then add new marks
        markerController.reprocessImmediately(builder);

        editor.undoManager.reset();

        history.insertNew(builder.segmentNumberInProject);

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

        editor.autoCompleter.setVisible(false);
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
            mw.showLengthMessage(lMsg);
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
            m_docSegList[displayedEntryIndex].onActiveEntryChanged();

            SwingUtilities.invokeLater(() -> {
                markerController.reprocessImmediately(m_docSegList[displayedEntryIndex]);
                editor.autoCompleter.textDidChange();
            });
        }
    }

    /**
     * Attempt to center the active segment in the editor. When the active
     * segment is taller than the editor, the first line of the editable area
     * will be at the bottom of the editor.
     */
    private void scrollForDisplayNearestSegments(final CaretPosition pos) {
        SwingUtilities.invokeLater(() -> {
            Rectangle rect = getSegmentBounds(displayedEntryIndex);
            if (rect != null) {
                // Expand rect vertically to fill height of viewport.
                int viewportHeight = scrollPane.getViewport().getHeight();
                rect.y -= (viewportHeight - rect.height) / 2;
                rect.height = viewportHeight;
                editor.scrollRectToVisible(rect);
            }
            setCaretPosition(pos);
        });
    }

    /**
     * Get a rectangle that bounds the specified segment in editor-space pixel
     * coordinates. Returns null if the specified segment index is invalid or if
     * the segment has not been loaded yet.
     */
    private Rectangle getSegmentBounds(int index) {
        if (index < 0 || index >= m_docSegList.length) {
            return null;
        }
        try {
            SegmentBuilder sb = m_docSegList[index];
            if (sb.hasBeenCreated()) {
                Rectangle2D start = editor.modelToView2D(sb.getStartPosition());
                Rectangle2D end = editor.modelToView2D(sb.getEndPosition());
                if (start != null && end != null) {
                    Rectangle2D.union(start, end, start);
                    return start.getBounds();
                }
            }
        } catch (BadLocationException ex) {
            Log.log(ex);
        }
        return null;
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

        final MainWindowUI.StatusBarMode progressMode =
                Preferences.getPreferenceEnumDefault(Preferences.SB_PROGRESS_MODE,
                        MainWindowUI.StatusBarMode.DEFAULT);

        if (progressMode == MainWindowUI.StatusBarMode.DEFAULT) {
            StringBuilder pMsg = new StringBuilder(1024).append(" ");
            pMsg.append(translatedInFile).append("/").append(fi.entries.size()).append(" (")
                    .append(stat.numberOfTranslatedSegments).append("/").append(stat.numberOfUniqueSegments)
                    .append(", ").append(stat.numberOfSegmentsTotal).append(") ");
            mw.showProgressMessage(pMsg.toString());
        } else {
            /*
             * Percentage mode based on idea by Yu Tang
             * http://dirtysexyquery.blogspot.tw/2013/03/omegat-custom-progress-format.html
             */
            java.text.NumberFormat nfPer = java.text.NumberFormat.getPercentInstance();
            nfPer.setRoundingMode(java.math.RoundingMode.DOWN);
            nfPer.setMaximumFractionDigits(1);

            String message = StringUtil.format(OStrings.getString("MW_PROGRESS_DEFAULT_PERCENTAGE"),
                    (translatedUniqueInFile == 0) ? "0%" : nfPer.format((double) translatedUniqueInFile / uniqueInFile),
                    uniqueInFile - translatedUniqueInFile,
                    (stat.numberOfTranslatedSegments == 0) ? "0%"
                            : nfPer.format((double) stat.numberOfTranslatedSegments / stat.numberOfUniqueSegments),
                    stat.numberOfUniqueSegments - stat.numberOfTranslatedSegments, stat.numberOfSegmentsTotal);

            mw.showProgressMessage(message);
        }
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
        if (segmentAtLocation < 0) {
            return false;
        }
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
        if (m_docSegList == null) {
            return -1;
        }
        for (int i = 0; i < m_docSegList.length; i++) {
            SegmentBuilder builder = m_docSegList[i];
            // Treat start and end positions inclusively to give
            // intuitive results when double-clicking to jump in editor
            if (builder.hasBeenCreated() && location >= builder.getStartPosition()
                    && location <= builder.getEndPosition()) {
                return i;
            }
        }
        return m_docSegList.length - 1;
    }

    /**
     * Refresh some entries. Usually after external translation changes replacement.
     */
    public void refreshEntries(Set<Integer> entryNumbers) {
        for (int i = 0; i < m_docSegList.length; i++) {
            if (entryNumbers.contains(m_docSegList[i].ste.entryNum())) {
                // the same source text - need to update
                m_docSegList[i].createSegmentElement(false, Core.getProject().getTranslationInfo(m_docSegList[i].ste));
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

        // remove internal bidi chars
        String transWithControlChars = doc.extractTranslation();
        String newTrans = EditorUtils.removeDirectionCharsAroundTags(transWithControlChars, getCurrentEntry());
        if (newTrans != null) {
            commitAndDeactivate(null, newTrans);
        }
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
        newen.source = sb.ste.getSrcText();
        newen.note = Core.getNotes().getNoteText();
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
            if (newTrans.isEmpty()) { // empty translation
                if (oldTE.isTranslated() && "".equals(oldTE.translation)) {
                    // It's an empty translation which should remain empty
                    newen.translation = "";
                } else {
                    newen.translation = null; // will be untranslated
                }
            } else if (newTrans.equals(newen.source)) { // equals to source
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
                if (currentEntryOrigin != null && newTrans.equals(translationFromOrigin)) {
                    if (newen.otherProperties == null) {
                        newen.otherProperties = new ArrayList<>();
                    }
                    newen.otherProperties.add(new TMXProp(ProjectTMX.PROP_ORIGIN, currentEntryOrigin));
                }
            }
        }

        boolean defaultTranslation = sb.isDefaultTranslation();
        boolean isNewDefaultTrans = defaultTranslation && !oldTE.defaultTranslation;
        boolean isNewAltTrans = !defaultTranslation && oldTE.defaultTranslation;
        boolean translationChanged = !Objects.equals(oldTE.translation, newen.translation);
        boolean noteChanged = !StringUtil.nvl(oldTE.note, "").equals(StringUtil.nvl(newen.note, ""));
        resetOrigin();

        if (!isNewAltTrans && !translationChanged && noteChanged) {
            // Only note was changed, and we are not making a new alt translation.
            Core.getProject().setNote(entry, oldTE, newen.note);
        } else if (isNewDefaultTrans || translationChanged || noteChanged) {
            while (true) {
                // iterate before optimistic locking will be resolved
                try {
                    Core.getProject().setTranslation(entry, newen, defaultTranslation, null,
                            previousTranslations);
                    break;
                } catch (OptimisticLockingFail ex) {
                    String result = new ConflictDialogController().show(ex.getOldTranslationText(),
                            ex.getNewTranslationText(), newen.translation);
                    if (result == newen.translation) {
                        // next iteration
                        previousTranslations = ex.getPrevious();
                    } else {
                        // use remote - don't save user's translation
                        break;
                    }
                }
            }
        }

        m_docSegList[displayedEntryIndex].createSegmentElement(false,
                Core.getProject().getTranslationInfo(m_docSegList[displayedEntryIndex].ste), defaultTranslation);

        // find all identical sources and redraw them
        for (int i = 0; i < m_docSegList.length; i++) {
            if (i == displayedEntryIndex) {
                // current entry, skip
                continue;
            }
            SegmentBuilder builder = m_docSegList[i];
            if (!builder.hasBeenCreated()) {
                // Skip because segment has not been drawn yet
                continue;
            }
            if (builder.ste.getSrcText().equals(entry.getSrcText())) {
                // the same source text - need to update
                builder.createSegmentElement(false,
                        Core.getProject().getTranslationInfo(builder.ste), !defaultTranslation);
                // then add new marks
                markerController.reprocessImmediately(builder);
            }
        }

        Core.getNotes().clear();

        // then add new marks
        markerController.reprocessImmediately(m_docSegList[displayedEntryIndex]);

        editor.undoManager.reset();

        // validate tags if required
        if (entry != null && Preferences.isPreference(Preferences.TAG_VALIDATE_ON_LEAVE)) {
            String file = getCurrentFile();
            new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() throws Exception {
                    return Core.getTagValidation().checkInvalidTags(entry);
                }

                @Override
                protected void done() {
                    try {
                        if (!get()) {
                            Core.getIssues().showForFiles(Pattern.quote(file), entry.entryNum());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        LOGGER.log(Level.SEVERE, "Exception when validating tags on leave", e);
                    }
                }
            }.execute();
        }

        // team sync for save thread
        if (Core.getProject().isTeamSyncPrepared()) {
            try {
                Core.executeExclusively(false, Core.getProject()::teamSync);
            } catch (InterruptedException | TimeoutException ex) {
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
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
        resetOrigin();
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

    private void iterateToEntry(boolean forward, Predicate<SourceTextEntry> shouldStop) {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = editor.getCursor();
        editor.setCursor(hourglassCursor);

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
            if (forward) {
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
            } else {
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
            }
            ste = getCurrentEntry();
            if (ste != null && shouldStop.test(ste)) {
                break;
            }
            if (looped && displayedFileIndex == startFileIndex) {
                if (forward && displayedEntryIndex >= startEntryIndex) {
                    // We have looped forward to our starting point
                    break;
                } else if (!forward && displayedEntryIndex <= startEntryIndex) {
                    // We have looped backwards to our starting point
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
        editor.setCursor(oldCursor);
    }

    private void anyEntry(boolean forwards) {
        iterateToEntry(forwards, ste -> true);
    }

    public void nextEntry() {
        anyEntry(true);
    }

    public void prevEntry() {
        anyEntry(false);
    }

    /**
     * Find the next (un)translated entry.
     * @param findTranslated should the next entry be translated or not.
     */
    private void nextTranslatedEntry(final boolean findTranslated) {
        iterateToEntry(true, ste -> {
            boolean isTranslated = Core.getProject().getTranslationInfo(ste).isTranslated();
            if (findTranslated && isTranslated) {
                return true; // translated
            }
            if (!findTranslated && !isTranslated) {
                return true; // non-translated
            }
            if (Preferences.isPreference(Preferences.STOP_ON_ALTERNATIVE_TRANSLATION)) {
                // when there is at least one alternative translation, then
                // we can consider that segment is not translated
                HasMultipleTranslations checker = new HasMultipleTranslations(ste.getSrcText());
                Core.getProject().iterateByMultipleTranslations(checker);
                if (checker.found) {
                    // stop - alternative translation exist
                    return true;
                }
            }
            return false;
        });
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

    private void linkedEntry(boolean forward, String linked) {
        iterateToEntry(forward, ste -> {
                TMXEntry info = Core.getProject().getTranslationInfo(ste);
                return String.valueOf(info.linked).equals(linked);
            });
    }

    /**
     * Finds the next/previous x-auto translated entry
     */
    public void nextXAutoEntry() {
        linkedEntry(true, "xAUTO");
    }
    public void prevXAutoEntry() {
        linkedEntry(false, "xAUTO");
    }

    /**
     * Finds the next/previous x-enforced translated entry
     */
    public void nextXEnforcedEntry() {
        linkedEntry(true, "xENFORCED");
    }
    public void prevXEnforcedEntry() {
        linkedEntry(false, "xENFORCED");
    }

    private void entryWithNote(boolean forward) {
        iterateToEntry(forward, ste -> Core.getProject().getTranslationInfo(ste).hasNote());
    }

    /**
     * Finds the next entry with a non-empty note.
     */
    public void nextEntryWithNote() {
        entryWithNote(true);
    }

    /**
     * Finds the previous entry with a non-empty note.
     */
    public void prevEntryWithNote() {
        entryWithNote(false);
    }

    /**
     * Find the next unique entry.
     */
    public void nextUniqueEntry() {
        iterateToEntry(true, ste -> ste.getDuplicate() != DUPLICATE.NEXT);
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

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        if (m_docSegList == null) {
            // document didn't loaded yet
            return;
        }

        if (fileIndex < 0 || fileIndex >= Core.getProject().getProjectFiles().size()) {
            throw new IndexOutOfBoundsException();
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

        if (!Core.getProject().isProjectLoaded()) {
            return;
        }

        if (m_docSegList == null) {
            // document didn't loaded yet
            return;
        }
        Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = editor.getCursor();
        editor.setCursor(hourglassCursor);
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
        editor.setCursor(oldCursor);
        updateTitleCurrentFile();
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
    @Override
    public void changeCase(CHANGE_CASE_TO toWhat) {
        UIThreadsUtil.mustBeSwingThread();

        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();

        int caretPosition = editor.getCaretPosition();

        int translationStart = editor.getOmDocument().getTranslationStart();
        int translationEnd = editor.getOmDocument().getTranslationEnd();

        // both should be within the limits
        if (end < translationStart || start > translationEnd) {
            return; // forget it, not worth the effort
        }

        // adjust the bound which exceeds the limits
        if (start < translationStart && end <= translationEnd) {
            start = translationStart;
        }

        if (end > translationEnd && start >= translationStart) {
            end = translationEnd;
        }

        try {
            // no selection? make it the current word
            if (start == end) {
                start = EditorUtils.getWordStart(editor, start);
                end = EditorUtils.getWordEnd(editor, end);

                // adjust the bound again
                if (start < translationStart && end <= translationEnd) {
                    start = translationStart;
                }

                if (end > translationEnd && start >= translationStart) {
                    end = translationEnd;
                }
            }

            editor.setSelectionStart(start);
            editor.setSelectionEnd(end);

            String selectionText = editor.getText(start, end - start);
            String result = EditorUtils.doChangeCase(selectionText, toWhat);
            if (selectionText.equals(result)) {
                // Nothing changed
                return;
            }

            // ok, write it back to the editor document
            editor.replaceSelection(result);

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
     * {@inheritDoc}
     */
    @Override
    public void replaceEditText(final String text) {
        replaceEditText(text, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceEditText(String text, final String origin) {
        UIThreadsUtil.mustBeSwingThread();

        setOrigin(text, origin);
        SegmentBuilder builder = m_docSegList[displayedEntryIndex];
        if (builder.hasRTL && targetLangIsRTL) {
            text = EditorUtils.addBidiAroundTags(EditorUtils.removeDirectionCharsAroundTags(text, builder.ste),
                    builder.ste);
        }

        // build local offsets
        int start = editor.getOmDocument().getTranslationStart();
        int end = editor.getOmDocument().getTranslationEnd();

        CalcMarkersThread thread = markerController.markerThreads[markerController
                .getMarkerIndex(ComesFromMTMarker.class.getName())];
        ((ComesFromMTMarker) thread.marker).setMark(null, null);

        // remove text
        editor.select(start, end);
        editor.replaceSelection(text);
    }

    public void replacePartOfText(final String text, int start, int end) {
        UIThreadsUtil.mustBeSwingThread();

        resetOrigin();
        CalcMarkersThread thread = markerController.markerThreads[markerController
                .getMarkerIndex(ComesFromMTMarker.class.getName())];
        ((ComesFromMTMarker) thread.marker).setMark(null, null);

        int off = editor.getOmDocument().getTranslationStart();
        // remove text
        editor.select(start + off, end + off);
        editor.replaceSelection(text);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceEditTextAndMark(final String text, final String origin) {
        replaceEditText(text, origin);
        markAsComesFromMT(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceEditTextAndMark(String text) {
        replaceEditTextAndMark(text, null);
    }

    private void markAsComesFromMT(String text) {
        SegmentBuilder sb = m_docSegList[displayedEntryIndex];
        CalcMarkersThread thread = markerController.markerThreads[markerController
                .getMarkerIndex(ComesFromMTMarker.class.getName())];
        ((ComesFromMTMarker) thread.marker).setMark(sb.getSourceTextEntry(), text);
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
    public void insertText(String text) {
        UIThreadsUtil.mustBeSwingThread();

        editor.checkAndFixCaret();
        resetOrigin();
        SegmentBuilder builder = m_docSegList[displayedEntryIndex];
        if (builder.hasRTL && targetLangIsRTL) {
            text = EditorUtils.addBidiAroundTags(EditorUtils.removeDirectionCharsAroundTags(text, builder.ste),
                    builder.ste);
        }
        editor.replaceSelection(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertTextAndMark(String text) {
        insertText(text);
        markAsComesFromMT(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertTag(final String tag) {
        UIThreadsUtil.mustBeSwingThread();

        editor.checkAndFixCaret();

        SegmentBuilder builder = m_docSegList[displayedEntryIndex];
        if (builder.hasRTL && targetLangIsRTL) {
            // add control bidi chars around
            String t = SegmentBuilder.BIDI_RLM + SegmentBuilder.BIDI_LRM + tag + SegmentBuilder.BIDI_LRM
                    + SegmentBuilder.BIDI_RLM;
            editor.replaceSelection(t);
        } else {
            // just insert tag
            editor.replaceSelection(tag);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectSourceText() {
        UIThreadsUtil.mustBeSwingThread();

        SourceTextEntry ste = getCurrentEntry();
        int sourceTextSize = ste.getSrcText().length();
        int end = editor.getOmDocument().getTranslationStart() - 1;
        int start = end - sourceTextSize;
        editor.setSelectionStart(start);
        editor.setSelectionEnd(end);
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

    private void setOrigin(final String text, final String origin) {
        if (origin != null) {
            currentEntryOrigin = origin;
            translationFromOrigin = text;
        } else {
            resetOrigin();
        }
    }

    private void resetOrigin() {
        currentEntryOrigin = null;
        translationFromOrigin = null;
    }

    /** Loads First Steps article */
    private void createAdditionalPanes() {
        introPaneTitle = OStrings.getString("DOCKING_FIRST_STEPS_TITLE");
        try {
            String language = detectFirstStepsLanguage();
            introPane = new JTextPane();
            introPane
                    .setComponentOrientation(BiDiUtils.isRtl(language) ? ComponentOrientation.RIGHT_TO_LEFT
                            : ComponentOrientation.LEFT_TO_RIGHT);
            introPane.setEditable(false);
            DragTargetOverlay.apply(introPane, dropInfo);
            URI uri = Help.getHelpFileURI(language, OConsts.HELP_FIRST_STEPS);
            if (uri != null) {
                introPane.setPage(uri.toURL());
            }
        } catch (IOException e) {
        }

        emptyProjectPaneTitle = OStrings.getString("TF_INTRO_EMPTYPROJECT_FILENAME");
        emptyProjectPane = new JTextPane();
        emptyProjectPane.setEditable(false);
        emptyProjectPane.setText(OStrings.getString("TF_INTRO_EMPTYPROJECT"));
        emptyProjectPane.setFont(mw.getApplicationFont());
        DragTargetOverlay.apply(emptyProjectPane, dropInfo);
    }

    /**
     * Detects the language of the first steps guide (checks if present in default locale's language).
     *
     * If there is no first steps guide in the default locale's language, "en" (English) is returned,
     * otherwise the acronym for the default locale's language.
     */
    private String detectFirstStepsLanguage() {
        // Get the system language and country
        String language = Language.getLowerCaseLanguageFromLocale();
        String country = Language.getUpperCaseCountryFromLocale();

        // Check if there's a translation for the full locale (lang + country)
        if (Help.getHelpFileURI(language + "_" + country, OConsts.HELP_FIRST_STEPS) != null) {
            return language + "_" + country;
        }

        // Check if there's a translation for the language only
        if (Help.getHelpFileURI(language, OConsts.HELP_FIRST_STEPS) != null) {
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
     * {@inheritDoc} Document is reloaded to immediately have the filter being
     * effective.
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
        // Prevent NullPointerErrors in loadDocument. Only load if there is a document.
        if (doc != null && project != null && project.getProjectFiles() != null && curEntry != null) {
            int curEntryNum = curEntry.entryNum();
            loadDocument(); // rebuild entrylist
            if (entriesFilter == null || entriesFilter.allowed(curEntry)) {
                gotoEntry(curEntry.entryNum());
            } else {
                // Go to next (available) segment. But first, we need to reset
                // the displayedEntryIndex to the number where the current but
                // filtered entry could have been if it was not filtered.
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
     * {@inheritDoc} Document is reloaded if appropriate to immediately remove
     * the filter;
     */
    public void removeFilter() {
        UIThreadsUtil.mustBeSwingThread();

        if (entriesFilter == null && entriesFilterControlComponent == null) {
            return;
        }

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
     * {@inheritDoc}
     */
    public void setAlternateTranslationForCurrentEntry(boolean alternate) {
        SegmentBuilder sb = m_docSegList[displayedEntryIndex];

        if (!alternate) {
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
        editor.autoCompleter.setVisible(false);
    }

    /**
     * Class for checking if alternative translation exist.
     */
    protected static class HasMultipleTranslations implements IProject.MultipleTranslationsIterator {
        final String sourceEntryText;
        boolean found;

        public HasMultipleTranslations(String sourceEntryText) {
            this.sourceEntryText = sourceEntryText;
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

    @SuppressWarnings("serial")
    public AlphabeticalMarkers getAlphabeticalMarkers() {
        return new AlphabeticalMarkers(scrollPane) {
            static final int UPPER_GAP = 5;

            @Override
            protected Map<Integer, Point> getViewableSegmentLocations() {
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
                    if (!sb.hasBeenCreated()) {
                        continue;
                    }
                    try {
                        Point location =
                                editor.modelToView2D(sb.getStartPosition()).getBounds().getLocation();
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

    @Override
    public IAutoCompleter getAutoCompleter() {
        return editor.autoCompleter;
    }
}
