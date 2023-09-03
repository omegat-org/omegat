/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2023 Briac Pilpré
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

package org.omegat.filters2.master;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.TranslationException;

import gen.core.filters.Filter.Option;
import gen.core.filters.Filters;

public class FilterMasterTest {
    private File tempFilter;

    @Before
    public final void setUpCore() throws Exception {
        tempFilter = Files.createTempFile(FilterMaster.FILE_FILTERS, null).toFile();

        // Workaround for Java 17 or later support of JAXB.
        // See https://sourceforge.net/p/omegat/feature-requests/1682/#12c5
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        FilterMaster.setFilterClasses(
                Arrays.asList(new Class<?>[] { org.omegat.filters3.xml.xhtml.XHTMLFilter.class }));
    }

    @After
    public final void tearDown() throws Exception {
        FileUtils.delete(tempFilter);
    }

    @Test
    public void deserializeEmptyOption() throws JsonProcessingException, JAXBException {
        String filters = "<?xml version='1.0' encoding='UTF-8'?>" + //
                "<filters removeTags='true' removeSpacesNonseg='true' preserveSpaces='false' ignoreFileContext='false'>"
                + //
                "  <filter className='org.omegat.filters3.xml.xhtml.XHTMLFilter' enabled='true'>" + //
                "    <files sourceFilenameMask='*.html' targetFilenamePattern='${filename}'/>" + //
                "    <files sourceFilenameMask='*.xhtml' targetFilenamePattern='${filename}'/>" + //
                "    <files sourceFilenameMask='*.xht' targetFilenamePattern='${filename}'/>" + //
                "    <option/>" + //
                "  </filter>" + //
                "</filters>";

        // Check equality between jaxb unmarshaller and xml mapper
        Unmarshaller unm;
        Thread thread = Thread.currentThread();
        ClassLoader classLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(Filters.class.getClassLoader());
            JAXBContext configCtx = JAXBContext.newInstance(Filters.class);
            unm = configCtx.createUnmarshaller();
            thread.setContextClassLoader(classLoader);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
        Filters filtersConfig = (Filters) unm.unmarshal(new StringReader(filters));
        List<Option> option = filtersConfig.getFilters().get(0).getOption();
        assertFalse("Desierialized <option/> is empty", option.isEmpty());
        assertNull(option.get(0).getName());

        XmlMapper mapper = FilterMaster.getMapper();
        Filters filtersConfig2 = mapper.readValue(filters, Filters.class);
        List<Option> option2 = filtersConfig2.getFilters().get(0).getOption();
        assertFalse("Desierialized <option/> is empty", option2.isEmpty());
        assertNull(option2.get(0).getName());
    }

    @Test
    public void testFilterInitOption() throws Exception {

        Filters defaultFilters = FilterMaster.createDefaultFiltersConfig();
        defaultFilters.setIgnoreFileContext(false);
        defaultFilters.setPreserveSpaces(false);
        defaultFilters.setRemoveTags(true);
        defaultFilters.setRemoveSpacesNonseg(true);

        assertEquals("More than one filter found", 1, defaultFilters.getFilters().size());
        FilterMaster fm = new FilterMaster(defaultFilters);

        // Note that if we use `loadFile(fm)` here, the default configuration
        // would be OK for the rest of the test.

        FilterMaster.saveConfig(defaultFilters, tempFilter);
        assertTrue("Temp filters.xml file not created", tempFilter.exists());

        Filters loadedFilters = FilterMaster.loadConfig(tempFilter);
        assertEquals("More than one filter found in serialized filters", 1,
                loadedFilters.getFilters().size());
        fm = new FilterMaster(loadedFilters);
        loadFile(fm);

        List<Option> option = fm.getConfig().getFilters().get(0).getOption();
        assertNotNull("Filter option is not null", option);
        assertTrue("Filter option is not empty", option.isEmpty());
    }

    private static void loadFile(FilterMaster fm) throws IOException, TranslationException, Exception {
        fm.loadFile("foo.html", new FilterContext(new ProjectProperties(new File("test-filemaster"))),
                new IParseCallback() {

                    @Override
                    public void addEntryWithProperties(String id, String source, String translation,
                            boolean isFuzzy, String[] props, String path, IFilter filter,
                            List<ProtectedPart> protectedParts) {
                        /* empty */
                    }

                    @Override
                    public void addEntry(String id, String source, String translation, boolean isFuzzy,
                            String comment, String path, IFilter filter, List<ProtectedPart> protectedParts) {
                        /* empty */
                    }

                    @Override
                    public void addEntry(String id, String source, String translation, boolean isFuzzy,
                            String comment, IFilter filter) {
                        /* empty */
                    }

                    @Override
                    public void linkPrevNextSegments() {
                        /* empty */
                    }
                });
    }
}
