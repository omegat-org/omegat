/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2024 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.omegat.gui.editor.mark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.DataUtils;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.matching.NearString;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.Language;

public class ComesFromMTMarkerTest extends MarkerTestBase {

    private ProjectTMX projectTMX;
    private Path tmroot;
    private Path project;

    @Before
    public void preUp() throws Exception {
        tmroot = Paths.get("test/data/tmx/");
        project = tmroot.resolve("mt/mt1.tmx"); // should under 'mt'
        TestCoreInitializer.initEditor(editor);
        Segmenter segmenter =  new Segmenter(SRX.getDefault());
        Core.setSegmenter(segmenter);
        projectTMX = new ProjectTMX();
        projectTMX.load(new Language("en"), new Language("fr"), true,
                project.toFile(), segmenter);
        Core.setProject(new NotLoadedProject() {
            @Override
            public boolean isProjectLoaded() {
                return true;
            }

            @Override
            public ProjectProperties getProjectProperties() {
                return new ProjectProperties() {
                    @Override
                    public Language getSourceLanguage() {
                        return new Language("en");
                    }

                    @Override
                    public Language getTargetLanguage() {
                        return new Language("fr");
                    }

                    @Override
                    public String getTMRoot() {
                        return tmroot.toString();
                    }

                };
            }

            @Override
            public TMXEntry getTranslationInfo(SourceTextEntry ste) {
                if (ste == null || projectTMX == null) {
                    return EMPTY_TRANSLATION;
                }
                TMXEntry r = projectTMX.getMultipleTranslation(ste.getKey());
                if (r == null) {
                    r = projectTMX.getDefaultTranslation(ste.getSrcText());
                }
                if (r == null) {
                    r = EMPTY_TRANSLATION;
                }
                return r;
            }

        });
    }

    @Test
    public void testMarkersDisabled() throws Exception {
        IMarker marker = new ComesFromMTMarker();
        assertThat(marker.getMarksForEntry(null, null, null, true))
                .describedAs("marker should return null when ste, sourceText and translationText is null")
                .isNull();
    }

    @Test
    public void testMarkersNotActive() throws Exception {
        IMarker marker = new ComesFromMTMarker();
        final String sourceText = "source";
        final String targetText = "target";
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
        assertThat(marker.getMarksForEntry(ste, sourceText, targetText, false))
                .describedAs("marker should return null when ste is not active")
                .isNull();
    }

    @Test
    public void testMarkersMT() {
        ComesFromMTMarker marker = new ComesFromMTMarker();
        final String sourceText = "source";
        final String targetText = "target";
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
        marker.setMark(ste, targetText);
        List<Mark> result = marker.getMarksForEntry(ste, sourceText, targetText, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).startOffset);
        assertEquals(6, result.get(0).endOffset);
        assertEquals("TRANSLATION", result.get(0).entryPart.toString());
    }

    @Test
    public void testNearString() {
        PrepareTMXEntry entry = new PrepareTMXEntry();
        entry.source = "source";
        entry.translation = "target";
        entry.changer = "translator";
        entry.creator = "translator";
        entry.changeDate = 10000L;
        entry.creationDate = 10000L;
        final int score = 75;
        final byte[] nearData = null;
        EntryKey key = new EntryKey("file", entry.getSourceText(), "id", "prev", "next", "path");
        NearString near = new NearString(key, entry, NearString.MATCH_SOURCE.TM, false,
                new NearString.Scores(score, score, score), nearData, project.toString());
        assertThat(DataUtils.isFromMTMemory(near)).isTrue();
    }
}
