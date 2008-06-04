/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel
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

package org.omegat.gui.glossary;

import java.awt.Font;
import java.io.File;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.StringEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a Glossary pane that displays glossary entries.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class GlossaryTextArea extends javax.swing.JTextPane
{
    /** Glossary manager instance.*/
    protected final GlossaryManager manager;

    /**
     * Currently processed entry. Used to detect if user moved into new entry.
     * In this case, new find should be started.
     */
    protected StringEntry processedEntry;
    
    /** Creates new form MatchGlossaryPane */
    public GlossaryTextArea()
    {
        manager = new GlossaryManager();
        setEditable(false);
        
        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(final PROJECT_CHANGE_TYPE eventType) {
                switch (eventType) {
                case LOAD:
                    loadGlossaries();
                    break;
                case CLOSE:
                    closeGlossaries();
                    break;
                }
            }
        });
        
        // find glossary entries on every editor entry change
        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            public void onNewFile(String activeFileName) {
            }

            public void onEntryActivated(final StringEntry newEntry) {
                processedEntry = newEntry;
                new FindGlossaryThread(GlossaryTextArea.this, newEntry).start();
            }
        });
        CoreEvents
                .registerFontChangedEventListener(new IFontChangedEventListener() {
                    public void onFontChanged(Font newFont) {
                        GlossaryTextArea.this.setFont(newFont);
                    }
                });
    }
    
    /**
     * Sets the list of glossary entries to show in the pane. Each element of
     * the list should be an instance of {@link GlossaryEntry}.
     */
    protected void setGlossaryEntries(List<GlossaryEntry> entries)
    {
        UIThreadsUtil.mustBeSwingThread();
        
        StringBuffer buf = new StringBuffer();
        for (GlossaryEntry entry : entries)
        {
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
    
    protected void loadGlossaries() {
        final File dir = new File(Core.getProject().getProjectProperties()
                .getGlossaryRoot());
        new Thread() {
            public void run() {
                manager.loadGlossaryFiles(dir);
            }
        }.start();
    }

    protected void closeGlossaries() {
        manager.clear();
    }
}
