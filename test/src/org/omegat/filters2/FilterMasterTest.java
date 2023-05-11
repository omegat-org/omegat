package org.omegat.filters2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.util.JaxbXmlMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import gen.core.filters.Filter.Option;
import gen.core.filters.Filters;

public class FilterMasterTest {
    private File tempFilter;

    @Before
    public final void setUpCore() throws Exception {
        tempFilter = Files.createTempFile(FilterMaster.FILE_FILTERS, null).toFile();

        FilterMaster.setFilterClasses(Arrays.asList(new Class[] { org.omegat.filters3.xml.xhtml.XHTMLFilter.class }));
    }

    @After
    public final void tearDown() throws Exception {
        FileUtils.delete(tempFilter);
    }

    @Test
    public void deserializeEmptyOption() throws JsonMappingException, JsonProcessingException {
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

        XmlMapper mapper = JaxbXmlMapper.getXmlMapper();
        Filters filtersConfig = mapper.readValue(filters, Filters.class);

        List<Option> option = filtersConfig.getFilters().get(0).getOption();
        assertFalse("Desierialized <option/> is empty", option.isEmpty());
        assertNull(option.get(0).getName());
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
        assertEquals("More than one filter found in serialized filters", 1, loadedFilters.getFilters().size());
        fm = new FilterMaster(loadedFilters);
        loadFile(fm);

        List<Option> option = fm.getConfig().getFilters().get(0).getOption();
        assertNotNull("Filter option is not null", option);
        assertFalse("Filter option is not empty", option.isEmpty());
        assertNull(option.get(0).getName());
    }

    private static void loadFile(FilterMaster fm) throws IOException, TranslationException, Exception {
        fm.loadFile("foo.html", new FilterContext(new ProjectProperties(new File("test-filemaster"))),
                new IParseCallback() {

                    @Override
                    public void addEntryWithProperties(String id, String source, String translation, boolean isFuzzy,
                            String[] props, String path, IFilter filter, List<ProtectedPart> protectedParts) {
                        /* empty */
                    }

                    @Override
                    public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                            String path, IFilter filter, List<ProtectedPart> protectedParts) {
                        /* empty */
                    }

                    @Override
                    public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                            IFilter filter) {
                        /* empty */
                    }

                    @Override
                    public void linkPrevNextSegments() {
                        /* empty */
                    }
                });
    }
}
