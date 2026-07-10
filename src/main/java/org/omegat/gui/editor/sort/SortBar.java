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
import java.util.Collections;
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
import org.openide.awt.Mnemonics;

/**
 * Collapsible control bar for the editor's segment sort, stacked in the editor
 * north container above the filter bar. It supports up to three combinable
 * criteria (primary, secondary, tertiary), each a SortKey with an
 * ascending/descending direction; criteria are added or removed with the +/-
 * buttons.
 *
 * Sorting is NOT applied on every combo change (that can be slow on large
 * projects). Changes are staged: as soon as the edited criteria differ from the
 * currently applied sort, an extra row with Apply/Discard buttons appears.
 * Apply runs the sort and Discard reverts the combos to the last applied state;
 * in both cases the bar collapses afterwards. While collapsed, the bar shows a
 * one-line summary of the APPLIED sort, assembled from the existing localized
 * SortKey names plus an arrow for direction (no new translatable strings).
 *
 * Whether the bar is shown at all (project open and more than one segment after
 * filtering) is decided by the editor controller, not here.
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
    /** The criteria that are currently applied to the editor (empty = unsorted). */
    private List<KeySpec> appliedKeys = Collections.emptyList();
    /** Guards programmatic combo changes so they do not count as user edits. */
    private boolean adjusting;
    /** The "add criterion" button of the current last row, or null if none is shown. */
    private JButton plusButton;

    public SortBar() {
        rows.add(new CriterionRow());
        rebuild();
    }

    /** Reset to the default single, unsorted criterion (called on project close). */
    public void reset() {
        rows.clear();
        rows.add(new CriterionRow());
        appliedKeys = Collections.emptyList();
        rebuild();
    }

    /** Rebuild the row layout inside the collapsible body. */
    private void rebuild() {
        JPanel body = getBody();
        body.removeAll();

        // Notice that the segment numbers are no longer sequential, on its own
        // line above the controls; only relevant while a sort is applied.
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

        if (hasPendingChanges()) {
            body.add(pendingRow());
        }

        warning.setVisible(!appliedKeys.isEmpty());
        refreshPlusEnabled();
        refreshSummary();
        body.revalidate();
        body.repaint();
    }

    /** The Apply/Discard row, shown only while there are staged, unapplied changes. */
    private JPanel pendingRow() {
        JPanel pr = new JPanel();
        pr.setLayout(new BoxLayout(pr, BoxLayout.LINE_AXIS));
        pr.add(Box.createHorizontalGlue());
        JButton apply = new JButton();
        Mnemonics.setLocalizedText(apply, OStrings.getString("BUTTON_APPLY"));
        apply.addActionListener(e -> applyPending());
        JButton discard = new JButton();
        Mnemonics.setLocalizedText(discard, OStrings.getString("BUTTON_DISCARD"));
        discard.addActionListener(e -> discardPending());
        pr.add(apply);
        pr.add(Box.createHorizontalStrut(4));
        pr.add(discard);
        pr.add(Box.createHorizontalGlue());
        return pr;
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

    void addRow() {
        if (rows.size() >= MAX_KEYS) {
            return;
        }
        rows.add(new CriterionRow());
        rebuild();
    }

    private void removeRow(int index) {
        if (index <= 0 || index >= rows.size()) {
            return;
        }
        rows.remove(index);
        rebuild();
    }

    /** True if the primary criterion actually reorders (i.e. is not file order). */
    private boolean isSortActive() {
        return !rows.isEmpty() && rows.get(0).key() != SortKey.NATURAL;
    }

    /**
     * The criteria currently selected in the combos (empty = unsorted / file
     * order). Rows left at NATURAL are skipped so a secondary "file order" row
     * never leaks into the applied sort or the summary.
     */
    List<KeySpec> currentKeys() {
        if (!isSortActive()) {
            return Collections.emptyList();
        }
        List<KeySpec> keys = new ArrayList<>();
        for (CriterionRow r : rows) {
            if (r.key() != SortKey.NATURAL) {
                keys.add(r.spec());
            }
        }
        return keys;
    }

    /** True if the edited criteria differ from what is currently applied. */
    boolean hasPendingChanges() {
        return !sameKeys(currentKeys(), appliedKeys);
    }

    private static boolean sameKeys(List<KeySpec> a, List<KeySpec> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).key != b.get(i).key || a.get(i).ascending != b.get(i).ascending) {
                return false;
            }
        }
        return true;
    }

    /**
     * The collapsed one-line summary of the APPLIED sort, built from the same
     * localized SortKey names shown in the expanded combos plus an arrow for the
     * direction (a symbol, so no new translatable string is needed).
     */
    @Override
    protected String buildSummary() {
        String prefix = OStrings.getString("SORT_BAR_LABEL");
        if (appliedKeys.isEmpty()) {
            return prefix + " " + SortKey.NATURAL.getLocalizedName();
        }
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < appliedKeys.size(); i++) {
            KeySpec spec = appliedKeys.get(i);
            sb.append(i == 0 ? " " : ", ");
            sb.append(spec.key.getLocalizedName());
            sb.append(spec.ascending ? " ↑" : " ↓");
        }
        return sb.toString();
    }

    /** Apply the staged criteria to the editor, then collapse the bar. */
    private void applyPending() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        List<KeySpec> pend = currentKeys();
        if (pend.isEmpty()) {
            Core.getEditor().removeSort();
        } else {
            Locale loc = Core.getProject().getProjectProperties().getSourceLanguage().getLocale();
            Core.getEditor().setSort(new MultiKeySorter(pend, loc));
        }
        appliedKeys = pend;
        setExpanded(false);
        rebuild();
    }

    /** Discard the staged criteria: revert the combos to the applied sort, then collapse. */
    void discardPending() {
        adjusting = true;
        setRowsFromKeys(appliedKeys);
        adjusting = false;
        setExpanded(false);
        rebuild();
    }

    /** Rebuild the criterion rows to reflect the given key list (empty = single unsorted row). */
    private void setRowsFromKeys(List<KeySpec> keys) {
        rows.clear();
        if (keys.isEmpty()) {
            rows.add(new CriterionRow());
        } else {
            for (KeySpec k : keys) {
                CriterionRow r = new CriterionRow();
                r.setSpec(k);
                rows.add(r);
            }
        }
    }

    /** Test/support hook: select a key in the given row as a user would. */
    void selectKey(int rowIndex, SortKey key) {
        rows.get(rowIndex).keyCombo.setSelectedItem(key);
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
                if (adjusting) {
                    return;
                }
                updateDirEnabled();
                rebuild();
            });
            dirCombo.addActionListener(e -> {
                if (adjusting) {
                    return;
                }
                rebuild();
            });
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

        void setSpec(KeySpec k) {
            keyCombo.setSelectedItem(k.key);
            dirCombo.setSelectedItem(k.ascending);
            updateDirEnabled();
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
