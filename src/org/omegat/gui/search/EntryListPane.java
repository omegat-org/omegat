/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006-2007 Henry Pijffers
               2010 Alex Buloichik, Didier Briel
               2014 Piotr Kulik
               2015 Yu Tang
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.search;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.search.SearchMatch;
import org.omegat.core.search.SearchResultEntry;
import org.omegat.core.search.Searcher;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.EditorController.CaretPosition;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.AlwaysVisibleCaret;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.Styles.EditorColor;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * EntryListPane displays translation segments and, upon doubleclick of a
 * segment, instructs the main UI to jump to that segment this replaces the
 * previous huperlink interface and is much more flexible in the fonts it
 * displays than the HTML text
 * 
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
@SuppressWarnings("serial")
class EntryListPane extends JTextPane {
    protected static final AttributeSet FOUND_MARK = Styles.createAttributeSet(Color.BLUE, null, true, null);
    protected static final int MARKS_PER_REQUEST = 100;
    protected static final String ENTRY_SEPARATOR = "---------\n";
    private static final String KEY_GO_TO_NEXT_SEGMENT = "gotoNextSegmentMenuItem";
    private static final String KEY_GO_TO_PREVIOUS_SEGMENT = "gotoPreviousSegmentMenuItem";
    private static final String KEY_TRANSFER_FOCUS = "transferFocus";
    private static final String KEY_TRANSFER_FOCUS_BACKWARD = "transferFocusBackward";
    private static final int ENTRY_LIST_INDEX_NO_ENTRIES  = -1;
    private static final int ENTRY_LIST_INDEX_END_OF_TEXT = -2;

    private static void bindKeyStrokesFromMainMenuShortcuts(InputMap map) {
        PropertiesShortcuts shortcuts = new PropertiesShortcuts(
                "/org/omegat/gui/main/MainMenuShortcuts.properties");
        // Add KeyStrokes Ctrl+N/P (Cmd+N/P for MacOS) to the map
        shortcuts.bindKeyStrokes(
                map,
                KEY_GO_TO_NEXT_SEGMENT, KEY_GO_TO_PREVIOUS_SEGMENT);
    }

