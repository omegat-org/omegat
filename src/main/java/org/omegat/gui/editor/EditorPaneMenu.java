/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor;

import java.awt.Frame;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.dialogs.FileCollisionDialog;
import org.omegat.gui.preferences.PreferencesWindowController;
import org.omegat.gui.preferences.view.EditingBehaviorController;
import org.omegat.util.OStrings;
import org.omegat.util.gui.IPaneMenu;

/**
 * Settings menu of the editor pane. Offers a shortcut to the editor
 * preferences and the CSV export of segments.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
final class EditorPaneMenu implements IPaneMenu {

    private final EditorController editor;

    EditorPaneMenu(EditorController editor) {
        this.editor = editor;
    }

    @Override
    public void populatePaneMenu(JPopupMenu menu) {
        JMenuItem prefs = new JMenuItem(OStrings.getString("GUI_EDITORWINDOW_OPEN_PREFS"));
        prefs.addActionListener(e -> new PreferencesWindowController().show(
                Objects.requireNonNull(Core.getMainWindow()).getApplicationFrame(),
                EditingBehaviorController.class));
        menu.add(prefs);

        JMenuItem exportCsv = new JMenuItem(OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV"));
        exportCsv.setEnabled(Core.getProject().isProjectLoaded());
        exportCsv.addActionListener(e -> exportCsv());
        menu.add(exportCsv);
    }

    private void exportCsv() {
        IProject project = Core.getProject();
        if (!project.isProjectLoaded()) {
            return;
        }
        Frame parent = Objects.requireNonNull(Core.getMainWindow()).getApplicationFrame();
        JFileChooser chooser = new JFileChooser(project.getProjectProperties().getProjectRoot());
        chooser.setDialogTitle(OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_TITLE"));
        chooser.setSelectedFile(new File(project.getProjectProperties().getProjectRoot(),
                project.getProjectProperties().getProjectName() + "_segments.csv"));
        CsvExportOptionsPanel optionsPanel = new CsvExportOptionsPanel(editor.getFilter() != null);
        chooser.setAccessory(optionsPanel);
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        CsvExportOptions options = optionsPanel.getOptions();
        options.saveToPreferences();
        if (options.getSelectedColumns().isEmpty()) {
            Objects.requireNonNull(Core.getMainWindow())
                    .showStatusMessageRB("GUI_EDITORWINDOW_EXPORT_CSV_NO_COLUMNS");
            return;
        }
        File target = ensureCsvExtension(chooser.getSelectedFile());
        if (target.exists() && !FileCollisionDialog.promptToReplace(parent, target.getName())) {
            return;
        }
        List<SegmentCsvExporter.Row> rows = new ArrayList<>();
        for (SourceTextEntry ste : collectEntries(options)) {
            rows.add(new SegmentCsvExporter.Row(ste, project.getTranslationInfo(ste)));
        }
        writeInBackground(rows, options, target.toPath(), target.getName());
    }

    private static File ensureCsvExtension(File file) {
        return file.getName().toLowerCase(Locale.ENGLISH).endsWith(".csv") ? file
                : new File(file.getParentFile(), file.getName() + ".csv");
    }

    /** Gathers the entries on the EDT; the file write then leaves the EDT. */
    List<SourceTextEntry> collectEntries(CsvExportOptions options) {
        IProject project = Core.getProject();
        List<SourceTextEntry> entries = new ArrayList<>();
        if (options.getScope() == CsvExportOptions.Scope.PROJECT) {
            entries.addAll(project.getAllEntries());
        } else {
            SegmentBuilder[] segList = editor.m_docSegList;
            if (options.isApplySort() && segList != null) {
                for (SegmentBuilder builder : segList) {
                    entries.add(builder.getSourceTextEntry());
                }
            } else {
                List<IProject.FileInfo> files = project.getProjectFiles();
                if (!files.isEmpty()) {
                    int index = Math.max(0, Math.min(editor.displayedFileIndex, files.size() - 1));
                    entries.addAll(files.get(index).entries);
                }
            }
        }
        IEditorFilter filter = editor.getFilter();
        if (options.isApplyFilter() && filter != null) {
            entries.removeIf(ste -> !filter.allowed(ste));
        }
        return entries;
    }

    private static void writeInBackground(List<SegmentCsvExporter.Row> rows, CsvExportOptions options,
            Path target, String targetName) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SegmentCsvExporter.export(rows, options, target);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Objects.requireNonNull(Core.getMainWindow()).showStatusMessageRB(
                            "GUI_EDITORWINDOW_EXPORT_CSV_DONE", rows.size(), targetName);
                } catch (Exception ex) {
                    Objects.requireNonNull(Core.getMainWindow()).displayErrorRB(ex,
                            "GUI_EDITORWINDOW_EXPORT_CSV_ERROR", targetName);
                }
            }
        }.execute();
    }
}
