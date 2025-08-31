/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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

package org.omegat.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.custommonkey.xmlunit.XMLUnit;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.omegat.core.Core;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.text.TextFilter;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Base methods for TMX compliance tests.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public abstract class TmxComplianceBase {

    static final Pattern RE_SEG = Pattern.compile("(<seg>.+</seg>)");

    protected File outFile;

    @Rule
    public TestName name = new TestName();

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
        Core.setSegmenter(new Segmenter(SRX.getDefault()));

        outFile = new File("build/testdata/" + getClass().getSimpleName() + "-" + name.getMethodName() + ".out");
        if (outFile.exists()) {
            if (!outFile.delete()) {
                throw new IOException("Can't remove " + outFile.getAbsolutePath());
            }
        } else {
            Files.createDirectories(outFile.getParentFile().toPath());
        }
    }

    protected void compareTexts(File f1, String charset1, File f2, String charset2) throws Exception {
        List<String> lines1 = readTextFile(f1, charset1);
        List<String> lines2 = readTextFile(f2, charset2);

        assertEquals(lines1.size(), lines2.size());
        for (int i = 0; i < lines1.size(); i++) {
            assertEquals(lines1.get(i), lines2.get(i));
        }
    }

    protected List<String> readTextFile(File f, String charset) throws Exception {
        BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));

        int ch;

        // BOM (byte order mark) bugfix
        rd.mark(1);
        ch = rd.read();
        if (ch != 0xFEFF) {
            rd.reset();
        }

        List<String> result = new ArrayList<>();
        String s;
        while ((s = rd.readLine()) != null) {
            result.add(s);
        }

        rd.close();

        return result;
    }

    protected void translateAndCheckTextUsingTmx(String fileTextIn, String inCharset, String fileTMX,
            String fileTextOut, String outCharset, String sourceLang, String targetLang,
            Map<String, TMXEntry> tmxPatch) throws Exception {
        TextFilter f = new TextFilter();
        Map<String, String> c = new TreeMap<>();
        c.put(TextFilter.OPTION_SEGMENT_ON, TextFilter.SEGMENT_BREAKS);

        ProjectProperties props = new TestProjectProperties(sourceLang, targetLang);
        translateUsingTmx(f, c, fileTextIn, inCharset, fileTMX, outCharset, props, tmxPatch);
        compareTexts(new File("test/data/tmx/TMXComplianceKit/" + fileTextOut), outCharset, outFile,
                outCharset);
    }

    protected void translateUsingTmx(IFilter filter, Map<String, String> config, final String fileTextIn,
            String inCharset, String fileTMX, String outCharset, ProjectProperties props,
            Map<String, TMXEntry> tmxPatch) throws Exception {
        final ProjectTMX tmx = new ProjectTMX(orphanedCallback);
               tmx.load(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(),
                       new File("test/data/tmx/TMXComplianceKit/" + fileTMX), Core.getSegmenter());
        if (tmxPatch != null) {
            tmx.defaults.putAll(tmxPatch);
        }

        FilterContext fc = new FilterContext(props);
        fc.setInEncoding(inCharset);
        fc.setOutEncoding(outCharset);
        ITranslateCallback cb = new TranslateEntry(props) {
            @Override
            protected String getSegmentTranslation(String id, int segmentIndex, String segmentSource,
                    String prevSegment, String nextSegment, String path) {
                TMXEntry e = tmx.getDefaultTranslation(segmentSource);
                assertNotNull(e);
                return e.translation;
            }

            @Override
            String getCurrentFile() {
                return fileTextIn;
            }
        };
        filter.translateFile(new File("test/data/tmx/TMXComplianceKit/" + fileTextIn), outFile, config, fc,
                cb);
    }

    protected List<String> loadTexts(final IFilter filter, final File sourceFile, final String inCharset,
            final FilterContext context, final Map<String, String> config) throws Exception {

        final List<String> result = new ArrayList<>();

        IParseCallback callback = new IParseCallback() {
            @Override
            public void addEntry(String id, String source, String translation, boolean isFuzzy,
                    String comment, String path, IFilter filter, List<ProtectedPart> protectedParts) {
                String[] props = comment == null ? null : new String[] { SegmentProperties.COMMENT, comment };
                addEntryWithProperties(id, source, translation, isFuzzy, props, path, filter, protectedParts);
            }

            @Override
            public void addEntryWithProperties(String id, String source, String translation, boolean isFuzzy,
                    String[] props, String path, IFilter filter, List<ProtectedPart> protectedParts) {
                result.addAll(Core.getSegmenter().segment(context.getSourceLang(), source, null, null));
            }

            public void linkPrevNextSegments() {
            }
        };
        filter.parseFile(sourceFile, config, context, callback);

        return result;
    }

    protected void align(IFilter filter, File sourceFile, String inCharset, File translatedFile,
            String outCharset, ProjectProperties props) throws Exception {

        FilterContext fc = new FilterContext(props);
        fc.setInEncoding(inCharset);
        fc.setOutEncoding(outCharset);

        RealProject.AlignFilesCallback callback = new RealProject.AlignFilesCallback(props);

        filter.alignFile(sourceFile, translatedFile, null, fc, callback);

        ProjectTMX tmx = new ProjectTMX(orphanedCallback);
        tmx.load(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), outFile,
                Core.getSegmenter());

        for (Map.Entry<EntryKey, ITMXEntry> en : callback.data.entrySet()) {
            if (en.getValue() instanceof TMXEntry) {
                tmx.defaults.put(en.getKey().sourceText, (TMXEntry) en.getValue());
            } else if (en.getValue() instanceof PrepareTMXEntry) {
                tmx.defaults.put(en.getKey().sourceText, new TMXEntry(en.getValue(), true, null));
            }
        }

        tmx.exportTMX(props, outFile, false, false, true);
    }

    protected Set<String> readTmxSegments(File tmx) throws Exception {
        Set<String> entries;
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(tmx), StandardCharsets.UTF_8))) {
            String s;
            entries = new TreeSet<>();
            while ((s = rd.readLine()) != null) {
                Matcher m = RE_SEG.matcher(s);
                if (m.find()) {
                    entries.add(m.group(1));
                }
            }
        }
        return entries;
    }

    protected void compareTMX(File orig, File created, int segmentsCount) throws Exception {
        Set<String> tmxOrig = readTmxSegments(orig);
        Set<String> tmxCreated = readTmxSegments(created);
        assertEquals(segmentsCount, tmxCreated.size());
        assertEquals(tmxOrig.size(), tmxCreated.size());

        List<String> listOrig = new ArrayList<>(tmxOrig);
        List<String> listCreated = new ArrayList<>(tmxCreated);
        for (int i = 0; i < listOrig.size(); i++) {
            XMLUnit.compareXML(listOrig.get(i), listCreated.get(i));
        }
    }

    protected ProjectTMX.CheckOrphanedCallback orphanedCallback = new ProjectTMX.CheckOrphanedCallback() {
        public boolean existSourceInProject(String src) {
            return true;
        }

        public boolean existEntryInProject(EntryKey key) {
            return true;
        }

        public void clear() {
            // do nothing
        }
    };

    protected static class TestProjectProperties extends ProjectProperties {
        public TestProjectProperties(String sourceLang, String targetLang) {
            setSupportDefaultTranslations(true);
            setSourceLanguage(sourceLang);
            setTargetLanguage(targetLang);
        }
    }
}
