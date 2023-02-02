/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.align;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTable.DropLocation;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.io.FilenameUtils;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.align.Aligner.AlgorithmClass;
import org.omegat.gui.align.Aligner.CalculatorType;
import org.omegat.gui.align.Aligner.ComparisonMode;
import org.omegat.gui.align.Aligner.CounterType;
import org.omegat.gui.align.MutableBead.Status;
import org.omegat.gui.filters2.FiltersCustomizer;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.segmentation.SegmentationCustomizer;
import org.omegat.util.Java8Compat;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.DelegatingComboBoxRenderer;
import org.omegat.util.gui.RoundedCornerBorder;
import org.omegat.util.gui.Styles;

import gen.core.filters.Filters;

/**
 * Controller for the alignment UI
 *
 * @author Aaron Madlon-Kay
 */
public class AlignPanelController {

    private final Aligner aligner;
    private final String defaultSaveDir;
    private boolean modified = false;
    private SRX customizedSRX;
    private Filters customizedFilters;

    private SwingWorker<?, ?> loader;

    private boolean doHighlight = true;
    private Pattern highlightPattern = Pattern.compile(Preferences.getPreferenceDefault(
            Preferences.ALIGNER_HIGHLIGHT_PATTERN, Preferences.ALIGNER_HIGHLIGHT_PATTERN_DEFAULT));

    private int ppRow = -1;
    private int ppCol = -1;

    private AlignPanel panel;
    private AlignMenuFrame frame;

    /**
     * The alignment workflow is separated into two phases:
     * <ol>
     * <li>Align: Verify and tweak the results of automatic algorithmic
     * alignment
     * <li>Edit: Manually edit the results
     * </ol>
     */
    private enum Phase {
        ALIGN, EDIT, PINPOINT
    }

    private Phase phase = Phase.ALIGN;

    public AlignPanelController(Aligner aligner, String defaultSaveDir) {
        this.aligner = aligner;
        this.defaultSaveDir = defaultSaveDir;
    }

