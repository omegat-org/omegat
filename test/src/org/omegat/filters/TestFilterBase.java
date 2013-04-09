/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.filters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters2.Shortcuts;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.util.LFileCopy;
import org.omegat.util.Language;
import org.omegat.util.TMXReader2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Base class for test filter parsing.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public abstract class TestFilterBase extends TestCore {
    protected FilterContext context = new FilterContext(new Language("en"), new Language("be"), false);

    protected File outFile;

    protected void setUp() throws Exception {
        super.setUp();

        Core.initializeConsole(new TreeMap<String, String>());
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));

        outFile = new File("build/testdata/OmegaT_test-" + getClass().getName() + "-" + getName());
        outFile.getParentFile().mkdirs();
    }

    protected List<String> parse(AbstractFilter filter, String filename) throws Exception {
        final List<String> result = new ArrayList<String>();

        filter.parseFile(new File(filename), new TreeMap<String, String>(), context, new IParseCallback() {
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, IFilter filter) {
                addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
            }

            public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                    String path, IFilter filter, Shortcuts shortcutDetails) {
                if (source.length() > 0)
                    result.add(source);
            }

            public void linkPrevNextSegments() {
            }
        });

        return result;
    }

    protected List<String> parse(AbstractFilter filter, String filename, Map<String, String> options)
            throws Exception {
        final List<String> result = new ArrayList<String>();

        filter.parseFile(new File(filename), options, context, new IParseCallback() {
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, IFilter filter) {
                addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
            }

            public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                    String path, IFilter filter, Shortcuts shortcutDetails) {
                if (source.length() > 0)
                    result.add(source);
            }

            public void linkPrevNextSegments() {
            }
        });

        return result;
    }

    protected void parse2(final AbstractFilter filter, final String filename,
            final Map<String, String> result, final Map<String, String> legacyTMX) throws Exception {

        filter.parseFile(new File(filename), new TreeMap<String, String>(), context, new IParseCallback() {
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, IFilter filter) {
                addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
            }

            public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                    String path, IFilter filter, Shortcuts shortcutDetails) {
                String segTranslation = isFuzzy ? null : translation;
                result.put(source, segTranslation);
                if (translation != null) {
                    // Add systematically the TU as a legacy TMX
                    String tmxSource = isFuzzy ? "[" + filter.getFuzzyMark() + "] " + source : source;
                    addFileTMXEntry(tmxSource, translation);
                }
            }

            public void addFileTMXEntry(String source, String translation) {
                legacyTMX.put(source, translation);
            }

            public void linkPrevNextSegments() {
            }
        });
    }

    protected List<ParsedEntry> parse3(AbstractFilter filter, String filename, Map<String, String> options)
            throws Exception {
        final List<ParsedEntry> result = new ArrayList<ParsedEntry>();

        filter.parseFile(new File(filename), options, context, new IParseCallback() {
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, IFilter filter) {
                addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
            }
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, String path, IFilter filter, Shortcuts shortcutDetails) {
                if (source.length() == 0) {
                    return;
                }
                ParsedEntry e = new ParsedEntry();
                e.id = id;
                e.source = source;
                e.translation = translation;
                e.isFuzzy = isFuzzy;
                e.comment = comment;
                e.path = path;
                result.add(e);
            }

            public void addFileTMXEntry(String source, String translation) {
            }

            public void linkPrevNextSegments() {
            }
        });

        return result;
    }

    protected void translate(AbstractFilter filter, String filename) throws Exception {
        filter.translateFile(new File(filename), outFile, new TreeMap<String, String>(), context,
                new ITranslateCallback() {
                    public String getTranslation(String id, String source, String path) {
                        return source;
                    }

                    public String getTranslation(String id, String source) {
                        return source;
                    }

                    public void linkPrevNextSegments() {
                    }

                    public void setPass(int pass) {
                    }
                });
    }

    protected void align(IFilter filter, String in, String out, IAlignCallback callback) throws Exception {
        File inFile = new File("test/data/filters/" + in);
        File outFile = new File("test/data/filters/" + out);
        filter.alignFile(inFile, outFile, new TreeMap<String, String>(), context, callback);
    }

    protected void translateText(AbstractFilter filter, String filename) throws Exception {
        translate(filter, filename);
        compareBinary(new File(filename), outFile);
    }

    protected void translateXML(AbstractFilter filter, String filename) throws Exception {
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

    /**
     * Remove version and toolname, then compare.
     */
    protected void compareTMX(File f1, File f2) throws Exception {
        XPathExpression exprVersion = XPathFactory.newInstance().newXPath()
                .compile("/tmx/header/@creationtoolversion");
        XPathExpression exprTool = XPathFactory.newInstance().newXPath().compile("/tmx/header/@creationtool");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(TMXReader2.TMX_DTD_RESOLVER);

        Document doc1 = builder.parse(f1);
        Document doc2 = builder.parse(f2);

        Node n;

        n = (Node) exprVersion.evaluate(doc1, XPathConstants.NODE);
        n.setNodeValue("");

        n = (Node) exprVersion.evaluate(doc2, XPathConstants.NODE);
        n.setNodeValue("");

        n = (Node) exprTool.evaluate(doc1, XPathConstants.NODE);
        n.setNodeValue("");

        n = (Node) exprTool.evaluate(doc2, XPathConstants.NODE);
        n.setNodeValue("");

        assertXMLEqual(doc1, doc2);
    }

    protected void compareXML(File f1, File f2) throws Exception {
        compareXML(f1.toURI().toURL(), f2.toURI().toURL());
    }

    protected void compareXML(URL f1, URL f2) throws Exception {
        assertXMLEqual(new InputSource(f1.toExternalForm()), new InputSource(f2.toExternalForm()));
    }

    protected static class ParsedEntry {
        String id;
        String source;
        String translation;
        boolean isFuzzy;
        String comment;
        String path;
    }

    protected IProject.FileInfo loadSourceFiles(IFilter filter, String file) throws Exception {
        ProjectPropertiesTest props = new ProjectPropertiesTest();
        RealProjectTest p = new RealProjectTest(props);
        return p.loadSourceFiles(filter, file);
    }

    protected IProject.FileInfo fi;
    protected int fiCount;

    protected void checkMultiStart(IProject.FileInfo fi, String file) {
        this.fi = fi;
        fiCount = 0;
        for (SourceTextEntry ste : fi.entries) {
            assertEquals(file, ste.getKey().file);
            assertEquals(ste.getSrcText(), ste.getKey().sourceText);
        }
    }

    protected void checkMultiEnd() {
        assertEquals(fiCount, fi.entries.size());
    }

    protected void checkMulti(String sourceText, String id, String path, String prev, String next,
            String comment) {
        assertEquals(new EntryKey(fi.filePath, sourceText, id, prev, next, path), fi.entries.get(fiCount)
                .getKey());
        assertEquals(comment, fi.entries.get(fiCount).getComment());
        fiCount++;
    }

    protected SourceTextEntry checkMultiNoPrevNext(String sourceText, String id, String path, String comment) {
        SourceTextEntry ste = fi.entries.get(fiCount);
        assertEquals(path, ste.getKey().path);
        assertEquals(id, ste.getKey().id);
        assertEquals(sourceText, ste.getKey().sourceText);
        assertEquals(comment, ste.getComment());
        fiCount++;
        return ste;
    }

    protected void skipMulti() {
        fiCount++;
    }

    /**
     * ProjectProperties successor for create project without directory.
     */
    protected static class ProjectPropertiesTest extends ProjectProperties {

    }

    /**
     * RealProject successor for load file testing only.
     */
    protected static class RealProjectTest extends RealProject {
        protected FilterContext context = new FilterContext(new Language("en"), new Language("be"), false);

        public RealProjectTest(ProjectProperties props) {
            super(props);
        }

        public FileInfo loadSourceFiles(IFilter filter, String file) throws Exception {
            Core.setProject(this);

            Set<String> existSource = new HashSet<String>();
            Set<EntryKey> existKeys = new HashSet<EntryKey>();

            LoadFilesCallback loadFilesCallback = new LoadFilesCallback(existSource, existKeys);

            FileInfo fi = new FileInfo();
            fi.filePath = file;

            loadFilesCallback.setCurrentFile(fi);

            filter.parseFile(new File(file), new TreeMap<String, String>(), context, loadFilesCallback);

            loadFilesCallback.fileFinished();

            return fi;
        }
    }

    protected List<AlignedEntry> al;
    protected int alCount;

    protected void checkAlignStart(TestAlignCallback calback) {
        this.al = calback.entries;
        alCount = 0;
    }

    protected void checkAlignEnd() {
        assertEquals(alCount, al.size());
    }

    protected void checkAlign(String id, String source, String translation, String path) {
        AlignedEntry en = al.get(alCount);
        assertEquals(id, en.id);
        assertEquals(source, en.source);
        assertEquals(translation, en.translation);
        assertEquals(path, en.path);
        alCount++;
    }
    
    protected void checkAlignById(String id, String source, String translation, String path) {
        for(AlignedEntry en:al) {
            if (id.equals(en.id)) {
                assertEquals(source, en.source);
                assertEquals(translation, en.translation);
                assertEquals(path, en.path);
                alCount++;
                return;
            }
        }
        fail();
    }

    protected static class TestAlignCallback implements IAlignCallback {
        public List<AlignedEntry> entries = new ArrayList<AlignedEntry>();

        public void addTranslation(String id, String source, String translation, boolean isFuzzy,
                String path, IFilter filter) {
            AlignedEntry en = new AlignedEntry();
            en.id = id;
            en.source = source;
            en.translation = translation;
            en.path = path;
            entries.add(en);
        }
    }

    protected static class AlignedEntry {
        public String id;
        public String source;
        public String translation;
        public String path;
    }
}
