/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Didier Briel
               2010 Wildrich Fourie
               2011 Alex Buloichik, Didier Briel
               2015 Aaron Madlon-Kay
               2023 Thomas Cordonnier
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.spellchecker.SpellCheckerMarker;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.Token;
import org.omegat.util.gui.MenuItemPager;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Some standard editor popups.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Wildrich Fourie
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Thomas Cordonnier
 */
public final class EditorPopups {
    public static void init(EditorController ec) {
        ec.registerPopupMenuConstructors(100, new SpellCheckerPopup(ec));
        ec.registerPopupMenuConstructors(200, new GoToSegmentPopup(ec));
        ec.registerPopupMenuConstructors(400, new DefaultPopup());
        ec.registerPopupMenuConstructors(500, new DuplicateSegmentsPopup(ec));
        ec.registerPopupMenuConstructors(600, new EmptyNoneTranslationPopup(ec));
        ec.registerPopupMenuConstructors(700, new InsertTagsPopup(ec));
        ec.registerPopupMenuConstructors(800, new InsertBidiPopup(ec));
    }

    private EditorPopups() {
    }
    
    /**
     * create the spell checker popup menu - suggestions for a wrong word, add
     * and ignore. Works only for the active segment, for the translation
     *
     * @param point
     *            : where should the popup be shown
     */
    public static class SpellCheckerPopup implements IPopupMenuConstructor {
        protected final EditorController ec;

