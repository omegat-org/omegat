/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Alex Buloichik, Yu Tang
               2014 Aaron Madlon-Kay
               2015 Yu Tang, Aaron Madlon-Kay
               2016 Aaron Madlon-Kay
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

package org.omegat.gui.filters2;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.omegat.core.Core;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.FiltersTableModel;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.TableColumnSizer;

import gen.core.filters.Filter;
import gen.core.filters.Filters;

/**
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik
 * @author Yu Tang
 * @author Aaron Madlon-Kay
 */
public class FiltersCustomizerController extends BasePreferencesController {

    private FiltersCustomizerPanel panel;

    /**
     * Flag if this customizer shows project specific filters or not
     */
    private final boolean isProjectSpecific;

    /** Filters from OmegaT. */
    private final Filters defaultFilters;
    /** User-modified filters */
    private final Filters userFilters;
    /** Project-specific filters */
    private final Filters projectFilters;

    private FiltersTableModel model;

    /** Filters which editable now. */
    private Filters editableFilters;

    /** Names of filters to mark as in-use in the list */
    private Collection<String> inUseFilters = Collections.emptySet();

    public FiltersCustomizerController() {
        this(false, FilterMaster.createDefaultFiltersConfig(), Preferences.getFilters(), null);
    }

    public FiltersCustomizerController(boolean projectSpecific, Filters defaultFilters, Filters userFilters,
            Filters projectFilters) {
        this.isProjectSpecific = projectSpecific;
        this.defaultFilters = defaultFilters;
        this.userFilters = userFilters == null ? defaultFilters : userFilters;
        this.projectFilters = projectFilters;
    }

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return isProjectSpecific ? OStrings.getString("FILTERSCUSTOMIZER_TITLE_PROJECTSPECIFIC")
                : OStrings.getString("FILTERSCUSTOMIZER_TITLE");
    }

    private void initGui() {
        panel = new FiltersCustomizerPanel();
        panel.filtersTable.setDefaultRenderer(String.class, new FilterFormatCellRenderer());
        panel.filtersTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
                panel.editButton.setEnabled(false);
                panel.optionsButton.setEnabled(false);
            } else {
                panel.editButton.setEnabled(true);
                Filter currFilter = getFilterAtRow(panel.filtersTable.getSelectedRow());
                IFilter f = FilterMaster.getFilterInstance(currFilter.getClassName());
                panel.optionsButton.setEnabled(f != null && f.hasOptions());
            }
        });
        panel.filtersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2 && me.getButton() == MouseEvent.BUTTON1) {
                    doEdit(panel.filtersTable.rowAtPoint(me.getPoint()));
                }
            }
        });
        TableColumnSizer.autoSize(panel.filtersTable, 0, true);
        if (!isProjectSpecific) {
            panel.projectSpecificCB.setVisible(false);
        }
        panel.projectSpecificCB.addActionListener(e -> updateEnabledness());
        panel.cbRemoveTags
                .addActionListener(e -> editableFilters.setRemoveTags(panel.cbRemoveTags.isSelected()));
        panel.cbRemoveSpacesNonseg.addActionListener(
                e -> editableFilters.setRemoveSpacesNonseg(panel.cbRemoveSpacesNonseg.isSelected()));
        panel.cbPreserveSpaces.addActionListener(
                e -> editableFilters.setPreserveSpaces(panel.cbPreserveSpaces.isSelected()));
        panel.cbIgnoreFileContext.addActionListener(
                e -> editableFilters.setIgnoreFileContext(panel.cbIgnoreFileContext.isSelected()));
        panel.optionsButton.addActionListener(e -> {
            Filter currFilter = getFilterAtRow(panel.filtersTable.getSelectedRow());
            IFilter f = FilterMaster.getFilterInstance(currFilter.getClassName());
            if (f != null) {
                // new options handling
                Map<String, String> newConfig = f.changeOptions(SwingUtilities.windowForComponent(panel),
                        FilterMaster.forFilter(currFilter.getOption()));
                if (newConfig != null) {
                    FilterMaster.setOptions(currFilter, newConfig);
                }
            }
        });
        panel.editButton.addActionListener(e -> doEdit(panel.filtersTable.getSelectedRow()));
        if (Core.getProject().isProjectLoaded()) {
            inUseFilters = Core.getProject().getProjectFiles().stream().map(info -> info.filterFileFormatName)
                    .collect(Collectors.toSet());
        }
    }

    private void updateEnabledness() {
        boolean enabled = isEditable();
        panel.filtersTable.setEnabled(enabled);
        panel.filtersTable.setFocusable(enabled);
        panel.cbRemoveTags.setEnabled(enabled);
        panel.cbRemoveSpacesNonseg.setEnabled(enabled);
        panel.cbPreserveSpaces.setEnabled(enabled);
        panel.cbIgnoreFileContext.setEnabled(enabled);
        if (!enabled) {
            panel.filtersTable.getSelectionModel().clearSelection();
        }
    }

    private void doEdit(int row) {
        if (!isEditable()) {
            return;
        }
        List<Filter> filters = editableFilters.getFilters();
        Filter filter = getFilterAtRow(row);
        FilterEditor editor = new FilterEditor(SwingUtilities.windowForComponent(panel), filter);
        editor.setVisible(true);
        if (editor.result != null) {
            Filter f = editor.result;
            filters.set(filters.indexOf(filter), f);
            model.setFilter(f);
        }
    }

    private Filter getFilterAtRow(int row) {
        return ((FiltersTableModel) panel.filtersTable.getModel())
                .getFilterAtRow(panel.filtersTable.convertRowIndexToModel(row));
    }

    @Override
    protected void initFromPrefs() {
        editableFilters = isProjectSpecific && projectFilters != null
                ? FilterMaster.cloneConfig(projectFilters)
                : FilterMaster.cloneConfig(userFilters);
        model = new FiltersTableModel(editableFilters);
        panel.filtersTable.setModel(model);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(panel.filtersTable.getModel());
        panel.filtersTable.setRowSorter(sorter);
        List<RowSorter.SortKey> sortkeys = new ArrayList<>();
        sortkeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
        sortkeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortkeys);
        if (isProjectSpecific) {
            panel.projectSpecificCB.setSelected(projectFilters != null);
        }
        panel.cbRemoveTags.setSelected(editableFilters.isRemoveTags());
        panel.cbRemoveSpacesNonseg.setSelected(editableFilters.isRemoveSpacesNonseg());
        panel.cbPreserveSpaces.setSelected(editableFilters.isPreserveSpaces());
        panel.cbIgnoreFileContext.setSelected(editableFilters.isIgnoreFileContext());
        updateEnabledness();
    }

    @Override
    public void persist() {
        if (!isProjectSpecific) {
            Core.setFilterMaster(new FilterMaster(editableFilters));
            Preferences.setFilters(editableFilters);
        }
    }

    @Override
    public void restoreDefaults() {
        if (isEditable()) {
            editableFilters = FilterMaster.cloneConfig(defaultFilters);
            model = new FiltersTableModel(editableFilters);
            panel.filtersTable.setModel(model);
            panel.cbRemoveTags.setSelected(editableFilters.isRemoveTags());
            panel.cbRemoveSpacesNonseg.setSelected(editableFilters.isRemoveSpacesNonseg());
            panel.cbPreserveSpaces.setSelected(editableFilters.isPreserveSpaces());
            panel.cbIgnoreFileContext.setSelected(editableFilters.isIgnoreFileContext());
        }
    }

    private boolean isEditable() {
        return !isProjectSpecific || panel.projectSpecificCB.isSelected();
    }

    public Filters getResult() {
        if (isEditable()) {
            return editableFilters;
        } else {
            return null;
        }
    }

    @SuppressWarnings("serial")
    private class FilterFormatCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (value != null && inUseFilters.contains(value.toString())) {
                component.setFont(component.getFont().deriveFont(Font.BOLD));
            }
            return component;
        }
    }
}
