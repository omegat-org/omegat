/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2012 Jean-Christophe Helary
               2015 Aaron Madlon-Kay
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

package org.omegat.gui.dictionaries;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.dictionaries.DictionariesManager;
import org.omegat.core.dictionaries.DictionaryEntry;
import org.omegat.core.dictionaries.IDictionaryFactory;
import org.omegat.core.events.IEditorEventListener;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.IMainWindow;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.Java8Compat;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.IPaneMenu;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.Styles.EditorColor;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a Dictionaries pane that displays dictionaries entries.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Jean-Christophe Helary
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class DictionariesTextArea extends EntryInfoThreadPane<List<DictionaryEntry>>
        implements IDictionaries, IPaneMenu {

    private static final String EXPLANATION = OStrings.getString("GUI_DICTIONARYWINDOW_explanation");
    private static final int TEXT_BATCH = 30;

    protected final DictionariesManager manager = new DictionariesManager(this);

    protected final List<String> displayedWords = new ArrayList<>();

    protected ITokenizer tokenizer;

    private final DockableScrollPane scrollPane;

    public DictionariesTextArea(IMainWindow mw) {
        super(true);

        setContentType("text/html");
        ((HTMLDocument) getDocument()).setPreservesUnknownTags(false);
        setFont(getFont());

        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Dictionary");
        scrollPane = new DockableScrollPane("DICTIONARY", title, this, true);
        mw.addDockable(scrollPane);

        addMouseListener(mouseCallback);

        setEditable(false);
        StaticUIUtils.makeCaretAlwaysVisible(this);
        setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        CoreEvents.registerEditorEventListener(new IEditorEventListener() {
            public void onNewWord(String newWord) {
                callDictionary(newWord);
            }
        });

        Core.getEditor().registerPopupMenuConstructors(750, new DictionaryPopup());

        Preferences.addPropertyChangeListener(Preferences.DICTIONARY_FUZZY_MATCHING, e -> refresh());
        Preferences.addPropertyChangeListener(Preferences.DICTIONARY_AUTO_SEARCH, e -> refresh());
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        Document doc = getDocument();
        if (!(doc instanceof HTMLDocument)) {
            return;
        }
        StyleSheet styleSheet = ((HTMLDocument) doc).getStyleSheet();
        styleSheet.addRule("body { font-family: " + font.getName() + "; "
                + " font-size: " + font.getSize() + "; "
                + " font-style: " + (font.getStyle() == Font.BOLD ? "bold"
                        : font.getStyle() == Font.ITALIC ? "italic" : "normal") + "; "
                + " color: " + EditorColor.COLOR_FOREGROUND.toHex() + "; "
                + " background: " + EditorColor.COLOR_BACKGROUND.toHex() + "; "
                + " }");
    }

    @Override
    protected void onProjectOpen() {
        clear();
        IProject project = Core.getProject();
        tokenizer = project.getSourceTokenizer();
        manager.setIndexLanguage(project.getProjectProperties().getSourceLanguage());
        manager.setTokenizer(tokenizer);
        manager.start(new File(project.getProjectProperties().getDictRoot()));
    }

    @Override
    protected void onProjectClose() {
        clear();
        setText(EXPLANATION);
        manager.stop();
        tokenizer = null;
    }

    /** Clears up the pane. */
    @Override
    public void clear() {
        super.clear();
        displayedWords.clear();
    }

    /**
     * Move position in pane to the currently selected word.
     */
    protected void callDictionary(String word) {
        UIThreadsUtil.mustBeSwingThread();

        HTMLDocument doc = (HTMLDocument) getDocument();

        int i = displayedWords.indexOf(word.toLowerCase());
        if (i == -1) {
            return;
        }
        Element el = doc.getElement(Integer.toString(i));
        if (el == null) {
            return;
        }
        try {
            // rectangle to be visible
            Rectangle rect = Java8Compat.modelToView(this, el.getStartOffset());
            // show 2 lines
            if (rect != null) {
                rect.height *= 2;
                scrollRectToVisible(rect);
            }
        } catch (BadLocationException ex) {
            // shouldn't be throwed
        }
    }

    @Override
    public void onEntryActivated(SourceTextEntry newEntry) {
        scrollPane.stopNotifying();
        super.onEntryActivated(newEntry);
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        if (!Preferences.isPreferenceDefault(Preferences.DICTIONARY_AUTO_SEARCH, true)) {
            return;
        };
        new DictionaryEntriesSearchThread(newEntry).start();
    }

    @Override
    public void searchText(String text) {
        new DictionaryTextSearchThread(text).start();
    }

    /**
     * Refresh content on dictionary file changed.
     */
    public void refresh() {
        SourceTextEntry ste = Core.getEditor().getCurrentEntry();
        if (ste != null) {
            startSearchThread(ste);
        }
    }

    @Override
    protected void setFoundResult(final SourceTextEntry se, final List<DictionaryEntry> data) {
        UIThreadsUtil.mustBeSwingThread();

        clear();

        if (data == null) {
            return;
        }

        if (!data.isEmpty() && Preferences.isPreference(Preferences.NOTIFY_DICTIONARY_HITS)) {
            scrollPane.notify(true);
        }

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
            if (i % TEXT_BATCH == 0) {
                appendText(txt.toString());
                txt = new StringBuilder();
            }
        }
        appendText(txt.toString());
    }

    private void appendText(final String txt) {
        Document doc = getDocument();
        if (doc.getLength() == 0) {
            // Appending to an empty document results in treating HTML tags as
            // plain text for some reason
            setText(txt);
        } else {
            try {
                doc.insertString(doc.getLength(), txt, null);
            } catch (BadLocationException e) {
                Log.log(e);
            }
        }
    }

    protected final transient MouseAdapter mouseCallback = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e.getPoint());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e.getPoint());
            }
        }

        private void doPopup(Point p) {
            UIThreadsUtil.mustBeSwingThread();

            JPopupMenu popup = new JPopupMenu();
            int mousepos = Java8Compat.viewToModel(DictionariesTextArea.this, p);
            final String word = getWordAtOffset(mousepos);
            if (word != null) {
                JMenuItem item = popup.add(StringUtil.format(OStrings.getString("DICTIONARY_HIDE"), word));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        manager.addIgnoreWord(word);
                    };
                });
                popup.show(DictionariesTextArea.this, p.x, p.y);
            }
        }
    };

    private String getWordAtOffset(int offset) {
        HTMLDocument doc = (HTMLDocument) getDocument();
        for (int i = 0; i < displayedWords.size(); i++) {
            Element el = doc.getElement(Integer.toString(i));
            if (el == null) {
                continue;
            }
            if (el.getStartOffset() <= offset && el.getEndOffset() >= offset) {
                return displayedWords.get(i);
            }
        }
        return null;
    }

    /**
     * Thread for search data in dictionaries.
     */
    public class DictionaryEntriesSearchThread extends EntryInfoSearchThread<List<DictionaryEntry>> {
        protected final String src;
        protected final ITokenizer tok;

        public DictionaryEntriesSearchThread(final SourceTextEntry newEntry) {
            super(DictionariesTextArea.this, newEntry);
            src = newEntry.getSrcText();
            tok = tokenizer;
        }

        @Override
        protected List<DictionaryEntry> search() {
            if (tok == null) {
                return null;
            }

            // Just get the words. Stemming and other complex lookup logic is
            // left to DictionaryManager.
            List<String> words = Stream.of(tok.tokenizeWordsToStrings(src, StemmingMode.NONE)).distinct()
                    .collect(Collectors.toList());

            List<DictionaryEntry> result = manager.findWords(words);
            Collections.sort(result);

            return result;
        }
    }

    /**
     * Thread for user requested dictionary search.
     */
    public class DictionaryTextSearchThread extends Thread {

        private final String src;
        private final ITokenizer tok;
        private final DictionariesTextArea pane;

        public DictionaryTextSearchThread(final String text) {
            src = text;
            tok = tokenizer;
            pane = DictionariesTextArea.this;
        }

        protected List<DictionaryEntry> search() {
            if (tok == null) {
                return null;
            }

            List<String> words = Stream.of(tok.tokenizeWordsToStrings(src, StemmingMode.NONE)).distinct()
                    .collect(Collectors.toList());

            List<DictionaryEntry> result = manager.findWords(words);
            Collections.sort(result);

            return result;
        }

        @Override
        public void run() {

            List<DictionaryEntry> result = null;
            Exception error = null;
            try {
                result = search();
            } catch (Exception ex) {
                error = ex;
                Log.log(ex);
            }

            final List<DictionaryEntry> fresult = result;
            final Exception ferror = error;
            SwingUtilities.invokeLater(() -> {
                if (ferror != null) {
                    pane.setError(ferror);
                } else {
                    pane.setFoundResult(currentlyProcessedEntry, fresult);
                }
            });
        }
    }

    @Override
    public void addDictionaryFactory(IDictionaryFactory factory) {
        manager.addDictionaryFactory(factory);
    }

    @Override
    public void removeDictionaryFactory(IDictionaryFactory factory) {
        manager.removeDictionaryFactory(factory);
    }

    @Override
    public void populatePaneMenu(JPopupMenu menu) {
        final JMenuItem notify = new JCheckBoxMenuItem(
                OStrings.getString("GUI_DICTIONARYWINDOW_SETTINGS_NOTIFICATIONS"));
        notify.setSelected(Preferences.isPreference(Preferences.NOTIFY_DICTIONARY_HITS));
        notify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Preferences.setPreference(Preferences.NOTIFY_DICTIONARY_HITS, notify.isSelected());
            }
        });
        menu.add(notify);
    }
}
