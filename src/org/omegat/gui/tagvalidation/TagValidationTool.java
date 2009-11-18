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
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TransEntry;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
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
        TransEntry te;
        List<String> srcTags = new ArrayList<String>(32);
        List<String> locTags = new ArrayList<String>(32);
        List<SourceTextEntry> suspects = new ArrayList<SourceTextEntry>(16);

        //programming language  validation: pattern to detect printf variables (%s and %n\$s)
        Pattern printfPattern=null;
        if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_ALL_PRINTF_TAGS))) {
            printfPattern = PatternConsts.PRINTF_VARS;
        } else if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_SIMPLE_PRINTF_TAGS))) {
            printfPattern = PatternConsts.SIMPLE_PRINTF_VARS;
        }

        for(FileInfo fi:Core.getProject().getProjectFiles()) {
          for (SourceTextEntry ste : fi.entries) {
            s = ste.getSrcText();
            te = Core.getProject().getTranslation(ste);

            // if there's no translation, skip the string
            // bugfix for http://sourceforge.net/support/tracker.php?aid=1209839
            if (te == null) {
                continue;
            }

            if (printfPattern != null) {
                // printf variables should be equal.
                // we check this by adding the string "index+typespecifier" of every 
                // found variable to a set.
                // If the sets of the source and target are not equal, then there is
                // a problem: either missing or extra variables, or the typespecifier
                // has changed for the variable at the given index.
                HashSet<String> printfSourceSet = new HashSet<String>();
                Matcher printfMatcher = printfPattern.matcher(s);
                int index=1;
                while (printfMatcher.find()) {
                    String printfVariable = printfMatcher.group(0);
                    String argumentswapspecifier = printfMatcher.group(1);
                    if (argumentswapspecifier != null && argumentswapspecifier.endsWith("$")) {
                        printfSourceSet.add(""+argumentswapspecifier.substring(0, argumentswapspecifier.length()-1)+printfVariable.substring(printfVariable.length()-1, printfVariable.length()));
                    } else {
                        printfSourceSet.add(""+index+printfVariable.substring(printfVariable.length()-1, printfVariable.length()));
                        index++;
                    }
                }
                HashSet<String> printfTargetSet = new HashSet<String>();
                printfMatcher = printfPattern.matcher(te.translation);
                index=1;
                while (printfMatcher.find()) {
                    String printfVariable = printfMatcher.group(0);
                    String argumentswapspecifier = printfMatcher.group(1);
                    if (argumentswapspecifier != null && argumentswapspecifier.endsWith("$")) {
                        printfTargetSet.add(""+argumentswapspecifier.substring(0, argumentswapspecifier.length()-1)+printfVariable.substring(printfVariable.length()-1, printfVariable.length()));
                    } else {
                        printfTargetSet.add(""+index+printfVariable.substring(printfVariable.length()-1, printfVariable.length()));
                        index++;
                    }
                }
                if (!printfSourceSet.equals(printfTargetSet)) {
                    suspects.add(ste);
                    continue;
                }
            }
            //Extra checks for PO files:
            if (fi.filePath.endsWith(".po") || fi.filePath.endsWith(".pot")) { //TODO: check with source-files settings for PO instead of hardcoded?
                // check PO line ending:
                Boolean s_ends_lf = s.endsWith("\n");
                Boolean t_ends_lf = te.translation.endsWith("\n");
                if (s_ends_lf && !t_ends_lf || !s_ends_lf && t_ends_lf) {
                    suspects.add(ste);
                    continue;
                }
            }
            // OmegaT tags check:
            // extract tags from src and loc string
            StaticUtils.buildTagList(s, srcTags);
            StaticUtils.buildTagList(te.translation, locTags);

            // make sure lists match
            // for now, insist on exact match
            if (srcTags.size() != locTags.size())
                suspects.add(ste);
            else {
                // compare one by one
                for (j = 0; j < srcTags.size(); j++) {
                    s = srcTags.get(j);
                    String t = locTags.get(j);
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
