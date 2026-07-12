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

package org.omegat.gui.numberconvert;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.numberconvert.NumberAutoConvertScanner.Proposal;
import org.omegat.util.NumberAutoConverter.DataType;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Window that scans the project for number-only segments (feature request
 * #794), proposes a locale-aware conversion for each and applies the selected
 * ones as translations marked {@link TMXEntry.ExternalLinked#xNUMBER}.
 *
 * It is modeled on the Issues window (a data type checklist to include before
 * scanning, a scope selector, an asynchronous scan, a results table, jump to
 * segment on selection) but has its own model: an editable accept column, an
 * original and a preview column, a minimum-confidence threshold and a single
 * bulk apply, because a convertible number is an action to batch, not a problem
 * to fix one by one.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumberAutoConvertWindow {

    private final JDialog dialog;
    private final Map<DataType, JCheckBox> typeChecks = new EnumMap<>(DataType.class);
    private final JRadioButton scopeAll = new JRadioButton(OStrings.getString("ISSUES_TYPE_ALL"), true);
    private final JRadioButton scopeFile = new JRadioButton(OStrings.getString("NUMBERCONVERT_SCOPE_FILE"));
    private final JRadioButton scopeFilter = new JRadioButton(OStrings.getString("NUMBERCONVERT_SCOPE_FILTER"));
    private final JSpinner minConfidence = new JSpinner(new SpinnerNumberModel(50, 0, 100, 5));
    private final ResultsModel model = new ResultsModel();
    private final JTable table = new JTable(model);
    private final JButton scanButton = new JButton(OStrings.getString("NUMBERCONVERT_SCAN"));
    private final JButton applyButton = new JButton(OStrings.getString("NUMBERCONVERT_APPLY"));
    private final JLabel status = new JLabel(" ");

    public NumberAutoConvertWindow(Window parent) {
        dialog = new JDialog(parent);
        dialog.setTitle(OStrings.getString("NUMBERCONVERT_TITLE"));
        StaticUIUtils.setEscapeClosable(dialog);
        buildUI();
        dialog.setSize(760, 520);
        dialog.setLocationRelativeTo(parent);
    }

    public void show() {
        dialog.setVisible(true);
    }

    private void buildUI() {
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        JPanel typesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        typesRow.add(new JLabel(OStrings.getString("NUMBERCONVERT_TYPES")));
        for (DataType t : DataType.values()) {
            JCheckBox cb = new JCheckBox(typeName(t), true);
            typeChecks.put(t, cb);
            typesRow.add(cb);
        }

        JPanel scopeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        scopeRow.add(new JLabel(OStrings.getString("NUMBERCONVERT_SCOPE")));
        ButtonGroup scope = new ButtonGroup();
        scope.add(scopeAll);
        scope.add(scopeFile);
        scope.add(scopeFilter);
        scopeRow.add(scopeAll);
        scopeRow.add(scopeFile);
        scopeRow.add(scopeFilter);

        JPanel runRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        runRow.add(new JLabel(OStrings.getString("NUMBERCONVERT_MIN_CONFIDENCE")));
        runRow.add(minConfidence);
        runRow.add(scanButton);

        controls.add(typesRow);
        controls.add(scopeRow);
        controls.add(runRow);

        minConfidence.addChangeListener(e -> model.applyThreshold(threshold()));
        scanButton.addActionListener(e -> scan());
        applyButton.addActionListener(e -> apply());
        applyButton.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> jumpToSelection(e.getValueIsAdjusting()));

        JPanel south = new JPanel(new BorderLayout());
        south.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        south.add(status, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton closeButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(closeButton, OStrings.getString("BUTTON_CLOSE"));
        closeButton.addActionListener(e -> dialog.dispose());
        buttons.add(applyButton);
        buttons.add(closeButton);
        south.add(buttons, BorderLayout.EAST);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(controls, BorderLayout.NORTH);
        dialog.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.getContentPane().add(south, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(scanButton);
    }

    private int threshold() {
        return (Integer) minConfidence.getValue();
    }

    private Set<DataType> enabledTypes() {
        Set<DataType> types = EnumSet.noneOf(DataType.class);
        typeChecks.forEach((t, cb) -> {
            if (cb.isSelected()) {
                types.add(t);
            }
        });
        return types;
    }

    private void scan() {
        IProject project = Core.getProject();
        if (project == null || !project.isProjectLoaded()) {
            status.setText(OStrings.getString("NUMBERCONVERT_STATUS_NOPROJECT"));
            return;
        }
        Set<DataType> types = enabledTypes();
        List<SourceTextEntry> entries = scopeEntries(project);
        ProjectProperties props = project.getProjectProperties();
        Locale src = props.getSourceLanguage().getLocale();
        Locale tgt = props.getTargetLanguage().getLocale();

        scanButton.setEnabled(false);
        applyButton.setEnabled(false);
        status.setText(OStrings.getString("NUMBERCONVERT_STATUS_SCANNING"));

        new SwingWorker<List<Row>, Void>() {
            @Override
            protected List<Row> doInBackground() {
                List<Row> rows = new ArrayList<>();
                for (SourceTextEntry ste : entries) {
                    TMXEntry info = project.getTranslationInfo(ste);
                    if (info != null && info.isTranslated()) {
                        continue; // never overwrite an existing translation
                    }
                    Optional<Proposal> p = NumberAutoConvertScanner.propose(ste.entryNum(), ste.getSrcText(),
                            src, tgt, types);
                    p.ifPresent(prop -> rows.add(new Row(ste, prop)));
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    model.setRows(get(), threshold());
                    status.setText(MessageFormat.format(OStrings.getString("NUMBERCONVERT_STATUS_FOUND"),
                            model.getRowCount()));
                    applyButton.setEnabled(model.getRowCount() > 0);
                } catch (Exception ex) {
                    status.setText(ex.getLocalizedMessage());
                } finally {
                    scanButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private List<SourceTextEntry> scopeEntries(IProject project) {
        if (scopeFile.isSelected()) {
            List<SourceTextEntry> entries = new ArrayList<>();
            String currentFile = Core.getEditor().getCurrentFile();
            for (FileInfo fi : project.getProjectFiles()) {
                if (fi.filePath.equals(currentFile)) {
                    entries.addAll(fi.entries);
                }
            }
            return entries;
        }
        if (scopeFilter.isSelected()) {
            IEditorFilter filter = Core.getEditor().getFilter();
            List<SourceTextEntry> entries = new ArrayList<>();
            for (SourceTextEntry ste : project.getAllEntries()) {
                if (filter == null || filter.allowed(ste)) {
                    entries.add(ste);
                }
            }
            return entries;
        }
        return project.getAllEntries();
    }

    private void apply() {
        IProject project = Core.getProject();
        if (project == null || !project.isProjectLoaded()) {
            return;
        }
        int applied = 0;
        for (Row row : model.rows) {
            if (!row.accept) {
                continue;
            }
            PrepareTMXEntry prep = new PrepareTMXEntry(row.entry.getSrcText(), row.proposal.getTarget());
            project.setTranslation(row.entry, prep, true, TMXEntry.ExternalLinked.xNUMBER);
            applied++;
        }
        Core.getEditor().refreshView(false);
        status.setText(MessageFormat.format(OStrings.getString("NUMBERCONVERT_STATUS_APPLIED"), applied));
        model.removeAccepted();
        applyButton.setEnabled(model.getRowCount() > 0);
    }

    private void jumpToSelection(boolean adjusting) {
        if (adjusting) {
            return;
        }
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        Row row = model.rows.get(table.convertRowIndexToModel(viewRow));
        Core.getEditor().gotoEntry(row.proposal.getSegmentNumber());
    }

    private static String typeName(DataType t) {
        return OStrings.getString("NUMBERCONVERT_TYPE_" + t.name());
    }

    private static final class Row {
        private final SourceTextEntry entry;
        private final Proposal proposal;
        private boolean accept;

        Row(SourceTextEntry entry, Proposal proposal) {
            this.entry = entry;
            this.proposal = proposal;
        }
    }

    private static final class ResultsModel extends AbstractTableModel {
        private final List<Row> rows = new ArrayList<>();
        private final String[] columns = {
                OStrings.getString("NUMBERCONVERT_COL_ACCEPT"),
                OStrings.getString("ISSUES_TABLE_COLUMN_ENTRY_NUM"),
                OStrings.getString("ISSUES_TABLE_COLUMN_TYPE"), OStrings.getString("NUMBERCONVERT_COL_ORIGINAL"),
                OStrings.getString("NUMBERCONVERT_COL_PREVIEW") };

        void setRows(List<Row> newRows, int thresholdPercent) {
            rows.clear();
            rows.addAll(newRows);
            applyThreshold(thresholdPercent);
        }

        void applyThreshold(int thresholdPercent) {
            for (Row r : rows) {
                r.accept = r.proposal.getConfidence() * 100.0 >= thresholdPercent;
            }
            fireTableDataChanged();
        }

        void removeAccepted() {
            rows.removeIf(r -> r.accept);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : columnIndex == 1 ? Integer.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return r.accept;
            case 1:
                return r.proposal.getSegmentNumber();
            case 2:
                return typeName(r.proposal.getType());
            case 3:
                return r.proposal.getSource();
            case 4:
                return r.proposal.getTarget();
            default:
                return null;
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                rows.get(rowIndex).accept = (Boolean) value;
            }
        }
    }
}
