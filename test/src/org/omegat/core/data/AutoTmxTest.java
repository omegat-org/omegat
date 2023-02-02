/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.tokenizer.LuceneFrenchTokenizer;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Tests for tm/auto/ tmx loading with replace translations.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class AutoTmxTest {
    RealProject p;

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
    }

    @Test
    public void autoFromIceAnd100PC() throws Exception {
        ExternalTMX autoTMX = prepareExternalTMX(new File("test/data/autotmx/project1.tmx"),
                new File("test/data/autotmx/auto1.tmx"));

        ITMXEntry e1 = autoTMX.getEntries().get(0);
        checkListValues(e1, ProjectTMX.PROP_XICE, "11");

        ITMXEntry e2 = autoTMX.getEntries().get(1);
        checkListValues(e2, ProjectTMX.PROP_XICE, "12");
        checkListValues(e2, ProjectTMX.PROP_X100PC, "10");

        Core.initializeConsole(new HashMap<String, String>());

        SourceTextEntry ste10, ste11, ste12;
        p.allProjectEntries.add(ste10 = createSTE("10", "Edit"));
        p.allProjectEntries.add(ste11 = createSTE("11", "Edit"));
        p.allProjectEntries.add(ste12 = createSTE("12", "Edit"));
        p.importHandler = new ImportFromAutoTMX(p, p.allProjectEntries);
        p.appendFromAutoTMX(autoTMX, false);
        checkTranslation(ste10, "Modifier", TMXEntry.ExternalLinked.x100PC);
        checkTranslation(ste11, "Edition", TMXEntry.ExternalLinked.xICE);
        checkTranslation(ste12, "Modifier", TMXEntry.ExternalLinked.xICE);
    }

    @Test
    public void enforcedMatchesOverrideDefault() throws Exception {
        ExternalTMX enforceTMX = prepareExternalTMX(new File("test/data/enforcetmx/project1.tmx"),
                new File("test/data/enforcetmx/enforce1.tmx"));

        SourceTextEntry ste;
        p.allProjectEntries.add(ste = createSTE(null, "Edit"));
        checkTranslation(ste, "foobar", null);
        p.importHandler = new ImportFromAutoTMX(p, p.allProjectEntries);
        p.appendFromAutoTMX(enforceTMX, true);
        checkTranslation(ste, "bizbaz", TMXEntry.ExternalLinked.xENFORCED);
    }

    /**
     * Test that an alternate translation matching an id is correctly applied when
     * the memory is in tm/enforce.
     */
    @Test
    public void enforcedAlternativeMatches() throws Exception {
        ExternalTMX enforceTMX = prepareExternalTMX(new File("test/data/enforcetmx/project1.tmx"),
                new File("test/data/enforcetmx/alternative.tmx"));

        SourceTextEntry ste = createSTE("1_0", "Edit");
        p.allProjectEntries.add(ste);
        checkTranslation(ste, "foobar", null);
        p.importHandler = new ImportFromAutoTMX(p, p.allProjectEntries);
        p.appendFromAutoTMX(enforceTMX, true);
        checkTranslation(ste, "alternative", TMXEntry.ExternalLinked.xENFORCED);
    }

    /**
     * Test that an alternate translation matching an id is correctly applied when
     * the memory is in tm/auto.
     */
    @Test
    public void autoAlternativeMatches() throws Exception {
        ExternalTMX autoTMX = prepareExternalTMX(new File("test/data/enforcetmx/project1.tmx"),
                new File("test/data/enforcetmx/alternative.tmx"));

        SourceTextEntry ste = createSTE("1_0", "Edit");
        p.allProjectEntries.add(ste);
        checkTranslation(ste, "foobar", null);
        p.importHandler = new ImportFromAutoTMX(p, p.allProjectEntries);
        p.appendFromAutoTMX(autoTMX, false);
        checkTranslation(ste, "alternative", TMXEntry.ExternalLinked.xAUTO);
    }

    private ExternalTMX prepareExternalTMX(File projectFile, File tmxFile) throws Exception {
        ProjectProperties props = new ProjectProperties();
        props.setSourceLanguage("en");
        props.setTargetLanguage("fr");
        props.setTargetTokenizer(LuceneFrenchTokenizer.class);

        Core.initializeConsole(new HashMap<String, String>());

        p = new RealProject(props);
        p.projectTMX = new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), false, projectFile,
                new ProjectTMX.CheckOrphanedCallback() {
                    @Override
                    public boolean existSourceInProject(String src) {
                        return true;
                    }

                    @Override
                    public boolean existEntryInProject(EntryKey key) {
                        return true;
                    }
                });

        return new ExternalTMFactory.TMXLoader(tmxFile).setDoSegmenting(props.isSentenceSegmentingEnabled())
                .load(props.getSourceLanguage(), props.getTargetLanguage());
    }

    private SourceTextEntry createSTE(String id, String source) {
        EntryKey ek = new EntryKey("file", source, id, null, null, null);
        return new SourceTextEntry(ek, 0, null, null, new ArrayList<ProtectedPart>());
    }

    void checkListValues(ITMXEntry en, String propType, String propValue) {
        assertTrue(en.hasPropValue(propType, propValue));
    }

    void checkTranslation(SourceTextEntry ste, String expectedTranslation,
            TMXEntry.ExternalLinked expectedExternalLinked) {
        TMXEntry e = p.getTranslationInfo(ste);
        assertTrue(e.isTranslated());
        assertEquals(expectedTranslation, e.translation);
        assertEquals(expectedExternalLinked, e.linked);
    }
}
