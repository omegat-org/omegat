/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
               2024 Hiroshi Miura
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

package org.omegat.languagetools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.languagetool.JLanguageTool;

import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.languagetools.LanguageToolWrapper.LanguageToolMarker;
import org.omegat.util.Language;

/**
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class FalseFriendsTest extends TestCore {

    @BeforeClass
    public static void setUpClass() {
        PluginUtils.loadPlugins(Collections.emptyMap());
        JLanguageTool.setClassBrokerBroker(new LanguageClassBroker());
        JLanguageTool.setDataBroker(new LanguageDataBroker());
        LanguageManager.registerLTLanguage("en", "org.languagetool.language.English");
        LanguageManager.registerLTLanguage("en-US", "org.languagetool.language.AmericanEnglish");
        LanguageManager.registerLTLanguage("en-CA", "org.languagetool.language.CanadianEnglish");
        LanguageManager.registerLTLanguage("pl-PL", "org.languagetool.language.Polish");
    }

    @Before
    public final void setUp() {
        final ProjectProperties props = new ProjectProperties() {
            public Language getSourceLanguage() {
                return new Language("en");
            }

            public Language getTargetLanguage() {
                return new Language("pl");
            }
        };
        Core.setProject(new NotLoadedProject() {
            public boolean isProjectLoaded() {
                return true;
            }
            public ProjectProperties getProjectProperties() {
                return props;
            }
        });
        LanguageToolWrapper.setBridgeFromCurrentProject();
    }

    @Test
    public void testExecute() throws Exception {
        LanguageToolMarker marker = new LanguageToolMarker() {
            public boolean isEnabled() {
                return true;
            };
        };

        List<Mark> marks = marker.getMarksForEntry(null, "This is abnegation.", "To jest abnegacja.", true);
        assertEquals(1, marks.size());
        assertTrue(marks.get(0).toolTipText.contains("slovenliness"));
    }

    @Test
    public void testRemoveRules() throws Exception {
        LanguageToolMarker marker = new LanguageToolMarker() {
            public boolean isEnabled() {
                return true;
            };
        };

        List<Mark> marks = marker.getMarksForEntry(null, "This is some long text without translation.", "",
                true);
        assertEquals(0, marks.size());

        marks = marker.getMarksForEntry(null, "This is text with the same translation.",
                "This is text with the same translation.", true);
        assertEquals(0, marks.size());
    }
}
