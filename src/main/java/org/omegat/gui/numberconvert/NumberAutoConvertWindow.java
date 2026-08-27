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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.Bidi;
import java.text.Collator;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.jspecify.annotations.Nullable;

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
import org.omegat.util.Language;
import org.omegat.util.NumberAutoConverter.ConfidenceFactor;
import org.omegat.util.NumberAutoConverter.DataType;
import org.omegat.util.NumberAutoConverter.RenderOptions;
import org.omegat.util.NumeralValueParser;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.ResourcesUtil;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Window that scans the project for number-only segments (feature request
 * #794), proposes a locale-aware conversion for each and applies the selected
 * ones as translations marked {@link TMXEntry.ExternalLinked#xNUMBER}.
 *
 * Modeled on the Issues window (an ordinary minimizable frame, asynchronous
 * scan, jump to segment) but with its own model: an editable accept column,
 * original/status/preview/confidence columns, a minimum-confidence threshold
 * and a bulk apply, because a convertible number is an action to batch, not a
 * problem to fix one by one. The convertible types sit in a column next to the
 * target-format options, with connector lines showing which format option
 * affects which type.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumberAutoConvertWindow {

    private static final int COL_ACCEPT = 0;
    private static final int COL_SEGMENT = 1;
    private static final int COL_STATUS = 2;
    private static final int COL_TYPE = 3;
    private static final int COL_ORIGINAL = 4;
    private static final int COL_PREVIEW = 5;
    private static final int COL_CONFIDENCE = 6;
    private static final int COL_GEAR = 7;

    private static final String PREF_GROUPING = "number_autoconvert_grouping";
    private static final String PREF_FRACTION = "number_autoconvert_fraction";
    private static final String PREF_STYLE = "number_autoconvert_style";
    private static final String PREF_TABLE_WIDTHS = "number_autoconvert_col_widths";
    private static final String PREF_SORT_COL = "number_autoconvert_sort_col";
    private static final String PREF_SORT_IDX = "number_autoconvert_sort_idx";

    // Same gear iconography as the Issues window's action column.
    private static final Icon GEAR_ICON = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.active.png"));
    private static final Icon GEAR_ICON_INACTIVE = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.inactive.png"));
    private static final Icon GEAR_ICON_PRESSED = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.pressed.png"));
    private static final Icon GEAR_ICON_INVISIBLE = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // draw nothing to hide the icon
        }

        @Override
        public int getIconWidth() {
            return GEAR_ICON.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return GEAR_ICON.getIconHeight();
        }
    };

    private final JFrame frame;
    private final JLabel infoSource = new JLabel(" ");
    private final JLabel infoTarget = new JLabel(" ");
    private final Map<DataType, JCheckBox> typeChecks = new EnumMap<>(DataType.class);
    private final Map<DataType, JComponent> typeRows = new EnumMap<>(DataType.class);
    private final JCheckBox romanCheck = new JCheckBox(OStrings.getString("NUMBERCONVERT_ROMAN"), false);
    private final JRadioButton scopeAll = new JRadioButton(OStrings.getString("ISSUES_TYPE_ALL"), true);
    private final JRadioButton scopeFile = new JRadioButton(OStrings.getString("NUMBERCONVERT_SCOPE_FILE"));
    private final JRadioButton scopeFilter = new JRadioButton(OStrings.getString("NUMBERCONVERT_SCOPE_FILTER"));
    private final JCheckBox includeTranslatedCheck =
            new JCheckBox(OStrings.getString("NUMBERCONVERT_INCLUDE_TRANSLATED"), false);
    private final JSpinner minConfidence = new JSpinner(new SpinnerNumberModel(50, 0, 100, 5));
    private final JComboBox<RenderOptions.Grouping> groupingCombo =
            new JComboBox<>(RenderOptions.Grouping.values());
    private final JComboBox<RenderOptions.Fraction> fractionCombo =
            new JComboBox<>(RenderOptions.Fraction.values());
    private final JComboBox<RenderOptions.Style> styleCombo = new JComboBox<>(RenderOptions.Style.values());
    private @Nullable JComponent groupingRow;
    private @Nullable JComponent fractionRow;
    private @Nullable JComponent styleRow;
    private @Nullable Locale lastSrc;
    private @Nullable Locale lastTgt;
    private Set<DataType> lastTypes = EnumSet.noneOf(DataType.class);
    private boolean lastAllowRoman;
    private boolean lastIncludeTranslated;
    private int previousThreshold = 50;
    private boolean revertingThreshold;
    private final ResultsModel model = new ResultsModel();
    private final JTable table = new JTable(model) {
        @Override
        public String getToolTipText(MouseEvent e) {
            // Everything known about the segment plus the confidence
            // breakdown is shown for the whole row.
            int row = rowAtPoint(e.getPoint());
            if (row >= 0 && row < model.rows.size()) {
                return rowTooltip(model.rows.get(row));
            }
            return super.getToolTipText(e);
        }
    };
    private final JPanel controls = new JPanel();
    private final JButton scanButton = new JButton(OStrings.getString("NUMBERCONVERT_SCAN"));
    private final JButton applyButton = new JButton(OStrings.getString("NUMBERCONVERT_APPLY"));
    private final JButton filterButton = new JButton(OStrings.getString("NUMBERCONVERT_FILTER"));
    private final JButton selectAllButton = new JButton(OStrings.getString("NUMBERCONVERT_SELECT_ALL"));
    private final JButton invertButton = new JButton(OStrings.getString("NUMBERCONVERT_INVERT"));
    private final JLabel status = new JLabel(" ");
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JPopupMenu rowPopup = new JPopupMenu();
    private final IProjectEventListener projectListener;

    public NumberAutoConvertWindow(Window parent) {
        frame = new JFrame(OStrings.getString("NUMBERCONVERT_TITLE"));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setEscapeClosable(frame);
        buildUI();

        // Close the window if the project is closed underneath it.
        projectListener = eventType -> {
            if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE) {
                SwingUtilities.invokeLater(frame::dispose);
            }
        };
        CoreEvents.registerProjectChangeListener(projectListener);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                saveTableState();
                CoreEvents.unregisterProjectChangeListener(projectListener);
            }
        });

        frame.setMinimumSize(new Dimension(600, 480));
        frame.setSize(980, 620);
        frame.setLocationRelativeTo(parent);
        updateInfoLabels();
    }

    public void show() {
        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            controls.revalidate();
            frame.getContentPane().revalidate();
        });
    }

    private void buildUI() {
        // --- types column with format options and connector lines ----------
        for (DataType t : DataType.values()) {
            JCheckBox cb = new JCheckBox(typeName(t), true);
            cb.addActionListener(e -> onScanOptionChanged());
            typeChecks.put(t, cb);
        }
        romanCheck.addActionListener(e -> onScanOptionChanged());
        JPanel typesPanel = new JPanel();
        typesPanel.setLayout(new BoxLayout(typesPanel, BoxLayout.Y_AXIS));
        for (DataType t : DataType.values()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            if (t == DataType.INTEGER) {
                // Roman is recognized within the integer type: one shared box,
                // Roman disabled while Integer is unchecked.
                row.setBorder(BorderFactory.createEtchedBorder());
                row.add(typeChecks.get(DataType.INTEGER));
                row.add(romanCheck);
            } else {
                row.add(typeChecks.get(t));
            }
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height + 4));
            typesPanel.add(row);
            typeRows.put(t, row);
        }
        JCheckBox integerCheck = typeChecks.get(DataType.INTEGER);
        romanCheck.setEnabled(integerCheck.isSelected());
        integerCheck.addActionListener(e -> romanCheck.setEnabled(integerCheck.isSelected()));

        groupingRow = formatRow(OStrings.getString("NUMBERCONVERT_FMT_GROUPING"), groupingCombo);
        fractionRow = formatRow(OStrings.getString("NUMBERCONVERT_FMT_FRACTION"), fractionCombo);
        styleRow = formatRow(OStrings.getString("NUMBERCONVERT_FMT_STYLE"), styleCombo);
        JPanel formatsPanel = new JPanel();
        formatsPanel.setLayout(new BoxLayout(formatsPanel, BoxLayout.Y_AXIS));
        formatsPanel.add(Box.createVerticalGlue());
        formatsPanel.add(groupingRow);
        formatsPanel.add(Box.createVerticalStrut(10));
        formatsPanel.add(fractionRow);
        formatsPanel.add(Box.createVerticalStrut(10));
        formatsPanel.add(styleRow);
        formatsPanel.add(Box.createVerticalGlue());

        // The whole matrix is anchored to the LEFT with everything at its
        // preferred size: a FlowLayout wrapper never stretches its child, so
        // the connector keeps its fixed width and nothing wanders on resize.
        // The info labels live OUTSIDE the columns (own rows above) so their
        // length cannot inflate the column widths.
        ConnectorPanel connector = new ConnectorPanel();
        JPanel matrixInner = new JPanel();
        matrixInner.setLayout(new BoxLayout(matrixInner, BoxLayout.X_AXIS));
        matrixInner.add(typesPanel);
        matrixInner.add(connector);
        matrixInner.add(formatsPanel);
        JPanel matrix = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        matrix.add(matrixInner);
        matrix.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        // --- scope and scan rows -------------------------------------------
        JPanel scopeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        scopeRow.add(new JLabel(OStrings.getString("NUMBERCONVERT_SCOPE")));
        ButtonGroup scope = new ButtonGroup();
        scope.add(scopeAll);
        scope.add(scopeFile);
        scope.add(scopeFilter);
        scopeRow.add(scopeAll);
        scopeRow.add(scopeFile);
        scopeRow.add(scopeFilter);
        scopeRow.add(includeTranslatedCheck);
        // A hit list reflects option changes immediately: a scope change needs
        // a real re-scan, while the checkboxes only re-scan when they demand
        // data the last scan did not collect (otherwise they filter locally).
        scopeAll.addActionListener(e -> rescanIfResults());
        scopeFile.addActionListener(e -> rescanIfResults());
        scopeFilter.addActionListener(e -> rescanIfResults());
        includeTranslatedCheck.addActionListener(e -> onScanOptionChanged());

        JPanel runRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        runRow.add(new JLabel(OStrings.getString("NUMBERCONVERT_MIN_CONFIDENCE")));
        runRow.add(minConfidence);
        runRow.add(scanButton);
        progress.setStringPainted(true);
        progress.setVisible(false);
        runRow.add(progress);

        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(infoLine(infoSource));
        controls.add(infoLine(infoTarget));
        controls.add(matrix);
        controls.add(scopeRow);
        controls.add(runRow);
        configureOptionCombos();

        JPanel north = new JPanel(new BorderLayout());
        north.add(controls, BorderLayout.CENTER);

        minConfidence.addChangeListener(e -> onThresholdChanged());
        scanButton.addActionListener(e -> scan());
        applyButton.addActionListener(e -> apply());
        filterButton.addActionListener(e -> applyEditorFilter());
        filterButton.setToolTipText(OStrings.getString("NUMBERCONVERT_FILTER_TOOLTIP"));
        filterButton.setEnabled(false);
        selectAllButton.addActionListener(e -> selectAll());
        invertButton.addActionListener(e -> runPreservingSelection(model::invertAccept));

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
        closeButton.addActionListener(e -> frame.dispose());
        actionButtons.add(filterButton);
        actionButtons.add(applyButton);
        actionButtons.add(closeButton);
        south.add(actionButtons, BorderLayout.EAST);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(north, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        frame.getContentPane().add(south, BorderLayout.SOUTH);
        frame.getRootPane().setDefaultButton(scanButton);
        updateApplyButton();
    }

    private static JComponent infoLine(JLabel label) {
        // Right-aligned, hugging the right window edge with a safety margin.
        JPanel line = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        line.add(label);
        return line;
    }

    private static JComponent formatRow(String label, JComboBox<?> combo) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.add(new JLabel(label));
        row.add(combo);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height + 4));
        return row;
    }

    /** Paints the lines connecting each data type to the format rows it obeys. */
    private final class ConnectorPanel extends JPanel {
        ConnectorPanel() {
            setOpaque(false);
            // Fixed width so the layout never wanders on resize.
            setMinimumSize(new Dimension(120, 10));
            setPreferredSize(new Dimension(120, 10));
            setMaximumSize(new Dimension(120, Integer.MAX_VALUE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(UIManager.getColor("Label.disabledForeground"));
            drawLinks(g, groupingRow, DataType.INTEGER, DataType.DECIMAL, DataType.CURRENCY);
            drawLinks(g, fractionRow, DataType.DECIMAL, DataType.PERCENT, DataType.CURRENCY);
            drawLinks(g, styleRow, DataType.DATE, DataType.TIME, DataType.ORDINAL);
        }

        private void drawLinks(Graphics g, @Nullable JComponent formatRow, DataType... types) {
            if (formatRow == null || !formatRow.isShowing() || !isShowing()) {
                return;
            }
            Point end = SwingUtilities.convertPoint(formatRow, 0, formatRow.getHeight() / 2, this);
            for (DataType t : types) {
                JComponent row = (JComponent) typeRows.get(t);
                if (row == null || !row.isShowing()) {
                    continue;
                }
                Point start = SwingUtilities.convertPoint(row, row.getWidth(), row.getHeight() / 2, this);
                g.drawLine(Math.max(0, start.x), start.y, getWidth(), end.y);
            }
        }
    }

    private void setupTable() {
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        model.addTableModelListener(e -> updateApplyButton());

        TableColumnModel cols = table.getColumnModel();
        cols.getColumn(COL_ACCEPT).setPreferredWidth(76);
        cols.getColumn(COL_ACCEPT).setMaxWidth(100);
        cols.getColumn(COL_SEGMENT).setPreferredWidth(64);
        cols.getColumn(COL_SEGMENT).setMaxWidth(90);
        cols.getColumn(COL_TYPE).setPreferredWidth(84);
        cols.getColumn(COL_TYPE).setMaxWidth(140);
        cols.getColumn(COL_STATUS).setPreferredWidth(90);
        cols.getColumn(COL_STATUS).setMaxWidth(130);
        cols.getColumn(COL_CONFIDENCE).setPreferredWidth(80);
        cols.getColumn(COL_CONFIDENCE).setMaxWidth(110);
        int gearWidth = GEAR_ICON.getIconWidth() + 6;
        cols.getColumn(COL_GEAR).setMinWidth(gearWidth);
        cols.getColumn(COL_GEAR).setMaxWidth(gearWidth);
        cols.getColumn(COL_ORIGINAL).setPreferredWidth(200);
        cols.getColumn(COL_PREVIEW).setPreferredWidth(200);

        // Right-align (trailing) so numerals read naturally, with the cell
        // orientation following the value's own script direction.
        DirectionalRenderer numeral = new DirectionalRenderer();
        cols.getColumn(COL_ORIGINAL).setCellRenderer(numeral);
        cols.getColumn(COL_PREVIEW).setCellRenderer(numeral);
        cols.getColumn(COL_CONFIDENCE).setCellRenderer(trailingRenderer());
        cols.getColumn(COL_TYPE).setCellRenderer(centerRenderer());
        cols.getColumn(COL_STATUS).setCellRenderer(centerRenderer());
        cols.getColumn(COL_SEGMENT).setCellRenderer(new SegmentLinkRenderer());

        // Gear rollover: the icon appears when the mouse is over the row, just
        // like the Issues window's action column.
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateGearRollover();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            // Keep the confidence tooltip up much longer than the Swing
            // default while the mouse is over the table.
            private int originalDismissDelay = -1;

            @Override
            public void mouseEntered(MouseEvent e) {
                originalDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
                ToolTipManager.sharedInstance().setDismissDelay(60_000);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (originalDismissDelay >= 0) {
                    ToolTipManager.sharedInstance().setDismissDelay(originalDismissDelay);
                }
                updateGearRollover();
            }
        });

        // Space or Enter inverts the accept mark of every selected row.
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("SPACE"), "zkfInvertSelection");
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "zkfInvertSelection");
        table.getActionMap().put("zkfInvertSelection", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invertSelectedRows();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 1) {
                    return;
                }
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) {
                    return;
                }
                int modelCol = table.convertColumnIndexToModel(col);
                if (modelCol == COL_SEGMENT) {
                    gotoSegment(row);
                } else if (modelCol == COL_GEAR) {
                    table.setRowSelectionInterval(row, row);
                    rowPopup.show(table, e.getX(), e.getY());
                }
            }
        });

        // Header-click sorting; the active column shows the look-and-feel's
        // subtle sort arrow (and the value-sort mode tag) before the title.
        JTableHeader header = table.getTableHeader();
        TableCellRenderer baseHeader = header.getDefaultRenderer();
        header.setDefaultRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            Component comp = baseHeader.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row,
                    column);
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                int modelCol = tbl.convertColumnIndexToModel(column);
                label.setText(model.headerText(modelCol));
                label.setIcon(model.headerIcon(modelCol));
                label.setHorizontalTextPosition(SwingConstants.TRAILING);
            }
            return comp;
        });
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewCol >= 0) {
                    int modelCol = table.convertColumnIndexToModel(viewCol);
                    if (modelCol != COL_GEAR) {
                        sortPreservingSelection(modelCol);
                        header.repaint();
                    }
                }
            }
        });

        installContextMenu();
        restoreTableState();
    }

    private static DefaultTableCellRenderer trailingRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.TRAILING);
        return r;
    }

    private static DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    private void installContextMenu() {
        JMenuItem gotoItem = new JMenuItem(OStrings.getString("NUMBERCONVERT_GOTO"));
        gotoItem.addActionListener(e -> gotoSegment(table.getSelectedRow()));
        JMenuItem copyOriginal = new JMenuItem(OStrings.getString("NUMBERCONVERT_COPY_ORIGINAL"));
        copyOriginal.addActionListener(e -> copySelected(COL_ORIGINAL));
        JMenuItem copyPreview = new JMenuItem(OStrings.getString("NUMBERCONVERT_COPY_PREVIEW"));
        copyPreview.addActionListener(e -> copySelected(COL_PREVIEW));
        rowPopup.add(gotoItem);
        rowPopup.addSeparator();
        rowPopup.add(copyOriginal);
        rowPopup.add(copyPreview);
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
                    if (!table.isRowSelected(row)) {
                        table.setRowSelectionInterval(row, row);
                    }
                    rowPopup.show(table, e.getX(), e.getY());
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

    private void invertSelectedRows() {
        int[] selected = table.getSelectedRows();
        if (selected.length == 0) {
            return;
        }
        runPreservingSelection(() -> {
            for (int r : selected) {
                model.rows.get(r).accept = !model.rows.get(r).accept;
            }
            model.refresh();
        });
    }

    /** Mirrors the Issues window's rollover handling for the gear column. */
    private void updateGearRollover() {
        int oldRow = model.mouseoverRow;
        Point point = table.getMousePosition();
        int newRow = point == null ? -1 : table.rowAtPoint(point);
        int newCol = point == null ? -1 : table.columnAtPoint(point);
        boolean repaint = newRow != oldRow || newCol != model.mouseoverCol;
        model.mouseoverRow = newRow;
        model.mouseoverCol = newCol < 0 ? -1 : table.convertColumnIndexToModel(newCol);
        if (repaint) {
            int gearView = table.convertColumnIndexToView(COL_GEAR);
            Rectangle rect = table.getCellRect(oldRow, gearView, true);
            table.repaint(rect);
            rect = table.getCellRect(newRow, gearView, true);
            table.repaint(rect);
        }
    }

    private void saveTableState() {
        StringBuilder widths = new StringBuilder();
        TableColumnModel cols = table.getColumnModel();
        for (int i = 0; i < cols.getColumnCount(); i++) {
            if (i > 0) {
                widths.append(',');
            }
            widths.append(cols.getColumn(i).getWidth());
        }
        Preferences.setPreference(PREF_TABLE_WIDTHS, widths.toString());
        Preferences.setPreference(PREF_SORT_COL, String.valueOf(model.sortColumn));
        Preferences.setPreference(PREF_SORT_IDX, String.valueOf(model.clickIndex[model.sortColumn]));
    }

    private void restoreTableState() {
        String widths = Preferences.getPreferenceDefault(PREF_TABLE_WIDTHS, "");
        if (!widths.isEmpty()) {
            String[] parts = widths.split(",");
            TableColumnModel cols = table.getColumnModel();
            for (int i = 0; i < parts.length && i < cols.getColumnCount(); i++) {
                try {
                    cols.getColumn(i).setPreferredWidth(Integer.parseInt(parts[i].trim()));
                } catch (NumberFormatException ignore) {
                    // keep the default width
                }
            }
        }
        try {
            int col = Integer.parseInt(Preferences.getPreferenceDefault(PREF_SORT_COL,
                    String.valueOf(COL_SEGMENT)));
            int idx = Integer.parseInt(Preferences.getPreferenceDefault(PREF_SORT_IDX, "0"));
            if (col >= 0 && col < model.getColumnCount() && col != COL_GEAR && idx >= 0) {
                model.sortColumn = col;
                model.clickIndex[col] = idx % model.statesFor(col);
            }
        } catch (NumberFormatException ignore) {
            // keep the default sort
        }
    }

    private void rescanIfResults() {
        if (model.hasScanData() && scanButton.isEnabled()) {
            scan();
        }
    }

    /**
     * A checkbox changed. Re-scan only when it demands data the last scan did
     * not collect; otherwise just show/hide the affected rows locally.
     */
    private void onScanOptionChanged() {
        if (!model.hasScanData() || !scanButton.isEnabled()) {
            return;
        }
        if (needsRescan()) {
            scan();
        } else {
            runPreservingSelection(model::refilter);
        }
    }

    private boolean needsRescan() {
        if (includeTranslatedCheck.isSelected() && !lastIncludeTranslated) {
            return true;
        }
        for (DataType t : enabledTypes()) {
            if (!lastTypes.contains(t)) {
                return true;
            }
        }
        return romanCheck.isSelected() && typeChecks.get(DataType.INTEGER).isSelected() && !lastAllowRoman;
    }

    /** Visibility of a scanned row under the current checkbox states. */
    private boolean rowVisible(Row row) {
        if (row.alreadyTranslated && !includeTranslatedCheck.isSelected()) {
            return false;
        }
        DataType type = row.proposal.getType();
        if (!typeChecks.get(type).isSelected()) {
            return false;
        }
        if (type == DataType.INTEGER && !romanCheck.isSelected()
                && "ROMAN".equals(row.proposal.getConfidenceFactors().get(0).getId())) {
            return false;
        }
        return true;
    }

    private void sortPreservingSelection(int modelColumn) {
        runPreservingSelection(() -> model.toggleSort(modelColumn));
    }

    /**
     * The table selection survives every action that reshuffles, filters or
     * replaces the rows: re-selection is keyed on the segment numbers.
     */
    private void runPreservingSelection(Runnable action) {
        Set<Integer> selected = captureSelectedSegments();
        action.run();
        restoreSelectedSegments(selected);
    }

    private Set<Integer> captureSelectedSegments() {
        Set<Integer> selected = new HashSet<>();
        for (int r : table.getSelectedRows()) {
            if (r < model.rows.size()) {
                selected.add(model.rows.get(r).proposal.getSegmentNumber());
            }
        }
        return selected;
    }

    private void restoreSelectedSegments(Set<Integer> selected) {
        table.clearSelection();
        if (selected.isEmpty()) {
            return;
        }
        for (int i = 0; i < model.rows.size(); i++) {
            if (selected.contains(model.rows.get(i).proposal.getSegmentNumber())) {
                table.addRowSelectionInterval(i, i);
            }
        }
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
            int choice = JOptionPane.showConfirmDialog(frame,
                    OStrings.getString("NUMBERCONVERT_CONFIRM_SELECT_ALL"),
                    OStrings.getString("NUMBERCONVERT_TITLE"), JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        runPreservingSelection(() -> model.setAllAccept(true));
    }

    private void setControlsEnabled(boolean enabled) {
        setEnabledRecursive(controls, enabled);
        selectAllButton.setEnabled(enabled);
        invertButton.setEnabled(enabled);
        if (enabled) {
            // Re-establish the Integer/Roman coupling after a blanket enable.
            romanCheck.setEnabled(typeChecks.get(DataType.INTEGER).isSelected());
        }
    }

    private static void setEnabledRecursive(Container container, boolean enabled) {
        for (Component c : container.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container) {
                setEnabledRecursive((Container) c, enabled);
            }
        }
    }

    private void updateInfoLabels() {
        IProject project = Core.getProject();
        if (project == null || !project.isProjectLoaded()) {
            infoSource.setText(OStrings.getString("NUMBERCONVERT_STATUS_NOPROJECT"));
            infoTarget.setText(" ");
            return;
        }
        ProjectProperties props = project.getProjectProperties();
        Language src = props.getSourceLanguage();
        Language tgt = props.getTargetLanguage();
        String srcTok = project.getSourceTokenizer() == null ? "-"
                : project.getSourceTokenizer().getClass().getSimpleName();
        String tgtTok = project.getTargetTokenizer() == null ? "-"
                : project.getTargetTokenizer().getClass().getSimpleName();
        infoSource.setText(MessageFormat.format(OStrings.getString("NUMBERCONVERT_INFO_SOURCE"),
                src.getLanguage() + " (" + src.getDisplayName() + ")", srcTok,
                Collator.getInstance(src.getLocale()).getClass().getSimpleName() + "(" + src.getLocale() + ")"));
        infoTarget.setText(MessageFormat.format(OStrings.getString("NUMBERCONVERT_INFO_TARGET"),
                tgt.getLanguage() + " (" + tgt.getDisplayName() + ")", tgtTok,
                Collator.getInstance(tgt.getLocale()).getClass().getSimpleName() + "(" + tgt.getLocale() + ")"));
    }

    private void scan() {
        IProject project = Core.getProject();
        if (project == null || !project.isProjectLoaded()) {
            status.setText(OStrings.getString("NUMBERCONVERT_STATUS_NOPROJECT"));
            return;
        }
        updateInfoLabels();
        Set<DataType> types = enabledTypes();
        boolean allowRoman = romanCheck.isSelected() && typeChecks.get(DataType.INTEGER).isSelected();
        boolean includeTranslated = includeTranslatedCheck.isSelected();
        List<SourceTextEntry> entries = scopeEntries(project);
        ProjectProperties props = project.getProjectProperties();
        Locale src = props.getSourceLanguage().getLocale();
        Locale tgt = props.getTargetLanguage().getLocale();
        model.setLocales(src, tgt);
        lastSrc = src;
        lastTgt = tgt;
        lastTypes = types;
        lastAllowRoman = allowRoman;
        lastIncludeTranslated = includeTranslated;
        RenderOptions options = currentOptions();

        Set<Integer> selectedBefore = captureSelectedSegments();
        setControlsEnabled(false);
        applyButton.setEnabled(false);
        filterButton.setEnabled(false);
        status.setText(OStrings.getString("NUMBERCONVERT_STATUS_SCANNING"));
        progress.setMaximum(Math.max(1, entries.size()));
        progress.setValue(0);
        progress.setString(MessageFormat.format(OStrings.getString("NUMBERCONVERT_SCAN_PROGRESS"), 0,
                entries.size()));
        progress.setVisible(true);

        new SwingWorker<List<Row>, Integer>() {
            @Override
            protected List<Row> doInBackground() {
                List<Row> rows = new ArrayList<>();
                int scanned = 0;
                for (SourceTextEntry ste : entries) {
                    scanned++;
                    if ((scanned & 0x3F) == 0 || scanned == entries.size()) {
                        publish(scanned);
                    }
                    TMXEntry info = project.getTranslationInfo(ste);
                    boolean translated = info != null && info.isTranslated();
                    if (translated && !includeTranslated) {
                        continue;
                    }
                    Optional<Proposal> p = NumberAutoConvertScanner.propose(ste.entryNum(), ste.getSrcText(),
                            src, tgt, types, allowRoman, options);
                    p.ifPresent(prop -> rows.add(new Row(ste, prop, translated)));
                }
                return rows;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int scanned = chunks.get(chunks.size() - 1);
                progress.setValue(scanned);
                progress.setString(MessageFormat.format(OStrings.getString("NUMBERCONVERT_SCAN_PROGRESS"),
                        scanned, entries.size()));
            }

            @Override
            protected void done() {
                try {
                    model.setRows(get(), threshold());
                    restoreSelectedSegments(selectedBefore);
                    status.setText(MessageFormat.format(OStrings.getString("NUMBERCONVERT_STATUS_FOUND"),
                            model.getRowCount()));
                    filterButton.setEnabled(model.getRowCount() > 0);
                } catch (Exception ex) {
                    status.setText(ex.getLocalizedMessage());
                } finally {
                    progress.setVisible(false);
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
        long overwrites = model.rows.stream().filter(r -> r.accept && r.alreadyTranslated).count();
        if (overwrites > 0) {
            int choice = JOptionPane.showConfirmDialog(frame,
                    MessageFormat.format(OStrings.getString("NUMBERCONVERT_CONFIRM_OVERWRITE"), overwrites),
                    OStrings.getString("NUMBERCONVERT_TITLE"), JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
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
        runPreservingSelection(model::removeAccepted);
        // Set after the model change so the count update does not overwrite it.
        status.setText(MessageFormat.format(OStrings.getString("NUMBERCONVERT_STATUS_APPLIED"), applied));
        filterButton.setEnabled(model.getRowCount() > 0);
    }

    /** Replace the editor's filter with a filter on the last scan's hits. */
    private void applyEditorFilter() {
        if (model.rows.isEmpty()) {
            return;
        }
        if (Core.getEditor().getFilter() != null) {
            int choice = JOptionPane.showConfirmDialog(frame,
                    OStrings.getString("NUMBERCONVERT_CONFIRM_FILTER"),
                    OStrings.getString("NUMBERCONVERT_TITLE"), JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        Set<Integer> nums = new HashSet<>();
        for (Row row : model.rows) {
            nums.add(row.proposal.getSegmentNumber());
        }
        Core.getEditor().setFilter(new SegmentNumberFilter(nums));
    }

    /** Editor filter that shows exactly the given segment numbers. */
    private static final class SegmentNumberFilter implements IEditorFilter {
        private final Set<Integer> segmentNumbers;
        private final JPanel control;

        SegmentNumberFilter(Set<Integer> segmentNumbers) {
            this.segmentNumbers = segmentNumbers;
            control = new JPanel(new FlowLayout(FlowLayout.LEFT));
            control.add(new JLabel(MessageFormat.format(OStrings.getString("NUMBERCONVERT_FILTER_LABEL"),
                    segmentNumbers.size())));
            JButton remove = new JButton();
            org.openide.awt.Mnemonics.setLocalizedText(remove, OStrings.getString("BUTTON_CLOSE"));
            remove.addActionListener(e -> Core.getEditor().removeFilter());
            control.add(remove);
        }

        @Override
        public boolean allowed(@Nullable SourceTextEntry ste) {
            return ste != null && segmentNumbers.contains(ste.entryNum());
        }

        @Override
        public Component getControlComponent() {
            return control;
        }

        @Override
        public boolean isSourceAsEmptyTranslation() {
            return false;
        }
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
        // Both counters stay current on every change, including checkbox
        // toggles and local filtering.
        if (model.hasScanData()) {
            status.setText(MessageFormat.format(OStrings.getString("NUMBERCONVERT_STATUS_FOUND"),
                    model.getRowCount()));
        }
    }

    private RenderOptions currentOptions() {
        return new RenderOptions((RenderOptions.Grouping) groupingCombo.getSelectedItem(),
                (RenderOptions.Fraction) fractionCombo.getSelectedItem(),
                (RenderOptions.Style) styleCombo.getSelectedItem());
    }

    private void configureOptionCombos() {
        groupingCombo.setRenderer(optionRenderer());
        fractionCombo.setRenderer(optionRenderer());
        styleCombo.setRenderer(optionRenderer());
        groupingCombo.setSelectedItem(RenderOptions.Grouping
                .valueOf(Preferences.getPreferenceDefault(PREF_GROUPING, RenderOptions.Grouping.ORIGINAL.name())));
        fractionCombo.setSelectedItem(RenderOptions.Fraction
                .valueOf(Preferences.getPreferenceDefault(PREF_FRACTION, RenderOptions.Fraction.ORIGINAL.name())));
        styleCombo.setSelectedItem(RenderOptions.Style
                .valueOf(Preferences.getPreferenceDefault(PREF_STYLE, RenderOptions.Style.ORIGINAL.name())));
        groupingCombo.addActionListener(e -> onRenderOptionsChanged());
        fractionCombo.addActionListener(e -> onRenderOptionsChanged());
        styleCombo.addActionListener(e -> onRenderOptionsChanged());
    }

    private void onRenderOptionsChanged() {
        Preferences.setPreference(PREF_GROUPING,
                ((RenderOptions.Grouping) groupingCombo.getSelectedItem()).name());
        Preferences.setPreference(PREF_FRACTION,
                ((RenderOptions.Fraction) fractionCombo.getSelectedItem()).name());
        Preferences.setPreference(PREF_STYLE, ((RenderOptions.Style) styleCombo.getSelectedItem()).name());
        runPreservingSelection(this::reRender);
    }

    private void reRender() {
        if (lastSrc == null || !model.hasScanData()) {
            return;
        }
        RenderOptions options = currentOptions();
        // Re-render the whole master list so hidden rows stay fresh too.
        for (Row row : model.allRows) {
            NumberAutoConvertScanner.propose(row.proposal.getSegmentNumber(), row.proposal.getSource(),
                    lastSrc, lastTgt, lastTypes, lastAllowRoman, options).ifPresent(p -> row.proposal = p);
        }
        model.refresh();
    }

    private void onThresholdChanged() {
        if (revertingThreshold) {
            return;
        }
        int newThreshold = threshold();
        // Only bother the user when the change would really alter the selection.
        if (model.wouldThresholdChangeSelection(newThreshold)) {
            if (model.acceptedCount() > 0) {
                int choice = JOptionPane.showConfirmDialog(frame,
                        OStrings.getString("NUMBERCONVERT_CONFIRM_SELECT_ALL"),
                        OStrings.getString("NUMBERCONVERT_TITLE"), JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) {
                    revertingThreshold = true;
                    minConfidence.setValue(previousThreshold);
                    revertingThreshold = false;
                    return;
                }
            }
            runPreservingSelection(() -> model.applyThreshold(newThreshold, true));
        }
        previousThreshold = newThreshold;
    }

    private static DefaultListCellRenderer optionRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(optionLabel(value));
                return this;
            }
        };
    }

    private static String optionLabel(Object value) {
        if (value instanceof RenderOptions.Grouping) {
            switch ((RenderOptions.Grouping) value) {
            case NEVER:
                return OStrings.getString("NUMBERCONVERT_NEVER");
            case ALWAYS:
                return OStrings.getString("NUMBERCONVERT_ALWAYS");
            default:
                return OStrings.getString("NUMBERCONVERT_LIKE_ORIGINAL");
            }
        }
        if (value instanceof RenderOptions.Fraction) {
            switch ((RenderOptions.Fraction) value) {
            case ZERO:
                return "0";
            case ONE:
                return "1";
            case TWO:
                return "2";
            default:
                return OStrings.getString("NUMBERCONVERT_LIKE_ORIGINAL");
            }
        }
        if (value instanceof RenderOptions.Style) {
            switch ((RenderOptions.Style) value) {
            case SHORT:
                return OStrings.getString("NUMBERCONVERT_DATE_SHORT");
            case MEDIUM:
                return OStrings.getString("NUMBERCONVERT_DATE_MEDIUM");
            case LONG:
                return OStrings.getString("NUMBERCONVERT_DATE_LONG");
            case SPELLOUT:
                return OStrings.getString("NUMBERCONVERT_DATE_SPELLOUT");
            default:
                return OStrings.getString("NUMBERCONVERT_LIKE_ORIGINAL");
            }
        }
        return String.valueOf(value);
    }

    private static String typeName(DataType t) {
        return OStrings.getString("NUMBERCONVERT_TYPE_" + t.name());
    }

    /** Percent format of the running GUI locale, for cell and tooltip. */
    private static java.text.NumberFormat guiPercent() {
        java.text.NumberFormat f = java.text.NumberFormat.getPercentInstance();
        f.setMaximumFractionDigits(0);
        return f;
    }

    private static String rowTooltip(Row r) {
        StringBuilder sb = new StringBuilder("<html>");
        appendConfidenceHtml(sb, r.proposal);
        sb.append("<hr>");
        sb.append(segmentInfoHtml(r.entry, Core.getProject().getTranslationInfo(r.entry)));
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * The segment-context half of the row tooltip: everything else the
     * project knows about the entry, one line per piece of information,
     * lines omitted when the information is absent.
     */
    static String segmentInfoHtml(SourceTextEntry entry, @Nullable TMXEntry info) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>")
                .append(MessageFormat.format(OStrings.getString("NUMBERCONVERT_TIP_SEGMENT"),
                        String.valueOf(entry.entryNum())))
                .append("</b> — ").append(escapeHtml(entry.getKey().file)).append("<br>");
        if (!StringUtil.isEmpty(entry.getKey().id)) {
            appendTipLine(sb, "NUMBERCONVERT_TIP_ID", escapeHtml(entry.getKey().id));
        }
        if (!StringUtil.isEmpty(entry.getComment())) {
            appendTipLine(sb, "NUMBERCONVERT_TIP_COMMENT", escapeHtml(clip(entry.getComment())));
        }
        if (entry.getDuplicate() != SourceTextEntry.DUPLICATE.NONE) {
            appendTipLine(sb, "NUMBERCONVERT_TIP_REPEATED",
                    String.valueOf(entry.getNumberOfDuplicates()));
        }
        if (info != null && info.isTranslated()) {
            appendTipLine(sb, "NUMBERCONVERT_TIP_TRANSLATION",
                    escapeHtml(clip(info.getTranslationText())));
            if (!StringUtil.isEmpty(info.getChanger()) && info.getChangeDate() != 0) {
                appendTipLine(sb, "NUMBERCONVERT_TIP_CHANGED", escapeHtml(info.getChanger()),
                        formatTipDate(info.getChangeDate()));
            } else if (!StringUtil.isEmpty(info.getCreator()) && info.getCreationDate() != 0) {
                appendTipLine(sb, "NUMBERCONVERT_TIP_CREATED", escapeHtml(info.getCreator()),
                        formatTipDate(info.getCreationDate()));
            }
            if (!StringUtil.isEmpty(info.getNote())) {
                appendTipLine(sb, "NUMBERCONVERT_TIP_NOTE", escapeHtml(clip(info.getNote())));
            }
            if (info.linked != null) {
                appendTipLine(sb, "NUMBERCONVERT_TIP_ORIGIN", info.linked.name());
            } else if (!StringUtil.isEmpty(info.origin)) {
                appendTipLine(sb, "NUMBERCONVERT_TIP_ORIGIN", escapeHtml(info.origin));
            }
        }
        return sb.toString();
    }

    private static void appendTipLine(StringBuilder sb, String key, Object... args) {
        sb.append(MessageFormat.format(OStrings.getString(key), args)).append("<br>");
    }

    private static String formatTipDate(long millis) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date(millis));
    }

    /** Minimal escaping for user text embedded in the HTML tooltip. */
    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Notes and comments can be arbitrarily long; keep the tooltip readable. */
    private static String clip(String s) {
        String flat = s.replace('\n', ' ').trim();
        return flat.length() > 160 ? flat.substring(0, 159) + "…" : flat;
    }

    private static void appendConfidenceHtml(StringBuilder sb, Proposal p) {
        java.text.NumberFormat pct = guiPercent();
        // Numbers first so the signed values line up and read at a glance; the
        // base value carries a plus sign like every other contribution.
        for (ConfidenceFactor f : p.getConfidenceFactors()) {
            String value = (f.getDelta() >= 0 ? "+" : "") + pct.format(f.getDelta());
            sb.append(value).append(' ').append(factorLabel(f.getId())).append("<br>");
        }
        sb.append("<b>= ").append(pct.format(p.getConfidence())).append("</b>");
    }

    private static String factorLabel(String id) {
        try {
            DataType.valueOf(id); // a base data-type factor reuses the type label
            return OStrings.getString("NUMBERCONVERT_TYPE_" + id);
        } catch (IllegalArgumentException notAType) {
            return OStrings.getString("NUMBERCONVERT_CONF_" + id);
        }
    }

    /** Confidence, formatted for the GUI locale. */
    private static String confidenceText(Proposal p) {
        return guiPercent().format(p.getConfidence());
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
        private Proposal proposal;
        private final boolean alreadyTranslated;
        private boolean accept;

        Row(SourceTextEntry entry, Proposal proposal, boolean alreadyTranslated) {
            this.entry = entry;
            this.proposal = proposal;
            this.alreadyTranslated = alreadyTranslated;
        }
    }

    /** Sort mode for the Original and Preview columns, cycled on header click. */
    private enum ValueSort {
        ALPHA, NUMERIC, RHYME, LENGTH
    }

    private final class ResultsModel extends AbstractTableModel {
        /** Everything the last scan collected, regardless of checkbox state. */
        private final List<Row> allRows = new ArrayList<>();
        /** The currently visible subset of {@link #allRows}. */
        private final List<Row> rows = new ArrayList<>();
        private final String[] columns = {
                OStrings.getString("NUMBERCONVERT_COL_ACCEPT"),
                OStrings.getString("ISSUES_TABLE_COLUMN_ENTRY_NUM"),
                OStrings.getString("NUMBERCONVERT_COL_STATUS"),
                OStrings.getString("ISSUES_TABLE_COLUMN_TYPE"), OStrings.getString("NUMBERCONVERT_COL_ORIGINAL"),
                OStrings.getString("NUMBERCONVERT_COL_PREVIEW"),
                OStrings.getString("NUMBERCONVERT_COL_CONFIDENCE"), "" };
        private int mouseoverRow = -1;
        private int mouseoverCol = -1;
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
            allRows.clear();
            allRows.addAll(newRows);
            rows.clear();
            for (Row r : allRows) {
                if (rowVisible(r)) {
                    rows.add(r);
                }
            }
            applyThreshold(thresholdPercent, false);
            sort();
            fireTableDataChanged();
        }

        boolean hasScanData() {
            return !allRows.isEmpty();
        }

        /** Re-derive the visible rows from the master list, keeping accept marks. */
        void refilter() {
            rows.clear();
            for (Row r : allRows) {
                if (rowVisible(r)) {
                    rows.add(r);
                }
            }
            sort();
            fireTableDataChanged();
        }

        void applyThreshold(int thresholdPercent, boolean fire) {
            for (Row r : rows) {
                r.accept = r.proposal.getConfidence() * 100.0 >= thresholdPercent;
            }
            if (fire) {
                // Accept marks feed the Apply sort column: keep the order true.
                sort();
                fireTableDataChanged();
            }
        }

        boolean wouldThresholdChangeSelection(int thresholdPercent) {
            for (Row r : rows) {
                if (r.accept != (r.proposal.getConfidence() * 100.0 >= thresholdPercent)) {
                    return true;
                }
            }
            return false;
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

        void refresh() {
            // Values (and with them confidences) may have changed: re-sort so
            // the active sort order stays reliably applied.
            sort();
            fireTableDataChanged();
        }

        void removeAccepted() {
            allRows.removeIf(r -> r.accept && rows.contains(r));
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
            case COL_STATUS:
                cmp = Comparator.comparing((Row r) -> r.alreadyTranslated);
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
                    Optional<Double> va = numericSortValue(a, text.apply(a));
                    Optional<Double> vb = numericSortValue(b, text.apply(b));
                    if (va.isPresent() && vb.isPresent()) {
                        return Double.compare(va.get(), vb.get());
                    }
                    return va.isPresent() ? -1 : vb.isPresent() ? 1 : 0;
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

        /**
         * Numeric value for sorting: the value the conversion actually parsed
         * (so "99'999" sorts as 99999, consistent with the value heuristic),
         * with the cross-script first-number parser as fallback for types
         * without a numeric value.
         */
        private Optional<Double> numericSortValue(Row row, String text) {
            Optional<Double> parsed = row.proposal.getNumericValue();
            if (parsed.isPresent()) {
                return parsed;
            }
            return NumeralValueParser.firstValue(text)
                    .map(r -> r.numerator().doubleValue() / r.denominator().doubleValue());
        }

        private String reverse(String s) {
            return new StringBuilder(s).reverse().toString();
        }

        String headerText(int column) {
            if (column != sortColumn || !isValueColumn(column)) {
                return columns[column];
            }
            int idx = clickIndex[column];
            ValueSort mode = ValueSort.values()[(idx / 2) % ValueSort.values().length];
            return modeTag(mode) + " " + columns[column];
        }

        @Nullable
        Icon headerIcon(int column) {
            if (column != sortColumn) {
                return null;
            }
            boolean asc = clickIndex[column] % 2 == 0;
            return UIManager.getIcon(asc ? "Table.ascendingSortIcon" : "Table.descendingSortIcon");
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
                    : columnIndex == COL_SEGMENT ? Integer.class
                            : columnIndex == COL_GEAR ? Icon.class : String.class;
        }

        /** Same visibility rules as the Issues window's action column. */
        private Icon gearIcon(int row) {
            if (table.getSelectedRow() == row) {
                return GEAR_ICON_PRESSED;
            } else if (row == mouseoverRow && mouseoverCol == COL_GEAR) {
                return GEAR_ICON;
            } else if (row == mouseoverRow) {
                return GEAR_ICON_INACTIVE;
            } else {
                return GEAR_ICON_INVISIBLE;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == COL_ACCEPT;
        }

        @Override
        public @Nullable Object getValueAt(int rowIndex, int columnIndex) {
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
            case COL_STATUS:
                return OStrings.getString(
                        r.alreadyTranslated ? "NUMBERCONVERT_STATUS_TRANSLATED"
                                : "NUMBERCONVERT_STATUS_UNTRANSLATED");
            case COL_PREVIEW:
                return r.proposal.getTarget();
            case COL_CONFIDENCE:
                return confidenceText(r.proposal);
            case COL_GEAR:
                return gearIcon(rowIndex);
            default:
                return null;
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex == COL_ACCEPT) {
                rows.get(rowIndex).accept = (Boolean) value;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
