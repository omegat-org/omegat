/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.align;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.util.ResourceBundle;
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
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.io.FilenameUtils;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
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
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.DelegatingComboBoxRenderer;
import org.omegat.util.gui.RoundedCornerBorder;
import org.omegat.util.gui.Styles;

import gen.core.filters.Filters;

/**
 * Controller for the alignment UI.
 *
 * @author Aaron Madlon-Kay
 */
@NullMarked
public class AlignPanelController {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.omegat.gui.align.Bundle");
    private final @Nullable String defaultSaveDir;
    private boolean modified = false;
    private @Nullable SRX customizedSRX;
    private @Nullable Filters customizedFilters;

    private @Nullable SwingWorker<?, ?> loader;

    private boolean doHighlight = true;
    private Pattern highlightPattern = Pattern.compile(Preferences.getPreferenceDefault(
            Preferences.ALIGNER_HIGHLIGHT_PATTERN, Preferences.ALIGNER_HIGHLIGHT_PATTERN_DEFAULT));

    private int ppRow = -1;
    private int ppCol = -1;

    private final AlignPanel alignPanel;
    private final AlignMenuFrame alignMenuFrame;

    /**
     * The alignment workflow is separated into two phases:
     * <ol>
     * <li>Align: Verify and tweak the results of automatic algorithmic
     * alignment
     * <li>Edit: Manually edit the results
     * </ol>
     */
    private enum Phase {
        ALIGN("ALIGN"), EDIT("EDIT"), PINPOINT("PINPOINT");

        final String key;

        Phase(String key) {
            this.key = key;
        }
    }

    private Phase phase = Phase.ALIGN;

    public AlignPanelController(@Nullable String defaultSaveDir) {
        this.defaultSaveDir = defaultSaveDir;
        alignPanel = new AlignPanel();
        alignMenuFrame = new AlignMenuFrame();
    }

