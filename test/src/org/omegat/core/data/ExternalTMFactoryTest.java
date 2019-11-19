/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.mozlang.MozillaLangFilter;
import org.omegat.filters2.po.PoFilter;
import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

public class ExternalTMFactoryTest extends TestCore {

    private Language sourceLang = new Language("en");
    private Language targetLang = new Language("pl");

    @Before
    public final void setUp() {
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        FilterMaster.setFilterClasses(Arrays.asList(PoFilter.class, MozillaLangFilter.class));
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
        ProjectProperties props = new ProjectProperties() {
            public Language getSourceLanguage() {
                return sourceLang;
            }

            public Language getTargetLanguage() {
                return targetLang;
            }

            @Override
            public boolean isSentenceSegmentingEnabled() {
                return true;
            }
        };
        Core.setProject(new NotLoadedProject() {
            @Override
            public ProjectProperties getProjectProperties() {
                return props;
            }
        });
    }

    @Test
    public void testLoadTMX() throws Exception {
        File tmxFile = new File("test/data/tmx/resegmenting.tmx");
        sourceLang = new Language("en");
        targetLang = new Language("fr");

        assertTrue(ExternalTMFactory.isSupported(tmxFile));

        ExternalTMX tmx = ExternalTMFactory.load(tmxFile);

        assertEquals(2, tmx.getEntries().size());
        assertEquals("This is test.", tmx.getEntries().get(0).source);
        assertEquals("Ceci est un test.", tmx.getEntries().get(0).translation);
        assertEquals("Just a test.", tmx.getEntries().get(1).source);
        assertEquals("Juste un test.", tmx.getEntries().get(1).translation);
    }

    @Test
    public void testLoadPO() throws Exception {
        File tmxFile = new File("test/data/filters/po/file-POFilter-be-utf8.po");
        sourceLang = new Language("en");
        targetLang = new Language("be");

        assertTrue(ExternalTMFactory.isSupported(tmxFile));

        ExternalTMX tmx = ExternalTMFactory.load(tmxFile);

        assertEquals(1013, tmx.getEntries().size());
        assertEquals("Choose syntax highlighting", tmx.getEntries().get(0).source);
        assertEquals(
                "\u0412\u044B\u043B\u0443\u0447\u044D\u043D\u044C\u043D\u0435 "
                        + "&\u043A\u043E\u043B\u0435\u0440\u0430\u043C "
                        + "\u0441\u044B\u043D\u0442\u0430\u043A\u0441\u044B\u0441\u0443",
                tmx.getEntries().get(0).translation);
        assertEquals("< Auto >", tmx.getEntries().get(1).source);
        assertEquals("\u041F\u0440\u0430 \u043F\u0440\u0430\u0433\u0440\u0430\u043C\u0443",
                tmx.getEntries().get(1).translation);
    }

    @Test
    public void testLoadMozillaLang() throws Exception {
        File tmxFile = new File("test/data/filters/MozillaLang/file-MozillaLangFilter-de.lang");
        sourceLang = new Language("en");
        targetLang = new Language("de");

        assertTrue(ExternalTMFactory.isSupported(tmxFile));

        ExternalTMX tmx = ExternalTMFactory.load(tmxFile);

        assertEquals(33, tmx.getEntries().size());
        assertEquals("Download %s for Android in your language", tmx.getEntries().get(0).source);
        assertEquals("Laden Sie %s f\u00FCr Android in Ihrer Sprache herunter", tmx.getEntries().get(0).translation);
        assertEquals("Download %s in your language", tmx.getEntries().get(1).source);
        assertEquals("Laden Sie %s in Ihrer Sprache herunter", tmx.getEntries().get(1).translation);
    }

    /**
     * Test for RFE #1452
     *
     * @see <a href="https://sourceforge.net/p/omegat/feature-requests/1452/">RFE
     *      #1452</a>
     */
    @Test
    public void testFuzzyMultipleTuv() throws Exception {
        TestPreferencesInitializer.init();
        Preferences.setPreference(Preferences.EXT_TMX_KEEP_FOREIGN_MATCH, false);

        File tmxFile = new File("test/data/tmx/test-multiple-tuv.tmx");
        sourceLang = new Language("en");
        targetLang = new Language("fr");

        assertTrue(ExternalTMFactory.isSupported(tmxFile));

        ExternalTMX tmx = ExternalTMFactory.load(tmxFile);

        // Only 5 FR translations
        assertEquals(5, tmx.getEntries().size());

        List<PrepareTMXEntry> matchingEntries = tmx.getEntries().stream().filter(t -> t.source.equals("Hello World!"))
                .collect(Collectors.toList());
        assertEquals(3, matchingEntries.size());
        
        // Set the EXT_TMX_KEEP_FOREIGN_MATCH prop
        Preferences.setPreference(Preferences.EXT_TMX_KEEP_FOREIGN_MATCH, true);
        tmx = ExternalTMFactory.load(tmxFile);

        // All foreign translations are present
        assertEquals(11, tmx.getEntries().size());

        matchingEntries = tmx.getEntries().stream().filter(t -> t.source.equals("Hello World!"))
                .collect(Collectors.toList());
        assertEquals(7, matchingEntries.size());

        matchingEntries = tmx.getEntries().stream().filter(t -> t.source.equals("This is an english sentence."))
                .collect(Collectors.toList());
        assertEquals(2, matchingEntries.size());
        PrepareTMXEntry entry = matchingEntries.get(0);
        assertEquals("DE", entry.getPropValue(ExternalTMFactory.TMXLoader.PROP_TARGET_LANGUAGE));
        assertEquals("true", entry.getPropValue(ExternalTMFactory.TMXLoader.PROP_FOREIGN_MATCH));

        entry = matchingEntries.get(1);
        assertEquals("ES", entry.getPropValue(ExternalTMFactory.TMXLoader.PROP_TARGET_LANGUAGE));
        assertEquals("true", entry.getPropValue(ExternalTMFactory.TMXLoader.PROP_FOREIGN_MATCH));
    }
}
