/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel and Tiago Saboga
               2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

import org.omegat.core.Core;
import org.omegat.core.data.CommandThread;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Spell checking processing for editor.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Tiago Saboga
 * @author Zoltan Bartko
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
class EditorSpellChecking {
    private static final String IMPOSSIBLE = "Should not have happened, " + // NOI18N
            "report to http://sf.net/tracker/?group_id=68187&atid=520347"; // NOI18N

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
     */
    protected static void checkSpelling(final boolean full, final EditorController controller,
            final EditorTextArea editor) {
        try {
            // here we are. Assuming that the caret has already been set
            // to a position within the edited segment
            int offset = editor.getCaretPosition();

            int start = controller.getTranslationStart();
            int end = controller.getTranslationEnd();

            int spellcheckStart;
            int spellcheckEnd;

            if (full) {
                spellcheckStart = start;
                spellcheckEnd = end;
            } else {
                // the previous word start and end
                int prevWord = Utilities.getPreviousWord(editor, offset);
                int endPrevWord = Utilities.getWordEnd(editor, prevWord);

                // the previous next word start and end
                int nextWord = Utilities.getNextWord(editor, offset);
                int endNextWord = Utilities.getWordEnd(editor, nextWord);

                spellcheckStart = (prevWord < start ? start : prevWord);
                spellcheckEnd = (endNextWord > end ? end : endNextWord);
            }

            String spellcheckBase = editor.getText(spellcheckStart, spellcheckEnd - spellcheckStart);

            AttributeSet attributes;
            AttributeSet correctAttributes = controller.getSettings().getTranslatedAttributeSet();
            AttributeSet wrongAttributes = Styles.applyStyles(correctAttributes, Styles.MISSPELLED);

            AbstractDocument xlDoc = (AbstractDocument) editor.getDocument();

            // to make the undo framework work
            xlDoc.removeUndoableEditListener(editor.undoManager);

            // first, repaint the whole area as if it were correct
            xlDoc.replace(spellcheckStart, spellcheckEnd - spellcheckStart, spellcheckBase, correctAttributes);

            // iterate!
            for (Token token : Core.getTokenizer().tokenizeWordsForSpelling(spellcheckBase)) {
                String word = token.getTextFromString(spellcheckBase);
                // is it correct?
                if (!Core.getSpellChecker().isCorrect(word)) {
                    attributes = wrongAttributes;
                } else {
                    attributes = correctAttributes;
                }

                xlDoc.replace(spellcheckStart + token.getOffset(), token.getLength(), word, attributes);
            }

            // put the caret position where it belongs to
            editor.setCaretPosition(offset);

            // put the undo manager back where it was
            xlDoc.addUndoableEditListener(editor.undoManager);

        } catch (BadLocationException ble) {
            // so now what?
        }
    }

    /**
     * create the spell checker popup menu - suggestions for a wrong word, add
     * and ignore. Works only for the active segment, for the translation
     * 
     * @param point :
     *                where should the popup be shown
     */
    protected static boolean createSpellCheckerPopUp(final Point point, final EditorController controller,
            final EditorTextArea editor) {
        // where is the mouse
        int mousepos = editor.viewToModel(point);

        if (mousepos < controller.getTranslationStart() || mousepos > controller.getTranslationEnd())
            return false;

        try {
            // find the word boundaries
            final int wordStart = Utilities.getWordStart(editor, mousepos);
            final int wordEnd = Utilities.getWordEnd(editor, mousepos);

            final String word = editor.getText(wordStart, wordEnd - wordStart);

            final AbstractDocument xlDoc = (AbstractDocument) editor.getDocument();

            if (!Core.getSpellChecker().isCorrect(word)) {
                // get the suggestions and create a menu
                List<String> suggestions = Core.getSpellChecker().suggest(word);

                // create the menu
                editor.popup = new JPopupMenu();

                // the suggestions
                for (final String replacement : suggestions) {
                    JMenuItem item = editor.popup.add(replacement);
                    item.addActionListener(new ActionListener() {
                        // the action: replace the word with the selected
                        // suggestion
                        public void actionPerformed(ActionEvent e) {
                            try {
                                int pos = editor.getCaretPosition();
                                xlDoc.replace(wordStart, word.length(), replacement, controller.getSettings()
                                        .getTranslatedAttributeSet());
                                editor.setCaretPosition(pos);
                            } catch (BadLocationException exc) {
                                System.err.println(exc);
                            }
                        }
                    });
                }

                // what if no action is done?
                if (suggestions.size() == 0) {
                    JMenuItem item = editor.popup.add(OStrings.getString("SC_NO_SUGGESTIONS"));
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            // just hide the menu
                        }
                    });
                }

                editor.popup.add(new JSeparator());

