/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
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

package org.omegat.gui.matches;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.core.StringData;
import org.omegat.core.matching.NearString;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;

/**
 * This is a Match pane, that displays fuzzy matches.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko
 */
public class MatchesTextArea extends javax.swing.JTextPane implements MouseListener
{
    private List<NearString> matches;
    private List<Integer> delimiters;
    private int activeMatch;
    private StringBuffer displayBuffer;
    
    private MainWindow mw;
    
    /** Creates new form MatchGlossaryPane */
    public MatchesTextArea(MainWindow mw)
    {
        this.mw = mw;
        setEditable(false);
        setMinimumSize(new java.awt.Dimension(100, 50));
        addMouseListener(this);
    }
    
    /** 
     * Sets the list of fuzzy matches to show in the pane.
     * Each element of the list should be an instance of {@link NearString}.
     */
    public void setMatches(List<NearString> matches)
    {
        this.matches = matches;
        activeMatch = -1;
        delimiters = new ArrayList<Integer>(matches.size()+1);
        delimiters.add(0);
        displayBuffer = new StringBuffer();
        
        for (int i=0; i<matches.size(); i++)
        {
            NearString match = matches.get(i);
            displayBuffer.append((i+1)+") " + match.str.getSrcText() + "\n" +   // NOI18N
                    match.str.getTranslation() + "\n< " + match.score + "% " +  // NOI18N
                    match.proj + " >");                                         // NOI18N
            if (i < (matches.size()-1))
                displayBuffer.append("\n\n");                                   // NOI18N
            delimiters.add(displayBuffer.length());
        }
        
        setText(displayBuffer.toString());
        setActiveMatch(0);
    }
    /** 
     * Sets the index of an active match. It basically highlights
     * the fuzzy match string selected.
     * (numbers start from 0)
     */
    public void setActiveMatch(int activeMatch)
    {
        if (activeMatch<0 || activeMatch>=matches.size() ||
                this.activeMatch==activeMatch)
        {
            return;
        }
        
        this.activeMatch = activeMatch;
        
        selectAll();
        setCharacterAttributes(Styles.PLAIN, true);
        
        int start = delimiters.get(activeMatch);
        int end = delimiters.get(activeMatch+1);
        
        NearString match = matches.get(activeMatch);
        // List tokens = match.str.getSrcTokenList();
        List<Token> tokens = match.str.getSrcTokenListAll(); // fix for bug 1586397
        byte[] attributes = match.attr;
        for (int i=0; i<tokens.size(); i++)
        {
            Token token = tokens.get(i);
            int tokstart = start + 3 + token.getOffset();
            int tokend = start + 3 + token.getOffset() + token.getLength();
            select(tokstart, tokend);
            if ((attributes[i] & StringData.UNIQ) != 0)
                setCharacterAttributes(Styles.TEXT_EXTRA, false);
            else if ((attributes[i] & StringData.PAIR) != 0)
                setCharacterAttributes(Styles.TEXT_BORDER, false);
        }
        
        select(start, end);
        setCharacterAttributes(Styles.BOLD, false);
        setCaretPosition(end-2); // two newlines
        final int fstart = start;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                setCaretPosition(fstart);
            }
        });
    }
    
    /** Clears up the pane. */
    public void clear()
    {
        matches = null;
        setText(new String());
    }

    public void mouseClicked(MouseEvent e) {
        // is there anything?
        if (matches == null || matches.isEmpty())
            return;
        
        // set up the menu
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
            // find out the clicked item
            int clickedItem = -1;
            
            // where did we click?
            int mousepos = this.viewToModel(e.getPoint());
            
            int i;
            for (i = 0; i < delimiters.size()-1; i++) {
                int start = delimiters.get(i);
                int end = delimiters.get(i+1);
                
                if (mousepos >= start && mousepos< end) {
                    clickedItem = i;
                    break;
                }
            }
            
            if (clickedItem == -1)
                clickedItem = delimiters.size()-1;
            
            final int clicked = clickedItem;
            
            // create the menu
            JPopupMenu popup = new JPopupMenu();
            
            JMenuItem item = popup.add(OStrings.getString("MATCHES_INSERT"));
            item.addActionListener(new ActionListener() {
                // the action: insert this match
                public synchronized void actionPerformed(ActionEvent e) {
                    setActiveMatch(clicked);
                    mw.doInsertTrans();
                }
            });
            
            item = popup.add(OStrings.getString("MATCHES_REPLACE"));
            item.addActionListener(new ActionListener() {
                public synchronized void actionPerformed(ActionEvent e) {
                    setActiveMatch(clicked);
                    mw.doRecycleTrans();
                }
            });
            
            popup.addSeparator();
            
            if (clicked >= matches.size())
                return;
            
            final NearString ns = matches.get(clicked);
            String project = ns.proj;
            
            item = popup.add(OStrings.getString("MATCHES_GO_TO_SEGMENT_SOURCE"));
            
            if (project == null || project.equals("")) {
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Core.getEditor().gotoEntry(ns.str.getParentList().first()
								.entryNum() + 1);
                    }
                });
            } else {
                item.setEnabled(false);
            }
            
            popup.show(this, e.getX(), e.getY());
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public int getActiveMatch() {
        return activeMatch;
    }
}
