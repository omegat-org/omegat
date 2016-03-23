/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.align;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.align.Aligner.AlgorithmClass;
import org.omegat.gui.align.Aligner.CalculatorType;
import org.omegat.gui.align.Aligner.ComparisonMode;
import org.omegat.gui.align.Aligner.CounterType;
import org.omegat.gui.filters2.FiltersCustomizer;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.segmentation.SegmentationCustomizer;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.RoundedCornerBorder;
import org.omegat.util.gui.Styles;

import gen.core.filters.Filters;

/**
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

    private enum Phase {
        ALIGN, EDIT
    }

    private Phase phase = Phase.ALIGN;

    public AlignPanelController(Aligner aligner, String defaultSaveDir) {
        this.aligner = aligner;
        this.defaultSaveDir = defaultSaveDir;
    }

    public void show(Component parent) {
        final AlignMenuFrame frame = new AlignMenuFrame();
        frame.setTitle(OStrings.getString("ALIGNER_PANEL"));
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeFrame(frame);
            }
        });

        final AlignPanel panel = new AlignPanel();

        ActionListener comparisonListener = e -> {
            ComparisonMode newValue = (ComparisonMode) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.comparisonMode && confirmReset(frame)) {
                aligner.comparisonMode = newValue;
                reloadBeads(panel, frame);
            } else {
                panel.comparisonComboBox.setSelectedItem(aligner.comparisonMode);
            }
        };
        panel.comparisonComboBox.addActionListener(comparisonListener);

        ActionListener algorithmListener = e -> {
            AlgorithmClass newValue = (AlgorithmClass) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.algorithmClass && confirmReset(frame)) {
                aligner.algorithmClass = newValue;
                reloadBeads(panel, frame);
            } else {
                panel.algorithmComboBox.setSelectedItem(aligner.algorithmClass);
            }
        };
        panel.algorithmComboBox.addActionListener(algorithmListener);

        ActionListener calculatorListener = e -> {
            CalculatorType newValue = (CalculatorType) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.calculatorType && confirmReset(frame)) {
                aligner.calculatorType = newValue;
                reloadBeads(panel, frame);
            } else {
                panel.calculatorComboBox.setSelectedItem(aligner.calculatorType);
            }
        };
        panel.calculatorComboBox.addActionListener(calculatorListener);

        ActionListener counterListener = e -> {
            CounterType newValue = (CounterType) ((JComboBox<?>) e.getSource()).getSelectedItem();
            if (newValue != aligner.counterType && confirmReset(frame)) {
                aligner.counterType = newValue;
                reloadBeads(panel, frame);
            } else {
                panel.counterComboBox.setSelectedItem(aligner.counterType);
            }
        };
        panel.counterComboBox.addActionListener(counterListener);

        ActionListener segmentingListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newValue = ((AbstractButton) e.getSource()).isSelected();
                if (newValue != aligner.segment && confirmReset(frame)) {
                    aligner.segment = newValue;
                    reloadBeads(panel, frame);
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
                    SegmentationCustomizer customizer = new SegmentationCustomizer(frame, false, SRX.getDefault(),
                            Core.getSegmenter().getSRX(), null);
                    customizer.setVisible(true);
                    if (customizer.getReturnStatus() == SegmentationCustomizer.RET_OK) {
                        customizedSRX = customizer.getSRX();
                        Core.setSegmenter(new Segmenter(customizedSRX));
                        reloadBeads(panel, frame);
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
                    FiltersCustomizer customizer = new FiltersCustomizer(frame, false,
                            FilterMaster.createDefaultFiltersConfig(), Core.getFilterMaster().getConfig(), null);
                    customizer.setVisible(true);
                    if (customizer.getReturnStatus() == SegmentationCustomizer.RET_OK) {
                        customizedFilters = customizer.result;
                        Core.setFilterMaster(new FilterMaster(customizedFilters));
                        aligner.clearLoaded();
                        reloadBeads(panel, frame);
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
                boolean up = e.getSource().equals(panel.moveUpButton) || e.getSource().equals(frame.moveUpItem);
                BeadTableModel model = (BeadTableModel) panel.table.getModel();
                if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                    int trgRow = up ? model.prevBeadFromRow(rows[0])
                            : model.nextBeadFromRow(rows[rows.length - 1]);
                    moveRows(panel.table, rows, col, trgRow);
                } else {
                    int offset = up ? -1 : 1;
                    slideRows(panel.table, rows, col, offset);
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
                if (beads < 1) {
                    // Do nothing
                } else if (beads == 1) {
                    mergeRows(panel.table, rows, col);
                } else {
                    moveRows(panel.table, rows, col, rows[0]);
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
                if (beads != 1) {
                    // Do nothing
                } else if (rows.length == 1) {
                    splitRow(panel.table, rows[0], col);
                } else {
                    splitBead(panel.table, rows, col);
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
                editRow(panel.table, row, col);
            }
        };
        panel.editButton.addActionListener(editListener);
        frame.editItem.addActionListener(editListener);

        ListSelectionListener selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] rows = panel.table.getSelectedRows();
                int col = panel.table.getSelectedColumn();
                BeadTableModel model = (BeadTableModel) panel.table.getModel();
                List<Integer> realRows = model.realCellsInRowSpan(col, rows);
                boolean enabled = !realRows.isEmpty() && panel.table.getSelectedColumnCount() == 1
                        && model.isEditableColumn(col);
                boolean canUp = enabled ? model.canMove(realRows.get(0), col, true) : false;
                boolean canDown = enabled ? model.canMove(realRows.get(realRows.size() - 1), col, false) : false;
                int beads = model.beadsInRowSpan(rows);
                boolean canSplit = (realRows.size() == 1 && rows.length == 1) || (realRows.size() > 1 && beads == 1);
                boolean canMerge = realRows.size() > 1
                        || (!realRows.isEmpty() && realRows.get(0) < panel.table.getRowCount() - 1);
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
                                    OStrings.getString("ALIGNER_DIALOG_WARNING_TITLE"), JOptionPane.WARNING_MESSAGE)) {
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
                            JOptionPane.showMessageDialog(frame, OStrings.getString("ALIGNER_PANEL_SAVE_ERROR"),
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
                    reloadBeads(panel, frame);
                }
            }
        };
        panel.resetButton.addActionListener(resetListener);
        frame.resetItem.addActionListener(resetListener);

        ActionListener removeTagsListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newValue = ((AbstractButton) e.getSource()).isSelected();
                if (newValue != aligner.removeTags && confirmReset(frame)) {
                    aligner.removeTags = newValue;
                    aligner.clearLoaded();
                    reloadBeads(panel, frame);
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
                updatePanel(panel, frame);
            }
        });

        ActionListener highlightListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doHighlight = ((AbstractButton) e.getSource()).isSelected();
                updateHighlight(panel, frame);
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
                updateHighlight(panel, frame);
            }
        };
        panel.highlightPatternButton.addActionListener(highlightPatternListener);
        frame.highlightPatternItem.addActionListener(highlightPatternListener);

        frame.markAcceptedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStatus(panel.table, MutableBead.Status.ACCEPTED, panel.table.getSelectedRows());
            }
        });

        frame.markNeedsReviewItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStatus(panel.table, MutableBead.Status.NEEDS_REVIEW, panel.table.getSelectedRows());
            }
        });

        frame.clearMarkItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStatus(panel.table, MutableBead.Status.DEFAULT, panel.table.getSelectedRows());
            }
        });

        frame.toggleSelectedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleEnabled(panel.table, panel.table.getSelectedRows());
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
                toggleAllEnabled(panel.table, true);
            }
        });

        frame.keepNoneItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleAllEnabled(panel.table, false);
            }
        });

        frame.resetItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        frame.saveItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        frame.closeItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

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
        if (Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
            try {
                String fontName = Preferences.getPreference(OConsts.TF_SRC_FONT_NAME);
                int fontSize = Integer.parseInt(Preferences.getPreference(OConsts.TF_SRC_FONT_SIZE));
                panel.table.setFont(new Font(fontName, Font.PLAIN, fontSize));
            } catch (Exception e) {
                Log.log(e);
            }
        }

        // Set initial state
        updateHighlight(panel, frame);
        reloadBeads(panel, frame);

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

    private void slideRows(JTable table, int[] rows, int col, int offset) {
        modified = true;
        Rectangle initialRect = table.getVisibleRect();
        table.clearSelection();
        BeadTableModel model = (BeadTableModel) table.getModel();
        List<Integer> realRows = model.realCellsInRowSpan(col, rows);
        int[] resultRows = model.slide(realRows, col, offset);
        table.changeSelection(resultRows[0], col, false, false);
        table.changeSelection(resultRows[1], col, false, true);
        ensureSelectionVisible(table, initialRect);
    }

    private void moveRows(JTable table, int[] rows, int col, int trgRow) {
        modified = true;
        Rectangle initialRect = table.getVisibleRect();
        table.clearSelection();
        BeadTableModel model = (BeadTableModel) table.getModel();
        List<Integer> realRows = model.realCellsInRowSpan(col, rows);
        int[] resultRows = model.move(realRows, col, trgRow);
        table.changeSelection(resultRows[0], col, false, false);
        table.changeSelection(resultRows[1], col, false, true);
        ensureSelectionVisible(table, initialRect);
    }

    private void mergeRows(JTable table, int[] rows, int col) {
        modified = true;
        Rectangle initialRect = table.getVisibleRect();
        table.clearSelection();
        BeadTableModel model = (BeadTableModel) table.getModel();
        List<Integer> realRows = model.realCellsInRowSpan(col, rows);
        int resultRow = model.mergeRows(realRows, col);
        table.changeSelection(resultRow, col, false, false);
        ensureSelectionVisible(table, initialRect);
    }

    private void splitRow(JTable table, int row, int col) {
        BeadTableModel model = (BeadTableModel) table.getModel();
        if (!model.isEditableColumn(col)) {
            throw new IllegalArgumentException();
        }
        String text = table.getValueAt(row, col).toString();
        String reference = table.getValueAt(row,
                col == BeadTableModel.COL_SRC ? BeadTableModel.COL_TRG : BeadTableModel.COL_SRC).toString();
        SplittingPanelController splitter = new SplittingPanelController(text, reference);
        String[] split = splitter.show(SwingUtilities.getWindowAncestor(table));
        if (split.length == 1) {
            return;
        }
        modified = true;
        Rectangle initialRect = table.getVisibleRect();
        table.clearSelection();
        int resultRows[] = model.splitRow(row, col, split);
        table.changeSelection(resultRows[0], col, false, false);
        table.changeSelection(resultRows[resultRows.length - 1], col, false, true);
        ensureSelectionVisible(table, initialRect);
    }

    private void splitBead(JTable table, int[] rows, int col) {
        modified = true;
        table.clearSelection();
        BeadTableModel model = (BeadTableModel) table.getModel();
        Rectangle initialRect = table.getVisibleRect();
        int resultRows[] = model.splitBead(rows, col);
        table.changeSelection(resultRows[0], col, false, false);
        table.changeSelection(resultRows[resultRows.length - 1], col, false, true);
        ensureSelectionVisible(table, initialRect);
    }

    private void editRow(JTable table, int row, int col) {
        String text = table.getValueAt(row, col).toString();
        EditingPanelController splitter = new EditingPanelController(text);
        String newText = splitter.show(SwingUtilities.getWindowAncestor(table));
        if (newText == null || text.equals(newText)) {
            return;
        }
        modified = true;
        Rectangle initialRect = table.getVisibleRect();
        table.clearSelection();
        BeadTableModel model = (BeadTableModel) table.getModel();
        model.editRow(row, col, newText);
        table.changeSelection(row, col, false, false);
        ensureSelectionVisible(table, initialRect);
    }

    private void toggleEnabled(JTable table, int... rows) {
        if (rows.length == 0) {
            return;
        }
        modified = true;
        BeadTableModel model = (BeadTableModel) table.getModel();
        model.toggleBeadsAtRows(rows);
        table.repaint();
    }

    private void toggleAllEnabled(JTable table, boolean value) {
        modified = true;
        BeadTableModel model = (BeadTableModel) table.getModel();
        model.toggleAllBeads(value);
        table.repaint();
    }

    private void setStatus(JTable table, MutableBead.Status status, int... rows) {
        if (rows.length == 0) {
            return;
        }
        modified = true;
        BeadTableModel model = (BeadTableModel) table.getModel();
        for (int row : rows) {
            model.setStatusAtRow(row, status);
        }
        int nextBeadRow = model.nextBeadFromRow(rows[rows.length - 1]);
        table.changeSelection(nextBeadRow, table.getSelectedColumn(), false, false);
        ensureSelectionVisible(table, table.getVisibleRect());
    }

    private void ensureSelectionVisible(JTable table, Rectangle initialView) {
        table.repaint();
        resizeRows(table);
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        Rectangle selectionRect = table.getCellRect(rows[0], cols[0], true)
                .union(table.getCellRect(rows[rows.length - 1], cols[cols.length - 1], true));
        table.scrollRectToVisible(initialView);
        table.scrollRectToVisible(selectionRect);
    }

    private boolean confirmReset(Component comp) {
        if (!modified) {
            return true;
        }
        return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(comp,
                OStrings.getString("ALIGNER_PANEL_RESET_WARNING_MESSAGE"),
                OStrings.getString("ALIGNER_DIALOG_WARNING_TITLE"), JOptionPane.OK_CANCEL_OPTION);
    }

    private void reloadBeads(final AlignPanel panel, final AlignMenuFrame frame) {
        if (loader != null) {
            loader.cancel(true);
        }
        phase = Phase.ALIGN;
        panel.progressBar.setVisible(true);
        loader = new SwingWorker<List<MutableBead>, Object>() {
            @Override
            protected List<MutableBead> doInBackground() throws Exception {
                return aligner.alignImpl().map(MutableBead::new).collect(Collectors.toList());
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

                updatePanel(panel, frame);
            }
        };
        loader.execute();
    }

    private void updatePanel(final AlignPanel panel, AlignMenuFrame frame) {
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
        panel.controlsPanel.setVisible(phase == Phase.EDIT);
        panel.saveButton.setVisible(phase == Phase.EDIT);
        String instructions = phase == Phase.ALIGN ? OStrings.getString("ALIGNER_PANEL_ALIGN_PHASE_HELP")
                : phase == Phase.EDIT ? OStrings.getString("ALIGNER_PANEL_EDIT_PHASE_HELP") : null;
        panel.instructionsLabel.setText(instructions);
        frame.editMenu.setEnabled(phase == Phase.EDIT);
        frame.optionsMenu.setEnabled(phase == Phase.ALIGN);
        frame.saveItem.setEnabled(phase == Phase.EDIT);

        JButton defaultButton = phase == Phase.ALIGN ? panel.continueButton
                : phase == Phase.EDIT ? panel.saveButton : null;
        frame.getRootPane().setDefaultButton(defaultButton);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                resizeRows(panel.table);
            }
        });
    }

    private void updateHighlight(AlignPanel panel, AlignMenuFrame frame) {
        panel.highlightCheckBox.setSelected(doHighlight);
        frame.highlightItem.setSelected(doHighlight);
        panel.highlightPatternButton.setEnabled(doHighlight);
        frame.highlightPatternItem.setEnabled(doHighlight);
        panel.table.repaint();
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

    private void confirmSaveFilters(Component comp) {
        if (Core.getMainWindow() == null || customizedFilters == null) {
            return;
        }
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(comp,
                OStrings.getString("ALIGNER_DIALOG_FILTERS_CONFIRM_MESSAGE"),
                OStrings.getString("ALIGNER_DIALOG_CONFIRM_TITLE"),
                JOptionPane.OK_CANCEL_OPTION)) {
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

        public MultilineCellRenderer() {
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
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
                } else {
                    comp.setBackground(
                            getBeadNumber(table, row) % 2 == 0 ? table.getBackground()
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
        static final int COL_CHECKBOX = 0;
        static final int COL_SRC = COL_CHECKBOX + 1;
        static final int COL_TRG = COL_SRC + 1;

        final List<MutableBead> data;

        List<Float> rowToDistance;
        List<MutableBead> rowToBead;
        List<String> rowToSourceLine;
        List<String> rowToTargetLine;

        public BeadTableModel(List<MutableBead> data) {
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

        int[] move(List<Integer> rows, int col, int trgRow) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            Collections.sort(rows);
            List<String> selected = new ArrayList<>(rows.size());
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            int origRowCount = getRowCount();
            MutableBead trgBead;
            if (trgRow < 0) {
                trgBead = new MutableBead();
                data.add(0, trgBead);
            } else if (trgRow > rowToBead.size() - 1) {
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
                trgLines.add(insertIndex, line);
            }
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
            lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            return new int[] { Util.indexByIdentity(lines, selected.get(0)),
                    Util.indexByIdentity(lines, selected.get(selected.size() - 1)) };
        }

        private MutableBead splitBead(MutableBead bead) {
            if (bead.isBalanced()) {
                return bead;
            }
            int index = data.indexOf(bead);
            bead = splitBeadByCount(bead, Math.min(bead.sourceLines.size(), bead.targetLines.size()));
            data.add(index + 1, bead);
            return bead;
        }

        private MutableBead splitBeadByCount(MutableBead bead, int count) {
            List<String> splitSrc = new ArrayList<>(bead.sourceLines);
            bead.sourceLines.clear();
            List<String> splitTrg = new ArrayList<>(bead.targetLines);
            bead.targetLines.clear();
            for (int i = 0; i < count; i++) {
                if (!splitSrc.isEmpty()) {
                    bead.sourceLines.add(splitSrc.remove(0));
                }
                if (!splitTrg.isEmpty()) {
                    bead.targetLines.add(splitTrg.remove(0));
                }
            }
            return new MutableBead(splitSrc, splitTrg);
        }

        int getBeadNumberForRow(int row) {
            return data.indexOf(rowToBead.get(row));
        }

        MutableBead.Status getStatusForRow(int row) {
            return rowToBead.get(row).status;
        }

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

        boolean canMoveTo(int trgRow, int row, int col, boolean up) {
            if (!canMove(row, col, up) || trgRow == row) {
                return false;
            }
            if (trgRow >= 0 && trgRow < rowToBead.size()) {
                MutableBead srcBead = rowToBead.get(row);
                MutableBead trgBead = rowToBead.get(trgRow);
                if (srcBead == trgBead) {
                    return false;
                }
            }
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
            return data;
        }

        int[] getRowExtentsForBeadAtRow(int row) {
            MutableBead bead = rowToBead.get(row);
            return new int[] { rowToBead.indexOf(bead), rowToBead.lastIndexOf(bead) };
        }

        boolean isEditableColumn(int col) {
            return col == COL_SRC || col == COL_TRG;
        }

        List<Integer> realCellsInRowSpan(int col, int... rows) {
            List<Integer> result = new ArrayList<Integer>();
            for (int row : rows) {
                if (getValueAt(row, col) != null) {
                    result.add(row);
                }
            }
            return result;
        }

        int prevBeadFromRow(int row) {
            return nextBeadFromRowByOffset(row, -1);
        }

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
                MutableBead bead = rowToBead.get(row);
                Util.removeByIdentity(col == COL_SRC ? bead.sourceLines : bead.targetLines, line);
            }
            MutableBead trgBead = rowToBead.get(rows.get(0));
            List<String> trgLines = col == COL_SRC ? trgBead.sourceLines : trgBead.targetLines;
            Language lang = col == COL_SRC ? aligner.srcLang : aligner.trgLang;
            String combined = Util.join(lang, toCombine);
            trgLines.set(Util.indexByIdentity(trgLines, toCombine.get(0)), combined);
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
            lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            return Util.indexByIdentity(lines, combined);
        }

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
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            return new int[] { Util.indexByIdentity(lines, split[0]),
                    Util.indexByIdentity(lines, split[split.length - 1]) };
        }

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

        int[] slide(List<Integer> rows, int col, int offset) {
            if (offset == 0) {
                return new int[0];
            }
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            Collections.sort(rows);
            if (offset > 0) {
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
                    trgBead = new MutableBead();
                    data.add(0, trgBead);
                } else if (trgRow > rowToBead.size() - 1) {
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
                        // cell -> split bead and
                        trgBead = splitBead(trgBead);
                    }
                }
                Util.removeByIdentity(col == COL_SRC ? bead.sourceLines : bead.targetLines, line);
                List<String> trgLines = col == COL_SRC ? trgBead.sourceLines : trgBead.targetLines;
                int insertIndex = trgRow > row ? 0 : trgLines.size();
                trgLines.add(insertIndex, line);
            }
            makeCache();
            if (origRowCount != getRowCount()) {
                fireTableDataChanged();
            }
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            return new int[] { Util.indexByIdentity(lines, selected.get(0)),
                    Util.indexByIdentity(lines, selected.get(selected.size() - 1)) };
        }

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

        int[] splitBead(int[] rows, int col) {
            if (!isEditableColumn(col)) {
                throw new IllegalArgumentException();
            }
            int origRowCount = getRowCount();
            MutableBead bead = rowToBead.get(rows[0]);
            int beadIndex = data.indexOf(bead);
            List<String> lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            List<String> selected = new ArrayList<>(rows.length);
            for (int row : rows) {
                String line = lines.get(row);
                List<String> beadLines = col == COL_SRC ? bead.sourceLines : bead.targetLines;
                int index;
                if (line == null) {
                    index = beadLines.size();
                } else {
                    index = Util.indexByIdentity(beadLines, line);
                    if (index == -1) {
                        throw new IllegalArgumentException();
                    }
                    selected.add(line);
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
            lines = col == COL_SRC ? rowToSourceLine : rowToTargetLine;
            return new int[] { Util.indexByIdentity(lines, selected.get(0)),
                    Util.indexByIdentity(lines, selected.get(selected.size() - 1)) };
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

                javax.swing.JTable.DropLocation dloc = (javax.swing.JTable.DropLocation) support.getDropLocation();
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

                javax.swing.JTable.DropLocation dloc = (javax.swing.JTable.DropLocation) support.getDropLocation();
                int trgRow = dloc.getRow();

                JTable table = (JTable) support.getComponent();
                moveRows(table, rows, col, trgRow);
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

        public TableSelection(int[] rows, int[] cols) {
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
        private static final Border BORDER = new RoundedCornerBorder(8, Color.BLUE, RoundedCornerBorder.SIDE_ALL, 2);

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
                        BORDER.paintBorder(table, table.getGraphics(), rect.x, rect.y, rect.width, rect.height);
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
            int[] rows = model.getRowExtentsForBeadAtRow(loc.getRow());
            return table.getCellRect(rows[0], BeadTableModel.COL_SRC, true)
                    .union(table.getCellRect(rows[1], BeadTableModel.COL_TRG, true));
        }
    }
}
