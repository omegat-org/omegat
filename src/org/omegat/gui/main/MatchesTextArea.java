/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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

package org.omegat.gui.main;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.omegat.core.StringData;
import org.omegat.core.matching.NearString;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;

/**
 * This is a Match pane, that displays fuzzy matches.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class MatchesTextArea extends javax.swing.JTextPane
{
    private List matches;
    private List delimiters;
    private int activeMatch;
    private StringBuffer displayBuffer;
    
    /** Creates new form MatchGlossaryPane */
    public MatchesTextArea()
    {
        setEditable(false);
        setMinimumSize(new java.awt.Dimension(100, 50));
    }
    
    /** 
     * Sets the list of fuzzy matches to show in the pane.
     * Each element of the list should be an instance of {@link NearString}.
     */
    public void setMatches(List matches)
    {
        this.matches = matches;
        activeMatch = -1;
        delimiters = new ArrayList(matches.size()+1);
        delimiters.add(new Integer(0));
        displayBuffer = new StringBuffer();
        
        for (int i=0; i<matches.size(); i++)
        {
            NearString match = (NearString) matches.get(i);
            displayBuffer.append((i+1)+") " + match.str.getSrcText() + "\n" +   // NOI18N
                    match.str.getTranslation() + "\n< " + match.score + "% " +  // NOI18N
                    match.proj + " >");                                         // NOI18N
            if (i < (matches.size()-1))
                displayBuffer.append("\n\n");                                   // NOI18N
            delimiters.add(new Integer(displayBuffer.length()));
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
        
        int start = ((Integer)delimiters.get(activeMatch)).intValue();
        int end = ((Integer)delimiters.get(activeMatch+1)).intValue();
        
        NearString match = (NearString) matches.get(activeMatch);
        List tokens = match.str.getSrcTokenList();
        byte[] attributes = match.attr;
        for (int i=0; i<tokens.size(); i++)
        {
            Token token = (Token) tokens.get(i);
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
}
