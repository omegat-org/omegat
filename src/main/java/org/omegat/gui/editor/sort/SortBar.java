/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.editor.sort;

import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.omegat.core.Core;
import org.omegat.gui.editor.CollapsibleBar;
import org.omegat.gui.editor.sort.MultiKeySorter.KeySpec;
import org.omegat.util.OStrings;

/**
 * Collapsible control bar for the editor's segment sort, stacked in the editor
 * north container above the filter bar. It supports up to three combinable
 * criteria (primary, secondary, tertiary), each a SortKey with an
 * ascending/descending direction; criteria are added or removed with the +/-
 * buttons. The default is a single SortKey.NATURAL criterion (file order,
 * unsorted). Changes are applied live through the editor's setSort/removeSort.
 *
 * While collapsed, the bar shows a one-line summary of the active criteria,
 * assembled from the same localized SortKey names used in the expanded combos,
 * so collapsing adds no new translatable strings. Whether the bar is shown at
 * all (project open and more than one segment after filtering) is decided by the
 * editor controller, not here.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
public class SortBar extends CollapsibleBar {

    private static final int MAX_KEYS = 3;
    private static final String[] ROW_LABEL_KEYS = { "SORT_KEY_PRIMARY", "SORT_KEY_SECONDARY",
            "SORT_KEY_TERTIARY" };

    private final List<CriterionRow> rows = new ArrayList<>();
    private final JLabel warning = new JLabel(OStrings.getString("SORT_BAR_WARNING"));
    /** The "add criterion" button of the current last row, or null if none is shown. */
    private JButton plusButton;

    public SortBar() {
        rows.add(new CriterionRow());
        rebuild();
    }

    /** Reset to the default single, unsorted criterion. */
    public void reset() {
        rows.clear();
        rows.add(new CriterionRow());
        rebuild();
    }

    /** Rebuild the row layout inside the collapsible body (after adding/removing a criterion). */
    private void rebuild() {
        JPanel body = getBody();
        body.removeAll();

        // Notice that the segment numbers are no longer sequential, on its own
        // line above the controls; only relevant while a sort is active.
        JPanel warnRow = new JPanel();
        warnRow.setLayout(new BoxLayout(warnRow, BoxLayout.LINE_AXIS));
        warnRow.add(Box.createHorizontalGlue());
        warnRow.add(warning);
        warnRow.add(Box.createHorizontalGlue());
        body.add(warnRow);

        plusButton = null;
        boolean multi = rows.size() > 1;
        for (int i = 0; i < rows.size(); i++) {
            CriterionRow row = rows.get(i);
            JPanel rp = new JPanel();
            rp.setLayout(new BoxLayout(rp, BoxLayout.LINE_AXIS));
            rp.add(Box.createHorizontalGlue());
            rp.add(new JLabel(rowLabel(i, multi)));
            rp.add(Box.createHorizontalStrut(4));
            rp.add(row.keyCombo);
            rp.add(Box.createHorizontalStrut(4));
            rp.add(row.dirCombo);
            if (i > 0) {
                rp.add(Box.createHorizontalStrut(4));
                rp.add(minusButton(i));
            }
            if (i == rows.size() - 1 && rows.size() < MAX_KEYS) {
                rp.add(Box.createHorizontalStrut(4));
                plusButton = createPlusButton();
                rp.add(plusButton);
            }
            rp.add(Box.createHorizontalGlue());
            body.add(rp);
        }

        warning.setVisible(isSortActive());
        refreshPlusEnabled();
        refreshSummary();
        body.revalidate();
        body.repaint();
    }

    /**
     * Adding a secondary/tertiary criterion only makes sense once the primary
     * criterion actually sorts, so the "+" is disabled while it is "file order".
     */
    private void refreshPlusEnabled() {
        if (plusButton != null) {
            plusButton.setEnabled(isSortActive());
        }
    }

    private String rowLabel(int index, boolean multi) {
        if (!multi) {
            return OStrings.getString("SORT_BAR_LABEL");
        }
        return OStrings.getString(ROW_LABEL_KEYS[index]);
    }

    private JButton createPlusButton() {
        JButton plus = new JButton("+");
        plus.setMargin(new Insets(0, 6, 0, 6));
        plus.setToolTipText(OStrings.getString("SORT_BAR_ADD"));
        plus.addActionListener(e -> addRow());
        return plus;
    }

    private JButton minusButton(int index) {
        JButton minus = new JButton("−");
        minus.setMargin(new Insets(0, 6, 0, 6));
        minus.setToolTipText(OStrings.getString("SORT_BAR_REMOVE_KEY"));
        minus.addActionListener(e -> removeRow(index));
        return minus;
    }

    private void addRow() {
        if (rows.size() >= MAX_KEYS) {
            return;
        }
        rows.add(new CriterionRow());
        rebuild();
        apply();
    }

    private void removeRow(int index) {
        if (index <= 0 || index >= rows.size()) {
            return;
        }
        rows.remove(index);
        rebuild();
        apply();
    }

    /** True if the primary criterion actually reorders (i.e. is not file order). */
    private boolean isSortActive() {
        return !rows.isEmpty() && rows.get(0).key() != SortKey.NATURAL;
    }

    /**
     * The collapsed one-line summary of the current sort, built from the same
     * localized SortKey names shown in the expanded combos plus an arrow for the
     * direction (a symbol, so no new translatable string is needed).
     */
    @Override
    protected String buildSummary() {
        String prefix = OStrings.getString("SORT_BAR_LABEL");
        if (!isSortActive()) {
            return prefix + " " + SortKey.NATURAL.getLocalizedName();
        }
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < rows.size(); i++) {
            KeySpec spec = rows.get(i).spec();
            sb.append(i == 0 ? " " : ", ");
            sb.append(spec.key.getLocalizedName());
            sb.append(spec.ascending ? " ↑" : " ↓");
        }
        return sb.toString();
    }

    private void apply() {
        refreshSummary();
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        Core.getEditor().commitAndDeactivate();
        if (!isSortActive()) {
            warning.setVisible(false);
            Core.getEditor().removeSort();
            return;
        }
        List<KeySpec> keys = new ArrayList<>();
        for (CriterionRow r : rows) {
            keys.add(r.spec());
        }
        Locale loc = Core.getProject().getProjectProperties().getSourceLanguage().getLocale();
        Core.getEditor().setSort(new MultiKeySorter(keys, loc));
        warning.setVisible(true);
    }

    /** A single criterion: a sort key plus a direction. */
    private final class CriterionRow {
        final JComboBox<SortKey> keyCombo = new JComboBox<>();
        final JComboBox<Boolean> dirCombo = new JComboBox<>(new Boolean[] { Boolean.TRUE, Boolean.FALSE });

        CriterionRow() {
            for (SortKey k : SortKey.values()) {
                keyCombo.addItem(k);
            }
            keyCombo.setSelectedItem(SortKey.NATURAL);
            keyCombo.setRenderer(new KeyRenderer());
            dirCombo.setRenderer(new DirectionRenderer());
            keyCombo.setMaximumSize(keyCombo.getPreferredSize());
            dirCombo.setMaximumSize(dirCombo.getPreferredSize());
            updateDirEnabled();
            keyCombo.addActionListener(e -> {
                updateDirEnabled();
                refreshPlusEnabled();
                apply();
            });
            dirCombo.addActionListener(e -> apply());
        }

        /** Direction is meaningless for the unsorted "file order" option. */
        void updateDirEnabled() {
            dirCombo.setEnabled(key() != SortKey.NATURAL);
        }

        SortKey key() {
            return (SortKey) keyCombo.getSelectedItem();
        }

        KeySpec spec() {
            return new KeySpec(key(), Boolean.TRUE.equals(dirCombo.getSelectedItem()));
        }
    }

    /** Renders a {@link SortKey} with its localized name. */
    private static class KeyRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String text = (value == null) ? "" : ((SortKey) value).getLocalizedName();
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }

    /** Renders the ascending/descending direction flag. */
    private static class DirectionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String text = Boolean.TRUE.equals(value) ? OStrings.getString("SORT_ASCENDING")
                    : OStrings.getString("SORT_DESCENDING");
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }
}
