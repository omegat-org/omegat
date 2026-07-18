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

package org.omegat.gui.align;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import gen.core.filters.Filters;

import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;
import org.omegat.gui.align.Aligner.AlgorithmClass;
import org.omegat.gui.align.Aligner.CalculatorType;
import org.omegat.gui.align.Aligner.CounterType;
import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Tests for remembering the align parameters between aligner sessions,
 * see feature request #1456.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class AlignSettingsPersistenceTest {

    private Aligner aligner;

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
        aligner = new Aligner(null, null, null, null);
    }

    @Test
    public void testDefaultsAreKeptWhenNothingStored() {
        AlignPanelController.restorePersistedSettings(aligner);
        assertEquals(AlgorithmClass.VITERBI, aligner.algorithmClass);
        assertEquals(CalculatorType.NORMAL, aligner.calculatorType);
        assertEquals(CounterType.WORD, aligner.counterType);
        assertTrue(aligner.segment);
        assertFalse(aligner.removeTags);
    }

    @Test
    public void testRoundTrip() {
        aligner.algorithmClass = AlgorithmClass.FB;
        aligner.calculatorType = CalculatorType.POISSON;
        aligner.counterType = CounterType.CHAR;
        aligner.segment = false;
        aligner.removeTags = true;
        AlignPanelController.persistSettings(aligner);

        Aligner other = new Aligner(null, null, null, null);
        AlignPanelController.restorePersistedSettings(other);
        assertEquals(AlgorithmClass.FB, other.algorithmClass);
        assertEquals(CalculatorType.POISSON, other.calculatorType);
        assertEquals(CounterType.CHAR, other.counterType);
        assertFalse(other.segment);
        assertTrue(other.removeTags);
    }

    @Test
    public void testStoredValuesRestored() {
        Preferences.setPreference(AlignerPrefs.ALIGNER_ALGORITHM_CLASS, AlgorithmClass.FB.name());
        Preferences.setPreference(AlignerPrefs.ALIGNER_SEGMENT, false);
        AlignPanelController.restorePersistedSettings(aligner);
        assertEquals(AlgorithmClass.FB, aligner.algorithmClass);
        assertFalse(aligner.segment);
        assertEquals(CalculatorType.NORMAL, aligner.calculatorType);
    }

    @Test
    public void testLanguageFallbackWhenNothingStored() {
        Language fallback = new Language("eo");
        assertEquals(fallback,
                AlignFilePickerController.restorePersistedLanguage(AlignerPrefs.ALIGNER_SOURCE_LANGUAGE, fallback));
    }

    @Test
    public void testLanguageFallbackWhenStoredCodeInvalid() {
        Preferences.setPreference(AlignerPrefs.ALIGNER_SOURCE_LANGUAGE, "not a code");
        Language fallback = new Language("eo");
        assertEquals(fallback,
                AlignFilePickerController.restorePersistedLanguage(AlignerPrefs.ALIGNER_SOURCE_LANGUAGE, fallback));
    }

    @Test
    public void testEmptyFiltersConfigFallsBackToDefaults() throws Exception {
        // A preferences store that never saved filter settings hands out an
        // empty filters configuration; the aligner must not end up with a
        // FilterMaster that cannot parse anything (latent bug uncovered by
        // running these tests before AlignerTest in the same JVM).
        Preferences.setFilters(new Filters());
        FilterMaster.setFilterClasses(Arrays.asList(TextFilter.class, ResourceBundleFilter.class));
        String srcFile = AlignSettingsPersistenceTest.class.getResource("/data/align/heapSource.txt")
                .getFile();
        String trgFile = AlignSettingsPersistenceTest.class.getResource("/data/align/heapTarget.txt")
                .getFile();
        Aligner other = new Aligner(srcFile, new Language("en"), trgFile, new Language("ja"));
        other.comparisonMode = Aligner.ComparisonMode.HEAPWISE;
        assertFalse(other.align().isEmpty());
    }

    @Test
    public void testInputDirRoundTrip() {
        File inputDir = new File("tmp", "foo");
        File inputFile = new File(inputDir, "src.txt");
        AlignFilePickerController.persistInputDir(AlignerPrefs.ALIGNER_LAST_SOURCE_DIR, inputFile.getPath());
        AlignFilePickerController.persistInputDir(AlignerPrefs.ALIGNER_LAST_TARGET_DIR, null);
        assertEquals(inputDir.getPath(),
                AlignFilePickerController.restorePersistedDir(AlignerPrefs.ALIGNER_LAST_SOURCE_DIR));
        assertNull(AlignFilePickerController.restorePersistedDir(AlignerPrefs.ALIGNER_LAST_TARGET_DIR));
    }

    @Test
    public void testLanguageRoundTrip() {
        AlignFilePickerController.persistLanguages(new Language("fr-FR"), new Language("de"));
        Language fallback = new Language("eo");
        assertEquals(new Language("fr-FR"),
                AlignFilePickerController.restorePersistedLanguage(AlignerPrefs.ALIGNER_SOURCE_LANGUAGE, fallback));
        assertEquals(new Language("de"),
                AlignFilePickerController.restorePersistedLanguage(AlignerPrefs.ALIGNER_TARGET_LANGUAGE, fallback));
    }
}
