/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.Language;
import org.omegat.util.TestPreferencesInitializer;

public class TmxSegmentationTest {

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
    }

    @Test
    public void testProjectTMX() throws Exception {
        ProjectProperties props = new ProjectProperties();
        props.setSupportDefaultTranslations(true);
        props.setSourceLanguage(new Language("en"));
        props.setTargetLanguage(new Language("fr"));
        props.setSentenceSegmentingEnabled(true);
        ProjectTMX tmx = new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), new File("test/data/tmx/resegmenting.tmx"),
                new ProjectTMX.CheckOrphanedCallback() {
                    public boolean existSourceInProject(String src) {
                        return true;
                    }

                    public boolean existEntryInProject(EntryKey key) {
                        return true;
                    }
                });

        assertEquals(2, tmx.defaults.size());
        assertEquals("Ceci est un test.", tmx.defaults.get("This is test.").translation);
        assertEquals("Juste un test.", tmx.defaults.get("Just a test.").translation);
    }

    @Test
    public void testExternalTMX() throws Exception {
        ExternalTMX tmx = new ExternalTMFactory.TMXLoader(new File("test/data/tmx/resegmenting.tmx"))
                .setDoSegmenting(true).load(new Language("en"), new Language("fr"));

        assertEquals(2, tmx.getEntries().size());
        assertEquals("This is test.", tmx.getEntries().get(0).source);
        assertEquals("Ceci est un test.", tmx.getEntries().get(0).translation);
        assertEquals("Just a test.", tmx.getEntries().get(1).source);
        assertEquals("Juste un test.", tmx.getEntries().get(1).translation);
    }
}
