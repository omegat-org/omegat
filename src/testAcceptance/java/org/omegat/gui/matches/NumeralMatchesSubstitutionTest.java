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

package org.omegat.gui.matches;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.assertj.swing.edt.GuiActionRunner;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.Preferences;

/**
 * End-to-end check of value-based fuzzy match number substitution (SF #465):
 * loads project_numerals fixture like user project, {@code match_numbers} in
 * omegat.project enables number matching, recycling match writes source
 * segment's number into match target - read from and rendered into numeral
 * systems beyond ASCII digits. Per-system rules: unit tests. Here: wiring
 * only, on representative segments.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumeralMatchesSubstitutionTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("src/testAcceptance/resources/data/project_numerals/");

    /**
     * Demo segments live in source/exotic-numerals.txt;
     * source/beyond-coverage.txt sorts first with two segments, so README
     * segment numbers are offset by two.
     */
    private static final int FIRST_DEMO_ENTRY = 3;

    @Test
    public void testMatchInsertionSubstitutesNumbersByValue() throws Exception {
        Preferences.setPreference(Preferences.CONVERT_NUMBERS, true);
        openSampleProject(PROJECT_PATH);
        assertTrue("Fixture's match_numbers setting must reach the loaded project.",
                Core.getProject().getProjectProperties().isMatchNumbersEnabled());

        // Demo segment 1, Western digits: source 1984 replaces match's 1750.
        assertEquals("Das Lagerverzeichnis nennt 1984 Tonkrüge.",
                recycledTranslationOfEntry(FIRST_DEMO_ENTRY));
        // Demo segment 2, Khmer digits: source value rendered in match
        // target's digit script.
        assertEquals("Die Volkszählung von Angkor erfasste 123456 Haushalte.",
                recycledTranslationOfEntry(FIRST_DEMO_ENTRY + 1));
        // Demo segment 3, Ethiopic sign numerals: read by value, written as
        // target's digits.
        assertEquals("Die Chronik datiert die Synode auf das Jahr 1976.",
                recycledTranslationOfEntry(FIRST_DEMO_ENTRY + 2));
        // Demo segment 23, counting-rod target: rods never composed, value
        // arrives as plain digits.
        assertEquals("Das Rechenbrett zählt 432 Münzen.",
                recycledTranslationOfEntry(FIRST_DEMO_ENTRY + 22));

        closeProject();
    }

    /**
     * Activates entry, waits for match, returns editor translation after
     * recycling that match.
     */
    private String recycledTranslationOfEntry(int entryNum) throws Exception {
        MatchesTextArea matcher = (MatchesTextArea) Core.getMatcher();
        CountDownLatch matchArrived = new CountDownLatch(1);
        // Entry check keeps stray events of previously activated entries from
        // releasing latch early.
        PropertyChangeListener waiter = evt -> SwingUtilities.invokeLater(() -> {
            if (matcher.getActiveMatch() != null
                    && Core.getEditor().getCurrentEntryNumber() == entryNum) {
                matchArrived.countDown();
            }
        });
        matcher.addPropertyChangeListener("matches", waiter);
        try {
            GuiActionRunner.execute(() -> Core.getEditor().gotoEntry(entryNum));
            assertTrue("No match arrived for entry " + entryNum + ".",
                    matchArrived.await(timeout, TimeUnit.SECONDS));
        } finally {
            matcher.removePropertyChangeListener("matches", waiter);
        }
        return GuiActionRunner.execute(() -> {
            MainWindow.doRecycleTrans();
            return Core.getEditor().getCurrentTranslation();
        });
    }
}