        public SpellCheckerPopup(EditorController ec) {
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
            int relOffset = ec.getPositionInEntryTranslation(mousepos);
            for (Token t : Core.getProject().getTargetTokenizer().tokenizeWords(translation, StemmingMode.NONE)) {
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
         * @param word
         *            : the word in question
         * @param offset
         *            : the offset of the word in the editor
         * @param add
         *            : true for add, false for ignore
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

    public static class DefaultPopup implements IPopupMenuConstructor {
        /**
         * Creates the Cut, Copy and Paste menu items
         */
        public void addItems(JPopupMenu menu, final JTextComponent comp, int mousepos,
                boolean isInActiveEntry, boolean isInActiveTranslation, SegmentBuilder sb) {
            final String selText = comp.getSelectedText();
            final Clipboard omClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = omClipboard.getContents(this);

            boolean cutEnabled = false;
            boolean copyEnabled = false;
            boolean pasteEnabled = false;

            // Calc enabled/disabled
            if (selText != null && comp.getSelectionStart() <= mousepos && mousepos <= comp.getSelectionEnd()) {
                // only on selected text
                if (isInActiveTranslation) {
                    // cut only in editable zone
                    cutEnabled = true;
                }
                // copy in any place
                copyEnabled = true;
            }
            if (contents != null && isInActiveTranslation) {
                pasteEnabled = true;
            }

            // Cut
            JMenuItem cutContextItem = menu.add(Mnemonics.removeMnemonics(OStrings.getString("CCP_CUT")));
            if (cutEnabled) {
                cutContextItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        comp.cut();
                    }
                });
            } else {
                cutContextItem.setEnabled(false);
            }

            // Copy
            JMenuItem copyContextItem = menu.add(Mnemonics.removeMnemonics(OStrings.getString("CCP_COPY")));
            if (copyEnabled) {
                copyContextItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        comp.copy();
                    }
                });
            } else {
                copyContextItem.setEnabled(false);
            }

            // Paste
            JMenuItem pasteContextItem = menu.add(Mnemonics.removeMnemonics(OStrings.getString("CCP_PASTE")));
            if (pasteEnabled) {
                pasteContextItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        comp.paste();
                    }
                });
            } else {
                pasteContextItem.setEnabled(false);
            }

            menu.addSeparator();

            // Add glossary entry
            JMenuItem item = menu.add(Mnemonics.removeMnemonics(OStrings.getString("GUI_GLOSSARYWINDOW_addentry")));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Core.getGlossary()
                            .showCreateGlossaryEntryDialog(Core.getMainWindow().getApplicationFrame());
                 }
            });

            menu.addSeparator();

        }
    }

    public static class GoToSegmentPopup implements IPopupMenuConstructor {
        protected final EditorController ec;

        public GoToSegmentPopup(EditorController ec) {
            this.ec = ec;
        }

        /**
         * creates a popup menu for inactive segments - with an item allowing to
         * go to the given segment.
         */
        public void addItems(JPopupMenu menu, final JTextComponent comp, final int mousepos,
                boolean isInActiveEntry, boolean isInActiveTranslation, SegmentBuilder sb) {
            if (isInActiveEntry) {
                return;
            }

            JMenuItem item = menu.add(Mnemonics.removeMnemonics(OStrings.getString("MW_PROMPT_SEG_NR_TITLE")));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    comp.setCaretPosition(mousepos);
                    ec.goToSegmentAtLocation(comp.getCaretPosition());
                }
            });
            menu.addSeparator();
        }
    }

    public static class DuplicateSegmentsPopup implements IPopupMenuConstructor {
        protected final EditorController ec;

        public DuplicateSegmentsPopup(EditorController ec) {
            this.ec = ec;
        }

        @Override
        public void addItems(JPopupMenu menu, JTextComponent comp,
                int mousepos, boolean isInActiveEntry,
                boolean isInActiveTranslation, SegmentBuilder sb) {
            if (!isInActiveEntry) {
                return;
            }
            SourceTextEntry ste = ec.getCurrentEntry();
            List<SourceTextEntry> dups = ste.getDuplicates();
            if (dups.isEmpty()) {
                return;
            }
            JMenuItem header = menu
                    .add(StringUtil.format(Mnemonics.removeMnemonics(OStrings.getString("MW_GO_TO_DUPLICATE_HEADER")), 
                    dups.size()));
            header.setEnabled(false);
            MenuItemPager pager = new MenuItemPager(menu);
            for (SourceTextEntry entry : dups) {
                int entryNum = entry.entryNum();
                String label = StringUtil.format(Mnemonics.removeMnemonics(OStrings.getString("MW_GO_TO_DUPLICATE_ITEM")), 
                    entryNum);
                JMenuItem item = pager.add(new JMenuItem(label));
                item.addActionListener(e -> ec.gotoEntry(entryNum));
            }
            menu.addSeparator();
        }
    }

    public static class EmptyNoneTranslationPopup implements IPopupMenuConstructor {
        protected final EditorController ec;

        public EmptyNoneTranslationPopup(EditorController ec) {
            this.ec = ec;
        }

        /**
         * creates a popup menu to remove translation or set empty translation
         */
        public void addItems(JPopupMenu menu, final JTextComponent comp, final int mousepos,
                boolean isInActiveEntry, boolean isInActiveTranslation, SegmentBuilder sb) {
            if (!isInActiveEntry) {
                return;
            }

            menu.add(Mnemonics.removeMnemonics(OStrings.getString("TRANS_POP_EMPTY_TRANSLATION"))).addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            ec.registerEmptyTranslation();
                        }
                    });
            menu.add(Mnemonics.removeMnemonics(OStrings.getString("TRANS_POP_REMOVE_TRANSLATION"))).addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            ec.registerUntranslated();
                        }
                    });
            menu.add(Mnemonics.removeMnemonics(OStrings.getString("TRANS_POP_IDENTICAL_TRANSLATION"))).addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            ec.registerIdenticalTranslation();
                        }
                    });
            menu.addSeparator();
        }
    }

    public static class InsertTagsPopup implements IPopupMenuConstructor {
        protected final EditorController ec;

        public InsertTagsPopup(EditorController ec) {
            this.ec = ec;
        }

        public void addItems(JPopupMenu menu, final JTextComponent comp, final int mousepos, boolean isInActiveEntry,
                boolean isInActiveTranslation, SegmentBuilder sb) {
            if (!isInActiveTranslation) {
                return;
            }

            for (final Tag tag : TagUtil.getAllTagsMissingFromTarget()) {
                JMenuItem item = menu.add(StringUtil.format(Mnemonics.removeMnemonics(
                    OStrings.getString("TF_MENU_EDIT_TAG_INSERT_N")), tag.tag));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Core.getEditor().insertTag(tag.tag);
                    }
                });
            }
            menu.addSeparator();
        }
    }

    public static class InsertBidiPopup implements IPopupMenuConstructor {
        protected final EditorController ec;
        protected String[] names = new String[] { "TF_MENU_EDIT_INSERT_CHARS_LRM",
                "TF_MENU_EDIT_INSERT_CHARS_RLM", "TF_MENU_EDIT_INSERT_CHARS_LRE",
                "TF_MENU_EDIT_INSERT_CHARS_RLE", "TF_MENU_EDIT_INSERT_CHARS_PDF" };
        protected String[] inserts = new String[] { "\u200E", "\u200F", "\u202A", "\u202B", "\u202C" };

        public InsertBidiPopup(EditorController ec) {
            this.ec = ec;
        }

        public void addItems(JPopupMenu menu, final JTextComponent comp, final int mousepos,
                boolean isInActiveEntry, boolean isInActiveTranslation, SegmentBuilder sb) {
            if (!isInActiveTranslation) {
                return;
            }
            JMenu submenu = new JMenu(Mnemonics.removeMnemonics(OStrings.getString("TF_MENU_EDIT_INSERT_CHARS")));
            for (int i = 0; i < names.length; i++) {
                JMenuItem item = new JMenuItem(Mnemonics.removeMnemonics(OStrings.getString(names[i])));
                final String insertText = inserts[i];
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Core.getEditor().insertText(insertText);
                    }
                });
                submenu.add(item);
            }
            menu.add(submenu);
        }
    }
}
