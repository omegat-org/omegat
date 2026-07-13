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

package org.omegat.gui.numberconvert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.omegat.gui.numberconvert.NumberAutoConvertScanner.Proposal;
import org.omegat.util.NumberAutoConverter.DataType;

/**
 * Drives {@link NumberAutoConvertScanner} with the shared numeric
 * demonstration fixture: only its number-only fmt-* segments produce a
 * proposal (with the expected data type), and every prose segment produces
 * none, which is exactly the #794 scope.
 */
public class NumberAutoConvertScannerTest {

    private static final Locale DE = Locale.GERMANY;
    private static final Locale EN = Locale.US;

    @Test
    public void proposesTopConversionAndNullForProse() throws Exception {
        Map<String, DataType> expected = new HashMap<>();
        expected.put("fmt-cur-eur", DataType.CURRENCY);
        expected.put("fmt-date-de", DataType.DATE);
        expected.put("fmt-date-iso", DataType.DATE);
        expected.put("fmt-pct-de", DataType.PERCENT);
        expected.put("fmt-pct-fw", DataType.PERCENT);
        expected.put("fmt-ord-en", DataType.ORDINAL);
        expected.put("fmt-ord-de", DataType.ORDINAL);
        expected.put("fmt-time", DataType.TIME);

        // Resolved from the test classpath so tree reorganizations cannot break it.
        java.net.URL fixture = getClass().getResource("/data/editor/sort/numeric-sort-demo.xliff");
        assertNotNull("fixture missing on test classpath", fixture);
        File xliff = new File(fixture.toURI());
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xliff);
        NodeList units = doc.getElementsByTagName("trans-unit");

        int fmtSeen = 0;
        int proseSeen = 0;
        for (int i = 0; i < units.getLength(); i++) {
            Element u = (Element) units.item(i);
            String id = u.getAttribute("id");
            NodeList src = u.getElementsByTagName("source");
            String source = src.getLength() == 0 ? "" : src.item(0).getTextContent();

            Optional<Proposal> p = NumberAutoConvertScanner.propose(i, source, DE, EN,
                    EnumSet.allOf(DataType.class));

            if (expected.containsKey(id)) {
                fmtSeen++;
                assertTrue("expected proposal for " + id + " [" + source + "]", p.isPresent());
                assertEquals("type for " + id, expected.get(id), p.get().getType());
                assertEquals("segment number is passed through", i, p.get().getSegmentNumber());
            } else if (!id.startsWith("fmt-")) {
                proseSeen++;
                assertFalse("prose must not yield a proposal: " + id + " [" + source + "]", p.isPresent());
            }
        }
        assertEquals("all typed fmt units seen", expected.size(), fmtSeen);
        assertTrue("too few prose units seen: " + proseSeen, proseSeen >= 50);
    }
}
