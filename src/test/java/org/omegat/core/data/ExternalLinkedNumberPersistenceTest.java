/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Round-trip persistence of the {@link TMXEntry.ExternalLinked#xNUMBER} marking
 * introduced for feature request #794: it must survive save and load through
 * the {@code x-number} TMX property, gated by the SAVE_AUTO_STATUS preference
 * exactly like {@code x-auto}.
 */
public class ExternalLinkedNumberPersistenceTest {

    private final ProjectTMX.CheckOrphanedCallback keepAll = new ProjectTMX.CheckOrphanedCallback() {
        public boolean existSourceInProject(String src) {
            return true;
        }

        public boolean existEntryInProject(EntryKey key) {
            return true;
        }

        public void clear() {
        }
    };

    @Before
    public void setUp() throws Exception {
        TestPreferencesInitializer.init();
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
    }

    private TMXEntry roundTrip(boolean saveAutoStatus) throws Exception {
        Preferences.setPreference(Preferences.SAVE_AUTO_STATUS, saveAutoStatus);

        ProjectProperties props = new ProjectProperties();
        props.setSupportDefaultTranslations(true);
        props.setSourceLanguage("de");
        props.setTargetLanguage("en");

        ProjectTMX tmx = new ProjectTMX(keepAll);
        tmx.defaults.put("5", new TMXEntryFactoryForTest().setSource("5").setTranslation("5")
                .setCreator("test").setCreationDate(1000L).setDefaultTranslation(true)
                .setExternalLinked(TMXEntry.ExternalLinked.xNUMBER).build());

        File out = File.createTempFile("xnumber", ".tmx");
        out.deleteOnExit();
        tmx.exportTMX(props, out, false, false, true);

        ProjectTMX reloaded = new ProjectTMX(keepAll);
        reloaded.load(props.getSourceLanguage(), props.getTargetLanguage(),
                props.isSentenceSegmentingEnabled(), out, Core.getSegmenter());
        return reloaded.getDefaultTranslation("5");
    }

    @Test
    public void xNumberSurvivesRoundTripWhenAutoStatusSaved() throws Exception {
        TMXEntry e = roundTrip(true);
        assertNotNull("the number-only default translation must reload", e);
        assertEquals("5", e.translation);
        assertEquals(TMXEntry.ExternalLinked.xNUMBER, e.linked);
    }

    @Test
    public void xNumberTagIsOmittedWhenAutoStatusNotSaved() throws Exception {
        // Like x-auto, the tag is only written when SAVE_AUTO_STATUS is set; the
        // translation itself still round-trips, only the marking is dropped.
        TMXEntry e = roundTrip(false);
        assertNotNull(e);
        assertEquals("5", e.translation);
        assertNull(e.linked);
    }
}
