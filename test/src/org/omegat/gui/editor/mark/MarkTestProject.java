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

import org.omegat.core.data.IProject;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.Language;

import java.io.File;

public class MarkTestProject extends NotLoadedProject implements IProject {

    private final ProjectTMX projectTMX = new ProjectTMX();

    public MarkTestProject(File testFile, Segmenter segmenter) throws Exception {
        projectTMX.load(new Language("en"), new Language("fr"), true, testFile, segmenter);
    }

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
        };
    }

    @Override
    public TMXEntry getTranslationInfo(SourceTextEntry ste) {
        if (ste == null) {
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

}
