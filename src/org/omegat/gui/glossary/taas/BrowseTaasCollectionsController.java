/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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

package org.omegat.gui.glossary.taas;

import gen.taas.TaasCollection;
import gen.taas.TaasDomain;
import gen.taas.TaasLanguage;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.omegat.core.Core;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.DockingUI;

/**
 * Controller for TaaS download UI.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class BrowseTaasCollectionsController {
    static BrowseTaasCollectionsUI dialog;

    public static void show() {
        dialog = new BrowseTaasCollectionsUI(Core.getMainWindow().getApplicationFrame(), true);

        final Language sourceLang = Core.getProject().getProjectProperties().getSourceLanguage();
        final Language targetLang = Core.getProject().getProjectProperties().getTargetLanguage();

        List<TaasCollection> list = Collections.emptyList();
        CollectionsTable model = new CollectionsTable(list, sourceLang, targetLang);
        dialog.tableCollections.setModel(model);
        dialog.tableCollections.setColumnModel(createColumnModel());

        dialog.labelStatus.setText(OStrings.getString("TAAS_STATUS_LIST"));
        new SwingWorker<List<TaasCollection>, Void>() {
            protected List<TaasCollection> doInBackground() throws Exception {
                return TaaSPlugin.client.getCollectionsList();
            }

            protected void done() {
                try {
                    List<TaasCollection> list = get();
                    removeUnusedCollections(list);

                    CollectionsTable model = new CollectionsTable(list, sourceLang, targetLang);
                    dialog.tableCollections.setModel(model);
                    TableRowSorter<CollectionsTable> sorter = new TableRowSorter<CollectionsTable>(model);
                    dialog.tableCollections.setRowSorter(sorter);
                    sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
                    dialog.tableCollections.setColumnModel(createColumnModel());
                    dialog.labelStatus.setText(" ");
                } catch (ExecutionException e) {
                    Throwable ex = e.getCause();
                    if (ex instanceof TaaSClient.FormatError) {
                        Log.logErrorRB(ex, "TAAS_FORMAT_ERROR", ex.getMessage());
                        dialog.labelStatus.setText(OStrings.getString("TAAS_FORMAT_ERROR"));
                    } else if (ex instanceof TaaSClient.Unauthorized) {
                        Log.logErrorRB(ex, "TAAS_UNAUTHORIZED_ERROR");
                        dialog.labelStatus.setText(OStrings.getString("TAAS_UNAUTHORIZED_ERROR"));
                    } else {
                        Log.logErrorRB(ex, "TAAS_GENERAL_ERROR", ex.getMessage());
                        dialog.labelStatus.setText(MessageFormat.format(
                                OStrings.getString("TAAS_GENERAL_ERROR"), ex.getMessage()));
                    }
                } catch (InterruptedException ex) {
                }
            }
        }.execute();
        dialog.tableCollections.addMouseListener(MOUSE_LISTENER);

        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        };
        dialog.btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        dialog.getRootPane().getActionMap().put("ESCAPE", escapeAction);

        DockingUI.displayCentered(dialog);

        dialog.setVisible(true);
    }

    /**
     * Removes collections with zero terms count.
     */
    static void removeUnusedCollections(List<TaasCollection> list) {
        final Language sourceLang = Core.getProject().getProjectProperties().getSourceLanguage();
        final Language targetLang = Core.getProject().getProjectProperties().getTargetLanguage();

        for (int i = 0; i < list.size(); i++) {
            TaasCollection c = list.get(i);
            if (getCountForLanguage(c, sourceLang) == 0 || getCountForLanguage(c, targetLang) == 0) {
                list.remove(i);
                i--;
            }
        }
    }

    static int getCountForLanguage(TaasCollection c, Language lang) {
        String langCode = lang.getLanguageCode();
        for (TaasLanguage l : c.getLanguages().getLanguage()) {
            if (langCode.equalsIgnoreCase(l.getId())) {
                return l.getCount();
            }
        }
        return 0;
    }

    static TableColumnModel createColumnModel() {
        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn cName = new TableColumn(0, 150);
        cName.setHeaderValue(OStrings.getString("TAAS_LIST_NAME"));
        TableColumn cDesc = new TableColumn(1, 150);
        cDesc.setHeaderValue(OStrings.getString("TAAS_LIST_DESC"));
        TableColumn cSource = new TableColumn(2, 50);
        cSource.setHeaderValue(OStrings.getString("TAAS_LIST_SOURCE_COUNT"));
        TableColumn cTarget = new TableColumn(3, 50);
        cTarget.setHeaderValue(OStrings.getString("TAAS_LIST_TARGET_COUNT"));
        TableColumn cDomains = new TableColumn(4, 150);
        cDomains.setHeaderValue(OStrings.getString("TAAS_LIST_DOMAINS"));
        TableColumn cUpdated = new TableColumn(5, 50);
        cUpdated.setHeaderValue(OStrings.getString("TAAS_LIST_UPDATED"));
        TableColumn cDownload = new TableColumn(6, 50);
        cDownload.setHeaderValue("");
        cDownload.setCellRenderer(new DownloadCellRenderer());

        columns.addColumn(cName);
        columns.addColumn(cDesc);
        columns.addColumn(cSource);
        columns.addColumn(cTarget);
        columns.addColumn(cDomains);
        columns.addColumn(cUpdated);
        columns.addColumn(cDownload);

        return columns;
    }

    static class DownloadCellRenderer extends DefaultTableCellRenderer {
        public DownloadCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(Color.BLUE);
        }
    }

    static MouseListener MOUSE_LISTENER = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            int colIndex = dialog.tableCollections.columnAtPoint(e.getPoint());
            int rowIndex = dialog.tableCollections.rowAtPoint(e.getPoint());
            colIndex = dialog.tableCollections.convertColumnIndexToModel(colIndex);
            rowIndex = dialog.tableCollections.convertRowIndexToModel(rowIndex);

            if (colIndex != 6) {
                return; // only for 'Download' column
            }

            final TaasCollection c = ((CollectionsTable) dialog.tableCollections.getModel()).list
                    .get(rowIndex);

            dialog.labelStatus.setText(MessageFormat.format(OStrings.getString("TAAS_STATUS_DOWNLOAD"),
                    c.getName()));
            new SwingWorker<Object, Void>() {
                File glossaryFile, newFile;

                protected Object doInBackground() throws Exception {
                    glossaryFile = getFileForCollection(c);
                    newFile = new File(glossaryFile.getAbsolutePath() + ".new");

                    TaaSPlugin.client.downloadCollection(c.getId(), newFile);

                    return null;
                }

                protected void done() {
                    try {
                        get();
                        if (glossaryFile.exists()) {
                            glossaryFile.delete();
                        }
                        if (!newFile.renameTo(glossaryFile)) {
                            dialog.labelStatus.setText(OStrings.getString("TAAS_REPLACE_ERROR"));
                        } else {
                            dialog.labelStatus.setText(" ");
                            dialog.tableCollections.repaint();
                        }
                    } catch (ExecutionException e) {
                        Throwable ex = e.getCause();
                        if (ex instanceof TaaSClient.FormatError) {
                            Log.logErrorRB(ex, "TAAS_FORMAT_ERROR", ex.getMessage());
                            dialog.labelStatus.setText(OStrings.getString("TAAS_FORMAT_ERROR"));
                        } else if (ex instanceof TaaSClient.Unauthorized) {
                            Log.logErrorRB(ex, "TAAS_UNAUTHORIZED_ERROR");
                            dialog.labelStatus.setText(OStrings.getString("TAAS_UNAUTHORIZED_ERROR"));
                        } else {
                            Log.logErrorRB(ex, "TAAS_GENERAL_ERROR", ex.getMessage());
                            dialog.labelStatus.setText(MessageFormat.format(
                                    OStrings.getString("TAAS_GENERAL_ERROR"), ex.getMessage()));
                        }
                    } catch (InterruptedException ex) {
                    }
                }
            }.execute();
        }
    };

    static File getFileForCollection(TaasCollection collection) {
        File dir = new File(Core.getProject().getProjectProperties().getGlossaryRoot());
        return new File(dir, "TaaS-" + collection.getId() + ".tbx");
    }

    public static class CollectionsTable extends AbstractTableModel {
        final List<TaasCollection> list;
        final Language source;
        final Language target;

        public CollectionsTable(List<TaasCollection> list, Language source, Language target) {
            this.list = list;
            this.source = source;
            this.target = target;
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public int getRowCount() {
            return list.size();
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return Integer.class;
            case 3:
                return Integer.class;
            case 4:
                return String.class;
            case 5:
                return String.class;
            case 6:
                return String.class;
            default:
                return null;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TaasCollection c = list.get(rowIndex);
            File glossaryFile = getFileForCollection(c);
            switch (columnIndex) {
            case 0:
                return c.getName();
            case 1:
                return c.getDescription();
            case 2:
                return getCountForLanguage(c, source);
            case 3:
                return getCountForLanguage(c, target);
            case 4:
                StringBuilder o = new StringBuilder(200);
                for (TaasDomain d : c.getDomains().getDomain()) {
                    o.append('/').append(d.getName());
                }
                return o.substring(1);
            case 5:
                if (glossaryFile.exists()) {
                    Date m = new Date(glossaryFile.lastModified());
                    String sm = DateFormat.getDateInstance().format(m);
                    String sc = DateFormat.getDateInstance().format(new Date());
                    if (sm.equals(sc)) {
                        sm = DateFormat.getTimeInstance().format(m);
                    }
                    return sm;
                } else {
                    return "";
                }
            case 6:
                return OStrings.getString("TAAS_LIST_DOWNLOAD");
            default:
                return null;
            }
        }
    }
}
