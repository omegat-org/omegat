/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Kim Bruning
               2007 Zoltan Bartko
               2008 Alex Buloichik, Didier Briel
               2012 Martin Fleurke
               2014 Alex Buloichik Piotr Kulik
               2015 Aaron Madlon-Kay
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

package org.omegat.gui.filelist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.util.Java8Compat;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.StreamUtil;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.DataTableStyling;
import org.omegat.util.gui.DragTargetOverlay;
import org.omegat.util.gui.DragTargetOverlay.FileDropInfo;
import org.omegat.util.gui.OSXIntegration;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.TableColumnSizer;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Controller for showing all the files of the project.
 *
 * @author Keith Godfrey
 * @author Kim Bruning
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Piotr Kulik
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class ProjectFilesListController {

    private ProjectFilesList list;
    private FileInfoModel modelFiles;
    private AbstractTableModel modelTotal;
    private Sorter currentSorter;

    private TableFilterPanel filterPanel;

    private Font defaultFont;

    public ProjectFilesListController(MainWindow parent) {

        list = new ProjectFilesList();

        if (Platform.isMacOSX()) {
            OSXIntegration.enableFullScreen(list);
        }

        createTableFiles();
        createTableTotal();

        TableColumnSizer colSizer = TableColumnSizer.autoSize(list.tableFiles, 0, true);
        colSizer.addColumnAdjustmentListener(e -> propagateTableColumns());

        DragTargetOverlay.apply(list.tableFiles, new FileDropInfo(true) {
            @Override
            public String getImportDestination() {
                return Core.getProject().getProjectProperties().getSourceRoot();
            }
            @Override
            public boolean canAcceptDrop() {
                return Core.getProject().isProjectLoaded();
            }

            @Override
            public String getOverlayMessage() {
                return OStrings.getString("DND_ADD_SOURCE_FILE");
            }
            @Override
            public boolean acceptFile(File path) {
                return true;
            }
            @Override
            public Component getComponentToOverlay() {
                return list.tablesInnerPanel;
            }
        });

        defaultFont = list.tableFiles.getFont();
        if (Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
            String fontName = Preferences.getPreference(Preferences.TF_SRC_FONT_NAME);
            int fontSize = Integer.parseInt(Preferences.getPreference(Preferences.TF_SRC_FONT_SIZE));
            setFont(new Font(fontName, Font.PLAIN, fontSize));
        } else {
            setFont(defaultFont);
        }

        list.tablesInnerPanel.setBorder(new JScrollPane().getBorder());

        // set the position and size
        initWindowLayout();

        list.m_addNewFileButton.addActionListener(e -> doImportSourceFiles());
        list.m_wikiImportButton.addActionListener(e -> doWikiImport());
        list.m_closeButton.addActionListener(e -> doCancel());
        list.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                doCancel();
            }
            @Override
            public void windowActivated(WindowEvent e) {
                propagateTableColumns();
            }
        });

        StaticUIUtils.setEscapeAction(list, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        CoreEvents.registerProjectChangeListener(eventType -> {
            switch (eventType) {
            case CLOSE:
                list.tableFiles.setModel(new DefaultTableModel());
                list.tableFiles.repaint();
                modelTotal.fireTableDataChanged();
                list.setVisible(false);
                break;
            case LOAD:
            case CREATE:
                buildDisplay(Core.getProject().getProjectFiles());
                if (!Preferences.isPreferenceDefault(Preferences.PROJECT_FILES_SHOW_ON_LOAD, true)) {
                    break;
                }
                list.setVisible(true);
                SwingUtilities.invokeLater(() -> {
                    list.toFront();
                    list.tableFiles.requestFocus();
                    // Correctly set the active file in the Project Files dialog after reloading the project.
                    SwingUtilities.invokeLater(() -> selectCurrentFile(Core.getProject().getProjectFiles()));
                });
                break;
            default:
                // Nothing
            }
        });

        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            @Override
            public void onNewFile(String activeFileName) {
                list.tableFiles.repaint();
                list.tableTotal.repaint();
                modelTotal.fireTableDataChanged();
            }

            /**
             * Updates the number of translated segments only, does not rebuild the whole display.
             */
            @Override
            public void onEntryActivated(SourceTextEntry newEntry) {
                UIThreadsUtil.mustBeSwingThread();
                modelTotal.fireTableDataChanged();
            }
        });

        CoreEvents.registerFontChangedEventListener(newFont -> {
            if (!Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
                newFont = defaultFont;
            }
            setFont(newFont);
        });

        list.tableFiles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getModifiersEx() == 0) {
                    gotoFile(list.tableFiles.rowAtPoint(e.getPoint()));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    doPopup(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    doPopup(e.getPoint());
                }
            }

            private void doPopup(Point p) {
                int row = list.tableFiles.rowAtPoint(p);
                if (row != -1) {
                    JPopupMenu popup = createContextMenuForRow(row);
                    if (popup != null) {
                        popup.show(list.tableFiles, p.x, p.y);
                    }
                }
            }
        });

        list.tableFiles.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    gotoFile(list.tableFiles.getSelectedRow());
                    e.consume();
                } else if (isFiltering() && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    endFilter();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
                    int row = list.tableFiles.getSelectedRow();
                    Point p = list.tableFiles.getCellRect(row, 0, false).getLocation();
                    JPopupMenu popup = createContextMenuForRow(row);
                    if (popup != null) {
                        popup.show(list.tableFiles, p.x, p.y);
                    }
                    e.consume();
                }
            }
        });
        list.tableFiles.getSelectionModel().addListSelectionListener(e -> updateButtonState());
        list.tableFiles.addKeyListener(filterTrigger);
        list.tableTotal.addKeyListener(filterTrigger);
        list.btnUp.addKeyListener(filterTrigger);
        list.btnDown.addKeyListener(filterTrigger);
        list.btnFirst.addKeyListener(filterTrigger);
        list.btnLast.addKeyListener(filterTrigger);

        list.btnUp.addActionListener(moveAction);
        list.btnDown.addActionListener(moveAction);
        list.btnFirst.addActionListener(moveAction);
        list.btnLast.addActionListener(moveAction);
    }

    private void updateTitle() {
        int numFiles = currentSorter.getModelRowCount();
        if (isFiltering()) {
            int showingFiles = currentSorter.getViewRowCount();
            list.setTitle(StringUtil.format(OStrings.getString("PF_WINDOW_TITLE_FILTERED"), showingFiles, numFiles));
        } else {
            list.setTitle(StringUtil.format(OStrings.getString("PF_WINDOW_TITLE"), numFiles));
        }
    }

    private void updateButtonState() {
        boolean enabled = list.tableFiles.getSelectedRow() != -1;
        list.btnDown.setEnabled(enabled);
        list.btnFirst.setEnabled(enabled);
        list.btnLast.setEnabled(enabled);
        list.btnUp.setEnabled(enabled);
    }

    private JPopupMenu createContextMenuForRow(int row) {
        int[] rows;
        if (IntStream.of(list.tableFiles.getSelectedRows()).anyMatch(r -> r == row)) {
            // If clicked on selection, use selection
            rows = list.tableFiles.getSelectedRows();
        } else {
            // Otherwise use the clicked row
            rows = new int[] { row };
        }
        List<FileInfo> infos = IntStream.of(rows).map(list.tableFiles.getRowSorter()::convertRowIndexToModel)
                .mapToObj(modelFiles::getDataAtRow)
                .collect(Collectors.toList());
        if (infos.isEmpty() || infos.stream().anyMatch(Objects::isNull)) {
            return null;
        }
        String sourceDir = Core.getProject().getProjectProperties().getSourceRoot();
        String targetDir = Core.getProject().getProjectProperties().getTargetRoot();
        JPopupMenu menu = new JPopupMenu();
        addContextMenuItem(menu, true,
                infos.stream().map(i -> new File(sourceDir, i.filePath)).collect(Collectors.toList()));
        addContextMenuItem(menu, false,
                infos.stream().map(i -> new File(targetDir, Core.getProject().getTargetPathForSourceFile(i.filePath)))
                        .collect(Collectors.toList()));
        return menu;
    }

    private void addContextMenuItem(JPopupMenu menu, boolean isSource, List<File> files) {
        long presentFiles = files.stream().filter(File::isFile).count();
        String defaultTitle, modTitle;
        if (presentFiles > 1) {
            defaultTitle = StringUtil.format(
                    OStrings.getString(isSource ? "PF_OPEN_SOURCE_FILES" : "PF_OPEN_TARGET_FILES"), presentFiles);
            modTitle = StringUtil.format(OStrings.getString(isSource ? "PF_OPEN_SOURCE_FILES" : "PF_OPEN_TARGET_FILES"),
                    presentFiles);
        } else {
            defaultTitle = OStrings.getString(isSource ? "PF_OPEN_SOURCE_FILE" : "PF_OPEN_TARGET_FILE");
            modTitle = OStrings.getString(isSource ? "PF_REVEAL_SOURCE_FILE" : "PF_REVEAL_TARGET_FILE");
        }
        JMenuItem item = menu.add(defaultTitle);
        item.addActionListener(e -> {
            boolean openParent = (e.getModifiers() & Java8Compat.getMenuShortcutKeyMaskEx()) != 0;
            Stream<File> stream;
            if (openParent) {
                stream = files.stream().map(File::getParentFile).distinct().filter(File::isDirectory);
            } else {
                stream = files.stream().filter(File::isFile);
            }
            stream.forEach(f -> {
                try {
                    Desktop.getDesktop().open(f);
                } catch (IOException ex) {
                    Log.log(ex);
                }
            });
        });
        item.setEnabled(presentFiles > 0);
        item.addMenuKeyListener(new MenuKeyListener() {
            @Override
            public void menuKeyTyped(MenuKeyEvent e) {
            }
            @Override
            public void menuKeyReleased(MenuKeyEvent e) {
                if ((e.getModifiersEx() & Java8Compat.getMenuShortcutKeyMaskEx()) != 0
                        || e.getKeyCode() == KeyEvent.VK_META || e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    setText(defaultTitle);
                }
            }
            @Override
            public void menuKeyPressed(MenuKeyEvent e) {
                if ((e.getModifiersEx() & Java8Compat.getMenuShortcutKeyMaskEx()) != 0) {
                    setText(modTitle);
                }
            }

            private void setText(String text) {
                item.setText(text);
                menu.pack();
            }
        });
    }

    private final KeyListener filterTrigger = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if ((e.getModifiersEx() == 0 || e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK)
                    && !Character.isWhitespace(c) && !Character.isISOControl(c)) {
                if (isFiltering()) {
                    resumeFilter(e.getKeyChar());
                } else {
                    startFilter(e.getKeyChar());
                }
                e.consume();
            }
        }
    };

    private void startFilter(char c) {
        if (isFiltering()) {
            throw new IllegalStateException("Already filtering!");
        }
        filterPanel = new TableFilterPanel();
        list.btnDown.setEnabled(false);
        list.btnUp.setEnabled(false);
        list.btnFirst.setEnabled(false);
        list.btnLast.setEnabled(false);
        list.tablesOuterPanel.add(filterPanel, BorderLayout.SOUTH);
        filterPanel.filterTextField.addActionListener(e -> gotoFile(list.tableFiles.getSelectedRow()));
        filterPanel.filterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    endFilter();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    int selection = Math.max(0, list.tableFiles.getSelectedRow());
                    int total = list.tableFiles.getRowCount();
                    int up = (selection - 1 + total) % total;
                    selectRow(up);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    int selection = list.tableFiles.getSelectedRow();
                    int down = (selection + 1) % list.tableFiles.getRowCount();
                    selectRow(down);
                    e.consume();
                }
            }
        });
        filterPanel.filterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        filterPanel.filterTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                filterPanel.filterTextField.setCaretPosition(filterPanel.filterTextField.getText().length());
            }
        });
        filterPanel.filterCloseButton.addActionListener(e -> endFilter());
        filterPanel.filterTextField.setText(Character.toString(c));
        filterPanel.filterTextField.requestFocus();
        list.validate();
        list.repaint();
    }

    private boolean isFiltering() {
        return filterPanel != null;
    }

    private void resumeFilter(char c) {
        if (!isFiltering()) {
            throw new IllegalStateException("Can't resume filtering when we're not filtering!");
        }
        try {
            filterPanel.filterTextField.getDocument().insertString(filterPanel.filterTextField.getText().length(),
                    Character.toString(c), null);
            filterPanel.filterTextField.requestFocus();
        } catch (BadLocationException ex) {
            // Nothing
        }
    }

    private void applyFilter() {
        if (!isFiltering()) {
            throw new IllegalStateException("Can't apply filter when we're not filtering!");
        }
        String quoted = Pattern.quote(filterPanel.filterTextField.getText());
        Pattern findPattern = Pattern.compile(quoted, Pattern.CASE_INSENSITIVE);
        FilesTableColumn.FILE_NAME.setHighlightPattern(findPattern);
        Pattern matchPattern = Pattern.compile(".*" + quoted + ".*", Pattern.CASE_INSENSITIVE);
        currentSorter.setFilter(matchPattern);
        selectRow(0);
    }

    private void endFilter() {
        if (!isFiltering()) {
            throw new IllegalStateException("Can't end filtering when we're not filtering!");
        }
        FilesTableColumn.FILE_NAME.setHighlightPattern(null);
        list.tablesOuterPanel.remove(filterPanel);
        list.btnDown.setEnabled(true);
        list.btnUp.setEnabled(true);
        list.btnFirst.setEnabled(true);
        list.btnLast.setEnabled(true);
        filterPanel = null;
        currentSorter.setFilter(null);
        list.tableFiles.requestFocus();
        int currentRow = list.tableFiles.getSelectedRow();
        list.tableFiles.scrollRectToVisible(list.tableFiles.getCellRect(currentRow, 0, true));
        list.validate();
        list.repaint();
    }

    ActionListener moveAction = e -> {
        int[] selected = list.tableFiles.getSelectedRows();
        if (selected.length == 0) {
            return;
        }

        int pos = selected[0];

        int newPos;
        if (e.getSource() == list.btnUp) {
            newPos = pos - 1;
        } else if (e.getSource() == list.btnDown) {
            newPos = pos + 1;
        } else if (e.getSource() == list.btnFirst) {
            newPos = 0;
        } else if (e.getSource() == list.btnLast) {
            newPos = Integer.MAX_VALUE;
        } else {
            return;
        }
        pos = currentSorter.moveTo(selected, newPos);
        list.tableFiles.getSelectionModel().setSelectionInterval(pos, pos + selected.length - 1);
    };

    public boolean isActive() {
        return list.isActive();
    }

    public void setActive(boolean active) {
        if (active) {
            // moved current file selection here so it will be properly set on each activation
            list.setVisible(true);
            list.toFront();
            SwingUtilities.invokeLater(() -> selectCurrentFile(Core.getProject().getProjectFiles()));
        } else {
            list.setVisible(false);
        }
    }

    /**
    * Selects current file on project files table
    */
    private void selectCurrentFile(List<IProject.FileInfo> files) {
        // clear selection from possible previous multiple selections
        list.tableFiles.getSelectionModel().clearSelection();
        String currentFile = Core.getEditor().getCurrentFile();
        // set current file as default selection
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).filePath.equals(currentFile)) {
                int pos = list.tableFiles.convertRowIndexToView(i);
                selectRow(pos);
                break;
            }
        }
        list.tableFiles.requestFocus();
    }

    /**
     * Select a row in tableFiles and make sure it's visible
     */
    private void selectRow(int row) {
        list.tableFiles.getSelectionModel().setSelectionInterval(row, row);
        list.tableFiles.scrollRectToVisible(list.tableFiles.getCellRect(row, 0, true));
    }

    /**
     * Loads/sets the position and size of the project files window.
     */
    private void initWindowLayout() {
        // set default size and position
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        list.setBounds((screenSize.width - 640) / 2, (screenSize.height - 400) / 2, 640, 400);
        StaticUIUtils.persistGeometry(list, Preferences.PROJECT_FILES_WINDOW_GEOMETRY_PREFIX);
    }

    private void doCancel() {
        list.setVisible(false);
    }

    /**
     * Builds the table which lists all the project files.
     */
    private void buildDisplay(List<IProject.FileInfo> files) {
        UIThreadsUtil.mustBeSwingThread();

        String path;
        String statFileName = Core.getProject().getProjectProperties().getProjectInternal()
                + OConsts.STATS_FILENAME;
        File statFile = new File(statFileName);
        try {
            path = statFile.getCanonicalPath();
        } catch (IOException ex) {
            path = statFile.getAbsolutePath();
        }
        String statText = MessageFormat.format(OStrings.getString("PF_STAT_PATH"), path);
        list.statLabel.setText(statText);

        uiUpdateImportButtonStatus();

        OSXIntegration.setProxyIcon(list.getRootPane(),
                new File(Core.getProject().getProjectProperties().getSourceRoot()));

        setTableFilesModel(files);
        updateTitle();
    }

    private void createTableFiles() {
        DataTableStyling.applyColors(list.tableFiles);
        list.tableFiles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    private void propagateTableColumns() {
        // Set last column of tableTotal to match size of scrollbar.
        JScrollBar scrollbar = list.scrollFiles.getVerticalScrollBar();
        int sbWidth = scrollbar == null || !scrollbar.isVisible() ? 0 : scrollbar.getWidth();
        list.tableTotal.getColumnModel().getColumn(TotalsTableColumn.MARGIN.index).setPreferredWidth(sbWidth);

        // Propagate column sizes to totals table
        for (int i = 0; i < list.tableFiles.getColumnCount(); i++) {
            TableColumn srcCol = list.tableFiles.getColumnModel().getColumn(i);
            TableColumn trgCol = list.tableTotal.getColumnModel().getColumn(i);
            trgCol.setPreferredWidth(srcCol.getWidth());
        }
    }

    enum FilesTableColumn {
        FILE_NAME(0, OStrings.getString("PF_FILENAME"), String.class,
                new DataTableStyling.PatternHighlightRenderer(false)),
        FILTER(1, OStrings.getString("PF_FILTERNAME"), String.class, DataTableStyling.getTextCellRenderer()),
        ENCODING(2, OStrings.getString("PF_ENCODING"), String.class, DataTableStyling.getTextCellRenderer()),
        SEGMENTS(3, OStrings.getString("PF_NUM_SEGMENTS"), Integer.class, DataTableStyling.getNumberCellRenderer()),
        UNIQUE_SEGMENTS(4, OStrings.getString("PF_NUM_UNIQUE_SEGMENTS"), Integer.class,
                DataTableStyling.getNumberCellRenderer());

        private final int index;
        private final String label;
        private final Class<?> clazz;
        private final TableCellRenderer renderer;

        FilesTableColumn(int index, String label, Class<?> clazz, TableCellRenderer renderer) {
            this.index = index;
            this.label = label;
            this.clazz = clazz;
            this.renderer = renderer;
        }

        static FilesTableColumn get(int index) {
            return values()[index];
        }

        private void setHighlightPattern(Pattern pattern) {
            if (renderer instanceof DataTableStyling.PatternHighlightRenderer) {
                ((DataTableStyling.PatternHighlightRenderer) renderer).setPattern(pattern);
            } else {
                throw new UnsupportedOperationException("Column " + label + " doesn't support pattern highlights");
            }
        }
    }

    private void setTableFilesModel(final List<IProject.FileInfo> files) {
        modelFiles = new FileInfoModel(files);
        list.tableFiles.setModel(modelFiles);
        TableColumnModel colModel = list.tableFiles.getColumnModel();
        colModel.addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                // Propagate movement to tableTotal
                list.tableTotal.getColumnModel().moveColumn(e.getFromIndex(), e.getToIndex());
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });
        for (FilesTableColumn col : FilesTableColumn.values()) {
            TableColumn tCol = colModel.getColumn(col.index);
            tCol.setCellRenderer(new CustomRenderer(files, col.renderer));
        }
        currentSorter = new Sorter(files);
        currentSorter.addRowSorterListener(e -> updateTitle());
        list.tableFiles.setRowSorter(currentSorter);
    }

    enum TotalsTableColumn {
        LABEL(0, String.class, DataTableStyling.getTextCellRenderer()) {
            @Override
            protected Object getValue(int row) {
                switch (row) {
                case 0:
                    return OStrings.getString("GUI_PROJECT_TOTAL_SEGMENTS");
                case 1:
                    return OStrings.getString("GUI_PROJECT_UNIQUE_SEGMENTS");
                case 2:
                    return OStrings.getString("GUI_PROJECT_TRANSLATED");
                default:
                    throw new IllegalArgumentException();
                }
            }
        },
        EMPTY_1(1, String.class, DataTableStyling.getTextCellRenderer()),
        EMPTY_2(2, String.class, DataTableStyling.getTextCellRenderer()),
        EMPTY_3(3, Integer.class, DataTableStyling.getNumberCellRenderer()),
        VALUE(4, Integer.class, DataTableStyling.getNumberCellRenderer()) {
            @Override
            protected Object getValue(int row) {
                if (!Core.getProject().isProjectLoaded()) {
                    return "-";
                }
                StatisticsInfo stat = Core.getProject().getStatistics();
                switch (row) {
                case 0:
                    return stat.numberOfSegmentsTotal;
                case 1:
                    return stat.numberOfUniqueSegments;
                case 2:
                    return stat.numberofTranslatedSegments;
                default:
                    throw new IllegalArgumentException();
                }
            }
        },
        MARGIN(5, String.class, new DataTableStyling.AlternatingHighlightRenderer().setDoHighlight(false));

        private final int index;
        private final Class<?> clazz;
        private final TableCellRenderer renderer;

        TotalsTableColumn(int index, Class<?> clazz, TableCellRenderer renderer) {
            this.index = index;
            this.clazz = clazz;
            this.renderer = renderer;
        }

        protected Object getValue(int row) {
            return "";
        }

        static TotalsTableColumn get(int index) {
            return values()[index];
        }
    }

    private void createTableTotal() {
        DataTableStyling.applyColors(list.tableTotal);
        list.tableTotal.setBorder(new MatteBorder(1, 0, 0, 0, DataTableStyling.COLOR_ALTERNATING_HILITE));

        modelTotal = new AbstractTableModel() {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return TotalsTableColumn.get(columnIndex).getValue(rowIndex);
            }

            @Override
            public int getColumnCount() {
                return TotalsTableColumn.values().length;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return TotalsTableColumn.get(columnIndex).clazz;
            }

            @Override
            public int getRowCount() {
                return 3;
            }
        };
        list.tableTotal.setModel(modelTotal);

        TableColumnModel colModel = list.tableTotal.getColumnModel();
        for (TotalsTableColumn col : TotalsTableColumn.values()) {
            TableColumn tCol = colModel.getColumn(col.index);
            tCol.setCellRenderer(new CustomRenderer(null, col.renderer));
            tCol.setMinWidth(0);
        }
    }

    /**
     * Imports the file/files/folder into project's source files.
     */
    private void doImportSourceFiles() {
        ProjectUICommands.doPromptImportSourceFiles();
    }

    private void doWikiImport() {
        ProjectUICommands.doWikiImport();
    }

    /** Updates the Import Files button status. */
    private void uiUpdateImportButtonStatus() {
        list.m_addNewFileButton.setEnabled(Core.getProject().isProjectLoaded());
        list.m_wikiImportButton.setEnabled(Core.getProject().isProjectLoaded());
    }

    private void gotoFile(int row) {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        if (row < 0) {
            return;
        }
        Cursor hourglassCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = list.getCursor();
        list.setCursor(hourglassCursor);
        try {
            int modelRow = list.tableFiles.convertRowIndexToModel(row);
            Core.getEditor().gotoFile(modelRow);
            Core.getEditor().requestFocus();
        } catch (IndexOutOfBoundsException ex) {
            // Data changed.
        } finally {
            list.setCursor(oldCursor);
        }
    }

    private static final Color COLOR_SPECIAL_FG = Color.BLACK;
    private static final Color COLOR_SPECIAL_BG = new Color(0xC8DDF2);

    /**
     * Render for table cells.
     */
    private class CustomRenderer implements TableCellRenderer {

        private final List<IProject.FileInfo> files;
        private final TableCellRenderer childRenderer;

        CustomRenderer(List<IProject.FileInfo> files, TableCellRenderer childRenderer) {
            this.files = files;
            this.childRenderer = childRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component c = childRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected && isSpecialHighlightRow(row)) {
                c.setForeground(COLOR_SPECIAL_FG);
                c.setBackground(COLOR_SPECIAL_BG);
            }
            return c;
        }

        private boolean isSpecialHighlightRow(int row) {
            if (files == null) {
                return false;
            }
            try {
                int modelRow = list.tableFiles.convertRowIndexToModel(row);
                IProject.FileInfo fi = files.get(modelRow);
                return fi.filePath.equals(Core.getEditor().getCurrentFile());
            } catch (IndexOutOfBoundsException ex) {
                // data changed
                return false;
            }
        }
    }

    private void setFont(Font font) {
        DataTableStyling.applyFont(list.tableFiles, font);
        DataTableStyling.applyFont(list.tableTotal, font.deriveFont(Font.BOLD));
        list.statLabel.setFont(font);
    }

    static class FileInfoModel extends AbstractTableModel {
        private final List<IProject.FileInfo> files;

        FileInfoModel(List<IProject.FileInfo> files) {
            this.files = files;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            IProject.FileInfo fi;
            try {
                fi = files.get(rowIndex);
            } catch (IndexOutOfBoundsException ex) {
                // data changed
                return null;
            }
            switch (FilesTableColumn.get(columnIndex)) {
            case FILE_NAME:
                return fi.filePath;
            case FILTER:
                return fi.filterFileFormatName;
            case ENCODING:
                return fi.fileEncoding;
            case SEGMENTS:
                return fi.entries.size();
            case UNIQUE_SEGMENTS:
                StatisticsInfo stat = Core.getProject().getStatistics();
                return stat.uniqueCountsByFile.get(fi.filePath);
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public int getColumnCount() {
            return FilesTableColumn.values().length;
        }

        @Override
        public int getRowCount() {
            return files.size();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return FilesTableColumn.get(columnIndex).clazz;
        }

        @Override
        public String getColumnName(int column) {
            return FilesTableColumn.get(column).label;
        }

        public FileInfo getDataAtRow(int row) {
            return (row >= 0 && row < files.size()) ? files.get(row) : null;
        }
    }

    class Sorter extends RowSorter<FileInfoModel> {
        private final List<IProject.FileInfo> files;
        private SortKey sortKey = new SortKey(0, SortOrder.UNSORTED);
        private Integer[] modelToView;
        private List<Integer> viewToModel;
        private Pattern filter;

        Sorter(final List<IProject.FileInfo> files) {
            this.files = files;
            init();
            applyPrefs();
        }

        private void init() {
            if (modelToView == null || modelToView.length != files.size()) {
                modelToView = new Integer[files.size()];
            }
            int excluded = 0;
            for (int i = 0; i < modelToView.length; i++) {
                if (include(files.get(i))) {
                    modelToView[i] = i - excluded;
                } else {
                    modelToView[i] = -1;
                    excluded++;
                }
            }
            viewToModel = new ArrayList<Integer>(modelToView.length - excluded);
            for (int i = 0, j = 0; i < modelToView.length; i++) {
                if (modelToView[i] != -1) {
                    viewToModel.add(j++, i);
                }
            }
        }

        private void applyPrefs() {
            List<String> filenames = files.stream().map(fi -> fi.filePath)
                    .sorted(StreamUtil.comparatorByList(Core.getProject().getSourceFilesOrder()))
                    .collect(Collectors.toList());
            Collections.sort(viewToModel, (o1, o2) -> {
                int pos1 = filenames.indexOf(files.get(o1).filePath);
                int pos2 = filenames.indexOf(files.get(o2).filePath);
                if (pos1 < pos2) {
                    return -1;
                } else if (pos1 > pos2) {
                    return 1;
                } else {
                    return 0;
                }
            });

            recalc();
        }

        @Override
        public int getModelRowCount() {
            return files.size();
        }

        @Override
        public int getViewRowCount() {
            return viewToModel.size();
        }

        @Override
        public FileInfoModel getModel() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void modelStructureChanged() {
        }

        @Override
        public void allRowsChanged() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void rowsInserted(int firstRow, int endRow) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void rowsUpdated(int firstRow, int endRow) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void rowsUpdated(int firstRow, int endRow, int column) {
        }

        @Override
        public void rowsDeleted(int firstRow, int endRow) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public List<? extends SortKey> getSortKeys() {
            return Arrays.asList(sortKey);
        }

        @Override
        public void setSortKeys(List<? extends javax.swing.RowSorter.SortKey> keys) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void toggleSortOrder(int column) {
            SortOrder order = SortOrder.ASCENDING;
            if (sortKey.getSortOrder() == SortOrder.ASCENDING) {
                order = SortOrder.DESCENDING;
            }
            sortKey = new SortKey(column, order);
            sort();
            save();
        }

        @Override
        public int convertRowIndexToModel(int index) {
            return viewToModel.get(index);
        }

        @Override
        public int convertRowIndexToView(int index) {
            return modelToView[index];
        }

        void sort() {
            if (sortKey.getSortOrder() == SortOrder.UNSORTED) {
                applyPrefs();
                return;
            }
            final StatisticsInfo stat = Core.getProject().getStatistics();
            Collections.sort(viewToModel, (o1, o2) -> {
                IProject.FileInfo f1 = files.get(o1);
                IProject.FileInfo f2 = files.get(o2);
                int c = 0;
                switch (sortKey.getColumn()) {
                case 0:
                    c = f1.filePath.compareToIgnoreCase(f2.filePath);
                    break;
                case 1:
                    c = f1.filterFileFormatName.compareToIgnoreCase(f2.filterFileFormatName);
                    break;
                case 2:
                    String fe1 = f1.fileEncoding == null ? "" : f1.fileEncoding;
                    String fe2 = f2.fileEncoding == null ? "" : f2.fileEncoding;
                    c = fe1.compareToIgnoreCase(fe2);
                    break;
                case 3:
                    int m1 = f1.entries.size();
                    int m2 = f2.entries.size();
                    c = m1 > m2 ? 1 : m1 < m2 ? -1 : 0;
                    break;
                case 4:
                    int n1 = stat.uniqueCountsByFile.get(f1.filePath);
                    int n2 = stat.uniqueCountsByFile.get(f2.filePath);
                    c = n1 > n2 ? 1 : n1 < n2 ? -1 : 0;
                    break;
                }
                if (sortKey.getSortOrder() == SortOrder.DESCENDING) {
                    c = -c;
                }
                return c;
            });
            recalc();
        }

        private void recalc() {
            for (int i = 0; i < viewToModel.size(); i++) {
                modelToView[viewToModel.get(i)] = i;
            }
        }

        public int moveTo(int[] selected, int newPos) {
            int[] temp = new int[selected.length];
            int n = selected.length;
            for (int i = 0; i < selected.length; i++) {
                temp[i] = viewToModel.remove(selected[--n]);
            }

            newPos = Math.max(newPos, 0);
            newPos = Math.min(newPos, viewToModel.size());

            for (int i = 0; i < temp.length; i++) {
                viewToModel.add(newPos, temp[i]);
            }
            recalc();
            save();
            list.tableFiles.scrollRectToVisible(list.tableFiles.getCellRect(newPos, 0, true)
                    .union(list.tableFiles.getCellRect(newPos + temp.length, 0, true)));
            list.tableFiles.repaint();
            sortKey = new SortKey(0, SortOrder.UNSORTED);
            list.tableFiles.getTableHeader().repaint();
            return newPos;
        }

        private void save() {
            List<String> filenames = new ArrayList<String>();
            for (Integer i : viewToModel) {
                String fn = files.get(i).filePath;
                filenames.add(fn);
            }
            Core.getProject().setSourceFilesOrder(filenames);
        }

        public void setFilter(Pattern pattern) {
            if (filter == pattern || pattern != null && pattern.equals(filter)) {
                return;
            }
            filter = pattern;
            int[] lastViewToModel = viewToModel.stream().mapToInt(Integer::intValue).toArray();
            init();
            sort();
            fireRowSorterChanged(lastViewToModel);
        }

        private boolean include(IProject.FileInfo item) {
            if (filter == null) {
                return true;
            }
            return filter.matcher(item.filePath).matches();
        }
    }
}
