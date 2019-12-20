/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.align;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;
import org.omegat.gui.align.Aligner.ComparisonMode;
import org.omegat.util.Language;

public class AlignerTest {

    @Before
    public final void setUp() {
        FilterMaster.setFilterClasses(Arrays.asList(TextFilter.class, ResourceBundleFilter.class));
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        assertTrue(Core.getFilterMaster().isFileSupported(new File("blah.txt"), true));
        assertTrue(Core.getFilterMaster().isFileSupported(new File("blah.properties"), true));
    }

    @Test
    public void testAlignerHeapMode() throws Exception {
        String srcFile = "test/data/align/heapSource.txt";
        Language srcLang = new Language(Locale.ENGLISH);
        String trgFile = "test/data/align/heapTarget.txt";
        Language trgLang = new Language(Locale.JAPANESE);
        Aligner aligner = new Aligner(srcFile, srcLang, trgFile, trgLang);

        aligner.comparisonMode = ComparisonMode.HEAPWISE;
        assertHeapResult(aligner.align());

        aligner.comparisonMode = ComparisonMode.PARSEWISE;
        try {
            aligner.align();
            fail("Parsewise not supported for these files");
        } catch (UnsupportedOperationException ex) {
        }

        aligner.comparisonMode = ComparisonMode.ID;
        try {
            aligner.align();
            fail("ID not supported for these files");
        } catch (UnsupportedOperationException ex) {
        }
    }

    @Test
    public void testAlignerParseMode() throws Exception {
        String srcFile = "test/data/align/parseSource.txt";
        Language srcLang = new Language(Locale.ENGLISH);
        String trgFile = "test/data/align/parseTarget.txt";
        Language trgLang = new Language(Locale.JAPANESE);
        Aligner aligner = new Aligner(srcFile, srcLang, trgFile, trgLang);

        aligner.comparisonMode = ComparisonMode.HEAPWISE;
        assertHeapResult(aligner.align());

        aligner.comparisonMode = ComparisonMode.PARSEWISE;
        List<Entry<String, String>> result = aligner.align();
        assertEquals(4, result.size());
        assertEntry("This is sentence one.",
                "\u3053\u308C\u304C1\u3064\u76EE\u306E\u30BB\u30F3\u30C6\u30F3\u30B9\u3002", result.get(0));
        assertEntry("Short sentence.", "\u77ED\u3044\u6587\u3002", result.get(1));
        assertEntry("And then this is a very, very, very long sentence.",
                "\u7D9A\u3044\u3066\u306F\u3068\u3066\u3082\u9577\u304F\u3066\u306A\u304C\u301C\u3044\u9577"
                        + "\u86C7\u306E\u602A\u7269\u30BB\u30F3\u30C6\u30F3\u30B9\u3060\u304C\u3001\u3044"
                        + "\u3064\u7D42\u308F\u308B\u306E\u3060\u308D\u3046\u304B\uFF1F",
                result.get(2));
        assertEntry("Where shall it end? No one knows.", "\u8AB0\u3082\u77E5\u3089\u306A\u3044\u3002",
                result.get(3));

        aligner.comparisonMode = ComparisonMode.ID;
        try {
            aligner.align();
            fail("ID not supported for these files");
        } catch (UnsupportedOperationException ex) {
        }
    }

    @Test
    public void testAlignerIDMode() throws Exception {
        String srcFile = "test/data/align/idSource.properties";
        Language srcLang = new Language(Locale.ENGLISH);
        String trgFile = "test/data/align/idTarget.properties";
        Language trgLang = new Language(Locale.JAPANESE);
        Aligner aligner = new Aligner(srcFile, srcLang, trgFile, trgLang);

        // Aligner will default to ID alignment when possible, so load and then change mode.
        aligner.loadFiles();
        aligner.comparisonMode = ComparisonMode.HEAPWISE;
        assertHeapResult(aligner.align());

        aligner.comparisonMode = ComparisonMode.PARSEWISE;
        try {
            aligner.align();
            fail("Parsewise mode not available for these files.");
        } catch (UnsupportedOperationException ex) {
        }

        aligner.comparisonMode = ComparisonMode.ID;
        List<Entry<String, String>> result = aligner.align();
        assertEquals(4, result.size());
        assertEntry("This is sentence one.",
                "\u3053\u308C\u304C1\u3064\u76EE\u306E\u30BB\u30F3\u30C6\u30F3\u30B9\u3002", result.get(0));
        assertEntry("Short sentence.", "\u77ED\u3044\u6587\u3002", result.get(1));
        assertEntry("And then this is a very, very, very long sentence.",
                "\u7D9A\u3044\u3066\u306F\u3068\u3066\u3082\u9577\u304F\u3066\u306A\u304C\u301C\u3044\u9577"
                        + "\u86C7\u306E\u602A\u7269\u30BB\u30F3\u30C6\u30F3\u30B9\u3060\u304C\u3001\u3044"
                        + "\u3064\u7D42\u308F\u308B\u306E\u3060\u308D\u3046\u304B\uFF1F",
                result.get(2));
        assertEntry("Where shall it end?", "\u8AB0\u3082\u77E5\u3089\u306A\u3044\u3002", result.get(3));
        // Key5 in source has no counterpart in target so it is dropped.
    }

    private void assertHeapResult(List<Entry<String, String>> result) {
        assertEquals(4, result.size());
        assertEntry("This is sentence one.",
                "\u3053\u308C\u304C1\u3064\u76EE\u306E\u30BB\u30F3\u30C6\u30F3\u30B9\u3002", result.get(0));
        assertEntry("Short sentence.", "\u77ED\u3044\u6587\u3002", result.get(1));
        assertEntry("And then this is a very, very, very long sentence. Where shall it end?",
                "\u7D9A\u3044\u3066\u306F\u3068\u3066\u3082\u9577\u304F\u3066\u306A\u304C\u301C\u3044\u9577"
                        + "\u86C7\u306E\u602A\u7269\u30BB\u30F3\u30C6\u30F3\u30B9\u3060\u304C\u3001\u3044"
                        + "\u3064\u7D42\u308F\u308B\u306E\u3060\u308D\u3046\u304B\uFF1F",
                result.get(2));
        assertEntry("No one knows.", "\u8AB0\u3082\u77E5\u3089\u306A\u3044\u3002", result.get(3));
    }

    <T, U> void assertEntry(T expectedKey, U expectedValue, Entry<T, U> entry) {
        assertEquals(expectedKey, entry.getKey());
        assertEquals(expectedValue, entry.getValue());
    }
}
