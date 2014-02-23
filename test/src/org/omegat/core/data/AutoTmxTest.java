/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.tokenizer.LuceneFrenchTokenizer;

/**
 * Tests for tm/auto/ tmx loading with replace translations.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class AutoTmxTest extends TestCase {
    RealProject p;

    @Test
    public void test1() throws Exception {
        ProjectProperties props = new ProjectProperties();
        props.setSourceLanguage("en");
        props.setTargetLanguage("fr");
        props.setTargetTokenizer(LuceneFrenchTokenizer.class);
        File file = new File("test/data/autotmx/auto1.tmx");
        ExternalTMX autoTMX = new ExternalTMX(props, file, false, false);

        PrepareTMXEntry e1 = autoTMX.getEntries().get(0);
        checkListValues(e1, ProjectTMX.PROP_XICE, "11");

        PrepareTMXEntry e2 = autoTMX.getEntries().get(1);
        checkListValues(e2, ProjectTMX.PROP_XICE, "12");
        checkListValues(e2, ProjectTMX.PROP_X100PC, "10");

        Core.initializeConsole(new HashMap<String, String>());

        p = new RealProject(props);
        p.projectTMX = new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), false, new File(
                "test/data/autotmx/project1.tmx"), new ProjectTMX.CheckOrphanedCallback() {
            public boolean existSourceInProject(String src) {
                return true;
            }

            public boolean existEntryInProject(EntryKey key) {
                return true;
            }
        });
        SourceTextEntry ste10, ste11, ste12;
        p.allProjectEntries.add(ste10 = createSTE("10", "Edit"));
        p.allProjectEntries.add(ste11 = createSTE("11", "Edit"));
        p.allProjectEntries.add(ste12 = createSTE("12", "Edit"));
        p.importHandler = new ImportFromAutoTMX(p, p.allProjectEntries);
        p.appendFromAutoTMX(autoTMX);
        checkTranslation(ste10, "Modifier", TMXEntry.ExternalLinked.x100PC);
        checkTranslation(ste11, "Edition", TMXEntry.ExternalLinked.xICE);
        checkTranslation(ste12, "Modifier", TMXEntry.ExternalLinked.xICE);
    }

    SourceTextEntry createSTE(String id, String source) {
        EntryKey ek = new EntryKey("file", source, id, null, null, null);
        return new SourceTextEntry(ek, 0, null, null, new ArrayList<ProtectedPart>());
    }

    void checkListValues(PrepareTMXEntry en, String propType, String propValue) {
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
