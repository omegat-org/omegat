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

package org.omegat.gui.main;

import java.util.List;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

import org.omegat.core.Core;
import org.omegat.core.StringEntry;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;

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
    private final EditorTextArea editor;
    private final MainWindow mw;

    public EditorController(final MainWindow mainWindow, final EditorTextArea editor) {
        this.mw = mainWindow;
        this.editor = editor;
    }

    /**
     * Displays all segments in current document.
     * <p>
     * Displays translation for each segment if it's available, otherwise
     * displays source text. Also stores length of each displayed segment plus
     * its starting offset.
     */
    public void loadDocument() {
        synchronized (mw) {
            mw.m_docReady = false;

            synchronized (editor) {
                // clear old text
                editor.setText(new String());

                // update the title and the project window
                if (mw.isProjectLoaded())
                    mw.updateTitle();
                mw.m_projWin.buildDisplay();

                mw.m_curEntry = CommandThread.core.getSTE(mw.m_curEntryNum);

                mw.m_xlFirstEntry = mw.m_curEntry.getFirstInFile();
                mw.m_xlLastEntry = mw.m_curEntry.getLastInFile();
                int xlEntries = 1 + mw.m_xlLastEntry - mw.m_xlFirstEntry;

                DocumentSegment docSeg;
                mw.m_docSegList = new DocumentSegment[xlEntries];

                int totalLength = 0;

                AbstractDocument xlDoc = (AbstractDocument) editor.getDocument();
                AttributeSet attributes = mw.m_translatedAttributeSet;

                // if the source should be displayed, too
                AttributeSet srcAttributes = mw.m_unTranslatedAttributeSet;

                // how to display the source segment
                if (mw.m_displaySegmentSources)
                    srcAttributes = Styles.GREEN;

                for (int i = 0; i < xlEntries; i++) {
                    docSeg = new DocumentSegment();

                    SourceTextEntry ste = CommandThread.core.getSTE(i + mw.m_xlFirstEntry);
                    String sourceText = ste.getSrcText();
                    String text = ste.getTranslation();

                    boolean doSpellcheck = false;
                    // set text and font
                    if (text.length() == 0) {
                        if (!mw.m_displaySegmentSources) {
                            // no translation available - use source text
                            text = ste.getSrcText();
                            attributes = mw.m_unTranslatedAttributeSet;
                        }
                    } else {
                        doSpellcheck = true;
                        attributes = mw.m_translatedAttributeSet;
                    }
                    try {
                        if (mw.m_displaySegmentSources) {
                            xlDoc.insertString(totalLength, sourceText + "\n", srcAttributes);
                            totalLength += sourceText.length() + 1;
                        }

                        xlDoc.insertString(totalLength, text, attributes);

                        // mark the incorrectly set words, if needed
                        if (doSpellcheck && mw.m_autoSpellChecking) {
                            mw.checkSpelling(totalLength, text);
                        }

                        totalLength += text.length();
                        // NOI18N
                        xlDoc.insertString(totalLength, "\n\n", Styles.PLAIN);

                        totalLength += 2;

                        if (mw.m_displaySegmentSources) {
                            text = sourceText + "\n" + text;
                        }

                        text += "\n\n";

                    } catch (BadLocationException ble) {
                        Log.log(mw.IMPOSSIBLE);
                        Log.log(ble);
                    }

                    docSeg.length = text.length();
                    mw.m_docSegList[i] = docSeg;
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
        synchronized (mw) {
            if (!mw.isProjectLoaded())
                return;
            int translatedInFile = 0;
            for (int _i = mw.m_xlFirstEntry; _i <= mw.m_xlLastEntry; _i++) {
                if (CommandThread.core.getSTE(_i).isTranslated())
                    translatedInFile++;
            }

            String pMsg = " " + Integer.toString(translatedInFile) + "/"
                    + Integer.toString(mw.m_xlLastEntry - mw.m_xlFirstEntry + 1) + " ("
                    + Integer.toString(CommandThread.core.getNumberofTranslatedSegments()) + "/"
                    + Integer.toString(CommandThread.core.getNumberOfUniqueSegments()) + ", "
                    + Integer.toString(CommandThread.core.getNumberOfSegmentsTotal()) + ") ";
            Core.getMainWindow().showProgressMessage(pMsg);

            String lMsg = " " + Integer.toString(mw.m_curEntry.getSrcText().length()) + "/"
                    + Integer.toString(mw.m_curEntry.getTranslation().length()) + " ";
            Core.getMainWindow().showLengthMessage(lMsg);

            synchronized (editor) {
                mw.history.insertNew(mw.m_curEntryNum);

                // update history menu items
                mw.menu.gotoHistoryBackMenuItem.setEnabled(mw.history.hasPrev());
                mw.menu.gotoHistoryForwardMenuItem.setEnabled(mw.history.hasNext());

                // recover data about current entry
                // <HP-experiment>
                if (mw.m_curEntryNum < mw.m_xlFirstEntry) {
                    Log.log("ERROR: Current entry # lower than first entry #");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    // FIX: m_curEntryNum = m_xlFirstEntry;
                }
                if (mw.m_curEntryNum > mw.m_xlLastEntry) {
                    Log.log("ERROR: Current entry # greater than last entry #");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    // FIX: m_curEntryNum = m_xlLastEntry;
                }
                // </HP-experiment>
                mw.m_curEntry = CommandThread.core.getSTE(mw.m_curEntryNum);
                String srcText = mw.m_curEntry.getSrcText();

                mw.m_sourceDisplayLength = srcText.length();

                // sum up total character offset to current segment start
                mw.m_segmentStartOffset = 0;
                int localCur = mw.m_curEntryNum - mw.m_xlFirstEntry;
                // <HP-experiment>
                DocumentSegment docSeg = null; // <HP-experiment> remove once done experimenting
                try {
                    for (int i = 0; i < localCur; i++) {
                        //DocumentSegment // <HP-experiment> re-join with next line once done experimenting
                        docSeg = mw.m_docSegList[i];
                        mw.m_segmentStartOffset += docSeg.length; // length includes \n
                    }

                    //DocumentSegment // <HP-experiment> re-join with next line once done experimenting
                    docSeg = mw.m_docSegList[localCur];
                } catch (Exception exception) {
                    Log.log("ERROR: exception while calculating character offset:");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    Log.log(exception);
                    return; // deliberately breaking, to simulate previous behaviour
                    // FIX: for (int i=0; i<localCur && i < m_docSegList.length; i++)
                }
                // </HP-experiment>

                // -2 to move inside newlines at end of segment
                mw.m_segmentEndInset = editor.getTextLength() - (mw.m_segmentStartOffset + docSeg.length - 2);

                String translation = mw.m_curEntry.getTranslation();

                if (translation == null || translation.length() == 0) {
                    translation = mw.m_curEntry.getSrcText();

                    // if "Leave translation empty" is set
                    // then we don't insert a source text into target
                    //
                    // RFE "Option: not copy source text into target field"
                    //      http://sourceforge.net/support/tracker.php?aid=1075972
                    if (Preferences.isPreference(Preferences.DONT_INSERT_SOURCE_TEXT)) {
                        translation = new String();
                    }

                    // if WORKFLOW_OPTION "Insert best fuzzy match into target field" is set
                    // RFE "Option: Insert best match (80%+) into target field"
                    //      http://sourceforge.net/support/tracker.php?aid=1075976
                    if (Preferences.isPreference(Preferences.BEST_MATCH_INSERT)) {
                        String percentage_s = Preferences.getPreferenceDefault(
                                Preferences.BEST_MATCH_MINIMAL_SIMILARITY,
                                Preferences.BEST_MATCH_MINIMAL_SIMILARITY_DEFAULT);
                        // <HP-experiment>
                        int percentage = 0;
                        try {
                            //int
                            percentage = Integer.parseInt(percentage_s);
                        } catch (Exception exception) {
                            Log.log("ERROR: exception while parsing percentage:");
                            Log
                                    .log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                            Log.log(exception);
                            return; // deliberately breaking, to simulate previous behaviour
                            // FIX: unknown, but expect number parsing errors
                        }
                        // </HP-experiment>
                        List<NearString> near = mw.m_curEntry.getStrEntry().getNearListTranslated();
                        if (near.size() > 0) {
                            NearString thebest = near.get(0);
                            if (thebest.score >= percentage) {
                                int old_tr_len = translation.length();
                                translation = Preferences.getPreferenceDefault(Preferences.BEST_MATCH_EXPLANATORY_TEXT,
                                        OStrings.getString("WF_DEFAULT_PREFIX"))
                                        + thebest.str.getTranslation();
                            }
                        }
                    }
                }

                int replacedLength = mw.replaceEntry(mw.m_segmentStartOffset, docSeg.length, srcText, translation,
                        mw.WITH_END_MARKERS);

                // <HP-experiment>
                try {
                    mw.updateFuzzyInfo();
                    mw.updateGlossaryInfo();
                } catch (Exception exception) {
                    Log.log("ERROR: exception while updating match and glossary info:");
                    Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                    Log.log(exception);
                    return; // deliberately breaking, to simulate previous behaviour
                    // FIX: unknown
                }
                // </HP-experiment>

                StringEntry curEntry = mw.m_curEntry.getStrEntry();
                int nearLength = curEntry.getNearListTranslated().size();

                // <HP-experiment>
                try {
                    String msg;
                    if (nearLength > 0 && mw.m_glossaryLength > 0) {
                        // display text indicating both categories exist
                        msg = StaticUtils.format(OStrings.getString("TF_NUM_NEAR_AND_GLOSSARY"), nearLength, mw.m_glossaryLength);
                    } else if (nearLength > 0) {
                        msg = StaticUtils.format(OStrings.getString("TF_NUM_NEAR"), nearLength);
                    } else if (mw.m_glossaryLength > 0) {
                        msg = StaticUtils.format(OStrings.getString("TF_NUM_GLOSSARY"), mw.m_glossaryLength);
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
                int localNum = mw.m_curEntryNum - mw.m_xlFirstEntry;
                // <HP-experiment>
                try {
                    for (int i = Math.max(0, localNum - 3); i < localNum; i++) {
                        docSeg = mw.m_docSegList[i];
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
                final int lookPrev = mw.m_segmentStartOffset - offsetPrev;

                int offsetNext = 0;
                int localLast = mw.m_xlLastEntry - mw.m_xlFirstEntry;
                // <HP-experiment>
                try {
                    for (int i = localNum + 1; i < (localNum + 4) && i <= localLast; i++) {
                        docSeg = mw.m_docSegList[i];
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
                final int lookNext = mw.m_segmentStartOffset + replacedLength + offsetNext;

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
                                                mw.checkCaret();
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

                if (!mw.m_docReady) {
                    mw.m_docReady = true;
                }
                editor.cancelUndo();

                editor.checkSpelling(true);
            } // synchronize (editor)

            mw.entryActivated = true;
        }
    }

    public void nextEntry() {
        synchronized (mw) {
            if (!mw.isProjectLoaded())
                return;

            mw.commitEntry();

            mw.m_curEntryNum++;
            if (mw.m_curEntryNum > mw.m_xlLastEntry) {
                if (mw.m_curEntryNum >= CommandThread.core.numEntries())
                    mw.m_curEntryNum = 0;
                loadDocument();
            }

            activateEntry();
        }
    }

    public void prevEntry() {
        synchronized (mw) {
            if (!mw.isProjectLoaded())
                return;

            mw.commitEntry();

            mw.m_curEntryNum--;
            if (mw.m_curEntryNum < mw.m_xlFirstEntry) {
                if (mw.m_curEntryNum < 0)
                    mw.m_curEntryNum = CommandThread.core.numEntries() - 1;
                // empty project bugfix:
                if (mw.m_curEntryNum < 0)
                    mw.m_curEntryNum = 0;
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
        synchronized (mw) {
            // check if a document is loaded
            if (mw.isProjectLoaded() == false)
                return;

            // save the current entry
            mw.commitEntry();

            // get the total number of entries
            int numEntries = CommandThread.core.numEntries();

            boolean found = false;
            int curEntryNum;

            // iterate through the list of entries,
            // starting at the current entry,
            // until an entry with no translation is found
            for (curEntryNum = mw.m_curEntryNum + 1; curEntryNum < numEntries; curEntryNum++) {
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
                for (curEntryNum = 0; curEntryNum < mw.m_curEntryNum; curEntryNum++) {
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
                mw.m_curEntryNum = curEntryNum;

                // load the document, if the segment is not in the current document
                if (mw.m_curEntryNum < mw.m_xlFirstEntry || mw.m_curEntryNum > mw.m_xlLastEntry)
                    loadDocument();
            }

            // activate the entry
            activateEntry();
        }
    }

    public void gotoEntry(final int entryNum) {
        synchronized (mw) {
            if (!mw.isProjectLoaded())
                return;

            mw.commitEntry();

            mw.m_curEntryNum = entryNum - 1;
            if (mw.m_curEntryNum < mw.m_xlFirstEntry) {
                if (mw.m_curEntryNum < 0)
                    mw.m_curEntryNum = CommandThread.core.numEntries() - 1;
                // empty project bugfix:
                if (mw.m_curEntryNum < 0)
                    mw.m_curEntryNum = 0;
                loadDocument();
            } else if (mw.m_curEntryNum > mw.m_xlLastEntry) {
                if (mw.m_curEntryNum >= CommandThread.core.numEntries())
                    mw.m_curEntryNum = 0;
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
        synchronized (editor) {
            int start = editor.getSelectionStart();
            int end = editor.getSelectionEnd();

            int caretPosition = editor.getCaretPosition();

            int translationStart = mw.getTranslationStart();
            int translationEnd = mw.getTranslationEnd();

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
                List<Token> tokenList = StaticUtils.tokenizeText(selectionText);

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
        Locale locale = CommandThread.core.getProjectProperties().getTargetLanguage().getLocale();

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
}
