/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.custommonkey.xmlunit.XMLTestCase;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.util.LFileCopy;
import org.xml.sax.InputSource;

/**
 * Base class for test filter parsing.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public abstract class TestFilterBase extends XMLTestCase {
    protected File outFile = new File(System.getProperty("java.io.tmpdir"),
            "OmegaT filter test - " + getClass().getSimpleName());

    protected List<String> parse(AbstractFilter filter, String filename)
            throws Exception {
        final List<String> result = new ArrayList<String>();

        filter.parseFile(new File(filename), null,
                new TreeMap<String, String>(), new IParseCallback() {
                    public void addEntry(String id, String source,
                            String translation, boolean isFuzzy,
                            String comment, IFilter filter) {
                        if (source.length() > 0)
                            result.add(source);
                    }

                    public void addFileTMXEntry(String source,
                            String translation) {
                    }
                });

        return result;
    }

    protected List<String> parse(AbstractFilter filter, String filename,
            Map<String, String> options) throws Exception {
        final List<String> result = new ArrayList<String>();

        filter.parseFile(new File(filename), null, options,
                new IParseCallback() {
                    public void addEntry(String id, String source,
                            String translation, boolean isFuzzy,
                            String comment, IFilter filter) {
                        if (source.length() > 0)
                            result.add(source);
                    }

                    public void addFileTMXEntry(String source,
                            String translation) {
                    }
                });

        return result;
    }

    protected void parse2(final AbstractFilter filter, final String filename,
            final Map<String, String> result,
            final Map<String, String> legacyTMX) throws Exception {

        filter.parseFile(new File(filename), null,
                new TreeMap<String, String>(), new IParseCallback() {
                    public void addEntry(String id, String source,
                            String translation, boolean isFuzzy,
                            String comment, IFilter filter) {
                        String segTranslation = isFuzzy ? null : translation;
                        result.put(source, segTranslation);
                        if (translation != null) {
                            // Add systematically the TU as a legacy TMX
                            String tmxSource = isFuzzy ? "["
                                    + filter.getFuzzyMark() + "] " + source
                                    : source;
                            addFileTMXEntry(tmxSource, translation);
                        }
                    }

                    public void addFileTMXEntry(String source,
                            String translation) {
                        legacyTMX.put(source, translation);
                    }
                });
    }

    protected void translate(AbstractFilter filter, String filename)
            throws Exception {
        filter.translateFile(new File(filename), null, null, outFile, null,
                new TreeMap<String, String>(), new ITranslateCallback() {
                    public String getTranslation(String id, String source) {
                        return source;
                    }
                });
    }
    
    protected void align(IFilter filter, String in, String out,
            IAlignCallback callback) throws Exception {
        File inFile = new File("test/data/filters/" + in);
        File outFile = new File("test/data/filters/" + out);
        filter.alignFile(inFile, null, outFile, null,
                new TreeMap<String, String>(), callback);
    }

    protected void translateText(AbstractFilter filter, String filename)
            throws Exception {
        translate(filter, filename);
        compareBinary(new File(filename), outFile);
    }

    protected void translateXML(AbstractFilter filter, String filename)
            throws Exception {
        translate(filter, filename);
        compareXML(new File(filename), outFile);
    }

    protected void compareBinary(File f1, File f2) throws Exception {
        ByteArrayOutputStream d1 = new ByteArrayOutputStream();
        LFileCopy.copy(f1, d1);

        ByteArrayOutputStream d2 = new ByteArrayOutputStream();
        LFileCopy.copy(f2, d2);

        assertEquals(d1.size(), d2.size());
        byte[] a1 = d1.toByteArray();
        byte[] a2 = d2.toByteArray();
        for (int i = 0; i < d1.size(); i++) {
            assertEquals(a1[i], a2[i]);
        }
    }

    protected void compareXML(File f1, File f2) throws Exception {
        compareXML(f1.toURI().toURL(), f2.toURI().toURL());
    }

    protected void compareXML(URL f1, URL f2) throws Exception {
        assertXMLEqual(new InputSource(f1.toExternalForm()), new InputSource(f2
                .toExternalForm()));
    }
}
