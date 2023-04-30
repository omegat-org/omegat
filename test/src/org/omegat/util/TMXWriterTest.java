/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProjectTest;
import org.omegat.filters.TestFilterBase;

/**
 * @author Alex Buloichik
 */
public class TMXWriterTest extends TestFilterBase {

    @Before
    public final void setUp() {
        XMLUnit.setControlEntityResolver(TMXReader2.TMX_DTD_RESOLVER);
        XMLUnit.setTestEntityResolver(TMXReader2.TMX_DTD_RESOLVER);
        XMLUnit.setIgnoreWhitespace(true);
    }

    @After
    public final void tearDown() throws Exception {
        XMLUnit.setControlEntityResolver(null);
        XMLUnit.setTestEntityResolver(null);
    }

    @Test
    public void testWriteInvalidChars() throws Exception {
        String in = "";
        in += (char) 0x00;
        in += (char) 0x01;
        in += (char) 0x02;
        in += (char) 0x18;
        in += (char) 0x19;
        in += (char) 0xD8FF;
        in += (char) 0xFFFE;
        in += (char) 0x12FFFF;

        TMXWriter2 wr = new TMXWriter2(outFile, new Language("en-US"), new Language("be-BY"), false, true,
                false);
        wr.writeEntry(in, "test", RealProjectTest.createEmptyTMXEntry(), null);
        wr.close();

        load(new ArrayList<String>(), null, false, false);
    }

    @Test
    public void testLevel2write() throws Exception {
        TMXWriter2 wr = new TMXWriter2(outFile, new Language("en-US"), new Language("be-BY"), false, true,
                false);

        wr.writeEntry("source", "target", RealProjectTest.createEmptyTMXEntry(), null);
        wr.writeEntry("1<a1/>2", "zz", RealProjectTest.createEmptyTMXEntry(), null);
        wr.writeEntry("3<a1>4</a1>5", "zz", RealProjectTest.createEmptyTMXEntry(), null);
        wr.writeEntry("6<a1>7", "zz", RealProjectTest.createEmptyTMXEntry(), null);

        wr.close();

        compareTMX(outFile, new File("test/data/tmx/test-save-tmx14.tmx"));
    }

    @Test
    public void testLevel2reads() throws Exception {
        final List<String> sources = new ArrayList<String>();

        // patch for 'OmegaT' tmx
        setCreationTool(new File("test/data/tmx/test-save-tmx14.tmx"), "OmegaT", outFile);
        load(sources, null, true, false);
        assertEquals(4, sources.size());
        assertEquals("source", sources.get(0));
        assertEquals("1<a1/>2", sources.get(1));
        assertEquals("3<a1>4</a1>5", sources.get(2));
        assertEquals("6<a1>7", sources.get(3));

        // patch for 'ext' tmx
        setCreationTool(new File("test/data/tmx/test-save-tmx14.tmx"), "ext", outFile);

        // extLevel2 = false; useSlash = false
        load(sources, null, false, false);
        assertEquals(4, sources.size());
        assertEquals("source", sources.get(0));
        assertEquals("12", sources.get(1));
        assertEquals("345", sources.get(2));
        assertEquals("67", sources.get(3));

        // extLevel2 = true; useSlash = false
        load(sources, null, true, false);
        assertEquals(4, sources.size());
        assertEquals("source", sources.get(0));
        assertEquals("1<a0>2", sources.get(1));
        assertEquals("3<a0>4</a0>5", sources.get(2));
        assertEquals("6<a0>7", sources.get(3));

        // extLevel2 = true; useSlash = true
        load(sources, null, true, true);
        assertEquals(4, sources.size());
        assertEquals("source", sources.get(0));
        assertEquals("1<a0/>2", sources.get(1));
        assertEquals("3<a0>4</a0>5", sources.get(2));
        // This last seg has an <it> tag with @pos="begin",
        // which should be treated as a beginning tag even when
        // useSlash = true so that we can match after segmenting,
        // such as in TmxComplianceTests#testImport2A:
        // "First <b0>sentence. Second</b0> sentence." -> ["First
        // <b0>sentence.", "Second</b0> sentence."]
        assertEquals("6<a0>7", sources.get(3));
    }

    @Test
    public void testEOLwrite() throws Exception {
        String eol = TMXWriter2.lineSeparator;
        TMXWriter2 wr = new TMXWriter2(outFile, new Language("en-US"), new Language("be-BY"), false, true,
                false);
        wr.writeEntry("source", "tar\nget", RealProjectTest.createEmptyTMXEntry(), null);
        wr.close();

        StringBuilder text = new StringBuilder();
        try (Reader rd = new InputStreamReader(Files.newInputStream(outFile.toPath()),
                StandardCharsets.UTF_8)) {
            char[] buffer = new char[512];
            while (true) {
                int len = rd.read(buffer);
                if (len < 0) {
                    break;
                }
                text.append(buffer, 0, len);
            }
        }
        assertTrue(text.toString().contains("tar" + eol + "get"));

        final List<String> trs = new ArrayList<>();
        load(null, trs, true, false);
        assertTrue(trs.get(0).contains("tar\nget"));
    }

    private void setCreationTool(File in, String tool, File out) throws Exception {
        XPathExpression exprTool = XPathFactory.newInstance().newXPath().compile("/tmx/header/@creationtool");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(TMXReader2.TMX_DTD_RESOLVER);

        Document doc = builder.parse(in);

        Node n = (Node) exprTool.evaluate(doc, XPathConstants.NODE);
        n.setNodeValue(tool);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(out);
        Source input = new DOMSource(doc);

        transformer.transform(input, output);
    }

    private void load(final List<String> sources, final List<String> translations, boolean extLevel2,
            boolean useSlash) throws Exception {
        if (sources != null) {
            sources.clear();
        }
        if (translations != null) {
            translations.clear();
        }
        new TMXReader2().readTMX(outFile, new Language("en-US"), new Language("be-BY"), false, false,
                extLevel2, useSlash, new TMXReader2.LoadCallback() {
                    public boolean onEntry(TMXReader2.ParsedTu tu, TMXReader2.ParsedTuv tuvSource,
                            TMXReader2.ParsedTuv tuvTarget, boolean isParagraphSegtype) {
                        if (sources != null) {
                            sources.add(tuvSource.text);
                        }
                        if (translations != null) {
                            translations.add(tuvTarget.text);
                        }
                        return true;
                    }
                });
    }

    static int tagNumber = 0;
    static boolean closeTag, standAloneTag;

    /**
     * ProjectProperties successor for create project without directory.
     */
    protected static class ProjectPropertiesTest extends ProjectProperties {

    }
}