                final ChangeUserDictionary changeHandler = new ChangeUserDictionary(controller, editor);
                // let us ignore it
                JMenuItem item = editor.popup.add(OStrings.getString("SC_IGNORE_ALL"));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        changeHandler.addIgnoreWord(word, wordStart, false);
                    }
                });

                // or add it to the dictionary
                item = editor.popup.add(OStrings.getString("SC_ADD_TO_DICTIONARY"));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        changeHandler.addIgnoreWord(word, wordStart, true);
                    }
                });

                editor.popup.show(editor, (int) point.getX(), (int) point.getY());

            }

        } catch (BadLocationException ex) {
            Log.logRB(IMPOSSIBLE);
            Log.log(ex);
        }
        return true;
    }

    private static class ChangeUserDictionary {
        private final EditorController controller;
        private final EditorTextArea editor;

        public ChangeUserDictionary(final EditorController controller, final EditorTextArea editor) {
            this.controller = controller;
            this.editor = editor;
        }

        /**
         * add a new word to the spell checker or ignore a word
         * 
         * @param word :
         *                the word in question
         * @param offset :
         *                the offset of the word in the editor
         * @param add :
         *                true for add, false for ignore
         */
        protected void addIgnoreWord(final String word, final int offset, final boolean add) {
            UIThreadsUtil.mustBeSwingThread();
            
                if (add) {
                    Core.getSpellChecker().learnWord(word);
                } else {
                    Core.getSpellChecker().ignoreWord(word);
                }

                // redraw document

                AbstractDocument xlDoc = (AbstractDocument) editor.getDocument();
                try {
                    // redraw the word in question
                    xlDoc.replace(offset, word.length(), word, controller.getSettings().getTranslatedAttributeSet());

                    // Replace the errors in the rest of the document

                    // which is the current segment in the document and what is
                    // the
                    // length?
                    int startOffset = controller.m_segmentStartOffset;
                    int currentTrLen = controller.getTranslationEnd() - controller.getTranslationStart();
                    int totalLen = controller.m_sourceDisplayLength + OConsts.segmentStartStringFull.length()
                            + currentTrLen + OConsts.segmentEndStringFull.length() + 2;

                    int localCur = controller.m_curEntryNum - controller.m_xlFirstEntry;
                    DocumentSegment docSeg = controller.m_docSegList[localCur];
                    docSeg.length = totalLen;

                    // the segment counter - local
                    int localCnt = 0;

                    // the caret offset in the cycle
                    int segOffset = 0;

                    // iterate through the entries in this file
                    for (int i = controller.m_xlFirstEntry; i <= controller.m_xlLastEntry; i++) {
                        SourceTextEntry ste = Core.getDataEngine().getAllEntries().get(i);
                        if (ste.isTranslated() && localCnt != localCur) {
                            // only translated and inactive made it
                            int translationStartOffset = segOffset;
                            if (controller.getSettings().isDisplaySegmentSources()) {
                                // don't forget sources if they are displayed
                                translationStartOffset += ste.getSrcText().length() + 1;
                            }

                            String translation = ste.getTranslation();

                            // is the word in the string?
                            if (translation.indexOf(word) != -1) {
                                // split the text into tokens. If there is a
                                // match,
                                // redraw it
                                for (Token token : Core.getTokenizer().tokenizeWordsForSpelling(translation)) {
                                    String tokenText = token.getTextFromString(translation);
                                    // redraw?
                                    if (tokenText.equals(word)) {
                                        xlDoc.replace(translationStartOffset + token.getOffset(), word.length(), word,
                                                controller.getSettings().getTranslatedAttributeSet());
                                    }
                                }
                            }
                        }

                        // next segment
                        segOffset += controller.m_docSegList[localCnt++].length;
                    }

                } catch (BadLocationException ex) {
                    Log.logRB(IMPOSSIBLE);
                    Log.log(ex);
                }
            }
    }

    /**
     * Checks the spelling of the segment.
     * 
     * @param start :
     *                the starting position
     * @param text :
     *                the text to check
     */
    protected static List<Token> checkSpelling(int start, String text, final EditorController controller,
            final EditorTextArea editor) {
            // we have the translation and it should be spellchecked
            Token[] wordlist = Core.getTokenizer().tokenizeWordsForSpelling(text);
            List<Token> wrongWordList = new ArrayList<Token>();

            AbstractDocument xlDoc = (AbstractDocument) editor.getDocument();
            AttributeSet attributes = controller.getSettings().getTranslatedAttributeSet();

            for (Token token : wordlist) {
                int tokenStart = token.getOffset();
                int tokenEnd = tokenStart + token.getLength();
                String word = text.substring(tokenStart, tokenEnd);

                if (!Core.getSpellChecker().isCorrect(word)) {
                    try {
                        xlDoc.replace(start + tokenStart, token.getLength(), word, Styles.applyStyles(attributes,
                                Styles.MISSPELLED));
                    } catch (BadLocationException ble) {
                        // Log.log(IMPOSSIBLE);
                        Log.log(ble);
                    }
                    wrongWordList.add(token);
                }
            }
            return wrongWordList;
    }
}
