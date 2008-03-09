/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.gui;

import java.awt.Font;
import java.util.List;

import javax.swing.JOptionPane;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;

/**
 * Class for show tag validation results.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TagValidationTool implements ITagValidation, IProjectEventListener {
    private TagValidationFrame m_tagWin;
    private MainWindow mainWindow;

    public TagValidationTool(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        CoreEvents.registerProjectChangeListener(this);
    }

    public void validateTags() {
        List<SourceTextEntry> suspects = CommandThread.core.validateTags();
        if (suspects.size() > 0) {
            // create a tag validation window if necessary
            if (m_tagWin == null) {
                m_tagWin = new TagValidationFrame(mainWindow);
                m_tagWin.setFont(Core.getMainWindow().getApplicationFont());
            } else {
                // close tag validation window if present
                m_tagWin.dispose();
            }

            // display list of suspect strings
            m_tagWin.setVisible(true);
            m_tagWin.displayStringList(suspects);
        } else {
            // close tag validation window if present
            if (m_tagWin != null)
                m_tagWin.dispose();

            // show dialog saying all is OK
            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(), OStrings
                    .getString("TF_NOTICE_OK_TAGS"), OStrings.getString("TF_NOTICE_TITLE_TAGS"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setFont(final Font newFont) {
        m_tagWin.setFont(newFont);
    }

    public void onProjectChanged() {
        if (!Core.getDataEngine().isProjectLoaded()) {
            if (m_tagWin != null)
                m_tagWin.dispose();
        }
    }
}
