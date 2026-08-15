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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.omegat.gui.editor.sort.MultiKeySorter.KeySpec;
import org.omegat.util.OStrings;

/**
 * Behavioral tests for {@link SortBar} that do not require a loaded project:
 * the collapsed-by-default state, the collapsed summary composition, and the
 * staged (deferred) apply model (pending detection + discard revert). The
 * actual apply path needs the editor/project and is verified live; the sort
 * ordering logic is covered by {@link MultiKeySorterTest}.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SortBarTest {

    private static String unsortedSummary() {
        return OStrings.getString("SORT_BAR_LABEL") + " " + SortKey.NATURAL.getLocalizedName();
    }

    @Test
    public void startsCollapsedByDefault() {
        SortBar bar = new SortBar();
        assertFalse("sort bar must start collapsed", bar.isExpanded());
    }

    @Test
    public void toggleExpandsAndCollapses() {
        SortBar bar = new SortBar();
        bar.toggle();
        assertTrue(bar.isExpanded());
        bar.toggle();
        assertFalse(bar.isExpanded());
    }

    @Test
    public void summaryShowsFileOrderWhenNothingApplied() {
        SortBar bar = new SortBar();
        assertEquals(unsortedSummary(), bar.buildSummary());
    }

    @Test
    public void resetRestoresUnsortedDefault() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.reset();
        assertFalse(bar.hasPendingChanges());
        assertEquals(unsortedSummary(), bar.buildSummary());
    }

    @Test
    public void editingCriteriaCreatesPendingChange() {
        SortBar bar = new SortBar();
        assertFalse("no pending changes initially", bar.hasPendingChanges());
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        assertTrue("editing a criterion must stage a pending change", bar.hasPendingChanges());
    }

    @Test
    public void pendingEditsDoNotChangeAppliedSummary() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        // Nothing applied yet, so the collapsed summary still reflects file order.
        assertEquals(unsortedSummary(), bar.buildSummary());
    }

    @Test
    public void discardRevertsToAppliedState() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        assertTrue(bar.hasPendingChanges());
        bar.discardPending();
        assertFalse("discard must clear pending changes", bar.hasPendingChanges());
        assertEquals(unsortedSummary(), bar.buildSummary());
    }

    @Test
    public void secondaryNaturalRowIsNotIncludedInKeys() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.addRow(); // a secondary row that stays at NATURAL (file order)
        List<KeySpec> keys = bar.currentKeys();
        assertEquals("a NATURAL secondary row must not leak into the applied keys", 1, keys.size());
        assertEquals(SortKey.SOURCE_ALPHA, keys.get(0).key);
    }

    @Test
    public void numericDirectionIsCarriedInKeys() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_FILE); // supports numeric
        bar.selectDir(0, true, true);          // numeric ascending
        List<KeySpec> keys = bar.currentKeys();
        assertEquals(1, keys.size());
        assertEquals(SortKey.SOURCE_FILE, keys.get(0).key);
        assertTrue("ascending", keys.get(0).ascending);
        assertTrue("numeric", keys.get(0).numeric);
    }

    @Test
    public void symbolButtonsStayCompactAcrossLookAndFeels() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.addRow(); // two rows, so the move and remove buttons all exist
        List<javax.swing.JButton> symbols = new java.util.ArrayList<>();
        collectSymbolButtons(bar, symbols);
        assertFalse("expected the +/−/arrow buttons in the expanded bar", symbols.isEmpty());
        for (javax.swing.JButton b : symbols) {
            assertEquals("margin of '" + b.getText() + "'", new java.awt.Insets(0, 6, 0, 6), b.getMargin());
            assertEquals("button type of '" + b.getText() + "'", "square",
                    b.getClientProperty("JButton.buttonType"));
        }
    }

    private static void collectSymbolButtons(java.awt.Container c, List<javax.swing.JButton> out) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof javax.swing.JButton
                    && List.of("+", "−", "▲", "▼").contains(((javax.swing.JButton) child).getText())) {
                out.add((javax.swing.JButton) child);
            }
            if (child instanceof java.awt.Container) {
                collectSymbolButtons((java.awt.Container) child, out);
            }
        }
    }

    @Test
    public void romanFreeNumericDirectionIsCarriedInKeys() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_FILE); // supports numeric
        bar.selectDir(0, false, true, true);   // numeric descending, Roman-free
        List<KeySpec> keys = bar.currentKeys();
        assertEquals(1, keys.size());
        assertFalse("descending", keys.get(0).ascending);
        assertTrue("numeric", keys.get(0).numeric);
        assertTrue("Roman-free", keys.get(0).ignoreRoman);
    }

    @Test
    public void applyIsAlwaysVisibleAndEnabledOnlyWhilePending() {
        SortBar bar = new SortBar();
        assertFalse("nothing staged yet, Apply must be disabled", bar.applyEnabled());
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        assertTrue("a staged change must enable Apply", bar.applyEnabled());
        bar.discardPending();
        assertFalse("discard must disable Apply again", bar.applyEnabled());
    }

    @Test
    public void emptySeedFieldStillAllowsApplying() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.selectRandomDir(0, true, null); // seeded mode, field left empty
        assertTrue("an empty seed must not block applying (it is drawn on apply)", bar.applyEnabled());
    }

    @Test
    public void randomDirectionIsCarriedInKeys() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.selectRandomDir(0, false, null);
        List<KeySpec> keys = bar.currentKeys();
        assertEquals(1, keys.size());
        assertTrue(keys.get(0).random);
        assertEquals(null, keys.get(0).seed);

        bar.selectRandomDir(0, true, 42L);
        keys = bar.currentKeys();
        assertTrue(keys.get(0).random);
        assertEquals(Long.valueOf(42L), keys.get(0).seed);
    }

    @Test
    public void randomIsOfferedForNonNumericKeysToo() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_LENGTH); // no numeric text mode offered
        bar.selectRandomDir(0, true, 7L);
        List<KeySpec> keys = bar.currentKeys();
        assertTrue("random must be selectable for every key", keys.get(0).random);
        assertEquals(Long.valueOf(7L), keys.get(0).seed);
    }

    @Test
    public void seedFieldOnlyPresentForSeededRandom() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.selectRandomDir(0, false, null);
        assertFalse("no seed field for the truly random mode", containsTextField(bar));
        bar.selectRandomDir(0, true, null);
        assertTrue("seed field must appear for the seeded mode", containsTextField(bar));
    }

    private static boolean containsTextField(java.awt.Container c) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof javax.swing.JTextField && child.getParent() != null) {
                return true;
            }
            if (child instanceof java.awt.Container && containsTextField((java.awt.Container) child)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void randomSpecRoundTripsThroughSetSpec() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.selectRandomDir(0, true, 4711L);
        List<KeySpec> keys = bar.currentKeys();
        SortBar second = new SortBar();
        second.setRowsForTest(keys);
        assertEquals("4711", second.seedText(0));
        assertTrue(second.currentKeys().get(0).random);
        assertEquals(Long.valueOf(4711L), second.currentKeys().get(0).seed);
    }

    @Test
    public void numericModeIgnoredForNonNumericKey() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_LENGTH); // not a text key -> only asc/desc offered
        bar.selectDir(0, true, true);            // requesting numeric is a no-op here
        assertFalse(bar.currentKeys().get(0).numeric);
    }

    @Test
    public void switchingToNonNumericKeyDropsNumericMode() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_FILE);
        bar.selectDir(0, true, true);
        assertTrue(bar.currentKeys().get(0).numeric);
        bar.selectKey(0, SortKey.DUPLICATE_COUNT); // non-numeric key
        assertFalse("numeric mode must reset when the key cannot sort numerically",
                bar.currentKeys().get(0).numeric);
    }

    @Test
    public void revertingPrimaryToNaturalClearsPending() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        assertTrue(bar.hasPendingChanges());
        bar.selectKey(0, SortKey.NATURAL);
        assertFalse("returning the primary to file order clears the pending change",
                bar.hasPendingChanges());
    }

    // --- removing the (first) criterion --------------------------------------

    @Test
    public void removingTheSoleRowResetsToFileOrder() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.removeRow(0); // the only row: reset to unsorted rather than leaving it empty
        assertEquals(1, bar.rowCount());
        assertTrue(bar.currentKeys().isEmpty());
        assertEquals(unsortedSummary(), bar.buildSummary());
    }

    @Test
    public void removingTheFirstRowPromotesTheSecond() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.addRow();
        bar.selectKey(1, SortKey.TARGET_ALPHA);
        bar.removeRow(0);
        assertEquals(1, bar.rowCount());
        List<KeySpec> keys = bar.currentKeys();
        assertEquals(1, keys.size());
        assertEquals(SortKey.TARGET_ALPHA, keys.get(0).key);
    }

    // --- at most four criteria -----------------------------------------------

    @Test
    public void atMostFourCriteria() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        for (int i = 0; i < 6; i++) {
            bar.addRow();
        }
        assertEquals("no more than four criterion rows are allowed", 4, bar.rowCount());
    }

    // --- no criterion can be picked twice (dynamic exclusion) ----------------

    @Test
    public void aKeyChosenInOneRowIsNotOfferedInAnother() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.addRow();
        assertTrue("the owning row still offers its own key", bar.rowOffersKey(0, SortKey.SOURCE_ALPHA));
        assertFalse("a used key is removed from other rows", bar.rowOffersKey(1, SortKey.SOURCE_ALPHA));
        assertTrue("free keys stay available", bar.rowOffersKey(1, SortKey.TARGET_ALPHA));
        assertTrue("file order stays available everywhere", bar.rowOffersKey(1, SortKey.NATURAL));
    }

    @Test
    public void aFreedKeyReappearsInOtherRows() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.addRow();
        assertFalse(bar.rowOffersKey(1, SortKey.SOURCE_ALPHA));
        bar.selectKey(0, SortKey.TARGET_ALPHA); // frees SOURCE_ALPHA, takes TARGET_ALPHA
        assertTrue("the freed key is offered again", bar.rowOffersKey(1, SortKey.SOURCE_ALPHA));
        assertFalse("the newly taken key is now hidden", bar.rowOffersKey(1, SortKey.TARGET_ALPHA));
    }

    @Test
    public void selectingAnAlreadyUsedKeyIsRejected() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.addRow();
        bar.selectKey(1, SortKey.SOURCE_ALPHA); // excluded from row 1 -> no-op, stays NATURAL
        List<KeySpec> keys = bar.currentKeys();
        assertEquals("the duplicate selection must not take effect", 1, keys.size());
        assertEquals(SortKey.SOURCE_ALPHA, keys.get(0).key);
    }

    // --- reordering criteria --------------------------------------------------

    @Test
    public void movingACriterionSwapsPriority() {
        SortBar bar = new SortBar();
        bar.selectKey(0, SortKey.SOURCE_ALPHA);
        bar.addRow();
        bar.selectKey(1, SortKey.TARGET_ALPHA);
        assertEquals(SortKey.SOURCE_ALPHA, bar.currentKeys().get(0).key);
        bar.moveRow(0, +1); // move the primary down
        List<KeySpec> keys = bar.currentKeys();
        assertEquals(SortKey.TARGET_ALPHA, keys.get(0).key);
        assertEquals(SortKey.SOURCE_ALPHA, keys.get(1).key);
    }
}
