/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2012 Jean-Christophe Helary
               2015 Aaron Madlon-Kay
               2021 Aaron Madlon-Kay, Hiroshi Miura
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.dictionaries.DictionariesManager;
import org.omegat.core.dictionaries.DictionaryEntry;
import org.omegat.core.dictionaries.IDictionary;
import org.omegat.core.dictionaries.IDictionaryFactory;
import org.omegat.core.events.IEditorEventListener;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.IMainWindow;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.ITokenizer.StemmingMode;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.ExternalBrowserLaunchingLinkListener;
import org.omegat.util.gui.IPaneMenu;
import org.omegat.util.gui.SoundActionListener;
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

    protected final DictionariesManager manager = new DictionariesManager(this);

    protected final List<String> displayedWords = new ArrayList<>();

    protected ITokenizer tokenizer;

    private final DockableScrollPane scrollPane;

    public DictionariesTextArea(IMainWindow mw) {
        super(true);

        setFont(getFont());
        initDocument();
        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Dictionary");
        scrollPane = new DockableScrollPane("DICTIONARY", title, this, true);
        mw.addDockable(scrollPane);

        addMouseListener(mouseCallback);
        addHyperlinkListener(new ExternalBrowserLaunchingLinkListener());
        addHyperlinkListener(new SoundActionListener());

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
    public void setFont(final Font font) {
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        super.setFont(font.deriveFont(attributes));
        if (displayedWords != null && !displayedWords.isEmpty()) {
            initDocument();
            refresh();
        }
    }

    @SuppressWarnings({"avoidinlineconditionals"})
    private void initDocument() {
        StyleSheet baseStyleSheet = new StyleSheet();
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        baseStyleSheet.addStyleSheet(htmlEditorKit.getStyleSheet()); // Add default styles
        Font font = getFont();
        int fontSize;
        if (Preferences.isPreferenceDefault(Preferences.DICTIONARY_USE_FONT, true)) {
            fontSize = font.getSize();
        } else {
            fontSize = Integer.parseInt(Preferences.getPreference(Preferences.TF_DICTIONARY_FONT_SIZE));
        }
        baseStyleSheet.addRule("body { font-family: " + font.getName() + "; font-size: " + fontSize + ";"
                + " font-style: " + (font.getStyle() == Font.ITALIC ? "italic" : "normal") + ";"
                + " font-weight: " + (font.getStyle() == Font.BOLD ? "bold" : "normal") + ";"
                + " color: " + EditorColor.COLOR_FOREGROUND.toHex() + ";"
                + " background: " + EditorColor.COLOR_BACKGROUND.toHex() + ";}"
                + " .word {font-size: 1.2em ; font-weight: bold;} .entry {line-height: 1.1;} "
                + " .article {font-size: 1.0em ;} .details {font-size: 0.8em ;}"
                + " .paragraph-start {font-size: 0.8em ; color: " + EditorColor.COLOR_PARAGRAPH_START + ";} "
                );
        htmlEditorKit.setStyleSheet(baseStyleSheet);
        setEditorKit(htmlEditorKit);
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

        // multiple entry can be existed for each query words,
        // try to get first one.
        int index = displayedWords.indexOf(word.toLowerCase());
        if (index == -1 && manager.doFuzzyMatching()) {
            // when not found and fuzzy matching allowed,retry with stemmed word
            String[] stemmed = manager.getStemmedWords(word);
            if (stemmed.length == 0) {
                return;
            }
            index = displayedWords.indexOf(stemmed[0]);
            if (index == -1) {
                return;
            }
        }
        Element el = doc.getElement(Integer.toString(index));
        if (el == null) {
            return;
        }
        int start = el.getStartOffset();
        // When trying to select the last word, Swing cannot make the rectangle
        // if the end is pointing past the last character.
        int end = Math.max(el.getEndOffset() - 1, start);
        try {
            // Start position of article
            Rectangle2D startRect = modelToView2D(start);
            // End position of article
            Rectangle2D endRect = modelToView2D(end);
            // To show maximum extent possible, scroll to end and then to start.
            // Scrolling to startRect.union(endRect) will not show the start
            // when initiating scroll from below the target article.
            if (endRect != null) {
                scrollRectToVisible(endRect.getBounds());
            }
            if (startRect != null) {
                scrollRectToVisible(startRect.getBounds());
            }
        } catch (BadLocationException ex) {
            Log.log(ex);
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
        }
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

        StringBuilder txt = new StringBuilder("<html>");
        boolean wasPrev = false;
        int i = 0;
        for (DictionaryEntry de : data) {
            if (wasPrev) {
                txt.append("<hr style=\"line-height: 1.3; color: #777\">");
            } else {
                wasPrev = true;
            }
            txt.append("<div id=\"").append(i).append("\" class=\"entry\">");
            txt.append("<b><span class=\"word\">");
            txt.append(de.getWord().replaceAll("\n", " ")).append("</span></b>");
            txt.append(" - <span class=\"article\">").append(de.getArticle()).append("</span>");
            txt.append("</div>");
            displayedWords.add(de.getQuery());
            i++;
        }
        txt.append("</html>");
        fastReplaceContent(txt.toString());
    }

    // Previously we were incrementally adding to the current document and later
    // batching updates to the current document, but it turns out that
    // recreating the document from scratch is actually faster for very large
    // content. See https://sourceforge.net/p/omegat/bugs/1068/
    private void fastReplaceContent(final String txt) {
        Document doc = getDocument();
        try {
            EditorKit editorKit = getEditorKit();
            doc = editorKit.createDefaultDocument();
            ((HTMLDocument) doc).setPreservesUnknownTags(false);
            editorKit.read(new StringReader(txt), doc, 0);
            setDocument(doc);
        } catch (IOException | BadLocationException  e) {
            Log.log(e);
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
            int mousepos = DictionariesTextArea.this.viewToModel2D(p);
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
    public void addDictionary(IDictionary dictionary) {
        manager.addOnlineDictionary(dictionary);
    }

    @Override
    public void removeDictionary(IDictionary dictionary) {
        manager.removeOnlineDictionary(dictionary);
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
