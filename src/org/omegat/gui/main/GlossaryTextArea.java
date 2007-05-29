/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel
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

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.List;

import org.omegat.core.StringData;
import org.omegat.core.glossary.GlossaryEntry;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.Token;

/**
 * This is a Glossary pane that displays glossary entries.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class GlossaryTextArea extends javax.swing.JTextPane
{
    /** Creates new form MatchGlossaryPane */
    public GlossaryTextArea()
    {
        setEditable(false);
    }
    
    /** 
     * Sets the list of glossary entries to show in the pane.
     * Each element of the list should be an instance of {@link GlossaryEntry}.
     */
    public void setGlossaryEntries(List entries)
    {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<entries.size(); i++)
        {
            GlossaryEntry entry = (GlossaryEntry) entries.get(i);
            buf.append(entry.getSrcText() + " = " +                             // NOI18N
                    entry.getLocText());                                        // NOI18N
            if (entry.getCommentText().length()>0)
                buf.append("\n" + entry.getCommentText());                      // NOI18N
            buf.append("\n\n");                                                 // NOI18N
        }
        setText(buf.toString());
    }
    
    /** Clears up the pane. */
    public void clear()
    {
        setText(new String());
    }
}
