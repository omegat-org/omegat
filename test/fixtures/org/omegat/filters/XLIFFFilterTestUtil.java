/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.ITranslateCallback;

public final class XLIFFFilterTestUtil {

    private XLIFFFilterTestUtil() {
    }

    /**
     * Test function to check translation of RFE1506 case.
     * <p>
     * Just return when pass the cases. Otherwise, raises assertion error.
     *
     * @param filter
     *            filter object
     * @param context
     *            filter context
     * @param outFile
     *            translated output from the filter.
     * @throws IOException
     *             when failed to read target file.
     */
    public static void checkXLiffTranslationRFE1506(IFilter filter, FilterContext context,
                                                    File target, File outFile, boolean optionNeedsTranslate)
            throws Exception {
        Map<String, String> config = new HashMap<>();
        if (optionNeedsTranslate) {
            config.put("changetargetstateneedsreviewtranslation", "true");
        }
        assertTrue(filter.isFileSupported(target, config, context));
        filter.translateFile(target, outFile, config, context, new ITranslateCallback() {
            public String getTranslation(String id, String source, String path) {
                if ("Create".equals(source)) {
                    return "\u4F5C\u6210";
                }
                if ("Emoji".equals(source)) {
                    return "\u7D75\u6587\u5B57";
                }
                return null; // not translated or already translated
            }

            public String getTranslation(String id, String source) {
                return getTranslation(id, source, "");
            }

            public void linkPrevNextSegments() {
            }

            public void setPass(int pass) {
            }
        });
        XmlMapper mapper = new XmlMapper();
        JsonNode nodes = mapper.readTree(outFile);
        assertNotNull(nodes);
        /*
         * expect: <xliff version="1.2"
         * xmlns="urn:oasis:names:tc:xliff:document:1.2">
         */
        assertEquals("1.2", nodes.get("version").asText());
        assertTrue(nodes.isContainerNode());
        nodes = nodes.findPath("file");
        assertEquals("92", nodes.get("id").asText());
        assertEquals("/22.txt", nodes.get("original").asText());
        assertEquals("en", nodes.get("source-language").asText());
        nodes = nodes.findPath("body");
        assertNotNull(nodes);
        JsonNode transUnits = nodes.findPath("trans-unit");
        assertNotNull(transUnits);
        for (JsonNode transUnit : transUnits) {
            String id = transUnit.get("id").asText();
            switch (id) {
            case "5078":
                /*
                 * expect: <trans-unit id="5078"> <source>1.0.1</source> <target
                 * state="needs-translation">1.0.1</target> </trans-unit>
                 */
                assertEquals("1.0.1", transUnit.get("source").asText());
                assertEquals("needs-translation", transUnit.get("target").get("state").asText());
                assertEquals("1.0.1", transUnit.get("target").get("").asText());
                break;
            case "5086":
                /*
                 * expect: <trans-unit id="5086" approved="yes">
                 * <source>foo</source> <target state="final">bar</target>
                 * </trans-unit>
                 */
                assertEquals("yes", transUnit.get("approved").asText());
                assertEquals("foo", transUnit.get("source").asText());
                assertEquals("final", transUnit.get("target").get("state").asText());
                assertEquals("bar", transUnit.get("target").get("").asText());
                break;
            case "5088":
                /*
                 * expect: <trans-unit id="5088" approved="yes">
                 * <source>Organization</source> <target
                 * state="needs-review-translation">&#x7D44;&#x7E54;</target>
                 * </trans-unit>
                 */
                assertEquals("yes", transUnit.get("approved").asText());
                assertEquals("Organization", transUnit.get("source").asText());
                assertEquals("needs-review-translation", transUnit.get("target").get("state").asText());
                assertEquals("\u7D44\u7E54", transUnit.get("target").get("").asText());
                break;
            case "5090":
                /*
                 * expect in default: <trans-unit id="5090">
                 * <source>Create</source> <target
                 * state="translated">&#x4F5C;&#x6210;</target> </trans-unit>
                 *
                 * expect with option: <trans-unit id="5090">
                 * <source>Create</source> <target
                 * state="needs-review-translation">&#x4F5C;&#x6210;</target>
                 * </trans-unit>
                 */
                assertEquals("Create", transUnit.get("source").asText());
                if (optionNeedsTranslate) {
                    assertEquals("needs-review-translation", transUnit.get("target").get("state").asText());
                } else {
                    assertEquals("translated", transUnit.get("target").get("state").asText());
                }
                assertEquals("\u4F5C\u6210", transUnit.get("target").get("").asText());
                break;
            case "5128":
                /*
                 * expected: <trans-unit id="5128" approved="yes"> <source>
                 * Emoji</source> <target
                 * state="translated">&#x7D75;&#x6587;&#x5B57;</target>
                 * </trans-unit>
                 */
                assertEquals("yes", transUnit.get("approved").asText());
                assertEquals("Emoji", transUnit.get("source").asText());
                if (optionNeedsTranslate) {
                    assertEquals("needs-review-translation", transUnit.get("target").get("state").asText());
                } else {
                    assertEquals("translated", transUnit.get("target").get("state").asText());
                }
                assertEquals("\u7D75\u6587\u5B57", transUnit.get("target").get("").asText());
                break;
            }
        }
    }
}
