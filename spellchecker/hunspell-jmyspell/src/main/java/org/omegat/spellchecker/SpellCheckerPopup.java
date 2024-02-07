/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Didier Briel
               2010 Wildrich Fourie
               2011 Alex Buloichik, Didier Briel
               2015 Aaron Madlon-Kay
               2023 Thomas Cordonnier
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

package org.omegat.spellchecker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Token;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * create the spell checker popup menu - suggestions for a wrong word, add
 * and ignore. Works only for the active segment, for the translation
 */
public class SpellCheckerPopup implements IPopupMenuConstructor {
    protected final IEditor ec;

    public SpellCheckerPopup(IEditor ec) {
        this.ec = ec;
    }

    public void addItems(JPopupMenu menu, final JTextComponent comp, int mousepos,
                         boolean isInActiveEntry, boolean isInActiveTranslation, SegmentBuilder sb) {
        if (!ec.getSettings().isAutoSpellChecking()) {
            // spellchecker disabled
            return;
        }
        if (!isInActiveTranslation) {
            // there is no need to display suggestions
            return;
        }

        // Use the project's target tokenizer to determine the word that was right-clicked.
        // EditorUtils.getWordEnd() and getWordStart() use Java's built-in BreakIterator
        // under the hood, which leads to inconsistent results when compared to other spell-
        // checking functionality in OmegaT.
        String translation = ec.getCurrentTranslation();
        Token tok = null;
        int relOffset = ((EditorController) ec).getPositionInEntryTranslation(mousepos);
        for (Token t : Core.getProject().getTargetTokenizer().tokenizeWords(translation, ITokenizer.StemmingMode.NONE)) {
            if (t.getOffset() <= relOffset && relOffset < t.getOffset() + t.getLength()) {
                tok = t;
                break;
            }
        }

        if (tok == null) {
            return;
        }

        final String word = tok.getTextFromString(translation);
        // The wordStart must be the absolute offset in the Editor document.
        final int wordStart = mousepos - relOffset + tok.getOffset();
        final int wordLength = tok.getLength();
        final AbstractDocument xlDoc = (AbstractDocument) comp.getDocument();

        if (!Core.getSpellChecker().isCorrect(word)) {
            // get the suggestions and create a menu
            List<String> suggestions = Core.getSpellChecker().suggest(word);

            // the suggestions
            for (final String replacement : suggestions) {
                JMenuItem item = menu.add(replacement);
                item.addActionListener(new ActionListener() {
                    // the action: replace the word with the selected
                    // suggestion
                    public void actionPerformed(ActionEvent e) {
                        try {
                            int pos = comp.getCaretPosition();
                            xlDoc.replace(wordStart, wordLength, replacement, null);
                            comp.setCaretPosition(pos);
                        } catch (BadLocationException exc) {
                            Log.log(exc);
                        }
                    }
                });
            }

            // what if no action is done?
            if (suggestions.isEmpty()) {
                JMenuItem item = menu.add(Mnemonics.removeMnemonics(OStrings.getString("SC_NO_SUGGESTIONS")));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // just hide the menu
                    }
                });
            }

            menu.addSeparator();

            // let us ignore it
            JMenuItem item = menu.add(Mnemonics.removeMnemonics(OStrings.getString("SC_IGNORE_ALL")));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addIgnoreWord(word, wordStart, false);
                }
            });

            // or add it to the dictionary
            item = menu.add(Mnemonics.removeMnemonics(OStrings.getString("SC_ADD_TO_DICTIONARY")));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addIgnoreWord(word, wordStart, true);
                }
            });

            menu.addSeparator();

        }
    }

    /**
     * add a new word to the spell checker or ignore a word
     *
     * @param word   : the word in question
     * @param offset : the offset of the word in the editor
     * @param add    : true for add, false for ignore
     */
    protected void addIgnoreWord(final String word, final int offset, final boolean add) {
        UIThreadsUtil.mustBeSwingThread();

        if (add) {
            Core.getSpellChecker().learnWord(word);
        } else {
            Core.getSpellChecker().ignoreWord(word);
        }

        ec.remarkOneMarker(SpellCheckerMarker.class.getName());
    }
}
