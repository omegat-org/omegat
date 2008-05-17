/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
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
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.CommandThread;
import org.omegat.core.data.StringEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Class for control all editor operations.
 * 
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class EditorController implements IEditor {

    private static final String IMPOSSIBLE = "Should not have happened, " + // NOI18N
            "report to http://sf.net/tracker/?group_id=68187&atid=520347"; // NOI18N

    private final int WITH_END_MARKERS = 1;
    private final int IS_NOT_TRANSLATED = 2;

    private final EditorTextArea editor;
    private JTextPane introPane;
    protected final MainWindow mw;

    private SourceTextEntry m_curEntry;
    protected int m_curEntryNum = -1;

    // starting offset and length of source lang in current segment
    protected int m_segmentStartOffset;
    protected int m_sourceDisplayLength;
    protected int m_segmentEndInset;
    // text length of glossary, if displayed
    private int m_glossaryLength;

    /** first entry number in current file. */
    protected int m_xlFirstEntry;
    /** last entry number in current file. */
    protected int m_xlLastEntry;

    // indicates the document is loaded and ready for processing
    protected boolean m_docReady;

    /** text segments in current document. */
    protected DocumentSegment[] m_docSegList;

    /** Is any segment edited currently? */
    private boolean entryActivated = false;

    // boolean set after safety check that org.omegat.OConsts.segmentStartStringFull
    //  contains empty "0000" for segment number
    private boolean m_segmentTagHasNumber;

    /** Object which store history of moving by segments. */
    private SegmentHistory history = new SegmentHistory();
    
    private final EditorSettings settings;
    
    private final DockableScrollPane pane;
    
    private String previousFileName;
    
    private enum SHOW_TYPE {INTRO, FIRST_ENTRY, NO_CHANGE};

    public EditorController(final MainWindow mainWindow, final EditorTextArea editor, final DockableScrollPane pane) {
        this.mw = mainWindow;
        this.editor = editor;
        this.pane = pane;
        editor.controller = this;
        
        settings = new EditorSettings(this);

        // check this only once as it can be changed only at compile time
        // should be OK, but localization might have messed it up
        String start = OConsts.segmentStartStringFull;
        int zero = start.lastIndexOf('0');
        m_segmentTagHasNumber = (zero > 4) && // 4 to reserve room for 10000 digit
                (start.charAt(zero - 1) == '0') && (start.charAt(zero - 2) == '0') && (start.charAt(zero - 3) == '0');
        
        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                SHOW_TYPE showType;
                switch (eventType) {
                case CREATE:
                    showType=SHOW_TYPE.FIRST_ENTRY;
                    break;
                case LOAD:
                    showType=SHOW_TYPE.FIRST_ENTRY;
                    break;
                case CLOSE:
                    showType=SHOW_TYPE.INTRO;
                    break;
                default:
                    showType=SHOW_TYPE.NO_CHANGE;
                }
                updateState(showType);
            }
        });
        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            public void onNewFile(String activeFileName) {
                updateState(SHOW_TYPE.NO_CHANGE);
            }
            public void onEntryActivated(StringEntry newEntry) {
            }
        });
        
        
        loadIntro();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateState(SHOW_TYPE.INTRO);
            }
        });
        
        CoreEvents
                .registerFontChangedEventListener(new IFontChangedEventListener() {
                    public void onFontChanged(Font newFont) {
                        // fonts have changed
                        // first commit current translation
                        commitEntry(false); // part of fix for bug 1409309
                        editor.setFont(newFont);
                        activateEntry();
                    }
                });
    }
    
    private void updateState(SHOW_TYPE showType) {
        UIThreadsUtil.mustBeSwingThread();

        Component data;
        String title;

        if (showType == SHOW_TYPE.INTRO
                || !Core.getDataEngine().isProjectLoaded()) {
            data = introPane;
            title = OStrings.getString("DOCKING_INSTANT_START_TITLE");

            pane.getViewport().getView().requestFocus();
        } else {
            String file = getCurrentFile();
            title = StaticUtils.format(OStrings
                    .getString("GUI_SUBWINDOWTITLE_Editor"), file);
            data = editor;
            if (showType == SHOW_TYPE.FIRST_ENTRY) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // need to run later because some other event listeners
                        // should be called before
                        m_curEntryNum = 0;
                        loadDocument();
                        activateEntry();
                    }
                });
            }
        }

        pane.setName(title);
        if (pane.getViewport().getView() != data) {
            pane.setViewportView(data);
        }
        data.requestFocus();
    }

    /**
     * {@inheritDoc}
     */
    public SourceTextEntry getCurrentEntry() {
        return m_curEntry;
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentFile() {
        String fullName = CommandThread.core.getSTE(m_curEntryNum).getSrcFile().name;
        return fullName.substring(CommandThread.core.sourceRoot().length());
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
        synchronized (mw) {
            m_docReady = false;

            synchronized (editor) {
                // clear old text
                editor.setText(new String());

                m_curEntry = CommandThread.core.getSTE(m_curEntryNum);

                m_xlFirstEntry = m_curEntry.getFirstInFile();
                m_xlLastEntry = m_curEntry.getLastInFile();
                int xlEntries = 1 + m_xlLastEntry - m_xlFirstEntry;

                DocumentSegment docSeg;
                m_docSegList = new DocumentSegment[xlEntries];

                int totalLength = 0;

                AbstractDocument xlDoc = (AbstractDocument) editor.getDocument();
                AttributeSet attributes = settings.getTranslatedAttributeSet();

                // if the source should be displayed, too
                AttributeSet srcAttributes = settings.getUntranslatedAttributeSet();

                // how to display the source segment
                if (settings.isDisplaySegmentSources())
                    srcAttributes = Styles.GREEN;

                for (int i = 0; i < xlEntries; i++) {
                    docSeg = new DocumentSegment();

                    SourceTextEntry ste = CommandThread.core.getSTE(i + m_xlFirstEntry);
                    String sourceText = ste.getSrcText();
                    String text = ste.getTranslation();

                    boolean doSpellcheck = false;
                    // set text and font
                    if (text.length() == 0) {
                        if (!settings.isDisplaySegmentSources()) {
                            // no translation available - use source text
                            text = ste.getSrcText();
                            attributes = settings.getUntranslatedAttributeSet();
                        }
                    } else {
                        doSpellcheck = true;
                        attributes = settings.getTranslatedAttributeSet();
                    }
                    try {
                        if (settings.isDisplaySegmentSources()) {
                            xlDoc.insertString(totalLength, sourceText + "\n", srcAttributes);
                            totalLength += sourceText.length() + 1;
                        }

                        xlDoc.insertString(totalLength, text, attributes);

                        // mark the incorrectly set words, if needed
                        if (doSpellcheck && settings.isAutoSpellChecking()) {
                            EditorSpellChecking.checkSpelling(totalLength, text, this, editor);
                        }

                        totalLength += text.length();
                        // NOI18N
                        xlDoc.insertString(totalLength, "\n\n", Styles.PLAIN);

                        totalLength += 2;

                        if (settings.isDisplaySegmentSources()) {
                            text = sourceText + "\n" + text;
                        }

                        text += "\n\n";

                    } catch (BadLocationException ble) {
                        Log.log(IMPOSSIBLE);
                        Log.log(ble);
                    }

                    docSeg.length = text.length();
                    m_docSegList[i] = docSeg;
                }
            } // synchronized (editor)

            Thread.yield();
        }
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
        
        synchronized (mw) {
            if (!mw.isProjectLoaded())
                return;
            int translatedInFile = 0;
            for (int _i = m_xlFirstEntry; _i <= m_xlLastEntry; _i++) {
                if (CommandThread.core.getSTE(_i).isTranslated())
                    translatedInFile++;
            }

            String pMsg = " " + Integer.toString(translatedInFile) + "/"
                    + Integer.toString(m_xlLastEntry - m_xlFirstEntry + 1) + " ("
                    + Integer.toString(CommandThread.core.getNumberofTranslatedSegments()) + "/"
                    + Integer.toString(CommandThread.core.getNumberOfUniqueSegments()) + ", "
                    + Integer.toString(CommandThread.core.getNumberOfSegmentsTotal()) + ") ";
            Core.getMainWindow().showProgressMessage(pMsg);

            String lMsg = " " + Integer.toString(m_curEntry.getSrcText().length()) + "/"
                    + Integer.toString(m_curEntry.getTranslation().length()) + " ";
            Core.getMainWindow().showLengthMessage(lMsg);

            synchronized (editor) {
                history.insertNew(m_curEntryNum);

                // update history menu items
                mw.menu.gotoHistoryBackMenuItem.setEnabled(history.hasPrev());
                mw.menu.gotoHistoryForwardMenuItem.setEnabled(history.hasNext());

                // recover data about current entry
                // <HP-experiment>
                if (m_curEntryNum < m_xlFirstEntry) {
                    Log.log("ERROR: Current entry # lower than first entry #");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    // FIX: m_curEntryNum = m_xlFirstEntry;
                }
                if (m_curEntryNum > m_xlLastEntry) {
                    Log.log("ERROR: Current entry # greater than last entry #");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    // FIX: m_curEntryNum = m_xlLastEntry;
                }
                // </HP-experiment>
                m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
                String srcText = m_curEntry.getSrcText();

                m_sourceDisplayLength = srcText.length();

                // sum up total character offset to current segment start
                m_segmentStartOffset = 0;
                int localCur = m_curEntryNum - m_xlFirstEntry;
                // <HP-experiment>
                DocumentSegment docSeg = null; // <HP-experiment> remove once done experimenting
                try {
                    for (int i = 0; i < localCur; i++) {
                        //DocumentSegment // <HP-experiment> re-join with next line once done experimenting
                        docSeg = m_docSegList[i];
                        m_segmentStartOffset += docSeg.length; // length includes \n
                    }

                    //DocumentSegment // <HP-experiment> re-join with next line once done experimenting
                    docSeg = m_docSegList[localCur];
                } catch (Exception exception) {
                    Log.log("ERROR: exception while calculating character offset:");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    Log.log(exception);
                    return; // deliberately breaking, to simulate previous behaviour
                    // FIX: for (int i=0; i<localCur && i < m_docSegList.length; i++)
                }
                // </HP-experiment>

                // -2 to move inside newlines at end of segment
                m_segmentEndInset = editor.getTextLength() - (m_segmentStartOffset + docSeg.length - 2);

                String translation = m_curEntry.getTranslation();

                if (translation == null || translation.length() == 0) {
                    translation = m_curEntry.getSrcText();

                    // if "Leave translation empty" is set
                    // then we don't insert a source text into target
                    //
                    // RFE "Option: not copy source text into target field"
                    //      http://sourceforge.net/support/tracker.php?aid=1075972
                    if (Preferences.isPreference(Preferences.DONT_INSERT_SOURCE_TEXT)) {
                        translation = new String();
                    }
                }

                int replacedLength = replaceEntry(m_segmentStartOffset, docSeg.length, srcText, translation,
                        WITH_END_MARKERS);

                StringEntry curEntry = m_curEntry.getStrEntry();
                int nearLength = 0;// TODO: curEntry.getNearListTranslated().size();

                // <HP-experiment>
                try {
                    String msg;
                    if (nearLength > 0 && m_glossaryLength > 0) {
                        // display text indicating both categories exist
                        msg = StaticUtils.format(OStrings.getString("TF_NUM_NEAR_AND_GLOSSARY"), nearLength,
                                m_glossaryLength);
                    } else if (nearLength > 0) {
                        msg = StaticUtils.format(OStrings.getString("TF_NUM_NEAR"), nearLength);
                    } else if (m_glossaryLength > 0) {
                        msg = StaticUtils.format(OStrings.getString("TF_NUM_GLOSSARY"), m_glossaryLength);
                    } else {
                        msg = new String();
                    }
                    Core.getMainWindow().showStatusMessage(msg);
                } catch (Exception exception) {
                    Log.log("ERROR: exception while setting message text:");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    Log.log(exception);
                    return; // deliberately breaking, to simulate previous behaviour
                    // FIX: unknown
                }
                // </HP-experiment>

                int offsetPrev = 0;
                int localNum = m_curEntryNum - m_xlFirstEntry;
                // <HP-experiment>
                try {
                    for (int i = Math.max(0, localNum - 3); i < localNum; i++) {
                        docSeg = m_docSegList[i];
                        offsetPrev += docSeg.length;
                    }
                } catch (Exception exception) {
                    Log.log("ERROR: exception while calculating previous offset:");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    Log.log(exception);
                    return; // deliberately breaking, to simulate previous behaviour
                    // FIX: unknown
                }
                // </HP-experiment>
                final int lookPrev = m_segmentStartOffset - offsetPrev;

                int offsetNext = 0;
                int localLast = m_xlLastEntry - m_xlFirstEntry;
                // <HP-experiment>
                try {
                    for (int i = localNum + 1; i < (localNum + 4) && i <= localLast; i++) {
                        docSeg = m_docSegList[i];
                        offsetNext += docSeg.length;
                    }
                } catch (Exception exception) {
                    Log.log("ERROR: exception while calculating next offset:");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    Log.log(exception);
                    return; // deliberately breaking, to simulate previous behaviour
                    // FIX: unknown
                }
                // </HP-experiment>
                final int lookNext = m_segmentStartOffset + replacedLength + offsetNext;

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            editor.setCaretPosition(lookNext);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    try {
                                        editor.setCaretPosition(lookPrev);
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                checkCaret();
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

                if (!m_docReady) {
                    m_docReady = true;
                }
                editor.cancelUndo();

                checkSpelling(true);
                CoreEvents.fireEntryActivated(m_curEntry.getStrEntry());
            } // synchronize (editor)

            entryActivated = true;
            if (previousFileName == null
                    || !previousFileName.equals(getCurrentFile())) {
                previousFileName = getCurrentFile();
                CoreEvents.fireEntryNewFile(previousFileName);
            }
        }
    }

    /**
     * Commits the translation. Reads current entry text and commit it to memory
     * if it's changed. Also clears out segment markers while we're at it.
     * <p>
     * Since 1.6: Translation equal to source may be validated as OK translation
     * if appropriate option is set in Workflow options dialog.
     */
    public void commitEntry() {
        UIThreadsUtil.mustBeSwingThread();
        
        commitEntry(true);
    }

    /**
     * Commits the translation. Reads current entry text and commit it to memory
     * if it's changed. Also clears out segment markers while we're at it.
     * <p>
     * Since 1.6: Translation equal to source may be validated as OK translation
     * if appropriate option is set in Workflow options dialog.
     * 
     * @param forceCommit
     *            If false, the translation will not be saved
     */
    public void commitEntry(final boolean forceCommit) {
        UIThreadsUtil.mustBeSwingThread();
        
        synchronized (mw) {
            if (!mw.isProjectLoaded())
                return;

            if (!entryActivated)
                return;
            entryActivated = false;

            synchronized (editor) {
                AbstractDocument xlDoc = (AbstractDocument) editor.getDocument();

                AttributeSet attributes = settings.getTranslatedAttributeSet();

                int start = getTranslationStart();
                int end = getTranslationEnd();
                String display_string;
                String new_translation;

                boolean doCheckSpelling = true;

                // the list of incorrect words returned eventually by the 
                // spellchecker
                List<Token> wordList = null;
                int flags = IS_NOT_TRANSLATED;

                if (start == end) {
                    new_translation = new String();
                    doCheckSpelling = false;

                    if (!settings.isDisplaySegmentSources()) {
                        display_string = m_curEntry.getSrcText();
                        attributes = settings.getUntranslatedAttributeSet();
                    } else {
                        display_string = new String();
                    }
                } else {
                    try {
                        new_translation = xlDoc.getText(start, end - start);
                        if (new_translation.equals(m_curEntry.getSrcText())
                                && !Preferences.isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC)) {
                            attributes = settings.getUntranslatedAttributeSet();
                            doCheckSpelling = false;
                        } else {
                            attributes = settings.getTranslatedAttributeSet();
                            flags = 0;
                        }
                    } catch (BadLocationException ble) {
                        Log.log(IMPOSSIBLE);
                        Log.log(ble);
                        new_translation = new String();
                        doCheckSpelling = false;
                    }
                    display_string = new_translation;
                }

                int startOffset = m_segmentStartOffset;
                int totalLen = m_sourceDisplayLength + OConsts.segmentStartStringFull.length()
                        + new_translation.length() + OConsts.segmentEndStringFull.length() + 2;

                int localCur = m_curEntryNum - m_xlFirstEntry;
                DocumentSegment docSeg = m_docSegList[localCur];
                docSeg.length = display_string.length() + "\n\n".length(); // NOI18N
                String segmentSource = null;

                if (settings.isDisplaySegmentSources()) {
                    int increment = m_sourceDisplayLength + 1;
                    startOffset += increment;
                    //totalLen -= increment;
                    docSeg.length += increment;
                    segmentSource = m_curEntry.getSrcText();
                }

                docSeg.length = replaceEntry(m_segmentStartOffset, totalLen, segmentSource, display_string, flags);

                if (doCheckSpelling && settings.isAutoSpellChecking()) {
                    wordList = EditorSpellChecking.checkSpelling(startOffset, display_string, this, editor);
                }

                if (forceCommit) { // fix for 
                    String old_translation = m_curEntry.getTranslation();
                    // update memory
                    if (new_translation.equals(m_curEntry.getSrcText())
                            && !Preferences.isPreference(Preferences.ALLOW_TRANS_EQUAL_TO_SRC))
                        m_curEntry.setTranslation(new String());
                    else
                        m_curEntry.setTranslation(new_translation);

                    // update the length parameters of all changed segments
                    // update strings in display
                    if (!m_curEntry.getTranslation().equals(old_translation)) {
                        // find all identical strings and redraw them

                        // build offsets of all strings
                        int localEntries = 1 + m_xlLastEntry - m_xlFirstEntry;
                        int[] offsets = new int[localEntries];
                        int currentOffset = 0;
                        for (int i = 0; i < localEntries; i++) {
                            offsets[i] = currentOffset;
                            docSeg = m_docSegList[i];
                            currentOffset += docSeg.length;
                        }

                        // starting from the last (guaranteed by sorting ParentList)
                        for (SourceTextEntry ste : m_curEntry.getStrEntry().getParentList()) {
                            int entry = ste.entryNum();
                            if (entry > m_xlLastEntry)
                                continue;
                            else if (entry < m_xlFirstEntry)
                                break;
                            else if (entry == m_curEntryNum)
                                continue;

                            int localEntry = entry - m_xlFirstEntry;
                            int offset = offsets[localEntry];
                            int replacementLength = docSeg.length;

                            // replace old text w/ new
                            docSeg = m_docSegList[localEntry];
                            docSeg.length = replaceEntry(offset, docSeg.length, segmentSource, display_string, flags);

                            int supplement = 0;

                            if (settings.isDisplaySegmentSources()) {
                                supplement = ste.getSrcText().length() + "\n".length();
                            }

                            if (doCheckSpelling && wordList != null) {
                                for (Token token : wordList) {
                                    int tokenStart = token.getOffset();
                                    int tokenEnd = tokenStart + token.getLength();
                                    String word = token.getTextFromString(display_string);

                                    try {
                                        xlDoc.replace(offset + supplement + tokenStart, token.getLength(), word, Styles
                                                .applyStyles(attributes, Styles.MISSPELLED));
                                    } catch (BadLocationException ble) {
                                        //Log.log(IMPOSSIBLE);
                                        Log.log(ble);
                                    }
                                }
                            }
                        }
                    }
                }
                editor.cancelUndo();
            } // synchronize (editor)
        }
    }

    public void nextEntry() {
        UIThreadsUtil.mustBeSwingThread();
        
        synchronized (mw) {
            if (!mw.isProjectLoaded())
                return;

            commitEntry();

            m_curEntryNum++;
            if (m_curEntryNum > m_xlLastEntry) {
                if (m_curEntryNum >= CommandThread.core.getNumberOfSegmentsTotal())
                    m_curEntryNum = 0;
                loadDocument();
            }

            activateEntry();
        }
    }

    public void prevEntry() {
        UIThreadsUtil.mustBeSwingThread();
        
        synchronized (mw) {
            if (!mw.isProjectLoaded())
                return;

            commitEntry();

            m_curEntryNum--;
            if (m_curEntryNum < m_xlFirstEntry) {
                if (m_curEntryNum < 0)
                    m_curEntryNum = CommandThread.core.getNumberOfSegmentsTotal() - 1;
                // empty project bugfix:
                if (m_curEntryNum < 0)
                    m_curEntryNum = 0;
                loadDocument();
            }
            activateEntry();
        }
    }

    /**
     * Finds the next untranslated entry in the document.
     * <p>
     * Since 1.6.0 RC9 also looks from the beginning of the document if there're
     * no untranslated till the end of document. This way it look at entire
     * project like Go To Next Segment does.
     * 
     * @author Henry Pijffers
     * @author Maxym Mykhalchuk
     */
    public void nextUntranslatedEntry() {
        UIThreadsUtil.mustBeSwingThread();
        
        synchronized (mw) {
            // check if a document is loaded
            if (mw.isProjectLoaded() == false)
                return;

            // save the current entry
            commitEntry();

            // get the total number of entries
            int numEntries = CommandThread.core.getNumberOfSegmentsTotal();

            boolean found = false;
            int curEntryNum;

            // iterate through the list of entries,
            // starting at the current entry,
            // until an entry with no translation is found
            for (curEntryNum = m_curEntryNum + 1; curEntryNum < numEntries; curEntryNum++) {
                // get the next entry
                SourceTextEntry entry = CommandThread.core.getSTE(curEntryNum);

                // check if the entry is not null, and whether it contains a translation
                if (entry != null && entry.getTranslation().length() == 0) {
                    // we've found it
                    found = true;
                    // stop searching
                    break;
                }
            }

            // if we haven't found untranslated entry till the end,
            // trying to search for it from the beginning
            if (!found) {
                for (curEntryNum = 0; curEntryNum < m_curEntryNum; curEntryNum++) {
                    // get the next entry
                    SourceTextEntry entry = CommandThread.core.getSTE(curEntryNum);

                    // check if the entry is not null, and whether it contains a translation
                    if (entry != null && entry.getTranslation().length() == 0) {
                        // we've found it
                        found = true;
                        // stop searching
                        break;
                    }
                }
            }

            if (found) {
                // mark the entry
                m_curEntryNum = curEntryNum;

                // load the document, if the segment is not in the current document
                if (m_curEntryNum < m_xlFirstEntry || m_curEntryNum > m_xlLastEntry)
                    loadDocument();
            }

            // activate the entry
            activateEntry();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void gotoEntry(final int entryNum) {
        UIThreadsUtil.mustBeSwingThread();
        
        synchronized (mw) {
            if (!mw.isProjectLoaded())
                return;

            commitEntry();

            m_curEntryNum = entryNum - 1;
            if (m_curEntryNum < m_xlFirstEntry) {
                if (m_curEntryNum < 0)
                    m_curEntryNum = CommandThread.core.getNumberOfSegmentsTotal() - 1;
                // empty project bugfix:
                if (m_curEntryNum < 0)
                    m_curEntryNum = 0;
                loadDocument();
            } else if (m_curEntryNum > m_xlLastEntry) {
                if (m_curEntryNum >= CommandThread.core.getNumberOfSegmentsTotal())
                    m_curEntryNum = 0;
                loadDocument();
            }
            activateEntry();
        }
    }

    /**
     * Change case of the selected text or if none is selected, of the current
     * word.
     * 
     * @param toWhat :
     *            lower, title, upper or cycle
     */
    public void changeCase(CHANGE_CASE_TO toWhat) {
        UIThreadsUtil.mustBeSwingThread();
        
        synchronized (editor) {
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
                Token[] tokenList = Core.getTokenizer().tokenizeWordsForSpelling(selectionText);

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
    }

    /**
     * perform the case change. Lowercase becomes titlecase, titlecase becomes
     * uppercase, uppercase becomes lowercase. if the text matches none of these
     * categories, it is uppercased.
     * 
     * @param input :
     *            the string to work on
     * @param toWhat:
     *            one of the CASE_* values - except for case CASE_CYCLE.
     */
    private String doChangeCase(String input, CHANGE_CASE_TO toWhat) {
        Locale locale = Core.getDataEngine().getProjectProperties().getTargetLanguage().getLocale();

        switch (toWhat) {
        case LOWER:
            return input.toLowerCase(locale);
        case UPPER:
            return input.toUpperCase(locale);
            // TODO: find out how to get a locale-aware title case
        case TITLE:
            return Character.toTitleCase(input.charAt(0)) + input.substring(1).toLowerCase(locale);
        }
        // if everything fails
        return input.toUpperCase(locale);
    }

    /**
     * Checks whether the selection & caret is inside editable text, and changes
     * their positions accordingly if not.
     * 
     * TODO: make private
     */
    public void checkCaret() {
        synchronized (mw) {
            synchronized (editor) {
                //int pos = m_editor.getCaretPosition();
                int spos = editor.getSelectionStart();
                int epos = editor.getSelectionEnd();
                /*
                 * int start = m_segmentStartOffset + m_sourceDisplayLength +
                 * OConsts.segmentStartStringFull.length();
                 */
                int start = getTranslationStart();
                // -1 for space before tag, -2 for newlines
                /*
                 * int end = editor.getTextLength() - m_segmentEndInset -
                 * OConsts.segmentEndStringFull.length();
                 */
                int end = getTranslationEnd();

                if (spos != epos) {
                    // dealing with a selection here - make sure it's w/in bounds
                    if (spos < start) {
                        editor.setSelectionStart(start);
                    } else if (spos > end) {
                        editor.setSelectionStart(end);
                    }
                    if (epos > end) {
                        editor.setSelectionEnd(end);
                    } else if (epos < start) {
                        editor.setSelectionStart(start);
                    }
                } else {
                    // non selected text
                    if (spos < start) {
                        editor.setCaretPosition(start);
                    } else if (spos > end) {
                        editor.setCaretPosition(end);
                    }
                }
            } // synchronized (editor)
        }
    }

    /**
     * Make sure there's one character in the direction indicated for delete
     * operation.
     * 
     * @param forward
     * @return true if space is available
     * 
     * TODO: make private
     */
    public boolean checkCaretForDelete(final boolean forward) {
        synchronized (mw) {
            synchronized (editor) {
                int pos = editor.getCaretPosition();

                // make sure range doesn't overlap boundaries
                checkCaret();

                if (forward) {
                    // make sure we're not at end of segment
                    // -1 for space before tag, -2 for newlines
                    int end = editor.getTextLength() - m_segmentEndInset - OConsts.segmentEndStringFull.length();
                    int spos = editor.getSelectionStart();
                    int epos = editor.getSelectionEnd();
                    if (pos >= end && spos >= end && epos >= end)
                        return false;
                } else {
                    // make sure we're not at start of segment
                    int start = getTranslationStart();
                    int spos = editor.getSelectionStart();
                    int epos = editor.getSelectionEnd();
                    if (pos <= start && epos <= start && spos <= start)
                        return false;
                }
            } // synchronized (editor)

            return true;
        }
    }

    /**
     * replace the text in the editor and return the new length
     */
    private int replaceEntry(int offset, int length, String source, String translation, int flags) {
        synchronized (mw) {
            synchronized (editor) {
                AbstractDocument xlDoc = (AbstractDocument) editor.getDocument();

                int result = 0;

                AttributeSet attr = ((flags & IS_NOT_TRANSLATED) == IS_NOT_TRANSLATED ? settings
                        .getUntranslatedAttributeSet() : settings.getTranslatedAttributeSet());

                try {
                    xlDoc.remove(offset, length);

                    xlDoc.insertString(offset, "\n\n", Styles.PLAIN);
                    result = 2;
                    if ((flags & WITH_END_MARKERS) == WITH_END_MARKERS) {
                        String endStr = OConsts.segmentEndStringFull;
                        xlDoc.insertString(offset, endStr, Styles.PLAIN);
                        // make the text bold
                        xlDoc.replace(offset + endStr.indexOf(OConsts.segmentEndString), OConsts.segmentEndString
                                .length(), OConsts.segmentEndString, Styles.BOLD);
                        result += endStr.length();
                    }
                    // modify the attributes only if absolutely necessary
                    if (translation != null && !translation.equals("")) {
                        xlDoc.insertString(offset, translation, attr);
                        result += translation.length();
                    }

                    if ((flags & WITH_END_MARKERS) == WITH_END_MARKERS) {
                        // insert a plain space
                        xlDoc.insertString(offset, " ", Styles.PLAIN);
                        String startStr = new String(OConsts.segmentStartString);
                        // <HP-experiment>

                        try {
                            if (m_segmentTagHasNumber) {
                                // put entry number in first tag
                                String num = String.valueOf(m_curEntryNum + 1);
                                int zero = startStr.lastIndexOf('0');
                                startStr = startStr.substring(0, zero - num.length() + 1) + num
                                        + startStr.substring(zero + 1, startStr.length());
                            }
                        } catch (Exception exception) {
                            Log.log("ERROR: exception while putting segment # in start tag:");
                            Log
                                    .log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                            Log.log(exception);
                            // FIX: since these are localised, don't assume number appears, keep try/catch block
                        }
                        // </HP-experiment>
                        /*
                         * startStr = "<segment
                         * "+Integer.toString(m_curEntryNum + 1)+">";
                         */
                        xlDoc.insertString(offset, startStr, Styles.BOLD);
                        result += startStr.length();
                    }
                    if (source != null) {
                        if ((flags & WITH_END_MARKERS) != WITH_END_MARKERS) {
                            source += "\n";
                        }
                        xlDoc.insertString(offset, source, Styles.GREEN);
                        result += source.length();
                    }
                } catch (BadLocationException ble) {
                    Log.log(IMPOSSIBLE);
                    Log.log(ble);
                }

                return result;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void replaceEditText(final String text) {
        UIThreadsUtil.mustBeSwingThread();
        
        synchronized (mw) {
            synchronized (editor) {
                // build local offsets
                int start = getTranslationStart();
                int end = getTranslationEnd();

                // remove text
                editor.select(start, end);
                editor.replaceSelection(text);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void insertText(final String text) {
        UIThreadsUtil.mustBeSwingThread();
        
        synchronized (mw) {
            synchronized (editor) {
                //            int pos = editor.getCaretPosition();
                //            editor.select(pos, pos);
                // Removing the two lines above implements:
                // RFE [ 1579488 ] overwriting with Ctrl+i
                editor.replaceSelection(text);
            }
        }
    }

    /**
     * Calculate the position of the start of the current translation
     * 
     * TODO: make private
     */
    protected int getTranslationStart() {
        synchronized (mw) {
            synchronized (editor) {
                return m_segmentStartOffset + m_sourceDisplayLength + OConsts.segmentStartStringFull.length();
            }
        }
    }

    /**
     * Calculcate the position of the end of the current translation
     * 
     * TODO: make private
     */
    protected int getTranslationEnd() {
        synchronized (mw) {
            synchronized (editor) {
                return editor.getTextLength() - m_segmentEndInset - OConsts.segmentEndStringFull.length();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearHistory() {
        UIThreadsUtil.mustBeSwingThread();
        
        history.clear();
    }

    /**
     * {@inheritDoc}
     */
    public void gotoHistoryBack() {
        UIThreadsUtil.mustBeSwingThread();
        
        int prevValue = history.back();
        if (prevValue != -1) {
            gotoEntry(prevValue + 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void gotoHistoryForward() {
        UIThreadsUtil.mustBeSwingThread();
        
        int nextValue = history.forward();
        if (nextValue != -1) {
            gotoEntry(nextValue + 1);
        }
    }

    /**
     * Check the spelling of the words around the caret (the word the caret is
     * in or, if between words, the word before and the word after.
     * 
     * Used with keyboard events which modify the text.
     * 
     * @param keycode :
     *                the keycode, to prevent multiple passes
     * @param full :
     *                if true, the whole segment is checked
     *                
     * TODO: make private               
     */
    protected void checkSpelling(final boolean full) {
        UIThreadsUtil.mustBeSwingThread();
        
        if (!settings.isAutoSpellChecking())
            return;
        synchronized (editor) {
            EditorSpellChecking.checkSpelling(full, this, editor);
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * TODO
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
    private void loadIntro() {
        try {
            String language = detectInstantStartLanguage();
            String filepath = StaticUtils.installDir() + File.separator
                    + OConsts.HELP_DIR + File.separator + language
                    + File.separator + OConsts.HELP_INSTANT_START;
            introPane = new JTextPane();
            introPane.setEditable(false);
            introPane.setPage("file:///" + filepath);
        } catch (IOException e) {
            // editorScroller.setViewportView(editor);
        }
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
