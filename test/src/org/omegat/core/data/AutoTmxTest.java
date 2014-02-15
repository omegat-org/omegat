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
import java.util.List;

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

        TMXEntry e1 = autoTMX.getEntries().get(0);
        checkListValues(e1.xICE, "11");
        checkListValues(e1.x100PC);

        TMXEntry e2 = autoTMX.getEntries().get(1);
        checkListValues(e2.xICE, "12");
        checkListValues(e2.x100PC, "10");

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
        checkTranslation(ste10, "Modifier", null, "10");
        checkTranslation(ste11, "Edition", "11", null);
        checkTranslation(ste12, "Modifier", "12", null);
    }

    SourceTextEntry createSTE(String id, String source) {
        EntryKey ek = new EntryKey("file", source, id, null, null, null);
        return new SourceTextEntry(ek, 0, null, null, new ArrayList<ProtectedPart>());
    }

    void checkListValues(List<String> list, String... values) {
        if (list == null && values.length == 0) {
            return;
        }
        assertEquals(list.size(), values.length);
        for (int i = 0; i < values.length; i++) {
            assertEquals(list.get(i), values[i]);
        }
    }

    void checkTranslation(SourceTextEntry ste, String expectedTranslation, String expectedXICE,
            String expectedX100PC) {
        TMXEntry e = p.getTranslationInfo(ste);
        assertTrue(e.isTranslated());
        assertEquals(expectedTranslation, e.translation);
        if (expectedXICE == null) {
            assertNull(e.xICE);
        } else {
            assertEquals(1, e.xICE.size());
            assertEquals(expectedXICE, e.xICE.get(0));
        }
        if (expectedX100PC == null) {
            assertNull(e.x100PC);
        } else {
            assertEquals(1, e.x100PC.size());
            assertEquals(expectedX100PC, e.x100PC.get(0));
        }
    }
}
