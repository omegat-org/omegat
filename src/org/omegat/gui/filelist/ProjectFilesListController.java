/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Kim Bruning
               2007 Zoltan Bartko
               2008 Alex Buloichik, Didier Briel
               2012 Martin Fleurke
               2014 Alex Buloichik
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

package org.omegat.gui.filelist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.statistics.StatisticsInfo;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
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
 */
public class ProjectFilesListController {
    private static final Color COLOR_STANDARD_FG = Color.BLACK;
    private static final Color COLOR_STANDARD_BG = Color.WHITE;
    private static final Color COLOR_CURRENT_FG = Color.BLACK;
    private static final Color COLOR_CURRENT_BG = new Color(0xC8DDF2);
    private static final Color COLOR_SELECTION_FG = Color.WHITE;
    private static final Color COLOR_SELECTION_BG = new Color(0x2F77DA);

    private static final int LINE_SPACING = 6;

    private ProjectFilesList list;
    private AbstractTableModel modelFiles, modelTotal;
    private Sorter currentSorter;

    private MainWindow m_parent;

    private Font dialogFont;

    public ProjectFilesListController(MainWindow parent) {
        m_parent = parent;

        list = new ProjectFilesList();

        createTableFiles();
        dialogFont = list.tableFiles.getFont();
        createTableTotal();

        // set the position and size
        initWindowLayout();

        list.m_addNewFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doImportSourceFiles();
            }
        });
        list.m_wikiImportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doWikiImport();
            }
        });
        list.m_closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });
        list.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                doCancel();
            }
        });

        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        };
        list.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        list.getRootPane().getActionMap().put("ESCAPE", escapeAction);

        list.statLabel.setFont(list.tableTotal.getFont());
        list.statLabel.setBackground(list.getBackground());

        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                switch (eventType) {
                case CLOSE:
                    list.setVisible(false);
                    break;
                case LOAD:
                case CREATE:
                    buildDisplay(Core.getProject().getProjectFiles());
                    list.setVisible(true);
                    list.tableFiles.requestFocus();
                    buildTotalTableLayout();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            list.toFront();
                        }
                    });
                    break;
                }
            }
        });

        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            public void onNewFile(String activeFileName) {
                list.tableFiles.repaint();
                list.tableTotal.repaint();
                modelTotal.fireTableDataChanged();
            }

            /**
             * Updates the number of translated segments only, does not rebuild the whole display.
             */
            public void onEntryActivated(SourceTextEntry newEntry) {
                UIThreadsUtil.mustBeSwingThread();
                modelTotal.fireTableDataChanged();
            }
        });

        CoreEvents.registerFontChangedEventListener(new IFontChangedEventListener() {
            public void onFontChanged(Font newFont) {
                if (!Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT))
                    // We're using the standard dialog font
                    newFont = dialogFont;
                list.tableFiles.setFont(newFont);
                list.tableTotal.setFont(new Font(newFont.getName(), Font.BOLD, newFont.getSize()));
                list.tableFiles.setRowHeight(newFont.getSize() + LINE_SPACING);
                list.tableTotal.setRowHeight(newFont.getSize() + LINE_SPACING);
                list.statLabel.setFont(newFont);
            }
        });

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
            }

            public void onApplicationShutdown() {
                saveWindowLayout();
            }
        });

        list.tableFiles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gotoFile(list.tableFiles.rowAtPoint(e.getPoint()));
            }
        });
        list.tableFiles.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    gotoFile(list.tableFiles.getSelectedRow());
                    e.consume();
                }
            }
        });

        list.btnUp.addActionListener(moveAction);
        list.btnDown.addActionListener(moveAction);
        list.btnFirst.addActionListener(moveAction);
        list.btnLast.addActionListener(moveAction);
    }

    ActionListener moveAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int pos = list.tableFiles.getSelectedRow();
            if (pos < 0) {
                return;
            }
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
            pos = currentSorter.moveTo(pos, newPos);
            list.tableFiles.getSelectionModel().setSelectionInterval(pos, pos);
        }
    };

    public boolean isActive() {
        return list.isActive();
    }

    public void setActive(boolean active) {
        if (active) {
            list.setVisible(true);
            list.toFront();
        } else {
            list.setVisible(false);
        }
    }

    /**
     * Loads/sets the position and size of the project files window.
     */
    private void initWindowLayout() {
        // main window
        try {
            String dx = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_X);
            String dy = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            list.setLocation(x, y);
            String dw = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_WIDTH);
            String dh = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            list.setSize(w, h);
        } catch (NumberFormatException nfe) {
            // set default size and position
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            list.setBounds((screenSize.width - 640) / 2, (screenSize.height - 400) / 2, 640, 400);
        }
    }

    /**
     * Saves the size and position of the project files window
     */
    private void saveWindowLayout() {
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_WIDTH, list.getWidth());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_HEIGHT, list.getHeight());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_X, list.getX());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_Y, list.getY());
    }

    private void doCancel() {
        list.setVisible(false);

        RowSorter<AbstractTableModel> rs = (RowSorter<AbstractTableModel>) list.tableFiles.getRowSorter();
        RowSorter.SortKey sk;
        if (rs.getSortKeys().isEmpty()) {
            sk = new RowSorter.SortKey(1, SortOrder.ASCENDING);
        } else {
            sk = rs.getSortKeys().get(0);
        }
    }

    /**
     * Builds the table which lists all the project files.
     */
    public void buildDisplay(List<IProject.FileInfo> files) {
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

        String currentFile = Core.getEditor().getCurrentFile();
        // set current file as default selection
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).filePath.equals(currentFile)) {
                // set selection to currently edited file
                list.tableFiles.getSelectionModel().setSelectionInterval(i, i);
                // set current file visible in scroller
                list.tableFiles.scrollRectToVisible(list.tableFiles.getCellRect(i, 0, true));
                break;
            }
        }

        uiUpdateImportButtonStatus();
        list.setTitle(StaticUtils.format(OStrings.getString("PF_WINDOW_TITLE"), files.size()));

        setTableFilesModel(files);
    }

    private void buildTotalTableLayout() {
        list.scrollFiles.setBorder(BorderFactory.createEmptyBorder());
        Border b2 = list.scrollFiles.getBorder();
        Insets i1 = b2.getBorderInsets(list.tableFiles);
        int sc = list.scrollFiles.getVerticalScrollBar().isVisible() ? list.scrollFiles
                .getVerticalScrollBar().getWidth() : 0;

        GridBagLayout ly = (GridBagLayout) list.getContentPane().getLayout();
        GridBagConstraints c = ly.getConstraints(list.tableTotal);
        c.insets = new Insets(0, i1.left, 0, sc);
        ly.setConstraints(list.tableTotal, c);
    }

    private void createTableFiles() {
        applyColors(list.tableFiles);

        list.tableFiles.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        list.tableFiles.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                changeTotalColumns();
            }
        });
    }

    private void setTableFilesModel(final List<IProject.FileInfo> files) {
        modelFiles = new AbstractTableModel() {
            public Object getValueAt(int rowIndex, int columnIndex) {
                IProject.FileInfo fi;
                try {
                    fi = files.get(rowIndex);
                } catch (IndexOutOfBoundsException ex) {
                    // data changed
                    return null;
                }
                switch (columnIndex) {
                case 0:
                    return fi.filePath;
                case 1:
                    return fi.filterFileFormatName;
                case 2:
                    return fi.fileEncoding;
                case 3:
                    return fi.entries.size();
                default:
                    return null;
                }
            }

            public int getColumnCount() {
                return 4;
            }

            public int getRowCount() {
                return files.size();
            }

            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
                case 3:
                    return Integer.class;
                default:
                    return null;
                }
            }
        };

        list.tableFiles.setModel(modelFiles);

        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn cFile = new TableColumn(0, 150);
        cFile.setHeaderValue(OStrings.getString("PF_FILENAME"));
        cFile.setCellRenderer(new CustomRenderer(files, SwingConstants.LEFT, null));
        TableColumn cFilter = new TableColumn(1, 100);
        cFilter.setHeaderValue(OStrings.getString("PF_FILTERNAME"));
        cFilter.setCellRenderer(new CustomRenderer(files, SwingConstants.LEFT, null));
        TableColumn cEncoding = new TableColumn(2, 50);
        cEncoding.setHeaderValue(OStrings.getString("PF_ENCODING"));
        cEncoding.setCellRenderer(new CustomRenderer(files, SwingConstants.LEFT, null));
        TableColumn cCount = new TableColumn(3, 50);
        cCount.setHeaderValue(OStrings.getString("PF_NUM_SEGMENTS"));
        cCount.setCellRenderer(new CustomRenderer(files, SwingConstants.RIGHT, ",##0"));
        columns.addColumn(cFile);
        columns.addColumn(cFilter);
        columns.addColumn(cEncoding);
        columns.addColumn(cCount);
        columns.addColumnModelListener(new TableColumnModelListener() {
            public void columnAdded(TableColumnModelEvent e) {
            }

            public void columnMarginChanged(ChangeEvent e) {
                changeTotalColumns();
            }

            public void columnMoved(TableColumnModelEvent e) {
                list.tableTotal.getColumnModel().moveColumn(e.getFromIndex(), e.getToIndex());
                changeTotalColumns();
            }

            public void columnRemoved(TableColumnModelEvent e) {
            }

            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });
        list.tableFiles.setColumnModel(columns);

        currentSorter = new Sorter(files);
        list.tableFiles.setRowSorter((RowSorter) currentSorter);
    }

    private void createTableTotal() {
        applyColors(list.tableTotal);

        modelTotal = new AbstractTableModel() {
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    switch (rowIndex) {
                    case 0:
                        return OStrings.getString("GUI_PROJECT_TOTAL_SEGMENTS");
                    case 1:
                        return OStrings.getString("GUI_PROJECT_UNIQUE_SEGMENTS");
                    case 2:
                        return OStrings.getString("GUI_PROJECT_TRANSLATED");
                    }
                } else if (columnIndex == 1) {
                    return "";
                } else if (columnIndex == 2) {
                    return "";
                } else {
                    StatisticsInfo stat = Core.getProject().getStatistics();
                    switch (rowIndex) {
                    case 0:
                        return stat.numberOfSegmentsTotal;
                    case 1:
                        return stat.numberOfUniqueSegments;
                    case 2:
                        return stat.numberofTranslatedSegments;
                    }
                }
                return null;
            }

            public int getColumnCount() {
                return 4;
            }

            public int getRowCount() {
                return 3;
            }
        };
        list.tableTotal.setModel(modelTotal);

        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn cFile = new TableColumn(0, 150);
        cFile.setCellRenderer(new CustomRenderer(null, SwingConstants.LEFT, null));
        TableColumn cFilter = new TableColumn(1, 100);
        cFilter.setCellRenderer(new CustomRenderer(null, SwingConstants.LEFT, null));
        TableColumn cEncoding = new TableColumn(2, 50);
        cEncoding.setCellRenderer(new CustomRenderer(null, SwingConstants.LEFT, null));
        TableColumn cCount = new TableColumn(3, 50);
        cCount.setCellRenderer(new CustomRenderer(null, SwingConstants.RIGHT, ",##0"));
        columns.addColumn(cFile);
        columns.addColumn(cFilter);
        columns.addColumn(cEncoding);
        columns.addColumn(cCount);
        list.tableTotal.setColumnModel(columns);
    }

    void applyColors(JTable table) {
        table.setForeground(COLOR_STANDARD_FG);
        table.setBackground(COLOR_STANDARD_BG);
        table.setSelectionForeground(COLOR_SELECTION_FG);
        table.setSelectionBackground(COLOR_SELECTION_BG);
    }

    /**
     * Copy columns width from files to total table.
     */
    private void changeTotalColumns() {
        for (int i = 0; i < list.tableFiles.getColumnCount(); i++) {
            TableColumn f = list.tableFiles.getColumnModel().getColumn(i);
            TableColumn t = list.tableTotal.getColumnModel().getColumn(i);

            t.setMaxWidth(f.getWidth());
            t.setMinWidth(f.getWidth());
            t.setPreferredWidth(f.getWidth());
        }
    }

    /**
     * Imports the file/files/folder into project's source files.
     * 
     * @author Kim Bruning
     * @author Maxym Mykhalchuk
     */
    private void doImportSourceFiles() {
        m_parent.doImportSourceFiles();
    }

    private void doWikiImport() {
        m_parent.doWikiImport();
    }

    /** Updates the Import Files button status. */
    public void uiUpdateImportButtonStatus() {
        list.m_addNewFileButton.setEnabled(Core.getProject().isProjectLoaded());
        list.m_wikiImportButton.setEnabled(Core.getProject().isProjectLoaded());
    }

    private void gotoFile(int row) {
        int modelRow;
        try {
            modelRow = list.tableFiles.convertRowIndexToModel(row);
            Core.getProject().getProjectFiles().get(modelRow);
        } catch (IndexOutOfBoundsException ex) {
            // data changed
            return;
        }

        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = list.getCursor();
        list.setCursor(hourglassCursor);
        Core.getEditor().gotoFile(modelRow);
        Core.getEditor().requestFocus();
        list.setCursor(oldCursor);
    }

    /**
     * Render for table cells.
     */
    private class CustomRenderer extends DefaultTableCellRenderer {
        protected DecimalFormat pattern;
        private final List<IProject.FileInfo> files;

        public CustomRenderer(List<IProject.FileInfo> files, final int alignment, final String decimalPattern) {
            this.files = files;
            setHorizontalAlignment(alignment);
            if (decimalPattern != null) {
                pattern = new DecimalFormat(decimalPattern);
            }
        }

        protected void setValue(Object value) {
            if (pattern != null && value instanceof Number) {
                super.setValue(pattern.format((Number) value));
            } else {
                super.setValue(value);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (files != null) {
                IProject.FileInfo fi;
                int modelRow;
                try {
                    modelRow = list.tableFiles.convertRowIndexToModel(row);
                    fi = files.get(modelRow);
                } catch (IndexOutOfBoundsException ex) {
                    // data changed
                    fi = null;
                }

                if (isSelected) {
                    super.setForeground(table.getSelectionForeground());
                    super.setBackground(table.getSelectionBackground());
                } else {
                    super.setForeground(table.getForeground());
                    super.setBackground(table.getBackground());
                }
                if (fi != null && fi.filePath.equals(Core.getEditor().getCurrentFile())) {
                    result.setForeground(COLOR_CURRENT_FG);
                    result.setBackground(COLOR_CURRENT_BG);
                }
            }
            return result;
        }
    }

    public void setFont(Font f) {
        list.setFont(f);

        if (Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
            String fontName = Preferences.getPreference(OConsts.TF_SRC_FONT_NAME);
            int fontSize = Integer.valueOf(Preferences.getPreference(OConsts.TF_SRC_FONT_SIZE)).intValue();
            list.tableFiles.setFont(new Font(fontName, Font.PLAIN, fontSize));
            list.tableTotal.setFont(new Font(fontName, Font.BOLD, fontSize));
            list.tableFiles.setRowHeight(fontSize + LINE_SPACING);
            list.tableTotal.setRowHeight(fontSize + LINE_SPACING);
            list.statLabel.setFont(new Font(fontName, Font.PLAIN, fontSize));
        }
    }

    class Sorter extends RowSorter<IProject.FileInfo> {
        private final List<IProject.FileInfo> files;
        private SortKey sortKey = new SortKey(0, SortOrder.ASCENDING);
        private Integer[] modelToView;
        private List<Integer> viewToModel;

        public Sorter(final List<IProject.FileInfo> files) {
            this.files = files;
            modelToView = new Integer[files.size()];
            viewToModel = new ArrayList<Integer>(files.size());
            for (int i = 0; i < modelToView.length; i++) {
                viewToModel.add(i);
            }

            final List<String> filenames = new ArrayList<String>();
            for (IProject.FileInfo fi : files) {
                filenames.add(fi.filePath);
            }
            StaticUtils.sortByList(filenames, Core.getProject().getSourceFilesOrder());
            Collections.sort(viewToModel, new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    int pos1 = filenames.indexOf(files.get(o1).filePath);
                    int pos2 = filenames.indexOf(files.get(o2).filePath);
                    if (pos1 < pos2) {
                        return -1;
                    } else if (pos1 > pos2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            recalc();
        }

        @Override
        public int getModelRowCount() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public int getViewRowCount() {
            return files.size();
        }

        @Override
        public FileInfo getModel() {
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
            List<SortKey> r = new ArrayList<RowSorter.SortKey>();
            r.add(sortKey);
            return r;
        }

        @Override
        public void setSortKeys(List<? extends javax.swing.RowSorter.SortKey> keys) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void toggleSortOrder(int column) {
            if (sortKey.getColumn() == column) {
                switch (sortKey.getSortOrder()) {
                case ASCENDING:
                    sortKey = new SortKey(sortKey.getColumn(), SortOrder.DESCENDING);
                    break;
                case DESCENDING:
                default:
                    sortKey = new SortKey(sortKey.getColumn(), SortOrder.ASCENDING);
                    break;
                }
            } else {
                sortKey = new SortKey(column, SortOrder.ASCENDING);
            }
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
            for (int i = 0; i < viewToModel.size(); i++) {
                viewToModel.set(i, i);
            }
            Collections.sort(viewToModel, new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
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
                        c = f1.fileEncoding.compareToIgnoreCase(f2.fileEncoding);
                        break;
                    case 3:
                        c = new Integer(f1.entries.size()).compareTo(new Integer(f2.entries.size()));
                        break;
                    }
                    if (sortKey.getSortOrder() == SortOrder.DESCENDING) {
                        c = -c;
                    }
                    return c;
                }
            });
            recalc();
        }

        private void recalc() {
            for (int i = 0; i < viewToModel.size(); i++) {
                modelToView[viewToModel.get(i)] = i;
            }
        }

        public int moveTo(int currentPos, int newPos) {
            newPos = Math.max(newPos, 0);
            newPos = Math.min(newPos, viewToModel.size() - 1);

            int n = viewToModel.remove(currentPos);
            viewToModel.add(newPos, n);
            recalc();
            save();
            list.tableFiles.repaint();
            return newPos;
        }

        private void save() {
            List<String> filenames = new ArrayList<String>();
            for (int i = 0; i < viewToModel.size(); i++) {
                String fn = files.get(viewToModel.get(i)).filePath;
                filenames.add(fn);
            }
            Core.getProject().setSourceFilesOrder(filenames);
        }
    }
}
