/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

￼OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.align;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.data.TestCoreState;
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
        TestCoreState.resetState();
        FilterMaster.setFilterClasses(Arrays.asList(TextFilter.class, ResourceBundleFilter.class));
        TestCoreState.getInstance().setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
        TestCoreState.getInstance().setSegmenter(new Segmenter(SRX.getDefault()));
        assertTrue(Core.getFilterMaster().isFileSupported(new File("blah.txt"), true));
        assertTrue(Core.getFilterMaster().isFileSupported(new File("blah.properties"), true));
    }

    @Test
    public void testAlignerHeapMode() throws Exception {
        var srcResource = AlignerTest.class.getResource("/data/align/heapSource.txt");
        if (srcResource == null) {
            throw new IllegalStateException("Test resource heapSource.txt not found");
        }
        String srcFile = srcResource.getFile();
        Language srcLang = new Language(Locale.ENGLISH);
        var trgResource = AlignerTest.class.getResource("/data/align/heapTarget.txt");
        if (trgResource == null) {
            throw new IllegalStateException("Test resource heapTarget.txt not found");
        }
        String trgFile = trgResource.getFile();
        Language trgLang = new Language(Locale.JAPANESE);
        Aligner aligner = new Aligner(srcFile, srcLang, trgFile, trgLang);

        aligner.comparisonMode = ComparisonMode.HEAPWISE;
        assertHeapResult(aligner.align());

        assertTrue("Parsewise not supported for these files", assertModeUnsupported(aligner, ComparisonMode.PARSEWISE));
        assertTrue("ID not supported for these files", assertModeUnsupported(aligner, ComparisonMode.ID));
    }

    @Test
    public void testAlignerParseMode() throws Exception {
        var srcResource = AlignerTest.class.getResource("/data/align/parseSource.txt");
        if (srcResource == null) {
            throw new IllegalStateException("Test resource parseSource.txt not found");
        }
        String srcFile = srcResource.getFile();
        Language srcLang = new Language(Locale.ENGLISH);
        var trgResource = AlignerTest.class.getResource("/data/align/parseTarget.txt");
        if (trgResource == null) {
            throw new IllegalStateException("Test resource parseTarget.txt not found");
        }
        String trgFile = trgResource.getFile();
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

        assertTrue("ID not supported for these files", assertModeUnsupported(aligner, ComparisonMode.ID));
    }

    @Test
    public void testAlignerIDMode() throws Exception {
        var srcResource = AlignerTest.class.getResource("/data/align/idSource.properties");
        if (srcResource == null) {
            throw new IllegalStateException("Test resource idSource.properties not found");
        }
        String srcFile = srcResource.getFile();
        Language srcLang = new Language(Locale.ENGLISH);
        var trgResource = AlignerTest.class.getResource("/data/align/idTarget.properties");
        if (trgResource == null) {
            throw new IllegalStateException("Test resource idTarget.properties not found");
        }
        String trgFile = trgResource.getFile();
        Language trgLang = new Language(Locale.JAPANESE);
        Aligner aligner = new Aligner(srcFile, srcLang, trgFile, trgLang);

        // Aligner will default to ID alignment when possible, so load and then change mode.
        aligner.loadFiles();
        aligner.comparisonMode = ComparisonMode.HEAPWISE;
        assertHeapResult(aligner.align());

        assertTrue("Parsewise not supported for these files", assertModeUnsupported(aligner, ComparisonMode.PARSEWISE));

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

    private boolean assertModeUnsupported(Aligner aligner, ComparisonMode mode) throws Exception {
        aligner.comparisonMode = mode;
        try {
            aligner.align();
            return false;
        } catch (UnsupportedOperationException ignored) {
            return true;
        }
    }

    @Test
    public void testWritePairsToTMX_writesExpectedTMX() throws Exception {
        // Given
        Language srcLang = new Language(Locale.ENGLISH); // "en"
        Language trgLang = new Language(Locale.JAPANESE); // "ja"
        Aligner aligner = new Aligner(null, srcLang, null, trgLang);

        List<Entry<String, String>> pairs = new ArrayList<>();
        pairs.add(new AbstractMap.SimpleImmutableEntry<>("Hello world", "こんにちは世界"));
        pairs.add(new AbstractMap.SimpleImmutableEntry<>("Goodbye", "さようなら"));

        File out = File.createTempFile("aligner-test", ".tmx");
        out.deleteOnExit();

        // When
        aligner.writePairsToTMX(out, pairs);

        // Then
        String tmx = Files.readString(out.toPath(), StandardCharsets.UTF_8);
        // Header should include srclang
        assertTrue("TMX header should include srclang=\"en\"", tmx.contains("srclang=\"en\""));
        // Should contain the written segments
        assertTrue(tmx.contains("Hello world"));
        assertTrue(tmx.contains("こんにちは世界"));
        assertTrue(tmx.contains("Goodbye"));
        assertTrue(tmx.contains("さようなら"));
        // Level 2 TMX should use xml:lang attributes for both languages
        assertTrue(tmx.contains("xml:lang=\"en\""));
        assertTrue(tmx.contains("xml:lang=\"ja\""));
    }

    @Test
    public void testWritePairsToTMX_missingLanguageThrows() throws Exception {
        Aligner aligner = new Aligner(null, null, null, null);
        File out = File.createTempFile("aligner-test-missing-lang", ".tmx");
        out.deleteOnExit();
        try {
            aligner.writePairsToTMX(out, List.of(new AbstractMap.SimpleImmutableEntry<>("a", "b")));
            fail("Expected IllegalStateException when languages are not set");
        } catch (IllegalStateException expected) {
            // ok
        }
    }

    @Test
    public void testDoAlign_withBeads_returnsAlignedBeads() {
        // Given: an aligner with deterministic settings
        Aligner aligner = new Aligner(null, new Language(Locale.ENGLISH), null, new Language(Locale.JAPANESE));
        // Ensure algorithm/calculator/counter are set (defaults already set by constructor)
        // Use short strings with distinct lengths to encourage 1:1 monotonic alignment
        List<MutableBead> beads = new ArrayList<>();
        beads.add(new MutableBead("a", "A"));
        beads.add(new MutableBead("bb", "BB"));
        beads.add(new MutableBead("ccc", "CCC"));

        // When
        List<MutableBead> result = aligner.doAlign(beads);

        // Then: expect 1:1 aligned beads in the same order
        assertEquals(3, result.size());
        assertEquals(List.of("a"), result.get(0).sourceLines);
        assertEquals(List.of("A"), result.get(0).targetLines);
        assertEquals(List.of("bb"), result.get(1).sourceLines);
        assertEquals(List.of("BB"), result.get(1).targetLines);
        assertEquals(List.of("ccc"), result.get(2).sourceLines);
        assertEquals(List.of("CCC"), result.get(2).targetLines);
    }

    @Test
    public void testDoAlign_missingSettingsThrows() {
        Aligner aligner = new Aligner(null, new Language(Locale.ENGLISH), null, new Language(Locale.JAPANESE));
        // Invalidate settings
        aligner.algorithmClass = null;
        try {
            aligner.doAlign(List.of(new MutableBead("x", "y")));
            fail("Expected IllegalStateException when required settings are not set");
        } catch (IllegalStateException expected) {
            // ok
        }
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
