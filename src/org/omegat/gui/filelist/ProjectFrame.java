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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JLabel;
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
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.UIThreadsUtil;
import org.openide.awt.Mnemonics;

/**
 * A frame for project, showing all the files of the project.
 * 
 * Synchronized around ProjectFrame when displayed data changed.
 * 
 * @author Keith Godfrey
 * @author Kim Bruning
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProjectFrame extends JFrame {
    private JTable table;
    private JLabel totalSegmentLabel, totalUniqueLabel, totalTranslatedLabel;
    private JLabel totalSegment, totalUnique, totalTranslated;
    private List<IDataEngine.FileInfo> files;

    private JButton m_addNewFileButton;
    private JButton m_wikiImportButton;
    private JButton m_closeButton;

    private MainWindow m_parent;

    private static final Color CURRENT_FILE_COLOR = new Color(0xC8DDF2);

    public ProjectFrame(MainWindow parent) {
        m_parent = parent;

        table = new JTable();
        
        totalSegment = new JLabel();
        totalUnique = new JLabel();
        totalTranslated = new JLabel();
        
        totalSegmentLabel = new JLabel(OStrings.getString("GUI_PROJECT_TOTAL_SEGMENTS"));
        totalUniqueLabel = new JLabel(OStrings.getString("GUI_PROJECT_UNIQUE_SEGMENTS"));
        totalTranslatedLabel = new JLabel(OStrings.getString("GUI_PROJECT_TRANSLATED"));

        // set the position and size
        initWindowLayout();

        Container cp = getContentPane();

        JPanel dataPanel = new JPanel(new BorderLayout());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dataPanel.add(scroll, BorderLayout.CENTER);

        JPanel totalPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 20);
        gbc.anchor = GridBagConstraints.EAST;

        gbc.gridx = 0;

        gbc.gridy = 0;
        totalPanel.add(totalSegmentLabel, gbc);
        gbc.gridy = 1;
        totalPanel.add(totalUniqueLabel, gbc);
        gbc.gridy = 2;
        totalPanel.add(totalTranslatedLabel, gbc);

        gbc.gridx = 1;

        gbc.gridy = 0;
        totalPanel.add(totalSegment, gbc);
        gbc.gridy = 1;
        totalPanel.add(totalUnique, gbc);
        gbc.gridy = 2;
        totalPanel.add(totalTranslated, gbc);
        totalPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        dataPanel.add(totalPanel, BorderLayout.SOUTH);

        cp.add(dataPanel, BorderLayout.CENTER);

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
        cp.add(bbut, "South"); // NOI18N

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
                table.repaint();
            }

            /**
             * Updates the number of translated segments only, does not rebuild
             * the whole display.
             */
            public void onEntryActivated(StringEntry newEntry) {
                UIThreadsUtil.mustBeSwingThread();
                totalTranslated.setText(Integer.toString(CommandThread.core
                        .getNumberofTranslatedSegments()));
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gotoFile(table.rowAtPoint(e.getPoint()));
            }
        });
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    gotoFile(table.getSelectedRow());
                }
            }
        });
    }

    private JLabel createTotalLabel(final String resourceKey) {
        JLabel result = new JLabel(OStrings.getString(resourceKey));
        Font f = result.getFont();
        f = new Font(f.getName(), Font.BOLD, f.getSize());
        result.setFont(f);
        return result;
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
     * Initialize table columns.
     */
    private void setupTableColumns() {
        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn cFile = new TableColumn(0, 200);
        cFile.setHeaderValue(OStrings.getString("PF_FILENAME"));
        cFile.setCellRenderer(new ColorRenderer());
        TableColumn cCount = new TableColumn(1, 50);
        cCount.setHeaderValue(OStrings.getString("PF_NUM_SEGMENTS"));
        cCount.setCellRenderer(new NumberRenderer());
        columns.addColumn(cFile);
        columns.addColumn(cCount);
        table.setColumnModel(columns);
    }

    /**
     * Builds the table which lists all the project files.
     */
    public void buildDisplay() {
        UIThreadsUtil.mustBeSwingThread();

        synchronized (this) {
            files = Core.getDataEngine().getProjectFiles();
            int firstEntry = 1;
            int entriesUpToNow = 0;
            for (IDataEngine.FileInfo fi : Core.getDataEngine()
                    .getProjectFiles()) {
                entriesUpToNow = fi.firstEntryIndex;
                fi.size = 1 + entriesUpToNow - firstEntry;
                firstEntry = entriesUpToNow + 1;
            }
            table.setModel(new AbstractTableModel() {
                public Object getValueAt(int rowIndex, int columnIndex) {
                    IDataEngine.FileInfo fi = files.get(rowIndex);
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
            });
            setupTableColumns();
        }
        totalSegment.setText(Integer.toString(CommandThread.core
                .getNumberOfSegmentsTotal()));
        totalUnique.setText(Integer.toString(CommandThread.core
                .getNumberOfUniqueSegments()));
        totalTranslated.setText(Integer.toString(CommandThread.core
                .getNumberofTranslatedSegments()));

        uiUpdateImportButtonStatus();
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

        synchronized (this) {
            if (row < 0 || row > files.size()) {
                return;
            }
            IDataEngine.FileInfo fi = files.get(row);
            entryIndex = fi.firstEntryIndex - fi.size + 1;
        }
        Core.getEditor().gotoEntry(entryIndex);
    }

    /** Call this to set OmegaT-wide font for this window. */
    public void setFont(Font f) {
        super.setFont(f);
        table.setFont(f);
        table.setRowHeight(f.getSize() + 2);
        
        Font bold=new Font(f.getName(), Font.BOLD, f.getSize());
        totalSegment.setFont(bold);
        totalUnique.setFont(bold);
        totalTranslated.setFont(bold);
        totalSegmentLabel.setFont(bold);
        totalUniqueLabel.setFont(bold);
        totalTranslatedLabel.setFont(bold);
    }

    /**
     * Renderer for display current file in different color.
     */
    private class ColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Component result = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);

            IDataEngine.FileInfo fi;
            synchronized (ProjectFrame.this) {
                fi = files.get(row);
            }
            if (!isSelected) {
                if (fi.filePath.equals(Core.getEditor().getCurrentFile())) {
                    result.setBackground(CURRENT_FILE_COLOR);
                } else {
                    result.setBackground(table.getBackground());
                }
            } else {
                result.setBackground(table.getSelectionBackground());
            }
            return result;
        }
    }

    /**
     * Render for display right-aligned numbers.
     */
    private class NumberRenderer extends ColorRenderer {
        protected DecimalFormat pattern = new DecimalFormat(",##0");

        public NumberRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        protected void setValue(Object value) {
            super.setValue(pattern.format(value));
        }
    }
}
