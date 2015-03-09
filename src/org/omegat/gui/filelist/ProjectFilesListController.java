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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;

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
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.StaticUIUtils;
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
    private static final Color COLOR_STANDARD_FG = Color.BLACK;
    private static final Color COLOR_STANDARD_BG = Color.WHITE;
    private static final Color COLOR_CURRENT_FG = Color.BLACK;
    private static final Color COLOR_CURRENT_BG = new Color(0xC8DDF2);
    private static final Color COLOR_SELECTION_FG = Color.WHITE;
    private static final Color COLOR_SELECTION_BG = new Color(0x2F77DA);
    private static final Color COLOR_ALTERNATING_HILITE = new Color(245, 245, 245);
    private static final Border TABLE_FOCUS_BORDER = new MatteBorder(1, 1, 1, 1, new Color(0x76AFE8));

    private static final int LINE_SPACING = 6;

    private ProjectFilesList list;
    private AbstractTableModel modelFiles, modelTotal;
    private Sorter currentSorter;

    private TableFilterPanel filterPanel;
    
    private final MainWindow m_parent;

    private Font defaultFont;
    
    private int[] optimalColWidths;
    private int cutoverWidth = -1;
    private boolean didManuallyAdjustCols;

    public ProjectFilesListController(MainWindow parent) {
        m_parent = parent;

        list = new ProjectFilesList();

        createTableFiles();
        createTableTotal();
        
        defaultFont = list.tableFiles.getFont();
        if (Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
            String fontName = Preferences.getPreference(OConsts.TF_SRC_FONT_NAME);
            int fontSize = Integer.parseInt(Preferences.getPreference(OConsts.TF_SRC_FONT_SIZE));
            setFont(new Font(fontName, Font.PLAIN, fontSize));
        } else {
            setFont(defaultFont);
        }

        list.tablesInnerPanel.setBorder(new JScrollPane().getBorder());
        
        if (!Platform.isMacOSX()) {
            // Windows needs some extra colors set for consistency, but these
            // ruin native LAF on OS X.
            list.scrollFiles.getViewport().setBackground(COLOR_STANDARD_BG);
            list.scrollFiles.setBackground(COLOR_STANDARD_BG);
            list.tableFiles.getTableHeader().setBackground(COLOR_STANDARD_BG);
        }
        list.scrollFiles.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (modelFiles == null) {
                    return;
                }
                adjustTableColumns();
            }
        });
        
        // set the position and size
        initWindowLayout();

        list.m_addNewFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doImportSourceFiles();
            }
        });
        list.m_wikiImportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doWikiImport();
            }
        });
        list.m_closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });
        list.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                doCancel();
            }
            @Override
            public void windowActivated(WindowEvent e) {
                adjustTableColumns();
            }
        });

        StaticUIUtils.setEscapeAction(list, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            @Override
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                switch (eventType) {
                case CLOSE:
                    list.setVisible(false);
                    break;
                case LOAD:
                case CREATE:
                    buildDisplay(Core.getProject().getProjectFiles());
                    if (!Preferences.isPreferenceDefault(Preferences.PROJECT_FILES_SHOW_ON_LOAD, true)) {
                        break;
                    }
                    list.setVisible(true);
                    list.tableFiles.requestFocus();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            list.toFront();
                        }
                    });
                    break;
                }
            }
        });

        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            @Override
            public void onNewFile(String activeFileName) {
                resetColWidthData();
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

        CoreEvents.registerFontChangedEventListener(new IFontChangedEventListener() {
            @Override
            public void onFontChanged(Font newFont) {
                if (!Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
                    newFont = defaultFont;
                }
                setFont(newFont);
                adjustTableColumns();
            }
        });

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public void onApplicationStartup() {
            }

            @Override
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
                } else if (filterPanel != null && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    endFilter();
                    e.consume();
                }
            }
        });
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
    
    private final KeyListener filterTrigger = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if ((e.getModifiers() == 0 || e.getModifiers() == KeyEvent.SHIFT_MASK)
                    && !Character.isWhitespace(c) && !Character.isISOControl(c)) {
                if (filterPanel == null) {
                    startFilter(e.getKeyChar());
                } else {
                    resumeFilter(e.getKeyChar());
                }
                e.consume();
            }
        }
    };
    
    private void startFilter(char c) {
        if (filterPanel != null) {
            return;
        }
        filterPanel = new TableFilterPanel();
        list.btnDown.setEnabled(false);
        list.btnUp.setEnabled(false);
        list.btnFirst.setEnabled(false);
        list.btnLast.setEnabled(false);
        list.tablesOuterPanel.add(filterPanel, BorderLayout.SOUTH);
        filterPanel.filterTextField.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gotoFile(list.tableFiles.getSelectedRow());
            }
        });
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
        filterPanel.filterCloseButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                endFilter();
            }
        });
        filterPanel.filterTextField.setText(Character.toString(c));
        filterPanel.filterTextField.requestFocus();
        list.validate();
        list.repaint();
    }
    
    private void resumeFilter(char c) {
        if (filterPanel == null) {
            return;
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
        if (filterPanel == null) {
            return;
        }
        String regex = ".*" + Pattern.quote(filterPanel.filterTextField.getText()) + ".*";
        currentSorter.setFilter(regex);
        selectRow(0);
    }

    private void endFilter() {
        if (filterPanel == null) {
            return;
        }
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
    
    ActionListener moveAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
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
        }
    };

    public boolean isActive() {
        return list.isActive();
    }

    public void setActive(boolean active) {
        if (active) {
            // moved current file selection here so it will be properly set on each activation
            selectCurrentFile(Core.getProject().getProjectFiles());
            list.setVisible(true);
            list.toFront();
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

        uiUpdateImportButtonStatus();
        list.setTitle(StaticUtils.format(OStrings.getString("PF_WINDOW_TITLE"), files.size()));

        setTableFilesModel(files);
        
        resetColWidthData();
    }

    private void createTableFiles() {
        applyColors(list.tableFiles);
        list.tableFiles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    /**
     * Calculate each column's ideal width, based on header and cells.
     * Results are cached.
     */
    private void calculateOptimalColWidths() {
        if (optimalColWidths != null) {
            return;
        }
        optimalColWidths = new int[list.tableFiles.getColumnCount()];
        
        // See: https://tips4java.wordpress.com/2008/11/10/table-column-adjuster/
        for (int column = 0; column < list.tableFiles.getColumnCount(); column++) {
            TableColumn col = list.tableFiles.getColumnModel().getColumn(column);
            int preferredWidth = col.getMinWidth();
            int maxWidth = col.getMaxWidth();

            for (int row = -1; row < list.tableFiles.getRowCount(); row++) {
                TableCellRenderer cellRenderer;
                Component c;
                int margin = 5;
                if (row == -1) {
                    cellRenderer = col.getHeaderRenderer();
                    if (cellRenderer == null) {
                        cellRenderer = list.tableFiles.getDefaultRenderer(col.getClass());
                    }
                    c = cellRenderer.getTableCellRendererComponent(list.tableFiles, col.getHeaderValue(), false, false, 0, column);
                    // Add somewhat arbitrary margin to header because it gets truncated at a smaller width
                    // than a regular cell does (Windows LAF more than OS X LAF).
                    margin = 10;
                } else {
                    cellRenderer = list.tableFiles.getCellRenderer(row, column);
                    c = list.tableFiles.prepareRenderer(cellRenderer, row, column);
                }
                
                c.setBounds(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
                int width = c.getPreferredSize().width + list.tableFiles.getIntercellSpacing().width + margin;
                preferredWidth = Math.max(preferredWidth, width);

                //  We've exceeded the maximum width, no need to check other rows
                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }
            optimalColWidths[column] = preferredWidth;
        }
    }
    
    private void resetColWidthData() {
        optimalColWidths = null;
        cutoverWidth = -1;
    }
    
    /**
     * Adjust the columns of the tables, propagating widths from tableFiles to
     * tableTotal.
     * 
     * If possible, this optimally sizes the columns such that columns greater
     * than 0 are only as big as necessary, and the rest of the space goes to
     * column 0.
     * 
     * This auto-sizing only happens if it represents an improvement over the
     * default sizing (gives more space to column 0), and only if the user has
     * not manually adjusted column widths.
     * 
     * Once auto-sizing is invoked, the width at which it was first invoked is
     * recorded as a boundary below which default sizing is used again.
     */
    private void adjustTableColumns() {
        // Set last column of tableTotal to match size of scrollbar.
        JScrollBar scrollbar = list.scrollFiles.getVerticalScrollBar();
        int sbWidth = scrollbar == null || !scrollbar.isVisible() ? 0 : scrollbar.getWidth();
        list.tableTotal.getColumnModel().getColumn(list.tableTotal.getColumnCount() - 1).setPreferredWidth(sbWidth);
        
        calculateOptimalColWidths();
        
        int otherCols = 0;
        for (int i = 1; i < optimalColWidths.length; i++) {
            otherCols += optimalColWidths[i];
        }
        
        int remainderFirstColWidth = list.scrollFiles.getViewport().getWidth() - otherCols;
                
        if (shouldAutoSize(remainderFirstColWidth)) {
            if (cutoverWidth == -1) {
                cutoverWidth = list.scrollFiles.getViewport().getWidth();
            }
            list.tableFiles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            list.tableFiles.getColumnModel().getColumn(0).setPreferredWidth(remainderFirstColWidth);
            list.tableTotal.getColumnModel().getColumn(0).setPreferredWidth(remainderFirstColWidth);
            for (int i = 1; i < optimalColWidths.length; i++) {
                list.tableFiles.getColumnModel().getColumn(i).setPreferredWidth(optimalColWidths[i]);
                list.tableTotal.getColumnModel().getColumn(i).setPreferredWidth(optimalColWidths[i]);
            }
        } else {
            list.tableFiles.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            for (int i = 0; i < list.tableFiles.getColumnCount(); i++) {
                TableColumn srcCol = list.tableFiles.getColumnModel().getColumn(i);
                TableColumn trgCol = list.tableTotal.getColumnModel().getColumn(i);
                trgCol.setPreferredWidth(srcCol.getWidth());
            }
        }
    }
    
    private boolean shouldAutoSize(int proposedFirstColWidth) {
        if (didManuallyAdjustCols) {
            return false;
        }
        if (proposedFirstColWidth > optimalColWidths[0]) {
            return true;
        }
        if (cutoverWidth != -1) {
            return list.scrollFiles.getViewport().getWidth() >= cutoverWidth;
        }
        return proposedFirstColWidth > list.tableFiles.getColumnModel().getColumn(0).getWidth();
    }

    private void setTableFilesModel(final List<IProject.FileInfo> files) {
        modelFiles = new AbstractTableModel() {
            @Override
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
                case 4:
                    StatisticsInfo stat = Core.getProject().getStatistics();
                    return stat.uniqueCountsByFile.get(fi.filePath);
                default:
                    return null;
                }
            }

            @Override
            public int getColumnCount() {
                return 5;
            }

            @Override
            public int getRowCount() {
                return files.size();
            }

            @Override
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
                case 4:
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
        TableColumn cUnique = new TableColumn(4, 50);
        cUnique.setHeaderValue(OStrings.getString("PF_NUM_UNIQUE_SEGMENTS"));
        cUnique.setCellRenderer(new CustomRenderer(files, SwingConstants.RIGHT, ",##0"));
        columns.addColumn(cFile);
        columns.addColumn(cFilter);
        columns.addColumn(cEncoding);
        columns.addColumn(cCount);
        columns.addColumn(cUnique);
        columns.addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                TableColumn col = list.tableFiles.getTableHeader().getResizingColumn();
                if (col != null) {
                    // User has manually resized a column. Don't try auto-sizing.
                    didManuallyAdjustCols = true;
                    adjustTableColumns();
                }
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                list.tableTotal.getColumnModel().moveColumn(e.getFromIndex(), e.getToIndex());
                adjustTableColumns();
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });
        list.tableFiles.setColumnModel(columns);

        currentSorter = new Sorter(files);
        list.tableFiles.setRowSorter((RowSorter) currentSorter);
    }

    private void createTableTotal() {
        applyColors(list.tableTotal);
        list.tableTotal.setBorder(new MatteBorder(1, 0, 0, 0, COLOR_ALTERNATING_HILITE));

        modelTotal = new AbstractTableModel() {
            @Override
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
                } else if (columnIndex == 3) {
                    return "";
                } else if (columnIndex == 4) {
                    StatisticsInfo stat = Core.getProject().getStatistics();
                    switch (rowIndex) {
                    case 0:
                        return stat.numberOfSegmentsTotal;
                    case 1:
                        return stat.numberOfUniqueSegments;
                    case 2:
                        return stat.numberofTranslatedSegments;
                    }
                } else if (columnIndex == 5) {
                    return "";
                }
                return null;
            }

            @Override
            public int getColumnCount() {
                return 6;
            }

            @Override
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
        TableColumn cUnique = new TableColumn(4, 50);
        cUnique.setCellRenderer(new CustomRenderer(null, SwingConstants.RIGHT, ",##0"));
        TableColumn cScrollbarMargin = new TableColumn(5, 0);
        cScrollbarMargin.setCellRenderer(new CustomRenderer(null, SwingConstants.LEFT, null, false));
        columns.addColumn(cFile);
        columns.addColumn(cFilter);
        columns.addColumn(cEncoding);
        columns.addColumn(cCount);
        columns.addColumn(cUnique);
        columns.addColumn(cScrollbarMargin);
        list.tableTotal.setColumnModel(columns);
    }

    void applyColors(JTable table) {
        table.setForeground(COLOR_STANDARD_FG);
        table.setBackground(COLOR_STANDARD_BG);
        table.setSelectionForeground(COLOR_SELECTION_FG);
        table.setSelectionBackground(COLOR_SELECTION_BG);
        table.setGridColor(COLOR_STANDARD_BG);
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
        private final boolean doHighlight;

        public CustomRenderer(List<IProject.FileInfo> files, final int alignment, final String decimalPattern) {
            this(files, alignment, decimalPattern, true);
        }
        
        public CustomRenderer(List<IProject.FileInfo> files, final int alignment, final String decimalPattern,
                boolean doHighlight) {
            this.files = files;
            setHorizontalAlignment(alignment);
            if (decimalPattern != null) {
                pattern = new DecimalFormat(decimalPattern);
            }
            this.doHighlight = doHighlight;
        }

        @Override
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
            if (isSelected) {
                result.setForeground(table.getSelectionForeground());
                result.setBackground(table.getSelectionBackground());
            } else if (isCurrentFile(row)) {
                result.setForeground(COLOR_CURRENT_FG);
                result.setBackground(COLOR_CURRENT_BG);
            } else if (row % 2 == 1 && doHighlight) {
                result.setForeground(table.getForeground());
                result.setBackground(COLOR_ALTERNATING_HILITE);
            } else {
                result.setForeground(table.getForeground());
                result.setBackground(table.getBackground());
            }
            if (hasFocus && result instanceof JComponent) {
                ((JComponent) result).setBorder(TABLE_FOCUS_BORDER);
            }
            return result;
        }
        
        private boolean isCurrentFile(int row) {
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
        list.tableFiles.setFont(font);
        list.tableTotal.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
        list.tableFiles.setRowHeight(font.getSize() + LINE_SPACING);
        list.tableTotal.setRowHeight(font.getSize() + LINE_SPACING);
        list.statLabel.setFont(font);
        resetColWidthData();
    }

    class Sorter extends RowSorter<IProject.FileInfo> {
        private final List<IProject.FileInfo> files;
        private SortKey sortKey = new SortKey(0, SortOrder.UNSORTED);
        private Integer[] modelToView;
        private List<Integer> viewToModel;
        private Pattern filter;

        public Sorter(final List<IProject.FileInfo> files) {
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
            final List<String> filenames = new ArrayList<String>();
            for (IProject.FileInfo fi : files) {
                filenames.add(fi.filePath);
            }
            StaticUtils.sortByList(filenames, Core.getProject().getSourceFilesOrder());
            Collections.sort(viewToModel, new Comparator<Integer>() {
                @Override
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
            return viewToModel.size();
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
            Collections.sort(viewToModel, new Comparator<Integer>() {
                @Override
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
                        String fe1 = f1.fileEncoding == null ? "" : f1.fileEncoding;
                        String fe2 = f2.fileEncoding == null ? "" : f2.fileEncoding;
                        c = fe1.compareToIgnoreCase(fe2);
                        break;
                    case 3:
                        c = new Integer(f1.entries.size()).compareTo(f2.entries.size());
                        break;
                    case 4:
                        int n1 = stat.uniqueCountsByFile.get(f1.filePath);
                        int n2 = stat.uniqueCountsByFile.get(f2.filePath);
                        c = new Integer(n1).compareTo(n2);
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

        public int moveTo(int[] selected, int newPos) {
            int[] temp = new int[selected.length];
            int n = selected.length;
            for(int i = 0; i < selected.length; i++) {
                temp[i] = viewToModel.remove(selected[--n]);
            }

            newPos = Math.max(newPos, 0);
            newPos = Math.min(newPos, viewToModel.size());

            for(int i = 0; i < temp.length; i++) {
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
        
        public void setFilter(String regex) {
            if (filter == null && regex == null) {
                return;
            }
            Pattern newFilter;
            try {
                newFilter = regex == null ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException ex) {
                return;
            }
            if (filter != null && filter.equals(newFilter)) {
                return;
            }
            filter = newFilter;
            int[] lastViewToModel = getIntArrayFromIntegerList(viewToModel);
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
        
        private int[] getIntArrayFromIntegerList(List<Integer> list) {
            int[] result = new int[list.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = list.get(i);
            }
            return result;
        }
    }
}
