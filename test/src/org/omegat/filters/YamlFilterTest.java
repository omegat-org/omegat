/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.filters;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.text.yaml.YamlFilter;

/**
 * Tests for {@link YamlFilter}.
 */
public class YamlFilterTest extends TestFilterBase {

    @Test
    public void testParse() throws Exception {
        List<String> entries = parse(new YamlFilter(), "test/data/filters/yaml/sample1.yaml");
        // Expected extraction order follows insertion order in YAML
        assertEquals(8, entries.size());
        assertEquals("Welcome", entries.get(0));
        assertEquals("Home", entries.get(1));
        assertEquals("About", entries.get(2));
        assertEquals("Contact", entries.get(3));
        assertEquals("(c) 2025 Example Co.", entries.get(4));
        assertEquals("/help", entries.get(5));
        assertEquals("/terms", entries.get(6));
        assertEquals("Enabled features", entries.get(7));
    }

    @Test
    public void testTranslate() throws Exception {
        // YAML formatting/quoting style may change when serialized by Jackson.
        // Binary equality is therefore not guaranteed and is explicitly a non-goal
        // per ADR 2025009. Instead of byte-by-byte comparison, parse both source
        // and produced YAML and assert that the sequence of extracted string
        // scalars (translation targets) is identical.

        YamlFilter filter = new YamlFilter();

        // Run the actual translate flow to produce outFile
        translate(filter, "test/data/filters/yaml/sample1.yaml");

        // Parse source and produced YAML and compare the list of textual scalars
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode src =
                mapper.readTree(new java.io.File("test/data/filters/yaml/sample1.yaml"));
        JsonNode trg = mapper.readTree(outFile);

        java.util.List<String> srcTexts = new java.util.ArrayList<>();
        java.util.List<String> trgTexts = new java.util.ArrayList<>();
        collectTextScalars(src, srcTexts);
        collectTextScalars(trg, trgTexts);

        assertEquals(srcTexts, trgTexts);
    }

    private static void collectTextScalars(JsonNode node, java.util.List<String> out) {
        if (node == null) return;
        if (node.isTextual()) {
            out.add(node.asText());
            return;
        }
        if (node.isArray()) {
            for (JsonNode it : node) {
                collectTextScalars(it, out);
            }
            return;
        }
        if (node.isObject()) {
            for (JsonNode it : node) {
                collectTextScalars(it, out);
            }
        }
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/yaml/sample1.yaml";
        IProject.FileInfo fi = loadSourceFiles(new YamlFilter(), f);

        checkMultiStart(fi, f);
        // id/path/prev/next/comment are null for this filter
        checkMulti("Welcome", null, null, null, null, null);
        checkMulti("Home", null, null, null, null, null);
        checkMulti("About", null, null, null, null, null);
        checkMulti("Contact", null, null, null, null, null);
        checkMulti("(c) 2025 Example Co.", null, null, null, null, null);
        checkMulti("/help", null, null, null, null, null);
        checkMulti("/terms", null, null, null, null, null);
        checkMulti("Enabled features", null, null, null, null, null);
        checkMultiEnd();
    }
}
