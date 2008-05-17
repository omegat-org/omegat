/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Kim Bruning
           (C) 2007 Zoltan Bartko
               2008 Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.filelist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
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
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.CommandThread;
import org.omegat.core.data.IDataEngine;
import org.omegat.core.data.StringEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.main.MainWindow;
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
 */
public class ProjectFrame extends JFrame {

    private static final Color CURRENT_FILE_COLOR = new Color(0xC8DDF2);
    private static final int LINE_SPACING = 16;

    private JTable tableFiles, tableTotal;
    private AbstractTableModel modelFiles, modelTotal;
    private List<IDataEngine.FileInfo> files;

    private JButton m_addNewFileButton;
    private JButton m_wikiImportButton;
    private JButton m_closeButton;

    private MainWindow m_parent;

    public ProjectFrame(MainWindow parent) {
        m_parent = parent;

        tableFiles = createTableFiles();
        tableTotal = createTableTotal();

        // set the position and size
        initWindowLayout();

        Container cp = getContentPane();
        cp.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        JScrollPane scroll = new JScrollPane(tableFiles,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gbc.gridy = 0;
        gbc.weighty = 1;
        cp.add(scroll, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0;
        JPanel sep = new JPanel();
        cp.add(sep, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;
        cp.add(tableTotal, gbc);

        gbc.gridy = 3;
        gbc.weighty = 0;
        JPanel sep2 = new JPanel();
        cp.add(sep2, gbc);

        m_addNewFileButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(m_addNewFileButton, OStrings
                .getString("TF_MENU_FILE_IMPORT"));
        m_addNewFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doImportSourceFiles();
            }
        });
        m_wikiImportButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(m_wikiImportButton, OStrings
                .getString("TF_MENU_WIKI_IMPORT"));
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
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                escape, "ESCAPE"); // NOI18N
        getRootPane().getActionMap().put("ESCAPE", escapeAction); // NOI18N

        Box bbut = Box.createHorizontalBox();
        bbut.add(Box.createHorizontalGlue());
        bbut.add(m_addNewFileButton);
        bbut.add(m_wikiImportButton);
        bbut.add(m_closeButton);
        bbut.add(Box.createHorizontalGlue());
        gbc.gridy = 5;
        cp.add(bbut, gbc); // NOI18N

