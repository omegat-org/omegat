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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.search;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.omegat.core.Core;
import org.omegat.core.search.SearchMatch;
import org.omegat.core.search.SearchResultEntry;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
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
class EntryListPane extends JTextPane {
    protected static final SimpleAttributeSet FOUND_MARK;
    protected static final int MARKS_PER_REQUEST = 100;

    static {
        FOUND_MARK = new SimpleAttributeSet();
        StyleConstants.setBold(FOUND_MARK, true);
        StyleConstants.setForeground(FOUND_MARK, Color.BLUE);
    }

    public EntryListPane() {
        setDocument(new DefaultStyledDocument());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
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
                                    }
                                });
                            }
                            break;
                        }
                    }
                }
            }
        });

        setEditable(false);
    }

    /**
     * Show search result for user
     */
    public void displaySearchResult(List<SearchResultEntry> entries) {
        UIThreadsUtil.mustBeSwingThread();

        m_entryList.clear();
        m_offsetList.clear();
        matches.clear();
        setText("");
        setDocument(new DefaultStyledDocument());
        if (entries == null) {
            // just reset
            return;
        }

        StringBuilder m_stringBuf = new StringBuilder();
        // display what's been found so far
        if (entries.size() == 0) {
            // no match
            addMessage(m_stringBuf, OStrings.getString("ST_NOTHING_FOUND"));
        }

        if (entries.size() >= OConsts.ST_MAX_SEARCH_RESULTS) {
            addMessage(m_stringBuf,
                    StaticUtils.format(OStrings
                            .getString("SW_MAX_FINDS_REACHED"),
                            new Object[] { new Integer(
                                    OConsts.ST_MAX_SEARCH_RESULTS) }));
        }

        for (SearchResultEntry e : entries) {
            addEntry(m_stringBuf, e.getEntryNum(), e.getPreamble(),
                    e.getSrcPrefix(), e.getSrcText(), e.getTranslation(),
                    e.getSrcMatch(), e.getTargetMatch());
        }

        setFont();

        setText(m_stringBuf.toString());
        setCaretPosition(0);

        SwingUtilities.invokeLater(displayMatches);
    }

    // add entry text - remember what its number is and where it ends
    public void addEntry(StringBuilder m_stringBuf, int num, String preamble,
            String srcPrefix, String src, String loc, SearchMatch[] srcMatches,
            SearchMatch[] targetMatches) {
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

        // Insert the essage text
        m_stringBuf.append(message);
    }

    public void setFont() {
        String srcFont = Preferences.getPreference(OConsts.TF_SRC_FONT_NAME);
        if (!srcFont.equals("")) {
            int fontsize;
            try {
                fontsize = Integer.valueOf(
                        Preferences.getPreference(OConsts.TF_SRC_FONT_SIZE))
                        .intValue();
            } catch (NumberFormatException nfe) {
                fontsize = 12;
            }
            setFont(new Font(srcFont, Font.PLAIN, fontsize));
        }
    }

    protected Runnable displayMatches = new Runnable() {
        public void run() {
            UIThreadsUtil.mustBeSwingThread();

            DefaultStyledDocument doc = (DefaultStyledDocument) getDocument();

            // we don't need synchronize around matches, because it used only in
            // UI thread
            List<SearchMatch> display = matches.subList(0,
                    Math.min(MARKS_PER_REQUEST, matches.size()));
            for (SearchMatch m : display) {
                doc.setCharacterAttributes(m.start, m.length, FOUND_MARK, true);
            }
            display.clear();

            if (matches.size() > 0) {
                SwingUtilities.invokeLater(displayMatches);
            }
        }
    };

    public void reset() {
        displaySearchResult(null);
    }

    public int getNrEntries() {
        return m_entryList.size();
    }

    public List<Integer> getEntryList() {
        return m_entryList;
    }

    private final List<Integer> m_entryList = new ArrayList<Integer>();
    private final List<Integer> m_offsetList = new ArrayList<Integer>();
    private final List<SearchMatch> matches = new ArrayList<SearchMatch>();
}
