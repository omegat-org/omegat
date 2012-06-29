/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Kim Bruning
               2007 Zoltan Bartko
               2008 Alex Buloichik, Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
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
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
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
import org.omegat.util.gui.UIThreadsUtil;
import org.openide.awt.Mnemonics;

/**
 * A frame for project, showing all the files of the project.
 * 
 * Object doesn't have any synchronization, because it just get one object (List
 * files) from DataEngine. Instead, it check IndexOutOfBoundException when get
 * data from this object.
 * 
 * @author Keith Godfrey
 * @author Kim Bruning
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
@SuppressWarnings("serial")
public class ProjectFrame extends JFrame {

    private static final Color COLOR_STANDARD_FG = Color.BLACK;
    private static final Color COLOR_STANDARD_BG = Color.WHITE;
    private static final Color COLOR_CURRENT_FG = Color.BLACK;
    private static final Color COLOR_CURRENT_BG = new Color(0xC8DDF2);
    private static final Color COLOR_SELECTION_FG = Color.WHITE;
    private static final Color COLOR_SELECTION_BG = new Color(0x2F77DA);

    private static final int LINE_SPACING = 6;

    private JTable tableFiles, tableTotal;
    private JScrollPane scrollFiles;
    private AbstractTableModel modelFiles, modelTotal;
    private List<IProject.FileInfo> files;

    private JTextArea statLabel;
    private JButton m_addNewFileButton;
    private JButton m_wikiImportButton;
    private JButton m_closeButton;

    private MainWindow m_parent;

    private Font dialogFont;

    public ProjectFrame(MainWindow parent) {
        m_parent = parent;

        createTableFiles();
        dialogFont = tableFiles.getFont();
        createTableTotal();

        // set the position and size
        initWindowLayout();

        JPanel cp = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        scrollFiles = new JScrollPane(tableFiles, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gbc.gridy = 0;
        gbc.weighty = 1;
        cp.add(scrollFiles, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;
        cp.add(tableTotal, gbc);

        cp.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setContentPane(cp);

        m_addNewFileButton = new JButton();
        Mnemonics.setLocalizedText(m_addNewFileButton, OStrings.getString("TF_MENU_FILE_IMPORT"));
        m_addNewFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doImportSourceFiles();
            }
        });
        m_wikiImportButton = new JButton();
        Mnemonics.setLocalizedText(m_wikiImportButton, OStrings.getString("TF_MENU_WIKI_IMPORT"));
        m_wikiImportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doWikiImport();
            }
        });

        // Configure close button
        m_closeButton = new JButton();
        m_closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        Mnemonics.setLocalizedText(m_closeButton, OStrings.getString("BUTTON_CLOSE"));
        setTitle(OStrings.getString("PF_WINDOW_TITLE"));

        statLabel = new JTextArea();
        statLabel.setFont(getFont());
        statLabel.setEditable(false);
        statLabel.setLineWrap(true);
        statLabel.setBackground(getBackground());
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 0, 0);
        cp.add(statLabel, gbc);

        Box bbut = Box.createHorizontalBox();
        bbut.add(Box.createHorizontalGlue());
        bbut.add(m_addNewFileButton);
        bbut.add(m_wikiImportButton);
        bbut.add(m_closeButton);
        bbut.add(Box.createHorizontalGlue());
        bbut.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        gbc.gridy = 6;
        cp.add(bbut, gbc);

        // Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // setBounds((screenSize.width-600)/2, (screenSize.height-500)/2, 600,
        // 400);

        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                switch (eventType) {
                case CLOSE:
                    setVisible(false);
                    break;
                case LOAD:
                case CREATE:
                    buildDisplay();
                    setVisible(true);
                    tableFiles.requestFocus();
                    buildTotalTableLayout();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            toFront();
                        }
                    });
                    break;
                }
            }
        });

        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            public void onNewFile(String activeFileName) {
                tableFiles.repaint();
                tableTotal.repaint();
                modelTotal.fireTableDataChanged();
            }

            /**
             * Updates the number of translated segments only, does not rebuild
             * the whole display.
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
                tableFiles.setFont(newFont);
                tableTotal.setFont(new Font(newFont.getName(), Font.BOLD, newFont.getSize()));
                tableFiles.setRowHeight(newFont.getSize() + LINE_SPACING);
                tableTotal.setRowHeight(newFont.getSize() + LINE_SPACING);
            }
        });

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
            }

            public void onApplicationShutdown() {
                saveWindowLayout();
            }
        });

        tableFiles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gotoFile(tableFiles.rowAtPoint(e.getPoint()));
            }
        });
        tableFiles.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    gotoFile(tableFiles.getSelectedRow());
                    e.consume();
                }
            }
        });
    }

    /**
     * Loads/sets the position and size of the search window.
     */
    private void initWindowLayout() {
        // main window
        try {
            String dx = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_X);
            String dy = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            setLocation(x, y);
            String dw = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_WIDTH);
            String dh = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            setSize(w, h);
        } catch (NumberFormatException nfe) {
            // set default size and position
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setBounds((screenSize.width - 600) / 2, (screenSize.height - 400) / 2, 600, 400);
        }
    }

    /**
     * Saves the size and position of the search window
     */
    private void saveWindowLayout() {
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_WIDTH, getWidth());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_HEIGHT, getHeight());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_X, getX());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_Y, getY());
    }

    private void doCancel() {
        setVisible(false);
    }

    /**
     * Builds the table which lists all the project files.
     */
    public void buildDisplay() {
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
        statLabel.setText(statText);

        files = Core.getProject().getProjectFiles();
        modelFiles.fireTableDataChanged();

        String currentFile = Core.getEditor().getCurrentFile();
        // need to copy to local vars against threads synchronization problems
        final List<IProject.FileInfo> fs = files;
        // set current file as default selection
        for (int i = 0; i < fs.size(); i++) {
            if (fs.get(i).filePath.equals(currentFile)) {
                // set selection to currently edited file
                tableFiles.getSelectionModel().setSelectionInterval(i, i);
                // set current file visible in scroller
                tableFiles.scrollRectToVisible(tableFiles.getCellRect(i, 0, true));
                break;
            }
        }

        uiUpdateImportButtonStatus();
    }

    private void buildTotalTableLayout() {
        scrollFiles.setBorder(BorderFactory.createEmptyBorder());
        Border b2 = scrollFiles.getBorder();
        Insets i1 = b2.getBorderInsets(tableFiles);
        int sc = scrollFiles.getVerticalScrollBar().isVisible() ? scrollFiles.getVerticalScrollBar()
                .getWidth() : 0;

        GridBagLayout ly = (GridBagLayout) getContentPane().getLayout();
        GridBagConstraints c = ly.getConstraints(tableTotal);
        c.insets = new Insets(0, i1.left, 0, sc);
        ly.setConstraints(tableTotal, c);
    }

    private void createTableFiles() {
        tableFiles = new JTable();
        tableFiles.setForeground(COLOR_STANDARD_FG);
        tableFiles.setBackground(COLOR_STANDARD_BG);
        tableFiles.setSelectionForeground(COLOR_SELECTION_FG);
        tableFiles.setSelectionBackground(COLOR_SELECTION_BG);

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
                    return fi.entries.size();
                default:
                    return null;
                }
            }

            public int getColumnCount() {
                return 2;
            }

            public int getRowCount() {
                return files.size();
            }
        };
        tableFiles.setModel(modelFiles);

        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn cFile = new TableColumn(0, 300);
        cFile.setHeaderValue(OStrings.getString("PF_FILENAME"));
        cFile.setCellRenderer(new CustomRenderer(SwingConstants.LEFT, null, true));
        TableColumn cCount = new TableColumn(1, 50);
        cCount.setHeaderValue(OStrings.getString("PF_NUM_SEGMENTS"));
        cCount.setCellRenderer(new CustomRenderer(SwingConstants.RIGHT, ",##0", true));
        columns.addColumn(cFile);
        columns.addColumn(cCount);
        tableFiles.setColumnModel(columns);

        tableFiles.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    }

    private void createTableTotal() {
        tableTotal = new JTable();
        tableTotal.setForeground(COLOR_STANDARD_FG);
        tableTotal.setBackground(COLOR_STANDARD_BG);
        tableTotal.setSelectionForeground(COLOR_SELECTION_FG);
        tableTotal.setSelectionBackground(COLOR_SELECTION_BG);

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
                return 2;
            }

            public int getRowCount() {
                return 3;
            }
        };
        tableTotal.setModel(modelTotal);

        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn cFile = new TableColumn(0, 300);
        cFile.setCellRenderer(new CustomRenderer(SwingConstants.LEFT, null, false));
        TableColumn cCount = new TableColumn(1, 50);
        cCount.setCellRenderer(new CustomRenderer(SwingConstants.RIGHT, ",##0", false));
        columns.addColumn(cFile);
        columns.addColumn(cCount);
        tableTotal.setColumnModel(columns);

        tableTotal.setBorder(BorderFactory.createEmptyBorder(50, 5, 10, 5));
        tableFiles.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            public void columnAdded(TableColumnModelEvent e) {
            }

            public void columnMarginChanged(ChangeEvent e) {
                changeTotalColumns();
            }

            public void columnMoved(TableColumnModelEvent e) {
                tableTotal.getColumnModel().moveColumn(e.getFromIndex(), e.getToIndex());
                changeTotalColumns();
            }

            public void columnRemoved(TableColumnModelEvent e) {
            }

            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });
        tableFiles.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                changeTotalColumns();
            }
        });
    }

    /**
     * Copy columns width from files to total table.
     */
    private void changeTotalColumns() {
        for (int i = 0; i < tableFiles.getColumnCount(); i++) {
            TableColumn f = tableFiles.getColumnModel().getColumn(i);
            TableColumn t = tableTotal.getColumnModel().getColumn(i);

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
        m_addNewFileButton.setEnabled(Core.getProject().isProjectLoaded());
        m_wikiImportButton.setEnabled(Core.getProject().isProjectLoaded());
    }

    private void gotoFile(int row) {
        try {
            files.get(row);
        } catch (IndexOutOfBoundsException ex) {
            // data changed
            return;
        }
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor oldCursor = this.getCursor();
        this.setCursor(hourglassCursor);
        Core.getEditor().gotoFile(row);
        Core.getEditor().requestFocus();
        this.setCursor(oldCursor);
    }

    /**
     * Render for table cells.
     */
    private class CustomRenderer extends DefaultTableCellRenderer {
        protected DecimalFormat pattern;
        private boolean showCurrentFile;

        public CustomRenderer(final int alignment, final String decimalPattern, final boolean showCurrentFile) {
            setHorizontalAlignment(alignment);
            this.showCurrentFile = showCurrentFile;
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
            if (showCurrentFile) {
                IProject.FileInfo fi;
                try {
                    fi = files.get(row);
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

    @Override
    public void setFont(Font f) {
        super.setFont(f);

        if (Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
            String fontName = Preferences.getPreference(OConsts.TF_SRC_FONT_NAME);
            int fontSize = Integer.valueOf(Preferences.getPreference(OConsts.TF_SRC_FONT_SIZE)).intValue();
            tableFiles.setFont(new Font(fontName, Font.PLAIN, fontSize));
            tableTotal.setFont(new Font(fontName, Font.BOLD, fontSize));
            tableFiles.setRowHeight(fontSize + LINE_SPACING);
            tableTotal.setRowHeight(fontSize + LINE_SPACING);
        }
    }
}
