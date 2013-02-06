/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006-2007 Henry Pijffers
               2010 Alex Buloichik, Didier Briel
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.search;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;

import org.omegat.core.Core;
import org.omegat.core.search.SearchMatch;
import org.omegat.core.search.SearchResultEntry;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.AlwaysVisibleCaret;
import org.omegat.util.gui.Styles;
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

    public EntryListPane() {
        setDocument(new DefaultStyledDocument());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    final Cursor oldCursor = getCursor();
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    // user double clicked on viewer pane - send message
                    // to org.omegat.gui.TransFrame to jump to this entry
                    int pos = getCaretPosition();
                    int off;
                    for (int i = 0; i < m_offsetList.size(); i++) {
                        off = m_offsetList.get(i);
                        if (off >= pos) {
                            final int entry = m_entryList.get(i);
                            if (entry >= 0) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        Core.getEditor().gotoEntry(entry);
                                        setCursor(oldCursor);
                                    }
                                });
                            } else {
                                setCursor(oldCursor);
                            }
                            break;
                        }
                    }
                }
            }
        });

        setEditable(false);
        AlwaysVisibleCaret.apply(this);
    }

    /**
     * Show search result for user
     */
    public void displaySearchResult(List<SearchResultEntry> entries, int numberOfResults) {
        UIThreadsUtil.mustBeSwingThread();

        this.numberOfResults = numberOfResults;

        currentlyDisplayedMatches = null;
        m_entryList.clear();
        m_offsetList.clear();

        if (entries == null) {
            // empty marks - just reset
            setText("");
            return;
        }

        currentlyDisplayedMatches = new DisplayMatches(entries);
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
                        new Object[] { new Integer(numberOfResults) }));
            }

            for (SearchResultEntry e : entries) {
                addEntry(m_stringBuf, e.getEntryNum(), e.getPreamble(), e.getSrcPrefix(), e.getSrcText(),
                        e.getTranslation(), e.getSrcMatch(), e.getTargetMatch());
            }

            try {
                doc.insertString(0, m_stringBuf.toString(), null);
            } catch (Exception ex) {
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
                String src, String loc, SearchMatch[] srcMatches, SearchMatch[] targetMatches) {
            if (m_stringBuf.length() > 0)
                m_stringBuf.append("---------\n");

            if (preamble != null && !preamble.equals(""))
                m_stringBuf.append(preamble + "\n");
            if (src != null && !src.equals("")) {
                m_stringBuf.append("-- ");
                if (srcPrefix != null) {
                    m_stringBuf.append(srcPrefix);
                }
                if (srcMatches != null) {
                    for (SearchMatch m : srcMatches) {
                        m.start += m_stringBuf.length();
                        matches.add(m);
                    }
                }
                m_stringBuf.append(src);
                m_stringBuf.append('\n');
            }
            if (loc != null && !loc.equals("")) {
                m_stringBuf.append("-- ");
                if (targetMatches != null) {
                    for (SearchMatch m : targetMatches) {
                        m.start += m_stringBuf.length();
                        matches.add(m);
                    }
                }
                m_stringBuf.append(loc);
                m_stringBuf.append('\n');
            }

            m_entryList.add(num);
            m_offsetList.add(m_stringBuf.length());
        }

        public void run() {
            UIThreadsUtil.mustBeSwingThread();

            if (currentlyDisplayedMatches != this) {
                // results changed - shouldn't mark old results
                return;
            }

            List<SearchMatch> display = matches.subList(0, Math.min(MARKS_PER_REQUEST, matches.size()));
            for (SearchMatch m : display) {
                doc.setCharacterAttributes(m.start, m.length, FOUND_MARK, true);
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
            m_stringBuf.append("---------\n");

        // Insert the message text
        m_stringBuf.append(message);
    }

    public void setFont() {
        String srcFont = Preferences.getPreference(OConsts.TF_SRC_FONT_NAME);
        if (!srcFont.equals("")) {
            int fontsize;
            try {
                fontsize = Integer.valueOf(Preferences.getPreference(OConsts.TF_SRC_FONT_SIZE)).intValue();
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

    private final List<Integer> m_entryList = new ArrayList<Integer>();
    private final List<Integer> m_offsetList = new ArrayList<Integer>();
    private DisplayMatches currentlyDisplayedMatches;
    private int numberOfResults;
}