    /**
     * Display the aligning tool. The tool is not modal, so this call will
     * return immediately.
     *
     * @param parent
     *            Parent window of the align tool
     */
    public void show(@Nullable Component parent, Aligner aligner) {
        alignMenuFrame.setTitle(BUNDLE.getString("ALIGNER_PANEL"));
        alignMenuFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        alignMenuFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeFrame(alignMenuFrame);
            }
        });

        // set names
        alignPanel.setName("align_panel");
        alignPanel.controlsPanel.setName("align_controls_panel");
        alignPanel.advancedPanel.setName("align_advanced_panel");
        alignPanel.comparisonComboBox.setName("align_panel_comparison_cb");
        alignPanel.calculatorComboBox.setName("align_panel_calculator_cb");
        alignPanel.algorithmComboBox.setName("align_panel_algorithm_cb");
        alignPanel.counterComboBox.setName("align_panel_counter_cb");
        alignPanel.segmentingCheckBox.setName("align_panel_segmenting_checkbox");
        alignPanel.removeTagsCheckBox.setName("align_panel_remove_tags_checkbox");
        alignPanel.highlightCheckBox.setName("align_panel_highlight_checkbox");
        alignPanel.segmentingRulesButton.setName("align_panel_segmenting_rules_button");
        alignPanel.fileFilterSettingsButton.setName("align_panel_file_filter_settings_button");
        alignPanel.table.setName("align_panel_table");
        alignPanel.instructionsLabel.setName("align_panel_instructions_label");
        alignPanel.averageDistanceLabel.setName("align_panel_average_distance_label");
        alignPanel.continueButton.setName("align_panel_continue_button");
        alignPanel.saveButton.setName("align_panel_save_button");
        alignMenuFrame.setName("ALIGN_MENU_FRAME");
        alignMenuFrame.editMenu.setName("align_menu_edit");
        alignMenuFrame.fileMenu.setName("align_menu_file");
        alignMenuFrame.optionsMenu.setName("align_menu_options");
        alignMenuFrame.segmentingItem.setName("align_menu_segmenting_item");
        alignMenuFrame.closeItem.setName("align_menu_close_item");

        ActionListener comparisonListener = e -> {
            ComparisonMode newValue = (ComparisonMode) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.comparisonMode && confirmReset(alignMenuFrame)) {
                aligner.comparisonMode = newValue;
                reloadBeads(aligner);
            } else {
                alignPanel.comparisonComboBox.setSelectedItem(aligner.comparisonMode);
            }
        };
        alignPanel.comparisonComboBox.addActionListener(comparisonListener);
        alignPanel.comparisonComboBox.setRenderer(new EnumRenderer<>("ALIGNER_ENUM_COMPARISON_MODE_"));

        ActionListener algorithmListener = e -> {
            AlgorithmClass newValue = (AlgorithmClass) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.algorithmClass && confirmReset(alignMenuFrame)) {
                aligner.algorithmClass = newValue;
                reloadBeads(aligner);
            } else {
                alignPanel.algorithmComboBox.setSelectedItem(aligner.algorithmClass);
            }
        };
        alignPanel.algorithmComboBox.addActionListener(algorithmListener);
        alignPanel.algorithmComboBox.setRenderer(new EnumRenderer<>("ALIGNER_ENUM_ALGORITHM_CLASS_"));

        ActionListener calculatorListener = e -> {
            CalculatorType newValue = (CalculatorType) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.calculatorType && confirmReset(alignMenuFrame)) {
                aligner.calculatorType = newValue;
                reloadBeads(aligner);
            } else {
                alignPanel.calculatorComboBox.setSelectedItem(aligner.calculatorType);
            }
        };
        alignPanel.calculatorComboBox.addActionListener(calculatorListener);
        alignPanel.calculatorComboBox.setRenderer(new EnumRenderer<>("ALIGNER_ENUM_CALCULATOR_TYPE_"));

        ActionListener counterListener = e -> {
            CounterType newValue = (CounterType) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.counterType && confirmReset(alignMenuFrame)) {
                aligner.counterType = newValue;
                reloadBeads(aligner);
            } else {
                alignPanel.counterComboBox.setSelectedItem(aligner.counterType);
            }
        };
        alignPanel.counterComboBox.addActionListener(counterListener);
        alignPanel.counterComboBox.setRenderer(new EnumRenderer<>("ALIGNER_ENUM_COUNTER_TYPE_"));

        ActionListener segmentingListener = e -> {
            boolean newValue = ((AbstractButton) e.getSource()).isSelected();
            if (newValue != aligner.segment && confirmReset(alignMenuFrame)) {
                aligner.segment = newValue;
                reloadBeads(aligner);
            } else {
                alignPanel.segmentingCheckBox.setSelected(aligner.segment);
                alignMenuFrame.segmentingItem.setSelected(aligner.segment);
            }
        };
        alignPanel.segmentingCheckBox.addActionListener(segmentingListener);
        alignMenuFrame.segmentingItem.addActionListener(segmentingListener);

        ActionListener segmentingRulesListener = e -> {
            if (confirmReset(alignMenuFrame)) {
                Segmenter segmenter = aligner.getSegmenter();
                SegmentationCustomizer customizer = new SegmentationCustomizer(false, SRX.getDefault(),
                        segmenter != null ? segmenter.getSRX() : null, null);
                if (customizer.show(alignMenuFrame)) {
                    customizedSRX = customizer.getResult();
                    aligner.updateSegmenter(customizedSRX);
                    reloadBeads(aligner);
                }
            }
        };
        alignPanel.segmentingRulesButton.addActionListener(segmentingRulesListener);
        alignMenuFrame.segmentingRulesItem.addActionListener(segmentingRulesListener);

        ActionListener filterSettingsListener = e -> {
            if (confirmReset(alignMenuFrame)) {
                FiltersCustomizer customizer = new FiltersCustomizer(false,
                        FilterMaster.createDefaultFiltersConfig(), Core.getFilterMaster().getConfig(), null);
                if (customizer.show(alignMenuFrame)) {
                    customizedFilters = customizer.getResult();
                    Core.setFilterMaster(new FilterMaster(customizedFilters));
                    aligner.clearLoaded();
                    reloadBeads(aligner);
                }
            }
        };
        alignPanel.fileFilterSettingsButton.addActionListener(filterSettingsListener);
        alignMenuFrame.fileFilterSettingsItem.addActionListener(filterSettingsListener);

        TableCellRenderer renderer = new MultilineCellRenderer();
        alignPanel.table.setDefaultRenderer(Object.class, renderer);
        alignPanel.table.setDefaultRenderer(Boolean.class, renderer);
        alignPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeRows(alignPanel.table);
            }
        });

        ActionListener oneAdjustListener = e -> {
            int[] rows = alignPanel.table.getSelectedRows();
            int col = alignPanel.table.getSelectedColumn();
            boolean up = e.getSource().equals(alignPanel.moveUpButton)
                    || e.getSource().equals(alignMenuFrame.moveUpItem);
            BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
            if ((e.getModifiers() & Java8Compat.getMenuShortcutKeyMaskEx()) != 0) {
                int trgRow = up ? model.prevBeadFromRow(rows[0])
                        : model.nextBeadFromRow(rows[rows.length - 1]);
                moveRows(rows, col, trgRow);
            } else {
                int offset = up ? -1 : 1;
                slideRows(rows, col, offset);
            }
        };
        alignPanel.moveUpButton.addActionListener(oneAdjustListener);
        alignMenuFrame.moveUpItem.addActionListener(oneAdjustListener);
        alignPanel.moveDownButton.addActionListener(oneAdjustListener);
        alignMenuFrame.moveDownItem.addActionListener(oneAdjustListener);

        ActionListener mergeListener = e -> {
            int[] rows = alignPanel.table.getSelectedRows();
            int col = alignPanel.table.getSelectedColumn();
            BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
            if (rows.length == 1) {
                rows = new int[] { rows[0], model.nextNonEmptyCell(rows[0], col) };
            }
            int beads = model.beadsInRowSpan(rows);
            if (beads >= 1) {
                if (beads == 1) {
                    mergeRows(aligner, rows, col);
                } else {
                    moveRows(rows, col, rows[0]);
                }
            }
        };
        alignPanel.mergeButton.addActionListener(mergeListener);
        alignMenuFrame.mergeItem.addActionListener(mergeListener);

        ActionListener splitListener = e -> {
            int[] rows = alignPanel.table.getSelectedRows();
            int col = alignPanel.table.getSelectedColumn();
            BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
            int beads = model.beadsInRowSpan(rows);
            if (beads == 1) {
                if (rows.length == 1) {
                    splitRow(rows[0], col);
                } else {
                    splitBead(rows, col);
                }
            }
        };
        alignPanel.splitButton.addActionListener(splitListener);
        alignMenuFrame.splitItem.addActionListener(splitListener);

        ActionListener editListener = arg0 -> {
            int row = alignPanel.table.getSelectedRow();
            int col = alignPanel.table.getSelectedColumn();
            editRow(row, col);
        };
        alignPanel.editButton.addActionListener(editListener);
        alignMenuFrame.editItem.addActionListener(editListener);

        ListSelectionListener selectionListener = e -> updateCommandAvailability(alignPanel, alignMenuFrame);
        alignPanel.table.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);
        alignPanel.table.getSelectionModel().addListSelectionListener(selectionListener);

        ActionListener saveListener = e -> {
            if (!confirmSaveTMX(alignPanel)) {
                return;
            }
            while (true) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(defaultSaveDir, getOutFileName(aligner)));
                chooser.setDialogTitle(BUNDLE.getString("ALIGNER_PANEL_DIALOG_SAVE"));
                if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(alignMenuFrame)) {
                    File file = chooser.getSelectedFile();
                    if (file.isFile()) {
                        if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(alignMenuFrame,
                                StringUtil.format(BUNDLE.getString("ALIGNER_PANEL_DIALOG_OVERWRITE"),
                                        file.getName()),
                                BUNDLE.getString("ALIGNER_DIALOG_WARNING_TITLE"),
                                JOptionPane.WARNING_MESSAGE)) {
                            continue;
                        }
                    }
                    List<MutableBead> beads = ((BeadTableModel) alignPanel.table.getModel()).getData();
                    try {
                        if (aligner.srcLang != null && aligner.trgLang != null) {
                            aligner.writePairsToTMX(file,
                                    MutableBead.beadsToEntries(aligner.srcLang, aligner.trgLang, beads));
                            modified = false;
                        } else {
                            throw new IllegalArgumentException("srcLang and trgLang must not be null");
                        }
                    } catch (Exception ex) {
                        Log.log(ex);
                        JOptionPane.showMessageDialog(alignMenuFrame,
                                BUNDLE.getString("ALIGNER_PANEL_SAVE_ERROR"), BUNDLE.getString("ERROR_TITLE"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            }
        };
        alignPanel.saveButton.addActionListener(saveListener);
        alignMenuFrame.saveItem.addActionListener(saveListener);

        ActionListener resetListener = e -> {
            if (confirmReset(alignMenuFrame)) {
                if (phase == Phase.ALIGN) {
                    aligner.restoreDefaults();
                }
                reloadBeads(aligner);
            }
        };
        alignPanel.resetButton.addActionListener(resetListener);
        alignMenuFrame.resetItem.addActionListener(resetListener);

        ActionListener reloadListener = e -> {
            if (confirmReset(alignMenuFrame)) {
                aligner.clearLoaded();
                reloadBeads(aligner);
            }
        };
        alignMenuFrame.reloadItem.addActionListener(reloadListener);

        ActionListener removeTagsListener = e -> {
            boolean newValue = ((AbstractButton) e.getSource()).isSelected();
            if (newValue != aligner.removeTags && confirmReset(alignMenuFrame)) {
                aligner.removeTags = newValue;
                aligner.clearLoaded();
                reloadBeads(aligner);
            } else {
                alignPanel.removeTagsCheckBox.setSelected(aligner.removeTags);
                alignMenuFrame.removeTagsItem.setSelected(aligner.removeTags);
            }
        };
        alignPanel.removeTagsCheckBox.addActionListener(removeTagsListener);
        alignMenuFrame.removeTagsItem.addActionListener(removeTagsListener);

        alignPanel.continueButton.addActionListener(e -> {
            phase = Phase.EDIT;
            updatePanel(aligner);
        });

        ActionListener highlightListener = e -> {
            doHighlight = ((AbstractButton) e.getSource()).isSelected();
            updateHighlight();
        };
        alignPanel.highlightCheckBox.addActionListener(highlightListener);
        alignMenuFrame.highlightItem.addActionListener(highlightListener);

        ActionListener highlightPatternListener = e -> {
            PatternPanelController patternEditor = new PatternPanelController(highlightPattern);
            highlightPattern = patternEditor.show(alignMenuFrame);
            Preferences.setPreference(Preferences.ALIGNER_HIGHLIGHT_PATTERN, highlightPattern.pattern());
            updateHighlight();
        };
        alignPanel.highlightPatternButton.addActionListener(highlightPatternListener);
        alignMenuFrame.highlightPatternItem.addActionListener(highlightPatternListener);

        alignMenuFrame.markAcceptedItem
                .addActionListener(e -> setStatus(Status.ACCEPTED, alignPanel.table.getSelectedRows()));

        alignMenuFrame.markNeedsReviewItem
                .addActionListener(e -> setStatus(Status.NEEDS_REVIEW, alignPanel.table.getSelectedRows()));

        alignMenuFrame.clearMarkItem
                .addActionListener(e -> setStatus(Status.DEFAULT, alignPanel.table.getSelectedRows()));

        alignMenuFrame.toggleSelectedItem
                .addActionListener(e -> toggleEnabled(alignPanel.table.getSelectedRows()));

        alignMenuFrame.closeItem.addActionListener(e -> closeFrame(alignMenuFrame));

        alignMenuFrame.keepAllItem.addActionListener(e -> toggleAllEnabled(true));

        alignMenuFrame.keepNoneItem.addActionListener(e -> toggleAllEnabled(false));

        alignMenuFrame.realignPendingItem.addActionListener(e -> realignPending(aligner));

        alignMenuFrame.pinpointAlignStartItem.addActionListener(e -> {
            phase = Phase.PINPOINT;
            ppRow = alignPanel.table.getSelectedRow();
            ppCol = alignPanel.table.getSelectedColumn();
            alignPanel.table.clearSelection();
            updatePanel(aligner);
        });

        alignMenuFrame.pinpointAlignEndItem.addActionListener(e -> pinpointAlign(aligner,
                alignPanel.table.getSelectedRow(), alignPanel.table.getSelectedColumn()));

        alignMenuFrame.pinpointAlignCancelItem.addActionListener(e -> {
            phase = Phase.EDIT;
            ppRow = -1;
            ppCol = -1;
            alignPanel.table.repaint();
            updatePanel(aligner);
        });

        alignPanel.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (phase == Phase.PINPOINT) {
                    JTable table = (JTable) e.getSource();
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    pinpointAlign(aligner, row, col);
                }
            }
        });

        alignMenuFrame.resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        alignMenuFrame.realignPendingItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        alignMenuFrame.saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        alignMenuFrame.closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        // emacs-like keys for table navigation
        // See javax.swing.plaf.BasicTableUI.Actions for supported action names.
        setKeyboardShortcut(alignPanel.table, "selectNextRow", 'n');
        setKeyboardShortcut(alignPanel.table, "selectNextRowExtendSelection", 'N');
        setKeyboardShortcut(alignPanel.table, "selectPreviousRow", 'p');
        setKeyboardShortcut(alignPanel.table, "selectPreviousRowExtendSelection", 'P');
        setKeyboardShortcut(alignPanel.table, "selectNextColumn", 'f');
        setKeyboardShortcut(alignPanel.table, "selectNextColumnExtendSelection", 'F');
        setKeyboardShortcut(alignPanel.table, "selectPreviousColumn", 'b');
        setKeyboardShortcut(alignPanel.table, "selectPreviousColumnExtendSelection", 'B');

        alignPanel.table.setTransferHandler(new AlignTransferHandler());
        alignPanel.table.addPropertyChangeListener("dropLocation", new DropLocationListener());
        alignPanel.table.setFont(Core.getMainWindow().getApplicationFont());
        CoreEvents.registerFontChangedEventListener(alignPanel.table::setFont);

        // Set initial state
        updateHighlight();
        updatePanel(aligner);
        reloadBeads(aligner);

        alignMenuFrame.add(alignPanel);
        alignMenuFrame.pack();
        alignMenuFrame.setMinimumSize(alignMenuFrame.getSize());
        alignMenuFrame.setLocationRelativeTo(parent);
        alignMenuFrame.setVisible(true);
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
        Rectangle initialRect = alignPanel.table.getVisibleRect();
        alignPanel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
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
        alignPanel.table.changeSelection(selStart, col, false, false);
        alignPanel.table.changeSelection(selEnd, col, false, true);
        ensureSelectionVisible(initialRect);
    }

    private void moveRows(int[] rows, int col, int trgRow) {
        modified = true;
        Rectangle initialRect = alignPanel.table.getVisibleRect();
        alignPanel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
        List<Integer> realRows = model.realCellsInRowSpan(col, rows);
        int[] resultRows = model.move(realRows, col, trgRow);
        alignPanel.table.changeSelection(resultRows[0], col, false, false);
        alignPanel.table.changeSelection(resultRows[1], col, false, true);
        ensureSelectionVisible(initialRect);
    }

    private void mergeRows(Aligner aligner, int[] rows, int col) {
        modified = true;
        Rectangle initialRect = alignPanel.table.getVisibleRect();
        alignPanel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
        List<Integer> realRows = model.realCellsInRowSpan(col, rows);
        int resultRow = model.mergeRows(aligner, realRows, col);
        alignPanel.table.changeSelection(resultRow, col, false, false);
        ensureSelectionVisible(initialRect);
    }

    private void splitRow(int row, int col) {
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
        if (!model.isEditableColumn(col)) {
            throw new IllegalArgumentException();
        }
        String text = alignPanel.table.getValueAt(row, col).toString();
        String reference = (String) alignPanel.table.getValueAt(row,
                col == BeadTableModel.COL_SRC ? BeadTableModel.COL_TRG : BeadTableModel.COL_SRC);
        SplittingPanelController splitter = new SplittingPanelController(text, reference);
        String[] split = splitter.show(SwingUtilities.getWindowAncestor(alignPanel.table));
        if (split.length == 1) {
            return;
        }
        modified = true;
        Rectangle initialRect = alignPanel.table.getVisibleRect();
        alignPanel.table.clearSelection();
        int[] resultRows = model.splitRow(row, col, split);
        alignPanel.table.changeSelection(resultRows[0], col, false, false);
        alignPanel.table.changeSelection(resultRows[resultRows.length - 1], col, false, true);
        ensureSelectionVisible(initialRect);
    }

    private void splitBead(int[] rows, int col) {
        modified = true;
        alignPanel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
        Rectangle initialRect = alignPanel.table.getVisibleRect();
        model.splitBead(rows);
        alignPanel.table.changeSelection(rows[0], col, false, false);
        alignPanel.table.changeSelection(rows[rows.length - 1], col, false, true);
        ensureSelectionVisible(initialRect);
    }

    private void editRow(int row, int col) {
        String text = alignPanel.table.getValueAt(row, col).toString();
        EditingPanelController splitter = new EditingPanelController(text);
        String newText = splitter.show(SwingUtilities.getWindowAncestor(alignPanel.table));
        if (newText == null || text.equals(newText)) {
            return;
        }
        modified = true;
        Rectangle initialRect = alignPanel.table.getVisibleRect();
        alignPanel.table.clearSelection();
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
        model.editRow(row, col, newText);
        alignPanel.table.changeSelection(row, col, false, false);
        ensureSelectionVisible(initialRect);
    }

    private void realignPending(Aligner aligner) {
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
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
        alignPanel.table.repaint();
        resizeRows(alignPanel.table);
    }

    private void pinpointAlign(Aligner aligner, int row, int col) {
        if (row == ppRow || col == ppCol) {
            return;
        }
        modified = true;
        Rectangle initialRect = alignPanel.table.getVisibleRect();
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
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
            toRelocate.add(line);
        }
        int resultRow = model.insertLines(toRelocate, Math.max(ppRow, row), relocateCol);
        model.setStatusAtRow(resultRow, Status.ACCEPTED);
        alignPanel.table.changeSelection(resultRow, ppCol, false, false);
        alignPanel.table.changeSelection(resultRow, col, false, true);
        ppRow = -1;
        ppCol = -1;
        phase = Phase.EDIT;
        ensureSelectionVisible(initialRect);
        updatePanel(aligner);
    }

    private void toggleEnabled(int... rows) {
        if (rows.length == 0) {
            return;
        }
        modified = true;
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
        model.toggleBeadsAtRows(rows);
        alignPanel.table.repaint();
    }

    private void toggleAllEnabled(boolean value) {
        modified = true;
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
        model.toggleAllBeads(value);
        alignPanel.table.repaint();
    }

    private void setStatus(MutableBead.Status status, int... rows) {
        if (rows.length == 0) {
            return;
        }
        modified = true;
        BeadTableModel model = (BeadTableModel) alignPanel.table.getModel();
        for (int row : rows) {
            model.setStatusAtRow(row, status);
        }
        int nextBeadRow = model.nextBeadFromRow(rows[rows.length - 1]);
        if (nextBeadRow != -1) {
            int[] cols = alignPanel.table.getSelectedColumns();
            alignPanel.table.changeSelection(nextBeadRow, cols[0], false, false);
            alignPanel.table.changeSelection(nextBeadRow, cols[cols.length - 1], false, true);
            ensureSelectionVisible(alignPanel.table.getVisibleRect());
        }
    }

    private void ensureSelectionVisible(Rectangle initialView) {
        alignPanel.table.repaint();
        resizeRows(alignPanel.table);
        int[] rows = alignPanel.table.getSelectedRows();
        int[] cols = alignPanel.table.getSelectedColumns();
        Rectangle selectionRect = alignPanel.table.getCellRect(rows[0], cols[0], true)
                .union(alignPanel.table.getCellRect(rows[rows.length - 1], cols[cols.length - 1], true));
        alignPanel.table.scrollRectToVisible(initialView);
        alignPanel.table.scrollRectToVisible(selectionRect);
    }

    private boolean confirmReset(Component comp) {
        if (!modified) {
            return true;
        }
        return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(comp,
                BUNDLE.getString("ALIGNER_PANEL_RESET_WARNING_MESSAGE"),
                BUNDLE.getString("ALIGNER_DIALOG_WARNING_TITLE"), JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Reloads the beads with the current settings. The loading itself takes
     * place on a background thread.
     */
    private void reloadBeads(Aligner aligner) {
        if (loader != null) {
            loader.cancel(true);
        }
        phase = Phase.ALIGN;
        alignPanel.progressBar.setVisible(true);
        alignPanel.continueButton.setEnabled(false);
        alignPanel.controlsPanel.setVisible(false);
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
                    JOptionPane.showMessageDialog(alignPanel, BUNDLE.getString("ALIGNER_ERROR_LOADING"),
                            BUNDLE.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                }
                alignPanel.continueButton.setEnabled(true);
                alignPanel.progressBar.setVisible(false);
                List<ComparisonMode> modes = aligner.allowedModes;
                alignPanel.comparisonComboBox.setModel(
                        new DefaultComboBoxModel<>(modes != null ? modes.toArray(new ComparisonMode[0]) : new ComparisonMode[0]));

                String distanceValue;
                if (beads != null) {
                    double avgDist = MutableBead.calculateAvgDist(beads);
                    distanceValue = StringUtil.format(BUNDLE.getString("ALIGNER_PANEL_LABEL_AVGSCORE"),
                            Double.compare(avgDist, 100.0) > 0 ? "-" : String.format("%.3f", avgDist));
                    alignPanel.table.setModel(new BeadTableModel(beads));
                    for (int i = 0; i < BeadTableModel.COL_SRC; i++) {
                        TableColumn col = alignPanel.table.getColumnModel().getColumn(i);
                        col.setMaxWidth(col.getWidth());
                    }
                    modified = false;
                } else {
                    distanceValue = StringUtil.format(BUNDLE.getString("ALIGNER_PANEL_LABEL_AVGSCORE"), "-");
                }
                alignPanel.averageDistanceLabel.setText(distanceValue);

                updatePanel(aligner);
            }
        };
        loader.execute();
    }

    /**
     * Ensure that the panel controls and available menu items are synced with
     * the settings of the underlying aligner.
     */
    private void updatePanel(Aligner aligner) {
        alignPanel.comparisonComboBox.setSelectedItem(aligner.comparisonMode);
        alignPanel.algorithmComboBox.setSelectedItem(aligner.algorithmClass);
        alignPanel.calculatorComboBox.setSelectedItem(aligner.calculatorType);
        alignPanel.counterComboBox.setSelectedItem(aligner.counterType);
        alignPanel.segmentingCheckBox.setSelected(aligner.segment);
        alignMenuFrame.segmentingItem.setSelected(aligner.segment);
        alignPanel.segmentingRulesButton.setEnabled(aligner.segment);
        alignMenuFrame.segmentingRulesItem.setEnabled(aligner.segment);
        alignPanel.removeTagsCheckBox.setSelected(aligner.removeTags);
        alignMenuFrame.removeTagsItem.setSelected(aligner.removeTags);

        alignPanel.advancedPanel.setVisible(phase == Phase.ALIGN);
        alignPanel.segmentationControlsPanel.setVisible(phase == Phase.ALIGN);
        alignPanel.filteringControlsPanel.setVisible(phase == Phase.ALIGN);
        alignPanel.continueButton.setVisible(phase == Phase.ALIGN);
        alignPanel.controlsPanel.setVisible(phase != Phase.ALIGN);
        alignPanel.controlsPanel.setEnabled(phase == Phase.EDIT);
        alignPanel.saveButton.setVisible(phase != Phase.ALIGN);
        alignPanel.saveButton.setEnabled(phase == Phase.EDIT);
        String instructions = BUNDLE.getString("ALIGNER_PANEL_" + phase.key + "_PHASE_HELP");
        alignPanel.instructionsLabel.setText(instructions);
        alignMenuFrame.editMenu.setEnabled(phase != Phase.ALIGN);
        for (Component c : alignMenuFrame.editMenu.getComponents()) {
            // Batch-enable/disable Edit menu items here, then override later if
            // necessary
            c.setEnabled(phase == Phase.EDIT);
        }
        alignMenuFrame.optionsMenu.setEnabled(phase == Phase.ALIGN);
        alignMenuFrame.saveItem.setEnabled(phase == Phase.EDIT);

        alignPanel.table.setCursor(Cursor.getPredefinedCursor(
                phase == Phase.PINPOINT ? Cursor.CROSSHAIR_CURSOR : Cursor.DEFAULT_CURSOR));
        alignMenuFrame.pinpointAlignStartItem.setVisible(phase != Phase.PINPOINT);
        alignMenuFrame.pinpointAlignEndItem.setVisible(phase == Phase.PINPOINT);
        // frame.pinpointAlign[Start|End]Item enabledness depends on table
        // selection
        alignMenuFrame.pinpointAlignCancelItem.setVisible(phase == Phase.PINPOINT);
        alignMenuFrame.pinpointAlignCancelItem.setEnabled(phase == Phase.PINPOINT);

        JButton defaultButton = phase == Phase.ALIGN ? alignPanel.continueButton
                : phase == Phase.EDIT ? alignPanel.saveButton : null;
        alignMenuFrame.getRootPane().setDefaultButton(defaultButton);

        updateCommandAvailability(alignPanel, alignMenuFrame);

        SwingUtilities.invokeLater(() -> resizeRows(alignPanel.table));
    }

    private void updateHighlight() {
        alignPanel.highlightCheckBox.setSelected(doHighlight);
        alignMenuFrame.highlightItem.setSelected(doHighlight);
        alignPanel.highlightPatternButton.setEnabled(doHighlight);
        alignMenuFrame.highlightPatternItem.setEnabled(doHighlight);
        alignPanel.table.repaint();
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

    private String getOutFileName(Aligner aligner) {
        String src = FilenameUtils.getBaseName(aligner.srcFile);
        String trg = FilenameUtils.getBaseName(aligner.trgFile);
        if (src.equals(trg) && aligner.srcLang != null && aligner.trgLang != null) {
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
        Core.getMainWindow();
        if (customizedSRX == null) {
            return;
        }
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(comp,
                BUNDLE.getString("ALIGNER_DIALOG_SEGMENTATION_CONFIRM_MESSAGE"),
                BUNDLE.getString("ALIGNER_DIALOG_CONFIRM_TITLE"), JOptionPane.OK_CANCEL_OPTION)) {
            if (Core.getProject().isProjectLoaded()
                    && Core.getProject().getProjectProperties().getProjectSRX() != null) {
                Core.getProject().getProjectProperties().setProjectSRX(customizedSRX);
                try {
                    Core.getProject().saveProjectProperties();
                } catch (Exception ex) {
                    Log.log(ex);
                    JOptionPane.showMessageDialog(comp, BUNDLE.getString("CT_ERROR_SAVING_PROJ"),
                            BUNDLE.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
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
        Core.getMainWindow();
        if (customizedFilters == null) {
            return;
        }
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(comp,
                BUNDLE.getString("ALIGNER_DIALOG_FILTERS_CONFIRM_MESSAGE"),
                BUNDLE.getString("ALIGNER_DIALOG_CONFIRM_TITLE"), JOptionPane.OK_CANCEL_OPTION)) {
            if (Core.getProject().isProjectLoaded()
                    && Core.getProject().getProjectProperties().getProjectFilters() != null) {
                Core.getProject().getProjectProperties().setProjectFilters(customizedFilters);
                try {
                    Core.getProject().saveProjectProperties();
                } catch (Exception ex) {
                    Log.log(ex);
                    JOptionPane.showMessageDialog(comp, BUNDLE.getString("CT_ERROR_SAVING_PROJ"),
                            BUNDLE.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
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
                    BUNDLE.getString("ALIGNER_DIALOG_NEEDSREVIEW_CONFIRM_MESSAGE"),
                    BUNDLE.getString("ALIGNER_DIALOG_CONFIRM_TITLE"), JOptionPane.OK_CANCEL_OPTION);
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
                String text = value.toString();
                textArea.setText(text);
                doHighlighting(text);
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
                    default:
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
            if (!doHighlight) {
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
            List<Float> aRowToDistance = new ArrayList<>();
            List<MutableBead> aRowToBead = new ArrayList<>();
            List<String> aRowToSourceLine = new ArrayList<>();
            List<String> aRowToTargetLine = new ArrayList<>();
            for (MutableBead bead : data) {
                int beadRows = Math.max(bead.sourceLines.size(), bead.targetLines.size());
                for (int i = 0; i < beadRows; i++) {
                    aRowToDistance.add(bead.score);
                    aRowToBead.add(bead);
                    aRowToSourceLine.add(i < bead.sourceLines.size() ? bead.sourceLines.get(i) : null);
                    aRowToTargetLine.add(i < bead.targetLines.size() ? bead.targetLines.get(i) : null);
                }
            }
            this.rowToDistance = aRowToDistance;
            this.rowToBead = aRowToBead;
            this.rowToSourceLine = aRowToSourceLine;
            this.rowToTargetLine = aRowToTargetLine;
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
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case COL_CHECKBOX - 3:
                return BUNDLE.getString("ALIGNER_PANEL_TABLE_COL_ROW");
            case COL_CHECKBOX - 2:
                return BUNDLE.getString("ALIGNER_PANEL_TABLE_COL_DISTANCE");
            case COL_CHECKBOX - 1:
                // Bead number
                return "";
            case COL_CHECKBOX:
                return BUNDLE.getString("ALIGNER_PANEL_TABLE_COL_KEEP");
            case COL_SRC:
                return BUNDLE.getString("ALIGNER_PANEL_TABLE_COL_SOURCE");
            case COL_TRG:
                return BUNDLE.getString("ALIGNER_PANEL_TABLE_COL_TARGET");
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public @Nullable Object getValueAt(int row, int column) {
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
            default:
                throw new IllegalArgumentException();
            }
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
                selected.add(line);
                MutableBead bead = rowToBead.get(row);
                if (bead == trgBead) {
                    continue;
                }
                if (Util.removeByIdentity(col == COL_SRC ? bead.sourceLines : bead.targetLines, line)) {
                    int insertIndex = trgRow > row ? 0 : trgLines.size();
                    trgLines.add(insertIndex, line);
                }
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
         * @param bead the bead to be split. It is modified in place to contain only
         *             the first 'count' lines of its source and target.
         * @param count the number of lines to retain in the original bead's source
         *              and target lines.
         * @return a new MutableBead object containing the remaining source and target
         *         lines after the split.
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
         * @param up
         *            Up (toward index=0) when true, down when false
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
                return false;
            }
            return true;
        }

        List<MutableBead> getData() {
            return Collections.unmodifiableList(data);
        }

        /**
         * Get a list of rows covered by the bead at <code>row</code>.
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
            List<Integer> result = new ArrayList<>();
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
        int mergeRows(Aligner aligner, List<Integer> rows, int col) {
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
                MutableBead bead = rowToBead.get(row);
                Util.removeByIdentity(col == COL_SRC ? bead.sourceLines : bead.targetLines, line);
                bead.status = Status.DEFAULT;
            }
            MutableBead trgBead = rowToBead.get(rows.get(0));
            List<String> trgLines = col == COL_SRC ? trgBead.sourceLines : trgBead.targetLines;
            Language lang = col == COL_SRC ? aligner.srcLang : aligner.trgLang;
            String combined = lang != null ? Util.join(lang, toCombine) : null;
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
            trgLines.set(insertAt++, split[0]);
            for (int i = 1; i < split.length; i++) {
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
         * @param row Row index of the cell to edit
         * @param col Column index of the cell to edit
         * @param newVal New text value to replace the existing content
         */
        void editRow(int row, int col, String newVal) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            MutableBead trgBead = rowToBead.get(row);
            List<String> trgLines = (col == COL_SRC ? trgBead.sourceLines : trgBead.targetLines);
            String line = (col == COL_SRC ? rowToSourceLine : rowToTargetLine).get(row);
            int insertAt = Util.indexByIdentity(trgLines, line);
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
                    // Already in target bead
                    continue;
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
         */
        int beadsInRowSpan(int... rows) {
            List<MutableBead> beads = new ArrayList<>();
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
         * @param rows  rows
         */
        void splitBead(int[] rows) {
            int origRowCount = getRowCount();
            MutableBead bead = rowToBead.get(rows[0]);
            int beadIndex = data.indexOf(bead);
            for (int row : rows) {
                String line = rowToSourceLine.get(row);
                List<String> indexFrom = bead.sourceLines;
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
            List<MutableBead> beads = new ArrayList<>(rows.length);
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
                return i;
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
        protected @Nullable Transferable createTransferable(JComponent c) {
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
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
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
        protected String getDisplayText(@Nullable T value) {
            if (value == null) {
                return "";
            }
            try {
                return BUNDLE.getString(keyPrefix + value.name());
            } catch (MissingResourceException ex) {
                return value.name();
            }
        }
    }
}
