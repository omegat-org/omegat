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

package org.omegat.gui.tagvalidation;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.StringEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

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
        List<SourceTextEntry> suspects = listInvalidTags();
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

    public void onProjectChanged(final IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        switch (eventType) {
        case CLOSE:
            if (m_tagWin != null)
                m_tagWin.dispose();
            break;
        }
    }
    
    /**
     * Scans project and builds the list of entries which are suspected of
     * having changed (possibly invalid) tag structures.
     */
    private List<SourceTextEntry> listInvalidTags() {
        int j;
        String s;
        String t;
        List<String> srcTags = new ArrayList<String>(32);
        List<String> locTags = new ArrayList<String>(32);
        List<SourceTextEntry> suspects = new ArrayList<SourceTextEntry>(16);

        StringEntry se;

        IProject dataEngine = Core.getProject();
        synchronized (dataEngine) {
        for (SourceTextEntry ste : dataEngine.getAllEntries()) {
            se = ste.getStrEntry();
            s = se.getSrcText();
            t = se.getTranslation();

            // if there's no translation, skip the string
            // bugfix for http://sourceforge.net/support/tracker.php?aid=1209839
            if (t == null || t.length() == 0)
                continue;

            // extract tags from src and loc string
            StaticUtils.buildTagList(s, srcTags);
            StaticUtils.buildTagList(t, locTags);

            // make sure lists match
            // for now, insist on exact match
            if (srcTags.size() != locTags.size())
                suspects.add(ste);
            else {
                // compare one by one
                for (j = 0; j < srcTags.size(); j++) {
                    s = srcTags.get(j);
                    t = locTags.get(j);
                    if (!s.equals(t)) {
                        suspects.add(ste);
                        break;
                    }
                }
            }

            srcTags.clear();
            locTags.clear();
        }
        }
        return suspects;
    }
}
