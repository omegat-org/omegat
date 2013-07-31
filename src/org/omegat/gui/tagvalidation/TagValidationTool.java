/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik, Martin Fleurke
               2009 Martin Fleurke
               2013 Aaron Madlon-Kay, Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.tagvalidation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.core.tagvalidation.ErrorReport.TagError;
import org.omegat.core.tagvalidation.TagRepair;
import org.omegat.core.tagvalidation.TagValidation;
import org.omegat.filters2.po.PoFilter;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

/**
 * Class for show tag validation results.
 * 
 * Class is synchronized around one check iteration, because it need to use some
 * local variables.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public class TagValidationTool implements ITagValidation, IProjectEventListener {
    private TagValidationFrame m_tagWin;
    private MainWindow mainWindow;

    // variables for one check iteration, by all entries or only by one entry
    private List<String> srcTags = new ArrayList<String>(32);
    private List<String> locTags = new ArrayList<String>(32);
    private HashSet<String> printfSourceSet = new HashSet<String>();
    private HashSet<String> printfTargetSet = new HashSet<String>();

    public TagValidationTool(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        CoreEvents.registerProjectChangeListener(this);
    }

    public TagValidationTool() {
        CoreEvents.registerProjectChangeListener(this);
    }

    @Override
    public synchronized void displayTagValidationErrors(List<ErrorReport> suspects, String message) {
        if (mainWindow != null) {
            showTagResultsInGui(suspects, message);
        } else {
            showTagResultsInConsole(suspects);
        }
    }

    private void showTagResultsInGui(List<ErrorReport> suspects, String message) {
        if (suspects != null && suspects.size() > 0) {
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
            m_tagWin.setMessage(message);
            m_tagWin.displayErrorList(suspects);
        } else {
            // close tag validation window if present
            if (m_tagWin != null)
                m_tagWin.dispose();

            // show dialog saying all is OK
            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                    OStrings.getString("TF_NOTICE_OK_TAGS"), OStrings.getString("TF_NOTICE_TITLE_TAGS"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showTagResultsInConsole(List<ErrorReport> suspects) {
        if (suspects != null && suspects.size() > 0) {
            for (ErrorReport report : suspects) {
                System.out.println(report.entryNum);
                System.out.println(report.source);
                System.out.println(report.translation);
                for (Map.Entry<TagError, List<String>> e : report.inverseReport().entrySet()) {
                    System.out.print("  ");
                    System.out.print(ErrorReport.localizedTagError(e.getKey()));
                    System.out.print(": ");
                    for (String tag : e.getValue()) {
                        System.out.print(tag);
                        System.out.print(" ");
                    }
                    System.out.println();
                }
            }
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
    @Override
    public synchronized List<ErrorReport> listInvalidTags() {

        List<ErrorReport> suspects = new ArrayList<ErrorReport>(16);
        for (FileInfo fi : Core.getProject().getProjectFiles()) {
            for (SourceTextEntry ste : fi.entries) {
                ErrorReport err = checkEntry(fi, ste);
                if (err != null) {
                    suspects.add(err);
                }
            } // end loop over entries
        } // end loop over files
        return suspects.isEmpty() ? null : suspects;
    }

    @Override
    public synchronized boolean checkInvalidTags(SourceTextEntry ste) {

        // find FileInfo
        for (FileInfo fi : Core.getProject().getProjectFiles()) {
            for (SourceTextEntry s : fi.entries) {
                if (s == ste) {
                    ErrorReport err = checkEntry(fi, ste);
                    return err == null;
                }
            }
        }

        throw new RuntimeException("Invalid SourceTextEntry storage for tag validation");
    }

    /**
     * Checks entry for valid tags.
     * 
     * @param ste
     * @return true if entry is valid, false if entry if not valid
     */
    private ErrorReport checkEntry(FileInfo fi, SourceTextEntry ste) {
        srcTags.clear();
        locTags.clear();
        printfSourceSet.clear();
        printfTargetSet.clear();

        String s = ste.getSrcText();
        TMXEntry te = Core.getProject().getTranslationInfo(ste);

        // if there's no translation, skip the string
        // bugfix for:
        // http://sourceforge.net/support/tracker.php?aid=1209839
        if (!te.isTranslated() || s.length() == 0) {
            return null;
        }
        ErrorReport report = new ErrorReport(ste, te.translation);

        // Check printf variables
        if (Preferences.isPreference(Preferences.CHECK_ALL_PRINTF_TAGS)) {
            TagValidation.inspectPrintfVariables(false, report);
        } else if (Preferences.isPreference(Preferences.CHECK_SIMPLE_PRINTF_TAGS)) {
            TagValidation.inspectPrintfVariables(true, report);
        }

        // Extra checks for PO files:
        if (fi.filterClass.equals(PoFilter.class)) {
            TagValidation.inspectPOWhitespace(report);
        }

        TagValidation.inspectOmegaTTags(ste, report);

        if (Preferences.isPreference(Preferences.CHECK_JAVA_PATTERN_TAGS)) {
            TagValidation.inspectJavaMessageFormat(report);
        }

        TagValidation.inspectRemovePattern(report);

        TagValidation.inspectCustomTags(report);

        return report.isEmpty() ? null : report;
    }

    /**
     * Fix all errors indicated in a given ErrorReport.
     * 
     * @param report
     *            The report indicating the segment and errors to fix
     * @return The fixed translation string, or null if one of the errors is of
     *         type UNSPECIFIED.
     */
    public static String fixErrors(ErrorReport report) {
        // Don't try to fix unspecified errors.
        if (report.srcErrors.containsValue(TagError.UNSPECIFIED)
                || report.transErrors.containsValue(TagError.UNSPECIFIED)) {
            return null;
        }

        // Sort the map first to ensure that fixing works properly.
        Map<String, TagError> sortedErrors = new TreeMap<String, TagError>(new StaticUtils.TagComparator(
                report.source));
        sortedErrors.putAll(report.srcErrors);
        sortedErrors.putAll(report.transErrors);

        StringBuilder sb = new StringBuilder(report.translation);

        for (Map.Entry<String, TagError> e : sortedErrors.entrySet()) {
            TagRepair.fixTag(report.ste, e.getKey(), e.getValue(), sb, report.source);
        }

        return sb.toString();
    }
}
