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
package org.omegat.core.statistics;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.statistics.dso.FileData;
import org.omegat.core.statistics.dso.StatCount;
import org.omegat.core.statistics.dso.StatsResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;

import static org.xmlunit.assertj3.XmlAssert.assertThat;

public class StatsResultTest {

    private DocumentBuilder builder;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = factory.newDocumentBuilder();
    }

    @Test
    public void testStatsResultXML() throws Exception {
        StatsResult result = new StatsResult("testProject", "", "English", "French", "/tmp/source");
        //
        EntryKey entryKey = new EntryKey("test", "List of sections in %s", "id1", null, null, null);
        SourceTextEntry ste = new SourceTextEntry(entryKey, 1, null, "source translation",
                Collections.emptyList());
        FileData fileNumber = new FileData();
        fileNumber.filename = "file1.txt";
        fileNumber.total.add(new StatCount(ste));
        result.getCounts().add(0, fileNumber);
        //
        String actual = result.getXmlData();
        //
        URL f1 = Paths.get("test/data/statistics/stats-result-1.xml").toUri().toURL();
        Document expected = builder.parse(f1.toExternalForm());
        Document actualParsed = builder.parse(new InputSource(new StringReader(actual)));
        assertThat(actualParsed).and(expected)
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.or(ElementSelectors.byNameAndText,
                        ElementSelectors.byNameAndAllAttributes, ElementSelectors.byName)))
                .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()).ignoreWhitespace()
                .withNodeFilter(node -> !"date".equals(node.getNodeName())).ignoreChildNodesOrder()
                .areIdentical();
    }
}