    private static InputMap createDefaultInputMap(InputMap parent) {
        InputMap map = new InputMap();
        map.setParent(parent);
        bindKeyStrokesFromMainMenuShortcuts(map);

        // Add KeyStrokes: Enter, Ctrl+Enter (Cmd+Enter for MacOS)
        int CTRL_CMD_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                KEY_GO_TO_NEXT_SEGMENT);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, CTRL_CMD_MASK),
                KEY_GO_TO_PREVIOUS_SEGMENT);
        return map;
    }

    private static InputMap createDefaultInputMapUseTab(InputMap parent) {
        InputMap map = new InputMap();
        map.setParent(parent);
        bindKeyStrokesFromMainMenuShortcuts(map);

        // Add KeyStrokes: TAB, Shift+TAB, Ctrl+TAB (Cmd+TAB for MacOS), Ctrl+Shift+TAB (Cmd+Shift+TAB for MacOS)
        int CTRL_CMD_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
                KEY_GO_TO_NEXT_SEGMENT);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK),
                KEY_GO_TO_PREVIOUS_SEGMENT);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, CTRL_CMD_MASK),
                KEY_TRANSFER_FOCUS);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, CTRL_CMD_MASK | InputEvent.SHIFT_DOWN_MASK),
                KEY_TRANSFER_FOCUS_BACKWARD);
        return map;
    }

    public EntryListPane() {
        setDocument(new DefaultStyledDocument());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!autoSyncWithEditor && e.getClickCount() == 2 && m_entryList.size() > 0) {
                    getActiveDisplayedEntry().gotoEntryInEditor();
                }
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                boolean useTabForAdvance = Core.getEditor().getSettings().isUseTabForAdvance();
                if (EntryListPane.this.useTabForAdvance != useTabForAdvance) {
                    EntryListPane.this.useTabForAdvance = useTabForAdvance;
                    initInputMap(useTabForAdvance);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // do nothing
            }
        });

        addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                SwingUtilities.invokeLater(highlighter);

                if (autoSyncWithEditor) {
                    getActiveDisplayedEntry().gotoEntryInEditor();
                }
            }
        });

        initActions();
        useTabForAdvance = Core.getEditor().getSettings().isUseTabForAdvance();
        autoSyncWithEditor = Preferences.isPreferenceDefault(Preferences.SEARCHWINDOW_AUTO_SYNC, false);
        initInputMap(useTabForAdvance);
        setEditable(false);
        AlwaysVisibleCaret.apply(this);
    }

    private void initInputMap(boolean useTabForAdvance) {
        setFocusTraversalKeysEnabled(!useTabForAdvance);
        InputMap parent = getInputMap().getParent();
        InputMap newMap = useTabForAdvance ? createDefaultInputMapUseTab(parent)
                                           : createDefaultInputMap(parent);
        setInputMap(WHEN_FOCUSED, newMap);
    }

    void setAutoSyncWithEditor(boolean autoSyncWithEditor) {
        this.autoSyncWithEditor = autoSyncWithEditor;
        if (autoSyncWithEditor) {
            getActiveDisplayedEntry().gotoEntryInEditor();
        }
    }

    /**
     * Show search result for user
     */
    public void displaySearchResult(Searcher searcher, int numberOfResults) {
        UIThreadsUtil.mustBeSwingThread();

        m_searcher = searcher;

        this.numberOfResults = numberOfResults;

        currentlyDisplayedMatches = null;
        m_entryList.clear();
        m_offsetList.clear();
        m_firstMatchList.clear();

        if (searcher == null || searcher.getSearchResults() == null) {
            // empty marks - just reset
            setText("");
            return;
        }

        currentlyDisplayedMatches = new DisplayMatches(searcher.getSearchResults());

        highlighter.reset();
        SwingUtilities.invokeLater(highlighter);

        if (autoSyncWithEditor) {
            getActiveDisplayedEntry().gotoEntryInEditor();
        }
    }

    private int getActiveEntryListIndex() {
        int nrEntries = getNrEntries();

        if (nrEntries == 0) {
            // No entry
            return ENTRY_LIST_INDEX_NO_ENTRIES;
        }

        if (nrEntries > 0) {
            int pos = getSelectionStart();
            for (int i = 0; i < nrEntries; i++) {
                if (pos < m_offsetList.get(i)) {
                    return i;
                }
            }
        }

        return ENTRY_LIST_INDEX_END_OF_TEXT;
    }

    protected class DisplayMatches implements Runnable {
        protected final DefaultStyledDocument doc;

        private final List<SearchMatch> matches = new ArrayList<SearchMatch>();

        public DisplayMatches(final List<SearchResultEntry> entries) {
            UIThreadsUtil.mustBeSwingThread();

            this.doc = new DefaultStyledDocument();

            StringBuilder m_stringBuf = new StringBuilder();
            // display what's been found so far
            if (entries.size() == 0) {
                // no match
                addMessage(m_stringBuf, OStrings.getString("ST_NOTHING_FOUND"));
            }

            if (entries.size() >= numberOfResults) {
                addMessage(m_stringBuf, StaticUtils.format(OStrings.getString("SW_MAX_FINDS_REACHED"),
                        numberOfResults));
            }

            for (SearchResultEntry e : entries) {
                addEntry(m_stringBuf, e.getEntryNum(), e.getPreamble(), e.getSrcPrefix(), e.getSrcText(),
                        e.getTranslation(), e.getNote(), e.getSrcMatch(), e.getTargetMatch(), e.getNoteMatch());
            }

            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, m_stringBuf.toString(), null);
            } catch (Exception ex) {
                Log.log(ex);
            }
            setDocument(doc);
            setCaretPosition(0);

            setFont();

            if (matches.size() > 0) {
                SwingUtilities.invokeLater(this);
            }
        }

        // add entry text - remember what its number is and where it ends
        public void addEntry(StringBuilder m_stringBuf, int num, String preamble, String srcPrefix,
                String src, String loc, String note, SearchMatch[] srcMatches,
                SearchMatch[] targetMatches, SearchMatch[] noteMatches) {
            if (m_stringBuf.length() > 0)
                m_stringBuf.append(ENTRY_SEPARATOR);

            if (preamble != null && !preamble.equals(""))
                m_stringBuf.append(preamble + "\n");
            if (src != null && !src.equals("")) {
                m_stringBuf.append("-- ");
                if (srcPrefix != null) {
                    m_stringBuf.append(srcPrefix);
                }
                if (srcMatches != null) {
                    for (SearchMatch m : srcMatches) {
                        m.move(m_stringBuf.length());
                        matches.add(m);
                    }
                }
                m_stringBuf.append(src);
                m_stringBuf.append('\n');
            }
            if (loc != null && !loc.equals("")) {
                m_stringBuf.append("-- ");
                if (targetMatches != null) {
                    // Save first match position to select it in Editor pane later
                    if (num > 0) {
                        SearchMatch m = targetMatches[0];
                        m_firstMatchList.put(num, new CaretPosition(m.getStart(), m.getEnd()));
                    }

                    for (SearchMatch m : targetMatches) {
                        m.move(m_stringBuf.length());
                        matches.add(m);
                    }
                }
                m_stringBuf.append(loc);
                m_stringBuf.append('\n');
            }

            if (note != null && !note.equals("")) {
                m_stringBuf.append("= ");
                if (noteMatches != null) {
                    for (SearchMatch m : noteMatches) {
                        m.move(m_stringBuf.length());
                        matches.add(m);
                    }
                }
                m_stringBuf.append(note);
                m_stringBuf.append('\n');
            }

            m_entryList.add(num);
            m_offsetList.add(m_stringBuf.length());
        }

        @Override
        public void run() {
            UIThreadsUtil.mustBeSwingThread();

            if (currentlyDisplayedMatches != this) {
                // results changed - shouldn't mark old results
                return;
            }

            List<SearchMatch> display = matches.subList(0, Math.min(MARKS_PER_REQUEST, matches.size()));
            for (SearchMatch m : display) {
                doc.setCharacterAttributes(m.getStart(), m.getLength(), FOUND_MARK, true);
            }
            display.clear();

            if (matches.size() > 0) {
                SwingUtilities.invokeLater(this);
            }
        }
    }

    /**
     * Adds a message text to be displayed. Used for displaying messages that
     * aren't results.
     * 
     * @param message
     *            The message to display
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    private void addMessage(StringBuilder m_stringBuf, String message) {
        // Insert entry/message separator if necessary
        if (m_stringBuf.length() > 0)
            m_stringBuf.append(ENTRY_SEPARATOR);

        // Insert the message text
        m_stringBuf.append(message);
    }

    public void setFont() {
        String srcFont = Preferences.getPreference(OConsts.TF_SRC_FONT_NAME);
        if (!srcFont.equals("")) {
            int fontsize;
            try {
                fontsize = Integer.parseInt(Preferences.getPreference(OConsts.TF_SRC_FONT_SIZE));
            } catch (NumberFormatException nfe) {
                fontsize = 12;
            }
            setFont(new Font(srcFont, Font.PLAIN, fontsize));
        }
    }

    public void reset() {
        displaySearchResult(null, 0);
    }

    public int getNrEntries() {
        return m_entryList.size();
    }

    public List<Integer> getEntryList() {
        return m_entryList;
    }

    public Searcher getSearcher() {
        return m_searcher;
    }

    private void initActions() {
        ActionMap actionMap = getActionMap();

        // go to next segment
        actionMap.put(KEY_GO_TO_NEXT_SEGMENT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                getActiveDisplayedEntry().getNext().activate();
            }
        });

        // go to previous segment
        actionMap.put(KEY_GO_TO_PREVIOUS_SEGMENT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                getActiveDisplayedEntry().getPrevious().activate();
            }
        });

        // transfer focus to next component
        actionMap.put(KEY_TRANSFER_FOCUS, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                transferFocus();
            }
        });

        // transfer focus to previous component
        actionMap.put(KEY_TRANSFER_FOCUS_BACKWARD, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                transferFocusBackward();
            }
        });
    }

    private DisplayedEntry getActiveDisplayedEntry() {
        int activeEntryListIndex = getActiveEntryListIndex();

        switch (activeEntryListIndex) {
            case ENTRY_LIST_INDEX_NO_ENTRIES:
                return new EmptyDisplayedEntry();
            case ENTRY_LIST_INDEX_END_OF_TEXT:
                // end of text (out of entries range)
                return new DisplayedEntryImpl(getNrEntries());
            default:
                return new DisplayedEntryImpl(activeEntryListIndex);
        }
    }

    private interface DisplayedEntry {

        DisplayedEntry getNext();

        DisplayedEntry getPrevious();

        void activate();
        
        void gotoEntryInEditor();
    }

    private static class EmptyDisplayedEntry implements DisplayedEntry {

        @Override
        public DisplayedEntry getNext() {
            return this;
        }

        @Override
        public DisplayedEntry getPrevious() {
            return this;
        }

        @Override
        public void activate() {
            // Do nothing
        }

        @Override
        public void gotoEntryInEditor() {
            // Do nothing
        }

    }

    private class DisplayedEntryImpl implements DisplayedEntry {

        private final int index;

        private DisplayedEntryImpl(int index) {
            this.index = index;
        }

        @Override
        public DisplayedEntry getNext() {
            if (index >= (getNrEntries() - 1)) {
                return this;
            } else {
                return new DisplayedEntryImpl(index + 1);
            }
        }

        @Override
        public DisplayedEntry getPrevious() {
            if (index == 0) {
                return this;
            } else {
                return new DisplayedEntryImpl(index - 1);
            }
        }

        @Override
        public void activate() {
            if (index >= getNrEntries()) {
                // end of text (out of entries range)
                return;
            }

            int beginPos = 0;
            if (index != 0) {
                beginPos = m_offsetList.get(index - 1) + ENTRY_SEPARATOR.length();
                int endPos = m_offsetList.get(index);
                try {
                    Rectangle endRect = modelToView(endPos);
                    scrollRectToVisible(endRect);
                } catch (BadLocationException ex) {
                    // Eat exception silently
                }
            }
            setSelectionStart(beginPos);
            setSelectionEnd(beginPos);
        }

        @Override
        public void gotoEntryInEditor() {
            if (index >= getNrEntries()) {
                // end of text (out of entries range)
                return;
            }

            final int entry = m_entryList.get(index);
            if (entry > 0) {
                final IEditor editor = Core.getEditor();
                int currEntryInEditor = editor.getCurrentEntryNumber();
                if (currEntryInEditor != 0 && entry != currEntryInEditor) {
                    final boolean isSegDisplayed = isSegmentDisplayed(entry);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (isSegDisplayed && m_firstMatchList.containsKey(entry)
                                    && editor instanceof EditorController) {
                                // Select search word in Editor pane
                                CaretPosition pos = m_firstMatchList.get(entry);
                                ((EditorController) editor).gotoEntry(entry, pos);
                            } else {
                                editor.gotoEntry(entry);
                            }
                        }
                    });
                }
            }
        }

        private boolean isSegmentDisplayed(int entry) {
            IEditorFilter filter = Core.getEditor().getFilter();
            if (filter == null) {
                return true;
            } else {
                SourceTextEntry ste = Core.getProject().getAllEntries().get(entry - 1);
                return filter.allowed(ste);
            }
        }
    }

    private class SegmentHighlighter implements Runnable {

        private final MutableAttributeSet attrNormal;
        private final MutableAttributeSet attrActive;
        
        private int entryListIndex = -1;
        private int offset         = -1;
        private int length         = -1;

        public SegmentHighlighter() {
            attrNormal = new SimpleAttributeSet();
            StyleConstants.setBackground(attrNormal, EditorColor.COLOR_BACKGROUND.getColor());

            attrActive = new SimpleAttributeSet();
            StyleConstants.setBackground(attrActive, EditorColor.COLOR_ACTIVE_SOURCE.getColor());
        }

        @Override
        public void run() {
            int activeEntryListIndex = getActiveEntryListIndex();
            if (activeEntryListIndex == ENTRY_LIST_INDEX_END_OF_TEXT) {
                // end of text (out of entries range) should belongs to the last segment
                activeEntryListIndex = getNrEntries() - 1;
            }

            if (activeEntryListIndex != entryListIndex) {
                removeCurrentHighlight();
                addHighlight(activeEntryListIndex);
            }
        }

        public void reset() {
            entryListIndex = -1;
            offset         = -1;
            length         = -1;
        }
        
        private void removeCurrentHighlight() {
            if (entryListIndex == -1 || entryListIndex >= m_offsetList.size() || length <= 0) {
                return;
            }

            getStyledDocument().setCharacterAttributes(offset, length, attrNormal, false);
            reset();
        }

        private void addHighlight(int entryListIndex) {
            if (entryListIndex == -1 || entryListIndex >= m_offsetList.size()) {
                return;
            }

            int offset = entryListIndex == 0
                    ? 0
                    : m_offsetList.get(entryListIndex - 1) + ENTRY_SEPARATOR.length();
            int length = m_offsetList.get(entryListIndex) - offset - 1; // except tail line break

            getStyledDocument().setCharacterAttributes(offset, length, attrActive, false);

            this.entryListIndex = entryListIndex;
            this.offset = offset;
            this.length = length;
        }
    }

    private volatile Searcher m_searcher;
    private final List<Integer> m_entryList = new ArrayList<Integer>();
    private final List<Integer> m_offsetList = new ArrayList<Integer>();
    private final Map<Integer, CaretPosition> m_firstMatchList = new HashMap<Integer, CaretPosition>();
    private DisplayMatches currentlyDisplayedMatches;
    private int numberOfResults;
    private boolean useTabForAdvance;
    private boolean autoSyncWithEditor;
    private final SegmentHighlighter highlighter = new SegmentHighlighter();
}
