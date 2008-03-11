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

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.util.Log;
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

            mw.activateEntry();
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
            mw.activateEntry();
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
            mw.activateEntry();
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
