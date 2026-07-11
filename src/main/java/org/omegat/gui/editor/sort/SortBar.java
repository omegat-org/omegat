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

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.UIManager;

import org.omegat.core.Core;
import org.omegat.gui.editor.CollapsibleBar;
import org.omegat.gui.editor.sort.MultiKeySorter.KeySpec;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

/**
 * Collapsible control bar for the editor's segment sort, stacked in the editor
 * north container above the filter bar. It supports up to four combinable
 * criteria ("Sort by ... then by ..."), each a SortKey with a direction; criteria
 * are added or removed with the +/- buttons and reordered with the up/down
 * buttons. A key already chosen in one row is removed from the other rows' key
 * choosers (and offered again when it is freed), so the same criterion can never
 * be selected twice. The key chooser separates the many criteria into thematic
 * blocks (order/structure, source, translation, notes, repetitions, history)
 * with thin dividers so the flat list stays scannable.
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

    private static final int MAX_KEYS = 4;

    /**
     * The sort keys grouped into thematic blocks for the key chooser. The blocks
     * are separated by a thin divider (no visible heading). {@link SortKey#NATURAL}
     * is offered separately at the very top (it means "unsorted"), so it is not
     * part of any group here.
     */
    private static final KeyGroup[] KEY_GROUPS = {
            new KeyGroup("SORT_CAT_STRUCTURE", SortKey.PARAGRAPH_START, SortKey.SOURCE_FILE, SortKey.PATH_ALPHA,
                    SortKey.ID_ALPHA),
            new KeyGroup("SORT_CAT_SOURCE", SortKey.SOURCE_ALPHA, SortKey.SOURCE_RHYME, SortKey.SOURCE_LENGTH),
            new KeyGroup("SORT_CAT_TARGET", SortKey.TARGET_ALPHA, SortKey.TARGET_RHYME, SortKey.TARGET_LENGTH,
                    SortKey.TRANSLATION_STATUS, SortKey.ORIGIN_ALPHA, SortKey.LINK_STATUS, SortKey.SOURCE_FUZZY),
            new KeyGroup("SORT_CAT_NOTES", SortKey.NOTE_ALPHA, SortKey.NOTE_RHYME, SortKey.NOTE_LENGTH,
                    SortKey.HAS_NOTE, SortKey.COMMENT_ALPHA, SortKey.COMMENT_RHYME, SortKey.COMMENT_LENGTH),
            new KeyGroup("SORT_CAT_REPETITION", SortKey.DUPLICATE_STATUS, SortKey.DUPLICATE_COUNT,
                    SortKey.TAG_COUNT),
            new KeyGroup("SORT_CAT_HISTORY", SortKey.CHANGE_DATE, SortKey.CREATION_DATE, SortKey.CHANGER,
                    SortKey.CREATOR),
    };

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
        refreshKeyChoices();
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
        for (int i = 0; i < rows.size(); i++) {
            CriterionRow row = rows.get(i);
            JPanel rp = new JPanel();
            rp.setLayout(new BoxLayout(rp, BoxLayout.LINE_AXIS));
            rp.add(Box.createHorizontalGlue());
            rp.add(new JLabel(rowLabel(i)));
            rp.add(Box.createHorizontalStrut(4));
            rp.add(row.keyCombo);
            rp.add(Box.createHorizontalStrut(4));
            rp.add(row.dirCombo);
            // Reorder: every row but the first can move up, every row but the last can move down.
            if (i > 0) {
                rp.add(Box.createHorizontalStrut(4));
                rp.add(moveButton(i, -1));
            }
            if (i < rows.size() - 1) {
                rp.add(Box.createHorizontalStrut(4));
                rp.add(moveButton(i, +1));
            }
            rp.add(Box.createHorizontalStrut(4));
            rp.add(minusButton(i));
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
     * The "+" is shown on the last row only while another criterion still fits
     * ({@code rows < MAX_KEYS}); whenever it is shown it is enabled.
     */
    private void refreshPlusEnabled() {
        if (plusButton != null) {
            plusButton.setEnabled(true);
        }
    }

    /** Row label: "Sort by:" for the first criterion, "then by:" for the rest. */
    private String rowLabel(int index) {
        return OStrings.getString(index == 0 ? "SORT_BAR_SORT_BY" : "SORT_BAR_THEN_BY");
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
        // Removing makes sense for any row when several exist; with a single row
        // it only does something when that row actually sorts (removing it resets
        // to file order). A lone "file order" row has nothing to remove.
        minus.setEnabled(rows.size() > 1 || rows.get(0).key() != SortKey.NATURAL);
        return minus;
    }

    /** An up ({@code delta < 0}) or down ({@code delta > 0}) reorder button for the given row. */
    private JButton moveButton(int index, int delta) {
        JButton b = new JButton(delta < 0 ? "▲" : "▼");
        b.setMargin(new Insets(0, 6, 0, 6));
        b.setToolTipText(OStrings.getString(delta < 0 ? "SORT_BAR_MOVE_UP" : "SORT_BAR_MOVE_DOWN"));
        b.addActionListener(e -> moveRow(index, delta));
        return b;
    }

    void addRow() {
        if (rows.size() >= MAX_KEYS) {
            return;
        }
        rows.add(new CriterionRow());
        rebuild();
    }

    /**
     * Remove the criterion in the given row. Removing the only remaining row does
     * not leave the bar empty; it resets that row to "file order" (unsorted).
     */
    void removeRow(int index) {
        if (index < 0 || index >= rows.size()) {
            return;
        }
        if (rows.size() == 1) {
            rows.set(0, new CriterionRow());
        } else {
            rows.remove(index);
        }
        rebuild();
    }

    /** Swap the given row with its neighbour to change criterion priority. */
    void moveRow(int index, int delta) {
        int target = index + delta;
        if (index < 0 || index >= rows.size() || target < 0 || target >= rows.size()) {
            return;
        }
        Collections.swap(rows, index, target);
        rebuild();
    }

    /**
     * Repopulate every row's key chooser so that a key already selected in another
     * row is not offered again (and reappears once it is freed). Guarded so the
     * programmatic repopulation does not register as a user edit.
     */
    private void refreshKeyChoices() {
        boolean prev = adjusting;
        adjusting = true;
        for (CriterionRow r : rows) {
            r.populateKeys();
        }
        adjusting = prev;
    }

    /** The non-NATURAL keys selected in rows other than {@code self}. */
    private boolean usedElsewhere(CriterionRow self, SortKey key) {
        for (CriterionRow r : rows) {
            if (r != self && r.key() == key) {
                return true;
            }
        }
        return false;
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
            if (a.get(i).key != b.get(i).key || a.get(i).ascending != b.get(i).ascending
                    || a.get(i).numeric != b.get(i).numeric) {
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
            if (spec.numeric) {
                sb.append(spec.ascending ? " #↑" : " #↓");
            } else {
                sb.append(spec.ascending ? " ↑" : " ↓");
            }
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

    /** Test/support hook: set the direction (and numeric mode) of the given row. */
    void selectDir(int rowIndex, boolean ascending, boolean numeric) {
        rows.get(rowIndex).dirCombo.setSelectedItem(Dir.of(ascending, numeric));
    }

    /** Test/support hook: the number of criterion rows currently shown. */
    int rowCount() {
        return rows.size();
    }

    /** Test/support hook: true if the given row currently offers the key as a choice. */
    boolean rowOffersKey(int rowIndex, SortKey key) {
        JComboBox<Object> combo = rows.get(rowIndex).keyCombo;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i) == key) {
                return true;
            }
        }
        return false;
    }

    /** A labelled, non-selectable group header plus its member keys, for the key combo. */
    private static final class KeyGroup {
        private final String labelKey;
        private final SortKey[] keys;

        KeyGroup(String labelKey, SortKey... keys) {
            this.labelKey = labelKey;
            this.keys = keys;
        }

        String getLabel() {
            return OStrings.getString(labelKey);
        }
    }

    /** Direction option: plain or value-based (numeric), ascending or descending. */
    private enum Dir {
        ASC(true, false, "SORT_ASCENDING"),
        DESC(false, false, "SORT_DESCENDING"),
        NUM_ASC(true, true, "SORT_NUM_ASCENDING"),
        NUM_DESC(false, true, "SORT_NUM_DESCENDING");

        final boolean ascending;
        final boolean numeric;
        private final String labelKey;

        Dir(boolean ascending, boolean numeric, String labelKey) {
            this.ascending = ascending;
            this.numeric = numeric;
            this.labelKey = labelKey;
        }

        String getLabel() {
            return OStrings.getString(labelKey);
        }

        static Dir of(boolean ascending, boolean numeric) {
            if (numeric) {
                return ascending ? NUM_ASC : NUM_DESC;
            }
            return ascending ? ASC : DESC;
        }
    }

    /** A single criterion: a sort key plus a direction (with optional numeric mode). */
    private final class CriterionRow {
        final JComboBox<Object> keyCombo = new JComboBox<>();
        final JComboBox<Dir> dirCombo = new JComboBox<>();
        /** The last real (non-header) key selected, used to reject header clicks. */
        private SortKey lastSelectedKey = SortKey.NATURAL;

        CriterionRow() {
            keyCombo.setRenderer(new KeyRenderer());
            dirCombo.setRenderer(new DirectionRenderer());
            populateKeys();
            keyCombo.addActionListener(e -> {
                // Ignore programmatic changes (repopulation/discard); their direction
                // options are set explicitly, so we must not react to transient
                // selections here (that would clobber a chosen numeric direction).
                if (adjusting) {
                    return;
                }
                Object sel = keyCombo.getSelectedItem();
                if (sel instanceof KeyGroup) {
                    // Divider rows are not selectable; revert to the last real key.
                    adjusting = true;
                    keyCombo.setSelectedItem(lastSelectedKey);
                    adjusting = false;
                    return;
                }
                lastSelectedKey = (SortKey) sel;
                updateDirOptions();
                rebuild();
            });
            dirCombo.addActionListener(e -> {
                if (adjusting) {
                    return;
                }
                rebuild();
            });
        }

        /**
         * (Re)fill the key chooser: "file order" first, then each thematic block
         * (preceded by a divider) with the keys not already taken by another row.
         * This row's own current selection is always kept available and reselected.
         * Empty blocks (all members used elsewhere) are dropped, divider included.
         */
        void populateKeys() {
            SortKey selected = key();
            boolean prev = adjusting;
            adjusting = true;
            keyCombo.removeAllItems();
            keyCombo.addItem(SortKey.NATURAL);
            for (KeyGroup g : KEY_GROUPS) {
                List<SortKey> available = new ArrayList<>();
                for (SortKey k : g.keys) {
                    if (k == selected || !usedElsewhere(this, k)) {
                        available.add(k);
                    }
                }
                if (!available.isEmpty()) {
                    keyCombo.addItem(g);
                    for (SortKey k : available) {
                        keyCombo.addItem(k);
                    }
                }
            }
            keyCombo.setSelectedItem(selected);
            lastSelectedKey = selected;
            keyCombo.setMaximumSize(keyCombo.getPreferredSize());
            // Re-derive the direction options for the (restored) key. Done here,
            // with the correct key selected, so a valid numeric direction is kept
            // and an invalid one (key no longer numeric) is dropped.
            updateDirOptions();
            adjusting = prev;
        }

        /**
         * Populate the direction combo with 2 options (asc/desc) or 4 (plus
         * numeric asc/desc) depending on whether the selected key supports
         * value-based ordering. Disabled for the unsorted "file order" option.
         */
        void updateDirOptions() {
            SortKey k = key();
            Dir current = (Dir) dirCombo.getSelectedItem();
            boolean prev = adjusting;
            adjusting = true;
            dirCombo.removeAllItems();
            dirCombo.addItem(Dir.ASC);
            dirCombo.addItem(Dir.DESC);
            boolean numeric = k.supportsNumeric();
            if (numeric) {
                dirCombo.addItem(Dir.NUM_ASC);
                dirCombo.addItem(Dir.NUM_DESC);
            }
            boolean keepCurrent = current == Dir.ASC || current == Dir.DESC
                    || (numeric && (current == Dir.NUM_ASC || current == Dir.NUM_DESC));
            dirCombo.setSelectedItem(keepCurrent ? current : Dir.ASC);
            dirCombo.setEnabled(k != SortKey.NATURAL);
            dirCombo.setMaximumSize(dirCombo.getPreferredSize());
            adjusting = prev;
        }

        SortKey key() {
            Object sel = keyCombo.getSelectedItem();
            return (sel instanceof SortKey) ? (SortKey) sel : SortKey.NATURAL;
        }

        KeySpec spec() {
            Dir d = (Dir) dirCombo.getSelectedItem();
            if (d == null) {
                d = Dir.ASC;
            }
            return new KeySpec(key(), d.ascending, d.numeric);
        }

        void setSpec(KeySpec k) {
            keyCombo.setSelectedItem(k.key);
            updateDirOptions();
            dirCombo.setSelectedItem(Dir.of(k.ascending, k.numeric));
        }
    }

    /**
     * Renders {@link SortKey} entries (with an explanatory tooltip for the less
     * obvious ones) and renders the non-selectable {@link KeyGroup} markers as a
     * thin divider between thematic blocks (no visible heading).
     */
    private static class KeyRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (value instanceof KeyGroup) {
                // A group marker becomes a subtle divider (a thin top line), not
                // selectable. It stays a JLabel like every other cell so the combo
                // keeps one consistent cell height (a bare JSeparator cell can make
                // the popup collapse all rows to a sliver under some look & feels).
                // The block name is kept only as a hover tooltip, not a heading.
                Component c = super.getListCellRendererComponent(list, " ", index, false, false);
                if (c instanceof JComponent jc) {
                    jc.setEnabled(false);
                    Color line = UIManager.getColor("Separator.foreground");
                    jc.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                            line != null ? line : Color.GRAY));
                    jc.setToolTipText(((KeyGroup) value).getLabel());
                }
                return c;
            }
            SortKey k = (SortKey) value;
            String text = (k == null) ? "" : k.getLocalizedName();
            Component c = super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            if (c instanceof JComponent) {
                ((JComponent) c).setToolTipText(k == null ? null : k.getTooltip());
            }
            return c;
        }
    }

    /**
     * Renders a {@link Dir} direction option. The plain ascending/descending
     * options are labelled "alphabetical …" when the same combo also offers the
     * numeric options, so the two orderings are clearly distinguished.
     */
    private static class DirectionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String text = "";
            if (value instanceof Dir) {
                Dir d = (Dir) value;
                boolean numericOffered = containsNumericOption(list);
                if (d == Dir.ASC) {
                    text = OStrings.getString(numericOffered ? "SORT_ALPHA_ASCENDING" : "SORT_ASCENDING");
                } else if (d == Dir.DESC) {
                    text = OStrings.getString(numericOffered ? "SORT_ALPHA_DESCENDING" : "SORT_DESCENDING");
                } else {
                    text = d.getLabel();
                }
            }
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }

        /** True if the combo's item list offers the numeric direction options. */
        private static boolean containsNumericOption(JList<?> list) {
            ListModel<?> model = list.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i) == Dir.NUM_ASC) {
                    return true;
                }
            }
            return false;
        }
    }
}
