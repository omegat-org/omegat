/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.w3c.dom.Document;

import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SegmentProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.Language;
import org.omegat.util.TMXReader2;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

/**
 * Base class for testing filter parsing.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public abstract class TestFilterBase extends TestCore {
    protected FilterContext context = new FilterContext(new Language("en"), new Language("be"), false)
            .setTargetTokenizerClass(DefaultTokenizer.class);

    protected File outFile;

    @Rule
    public TestName name = new TestName();

    @Before
    public final void setUpFilterBase() throws Exception {
        Core.initializeConsole(Collections.emptyMap());
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
        Core.setProject(new TestProject(new ProjectPropertiesTest()));

        outFile = new File("build/testdata/OmegaT_test-" + getClass().getName() + "-" + name.getMethodName());
        outFile.getParentFile().mkdirs();
    }

    /**
     * Helper function for testing the parseFile method of a given filter without options;
     * returns a list of source segments that the
     * filter-under-test finds in the given file and returns to the IParseCallback.addEntry method.
     * NB: Id, comments, fuzzyness, path, properties etc is all ignored.
     *
     * @param filter the filter to test
     * @param filename the file to use as input for the filter
     * @return list of source segments
     * @throws Exception when the filter throws an exception on parseFile.
     */
    protected List<String> parse(AbstractFilter filter, String filename) throws Exception {
        final List<String> result = new ArrayList<String>();

        filter.parseFile(new File(filename), Collections.emptyMap(), context, new IParseCallback() {
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, IFilter filter) {
                addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
            }

            public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                    String path, IFilter filter, List<ProtectedPart> protectedParts) {
                String[] props = comment == null ? null : new String[] { SegmentProperties.COMMENT, comment };
                addEntryWithProperties(id, source, translation, isFuzzy, props, path, filter, protectedParts);
            }

            public void addEntryWithProperties(String id, String source, String translation,
                    boolean isFuzzy, String[] props, String path,
                    IFilter filter, List<ProtectedPart> protectedParts) {
                if (!source.isEmpty()) {
                    result.add(source);
                }
            }

            public void linkPrevNextSegments() {
            }
        });

        return result;
    }

    /**
     * Helper function for testing the parseFile method of a given filter using some options;
     * returns a list of source segments that
     * the filter-under-test finds in the given file and returns to the IParseCallback.addEntry method.
     * NB: Id, comments, fuzzyness, path, properties etc is all ignored.
     *
     * @param filter the filter to test
     * @param filename the file to use as input for the filter
     * @param options the filter options/config to use
     * @return list of source segments
     * @throws Exception when the filter throws an exception on parseFile.
     */
    protected List<String> parse(AbstractFilter filter, String filename, Map<String, String> options)
            throws Exception {
        final List<String> result = new ArrayList<String>();

        filter.parseFile(new File(filename), options, context, new IParseCallback() {
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, IFilter filter) {
                addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
            }

            public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                    String path, IFilter filter, List<ProtectedPart> protectedParts) {
                String[] props = comment == null ? null : new String[] { SegmentProperties.COMMENT, comment };
                addEntryWithProperties(id, source, translation, isFuzzy, props, path, filter, protectedParts);
            }

            public void addEntryWithProperties(String id, String source, String translation,
                    boolean isFuzzy, String[] props, String path,
                    IFilter filter, List<ProtectedPart> protectedParts) {
                if (!source.isEmpty()) {
                    result.add(source);
                }
            }

            public void linkPrevNextSegments() {
            }
        });

        return result;
    }

    /**
     * Helper function for testing the parseFile method of a given filter without options.
     * The given 'result' map is filled with
     * key=source, value=translation as the filter-under-test finds in the given file and returns to the
     * IParseCallback.addEntry method, if the translation is not fuzzy.
     * The given legacyTMX map is filled too, but also including fuzzy translations,
     * where <code>key=[&lt;fuzzyMark&gt;] source</code>
     * NB: Id, comments, path, properties etc is all ignored.
     *
     * @param filter the filter to test
     * @param filename the file to use as input for the filter
     * @param result a map to fill by the filter with key=source, value=translation
     * @param legacyTMX a map to fill by the filter with key=source or key=[&lt;fuzzyMark&gt;] source, value=translation
     * @throws Exception when the filter throws an exception on parseFile.
     */
    protected void parse2(final AbstractFilter filter, final String filename,
            final Map<String, String> result, final Map<String, String> legacyTMX) throws Exception {

        filter.parseFile(new File(filename), Collections.emptyMap(), context, new IParseCallback() {
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, IFilter filter) {
                addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
            }

            public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                    String path, IFilter filter, List<ProtectedPart> protectedParts) {
                String[] props = comment == null ? null : new String[] { SegmentProperties.COMMENT, comment };
                addEntryWithProperties(id, source, translation, isFuzzy, props, path, filter, protectedParts);
            }

            @Override
            public void addEntryWithProperties(String id, String source, String translation,
                    boolean isFuzzy, String[] props, String path,
                    IFilter filter, List<ProtectedPart> protectedParts) {
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

    /**
     * Helper function for testing the parseFile method of a given filter using some options;
     * returns a list of ParsedEntry with the
     * attributes that the filter-under-test finds in the given file and returns to the IParseCallback.addEntry method.
     * @param filter the filter to test
     * @param filename the file to use as input for the filter
     * @param options the filter options/config to use
     * @return list of found information
     * @throws Exception when the filter throws an exception on parseFile.
     */
    protected List<ParsedEntry> parse3(AbstractFilter filter, String filename, Map<String, String> options)
            throws Exception {
        final List<ParsedEntry> result = new ArrayList<>();

        filter.parseFile(new File(filename), options, context, new IParseCallback() {
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, IFilter filter) {
                addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
            }
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, String path, IFilter filter, List<ProtectedPart> protectedParts) {
                String[] props = comment == null ? null : new String[] { SegmentProperties.COMMENT, comment };
                addEntryWithProperties(id, source, translation, isFuzzy, props, path, filter, protectedParts);
            }

            @Override
            public void addEntryWithProperties(String id, String source, String translation,
                    boolean isFuzzy, String[] props, String path,
                    IFilter filter, List<ProtectedPart> protectedParts) {
                if (source.isEmpty()) {
                    return;
                }
                ParsedEntry e = new ParsedEntry();
                e.id = id;
                e.source = source;
                e.translation = translation;
                e.isFuzzy = isFuzzy;
                e.props = props;
                e.path = path;
                result.add(e);
            }

            public void linkPrevNextSegments() {
            }
        });

        return result;
    }

    /**
     * Helper function for testing the translateFile method of a filter. Translation equals the source.
     * Translation is written to {@link #outFile}.
     * @param filter the filter to test
     * @param filename the file to use as input for the filter
     * @throws Exception when the filter throws an exception
     */
    protected void translate(AbstractFilter filter, String filename) throws Exception {
        translate(filter, filename, Collections.emptyMap());
    }

    /**
     * Helper function for testing the translateFile method of a filter.
     * Translation equals the source when monolingual filter.
     * Translation is written to {@link #outFile}.
     * @param filter the filter to test
     * @param filename the file to use as input for the filter
     * @param config the filter options/config to use
     * @throws Exception when the filter throws an exception
     */
    protected void translate(AbstractFilter filter, String filename, Map<String, String> config) throws Exception {
        translate(filter, filename, config, Collections.emptyMap());
    }

    /**
     * Helper function for testing the translateFile method of a filter.
     * Translation equals the source when monolingual filter when no translation is found.
     * @param filter the filter to test
     * @param filename the file to use as input for the filter
     * @param config the filter options/config to use
     * @param translations expected translations.
     * @throws Exception when the filter throws an exception
     */
    protected void translate(AbstractFilter filter, String filename, Map<String, String> config,
                             Map<String, String> translations) throws Exception {
        translate(filter, filename, config, translations, filter.isBilingual());
    }

    /**
     * Helper function for testing the translateFile method of a filter.
     * Translation equals the source when allowBlank is false and translation is not found.
     * @param filter the filter to test
     * @param filename the file to use as input for the filter
     * @param config the filter options/config to use
     * @param translations expected translations.
     * @param allowBlank return null as translation when translation is not found.
     * @throws Exception when the filter throws an exception
     */
    protected void translate(AbstractFilter filter, String filename, Map<String, String> config,
                           Map<String, String> translations, boolean allowBlank) throws Exception {
        filter.translateFile(new File(filename), outFile, config, context,
                new ITranslateCallback() {
                    public String getTranslation(String id, String source, String path) {
                        String translation = translations.get(source);
                        if (translation == null && !allowBlank) {
                            return source;
                        }
                        return translation;
                    }

                    public String getTranslation(String id, String source) {
                        return getTranslation(id, source, null);
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
        filter.alignFile(inFile, outFile, Collections.emptyMap(), context, callback);
    }

    /**
     * Asserts if the filter translateFile method produces a binary identical file from the given file, when no
     * filter options/config given.
     *
     * @param filter The filter to test
     * @param filename the file to translate during the test
     * @throws Exception when the filter throws an exception.
     */
    protected void translateText(AbstractFilter filter, String filename) throws Exception {
        translateText(filter, filename, Collections.emptyMap());
    }
    /**
     * Asserts if the filter translateFile method produces a binary identical file from the given file under the given
     * filter options/config.
     * @param filter The filter to test
     * @param filename the file to translate during the test
     * @param config  the filter options/config
     * @throws Exception when the filter throws an exception.
     */

    protected void translateText(AbstractFilter filter, String filename, Map<String, String> config) throws Exception {
        translate(filter, filename, config);
        compareBinary(new File(filename), outFile);
    }

    /**
     * Tests if the filter translateFile method produces an identical XML file from the given file, when no
     * filter options/config given.
     *
     * @param filter The filter to test
     * @param filename the file to translate during the test
     * @throws Exception when the filter throws an exception.
     */
    protected void translateXML(AbstractFilter filter, String filename) throws Exception {
        translate(filter, filename);
        compareXML(new File(filename), outFile);
    }

    public static void compareBinary(File f1, File f2) throws Exception {
        ByteArrayOutputStream d1 = new ByteArrayOutputStream();
        FileUtils.copyFile(f1, d1);

        ByteArrayOutputStream d2 = new ByteArrayOutputStream();
        FileUtils.copyFile(f2, d2);

        assertEquals(d1.size(), d2.size());
        byte[] a1 = d1.toByteArray();
        byte[] a2 = d2.toByteArray();
        for (int i = 0; i < d1.size(); i++) {
            assertEquals(a1[i], a2[i]);
        }
    }

    /**
     * Remove a version and tool name, then compare.
     */
    protected void compareTMX(File f1, File f2) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(TMXReader2.TMX_DTD_RESOLVER);

        Document doc1 = builder.parse(f1);
        Document doc2 = builder.parse(f2);
        assertThat(doc1).and(doc2)
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                .withAttributeFilter(attr ->
                        !("creationtoolversion".equals(attr.getName()) || "creationtool".equals(attr.getName())))
                .ignoreWhitespace()
                .areIdentical();
    }

    protected void compareXML(File f1, File f2) throws Exception {
        compareXML(f1.toURI().toURL(), f2.toURI().toURL());
    }

    protected void compareXML(URL f1, URL f2) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        var doc1 = builder.parse(f1.toExternalForm());
        var doc2 = builder.parse(f2.toExternalForm());
        assertThat(doc1).and(doc2)
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                .areIdentical();
    }

    protected static class ParsedEntry {
        public String id;
        public String source;
        public String translation;
        public boolean isFuzzy;
        public String[] props;
        public String path;
    }

    protected TestFileInfo loadSourceFiles(IFilter filter, String file, Map<String, String> filterOptions)
            throws Exception {
        ProjectPropertiesTest props = new ProjectPropertiesTest();
        TestProject p = new TestProject(props);
        return p.loadSourceFiles(filter, file, filterOptions);
    }

    protected TestFileInfo loadSourceFiles(IFilter filter, String file) throws Exception {
        return loadSourceFiles(filter, file, Collections.emptyMap());
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
        assertEquals(new EntryKey(fi.filePath, sourceText, id, prev, next, path), fi.entries.get(fiCount).getKey());
        assertEquals(comment, fi.entries.get(fiCount).getComment());
        fiCount++;
    }

    protected void checkMultiProps(String sourceText, String id, String path, String prev, String next,
            String... props) {
        assertEquals(new EntryKey(fi.filePath, sourceText, id, prev, next, path),
                fi.entries.get(fiCount).getKey());
        List<String> expected = Arrays.asList(props);
        String[] actual = fi.entries.get(fiCount).getRawProperties();
        assertEquals(props.length, actual.length);
        for (int i = 0; i < actual.length; i += 2) {
            int keyIndex = expected.indexOf(actual[i]);
            assertNotEquals(keyIndex, -1);
            int valIndex = expected.indexOf(actual[i + 1]);
            assertEquals(keyIndex + 1, valIndex);
        }
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
        ProjectPropertiesTest() {
            super();
            setTargetTokenizer(DefaultTokenizer.class);
        }
    }

    /**
     * RealProject successor for load file testing only.
     */
    protected class TestProject extends RealProject {

        public TestProject(ProjectProperties props) {
            super(props);
        }

        public TestFileInfo loadSourceFiles(IFilter filter, String file, Map<String, String> filterOptions)
                throws Exception {
            Core.setProject(this);

            Set<String> existSource = new HashSet<String>();
            Set<EntryKey> existKeys = new HashSet<EntryKey>();
            Map<String, ExternalTMX> transMemories = new HashMap<>();

            LoadFilesCallback loadFilesCallback = new LoadFilesCallback(existSource, existKeys, transMemories);

            TestFileInfo fi = new TestFileInfo();
            fi.filePath = file;

            loadFilesCallback.setCurrentFile(fi);

            filter.parseFile(new File(file), filterOptions, context, loadFilesCallback);

            loadFilesCallback.fileFinished();

            if (!transMemories.isEmpty()) {
                fi.referenceEntries = transMemories.values().iterator().next();
            }

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
        for (AlignedEntry en : al) {
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

    public static class TestFileInfo extends FileInfo {
        public ExternalTMX referenceEntries;
    }
}
