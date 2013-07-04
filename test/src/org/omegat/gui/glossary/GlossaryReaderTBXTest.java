/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
package org.omegat.gui.glossary;

import java.io.File;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.TestCore;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.util.Language;

/**
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class GlossaryReaderTBXTest extends TestCore {
    public void testRead() throws Exception {
        Core.setProject(new NotLoadedProject() {
            public ProjectProperties getProjectProperties() {
                try {
                    return new ProjectProperties(new File("stub")) {
                        @Override
                        public Language getSourceLanguage() {
                            return new Language("en");
                        }

                        @Override
                        public Language getTargetLanguage() {
                            return new Language("hu");
                        }
                    };
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        List<GlossaryEntry> g = GlossaryReaderTBX.read(new File(
                "test/data/glossaries/sampleTBXfile.tbx"));
        assertEquals(1, g.size());
        assertEquals("alpha smoothing factor", g.get(0).getSrcText());
        assertEquals("hu translation", g.get(0).getLocText());
    }
}
