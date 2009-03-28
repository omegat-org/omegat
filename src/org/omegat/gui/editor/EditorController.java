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
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringEntry;
import org.omegat.core.data.stat.StatisticsInfo;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.editor.OmDocument.ORIENTATION;
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

/**
 * Class for control all editor operations.
 * 
 * We had to change standard editor implementation for better UI control. So, we
 * have to implement own Document, DocumentFilter, Content, Elements.
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
    private static final Logger LOGGER = Logger
            .getLogger(EditorController.class.getName());

    private final DockableScrollPane pane;
    protected final OmTextArea editor;
    private String introPaneTitle, emptyProjectPaneTitle;
    private JTextPane introPane, emptyProjectPane;
    protected final MainWindow mw;

    /** Currently displayed segments info. */
    protected OmDocument.OmElementSegment[] m_docSegList;

    /** Current displayed file. */
    protected int displayedFileIndex, previousDisplayedFileIndex;
    /** Current active segment in current file. */
    protected int displayedEntryIndex;

    /** Object which store history of moving by segments. */
    private SegmentHistory history = new SegmentHistory();

    protected final EditorSettings settings;

    protected final SpellCheckerThread spellCheckerThread;

    private enum SHOW_TYPE {
        INTRO, EMPTY_PROJECT, FIRST_ENTRY, NO_CHANGE
    };

    private OmDocument.ORIENTATION currentOrientation;
    protected boolean sourceLangIsRTL, targetLangIsRTL;

    public EditorController(final MainWindow mainWindow) {
        this.mw = mainWindow;

        editor = new OmTextArea(this);
        editor.setFont(Core.getMainWindow().getApplicationFont());

        pane = new DockableScrollPane("EDITOR", " ", editor, false);
        pane.setComponentOrientation(ComponentOrientation.getOrientation(Locale
                .getDefault()));
        pane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setMinimumSize(new Dimension(100, 100));

        Core.getMainWindow().addDockable(pane);

        // editor.controller = this;

        settings = new EditorSettings(this);

        spellCheckerThread = new SpellCheckerThread();
        spellCheckerThread.start();

        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                spellCheckerThread.resetCache();
                
                SHOW_TYPE showType;
                switch (eventType) {
                case CREATE:
                case LOAD:
                    history.clear();
                    if (!Core.getProject().getAllEntries().isEmpty()) {
                        showType = SHOW_TYPE.FIRST_ENTRY;
                    } else {
                        showType = SHOW_TYPE.EMPTY_PROJECT;
                    }
                    setInitialOrientation();
                    break;
                case CLOSE:
                    history.clear();
                    showType = SHOW_TYPE.INTRO;
                    break;
                default:
                    showType = SHOW_TYPE.NO_CHANGE;
                }
                if (showType != SHOW_TYPE.NO_CHANGE) {
                    updateState(showType);
                }
            }
        });
        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            public void onNewFile(String activeFileName) {
                spellCheckerThread.resetCache();
                
                updateState(SHOW_TYPE.NO_CHANGE);
            }

            public void onEntryActivated(StringEntry newEntry) {
            }
        });

        createAdditionalPanes();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateState(SHOW_TYPE.INTRO);
                pane.requestFocus();
            }
        });

        CoreEvents
                .registerFontChangedEventListener(new IFontChangedEventListener() {
                    public void onFontChanged(Font newFont) {
                        // fonts have changed
                        if (m_docSegList != null) {
                            // segments displayed
                            OmDocument doc = editor.getOmDocument();
                            if (doc != null) {
                                doc.setFont(newFont);
                            }
                        }
                        editor.setFont(newFont);
                        emptyProjectPane.setFont(newFont);
                    }
                });

        Thread
                .setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread t, Throwable e) {
                        LOGGER.log(Level.SEVERE,
                                "Uncatched exception in thread [" + t.getName()
                                        + "]", e);
                    }
                });
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
            title = StaticUtils.format(OStrings
                    .getString("GUI_SUBWINDOWTITLE_Editor"), getCurrentFile());
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
            title = StaticUtils.format(OStrings
                    .getString("GUI_SUBWINDOWTITLE_Editor"), getCurrentFile());
            data = editor;
            break;
        }

        pane.setName(title);
        if (pane.getViewport().getView() != data) {
            pane.setViewportView(data);
        }
    }

    /**
     * Decide what document orientation should be default for source/target
     * languages.
     */
    private void setInitialOrientation() {
        String sourceLang = Core.getProject().getProjectProperties()
                .getSourceLanguage().getLanguageCode();
        String targetLang = Core.getProject().getProjectProperties()
                .getTargetLanguage().getLanguageCode();

        sourceLangIsRTL = EditorUtils.isRTL(sourceLang);
        targetLangIsRTL = EditorUtils.isRTL(targetLang);

        if (sourceLangIsRTL != targetLangIsRTL) {
            currentOrientation = OmDocument.ORIENTATION.DIFFER;
        } else {
            if (sourceLangIsRTL) {
                currentOrientation = ORIENTATION.RTL;
            } else {
                currentOrientation = ORIENTATION.LTR;
            }
        }
    }

    /**
     * Toggle component orientation: LTR, RTL, language dependent.
     */
    protected void toggleOrientation() {
        ORIENTATION newOrientation = currentOrientation;
        switch (currentOrientation) {
        case LTR:
            newOrientation = ORIENTATION.RTL;
            break;
        case RTL:
            if (sourceLangIsRTL != targetLangIsRTL) {
                newOrientation = ORIENTATION.DIFFER;
            } else {
                newOrientation = ORIENTATION.LTR;
            }
            break;
        case DIFFER:
            newOrientation = ORIENTATION.LTR;
            break;
        }
        LOGGER.info("Switch document orientation from " + currentOrientation
                + " to " + newOrientation);
        currentOrientation = newOrientation;
        editor.getOmDocument().setOrientation(currentOrientation);
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
     * Displays translation for each segment if it's available, otherwise
     * displays source text. Also stores length of each displayed segment plus
     * its starting offset.
     */
    protected void loadDocument() {
        UIThreadsUtil.mustBeSwingThread();

        // Currently displayed file
        IProject.FileInfo file = Core.getProject().getProjectFiles().get(
                displayedFileIndex);

        OmDocument doc = new OmDocument(this);

        List<SourceTextEntry> entries = Core.getProject().getAllEntries();
        SegmentElementsDescription[] descriptions = new SegmentElementsDescription[file.size];
        for (int i = 0; i < descriptions.length; i++) {
            SourceTextEntry ste = entries.get(file.firstEntryIndexInGlobalList
                    + i);
            descriptions[i] = new SegmentElementsDescription(doc, ste,
                    getEntryNumber(i));
        }

        try {
            m_docSegList = doc.initialize(descriptions, currentOrientation);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, "Error initialize document", ex);
        }
        doc.setDocumentFilter(new OmDocumentFilter());

        editor.setDocument(doc);
        doc.setFont(Core.getMainWindow().getApplicationFont());

        doc.addUndoableEditListener(editor.undoManager);
        editor.repaint();
    }

    /**
     * Activates the current entry by displaying source text and embedding
     * displayed text in markers.
     * <p>
     * Also moves document focus to current entry, and makes sure fuzzy info
     * displayed if available.
     */
    public void activateEntry() {
        UIThreadsUtil.mustBeSwingThread();

        if (pane.getViewport().getView() != editor) {
            // editor not displayed
            return;
        }

        if (!Core.getProject().isProjectLoaded())
            return;

        OmDocument doc = editor.getOmDocument();
        doc.replaceSegment(displayedEntryIndex, true);

        editor.cancelUndo();

        history
                .insertNew(m_docSegList[displayedEntryIndex].segmentNumberInProject);
        // update history menu items
        mw.menu.gotoHistoryBackMenuItem.setEnabled(history.hasPrev());
        mw.menu.gotoHistoryForwardMenuItem.setEnabled(history.hasNext());

        showStat();

        // Show info about text length in status bar
        SourceTextEntry ste = m_docSegList[displayedEntryIndex].ste;
        String lMsg = " " + Integer.toString(ste.getSrcText().length()) + "/"
                + Integer.toString(ste.getTranslation().length()) + " ";
        Core.getMainWindow().showLengthMessage(lMsg);

        if (Preferences.isPreference(Preferences.EXPORT_CURRENT_SEGMENT)) {
            exportCurrentSegment(ste);
        }
        
        scrollForDisplayNearestSegments(doc.activeTranslationBegin.getOffset() + 1);        
        

        // check if file was changed
        if (previousDisplayedFileIndex != displayedFileIndex) {
            previousDisplayedFileIndex = displayedFileIndex;
            CoreEvents.fireEntryNewFile(Core.getProject().getProjectFiles()
                    .get(displayedFileIndex).filePath);
        }

        // fire event about new segment activated
        CoreEvents.fireEntryActivated(ste.getStrEntry());
    }
    
    /**
     * Display some segments before and after when user on the top or bottom of
     * page.
     */
    private void scrollForDisplayNearestSegments(final int requiredPosition) {
        int lookNext, lookPrev;
        try {
            OmDocument.OmElementSegment prev = m_docSegList[displayedEntryIndex - 3];
            lookPrev = prev.getStartOffset();
        } catch (IndexOutOfBoundsException ex) {
            lookPrev = 0;
        }
        try {
            OmDocument.OmElementSegment next = m_docSegList[displayedEntryIndex + 4];
            lookNext = next.getStartOffset() - 1;
        } catch (IndexOutOfBoundsException ex) {
            lookNext = editor.getOmDocument().getLength();
        }

        final int p = lookPrev;
        final int n = lookNext;
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
                                        editor
                                                .setCaretPosition(requiredPosition);
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
    private void exportCurrentSegment(final SourceTextEntry ste){
        String s1 = ste.getSrcText();
        String s2 = ste.getTranslation();

        FileUtil.writeScriptFile(s1, OConsts.SOURCE_EXPORT);                                   // NOI18N
        FileUtil.writeScriptFile(s2, OConsts.TARGET_EXPORT);                                   // NOI18N
    }

    /**
     * Calculate statistic for file, request statistic for project and display
     * in status bar.
     */
    private void showStat() {
        IProject project = Core.getProject();
        IProject.FileInfo fi = project.getProjectFiles()
                .get(displayedFileIndex);
        int translatedInFile = 0;
        for (int i = 0; i < fi.size; i++) {
            if (project.getAllEntries().get(i + fi.firstEntryIndexInGlobalList)
                    .isTranslated())
                translatedInFile++;
        }

        StatisticsInfo stat = project.getStatistics();

        String pMsg = " " + Integer.toString(translatedInFile) + "/"
                + Integer.toString(fi.size) + " ("
                + Integer.toString(stat.numberofTranslatedSegments) + "/"
                + Integer.toString(stat.numberOfUniqueSegments) + ", "
                + Integer.toString(stat.numberOfSegmentsTotal) + ") ";
        Core.getMainWindow().showProgressMessage(pMsg);
    }

    protected void goToSegmentAtLocation(int location) {
        // clicked segment
        int segmentAtLocation = editor.getOmDocument().getSegmentAtLocation(
                location);
        if (displayedEntryIndex != segmentAtLocation) {
            commitAndDeactivate();
            displayedEntryIndex = segmentAtLocation;
            activateEntry();
        }
    }

    /**
     * Commits the translation. Reads current entry text and commit it to memory
     * if it's changed. Also clears out segment markers while we're at it.
     * <p>
     * Since 1.6: Translation equal to source may be validated as OK translation
     * if appropriate option is set in Workflow options dialog.
     * <p>
     * All displayed segments with the same source text updated also.
     * 
     * @param forceCommit
     *            If false, the translation will not be saved
     */
    public void commitAndDeactivate() {
        UIThreadsUtil.mustBeSwingThread();

        OmDocument doc = editor.getOmDocument();

        if (doc == null) {
            // there is no active doc, it's empty project
            return;
        }
        
        // try {
        String newTrans = doc.extractTranslation();
        if (newTrans != null) {
            // segment was active
            SourceTextEntry entry = m_docSegList[displayedEntryIndex].ste;

            String old_translation = entry.getTranslation();
            // update memory
            if (newTrans.equals(entry.getSrcText())
                    && !Preferences
                            .isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC))
                Core.getProject().setTranslation(entry, "");
            else
                Core.getProject().setTranslation(entry, newTrans);

            doc.replaceSegment(displayedEntryIndex, false);

            if (!entry.getTranslation().equals(old_translation)) {
                // find all identical strings and redraw them

                for (int i = 0; i < m_docSegList.length; i++) {
                    if (i == displayedEntryIndex) {
                        // commited entry, skip
                        continue;
                    }
                    if (m_docSegList[i].ste.getSrcText().equals(
                            entry.getSrcText())) {
                        // the same source text - need to update
                        doc.replaceSegment(i, false);
                    }
                }
            }
        }
        // } catch (BadLocationException ex) {
        // LOGGER.log(Level.SEVERE, "Error activate entry", ex);
        // }
        editor.cancelUndo();
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

        commitAndDeactivate();

        displayedEntryIndex++;
        if (displayedEntryIndex >= m_docSegList.length) {
            displayedFileIndex++;
            displayedEntryIndex = 0;
            if (displayedFileIndex >= Core.getProject().getProjectFiles()
                    .size()) {
                displayedFileIndex = 0;
            }
            loadDocument();
        }

        activateEntry();
    }

    public void prevEntry() {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded())
            return;

        commitAndDeactivate();

        displayedEntryIndex--;
        if (displayedEntryIndex < 0) {
            displayedFileIndex--;
            if (displayedFileIndex < 0) {
                displayedFileIndex = Core.getProject().getProjectFiles().size() - 1;
            }
            displayedEntryIndex = Core.getProject().getProjectFiles().get(
                    displayedFileIndex).size - 1;
            loadDocument();
        }

        activateEntry();
    }

    /**
     * Finds the next untranslated entry in the document.
     */
    public void nextUntranslatedEntry() {
        UIThreadsUtil.mustBeSwingThread();

        // check if a document is loaded
        if (Core.getProject().isProjectLoaded() == false)
            return;

        // save the current entry
        commitAndDeactivate();

        int oldDisplayedEntryIndex = displayedEntryIndex;
        int oldDisplayedFileIndex = displayedFileIndex;

        while (true) {
            displayedEntryIndex++;
            if (displayedEntryIndex >= m_docSegList.length) {
                displayedFileIndex++;
                displayedEntryIndex = 0;
                if (displayedFileIndex >= Core.getProject().getProjectFiles()
                        .size()) {
                    displayedFileIndex = 0;
                }
            }
            if (displayedFileIndex == oldDisplayedFileIndex
                    && displayedEntryIndex == oldDisplayedEntryIndex) {
                // The same entry which was displayed. So, there is no
                // untranslated.
                break;
            }

            int globalEntryIndex = Core.getProject().getProjectFiles().get(
                    displayedFileIndex).firstEntryIndexInGlobalList
                    + displayedEntryIndex;
            SourceTextEntry ste = Core.getProject().getAllEntries().get(
                    globalEntryIndex);
            if (ste.getTranslation() == null
                    || ste.getTranslation().length() == 0) {
                // It's untranslated.
                break;
            }
        }

        if (displayedFileIndex != oldDisplayedFileIndex) {
            loadDocument();
        }

        activateEntry();
    }

    /**
     * {@inheritDoc}
     */
    public int getCurrentEntryNumber() {
        return getEntryNumber(displayedEntryIndex);
    }

    /**
     * Convert entry index in current file into global entry number.
     * 
     * @param entryIndexInCurrentFile
     *            index
     * @return global number
     */
    protected int getEntryNumber(int entryIndexInCurrentFile) {
        if (Core.getProject().getProjectFiles().isEmpty()) {
            // there is no files yet
            return -1;
        }

        int globalEntryIndex = Core.getProject().getProjectFiles().get(
                displayedFileIndex).firstEntryIndexInGlobalList
                + entryIndexInCurrentFile + 1;
        return globalEntryIndex;
    }

    /**
     * {@inheritDoc}
     */
    public void gotoEntry(final int entryNum) {
        UIThreadsUtil.mustBeSwingThread();

        if (!Core.getProject().isProjectLoaded())
            return;

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
                if (fi.firstEntryIndexInGlobalList <= entryNum - 1
                        && fi.firstEntryIndexInGlobalList + fi.size > entryNum - 1) {
                    // this file
                    displayedEntryIndex = entryNum - 1
                            - fi.firstEntryIndexInGlobalList;
                    if (i != displayedFileIndex) {
                        // it's other file than displayed
                        displayedFileIndex = i;
                        loadDocument();
                    }
                    break;
                }
            }
        }
        activateEntry();
    }

    /**
     * Change case of the selected text or if none is selected, of the current
     * word.
     * 
     * @param toWhat
     *            : lower, title, upper or cycle
     */
    public void changeCase(CHANGE_CASE_TO toWhat) {
        UIThreadsUtil.mustBeSwingThread();

        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();

        int caretPosition = editor.getCaretPosition();

        int translationStart = getTranslationStart();
        int translationEnd = getTranslationEnd();

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
                start = Utilities.getWordStart(editor, start);
                end = Utilities.getWordEnd(editor, end);
            }

            editor.setSelectionStart(start);
            editor.setSelectionEnd(end);

            String selectionText = editor.getText(start, end - start);
            // tokenize the selection
            Token[] tokenList = Core.getTokenizer().tokenizeWordsForSpelling(
                    selectionText);

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
                String result = doChangeCase(token
                        .getTextFromString(selectionText), toWhat);

                // replace this token
                buffer.replace(token.getOffset() + lengthIncrement, token
                        .getLength()
                        + token.getOffset() + lengthIncrement, result);

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
     * perform the case change. Lowercase becomes titlecase, titlecase becomes
     * uppercase, uppercase becomes lowercase. if the text matches none of these
     * categories, it is uppercased.
     * 
     * @param input
     *            : the string to work on
     * @param toWhat
     *            : one of the CASE_* values - except for case CASE_CYCLE.
     */
    private String doChangeCase(String input, CHANGE_CASE_TO toWhat) {
        Locale locale = Core.getProject().getProjectProperties()
                .getTargetLanguage().getLocale();

        switch (toWhat) {
        case LOWER:
            return input.toLowerCase(locale);
        case UPPER:
            return input.toUpperCase(locale);
        case TITLE:
            // TODO: find out how to get a locale-aware title case
            return Character.toTitleCase(input.charAt(0))
                    + input.substring(1).toLowerCase(locale);
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
        int start = getTranslationStart();
        int end = getTranslationEnd();

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
     * Calculate the position of the start of the current translation
     */
    protected int getTranslationStart() {
        return editor.getOmDocument().activeTranslationBegin.getOffset() + 1;
    }

    /**
     * Calculcate the position of the end of the current translation
     */
    protected int getTranslationEnd() {
        return editor.getOmDocument().activeTranslationEnd.getOffset() - 1;
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

        return editor.getSelectedText();
    }

    /** Loads Instant start article */
    private void createAdditionalPanes() {
        introPaneTitle = OStrings.getString("DOCKING_INSTANT_START_TITLE");
        try {
            String language = detectInstantStartLanguage();
            String filepath = StaticUtils.installDir() + File.separator
                    + OConsts.HELP_DIR + File.separator + language
                    + File.separator + OConsts.HELP_INSTANT_START;
            introPane = new JTextPane();
            introPane
                    .setComponentOrientation(EditorUtils.isRTL(language) ? ComponentOrientation.RIGHT_TO_LEFT
                            : ComponentOrientation.LEFT_TO_RIGHT);
            introPane.setEditable(false);
            introPane.setPage("file:///" + filepath);
        } catch (IOException e) {
            // editorScroller.setViewportView(editor);
        }

        emptyProjectPaneTitle = OStrings
                .getString("TF_INTRO_EMPTYPROJECT_FILENAME");
        emptyProjectPane = new JTextPane();
        emptyProjectPane.setEditable(false);
        emptyProjectPane.setText(OStrings.getString("TF_INTRO_EMPTYPROJECT"));
        emptyProjectPane.setFont(Core.getMainWindow().getApplicationFont());
    }

    /**
     * Detects the language of the instant start guide (checks if present in
     * default locale's language).
     * 
     * If there is no instant start guide in the default locale's language, "en"
     * (English) is returned, otherwise the acronym for the default locale's
     * language.
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    private String detectInstantStartLanguage() {
        // Get the system language and country
        String language = Locale.getDefault().getLanguage().toLowerCase();
        String country = Locale.getDefault().getCountry().toUpperCase();

        // Check if there's a translation for the full locale (lang + country)
        File isg = new File(StaticUtils.installDir() + File.separator
                + OConsts.HELP_DIR + File.separator + language + "_" + country
                + File.separator + OConsts.HELP_INSTANT_START);
        if (isg.exists())
            return language + "_" + country;

        // Check if there's a translation for the language only
        isg = new File(StaticUtils.installDir() + File.separator
                + OConsts.HELP_DIR + File.separator + language + File.separator
                + OConsts.HELP_INSTANT_START);
        if (isg.exists())
            return language;

        // Default to English, if no translation exists
        return "en";
    }
}