        Mnemonics.setLocalizedText(m_closeButton, OStrings
                .getString("BUTTON_CLOSE"));
        setTitle(OStrings.getString("PF_WINDOW_TITLE"));

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
                    buildDisplay();
                    setVisible(true);
                    toFront();
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
            public void onEntryActivated(StringEntry newEntry) {
                UIThreadsUtil.mustBeSwingThread();
                modelTotal.fireTableDataChanged();
            }
        });
        
        CoreEvents
                .registerFontChangedEventListener(new IFontChangedEventListener() {
                    public void onFontChanged(Font newFont) {
                        ProjectFrame.this.setFont(newFont);
                        tableFiles.setFont(newFont);
                        tableTotal.setFont(new Font(newFont.getName(),
                                Font.BOLD, newFont.getSize()));
                        tableFiles.setRowHeight(newFont.getSize()
                                + LINE_SPACING);
                        tableTotal.setRowHeight(newFont.getSize()
                                + LINE_SPACING);
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
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    gotoFile(tableFiles.getSelectedRow());
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
            String dx = Preferences
                    .getPreference(Preferences.PROJECT_FILES_WINDOW_X);
            String dy = Preferences
                    .getPreference(Preferences.PROJECT_FILES_WINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            setLocation(x, y);
            String dw = Preferences
                    .getPreference(Preferences.PROJECT_FILES_WINDOW_WIDTH);
            String dh = Preferences
                    .getPreference(Preferences.PROJECT_FILES_WINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            setSize(w, h);
        } catch (NumberFormatException nfe) {
            // set default size and position
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setBounds((screenSize.width - 600) / 2,
                    (screenSize.height - 400) / 2, 600, 400);
        }
    }

    /**
     * Saves the size and position of the search window
     */
    private void saveWindowLayout() {
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_WIDTH,
                getWidth());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_HEIGHT,
                getHeight());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_X, getX());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_Y, getY());
    }

    public void processWindowEvent(WindowEvent w) {
        int evt = w.getID();
        if (evt == WindowEvent.WINDOW_CLOSING
                || evt == WindowEvent.WINDOW_CLOSED) {
            // save window size and position
            saveWindowLayout();
        }
        super.processWindowEvent(w);
    }

    private void doCancel() {
        setVisible(false);
    }

    /**
     * Builds the table which lists all the project files.
     */
    public void buildDisplay() {
        UIThreadsUtil.mustBeSwingThread();

        files = Core.getDataEngine().getProjectFiles();
        modelFiles.fireTableDataChanged();

        uiUpdateImportButtonStatus();
    }

    private JTable createTableFiles() {
        final JTable result = new JTable();
        modelFiles = new AbstractTableModel() {
            public Object getValueAt(int rowIndex, int columnIndex) {
                IDataEngine.FileInfo fi;
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
                    return fi.size;
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
        result.setModel(modelFiles);

        result.setSelectionBackground(result.getBackground());
        result.setSelectionForeground(result.getForeground());

        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn cFile = new TableColumn(0, 200);
        cFile.setHeaderValue(OStrings.getString("PF_FILENAME"));
        cFile.setCellRenderer(new CustomRenderer(SwingConstants.LEFT, null,
                true));
        TableColumn cCount = new TableColumn(1, 50);
        cCount.setHeaderValue(OStrings.getString("PF_NUM_SEGMENTS"));
        cCount.setCellRenderer(new CustomRenderer(SwingConstants.RIGHT, ",##0",
                true));
        columns.addColumn(cFile);
        columns.addColumn(cCount);
        result.setColumnModel(columns);

        result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return result;
    }

    private JTable createTableTotal() {
        final JTable result = new JTable();
        modelTotal = new AbstractTableModel() {
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    switch (rowIndex) {
                    case 0:
                        return OStrings.getString("GUI_PROJECT_TOTAL_SEGMENTS");
                    case 1:
                        return OStrings
                                .getString("GUI_PROJECT_UNIQUE_SEGMENTS");
                    case 2:
                        return OStrings.getString("GUI_PROJECT_TRANSLATED");
                    }
                } else {
                    switch (rowIndex) {
                    case 0:
                        return CommandThread.core.getNumberOfSegmentsTotal();
                    case 1:
                        return CommandThread.core.getNumberOfUniqueSegments();
                    case 2:
                        return CommandThread.core
                                .getNumberofTranslatedSegments();
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
        result.setModel(modelTotal);

        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn cFile = new TableColumn(0, 200);
        cFile.setCellRenderer(new CustomRenderer(SwingConstants.RIGHT, null,
                false));
        TableColumn cCount = new TableColumn(1, 50);
        cCount.setCellRenderer(new CustomRenderer(SwingConstants.RIGHT, ",##0",
                false));
        columns.addColumn(cFile);
        columns.addColumn(cCount);
        result.setColumnModel(columns);

        result.setEnabled(false);
        // result.setShowGrid(false);

        result.setBorder(BorderFactory.createEmptyBorder(50, 5, 10, 5));

        return result;
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
        m_addNewFileButton.setEnabled(Core.getDataEngine().isProjectLoaded());
        m_wikiImportButton.setEnabled(Core.getDataEngine().isProjectLoaded());
    }

    private void gotoFile(int row) {
        int entryIndex;

        IDataEngine.FileInfo fi;
        try {
            fi = files.get(row);
        } catch (IndexOutOfBoundsException ex) {
            // data changed
            return;
        }
        entryIndex = fi.firstEntryIndex - fi.size + 1;
        Core.getEditor().gotoEntry(entryIndex);
    }

    /**
     * Render for table cells.
     */
    private class CustomRenderer extends DefaultTableCellRenderer {
        protected DecimalFormat pattern;
        private boolean showCurrentFile;

        public CustomRenderer(final int alignment, final String decimalPattern,
                final boolean showCurrentFile) {
            setHorizontalAlignment(alignment);
            this.showCurrentFile = showCurrentFile;
            if (decimalPattern != null) {
                pattern = new DecimalFormat(decimalPattern);
            }
        }

        protected void setValue(Object value) {
            if (pattern != null) {
                super.setValue(pattern.format((Number) value));
            } else {
                super.setValue(value);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Component result = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);
            if (showCurrentFile) {
                IDataEngine.FileInfo fi;
                try {
                    fi = files.get(row);
                } catch (IndexOutOfBoundsException ex) {
                    // data changed
                    fi = null;
                }
                result.setBackground(table.getBackground());
                if (fi != null
                        && fi.filePath
                                .equals(Core.getEditor().getCurrentFile())) {
                    result.setBackground(CURRENT_FILE_COLOR);

                }
            }
            return result;
        }
    }
}
