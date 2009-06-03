/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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

package org.omegat.gui.dictionaries;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringEntry;
import org.omegat.core.dictionaries.DictionariesManager;
import org.omegat.core.dictionaries.DictionaryEntry;
import org.omegat.core.events.IEditorEventListener;
import org.omegat.core.matching.ITokenizer;
import org.omegat.gui.common.EntryInfoPane;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a Dictionaries pane that displays dictionaries entries.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class DictionariesTextArea extends EntryInfoPane<List<DictionaryEntry>> {

    protected final DictionariesManager manager = new DictionariesManager(this);

    protected final List<String> displayedWords = new ArrayList<String>();

    public DictionariesTextArea() {
        super(true);

        setContentType("text/html");

        // setEditable(false);
        String title = OStrings
                .getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Dictionary");
        Core.getMainWindow().addDockable(
                new DockableScrollPane("DICTIONARY", title, this, true));

        addMouseListener(mouseCallback);

        CoreEvents.registerEditorEventListener(new IEditorEventListener() {
            public void onNewWord(String newWord) {
                callDictionary(newWord);
            }
        });
    }

    @Override
    protected void onProjectOpen() {
        clear();
        manager.start(Core.getProject().getProjectProperties().getDictRoot());
    }

    @Override
    protected void onProjectClose() {
        clear();
        manager.stop();
    }

    /** Clears up the pane. */
    protected void clear() {
        UIThreadsUtil.mustBeSwingThread();

        setText("");
    }

    /**
     * Move position in pane to the currently selected word.
     */
    protected void callDictionary(String word) {
        UIThreadsUtil.mustBeSwingThread();

        HTMLDocument doc = (HTMLDocument) getDocument();

        int i = displayedWords.indexOf(word.toLowerCase());
        if (i >= 0) {
            final Element el = doc.getElement(Integer.toString(i));
            if (el != null) {
                try {
                    // rectangle to be visible
                    Rectangle rect = getUI().modelToView(this,
                            el.getStartOffset());
                    // show 2 lines
                    if (rect != null) {
                        rect.height *= 2;
                        scrollRectToVisible(rect);
                    }
                } catch (BadLocationException ex) {
                    // shouldn't be throwed
                }
            }
        }
    }

    @Override
    protected void startSearchThread(StringEntry newEntry) {
        new DictionaryEntriesSearchThread(newEntry).start();
    }

    /**
     * Refresh content on dictionary file changed.
     */
    public void refresh() {
        SourceTextEntry ste = Core.getEditor().getCurrentEntry();
        if (ste != null) {
            startSearchThread(ste.getStrEntry());
        }
    }

    @Override
    protected void setFoundResult(final List<DictionaryEntry> data) {
        UIThreadsUtil.mustBeSwingThread();

        displayedWords.clear();
        StringBuilder txt = new StringBuilder();
        boolean wasPrev = false;
        int i = 0;
        for (DictionaryEntry de : data) {
            if (wasPrev) {
                txt.append("<br><hr>");
            } else {
                wasPrev = true;
            }
            txt.append("<b><span id=\"" + i + "\">");
            txt.append(de.getWord());
            txt.append("</span></b>");
            txt.append(" - ").append(de.getArticle());

            displayedWords.add(de.getWord().toLowerCase());
            i++;
        }
        setText(txt.toString());
        setCaretPosition(0);
    }

    protected final MouseAdapter mouseCallback = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                UIThreadsUtil.mustBeSwingThread();

                JPopupMenu popup = new JPopupMenu();
                int mousepos = viewToModel(e.getPoint());
                HTMLDocument doc = (HTMLDocument) getDocument();
                for (int i = 0; i < displayedWords.size(); i++) {
                    Element el = doc.getElement(Integer.toString(i));
                    if (el != null) {
                        if (el.getStartOffset() <= mousepos
                                && el.getEndOffset() >= mousepos) {
                            final String w = displayedWords.get(i);
                            String hideW = StaticUtils.format(OStrings
                                    .getString("DICTIONARY_HIDE"), w);
                            JMenuItem item = popup.add(hideW);
                            item.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    manager.addIgnoreWord(w);
                                };
                            });
                        }
                    }
                }
                popup.show(DictionariesTextArea.this, e.getX(), e.getY());
            }
        }
    };

    /**
     * Thread for search data in dictionaries.
     */
    public class DictionaryEntriesSearchThread extends
            EntryInfoSearchThread<List<DictionaryEntry>> {
        protected final String src;

        public DictionaryEntriesSearchThread(final StringEntry newEntry) {
            super(DictionariesTextArea.this, newEntry);
            src = newEntry.getSrcText();
        }

        @Override
        protected List<DictionaryEntry> search() {
            Token[] tokenList = Core.getTokenizer().tokenizeWords(src,
                    ITokenizer.StemmingMode.NONE);
            Set<String> words = new TreeSet<String>();
            for (Token tok : tokenList) {
                if (isEntryChanged()) {
                    return null;
                }
                String w = src.substring(tok.getOffset(), tok.getOffset()
                        + tok.getLength());

                words.add(w);
            }
            List<DictionaryEntry> result = manager.findWords(words);

            Collections.sort(result, new Comparator<DictionaryEntry>() {
                public int compare(DictionaryEntry o1, DictionaryEntry o2) {
                    return o1.getWord().compareTo(o2.getWord());
                }
            });
            return result;
        }
    }
}