    /**
     * Display the align tool. The tool is not modal, so this call will return
     * immediately.
     *
     * @param parent
     *            Parent window of the align tool
     */
    public void show(Component parent) {
        frame = new AlignMenuFrame();
        frame.setTitle(OStrings.getString("ALIGNER_PANEL"));
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeFrame(frame);
            }
        });

        panel = new AlignPanel();

        ActionListener comparisonListener = e -> {
            ComparisonMode newValue = (ComparisonMode) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.comparisonMode && confirmReset(frame)) {
                aligner.comparisonMode = newValue;
                reloadBeads();
            } else {
                panel.comparisonComboBox.setSelectedItem(aligner.comparisonMode);
            }
        };
        panel.comparisonComboBox.addActionListener(comparisonListener);
        panel.comparisonComboBox
                .setRenderer(new EnumRenderer<ComparisonMode>("ALIGNER_ENUM_COMPARISON_MODE_"));

        ActionListener algorithmListener = e -> {
            AlgorithmClass newValue = (AlgorithmClass) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.algorithmClass && confirmReset(frame)) {
                aligner.algorithmClass = newValue;
                reloadBeads();
            } else {
                panel.algorithmComboBox.setSelectedItem(aligner.algorithmClass);
            }
        };
        panel.algorithmComboBox.addActionListener(algorithmListener);
        panel.algorithmComboBox
                .setRenderer(new EnumRenderer<AlgorithmClass>("ALIGNER_ENUM_ALGORITHM_CLASS_"));

        ActionListener calculatorListener = e -> {
            CalculatorType newValue = (CalculatorType) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.calculatorType && confirmReset(frame)) {
                aligner.calculatorType = newValue;
                reloadBeads();
            } else {
                panel.calculatorComboBox.setSelectedItem(aligner.calculatorType);
            }
        };
        panel.calculatorComboBox.addActionListener(calculatorListener);
        panel.calculatorComboBox
                .setRenderer(new EnumRenderer<CalculatorType>("ALIGNER_ENUM_CALCULATOR_TYPE_"));

        ActionListener counterListener = e -> {
            CounterType newValue = (CounterType) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.counterType && confirmReset(frame)) {
                aligner.counterType = newValue;
                reloadBeads();
            } else {
                panel.counterComboBox.setSelectedItem(aligner.counterType);
            }
        };
        panel.counterComboBox.addActionListener(counterListener);
        panel.counterComboBox.setRenderer(new EnumRenderer<CounterType>("ALIGNER_ENUM_COUNTER_TYPE_"));

        ActionListener segmentingListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newValue = ((AbstractButton) e.getSource()).isSelected();
                if (newValue != aligner.segment && confirmReset(frame)) {
                    aligner.segment = newValue;
                    reloadBeads();
                } else {
                    panel.segmentingCheckBox.setSelected(aligner.segment);
                    frame.segmentingItem.setSelected(aligner.segment);
                }
            }
        };
        panel.segmentingCheckBox.addActionListener(segmentingListener);
        frame.segmentingItem.addActionListener(segmentingListener);

        ActionListener segmentingRulesListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (confirmReset(frame)) {
                    SegmentationCustomizer customizer = new SegmentationCustomizer(false, SRX.getDefault(),
                            Core.getSegmenter().getSRX(), null);
                    if (customizer.show(frame)) {
                        customizedSRX = customizer.getResult();
                        Core.setSegmenter(new Segmenter(customizedSRX));
                        reloadBeads();
                    }
                }
            }
        };
        panel.segmentingRulesButton.addActionListener(segmentingRulesListener);
        frame.segmentingRulesItem.addActionListener(segmentingRulesListener);

        ActionListener filterSettingsListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (confirmReset(frame)) {
                    FiltersCustomizer customizer = new FiltersCustomizer(false,
                            FilterMaster.createDefaultFiltersConfig(), Core.getFilterMaster().getConfig(),
                            null);
                    if (customizer.show(frame)) {
                        customizedFilters = customizer.getResult();
                        Core.setFilterMaster(new FilterMaster(customizedFilters));
                        aligner.clearLoaded();
                        reloadBeads();
                    }
                }
            }
        };
        panel.fileFilterSettingsButton.addActionListener(filterSettingsListener);
        frame.fileFilterSettingsItem.addActionListener(filterSettingsListener);

        TableCellRenderer renderer = new MultilineCellRenderer();
        panel.table.setDefaultRenderer(Object.class, renderer);
        panel.table.setDefaultRenderer(Boolean.class, renderer);
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeRows(panel.table);
            }
        });

        ActionListener oneAdjustListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = panel.table.getSelectedRows();
                int col = panel.table.getSelectedColumn();
                boolean up = e.getSource().equals(panel.moveUpButton)
                        || e.getSource().equals(frame.moveUpItem);
                BeadTableModel model = (BeadTableModel) panel.table.getModel();
                if ((e.getModifiers() & Java8Compat.getMenuShortcutKeyMaskEx()) != 0) {
                    int trgRow = up ? model.prevBeadFromRow(rows[0])
                            : model.nextBeadFromRow(rows[rows.length - 1]);
                    moveRows(rows, col, trgRow);
                } else {
                    int offset = up ? -1 : 1;
                    slideRows(rows, col, offset);
                }
            }
        };
        panel.moveUpButton.addActionListener(oneAdjustListener);
        frame.moveUpItem.addActionListener(oneAdjustListener);
        panel.moveDownButton.addActionListener(oneAdjustListener);
        frame.moveDownItem.addActionListener(oneAdjustListener);

        ActionListener mergeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = panel.table.getSelectedRows();
                int col = panel.table.getSelectedColumn();
                BeadTableModel model = (BeadTableModel) panel.table.getModel();
                if (rows.length == 1) {
                    rows = new int[] { rows[0], model.nextNonEmptyCell(rows[0], col) };
                }
                int beads = model.beadsInRowSpan(rows);
                if (beads >= 1) {
                    if (beads == 1) {
                        mergeRows(rows, col);
                    } else {
                        moveRows(rows, col, rows[0]);
                    }
                }
            }
        };
        panel.mergeButton.addActionListener(mergeListener);
        frame.mergeItem.addActionListener(mergeListener);

        ActionListener splitListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = panel.table.getSelectedRows();
                int col = panel.table.getSelectedColumn();
                BeadTableModel model = (BeadTableModel) panel.table.getModel();
                int beads = model.beadsInRowSpan(rows);
                if (beads == 1) {
                    if (rows.length == 1) {
                        splitRow(rows[0], col);
                    } else {
                        splitBead(rows, col);
                    }
                }
            }
        };
        panel.splitButton.addActionListener(splitListener);
        frame.splitItem.addActionListener(splitListener);

        ActionListener editListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                int row = panel.table.getSelectedRow();
                int col = panel.table.getSelectedColumn();
                editRow(row, col);
            }
        };
        panel.editButton.addActionListener(editListener);
        frame.editItem.addActionListener(editListener);

        ListSelectionListener selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateCommandAvailability(panel, frame);
            }
        };
        panel.table.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);
        panel.table.getSelectionModel().addListSelectionListener(selectionListener);

        ActionListener saveListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!confirmSaveTMX(panel)) {
                    return;
                }
                while (true) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setSelectedFile(new File(defaultSaveDir, getOutFileName()));
                    chooser.setDialogTitle(OStrings.getString("ALIGNER_PANEL_DIALOG_SAVE"));
                    if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(frame)) {
                        File file = chooser.getSelectedFile();
                        if (file.isFile()) {
                            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(frame,
                                    StringUtil.format(OStrings.getString("ALIGNER_PANEL_DIALOG_OVERWRITE"),
                                            file.getName()),
                                    OStrings.getString("ALIGNER_DIALOG_WARNING_TITLE"),
                                    JOptionPane.WARNING_MESSAGE)) {
                                continue;
                            }
                        }
                        List<MutableBead> beads = ((BeadTableModel) panel.table.getModel()).getData();
                        try {
                            aligner.writePairsToTMX(file,
                                    MutableBead.beadsToEntries(aligner.srcLang, aligner.trgLang, beads));
                            modified = false;
                        } catch (Exception ex) {
                            Log.log(ex);
                            JOptionPane.showMessageDialog(frame,
                                    OStrings.getString("ALIGNER_PANEL_SAVE_ERROR"),
                                    OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    break;
                }
            }
        };
        panel.saveButton.addActionListener(saveListener);
        frame.saveItem.addActionListener(saveListener);

        ActionListener resetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (confirmReset(frame)) {
                    if (phase == Phase.ALIGN) {
                        aligner.restoreDefaults();
                    }
                    reloadBeads();
                }
            }
        };
        panel.resetButton.addActionListener(resetListener);
        frame.resetItem.addActionListener(resetListener);

        ActionListener reloadListener = e -> {
            if (confirmReset(frame)) {
                aligner.clearLoaded();
                reloadBeads();
            }
        };
        frame.reloadItem.addActionListener(reloadListener);

        ActionListener removeTagsListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newValue = ((AbstractButton) e.getSource()).isSelected();
                if (newValue != aligner.removeTags && confirmReset(frame)) {
                    aligner.removeTags = newValue;
                    aligner.clearLoaded();
                    reloadBeads();
                } else {
                    panel.removeTagsCheckBox.setSelected(aligner.removeTags);
                    frame.removeTagsItem.setSelected(aligner.removeTags);
                }
            }
        };
        panel.removeTagsCheckBox.addActionListener(removeTagsListener);
        frame.removeTagsItem.addActionListener(removeTagsListener);

        panel.continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                phase = Phase.EDIT;
                updatePanel();
            }
        });

        ActionListener highlightListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doHighlight = ((AbstractButton) e.getSource()).isSelected();
                updateHighlight();
            }
        };
        panel.highlightCheckBox.addActionListener(highlightListener);
        frame.highlightItem.addActionListener(highlightListener);

        ActionListener highlightPatternListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PatternPanelController patternEditor = new PatternPanelController(highlightPattern);
                highlightPattern = patternEditor.show(frame);
                Preferences.setPreference(Preferences.ALIGNER_HIGHLIGHT_PATTERN, highlightPattern.pattern());
                updateHighlight();
            }
        };
        panel.highlightPatternButton.addActionListener(highlightPatternListener);
        frame.highlightPatternItem.addActionListener(highlightPatternListener);

        frame.markAcceptedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStatus(MutableBead.Status.ACCEPTED, panel.table.getSelectedRows());
            }
        });

        frame.markNeedsReviewItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStatus(MutableBead.Status.NEEDS_REVIEW, panel.table.getSelectedRows());
            }
        });

        frame.clearMarkItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStatus(MutableBead.Status.DEFAULT, panel.table.getSelectedRows());
            }
        });

        frame.toggleSelectedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleEnabled(panel.table.getSelectedRows());
            }
        });

        frame.closeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeFrame(frame);
            }
        });

        frame.keepAllItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleAllEnabled(true);
            }
        });

        frame.keepNoneItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleAllEnabled(false);
            }
        });

        frame.realignPendingItem.addActionListener(e -> {
            realignPending();
        });

        frame.pinpointAlignStartItem.addActionListener(e -> {
            phase = Phase.PINPOINT;
            ppRow = panel.table.getSelectedRow();
            ppCol = panel.table.getSelectedColumn();
            panel.table.clearSelection();
            updatePanel();
        });

        frame.pinpointAlignEndItem.addActionListener(e -> {
            pinpointAlign(panel.table.getSelectedRow(), panel.table.getSelectedColumn());
        });

        frame.pinpointAlignCancelItem.addActionListener(e -> {
            phase = Phase.EDIT;
            ppRow = -1;
            ppCol = -1;
            panel.table.repaint();
            updatePanel();
        });

        panel.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (phase == Phase.PINPOINT) {
                    JTable table = (JTable) e.getSource();
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    pinpointAlign(row, col);
                }
            }
        });

        frame.resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                Java8Compat.getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        frame.realignPendingItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, Java8Compat.getMenuShortcutKeyMaskEx()));
        frame.saveItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Java8Compat.getMenuShortcutKeyMaskEx()));
        frame.closeItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, Java8Compat.getMenuShortcutKeyMaskEx()));

        // emacs-like keys for table navigation
        // See javax.swing.plaf.BasicTableUI.Actions for supported action names.
        setKeyboardShortcut(panel.table, "selectNextRow", 'n');
        setKeyboardShortcut(panel.table, "selectNextRowExtendSelection", 'N');
        setKeyboardShortcut(panel.table, "selectPreviousRow", 'p');
        setKeyboardShortcut(panel.table, "selectPreviousRowExtendSelection", 'P');
        setKeyboardShortcut(panel.table, "selectNextColumn", 'f');
        setKeyboardShortcut(panel.table, "selectNextColumnExtendSelection", 'F');
        setKeyboardShortcut(panel.table, "selectPreviousColumn", 'b');
        setKeyboardShortcut(panel.table, "selectPreviousColumnExtendSelection", 'B');

        panel.table.setTransferHandler(new AlignTransferHandler());
        panel.table.addPropertyChangeListener("dropLocation", new DropLocationListener());
        panel.table.setFont(Core.getMainWindow().getApplicationFont());
        CoreEvents.registerFontChangedEventListener(panel.table::setFont);

        // Set initial state
        updateHighlight();
        updatePanel();
        reloadBeads();

        frame.add(panel);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
    }

    private static void setKeyboardShortcut(JComponent comp, Object actionName, char stroke) {
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(stroke), actionName);
    }

    private static void resizeRows(JTable table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            int max = 0;
            for (int col = BeadTableModel.COL_SRC; col < table.getColumnCount(); col++) {
                int colWidth = table.getColumnModel().getColumn(col).getWidth();
                TableCellRenderer cellRenderer = table.getCellRenderer(row, col);
                Component c = table.prepareRenderer(cellRenderer, row, col);
                c.setBounds(0, 0, colWidth, Integer.MAX_VALUE);
                int height = c.getPreferredSize().height;
                max = Math.max(max, height);
            }
            table.setRowHeight(row, max);
        }
    }

    private void slideRows(int[] rows, int col, int offset) {
        modified = true;
        Rectangle initialRect = panel.table.getVisibleRect();
        panel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        List<Integer> realRows = model.realCellsInRowSpan(col, rows);
        int[] resultRows = model.slide(realRows, col, offset);
        int selStart = resultRows[0];
        int selEnd = resultRows[1];
        // If we have a multi-cell selection, trim the selection so that the
        // result remains slideable
        if (selStart != selEnd) {
            while (offset < 0 && !model.canMove(selStart, col, true)) {
                selStart++;
            }
            while (offset > 0 && !model.canMove(selEnd, col, false)) {
                selEnd--;
            }
        }
        panel.table.changeSelection(selStart, col, false, false);
        panel.table.changeSelection(selEnd, col, false, true);
        ensureSelectionVisible(initialRect);
    }

    private void moveRows(int[] rows, int col, int trgRow) {
        modified = true;
        Rectangle initialRect = panel.table.getVisibleRect();
        panel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        List<Integer> realRows = model.realCellsInRowSpan(col, rows);
        int[] resultRows = model.move(realRows, col, trgRow);
        panel.table.changeSelection(resultRows[0], col, false, false);
        panel.table.changeSelection(resultRows[1], col, false, true);
        ensureSelectionVisible(initialRect);
    }

    private void mergeRows(int[] rows, int col) {
        modified = true;
        Rectangle initialRect = panel.table.getVisibleRect();
        panel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        List<Integer> realRows = model.realCellsInRowSpan(col, rows);
        int resultRow = model.mergeRows(realRows, col);
        panel.table.changeSelection(resultRow, col, false, false);
        ensureSelectionVisible(initialRect);
    }

    private void splitRow(int row, int col) {
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        if (!model.isEditableColumn(col)) {
            throw new IllegalArgumentException();
        }
        String text = panel.table.getValueAt(row, col).toString();
        String reference = (String) panel.table.getValueAt(row,
                col == BeadTableModel.COL_SRC ? BeadTableModel.COL_TRG : BeadTableModel.COL_SRC);
        SplittingPanelController splitter = new SplittingPanelController(text, reference);
        String[] split = splitter.show(SwingUtilities.getWindowAncestor(panel.table));
        if (split.length == 1) {
            return;
        }
        modified = true;
        Rectangle initialRect = panel.table.getVisibleRect();
        panel.table.clearSelection();
        int[] resultRows = model.splitRow(row, col, split);
        panel.table.changeSelection(resultRows[0], col, false, false);
        panel.table.changeSelection(resultRows[resultRows.length - 1], col, false, true);
        ensureSelectionVisible(initialRect);
    }

    private void splitBead(int[] rows, int col) {
        modified = true;
        panel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        Rectangle initialRect = panel.table.getVisibleRect();
        model.splitBead(rows);
        panel.table.changeSelection(rows[0], col, false, false);
        panel.table.changeSelection(rows[rows.length - 1], col, false, true);
        ensureSelectionVisible(initialRect);
    }

    private void editRow(int row, int col) {
        String text = panel.table.getValueAt(row, col).toString();
        EditingPanelController splitter = new EditingPanelController(text);
        String newText = splitter.show(SwingUtilities.getWindowAncestor(panel.table));
        if (newText == null || text.equals(newText)) {
            return;
        }
        modified = true;
        Rectangle initialRect = panel.table.getVisibleRect();
        panel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        model.editRow(row, col, newText);
        panel.table.changeSelection(row, col, false, false);
        ensureSelectionVisible(initialRect);
    }

    private void realignPending() {
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        List<MutableBead> data = model.getData();
        List<MutableBead> toAlign = new ArrayList<>();
        List<MutableBead> result = new ArrayList<>(data.size());
        for (MutableBead bead : data) {
            if (bead.status == Status.ACCEPTED) {
                if (!toAlign.isEmpty()) {
                    result.addAll(aligner.doAlign(toAlign));
                    toAlign.clear();
                }
                result.add(bead);
            } else {
                toAlign.add(bead);
            }
        }
        if (!toAlign.isEmpty()) {
            result.addAll(aligner.doAlign(toAlign));
        }
        modified = true;
        model.replaceData(result);
        panel.table.repaint();
        resizeRows(panel.table);
    }

    private void pinpointAlign(int row, int col) {
        if (row == ppRow || col == ppCol) {
            return;
        }
        modified = true;
        Rectangle initialRect = panel.table.getVisibleRect();
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        IntStream.of(ppRow, row).forEach(i -> {
            List<Integer> rowspan = model.getRowExtentsForBeadAtRow(i);
            if (rowspan.size() > 1) {
                model.splitBead(rowspan.stream().mapToInt(Integer::intValue).toArray());
            }
        });
        int relocateCol = ppRow < row ? ppCol : col;
        List<String> toRelocate = new ArrayList<>();
        for (int i = Math.min(ppRow, row); i <= Math.max(ppRow, row); i++) {
            String line = model.removeLine(i, relocateCol);
            if (line != null) {
                toRelocate.add(line);
            }
        }
        int resultRow = model.insertLines(toRelocate, Math.max(ppRow, row), relocateCol);
        model.setStatusAtRow(resultRow, Status.ACCEPTED);
        panel.table.changeSelection(resultRow, ppCol, false, false);
        panel.table.changeSelection(resultRow, col, false, true);
        ppRow = -1;
        ppCol = -1;
        phase = Phase.EDIT;
        ensureSelectionVisible(initialRect);
        updatePanel();
    }

    private void toggleEnabled(int... rows) {
        if (rows.length == 0) {
            return;
        }
        modified = true;
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        model.toggleBeadsAtRows(rows);
        panel.table.repaint();
    }

    private void toggleAllEnabled(boolean value) {
        modified = true;
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        model.toggleAllBeads(value);
        panel.table.repaint();
    }

    private void setStatus(MutableBead.Status status, int... rows) {
        if (rows.length == 0) {
            return;
        }
        modified = true;
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        for (int row : rows) {
            model.setStatusAtRow(row, status);
        }
        int nextBeadRow = model.nextBeadFromRow(rows[rows.length - 1]);
        if (nextBeadRow != -1) {
            int[] cols = panel.table.getSelectedColumns();
            panel.table.changeSelection(nextBeadRow, cols[0], false, false);
            panel.table.changeSelection(nextBeadRow, cols[cols.length - 1], false, true);
            ensureSelectionVisible(panel.table.getVisibleRect());
        }
    }

    private void ensureSelectionVisible(Rectangle initialView) {
        panel.table.repaint();
        resizeRows(panel.table);
        int[] rows = panel.table.getSelectedRows();
        int[] cols = panel.table.getSelectedColumns();
        Rectangle selectionRect = panel.table.getCellRect(rows[0], cols[0], true)
                .union(panel.table.getCellRect(rows[rows.length - 1], cols[cols.length - 1], true));
        panel.table.scrollRectToVisible(initialView);
        panel.table.scrollRectToVisible(selectionRect);
    }

    private boolean confirmReset(Component comp) {
        if (!modified) {
            return true;
        }
        return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(comp,
                OStrings.getString("ALIGNER_PANEL_RESET_WARNING_MESSAGE"),
                OStrings.getString("ALIGNER_DIALOG_WARNING_TITLE"), JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Reloads the beads with the current settings. The loading itself takes
     * place on a background thread.
     */
    private void reloadBeads() {
        if (loader != null) {
            loader.cancel(true);
        }
        phase = Phase.ALIGN;
        panel.progressBar.setVisible(true);
        panel.continueButton.setEnabled(false);
        panel.controlsPanel.setVisible(false);
        loader = new SwingWorker<List<MutableBead>, Object>() {
            @Override
            protected List<MutableBead> doInBackground() throws Exception {
                return aligner.alignImpl().filter(o -> !isCancelled()).map(MutableBead::new)
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                List<MutableBead> beads = null;
                try {
                    beads = get();
                } catch (CancellationException ex) {
                    // Ignore
                } catch (Exception e) {
                    Log.log(e);
                    JOptionPane.showMessageDialog(panel, OStrings.getString("ALIGNER_ERROR_LOADING"),
                            OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                }
                panel.continueButton.setEnabled(true);
                panel.progressBar.setVisible(false);
                panel.comparisonComboBox.setModel(
                        new DefaultComboBoxModel<>(aligner.allowedModes.toArray(new ComparisonMode[0])));

                String distanceValue = null;
                if (beads != null) {
                    double avgDist = MutableBead.calculateAvgDist(beads);
                    distanceValue = StringUtil.format(OStrings.getString("ALIGNER_PANEL_LABEL_AVGSCORE"),
                            avgDist == Long.MAX_VALUE ? "-" : String.format("%.3f", avgDist));
                    panel.table.setModel(new BeadTableModel(beads));
                    for (int i = 0; i < BeadTableModel.COL_SRC; i++) {
                        TableColumn col = panel.table.getColumnModel().getColumn(i);
                        col.setMaxWidth(col.getWidth());
                    }
                    modified = false;
                }
                panel.averageDistanceLabel.setText(distanceValue);

                updatePanel();
            }
        };
        loader.execute();
    }

    /**
     * Ensure that the panel controls and available menu items are synced with
     * the settings of the underlying aligner.
     */
    private void updatePanel() {
        panel.comparisonComboBox.setSelectedItem(aligner.comparisonMode);
        panel.algorithmComboBox.setSelectedItem(aligner.algorithmClass);
        panel.calculatorComboBox.setSelectedItem(aligner.calculatorType);
        panel.counterComboBox.setSelectedItem(aligner.counterType);
        panel.segmentingCheckBox.setSelected(aligner.segment);
        frame.segmentingItem.setSelected(aligner.segment);
        panel.segmentingRulesButton.setEnabled(aligner.segment);
        frame.segmentingRulesItem.setEnabled(aligner.segment);
        panel.removeTagsCheckBox.setSelected(aligner.removeTags);
        frame.removeTagsItem.setSelected(aligner.removeTags);

        panel.advancedPanel.setVisible(phase == Phase.ALIGN);
        panel.segmentationControlsPanel.setVisible(phase == Phase.ALIGN);
        panel.filteringControlsPanel.setVisible(phase == Phase.ALIGN);
        panel.continueButton.setVisible(phase == Phase.ALIGN);
        panel.controlsPanel.setVisible(phase != Phase.ALIGN);
        panel.controlsPanel.setEnabled(phase == Phase.EDIT);
        panel.saveButton.setVisible(phase != Phase.ALIGN);
        panel.saveButton.setEnabled(phase == Phase.EDIT);
        String instructions = null;
        switch (phase) {
        case ALIGN:
            instructions = OStrings.getString("ALIGNER_PANEL_ALIGN_PHASE_HELP");
            break;
        case EDIT:
            instructions = OStrings.getString("ALIGNER_PANEL_EDIT_PHASE_HELP");
            break;
        case PINPOINT:
            instructions = OStrings.getString("ALIGNER_PANEL_PINPOINT_PHASE_HELP");
        }
        panel.instructionsLabel.setText(instructions);
        frame.editMenu.setEnabled(phase != Phase.ALIGN);
        for (Component c : frame.editMenu.getComponents()) {
            // Batch-enable/disable Edit menu items here, then override later if
            // necessary
            c.setEnabled(phase == Phase.EDIT);
        }
        frame.optionsMenu.setEnabled(phase == Phase.ALIGN);
        frame.saveItem.setEnabled(phase == Phase.EDIT);

        panel.table.setCursor(Cursor.getPredefinedCursor(
                phase == Phase.PINPOINT ? Cursor.CROSSHAIR_CURSOR : Cursor.DEFAULT_CURSOR));
        frame.pinpointAlignStartItem.setVisible(phase != Phase.PINPOINT);
        frame.pinpointAlignEndItem.setVisible(phase == Phase.PINPOINT);
        // frame.pinpointAlign[Start|End]Item enabledness depends on table
        // selection
        frame.pinpointAlignCancelItem.setVisible(phase == Phase.PINPOINT);
        frame.pinpointAlignCancelItem.setEnabled(phase == Phase.PINPOINT);

        JButton defaultButton = phase == Phase.ALIGN ? panel.continueButton
                : phase == Phase.EDIT ? panel.saveButton : null;
        frame.getRootPane().setDefaultButton(defaultButton);

        updateCommandAvailability(panel, frame);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                resizeRows(panel.table);
            }
        });
    }

    private void updateHighlight() {
        panel.highlightCheckBox.setSelected(doHighlight);
        frame.highlightItem.setSelected(doHighlight);
        panel.highlightPatternButton.setEnabled(doHighlight);
        frame.highlightPatternItem.setEnabled(doHighlight);
        panel.table.repaint();
    }

    private void updateCommandAvailability(AlignPanel panel, AlignMenuFrame frame) {
        if (!(panel.table.getModel() instanceof BeadTableModel)) {
            return;
        }
        int[] rows = panel.table.getSelectedRows();
        int[] cols = panel.table.getSelectedColumns();
        int col = cols.length > 0 ? cols[0] : -1;
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        List<Integer> realRows = model.realCellsInRowSpan(col, rows);
        boolean enabled = phase == Phase.EDIT && !realRows.isEmpty() && cols.length == 1
                && model.isEditableColumn(col);
        boolean canUp = enabled && model.canMove(realRows.get(0), col, true);
        boolean canDown = enabled && model.canMove(realRows.get(realRows.size() - 1), col, false);
        int beads = model.beadsInRowSpan(rows);
        boolean canSplit = (realRows.size() == 1 && rows.length == 1) || (!realRows.isEmpty() && beads == 1);
        boolean canMerge = realRows.size() > 1 || (realRows.size() == 1 && rows.length == 1
                && realRows.get(0) < panel.table.getRowCount() - 1);
        boolean canEdit = realRows.size() == 1;
        panel.moveDownButton.setEnabled(enabled && canDown);
        frame.moveDownItem.setEnabled(enabled && canDown);
        panel.moveUpButton.setEnabled(enabled && canUp);
        frame.moveUpItem.setEnabled(enabled && canUp);
        panel.splitButton.setEnabled(enabled && canSplit);
        frame.splitItem.setEnabled(enabled && canSplit);
        panel.mergeButton.setEnabled(enabled && canMerge);
        frame.mergeItem.setEnabled(enabled && canMerge);
        panel.editButton.setEnabled(enabled && canEdit);
        frame.editItem.setEnabled(enabled && canEdit);
        frame.pinpointAlignStartItem.setEnabled(enabled && rows.length == 1);
        frame.pinpointAlignEndItem.setEnabled(
                phase == Phase.PINPOINT && rows.length == 1 && cols.length == 1 && realRows.size() == 1
                        && realRows.get(0) != ppRow && col != ppCol && model.isEditableColumn(col));
    }

    private String getOutFileName() {
        String src = FilenameUtils.getBaseName(aligner.srcFile);
        String trg = FilenameUtils.getBaseName(aligner.trgFile);
        if (src.equals(trg)) {
            return src + "_" + aligner.srcLang.getLanguage() + "_" + aligner.trgLang.getLanguage() + ".tmx";
        } else {
            return src + "_" + trg + ".tmx";
        }
    }

    private void closeFrame(JFrame frame) {
        if (confirmReset(frame)) {
            frame.setVisible(false);
            confirmSaveSRX(frame);
            confirmSaveFilters(frame);
            frame.dispose();
        }
    }

    /**
     * If the user has modified the SRX rules, offer to save them permanently.
     * Otherwise, they are simply discarded. Does nothing when OmegaT's main
     * window is not available (changes are always discarded under standalone
     * use).
     *
     * @param comp
     *            Parent component for dialog boxes
     */
    private void confirmSaveSRX(Component comp) {
        if (Core.getMainWindow() == null || customizedSRX == null) {
            return;
        }
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(comp,
                OStrings.getString("ALIGNER_DIALOG_SEGMENTATION_CONFIRM_MESSAGE"),
                OStrings.getString("ALIGNER_DIALOG_CONFIRM_TITLE"), JOptionPane.OK_CANCEL_OPTION)) {
            if (Core.getProject().isProjectLoaded()
                    && Core.getProject().getProjectProperties().getProjectSRX() != null) {
                Core.getProject().getProjectProperties().setProjectSRX(customizedSRX);
                try {
                    Core.getProject().saveProjectProperties();
                } catch (Exception ex) {
                    Log.log(ex);
                    JOptionPane.showMessageDialog(comp, OStrings.getString("CT_ERROR_SAVING_PROJ"),
                            OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                }
                ProjectUICommands.promptReload();
            } else {
                Preferences.setSRX(customizedSRX);
            }
        }
    }

    /**
     * If the user has modified the file filter settings, offer to save them
     * permanently. Otherwise they are simply discarded. Does nothing when
     * OmegaT's main window is not available (changes are always discarded under
     * standalone use).
     *
     * @param comp
     *            Parent component for dialog boxes
     */
    private void confirmSaveFilters(Component comp) {
        if (Core.getMainWindow() == null || customizedFilters == null) {
            return;
        }
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(comp,
                OStrings.getString("ALIGNER_DIALOG_FILTERS_CONFIRM_MESSAGE"),
                OStrings.getString("ALIGNER_DIALOG_CONFIRM_TITLE"), JOptionPane.OK_CANCEL_OPTION)) {
            if (Core.getProject().isProjectLoaded()
                    && Core.getProject().getProjectProperties().getProjectFilters() != null) {
                Core.getProject().getProjectProperties().setProjectFilters(customizedFilters);
                try {
                    Core.getProject().saveProjectProperties();
                } catch (Exception ex) {
                    Log.log(ex);
                    JOptionPane.showMessageDialog(comp, OStrings.getString("CT_ERROR_SAVING_PROJ"),
                            OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                }
                ProjectUICommands.promptReload();
            } else {
                Preferences.setFilters(customizedFilters);
            }
        }
    }

    private boolean confirmSaveTMX(AlignPanel panel) {
        BeadTableModel model = (BeadTableModel) panel.table.getModel();
        boolean needsReview = false;
        for (MutableBead bead : model.getData()) {
            if (bead.status == MutableBead.Status.NEEDS_REVIEW) {
                needsReview = true;
                break;
            }
        }
        if (needsReview) {
            return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(panel,
                    OStrings.getString("ALIGNER_DIALOG_NEEDSREVIEW_CONFIRM_MESSAGE"),
                    OStrings.getString("ALIGNER_DIALOG_CONFIRM_TITLE"), JOptionPane.OK_CANCEL_OPTION);
        } else {
            return true;
        }
    }

    static final Border FOCUS_BORDER = new MatteBorder(1, 1, 1, 1, new Color(0x76AFE8));

    // See: http://esus.com/creating-a-jtable-with-multiline-cells/
    class MultilineCellRenderer implements TableCellRenderer {
        private final JTextPane textArea = new JTextPane();
        private final Border noFocusBorder = new EmptyBorder(FOCUS_BORDER.getBorderInsets(textArea));
        private final JCheckBox checkBox = new JCheckBox();
        private final AttributeSet highlight;

        MultilineCellRenderer() {
            // textArea.setLineWrap(true);
            // textArea.setWrapStyleWord(true);
            textArea.setOpaque(true);
            checkBox.setHorizontalAlignment(JLabel.CENTER);
            checkBox.setBorderPainted(true);
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setBackground(sas, Styles.EditorColor.COLOR_ALIGNER_HIGHLIGHT.getColor());
            highlight = sas;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            if (value instanceof Boolean) {
                doStyling(checkBox, table, isSelected, hasFocus, row, column);
                checkBox.setSelected((Boolean) value);
                return checkBox;
            } else {
                doStyling(textArea, table, isSelected, hasFocus, row, column);
                textArea.setText(null);
                if (value != null) {
                    String text = value.toString();
                    textArea.setText(text);
                    doHighlighting(text);
                }
                return textArea;
            }
        }

        private void doStyling(JComponent comp, JTable table, boolean isSelected, boolean hasFocus, int row,
                int column) {
            if (isSelected) {
                comp.setBackground(table.getSelectionBackground());
                comp.setForeground(table.getSelectionForeground());
            } else {
                MutableBead.Status status = ((BeadTableModel) table.getModel()).getStatusForRow(row);
                if (column == BeadTableModel.COL_CHECKBOX && status != MutableBead.Status.DEFAULT) {
                    switch (status) {
                    case ACCEPTED:
                        comp.setBackground(Styles.EditorColor.COLOR_ALIGNER_ACCEPTED.getColor());
                        break;
                    case NEEDS_REVIEW:
                        comp.setBackground(Styles.EditorColor.COLOR_ALIGNER_NEEDSREVIEW.getColor());
                        break;
                    case DEFAULT:
                        // Leave color as-is
                    }
                } else if (row == ppRow && column == ppCol) {
                    comp.setBackground(Color.GREEN);
                } else {
                    comp.setBackground(getBeadNumber(table, row) % 2 == 0 ? table.getBackground()
                            : Styles.EditorColor.COLOR_ALIGNER_TABLE_ROW_HIGHLIGHT.getColor());
                    comp.setForeground(table.getForeground());
                }
            }
            Border marginBorder = new EmptyBorder(1, column == 0 ? 5 : 1, 1,
                    column == table.getColumnCount() - 1 ? 5 : 1);
            if (hasFocus) {
                comp.setBorder(new CompoundBorder(marginBorder, FOCUS_BORDER));
            } else {
                comp.setBorder(new CompoundBorder(marginBorder, noFocusBorder));
            }
            comp.setFont(table.getFont());
        }

        private int getBeadNumber(JTable table, int row) {
            return ((BeadTableModel) table.getModel()).getBeadNumberForRow(row);
        }

        void doHighlighting(String text) {
            StyledDocument doc = textArea.getStyledDocument();
            doc.setCharacterAttributes(0, text.length(), new SimpleAttributeSet(), true);
            if (!doHighlight || highlightPattern == null) {
                return;
            }
            Matcher m = highlightPattern.matcher(text);
            while (m.find()) {
                doc.setCharacterAttributes(m.start(), m.end() - m.start(), highlight, true);
            }
        }
    }

    @SuppressWarnings("serial")
    class BeadTableModel extends AbstractTableModel {
        // For debugging purposes, additional columns are defined as
        // COL_CHECKBOX - n.
        // To enable them, set COL_CHECKBOX > 0.
        static final int COL_CHECKBOX = 0;
        static final int COL_SRC = COL_CHECKBOX + 1;
        static final int COL_TRG = COL_SRC + 1;

        private final List<MutableBead> data;

        // Maintain an integer (index) mapping of the contents of each row. This
        // is required when modifying
        // the underlying beads, as all changes are destructive and non-atomic.
        // It also speeds up access.
        List<Float> rowToDistance;
        List<MutableBead> rowToBead;
        List<String> rowToSourceLine;
        List<String> rowToTargetLine;

        BeadTableModel(List<MutableBead> data) {
            this.data = data;
            makeCache();
        }

        private void makeCache() {
            for (int i = 0; i < data.size(); i++) {
                MutableBead bead = data.get(i);
                // Cull empty beads (can be created by splitting top/bottom
                // line)
                if (bead.isEmpty()) {
                    data.remove(i--);
                    continue;
                }
                // Split beads with 2+-2+
                while (bead.sourceLines.size() > 1 && bead.targetLines.size() > 1) {
                    bead = splitBeadByCount(bead, 1);
                    data.add(++i, bead);
                }
            }
            List<Float> rowToDistance = new ArrayList<>();
            List<MutableBead> rowToBead = new ArrayList<>();
            List<String> rowToSourceLine = new ArrayList<>();
            List<String> rowToTargetLine = new ArrayList<>();
            for (MutableBead bead : data) {
                int beadRows = Math.max(bead.sourceLines.size(), bead.targetLines.size());
                for (int i = 0; i < beadRows; i++) {
                    rowToDistance.add(bead.score);
                    rowToBead.add(bead);
                    rowToSourceLine.add(i < bead.sourceLines.size() ? bead.sourceLines.get(i) : null);
                    rowToTargetLine.add(i < bead.targetLines.size() ? bead.targetLines.get(i) : null);
                }
            }
            this.rowToDistance = rowToDistance;
            this.rowToBead = rowToBead;
            this.rowToSourceLine = rowToSourceLine;
            this.rowToTargetLine = rowToTargetLine;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return phase == Phase.EDIT && column == COL_CHECKBOX && getValueAt(row, column) != null;
        }

        @Override
        public int getColumnCount() {
            return COL_TRG + 1;
        }

        @Override
        public int getRowCount() {
            return rowToBead.size();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case COL_CHECKBOX - 3:
                return Integer.class;
            case COL_CHECKBOX - 2:
                return Integer.class;
            case COL_CHECKBOX - 1:
                // Bead number
                return Integer.class;
            case COL_CHECKBOX:
                return Boolean.class;
            case COL_SRC:
                return String.class;
            case COL_TRG:
                return String.class;
            }
            throw new IllegalArgumentException();
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case COL_CHECKBOX - 3:
                return OStrings.getString("ALIGNER_PANEL_TABLE_COL_ROW");
            case COL_CHECKBOX - 2:
                return OStrings.getString("ALIGNER_PANEL_TABLE_COL_DISTANCE");
            case COL_CHECKBOX - 1:
                // Bead number
                return "";
            case COL_CHECKBOX:
                return OStrings.getString("ALIGNER_PANEL_TABLE_COL_KEEP");
            case COL_SRC:
                return OStrings.getString("ALIGNER_PANEL_TABLE_COL_SOURCE");
            case COL_TRG:
                return OStrings.getString("ALIGNER_PANEL_TABLE_COL_TARGET");
            }
            throw new IllegalArgumentException();
        }

        @Override
        public Object getValueAt(int row, int column) {
            MutableBead bead;
            switch (column) {
            case COL_CHECKBOX - 3:
                return row;
            case COL_CHECKBOX - 2:
                bead = rowToBead.get(row);
                if (row > 0 && bead == rowToBead.get(row - 1)) {
                    return null;
                }
                return rowToDistance.get(row);
            case COL_CHECKBOX - 1:
                bead = rowToBead.get(row);
                if (row > 0 && bead == rowToBead.get(row - 1)) {
                    return null;
                }
                return data.indexOf(bead) + 1;
            case COL_CHECKBOX:
                bead = rowToBead.get(row);
                if (row > 0 && bead == rowToBead.get(row - 1)) {
                    return null;
                }
                return bead.enabled;
            case COL_SRC:
                return rowToSourceLine.get(row);
            case COL_TRG:
                return rowToTargetLine.get(row);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex != COL_CHECKBOX) {
                throw new IllegalArgumentException();
            }
            if (!(aValue instanceof Boolean)) {
                throw new IllegalArgumentException();
            }
            rowToBead.get(rowIndex).enabled = (Boolean) aValue;
        }

        /**
         * Move the specified lines located in <code>rows</code> and
         * <code>col</code> into the bead indicated by <code>trgRow</code>.
         *
         * @param rows
         *            Rows to move
         * @param col
         *            Column of
         * @param trgRow
         *            Target to move
         * @return An array of two ints indicating the start and end rows of the
         *         selection after moving
         */
        int[] move(List<Integer> rows, int col, int trgRow) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            Collections.sort(rows);
            List<String> selected = new ArrayList<>(rows.size());
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            int origRowCount = getRowCount();
            // Bead to be modified selected here
            MutableBead trgBead;
            if (trgRow < 0) {
                // New bead created
                trgBead = new MutableBead();
                data.add(0, trgBead);
            } else if (trgRow > rowToBead.size() - 1) {
                // New bead created
                trgBead = new MutableBead();
                data.add(trgBead);
            } else {
                trgBead = rowToBead.get(trgRow);
            }
            List<String> trgLines = col == COL_SRC ? trgBead.sourceLines : trgBead.targetLines;
            for (int row : rows) {
                String line = lines.get(row);
                if (line == null) {
                    throw new IllegalArgumentException();
                }
                selected.add(line);
                MutableBead bead = rowToBead.get(row);
                if (bead == trgBead) {
                    continue;
                }
                Util.removeByIdentity(col == COL_SRC ? bead.sourceLines : bead.targetLines, line);
                int insertIndex = trgRow > row ? 0 : trgLines.size();
                // XXX: Bead modified here
                trgLines.add(insertIndex, line);
            }
            trgBead.status = Status.DEFAULT;
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
            lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            return new int[] { Util.indexByIdentity(lines, selected.get(0)),
                    Util.indexByIdentity(lines, selected.get(selected.size() - 1)) };
        }

        /**
         * Split the specified bead into two: one with an equal number of source
         * and target lines (e.g. 1-1) and one with the remainder (e.g. 0-1).
         * The new bead is inserted into the underlying data store.
         *
         * @param bead
         *            to split.
         * @return The remainder bead
         */
        private MutableBead splitBead(MutableBead bead) {
            if (bead.isBalanced()) {
                return bead;
            }
            int index = data.indexOf(bead);
            bead = splitBeadByCount(bead, Math.min(bead.sourceLines.size(), bead.targetLines.size()));
            data.add(index + 1, bead);
            return bead;
        }

        /**
         * Split the specified bead into two: the first with the specified count
         * of lines, and the second with the remainder.
         *
         * @param bead
         * @param count
         * @return The remainder bead
         */
        private MutableBead splitBeadByCount(MutableBead bead, int count) {
            List<String> splitSrc = new ArrayList<>(bead.sourceLines);
            // XXX: Bead modified here
            bead.sourceLines.clear();
            List<String> splitTrg = new ArrayList<>(bead.targetLines);
            bead.targetLines.clear();
            bead.status = Status.DEFAULT;
            for (int i = 0; i < count; i++) {
                if (!splitSrc.isEmpty()) {
                    bead.sourceLines.add(splitSrc.remove(0));
                }
                if (!splitTrg.isEmpty()) {
                    bead.targetLines.add(splitTrg.remove(0));
                }
            }
            // New bead created
            return new MutableBead(splitSrc, splitTrg);
        }

        int getBeadNumberForRow(int row) {
            return data.indexOf(rowToBead.get(row));
        }

        MutableBead.Status getStatusForRow(int row) {
            return rowToBead.get(row).status;
        }

        /**
         * Indicate whether the line at the specified <code>row</code> and
         * <code>col</code> can be moved in the indicated direction. A line is
         * movable if it is not blocked by another line in the same bead.
         *
         * @param row
         * @param col
         * @param up
         *            Up (toward index=0) when true, down when false
         * @return
         */
        boolean canMove(int row, int col, boolean up) {
            if (!isEditableColumn(col)) {
                return false;
            }
            MutableBead bead = rowToBead.get(row);
            if ((row == 0 && up) || (row == rowToBead.size() - 1 && !up)) {
                return !(col == COL_SRC ? bead.targetLines : bead.sourceLines).isEmpty();
            }
            List<String> lines = col == COL_SRC ? bead.sourceLines : bead.targetLines;
            String line = (col == COL_SRC ? rowToSourceLine : rowToTargetLine).get(row);
            int index = Util.indexByIdentity(lines, line);
            return up ? index == 0 : index == lines.size() - 1;
        }

        /**
         * Indicate whether the line at the specified <code>row</code> and
         * <code>col</code> can be moved to the bead indicated by
         * <code>trgRow</code>. In addition to requiring
         * {@link #canMove(int, int, boolean)} to return true, the bead must be
         * different from the current bead, and no non-empty cells can exist
         * between the current and target rows.
         *
         * @param trgRow
         *            target row to be moved.
         * @param row
         *            specified row to move.
         * @param col
         *            specified column to move.
         * @param up
         *            Up (toward index=0) when true, down when false
         * @return false when condition not met, otherwise true.
         */
        boolean canMoveTo(int trgRow, int row, int col, boolean up) {
            if (!canMove(row, col, up) || trgRow == row) {
                return false;
            }
            // Check same bead
            if (trgRow >= 0 && trgRow < rowToBead.size()) {
                MutableBead srcBead = rowToBead.get(row);
                MutableBead trgBead = rowToBead.get(trgRow);
                if (srcBead == trgBead) {
                    return false;
                }
            }
            // Check no non-empty cells in path
            int inc = up ? -1 : 1;
            for (int r = row + inc; r != trgRow && r >= 0 && r < rowToSourceLine.size(); r += inc) {
                String line = (col == COL_SRC ? rowToSourceLine : rowToTargetLine).get(r);
                if (line != null) {
                    return false;
                }
            }
            return true;
        }

        List<MutableBead> getData() {
            return Collections.unmodifiableList(data);
        }

        /**
         * Get a list of rows covered by the bead at <code>row</code>.
         *
         * @param row
         * @return
         */
        List<Integer> getRowExtentsForBeadAtRow(int row) {
            MutableBead bead = rowToBead.get(row);
            List<Integer> result = new ArrayList<>();
            int firstIndex = rowToBead.indexOf(bead);
            if (firstIndex == -1) {
                throw new IllegalArgumentException();
            }
            for (int i = firstIndex; i < rowToBead.size(); i++) {
                if (rowToBead.get(i) != bead) {
                    break;
                }
                result.add(i);
            }
            return result;
        }

        boolean isEditableColumn(int col) {
            return col == COL_SRC || col == COL_TRG;
        }

        /**
         * Get a list of rows for which the cell in the specified
         * <code>col</code> represents an actual line (is not empty).
         *
         * @param col
         *            column number
         * @param rows
         *            requested row index
         * @return list of row numbers cell in.
         */
        List<Integer> realCellsInRowSpan(int col, int... rows) {
            List<Integer> result = new ArrayList<Integer>();
            for (int row : rows) {
                if (getValueAt(row, col) != null) {
                    result.add(row);
                }
            }
            return result;
        }

        /**
         * Get the last row of the bead immediately preceding the one at the
         * indicated <code>row</code>.
         *
         * @param row
         *            offset to move.
         * @return The row, or -1 if there is no previous bead
         */
        int prevBeadFromRow(int row) {
            return nextBeadFromRowByOffset(row, -1);
        }

        /**
         * Get the first row of the bead immediately after the one at the
         * indicated <code>row</code>.
         *
         * @param row
         *            offset to move.
         * @return The row, or -1 if there is no next bead
         */
        int nextBeadFromRow(int row) {
            return nextBeadFromRowByOffset(row, 1);
        }

        private int nextBeadFromRowByOffset(int row, int offset) {
            MutableBead bead = rowToBead.get(row);
            for (int i = row + offset; i < getRowCount(); i += offset) {
                if (rowToBead.get(i) != bead) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Merge all lines at the indicated <code>rows</code> and
         * <code>col</code> into the first specified row. This is destructive in
         * that it actually joins the strings together and replaces the existing
         * value.
         *
         * @param rows
         *            target to be merged.
         * @param col
         *            Column of
         * @return The resulting row
         */
        int mergeRows(List<Integer> rows, int col) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            int origRowCount = getRowCount();
            List<String> toCombine = new ArrayList<>();
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            toCombine.add(lines.get(rows.get(0)));
            for (int i = 1; i < rows.size(); i++) {
                int row = rows.get(i);
                String line = lines.get(row);
                toCombine.add(line);
                // XXX: Bead modified
                MutableBead bead = rowToBead.get(row);
                Util.removeByIdentity(col == COL_SRC ? bead.sourceLines : bead.targetLines, line);
                bead.status = Status.DEFAULT;
            }
            MutableBead trgBead = rowToBead.get(rows.get(0));
            List<String> trgLines = col == COL_SRC ? trgBead.sourceLines : trgBead.targetLines;
            Language lang = col == COL_SRC ? aligner.srcLang : aligner.trgLang;
            String combined = Util.join(lang, toCombine);
            // XXX: Bead modified
            trgLines.set(Util.indexByIdentity(trgLines, toCombine.get(0)), combined);
            trgBead.status = Status.DEFAULT;
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
            lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            return Util.indexByIdentity(lines, combined);
        }

        /**
         * Replace the line at <code>row</code> and <code>col</code> with the
         * specified <code>split</code> lines, which are inserted in its place.
         * This is destructive in that it removes the original line entirely.
         *
         * @param row
         *            target to split.
         * @param col
         *            Column of
         * @param split
         *            lines to insert.
         * @return A two-member array indicating the first and last resulting
         *         rows
         */
        int[] splitRow(int row, int col, String[] split) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            int origRowCount = getRowCount();
            MutableBead trgBead = rowToBead.get(row);
            List<String> trgLines = (col == COL_SRC ? trgBead.sourceLines : trgBead.targetLines);
            String line = (col == COL_SRC ? rowToSourceLine : rowToTargetLine).get(row);
            int insertAt = Util.indexByIdentity(trgLines, line);
            // XXX: Bead modified
            trgLines.set(insertAt++, split[0]);
            for (int i = 1; i < split.length; i++) {
                // XXX: Bead modified
                trgLines.add(insertAt++, split[i]);
            }
            trgBead.status = Status.DEFAULT;
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            return new int[] { Util.indexByIdentity(lines, split[0]),
                    Util.indexByIdentity(lines, split[split.length - 1]) };
        }

        /**
         * Replace the line at <code>row</code> and <code>col</code> with the
         * specified <code>newVal</code>. This is destructive in that it removes
         * the original line entirely.
         *
         * @param row
         * @param col
         * @param newVal
         */
        void editRow(int row, int col, String newVal) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            MutableBead trgBead = rowToBead.get(row);
            List<String> trgLines = (col == COL_SRC ? trgBead.sourceLines : trgBead.targetLines);
            String line = (col == COL_SRC ? rowToSourceLine : rowToTargetLine).get(row);
            int insertAt = Util.indexByIdentity(trgLines, line);
            // XXX: Bead modified
            trgLines.set(insertAt, newVal);
            makeCache();
        }

        /**
         * Move the lines at the specified <code>rows</code> and
         * <code>col</code> by the specified offset, e.g. +1 or -1 where
         * negative indicates the upwards direction in the table.
         * <p>
         * This is different from {@link #move(List, int, int)} in that the
         * intended effect is to give the impression of each row moving by the
         * offset relative to the opposing column. Because displayed rows don't
         * map directly to lines, that means some rows won't move at all, e.g.
         * if the target row is still the same bead.
         *
         * @param rows
         *            rows data
         * @param col
         *            column number to slide
         * @param offset
         *            offset of slide
         * @return A two-member array indicating the first and last resulting
         *         rows
         */
        int[] slide(List<Integer> rows, int col, int offset) {
            if (offset == 0) {
                return new int[0];
            }
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            Collections.sort(rows);
            if (offset > 0) {
                // Handling traversing empty rows when sliding down requires
                // sliding in reverse order.
                Collections.reverse(rows);
            }
            int origRowCount = getRowCount();
            List<String> selected = new ArrayList<>(rows.size());
            for (int row : rows) {
                List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
                String line = lines.get(row);
                if (line == null) {
                    throw new IllegalArgumentException();
                }
                selected.add(line);
                MutableBead bead = rowToBead.get(row);
                int trgRow = row + offset;
                MutableBead trgBead;
                if (trgRow < 0) {
                    // New bead created
                    trgBead = new MutableBead();
                    data.add(0, trgBead);
                } else if (trgRow > rowToBead.size() - 1) {
                    // New bead created
                    trgBead = new MutableBead();
                    data.add(trgBead);
                } else {
                    trgBead = rowToBead.get(trgRow);
                }
                if (trgBead == bead) {
                    if (lines.get(trgRow) != null) {
                        // Already in target bead
                        continue;
                    } else {
                        // Moving down in unbalanced bead where target is blank
                        // cell -> split bead and place
                        // into resulting remainder bead
                        trgBead = splitBead(trgBead);
                    }
                }
                // XXX: Bead modified here
                Util.removeByIdentity(col == COL_SRC ? bead.sourceLines : bead.targetLines, line);
                bead.status = Status.DEFAULT;
                List<String> trgLines = col == COL_SRC ? trgBead.sourceLines : trgBead.targetLines;
                int insertIndex = trgRow > row ? 0 : trgLines.size();
                trgLines.add(insertIndex, line);
                trgBead.status = Status.DEFAULT;
            }
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            int[] resultRows = new int[] { Util.indexByIdentity(lines, selected.get(0)),
                    Util.indexByIdentity(lines, selected.get(selected.size() - 1)) };
            // Sort result rows so that callers can expect high-to-low order
            Arrays.sort(resultRows);
            return resultRows;
        }

        /**
         * Get the number of beads contained within the specified rows.
         *
         * @param rows
         * @return
         */
        int beadsInRowSpan(int... rows) {
            List<MutableBead> beads = new ArrayList<MutableBead>();
            for (int row : rows) {
                MutableBead bead = rowToBead.get(row);
                if (!beads.contains(bead)) {
                    beads.add(bead);
                }
            }
            return beads.size();
        }

        /**
         * Split the lines specified at <code>rows</code> and <code>col</code>
         * into multiple beads.
         *
         * @param rows
         * @return A two-member array indicating the first and last resulting
         *         rows
         */
        void splitBead(int[] rows) {
            int origRowCount = getRowCount();
            MutableBead bead = rowToBead.get(rows[0]);
            int beadIndex = data.indexOf(bead);
            for (int row : rows) {
                String line = rowToSourceLine.get(row);
                List<String> indexFrom = bead.sourceLines;
                if (line == null) {
                    line = rowToTargetLine.get(row);
                    indexFrom = bead.targetLines;
                }
                int index = Util.indexByIdentity(indexFrom, line);
                if (index == -1) {
                    throw new IllegalArgumentException();
                }
                if (index > 0) {
                    bead = splitBeadByCount(bead, index);
                    data.add(++beadIndex, bead);
                }
            }
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
        }

        void toggleBeadsAtRows(int... rows) {
            List<MutableBead> beads = new ArrayList<MutableBead>(rows.length);
            for (int row : rows) {
                MutableBead bead = rowToBead.get(row);
                if (!beads.contains(bead)) {
                    bead.enabled = !bead.enabled;
                    beads.add(bead);
                }
            }
        }

        void toggleAllBeads(boolean value) {
            for (MutableBead bead : data) {
                bead.enabled = value;
            }
        }

        void setStatusAtRow(int row, MutableBead.Status status) {
            MutableBead bead = rowToBead.get(row);
            bead.status = status;
        }

        int nextNonEmptyCell(int row, int col) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            for (int i = row + 1; i < lines.size(); i++) {
                if (lines.get(i) != null) {
                    return i;
                }
            }
            return -1;
        }

        void replaceData(List<MutableBead> newData) {
            data.clear();
            data.addAll(newData);
            makeCache();
            fireTableDataChanged();
        }

        String removeLine(int row, int col) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            MutableBead bead = rowToBead.get(row);
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            String line = lines.get(row);
            // XXX: Bead modified here
            Util.removeByIdentity(col == COL_SRC ? bead.sourceLines : bead.targetLines, line);
            bead.status = Status.DEFAULT;
            return line;
        }

        int insertLines(List<String> lines, int row, int col) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            int origRowCount = getRowCount();
            MutableBead bead = rowToBead.get(row);
            // XXX: Bead modified here
            (col == COL_SRC ? bead.sourceLines : bead.targetLines).add(lines.get(0));
            bead.status = Status.DEFAULT;
            int beadInsertIndex = data.indexOf(bead) + 1;
            List<MutableBead> newBeads = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                // New bead created
                MutableBead newBead = new MutableBead();
                (col == COL_SRC ? newBead.sourceLines : newBead.targetLines).add(lines.get(i));
                newBeads.add(newBead);
            }
            data.addAll(beadInsertIndex, newBeads);
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
            return Util.indexByIdentity(col == COL_SRC ? rowToSourceLine : rowToTargetLine, lines.get(0));
        }
    }

    @SuppressWarnings("serial")
    class AlignTransferHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (!(c instanceof JTable)) {
                return null;
            }
            JTable table = (JTable) c;
            return new TableSelection(table.getSelectedRows(), table.getSelectedColumns());
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (phase != Phase.EDIT) {
                return false;
            }
            if (!support.isDataFlavorSupported(ARRAY2DFLAVOR)) {
                return false;
            }
            try {
                Object o = support.getTransferable().getTransferData(ARRAY2DFLAVOR);
                int[][] sel = (int[][]) o;

                int[] rows = sel[0];
                if (rows.length < 1) {
                    return false;
                }

                int[] cols = sel[1];
                if (cols.length != 1) {
                    return false;
                }

                JTable table = (JTable) support.getComponent();
                BeadTableModel model = (BeadTableModel) table.getModel();

                int col = cols[0];
                if (!model.isEditableColumn(col)) {
                    return false;
                }

                javax.swing.JTable.DropLocation dloc = (javax.swing.JTable.DropLocation) support
                        .getDropLocation();
                if (dloc.getColumn() != col) {
                    return false;
                }
                int trgRow = dloc.getRow();

                List<Integer> realRows = model.realCellsInRowSpan(col, rows);
                if (trgRow < realRows.get(0)) {
                    return model.canMoveTo(trgRow, realRows.get(0), col, true);
                } else if (trgRow > realRows.get(realRows.size() - 1)) {
                    return model.canMoveTo(trgRow, realRows.get(realRows.size() - 1), col, false);
                }
            } catch (Exception e) {
                Log.log(e);
            }
            return false;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                Object o = support.getTransferable().getTransferData(ARRAY2DFLAVOR);
                int[][] sel = (int[][]) o;

                int[] rows = sel[0];
                int[] cols = sel[1];
                int col = cols[0];

                javax.swing.JTable.DropLocation dloc = (javax.swing.JTable.DropLocation) support
                        .getDropLocation();
                int trgRow = dloc.getRow();

                moveRows(rows, col, trgRow);
                return true;
            } catch (Exception e) {
                Log.log(e);
            }
            return false;
        }
    }

    private static final DataFlavor ARRAY2DFLAVOR = new DataFlavor(int[][].class, "2D int array");

    static class TableSelection implements Transferable {
        private static final DataFlavor[] FLAVORS = new DataFlavor[] { ARRAY2DFLAVOR };
        private final int[] rows;
        private final int[] cols;

        TableSelection(int[] rows, int[] cols) {
            this.rows = rows;
            this.cols = cols;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return ARRAY2DFLAVOR.equals(flavor);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return FLAVORS;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (ARRAY2DFLAVOR.equals(flavor)) {
                return new int[][] { rows, cols };
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    static class DropLocationListener implements PropertyChangeListener {
        private static final int ERASE_MARGIN = 5;
        private static final int INSET_MARGIN = 3;
        private static final Border BORDER = new RoundedCornerBorder(8, Color.BLUE,
                RoundedCornerBorder.SIDE_ALL, 2);

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            DropLocation oldVal = (DropLocation) evt.getOldValue();
            DropLocation newVal = (DropLocation) evt.getNewValue();
            if (equals(oldVal, newVal)) {
                return;
            }
            final JTable table = (JTable) evt.getSource();
            if (oldVal != null) {
                Rectangle rect = rectForTarget(table, oldVal);
                rect.grow(ERASE_MARGIN, ERASE_MARGIN);
                table.paintImmediately(rect);
            }
            if (newVal != null) {
                final Rectangle rect = rectForTarget(table, newVal);
                rect.grow(INSET_MARGIN, INSET_MARGIN);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        BORDER.paintBorder(table, table.getGraphics(), rect.x, rect.y, rect.width,
                                rect.height);
                    }
                });
            }
        }

        private boolean equals(DropLocation oldVal, DropLocation newVal) {
            if (oldVal == newVal) {
                return true;
            }
            if (oldVal == null || newVal == null) {
                return false;
            }
            return oldVal.getColumn() == newVal.getColumn() && oldVal.getRow() == newVal.getRow();
        }

        private Rectangle rectForTarget(JTable table, DropLocation loc) {
            BeadTableModel model = (BeadTableModel) table.getModel();
            List<Integer> rows = model.getRowExtentsForBeadAtRow(loc.getRow());
            return table.getCellRect(rows.get(0), BeadTableModel.COL_SRC, true)
                    .union(table.getCellRect(rows.get(rows.size() - 1), BeadTableModel.COL_TRG, true));
        }
    }

    static class EnumRenderer<T extends Enum<?>> extends DelegatingComboBoxRenderer<T, String> {
        private final String keyPrefix;

        EnumRenderer(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        @Override
        protected String getDisplayText(T value) {
            if (value == null) {
                return null;
            }
            try {
                return OStrings.getString(keyPrefix + value.name());
            } catch (MissingResourceException ex) {
                return value.name();
            }
        }
    }
}
