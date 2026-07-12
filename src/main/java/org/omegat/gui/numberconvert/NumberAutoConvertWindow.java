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
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.Bidi;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.numberconvert.NumberAutoConvertScanner.Proposal;
import org.omegat.util.NumberAutoConverter.DataType;
import org.omegat.util.NumeralValueParser;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Window that scans the project for number-only segments (feature request
 * #794), proposes a locale-aware conversion for each and applies the selected
 * ones as translations marked {@link TMXEntry.ExternalLinked#xNUMBER}.
 *
 * Modeled on the Issues window but with its own model: an editable accept
 * column, original/preview/confidence columns, a minimum-confidence threshold
 * and a bulk apply, because a convertible number is an action to batch, not a
 * problem to fix one by one.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumberAutoConvertWindow {

    private static final int COL_ACCEPT = 0;
    private static final int COL_SEGMENT = 1;
    private static final int COL_TYPE = 2;
    private static final int COL_ORIGINAL = 3;
    private static final int COL_PREVIEW = 4;
    private static final int COL_CONFIDENCE = 5;

    private final JDialog dialog;
    private final Map<DataType, JCheckBox> typeChecks = new EnumMap<>(DataType.class);
    private final JCheckBox romanCheck = new JCheckBox(OStrings.getString("NUMBERCONVERT_ROMAN"), false);
    private final JRadioButton scopeAll = new JRadioButton(OStrings.getString("ISSUES_TYPE_ALL"), true);
    private final JRadioButton scopeFile = new JRadioButton(OStrings.getString("NUMBERCONVERT_SCOPE_FILE"));
    private final JRadioButton scopeFilter = new JRadioButton(OStrings.getString("NUMBERCONVERT_SCOPE_FILTER"));
    private final JSpinner minConfidence = new JSpinner(new SpinnerNumberModel(50, 0, 100, 5));
    private final ResultsModel model = new ResultsModel();
    private final JTable table = new JTable(model);
    private final JPanel controls = new JPanel();
    private final JButton scanButton = new JButton(OStrings.getString("NUMBERCONVERT_SCAN"));
    private final JButton applyButton = new JButton(OStrings.getString("NUMBERCONVERT_APPLY"));
    private final JButton selectAllButton = new JButton(OStrings.getString("NUMBERCONVERT_SELECT_ALL"));
    private final JButton invertButton = new JButton(OStrings.getString("NUMBERCONVERT_INVERT"));
    private final JLabel status = new JLabel(" ");
    private final IProjectEventListener projectListener;

    public NumberAutoConvertWindow(Window parent) {
        dialog = new JDialog(parent);
        dialog.setTitle(OStrings.getString("NUMBERCONVERT_TITLE"));
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setEscapeClosable(dialog);
        buildUI();

        // Close the window if the project is closed underneath it.
        projectListener = eventType -> {
            if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE) {
                SwingUtilities.invokeLater(dialog::dispose);
            }
        };
        CoreEvents.registerProjectChangeListener(projectListener);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                CoreEvents.unregisterProjectChangeListener(projectListener);
            }
        });
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                controls.revalidate();
            }
        });

        dialog.setMinimumSize(new Dimension(560, 420));
        dialog.setSize(840, 560);
        dialog.setLocationRelativeTo(parent);
    }

    public void show() {
        dialog.setVisible(true);
        // WrapLayout derives its height from the actual width, which is only
        // known once shown; revalidate so wrapped rows (such as the Roman
        // option at the end of the types row) are not clipped on first display.
        SwingUtilities.invokeLater(() -> {
            controls.revalidate();
            dialog.getContentPane().revalidate();
        });
    }

    private void buildUI() {
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        for (DataType t : DataType.values()) {
            typeChecks.put(t, new JCheckBox(typeName(t), true));
        }
        JPanel typesRow = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 2));
        typesRow.add(new JLabel(OStrings.getString("NUMBERCONVERT_TYPES")));
        for (DataType t : DataType.values()) {
            if (t == DataType.INTEGER) {
                // Roman is recognized within the integer type, so the two share
                // a box and Roman is disabled when Integer is unchecked.
                JPanel integerBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                integerBox.setBorder(BorderFactory.createEtchedBorder());
                integerBox.add(typeChecks.get(DataType.INTEGER));
                integerBox.add(romanCheck);
                typesRow.add(integerBox);
            } else {
                typesRow.add(typeChecks.get(t));
            }
        }
        JCheckBox integerCheck = typeChecks.get(DataType.INTEGER);
        romanCheck.setEnabled(integerCheck.isSelected());
        integerCheck.addActionListener(e -> romanCheck.setEnabled(integerCheck.isSelected()));

        JPanel scopeRow = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 2));
        scopeRow.add(new JLabel(OStrings.getString("NUMBERCONVERT_SCOPE")));
        ButtonGroup scope = new ButtonGroup();
        scope.add(scopeAll);
        scope.add(scopeFile);
        scope.add(scopeFilter);
        scopeRow.add(scopeAll);
        scopeRow.add(scopeFile);
        scopeRow.add(scopeFilter);

        JPanel runRow = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 2));
        runRow.add(new JLabel(OStrings.getString("NUMBERCONVERT_MIN_CONFIDENCE")));
        runRow.add(minConfidence);
        runRow.add(scanButton);

        controls.add(typesRow);
        controls.add(scopeRow);
        controls.add(runRow);

        minConfidence.addChangeListener(e -> model.applyThreshold(threshold(), true));
        scanButton.addActionListener(e -> scan());
        applyButton.addActionListener(e -> apply());
        selectAllButton.addActionListener(e -> selectAll());
        invertButton.addActionListener(e -> model.invertAccept());

        setupTable();

        JPanel south = new JPanel(new BorderLayout(8, 0));
        south.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        JPanel selectButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        selectButtons.add(selectAllButton);
        selectButtons.add(invertButton);
        south.add(selectButtons, BorderLayout.WEST);
        south.add(status, BorderLayout.CENTER);
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton closeButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(closeButton, OStrings.getString("BUTTON_CLOSE"));
        closeButton.addActionListener(e -> dialog.dispose());
        actionButtons.add(applyButton);
        actionButtons.add(closeButton);
        south.add(actionButtons, BorderLayout.EAST);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(controls, BorderLayout.NORTH);
        dialog.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.getContentPane().add(south, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(scanButton);
        updateApplyButton();
    }

    private void setupTable() {
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        model.addTableModelListener(e -> updateApplyButton());

        TableColumnModel cols = table.getColumnModel();
        cols.getColumn(COL_ACCEPT).setPreferredWidth(76);
        cols.getColumn(COL_ACCEPT).setMaxWidth(100);
        cols.getColumn(COL_SEGMENT).setPreferredWidth(64);
        cols.getColumn(COL_SEGMENT).setMaxWidth(90);
        cols.getColumn(COL_TYPE).setPreferredWidth(84);
        cols.getColumn(COL_TYPE).setMaxWidth(140);
        cols.getColumn(COL_CONFIDENCE).setPreferredWidth(80);
        cols.getColumn(COL_CONFIDENCE).setMaxWidth(110);
        cols.getColumn(COL_ORIGINAL).setPreferredWidth(220);
        cols.getColumn(COL_PREVIEW).setPreferredWidth(220);

        // Right-align (trailing) so numerals read naturally, with the cell
        // orientation following the value's own script direction.
        DirectionalRenderer numeral = new DirectionalRenderer();
        cols.getColumn(COL_ORIGINAL).setCellRenderer(numeral);
        cols.getColumn(COL_PREVIEW).setCellRenderer(numeral);
        DefaultTableCellRenderer trailing = new DefaultTableCellRenderer();
        trailing.setHorizontalAlignment(SwingConstants.TRAILING);
        cols.getColumn(COL_CONFIDENCE).setCellRenderer(trailing);

        // Segment numbers are clickable links to jump into the editor.
        cols.getColumn(COL_SEGMENT).setCellRenderer(new SegmentLinkRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && table.convertColumnIndexToModel(col) == COL_SEGMENT) {
                        gotoSegment(row);
                    }
                }
            }
        });

        // Header-click sorting with a per-column active-sort indicator.
        JTableHeader header = table.getTableHeader();
        TableCellRenderer baseHeader = header.getDefaultRenderer();
        header.setDefaultRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            Component comp = baseHeader.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row,
                    column);
            if (comp instanceof JLabel) {
                ((JLabel) comp).setText(model.headerText(tbl.convertColumnIndexToModel(column)));
            }
            return comp;
        });
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewCol >= 0) {
                    model.toggleSort(table.convertColumnIndexToModel(viewCol));
                    header.repaint();
                }
            }
        });

        installContextMenu();
    }

    private void installContextMenu() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem gotoItem = new JMenuItem(OStrings.getString("NUMBERCONVERT_GOTO"));
        gotoItem.addActionListener(e -> gotoSegment(table.getSelectedRow()));
        JMenuItem copyOriginal = new JMenuItem(OStrings.getString("NUMBERCONVERT_COPY_ORIGINAL"));
        copyOriginal.addActionListener(e -> copySelected(COL_ORIGINAL));
        JMenuItem copyPreview = new JMenuItem(OStrings.getString("NUMBERCONVERT_COPY_PREVIEW"));
        copyPreview.addActionListener(e -> copySelected(COL_PREVIEW));
        popup.add(gotoItem);
        popup.addSeparator();
        popup.add(copyOriginal);
        popup.add(copyPreview);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybePopup(e);
            }

            private void maybePopup(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    popup.show(table, e.getX(), e.getY());
                }
            }
        });
    }

    private void copySelected(int column) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        String text = column == COL_ORIGINAL ? model.rows.get(row).proposal.getSource()
                : model.rows.get(row).proposal.getTarget();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
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

    private void selectAll() {
        if (model.acceptedCount() > 0) {
            int choice = JOptionPane.showConfirmDialog(dialog,
                    OStrings.getString("NUMBERCONVERT_CONFIRM_SELECT_ALL"),
                    OStrings.getString("NUMBERCONVERT_TITLE"), JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        model.setAllAccept(true);
    }

    private void setControlsEnabled(boolean enabled) {
        setEnabledRecursive(controls, enabled);
        selectAllButton.setEnabled(enabled);
        invertButton.setEnabled(enabled);
    }

    private static void setEnabledRecursive(Container container, boolean enabled) {
        for (Component c : container.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container) {
                setEnabledRecursive((Container) c, enabled);
            }
        }
    }

    private void scan() {
        IProject project = Core.getProject();
        if (project == null || !project.isProjectLoaded()) {
            status.setText(OStrings.getString("NUMBERCONVERT_STATUS_NOPROJECT"));
            return;
        }
        Set<DataType> types = enabledTypes();
        boolean allowRoman = romanCheck.isSelected();
        List<SourceTextEntry> entries = scopeEntries(project);
        ProjectProperties props = project.getProjectProperties();
        Locale src = props.getSourceLanguage().getLocale();
        Locale tgt = props.getTargetLanguage().getLocale();
        model.setLocales(src, tgt);

        setControlsEnabled(false);
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
                            src, tgt, types, allowRoman);
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
                } catch (Exception ex) {
                    status.setText(ex.getLocalizedMessage());
                } finally {
                    setControlsEnabled(true);
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
    }

    private void gotoSegment(int row) {
        if (row >= 0 && row < model.rows.size()) {
            Core.getEditor().gotoEntry(model.rows.get(row).proposal.getSegmentNumber());
        }
    }

    private void updateApplyButton() {
        int n = model.acceptedCount();
        applyButton.setText(OStrings.getString("NUMBERCONVERT_APPLY") + " (" + n + ")");
        applyButton.setEnabled(n > 0);
    }

    private static String typeName(DataType t) {
        return OStrings.getString("NUMBERCONVERT_TYPE_" + t.name());
    }

    /** Right-aligned renderer whose orientation follows the value's script. */
    private static final class DirectionalRenderer extends DefaultTableCellRenderer {
        DirectionalRenderer() {
            setHorizontalAlignment(SwingConstants.TRAILING);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
            String text = value == null ? "" : value.toString();
            boolean rtl = Bidi.requiresBidi(text.toCharArray(), 0, text.length());
            comp.setComponentOrientation(
                    rtl ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
            return comp;
        }
    }

    /** Renders the segment number as an underlined link. */
    private static final class SegmentLinkRenderer extends DefaultTableCellRenderer {
        private static final java.awt.Color LINK = new java.awt.Color(0x3E, 0x8E, 0xDE);

        SegmentLinkRenderer() {
            setHorizontalAlignment(SwingConstants.TRAILING);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
            setText("<html><u>" + value + "</u></html>");
            if (!isSelected) {
                setForeground(LINK);
            }
            return this;
        }
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

    /** Sort mode for the Original and Preview columns, cycled on header click. */
    private enum ValueSort {
        ALPHA, NUMERIC, RHYME, LENGTH
    }

    private final class ResultsModel extends AbstractTableModel {
        private final List<Row> rows = new ArrayList<>();
        private final String[] columns = {
                OStrings.getString("NUMBERCONVERT_COL_ACCEPT"),
                OStrings.getString("ISSUES_TABLE_COLUMN_ENTRY_NUM"),
                OStrings.getString("ISSUES_TABLE_COLUMN_TYPE"), OStrings.getString("NUMBERCONVERT_COL_ORIGINAL"),
                OStrings.getString("NUMBERCONVERT_COL_PREVIEW"),
                OStrings.getString("NUMBERCONVERT_COL_CONFIDENCE") };
        private Collator sourceCollator = Collator.getInstance();
        private Collator targetCollator = Collator.getInstance();
        private final Collator typeCollator = Collator.getInstance();
        private int sortColumn = COL_SEGMENT;
        private final int[] clickIndex = new int[columns.length];

        void setLocales(Locale src, Locale tgt) {
            sourceCollator = Collator.getInstance(src);
            targetCollator = Collator.getInstance(tgt);
        }

        void setRows(List<Row> newRows, int thresholdPercent) {
            rows.clear();
            rows.addAll(newRows);
            applyThreshold(thresholdPercent, false);
            sort();
            fireTableDataChanged();
        }

        void applyThreshold(int thresholdPercent, boolean fire) {
            for (Row r : rows) {
                r.accept = r.proposal.getConfidence() * 100.0 >= thresholdPercent;
            }
            if (fire) {
                fireTableDataChanged();
            }
        }

        void setAllAccept(boolean accept) {
            rows.forEach(r -> r.accept = accept);
            fireTableDataChanged();
        }

        void invertAccept() {
            rows.forEach(r -> r.accept = !r.accept);
            fireTableDataChanged();
        }

        int acceptedCount() {
            int n = 0;
            for (Row r : rows) {
                if (r.accept) {
                    n++;
                }
            }
            return n;
        }

        void removeAccepted() {
            rows.removeIf(r -> r.accept);
            fireTableDataChanged();
        }

        void toggleSort(int column) {
            if (column == sortColumn) {
                clickIndex[column] = (clickIndex[column] + 1) % statesFor(column);
            } else {
                sortColumn = column;
                clickIndex[column] = 0;
            }
            sort();
            fireTableDataChanged();
        }

        private int statesFor(int column) {
            return isValueColumn(column) ? ValueSort.values().length * 2 : 2;
        }

        private boolean isValueColumn(int column) {
            return column == COL_ORIGINAL || column == COL_PREVIEW;
        }

        private void sort() {
            rows.sort(currentComparator());
        }

        private Comparator<Row> currentComparator() {
            int idx = clickIndex[sortColumn];
            boolean asc = idx % 2 == 0;
            Comparator<Row> cmp;
            switch (sortColumn) {
            case COL_ACCEPT:
                cmp = Comparator.comparing((Row r) -> r.accept);
                break;
            case COL_TYPE:
                cmp = (a, b) -> typeCollator.compare(typeName(a.proposal.getType()),
                        typeName(b.proposal.getType()));
                break;
            case COL_ORIGINAL:
                cmp = valueComparator(true, ValueSort.values()[(idx / 2) % ValueSort.values().length]);
                break;
            case COL_PREVIEW:
                cmp = valueComparator(false, ValueSort.values()[(idx / 2) % ValueSort.values().length]);
                break;
            case COL_CONFIDENCE:
                cmp = Comparator.comparingDouble(r -> r.proposal.getConfidence());
                break;
            case COL_SEGMENT:
            default:
                cmp = Comparator.comparingInt(r -> r.proposal.getSegmentNumber());
            }
            return asc ? cmp : cmp.reversed();
        }

        private Comparator<Row> valueComparator(boolean original, ValueSort mode) {
            Collator collator = original ? sourceCollator : targetCollator;
            java.util.function.Function<Row, String> text =
                    r -> original ? r.proposal.getSource() : r.proposal.getTarget();
            switch (mode) {
            case NUMERIC:
                return (a, b) -> {
                    Optional<NumeralValueParser.Rational> ra = NumeralValueParser.firstValue(text.apply(a));
                    Optional<NumeralValueParser.Rational> rb = NumeralValueParser.firstValue(text.apply(b));
                    if (ra.isPresent() && rb.isPresent()) {
                        return ra.get().compareTo(rb.get());
                    }
                    return ra.isPresent() ? -1 : rb.isPresent() ? 1 : 0;
                };
            case RHYME:
                return (a, b) -> collator.compare(reverse(text.apply(a)), reverse(text.apply(b)));
            case LENGTH:
                return Comparator.comparingInt(r -> text.apply(r).length());
            case ALPHA:
            default:
                return (a, b) -> collator.compare(text.apply(a), text.apply(b));
            }
        }

        private String reverse(String s) {
            return new StringBuilder(s).reverse().toString();
        }

        String headerText(int column) {
            if (column != sortColumn) {
                return columns[column];
            }
            int idx = clickIndex[column];
            // Indicator goes in front of the title so it stays visible even
            // when the column is narrow.
            String arrow = idx % 2 == 0 ? "▲" : "▼";
            if (isValueColumn(column)) {
                ValueSort mode = ValueSort.values()[(idx / 2) % ValueSort.values().length];
                return arrow + modeTag(mode) + " " + columns[column];
            }
            return arrow + " " + columns[column];
        }

        private String modeTag(ValueSort mode) {
            switch (mode) {
            case NUMERIC:
                return "123";
            case RHYME:
                return "…zyx";
            case LENGTH:
                return "len";
            case ALPHA:
            default:
                return "abc";
            }
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
            return columnIndex == COL_ACCEPT ? Boolean.class
                    : columnIndex == COL_SEGMENT ? Integer.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == COL_ACCEPT;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);
            switch (columnIndex) {
            case COL_ACCEPT:
                return r.accept;
            case COL_SEGMENT:
                return r.proposal.getSegmentNumber();
            case COL_TYPE:
                return typeName(r.proposal.getType());
            case COL_ORIGINAL:
                return r.proposal.getSource();
            case COL_PREVIEW:
                return r.proposal.getTarget();
            case COL_CONFIDENCE:
                return Math.round(r.proposal.getConfidence() * 100.0) + "%";
            default:
                return null;
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex == COL_ACCEPT) {
                rows.get(rowIndex).accept = (Boolean) value;
            }
        }
    }

    /** A FlowLayout that wraps its components onto new rows when too narrow. */
    private static final class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension d = layoutSize(target, false);
            d.width -= getHgap() + 1;
            return d;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) {
                    targetWidth = Integer.MAX_VALUE;
                }
                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;
                for (Component m : target.getComponents()) {
                    if (!m.isVisible()) {
                        continue;
                    }
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth && rowWidth > 0) {
                        dim.width = Math.max(dim.width, rowWidth);
                        dim.height += rowHeight + vgap;
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    rowWidth += d.width + hgap;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight;
                dim.width += insets.left + insets.right + hgap * 2;
                dim.height += insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }
    }
}
