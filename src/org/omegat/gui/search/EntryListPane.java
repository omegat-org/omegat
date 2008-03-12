/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;

import org.omegat.gui.main.MainWindow;
import org.omegat.gui.messages.MessageRelay;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;

/** 
 * EntryListPane displays translation segments and, upon doubleclick
 * of a segment, instructs the main UI to jump to that segment
 * this replaces the previous huperlink interface and is much more
 * flexible in the fonts it displays than the HTML text
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
class EntryListPane extends JTextPane
{
    public EntryListPane(MainWindow trans)
    {
        m_transFrame = trans;
        m_offsetList = new ArrayList<Integer>();
        m_entryList = new ArrayList<Integer>();
        m_stringBuf = new StringBuffer();

        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                {
                    // user double clicked on viewer pane - send message
                    //    to org.omegat.gui.TransFrame to jump to this entry
                    int pos = getCaretPosition();
                    int off;
                    int entry;
                    for (int i=0; i<m_offsetList.size(); i++)
                    {
                        off = m_offsetList.get(i);
                        if (off >= pos)
                        {
                            entry = m_entryList.get(i);
                            if (entry >= 0)
                            {
                                MessageRelay.uiMessageDoGotoEntry(
                                        m_transFrame, Integer.toString(entry));
                            }
                            break;
                        }
                    }
                }
            }
        });

        setEditable(false);
    }

    // add entry text - remember what it's number is and where it ends
    public void addEntry(int num, String preamble, String src, String loc)
    {
        if (m_stringBuf.length() > 0)
            m_stringBuf.append("---------\n");                                    // NOI18N

        if (preamble != null && !preamble.equals(""))                // NOI18N
            m_stringBuf.append(preamble + "\n");                                // NOI18N
        if (src != null && !src.equals(""))                            // NOI18N
        {
            m_stringBuf.append("-- "+src + "\n");                                // NOI18N
        }
        if (loc != null && !loc.equals(""))                            // NOI18N
        {
            m_stringBuf.append("-- "+loc + "\n");                                // NOI18N
        }

        m_entryList.add(num);
        m_offsetList.add(m_stringBuf.length());
    }

    /**
      * Adds a message text to be displayed.
      * Used for displaying messages that aren't results.
      *
      * @param message The message to display
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public void addMessage(String message) {
        // Insert entry/message separator if necessary
        if (m_stringBuf.length() > 0)
            m_stringBuf.append("---------\n");                                    // NOI18N

        // Insert the essage text
        m_stringBuf.append(message);
    }

    public void finalize()
    {
        String srcFont = Preferences.getPreference(OConsts.TF_SRC_FONT_NAME);
        if (!srcFont.equals(""))                                        // NOI18N
        {
            int fontsize;
            try 
            {
                fontsize = Integer.valueOf(
                        Preferences.getPreference(OConsts.TF_SRC_FONT_SIZE)).
                        intValue();
            }
            catch (NumberFormatException nfe) { fontsize = 12; }
            setFont(new Font(srcFont, Font.PLAIN, fontsize));
        }
        setText(m_stringBuf.toString());
    }

    public void reset()    
    {
        m_entryList.clear();
        m_offsetList.clear();
        m_stringBuf.setLength(0);
        setText("");                                                            // NOI18N
    }

    public int getNrEntries() {
        return m_entryList.size();
    }

    private StringBuffer    m_stringBuf;
    private List<Integer>        m_entryList;
    private List<Integer> m_offsetList;
    private MainWindow    m_transFrame;
}
